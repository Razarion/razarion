import {Injectable} from "@angular/core";
import type {AbstractSound, AudioEngineV2, StaticSoundBuffer} from "@babylonjs/core";
import {CreateAudioEngineAsync, CreateSoundAsync, CreateSoundBufferAsync} from "@babylonjs/core";
import {Vector3} from "@babylonjs/core/Maths/math.vector";
import type {Nullable} from '@babylonjs/core/types';
import type {Node} from '@babylonjs/core/node';
import type {AudioConfig, AudioItemConfig} from "../../gwtangular/GwtAngularFacade";

const ENVIRONMENT_VOLUME = 0.1;
const MAX_INSTANCES = 5;
const ATMOSPHERE_VOLUME = 0.3;
const ATMOSPHERE_CROSSFADE_DURATION = 1.0;
const ATMOSPHERE_CHECK_INTERVAL = 500;
// Distance at which a spatial sound fades to silence. 100 units was inaudible
// for any sound played beyond camera-zoom range (e.g. defensive Tesla coils
// at base perimeter while the player watches frontline combat).
const SPATIAL_MAX_DISTANCE = 300;

@Injectable({providedIn: 'root'})
export class BabylonAudioService {
  private audioEngine: AudioEngineV2 | null = null;
  private soundBufferCache = new Map<number, StaticSoundBuffer>();
  // Dedup parallel first-loads: N simultaneous Tesla shots should trigger ONE
  // network fetch, not N. All callers await the same in-flight Promise.
  private bufferLoadPromises = new Map<number, Promise<StaticSoundBuffer>>();
  // Holds references to one-shot Sounds while they play. Without this, the
  // Sound is unreferenced after .play() and the GC may collect it mid-playback —
  // disposing the underlying Web Audio source mid-stream produces a quiet click
  // instead of the full sound. Sounds are removed and disposed on completion.
  private activeOneShots = new Set<AbstractSound>();
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
    // Browsers expose many voices flagged en-US whose underlying TTS engine has
    // a French/German/etc. accent (e.g. Microsoft Henri). Prefer known-clean
    // native English voices by name, then fall back to en-* while excluding
    // names that signal a non-English origin.
    const PREFERRED_VOICE_NAMES = [
      'Google US English',
      'Microsoft Aria',
      'Microsoft Jenny',
      'Microsoft Guy',
      'Microsoft Davis',
      'Microsoft Zira',
      'Microsoft David',
      'Microsoft Mark',
    ];
    const NON_ENGLISH_NAME = /\b(fr|french|de|german|es|spanish|it|italian|pt|portuguese|ru|russian|zh|chinese|ja|japanese|ko|korean|ar|arabic|hi|hindi)\b/i;
    const pickVoice = () => {
      const voices = speechSynthesis.getVoices();
      this.englishVoice =
        voices.find(v => PREFERRED_VOICE_NAMES.some(p => v.name.includes(p))) ??
        voices.find(v => v.lang === 'en-US' && !NON_ENGLISH_NAME.test(v.name)) ??
        voices.find(v => v.lang.startsWith('en') && !NON_ENGLISH_NAME.test(v.name)) ??
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
    // Skip if no English voice is installed (e.g. Selenium-driven Chrome on a
    // German Windows system has only de-DE OneCore voices). Without an English
    // voice, Chrome falls back to its built-in eSpeak engine which produces a
    // grating French-sounding accent — worse than silence.
    if (!this.englishVoice) {
      return;
    }
    speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-US';
    utterance.rate = 1.1;
    utterance.pitch = 0.9;
    utterance.volume = 0.8;
    utterance.voice = this.englishVoice;
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

  /**
   * Loads (or returns cached) decoded audio buffer for the given id. Subsequent
   * calls for the same id are O(map-lookup) — no network, no decode. Concurrent
   * first-calls share one in-flight Promise.
   */
  preloadAudio(audioId: number): Promise<StaticSoundBuffer | null> {
    if (!this.audioEngine) {
      return Promise.resolve(null);
    }
    return this.getOrLoadBuffer(audioId);
  }

  private getOrLoadBuffer(audioId: number): Promise<StaticSoundBuffer> {
    const cached = this.soundBufferCache.get(audioId);
    if (cached) {
      return Promise.resolve(cached);
    }
    const inflight = this.bufferLoadPromises.get(audioId);
    if (inflight) {
      return inflight;
    }
    const promise = CreateSoundBufferAsync(`/rest/audio/${audioId}`, {}, this.audioEngine!)
      .then(buf => {
        this.soundBufferCache.set(audioId, buf);
        this.bufferLoadPromises.delete(audioId);
        return buf;
      })
      .catch(e => {
        this.bufferLoadPromises.delete(audioId);
        throw e;
      });
    this.bufferLoadPromises.set(audioId, promise);
    return promise;
  }

  private trackAndPlay(sound: AbstractSound): void {
    this.activeOneShots.add(sound);
    sound.onEndedObservable.addOnce(() => {
      this.activeOneShots.delete(sound);
      sound.dispose();
    });
    sound.play();
  }

  playAudio(audioId: number): void {
    if (!this.audioEngine) {
      return;
    }
    this.getOrLoadBuffer(audioId)
      .then(buffer => CreateSoundAsync(`sound-${audioId}`, buffer, {maxInstances: MAX_INSTANCES}, this.audioEngine!))
      .then(sound => this.trackAndPlay(sound))
      .catch(e => console.error(`BabylonAudioService: playAudio failed for ${audioId}`, e));
  }

  playAudioAtPosition(audioId: number, position: Vector3): void {
    if (!this.audioEngine) {
      return;
    }
    this.getOrLoadBuffer(audioId)
      .then(buffer => CreateSoundAsync(`sound-${audioId}`, buffer, {
        maxInstances: MAX_INSTANCES,
        spatialEnabled: true,
        spatialPosition: position,
        spatialMaxDistance: SPATIAL_MAX_DISTANCE,
      }, this.audioEngine!))
      .then(sound => this.trackAndPlay(sound))
      .catch(e => console.error(`BabylonAudioService: playAudioAtPosition failed for ${audioId}`, e));
  }

  playAudioAtPositionWithConfig(config: AudioItemConfig, position: Vector3): void {
    const audioId = config.getAudioId();
    if (!this.audioEngine || audioId == null) {
      return;
    }
    const pitchMin = config.getPitchCentsMin();
    const pitchMax = config.getPitchCentsMax();
    const volMin = config.getVolumeMin();
    const volMax = config.getVolumeMax();
    this.getOrLoadBuffer(audioId)
      .then(buffer => CreateSoundAsync(`sound-${audioId}`, buffer, {
        maxInstances: MAX_INSTANCES,
        spatialEnabled: true,
        spatialPosition: position,
        spatialMaxDistance: SPATIAL_MAX_DISTANCE,
      }, this.audioEngine!))
      .then(sound => {
        sound.pitch = pitchMin + Math.random() * (pitchMax - pitchMin);
        sound.volume = volMin + Math.random() * (volMax - volMin);
        this.trackAndPlay(sound);
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
