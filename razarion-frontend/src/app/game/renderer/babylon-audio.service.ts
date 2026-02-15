import {Injectable} from "@angular/core";
import type {AbstractSound, AudioEngineV2, StaticSoundBuffer} from "@babylonjs/core";
import {CreateAudioEngineAsync, CreateSoundAsync,} from "@babylonjs/core";
import {Vector3} from "@babylonjs/core/Maths/math.vector";
import type {Nullable} from '@babylonjs/core/types';
import type {Node} from '@babylonjs/core/node';
import type {AudioConfig, AudioItemConfig} from "../../gwtangular/GwtAngularFacade";

const ENVIRONMENT_VOLUME = 0.1;
const MAX_INSTANCES = 5;
const ATMOSPHERE_VOLUME = 0.3;
const ATMOSPHERE_CROSSFADE_DURATION = 1.0;
const ATMOSPHERE_CHECK_INTERVAL = 500;

@Injectable({providedIn: 'root'})
export class BabylonAudioService {
  private audioEngine: AudioEngineV2 | null = null;
  private soundBufferCache = new Map<number, StaticSoundBuffer>();
  private waterLoop: AbstractSound | null = null;
  private landLoop: AbstractSound | null = null;
  private atmosphereInterval: ReturnType<typeof setInterval> | null = null;
  private currentTerrainIsWater = false;
  private rendererService: any = null;
  private audioConfig: AudioConfig | null = null;
  private englishVoice: SpeechSynthesisVoice | null = null;

  async init(): Promise<void> {
    try {
      this.audioEngine = await CreateAudioEngineAsync();
      await this.audioEngine.unlockAsync();
    } catch (e) {
      console.error("BabylonAudioService: Failed to initialize audio engine", e);
    }
    this.initSpeechSynthesis();
  }

  private initSpeechSynthesis(): void {
    if (typeof speechSynthesis === 'undefined') {
      return;
    }
    const pickVoice = () => {
      const voices = speechSynthesis.getVoices();
      this.englishVoice =
        voices.find(v => v.lang === 'en-US') ??
        voices.find(v => v.lang.startsWith('en')) ??
        null;
    };
    pickVoice();
    if (!this.englishVoice) {
      speechSynthesis.onvoiceschanged = () => pickVoice();
    }
  }

  speakSelection(name: string): void {
    this.speak(`${name} ready`);
  }

  speakCommand(command: string): void {
    this.speak(command);
  }

  private speak(text: string): void {
    if (typeof speechSynthesis === 'undefined') {
      return;
    }
    speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-US';
    utterance.rate = 1.1;
    utterance.pitch = 0.9;
    utterance.volume = 0.8;
    if (this.englishVoice) {
      utterance.voice = this.englishVoice;
    }
    speechSynthesis.speak(utterance);
  }

  attachListenerTo(node: Nullable<Node>): void {
    if (!this.audioEngine) {
      return;
    }
    this.audioEngine.listener.attach(node);
  }

  setRendererService(service: any): void {
    this.rendererService = service;
  }

  configureFromAudioConfig(config: AudioConfig | null): void {
    this.audioConfig = config;
  }

  playAudio(audioId: number): void {
    if (!this.audioEngine) {
      return;
    }
    const url = `/rest/audio/${audioId}`;
    const buffer = this.soundBufferCache.get(audioId);
    const source = buffer ?? url;
    CreateSoundAsync(`sound-${audioId}`, source, {maxInstances: MAX_INSTANCES}, this.audioEngine)
      .then(sound => sound.play())
      .catch(e => console.error(`BabylonAudioService: playAudio failed for ${audioId}`, e));
  }

  playAudioAtPosition(audioId: number, position: Vector3): void {
    if (!this.audioEngine) {
      return;
    }
    const url = `/rest/audio/${audioId}`;
    const buffer = this.soundBufferCache.get(audioId);
    const source = buffer ?? url;
    CreateSoundAsync(`sound-${audioId}`, source, {
      maxInstances: MAX_INSTANCES,
      spatialEnabled: true,
      spatialPosition: position,
      spatialMaxDistance: 100,
    }, this.audioEngine)
      .then(sound => sound.play())
      .catch(e => console.error(`BabylonAudioService: playAudioAtPosition failed for ${audioId}`, e));
  }

