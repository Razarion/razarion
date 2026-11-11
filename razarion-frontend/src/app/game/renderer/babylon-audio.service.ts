import {inject, Injectable, NgZone} from "@angular/core";
import type {AbstractSound} from "@babylonjs/core/AudioV2/abstractAudio/abstractSound";
import type {StaticSound} from "@babylonjs/core/AudioV2/abstractAudio/staticSound";
import {SpatialAudioAttachmentType} from "@babylonjs/core/AudioV2/spatialAudioAttachmentType";
import type {TransformNode} from "@babylonjs/core/Meshes/transformNode";
import type {AudioEngineV2} from "@babylonjs/core/AudioV2/abstractAudio/audioEngineV2";
import type {StaticSoundBuffer} from "@babylonjs/core/AudioV2/abstractAudio/staticSoundBuffer";
import {CreateSoundAsync, CreateSoundBufferAsync} from "@babylonjs/core/AudioV2/abstractAudio/audioEngineV2";
import {CreateAudioEngineAsync} from "@babylonjs/core/AudioV2/webAudio/webAudioEngine";
import {Vector3} from "@babylonjs/core/Maths/math.vector";
import type {Nullable} from '@babylonjs/core/types';
import type {Node} from '@babylonjs/core/node';
import type {AudioConfig, AudioItemConfig} from "../../gwtangular/GwtAngularFacade";
import {PROCEDURAL_SAMPLE_RATE} from "./procedural-sounds";

const ENVIRONMENT_VOLUME = 0.1;
const MAX_INSTANCES = 5;
// Distance at which a spatial sound fades to silence. 100 units was inaudible
// for any sound played beyond camera-zoom range (e.g. defensive Tesla coils
// at base perimeter while the player watches frontline combat).
const SPATIAL_MAX_DISTANCE = 300;

/** Looping unit sounds (build hum, engines). Each kind has its own budget of voices. */
export type UnitLoopCategory = 'build' | 'drive' | 'harvest';
// Every spatial sound runs its own update loop (see the zone comment below), and thirty tanks
// humming at once is noise anyway: only the loops nearest to the camera get a voice.
const LOOP_BUDGET: Record<UnitLoopCategory, number> = {build: 4, drive: 5, harvest: 4};
const LOOP_ARBITRATION_MS = 250;
const LOOP_FADE_SECONDS = 0.15;
const SPATIAL_MIN_UPDATE_SECONDS = 0.05;
// A late-loaded loop with less than this left of its motion is skipped rather than blipped.
const MIN_LATE_LOOP_SECONDS = 0.25;
const UNIT_SOUNDS_STORAGE_KEY = 'razarion.unitSounds';

interface ActiveLoop {
  sound: StaticSound | null;
  stopped: boolean;
}

/** What a loop plays: a synthesised buffer (procedural-sounds.ts) or a sound from the audio library. */
export type LoopSource = { bufferKey: string, render: () => Float32Array } | { audioId: number };

interface LoopRequest {
  category: UnitLoopCategory;
  node: TransformNode;
  source: LoopSource;
  volume: number;
  playbackRate: number;
  active: ActiveLoop | null;
}

@Injectable({providedIn: 'root'})
export class BabylonAudioService {
  // Babylon's spatial audio keeps itself up to date with a requestAnimationFrame loop per listener
  // and per spatial sound, started where the object is made. Made inside Angular, every one of
  // those frames ended in a change detection run: the listener alone was 90 runs a second on a
  // Pixel 7 (2026-10-12). So the engine, the listener and the spatial sounds are made outside.
  private readonly zone = inject(NgZone);
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

  private currentTerrainIsWater = false;
  private rendererService: any = null;
  private audioConfig: AudioConfig | null = null;
  private englishVoice: SpeechSynthesisVoice | null = null;
  private listenerNode: Nullable<Node> = null;
  // Synthesised unit sounds (procedural-sounds.ts), keyed by name, each rendered once.
  private proceduralBuffers = new Map<string, Promise<StaticSoundBuffer>>();
  private loopRequests = new Map<string, LoopRequest>();
  private loopArbitrationTimer: ReturnType<typeof setInterval> | null = null;
  private unitSoundsEnabled = BabylonAudioService.readUnitSoundsEnabled();

