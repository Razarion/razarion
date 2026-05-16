import {Injectable, inject, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {
  BaseItemTypeThumbnailEditorControllerClient,
  BaseItemTypeThumbnailConfig,
  ResourceItemTypeThumbnailEditorControllerClient,
  ResourceItemTypeThumbnailConfig,
  BoxItemTypeThumbnailEditorControllerClient,
  BoxItemTypeThumbnailConfig
} from '../../../../../src/app/generated/razarion-share';
import {TypescriptGenerator} from '../../../../../src/app/backend/typescript-generator';

/** Per-item camera framing for the thumbnail render. */
export interface ThumbnailOverride {
  alpha: number;
  beta: number;
  radius: number;
  targetX: number;
  targetY: number;
  targetZ: number;
  /** Frontend Diplomacy enum name; drives material team colour at clone time. */
  diplomacy: string;
}

/** Which item-type endpoint family backs this row. */
export type ItemKind = 'base' | 'resource' | 'box';

/** A single grid cell — base item or resource, with optional saved framing/PNG. */
export interface ThumbnailItem {
  kind: ItemKind;
  id: number;
  internalName: string;
  model3DId: number | null;
  thumbnailImageId: number | null;
  override: ThumbnailOverride | null;
}

/**
 * Talks to /rest/editor/{base|resource}_item_type/thumbnail/* on razarion-server.
 * Holds the merged grid (all base items + all resources) in memory; two
 * round-trips seed it. Login flow lives here too — required because every
 * endpoint is admin-gated.
 */
@Injectable({providedIn: 'root'})
export class ThumbnailStorageService {
  private readonly httpClient = inject(HttpClient);
  private readonly baseClient = new BaseItemTypeThumbnailEditorControllerClient(
    TypescriptGenerator.generateHttpClientAdapter(this.httpClient)
  );
  private readonly resourceClient = new ResourceItemTypeThumbnailEditorControllerClient(
    TypescriptGenerator.generateHttpClientAdapter(this.httpClient)
  );
  private readonly boxClient = new BoxItemTypeThumbnailEditorControllerClient(
    TypescriptGenerator.generateHttpClientAdapter(this.httpClient)
  );

  readonly items = signal<ThumbnailItem[]>([]);
  readonly loaded = signal(false);
  readonly lastError = signal<string | null>(null);

  async loadAll(): Promise<void> {
    this.lastError.set(null);
    try {
      const [baseConfigs, resourceConfigs, boxConfigs] = await Promise.all([
        this.baseClient.readAll(),
        this.resourceClient.readAll(),
        this.boxClient.readAll()
      ]);
      const baseItems = baseConfigs.map(c => this.toBaseItem(c));
      const resourceItems = resourceConfigs.map(c => this.toResourceItem(c));
      const boxItems = boxConfigs.map(c => this.toBoxItem(c));
      // Grid order: base → resource → box (by id within each).
      // The component renders a labelled divider when kind changes.
      this.items.set([
        ...baseItems.sort((a, b) => a.id - b.id),
        ...resourceItems.sort((a, b) => a.id - b.id),
        ...boxItems.sort((a, b) => a.id - b.id)
      ]);
      this.loaded.set(true);
    } catch (e: any) {
      this.items.set([]);
      this.loaded.set(false);
      this.lastError.set(this.formatError(e));
      throw e;
    }
  }

  /** POST framing + PNG, then reload the affected item locally. */
  async save(item: ThumbnailItem, override: ThumbnailOverride, png: Blob): Promise<void> {
    this.lastError.set(null);
    try {
      const imageUrl = this.imageUploadUrl(item);
      if (item.kind === 'base') {
        await this.baseClient.update({
          baseItemTypeId: item.id,
          internalName: '', model3DId: 0, thumbnailImageId: 0,
          alpha: override.alpha, beta: override.beta, radius: override.radius,
          targetX: override.targetX, targetY: override.targetY, targetZ: override.targetZ,
          diplomacy: override.diplomacy
        });
      } else if (item.kind === 'resource') {
        await this.resourceClient.update({
          resourceItemTypeId: item.id,
          internalName: '', model3DId: 0, thumbnailImageId: 0,
          alpha: override.alpha, beta: override.beta, radius: override.radius,
          targetX: override.targetX, targetY: override.targetY, targetZ: override.targetZ,
          diplomacy: override.diplomacy
        });
      } else {
        await this.boxClient.update({
          boxItemTypeId: item.id,
          internalName: '', model3DId: 0, thumbnailImageId: 0,
          alpha: override.alpha, beta: override.beta, radius: override.radius,
          targetX: override.targetX, targetY: override.targetY, targetZ: override.targetZ,
          diplomacy: override.diplomacy
        });
      }
      const formData = new FormData();
      formData.append('image', png, `${item.id}.png`);
      await new Promise<void>((resolve, reject) => {
        this.httpClient
          .post(imageUrl, formData, {responseType: 'text'})
          .subscribe({next: () => resolve(), error: err => reject(err)});
      });
      // Re-fetch the single item so we get the fresh thumbnailImageId.
      const updated =
        item.kind === 'base' ? this.toBaseItem(await this.baseClient.read(item.id)) :
        item.kind === 'resource' ? this.toResourceItem(await this.resourceClient.read(item.id)) :
        this.toBoxItem(await this.boxClient.read(item.id));
      this.items.update(arr => arr.map(it => sameItem(it, item) ? updated : it));
    } catch (e: any) {
      this.lastError.set(this.formatError(e));
      throw e;
    }
  }

  async clear(item: ThumbnailItem): Promise<void> {
    this.lastError.set(null);
    try {
      if (item.kind === 'base') {
        await this.baseClient.reset(item.id);
      } else if (item.kind === 'resource') {
        await this.resourceClient.reset(item.id);
      } else {
        await this.boxClient.reset(item.id);
      }
      this.items.update(arr => arr.map(it =>
        sameItem(it, item) ? {...it, override: null, thumbnailImageId: null} : it
      ));
    } catch (e: any) {
      this.lastError.set(this.formatError(e));
      throw e;
    }
  }

  // ===== Auth =====

  needsLogin(): boolean {
    return !localStorage.getItem('app.token');
  }

  loggedInAs(): string {
    const t = localStorage.getItem('app.token');
    if (!t) return '';
    try {
      const payload = JSON.parse(atob(t.split('.')[1]));
      return payload.sub ?? '<unknown>';
    } catch {
      return '<invalid token>';
    }
  }

  /** HTTP-Basic against /rest/user/auth, stores returned JWT, then loads grid. */
  async login(email: string, password: string): Promise<void> {
    this.lastError.set(null);
    try {
      const basic = btoa(`${email}:${password}`);
      const token = await new Promise<string>((resolve, reject) => {
        this.httpClient
          .post('/rest/user/auth', null, {
            responseType: 'text',
            headers: {Authorization: `Basic ${basic}`}
          })
          .subscribe({next: t => resolve(t), error: err => reject(err)});
      });
      localStorage.setItem('app.token', token);
      await this.loadAll();
    } catch (e: any) {
      this.lastError.set(this.formatError(e));
      throw e;
    }
  }

  logout(): void {
    localStorage.removeItem('app.token');
    this.items.set([]);
    this.loaded.set(false);
    this.lastError.set(null);
  }

  // ===== Helpers =====

  private imageUploadUrl(item: ThumbnailItem): string {
    const root =
      item.kind === 'base' ? 'base_item_type' :
      item.kind === 'resource' ? 'resource_item_type' :
      'box_item_type';
    return `/rest/editor/${root}/thumbnail/image/${item.id}`;
  }

  private toBaseItem(c: BaseItemTypeThumbnailConfig): ThumbnailItem {
    return this.toItem('base', c.baseItemTypeId, c.internalName, c.model3DId, c.thumbnailImageId,
      c.alpha, c.beta, c.radius, c.targetX, c.targetY, c.targetZ, c.diplomacy);
  }

  private toResourceItem(c: ResourceItemTypeThumbnailConfig): ThumbnailItem {
    return this.toItem('resource', c.resourceItemTypeId, c.internalName, c.model3DId, c.thumbnailImageId,
      c.alpha, c.beta, c.radius, c.targetX, c.targetY, c.targetZ, c.diplomacy);
  }

  private toBoxItem(c: BoxItemTypeThumbnailConfig): ThumbnailItem {
    return this.toItem('box', c.boxItemTypeId, c.internalName, c.model3DId, c.thumbnailImageId,
      c.alpha, c.beta, c.radius, c.targetX, c.targetY, c.targetZ, c.diplomacy);
  }

  private toItem(kind: ItemKind, id: number, internalName: string,
                 model3DId: number, thumbnailImageId: number,
                 alpha: number, beta: number, radius: number,
                 targetX: number, targetY: number, targetZ: number,
                 diplomacy: string): ThumbnailItem {
    const hasFraming = alpha != null;
    return {
      kind, id, internalName,
      model3DId: model3DId == null ? null : model3DId,
      thumbnailImageId: thumbnailImageId == null ? null : thumbnailImageId,
      override: hasFraming
        ? {alpha, beta, radius, targetX, targetY, targetZ, diplomacy: diplomacy ?? 'OWN'}
        : null
    };
  }

  private formatError(e: any): string {
    const status = e?.status ?? e?.response?.status;
    const msg = e?.error?.message ?? e?.message ?? 'request failed';
    return status ? `HTTP ${status}: ${msg}` : msg;
  }
}

export function sameItem(a: ThumbnailItem, b: ThumbnailItem): boolean {
  return a.kind === b.kind && a.id === b.id;
}
