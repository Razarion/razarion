import {Injectable, inject, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';

/**
 * Studio-side client for the dev-only director API (/rest/director). The studio
 * is the CONTROL plane: it authors {@link DirectorPlan}s and drives the live
 * rendering client (the /director route of the main game app) through the
 * server command channel. No auth — the endpoints are dev-only (absent on prod).
 *
 * NB the studio dev-server proxies /rest to :8080, so the REAL Spring Boot
 * server must be running there (not the mock fake-server) for director work.
 */
export interface DirectorCameraKey {
  time: number;
  mode: 'orbit' | 'free';
  target: [number, number, number];
  position?: [number, number, number];
  alpha?: number;
  beta?: number;
  radius?: number;
  easing?: 'linear' | 'ease';
}

/** A timed attack on the plan timeline (spawn a green force + attack a base). */
export interface DirectorCue {
  time: number;
  action: 'attack';
  x: number;
  y: number;
  count: number;
  baseItemTypeId?: number | null;
  /** Base id to attack; null = first bot base. */
  targetBaseId?: number | null;
}

export interface DirectorBaseInfo {
  baseId: number;
  name: string;
  character: string;
  botId?: number | null;
}

export interface DirectorPlan {
  version: number;
  durationMs: number;
  cameraKeys: DirectorCameraKey[];
  cues?: DirectorCue[];
}

export interface DirectorPlanSummary {
  id: number;
  name: string;
  lastModified?: string;
}

interface DirectorPlanDto {
  id: number;
  name: string;
  jsonContent: string;
  lastModified?: string;
}

export interface DirectorCameraPose {
  seq: number;
  posX: number; posY: number; posZ: number;
  targetX: number; targetY: number; targetZ: number;
}

export type DirectorCommandType =
  'LOAD_PLAN' | 'PLAY' | 'PAUSE' | 'STOP' | 'SEEK' | 'RECORD_START' | 'RECORD_STOP' | 'CAPTURE';

export interface StageAttackRequest {
  x: number;
  y: number;
  count?: number;
  baseItemTypeId?: number;
  targetBaseId?: number;
}

export interface StageAttackResult {
  spawned: number;
  attackerType: string;
  targetBaseId: number;
  targetItemId: number;
  errors: string[];
}

export function emptyPlan(): DirectorPlan {
  return {
    version: 1,
    durationMs: 20000,
    cameraKeys: [
      {time: 0, mode: 'orbit', target: [2560, 30, 2560], alpha: 0, beta: 0.55, radius: 1600, easing: 'ease'},
      {time: 20000, mode: 'orbit', target: [2560, 30, 2560], alpha: Math.PI * 2, beta: 0.55, radius: 1600, easing: 'ease'},
    ],
    cues: [],
  };
}

@Injectable({providedIn: 'root'})
export class DirectorStorageService {
  private readonly http = inject(HttpClient);
  private readonly base = '/rest/director';

  readonly plans = signal<DirectorPlanSummary[]>([]);
  readonly lastError = signal<string | null>(null);

  async list(): Promise<void> {
    this.lastError.set(null);
    try {
      const list = await firstValueFrom(this.http.get<DirectorPlanSummary[]>(`${this.base}/plan`));
      this.plans.set(list ?? []);
    } catch (e: any) {
      this.lastError.set(this.formatError(e));
      throw e;
    }
  }

  async read(id: number): Promise<{summary: DirectorPlanSummary; plan: DirectorPlan}> {
    const dto = await firstValueFrom(this.http.get<DirectorPlanDto>(`${this.base}/plan/${id}`));
    return {
      summary: {id: dto.id, name: dto.name, lastModified: dto.lastModified},
      plan: this.parsePlan(dto.jsonContent),
    };
  }

  async create(name: string, plan: DirectorPlan): Promise<DirectorPlanSummary> {
    const dto: DirectorPlanDto = {id: 0, name, jsonContent: JSON.stringify(plan)};
    const created = await firstValueFrom(this.http.post<DirectorPlanDto>(`${this.base}/plan`, dto));
    const summary: DirectorPlanSummary = {id: created.id, name: created.name, lastModified: created.lastModified};
    this.plans.update(arr => [summary, ...arr]);
    return summary;
  }

  async save(id: number, name: string, plan: DirectorPlan): Promise<DirectorPlanSummary> {
    const dto: DirectorPlanDto = {id, name, jsonContent: JSON.stringify(plan)};
    const updated = await firstValueFrom(this.http.post<DirectorPlanDto>(`${this.base}/plan/${id}`, dto));
    const summary: DirectorPlanSummary = {id: updated.id, name: updated.name, lastModified: updated.lastModified};
    this.plans.update(arr => arr.map(s => s.id === id ? summary : s));
    return summary;
  }

  async delete(id: number): Promise<void> {
    await firstValueFrom(this.http.delete(`${this.base}/plan/${id}`));
    this.plans.update(arr => arr.filter(s => s.id !== id));
  }

  // ===== Transport command channel =====

  async sendCommand(type: DirectorCommandType, extra: {planId?: number; timeMs?: number; fileName?: string} = {}): Promise<void> {
    await firstValueFrom(this.http.post(`${this.base}/command`, {type, ...extra}));
  }

  /** Create (reset) the operator's green base with its start building at (x,y).
   *  Returns the new base id. */
  async createBase(x: number, y: number): Promise<number> {
    return await firstValueFrom(this.http.post<number>(`${this.base}/create-base`, {x, y}));
  }

  /** Spawn a green strike force at (x,y) and order it to attack the bot. */
  async stageAttack(req: StageAttackRequest): Promise<StageAttackResult> {
    return await firstValueFrom(this.http.post<StageAttackResult>(`${this.base}/stage-attack`, req));
  }

  /** All current bases (for the attack-target dropdown). */
  async listBases(): Promise<DirectorBaseInfo[]> {
    try {
      return (await firstValueFrom(this.http.get<DirectorBaseInfo[]>(`${this.base}/bases`))) ?? [];
    } catch {
      return [];
    }
  }

  /** Ask the client to publish its current camera, then read it back. Returns
   *  null if no FRESH pose (higher seq) arrived within the timeout. Using seq
   *  (not coord comparison) means re-capturing the same view still works. */
  async captureCameraFromClient(timeoutMs = 2500): Promise<DirectorCameraPose | null> {
    const beforeSeq = (await this.lastCamera())?.seq ?? 0;
    await this.sendCommand('CAPTURE');
    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
      await new Promise(r => setTimeout(r, 150));
      const pose = await this.lastCamera();
      if (pose && pose.seq > beforeSeq) return pose;
    }
    return null;
  }

  private async lastCamera(): Promise<DirectorCameraPose | null> {
    try {
      return await firstValueFrom(this.http.get<DirectorCameraPose | null>(`${this.base}/camera`));
    } catch {
      return null;
    }
  }

  private parsePlan(raw: string): DirectorPlan {
    try {
      const parsed = JSON.parse(raw);
      if (parsed?.version === 1) return parsed;
      return emptyPlan();
    } catch {
      return emptyPlan();
    }
  }

  private formatError(e: any): string {
    const status = e?.status ?? e?.response?.status;
    const msg = e?.error?.message ?? e?.message ?? 'request failed';
    return status ? `${status}: ${msg}` : msg;
  }
}