  async init(): Promise<void> {
    try {
      this.audioEngine = await this.zone.runOutsideAngular(() => CreateAudioEngineAsync());
      await this.audioEngine.unlockAsync();
    } catch (e) {
      console.error("BabylonAudioService: Failed to initialize audio engine", e);
    }
    this.initSpeechSynthesis();
    // Console switch to compare with and without: __razarionUnitSounds(false)
    (window as any).__razarionUnitSounds = (enabled: boolean) => this.setUnitSoundsEnabled(enabled);
  }

  private static readUnitSoundsEnabled(): boolean {
    try {
      return localStorage.getItem(UNIT_SOUNDS_STORAGE_KEY) !== 'off';
    } catch (e) {
      return true;
    }
  }

  /** Builder arm, build hum and engine sounds on or off; remembered in this browser. */
  setUnitSoundsEnabled(enabled: boolean): void {
    this.unitSoundsEnabled = enabled;
    try {
      localStorage.setItem(UNIT_SOUNDS_STORAGE_KEY, enabled ? 'on' : 'off');
    } catch (e) {
      // Private window or blocked storage: the switch holds for this session only.
    }
    if (!enabled) {
      [...this.loopRequests.keys()].forEach(key => this.releaseLoop(key));
    }
  }

  isUnitSoundsEnabled(): boolean {
    return this.unitSoundsEnabled;
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
    this.listenerNode = node;
    if (!this.audioEngine) {
      return;
    }
    this.zone.runOutsideAngular(() => this.audioEngine!.listener.attach(node));
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
    this.zone.runOutsideAngular(() => this.getOrLoadBuffer(audioId)
      .then(buffer => CreateSoundAsync(`sound-${audioId}`, buffer, {
        maxInstances: MAX_INSTANCES,
        spatialEnabled: true,
        spatialPosition: position,
        spatialMaxDistance: SPATIAL_MAX_DISTANCE,
      }, this.audioEngine!))
      .then(sound => this.trackAndPlay(sound))
      .catch(e => console.error(`BabylonAudioService: playAudioAtPosition failed for ${audioId}`, e)));
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
    this.zone.runOutsideAngular(() => this.getOrLoadBuffer(audioId)
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
      .catch(e => console.error(`BabylonAudioService: playAudioAtPositionWithConfig failed for ${audioId}`, e)));
  }

  private getOrRenderProceduralBuffer(bufferKey: string, render: () => Float32Array): Promise<StaticSoundBuffer> {
    let promise = this.proceduralBuffers.get(bufferKey);
    if (!promise) {
      const samples = render();
      const audioBuffer = new AudioBuffer({length: samples.length, numberOfChannels: 1, sampleRate: PROCEDURAL_SAMPLE_RATE});
      audioBuffer.getChannelData(0).set(samples);
      promise = CreateSoundBufferAsync(audioBuffer, {}, this.audioEngine!);
      promise.catch(() => this.proceduralBuffers.delete(bufferKey));
      this.proceduralBuffers.set(bufferKey, promise);
    }
    return promise;
  }

  /** One-shot synthesised sound that travels with the node (e.g. the builder arm servo). */
  playProceduralAtNode(bufferKey: string, render: () => Float32Array, node: TransformNode, volume: number): void {
    if (!this.audioEngine || !this.unitSoundsEnabled) {
      return;
    }
    this.zone.runOutsideAngular(() => this.getOrRenderProceduralBuffer(bufferKey, render)
      .then(buffer => CreateSoundAsync(`unit-${bufferKey}`, buffer, {
        maxInstances: MAX_INSTANCES,
        volume: volume,
        spatialEnabled: true,
        spatialMaxDistance: SPATIAL_MAX_DISTANCE,
        spatialMinUpdateTime: SPATIAL_MIN_UPDATE_SECONDS,
        spatialPosition: node.getAbsolutePosition().clone(),
      }, this.audioEngine!))
      .then(sound => {
        if (node.isDisposed()) {
          sound.dispose();
          return;
        }
        sound.spatial.attach(node, false, SpatialAudioAttachmentType.Position);
        this.trackAndPlay(sound);
      })
      .catch(e => console.error(`BabylonAudioService: playProceduralAtNode failed for ${bufferKey}`, e)));
  }