  playAudioAtPositionWithConfig(config: AudioItemConfig, position: Vector3): void {
    const audioId = config.getAudioId();
    if (!this.audioEngine || audioId == null) {
      return;
    }
    const url = `/rest/audio/${audioId}`;
    const buffer = this.soundBufferCache.get(audioId);
    const source = buffer ?? url;
    const pitchMin = config.getPitchCentsMin();
    const pitchMax = config.getPitchCentsMax();
    const volMin = config.getVolumeMin();
    const volMax = config.getVolumeMax();
    CreateSoundAsync(`sound-${audioId}`, source, {
      maxInstances: MAX_INSTANCES,
      spatialEnabled: true,
      spatialPosition: position,
      spatialMaxDistance: 100,
    }, this.audioEngine)
      .then(sound => {
        sound.pitch = pitchMin + Math.random() * (pitchMax - pitchMin);
        sound.volume = volMin + Math.random() * (volMax - volMin);
        sound.play();
      })
      .catch(e => console.error(`BabylonAudioService: playAudioAtPositionWithConfig failed for ${audioId}`, e));
  }

  playQuestActivatedAudio() {
    if (this.audioConfig?.getOnQuestActivated()) {
      this.playAudio(this.audioConfig.getOnQuestActivated()!);
    }
  }

  async startAtmosphere(waterAudioId: number | null, landAudioId: number | null): Promise<void> {
    if (!this.audioEngine) {
      return;
    }
    if (!waterAudioId && !landAudioId) {
      return;
    }

    try {
      if (waterAudioId) {
        this.waterLoop = await CreateSoundAsync('atmosphere-water', `/rest/audio/${waterAudioId}`, {
          loop: true,
          volume: 0,
          maxInstances: 1,
        }, this.audioEngine);
        this.waterLoop.play();
      }

      if (landAudioId) {
        this.landLoop = await CreateSoundAsync('atmosphere-land', `/rest/audio/${landAudioId}`, {
          loop: true,
          volume: ATMOSPHERE_VOLUME,
          maxInstances: 1,
        }, this.audioEngine);
        this.landLoop.play();
      }

      // Start with land as default
      this.currentTerrainIsWater = false;

      this.atmosphereInterval = setInterval(() => this.updateAtmosphere(), ATMOSPHERE_CHECK_INTERVAL);
    } catch (e) {
      console.error("BabylonAudioService: Failed to start atmosphere loops", e);
    }
  }

  private updateAtmosphere(): void {
    if (!this.rendererService) {
      return;
    }

    try {
      const center = this.rendererService.setupCenterGroundPosition();
      if (!center || !isFinite(center.x) || !isFinite(center.z)) {
        return;
      }

      const height = this.rendererService.getTerrainHeightAt(center.x, center.z);
      if (height === null) {
        return;
      }

      const isWater = height <= 0;
      if (isWater === this.currentTerrainIsWater) {
        return;
      }

      this.currentTerrainIsWater = isWater;

      if (isWater) {
        this.setLoopVolume(this.waterLoop, ATMOSPHERE_VOLUME);
        this.setLoopVolume(this.landLoop, 0);
      } else {
        this.setLoopVolume(this.waterLoop, 0);
        this.setLoopVolume(this.landLoop, ATMOSPHERE_VOLUME);
      }
    } catch (e) {
      // Silently ignore errors during atmosphere update
    }
  }

  private setLoopVolume(sound: AbstractSound | null, volume: number): void {
    if (!sound) {
      return;
    }
    try {
      sound.setVolume(volume, {duration: ATMOSPHERE_CROSSFADE_DURATION});
    } catch (e) {
      // Ramp already in progress — ignore
    }
  }

  stopAtmosphere(): void {
    if (this.atmosphereInterval !== null) {
      clearInterval(this.atmosphereInterval);
      this.atmosphereInterval = null;
    }
    if (this.waterLoop) {
      this.waterLoop.dispose();
      this.waterLoop = null;
    }
    if (this.landLoop) {
      this.landLoop.dispose();
      this.landLoop = null;
    }
  }
}
