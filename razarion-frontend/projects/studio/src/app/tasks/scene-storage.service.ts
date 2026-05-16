import {Injectable, inject, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {
  StudioSceneEditorControllerClient,
  StudioSceneSummary,
  StudioSceneDto
} from '../../../../../src/app/generated/razarion-share';
import {TypescriptGenerator} from '../../../../../src/app/backend/typescript-generator';

/**
 * Schema of the JSON blob stored in StudioSceneEntity.jsonContent. Lives in
 * the frontend on purpose — the server treats the payload as opaque.
 *
 * Versioned so we can migrate later if the shape changes.
 */
export interface SceneContent {
  version: 1;
  camera: SceneCamera;
  items: SceneItem[];
  particles: SceneParticle[];
  terrain: SceneTerrain | null;
  background: SceneBackground;
}

export interface SceneCamera {
  mode: 'arc' | 'fly';
  position: [number, number, number];
  target: [number, number, number];
  alpha?: number;
  beta?: number;
  radius?: number;
}

export interface SceneItem {
  /** Stable id within the scene, assigned at spawn and preserved across saves. */
  id: number;
  kind: 'base' | 'resource' | 'box';
  itemTypeId: number;
  position: [number, number, number];
  rotationY: number;
  scale: number;
  diplomacy: string;
  /** Another item in the scene this one is firing at, or null. Drives turret
   *  aiming and the optional looping attack VFX. */
  attackTargetId?: number | null;
  /** Detonate the target item every time this one fires. Persisted so a saved
   *  attack-scene re-arms automatically; only meaningful when attackTargetId
   *  is set. */
  explodeTargetOnFire?: boolean;
}

export interface SceneParticle {
  particleSystemId: number;
  position: [number, number, number];
}

export interface SceneTerrain {
  planetId: number;
  /** Optional sub-region; null = full map. */
  region: {x: number; y: number; w: number; h: number} | null;
}

export interface SceneBackground {
  /** 'transparent' for image-with-alpha, 'colour' for solid backdrop. */
  mode: 'transparent' | 'colour';
  colour: string;
}

export function emptyScene(): SceneContent {
  return {
    version: 1,
    camera: {mode: 'arc', position: [10, 10, 10], target: [0, 0, 0], alpha: -Math.PI / 4, beta: Math.PI / 3, radius: 20},
    items: [],
    particles: [],
    terrain: null,
    background: {mode: 'transparent', colour: '#15171c'}
  };
}

@Injectable({providedIn: 'root'})
export class SceneStorageService {
  private readonly httpClient = inject(HttpClient);
  private readonly client = new StudioSceneEditorControllerClient(
    TypescriptGenerator.generateHttpClientAdapter(this.httpClient)
  );

  readonly scenes = signal<StudioSceneSummary[]>([]);
  readonly lastError = signal<string | null>(null);

  async list(): Promise<void> {
    this.lastError.set(null);
    try {
      const list = await this.client.list();
      this.scenes.set(list);
    } catch (e: any) {
      this.lastError.set(this.formatError(e));
      throw e;
    }
  }

  async read(id: number): Promise<{summary: StudioSceneSummary; content: SceneContent}> {
    this.lastError.set(null);
    try {
      const dto = await this.client.read(id);
      return {
        summary: {id: dto.id, name: dto.name, lastModified: dto.lastModified},
        content: this.parseContent(dto.jsonContent)
      };
    } catch (e: any) {
      this.lastError.set(this.formatError(e));
      throw e;
    }
  }

  async create(name: string, content: SceneContent): Promise<StudioSceneSummary> {
    this.lastError.set(null);
    try {
      const dto: StudioSceneDto = {id: 0, name, jsonContent: JSON.stringify(content), lastModified: '' as any};
      const created = await this.client.create(dto);
      const summary: StudioSceneSummary = {id: created.id, name: created.name, lastModified: created.lastModified};
      this.scenes.update(arr => [summary, ...arr]);
      return summary;
    } catch (e: any) {
      this.lastError.set(this.formatError(e));
      throw e;
    }
  }

  async save(id: number, name: string, content: SceneContent): Promise<StudioSceneSummary> {
    this.lastError.set(null);
    try {
      const dto: StudioSceneDto = {id, name, jsonContent: JSON.stringify(content), lastModified: '' as any};
      const updated = await this.client.update(id, dto);
      const summary: StudioSceneSummary = {id: updated.id, name: updated.name, lastModified: updated.lastModified};
      this.scenes.update(arr => arr.map(s => s.id === id ? summary : s));
      return summary;
    } catch (e: any) {
      this.lastError.set(this.formatError(e));
      throw e;
    }
  }

  async delete(id: number): Promise<void> {
    this.lastError.set(null);
    try {
      await this.client.delete(id);
      this.scenes.update(arr => arr.filter(s => s.id !== id));
    } catch (e: any) {
      this.lastError.set(this.formatError(e));
      throw e;
    }
  }

  private parseContent(raw: string): SceneContent {
    try {
      const parsed = JSON.parse(raw);
      // Future migrations key off parsed.version.
      if (parsed?.version === 1) return parsed;
      return emptyScene();
    } catch {
      return emptyScene();
    }
  }

  private formatError(e: any): string {
    const status = e?.status ?? e?.response?.status;
    const msg = e?.error?.message ?? e?.message ?? 'request failed';
    return status ? `HTTP ${status}: ${msg}` : msg;
  }
}