  /**
   * Plays a looping library sound on the node for exactly `durationSeconds` (e.g. the builder arm
   * while it moves), starting at a random point in the loop and fading in and out.
   */
  playLoopForDuration(audioId: number, node: TransformNode, durationSeconds: number, playbackRate: number, volume: number): void {
    if (!this.audioEngine || !this.unitSoundsEnabled || durationSeconds <= 0) {
      return;
    }
    const requested = performance.now();
    this.zone.runOutsideAngular(() => this.getOrLoadBuffer(audioId)
      .then(buffer => CreateSoundAsync(`unit-loop-${audioId}`, buffer, {
        loop: true,
        volume: 0,
        spatialEnabled: true,
        spatialMaxDistance: SPATIAL_MAX_DISTANCE,
        spatialMinUpdateTime: SPATIAL_MIN_UPDATE_SECONDS,
        spatialPosition: node.getAbsolutePosition().clone(),
      }, this.audioEngine!)
        .then(sound => ({sound, loopSeconds: buffer.duration})))
      .then(({sound, loopSeconds}) => {
        // The file is loaded lazily, so the first play may come late: only the rest of the motion
        // is played, never a sound that outlasts the arm.
        const remaining = durationSeconds - (performance.now() - requested) / 1000;
        if (node.isDisposed() || remaining < MIN_LATE_LOOP_SECONDS) {
          sound.dispose();
          return;
        }
        const fade = Math.min(LOOP_FADE_SECONDS, remaining / 3);
        sound.spatial.attach(node, false, SpatialAudioAttachmentType.Position);
        sound.playbackRate = playbackRate;
        this.activeOneShots.add(sound);
        sound.play({startOffset: Math.random() * loopSeconds * 0.9});
        BabylonAudioService.rampVolume(sound, volume);
        setTimeout(() => BabylonAudioService.rampVolume(sound, 0), (remaining - fade) * 1000);
        setTimeout(() => {
          this.activeOneShots.delete(sound);
          sound.stop();
          sound.dispose();
        }, remaining * 1000 + 30);
      })
      .catch(e => console.error(`BabylonAudioService: playLoopForDuration failed for ${audioId}`, e)));
  }

  /**
   * Asks for a looping sound on the node under `key` (one per unit and kind). Repeated calls only
   * update the pitch. Whether it is actually heard is decided every LOOP_ARBITRATION_MS: the
   * loops nearest to the camera within the category budget play, the rest wait silently.
   */
  requestLoop(key: string, category: UnitLoopCategory, node: TransformNode, source: LoopSource,
              volume: number, playbackRate: number): void {
    if (!this.audioEngine || !this.unitSoundsEnabled) {
      return;
    }
    const existing = this.loopRequests.get(key);
    if (existing) {
      existing.playbackRate = playbackRate;
      if (existing.active?.sound) {
        existing.active.sound.playbackRate = playbackRate;
      }
      return;
    }
    this.loopRequests.set(key, {category, node, source, volume, playbackRate, active: null});
    if (!this.loopArbitrationTimer) {
      this.zone.runOutsideAngular(() => {
        this.loopArbitrationTimer = setInterval(() => this.arbitrateLoops(), LOOP_ARBITRATION_MS);
      });
    }
    // Start without waiting for the next round, or a short build job would miss its hum.
    this.arbitrateLoops();
  }

  releaseLoop(key: string): void {
    const request = this.loopRequests.get(key);
    if (!request) {
      return;
    }
    this.loopRequests.delete(key);
    this.stopLoop(request);
    if (this.loopRequests.size === 0 && this.loopArbitrationTimer) {
      clearInterval(this.loopArbitrationTimer);
      this.loopArbitrationTimer = null;
    }
  }

