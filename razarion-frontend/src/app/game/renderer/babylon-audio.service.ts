import {Injectable} from "@angular/core";
import type {AudioEngineV2, StaticSoundBuffer} from "@babylonjs/core";
import {CreateAudioEngineAsync, CreateSoundAsync,} from "@babylonjs/core";
import {Vector3} from "@babylonjs/core/Maths/math.vector";
import type {Nullable} from '@babylonjs/core/types';
import type {Node} from '@babylonjs/core/node';

const ENVIRONMENT_VOLUME = 0.1;
const MAX_INSTANCES = 5;

@Injectable({providedIn: 'root'})
export class BabylonAudioService {
  private audioEngine: AudioEngineV2 | null = null;
  private soundBufferCache = new Map<number, StaticSoundBuffer>();

  async init(): Promise<void> {
    try {
      this.audioEngine = await CreateAudioEngineAsync();
      await this.audioEngine.unlockAsync();
    } catch (e) {
      console.error("BabylonAudioService: Failed to initialize audio engine", e);
    }
  }

  attachListenerTo(node: Nullable<Node>): void {
    if (!this.audioEngine) {
      return;
    }
    this.audioEngine.listener.attach(node);
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
    // Game coords: (x=East-West, y=North-South, z=Height) -> Babylon: (x, z=Height, y=North-South)
    CreateSoundAsync(`sound-${audioId}`, source, {
      maxInstances: MAX_INSTANCES,
      spatialEnabled: true,
      spatialPosition: position,
      spatialMaxDistance: 100,
    }, this.audioEngine)
      .then(sound => sound.play())
      .catch(e => console.error(`BabylonAudioService: playAudioAtPosition failed for ${audioId}`, e));
  }

  playCommandSentAudio() {

  }

  playQuestActivatedAudio() {

  }
}