  private arbitrateLoops(): void {
    const listener = this.getListenerPosition();
    const byCategory = new Map<UnitLoopCategory, { request: LoopRequest, distance: number }[]>();
    for (const [key, request] of [...this.loopRequests]) {
      if (request.node.isDisposed()) {
        this.releaseLoop(key);
        continue;
      }
      const distance = listener ? Vector3.Distance(listener, request.node.getAbsolutePosition()) : 0;
      let list = byCategory.get(request.category);
      if (!list) {
        list = [];
        byCategory.set(request.category, list);
      }
      list.push({request, distance});
    }
    byCategory.forEach((list, category) => {
      list.sort((a, b) => a.distance - b.distance);
      list.forEach((entry, index) => {
        const audible = index < LOOP_BUDGET[category] && entry.distance < SPATIAL_MAX_DISTANCE;
        if (audible && !entry.request.active) {
          this.startLoop(entry.request);
        } else if (!audible && entry.request.active) {
          this.stopLoop(entry.request);
        }
      });
    });
  }

  private getListenerPosition(): Vector3 | null {
    const node: any = this.listenerNode;
    if (!node) {
      return null;
    }
    return node.globalPosition ?? node.getAbsolutePosition?.() ?? node.position ?? null;
  }

  private startLoop(request: LoopRequest): void {
    const active: ActiveLoop = {sound: null, stopped: false};
    request.active = active;
    const source = request.source;
    const name = 'audioId' in source ? `audio-${source.audioId}` : source.bufferKey;
    // A library sound is fetched on the first drive of its unit type, not at boot.
    const loadBuffer = () => 'audioId' in source
      ? this.getOrLoadBuffer(source.audioId)
      : this.getOrRenderProceduralBuffer(source.bufferKey, source.render);
    this.zone.runOutsideAngular(() => loadBuffer()
      .then(buffer => CreateSoundAsync(`unit-loop-${name}`, buffer, {
        loop: true,
        volume: 0,
        spatialEnabled: true,
        spatialMaxDistance: SPATIAL_MAX_DISTANCE,
        spatialMinUpdateTime: SPATIAL_MIN_UPDATE_SECONDS,
        spatialPosition: request.node.getAbsolutePosition().clone(),
      }, this.audioEngine!)
        .then(sound => ({sound, duration: buffer.duration})))
      .then(({sound, duration}) => {
        if (active.stopped || request.node.isDisposed()) {
          sound.dispose();
          return;
        }
        active.sound = sound;
        sound.spatial.attach(request.node, false, SpatialAudioAttachmentType.Position);
        sound.playbackRate = request.playbackRate;
        // Start somewhere inside the loop so several units of one type don't drone in unison.
        sound.play({startOffset: Math.random() * duration * 0.9});
        BabylonAudioService.rampVolume(sound, request.volume);
      })
      .catch(e => {
        if (request.active === active) {
          request.active = null;
        }
        console.error(`BabylonAudioService: startLoop failed for ${name}`, e);
      }));
  }

  private stopLoop(request: LoopRequest): void {
    const active = request.active;
    request.active = null;
    if (!active || active.stopped) {
      return;
    }
    active.stopped = true;
    const sound = active.sound;
    if (!sound) {
      return; // still being created; the creation sees `stopped` and disposes it
    }
    BabylonAudioService.rampVolume(sound, 0);
    setTimeout(() => {
      sound.stop();
      sound.dispose();
    }, LOOP_FADE_SECONDS * 1000 + 30);
  }

  /** Fades instead of jumping: a loop cut off mid-wave clicks. */
  private static rampVolume(sound: StaticSound, volume: number): void {
    try {
      sound.setVolume(volume, {duration: LOOP_FADE_SECONDS});
    } catch (e) {
      // A ramp is still running (started and stopped within the fade time): set it directly.
      sound.volume = volume;
    }
  }

  playQuestActivatedAudio() {
    if (this.audioConfig?.getOnQuestActivated()) {
      this.playAudio(this.audioConfig.getOnQuestActivated()!);
    }
  }
}
