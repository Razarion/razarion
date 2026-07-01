import {Component, OnInit, inject, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {
  DirectorBaseInfo,
  DirectorPlan,
  DirectorPlanSummary,
  DirectorStorageService,
  emptyPlan,
} from './director-storage.service';

/**
 * Director control plane: author a camera flight plan and drive the live
 * rendering client (the main game app's /director route) via the server command
 * channel. The studio has no engine — preview/recording happen in the client;
 * here you build the plan and press the transport buttons.
 *
 * Workflow: open the client at /director, frame a shot with the normal RTS
 * camera, hit "Capture" here to append a keyframe; repeat; Save; "Load in
 * client"; Play / Record.
 */
@Component({
  selector: 'studio-director-task',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="director">
      <!-- ===== Left: plan list ===== -->
      <aside class="left">
        <header>
          <h3>Plans</h3>
          <button class="ghost sm" (click)="reload()" title="Reload">↻</button>
        </header>
        @if (storage.lastError(); as err) {
          <div class="status error">{{ err }}</div>
        }
        <ul class="plan-list">
          @for (p of storage.plans(); track p.id) {
            <li [class.active]="current()?.id === p.id" (click)="openPlan(p.id)">
              <span class="name">{{ p.name }}</span>
              <button class="ghost xs" (click)="deletePlan(p, $event)" title="Delete">×</button>
            </li>
          } @empty {
            <li class="empty">No plans yet.</li>
          }
        </ul>
        <button class="primary block" (click)="createPlan()" [disabled]="busy()">+ New plan</button>
        <div class="hint">
          Director endpoints are dev-only. Run the REAL server on :8080 and open
          the client at <code>/director</code>.
        </div>
      </aside>

      <!-- ===== Center: keyframe editor ===== -->
      <section class="editor">
        @if (plan(); as pl) {
          <div class="editor-header">
            <input class="rename" type="text" [(ngModel)]="currentName" [disabled]="busy()">
            <label class="muted">Duration
              <input type="number" min="500" step="500" [(ngModel)]="pl.durationMs" style="width:90px;"> ms
            </label>
            <div class="spacer"></div>
            <button class="primary" (click)="save()" [disabled]="busy()">{{ busy() ? 'Saving…' : 'Save' }}</button>
          </div>

          <div class="keys">
            <table>
              <thead>
                <tr>
                  <th>t (ms)</th><th>mode</th><th>target x/y/z</th>
                  <th>orbit α/β/r  ·  free pos x/y/z</th><th>ease</th><th></th>
                </tr>
              </thead>
              <tbody>
                @for (k of pl.cameraKeys; track $index) {
                  <tr>
                    <td><input type="number" step="100" [(ngModel)]="k.time" style="width:70px;"></td>
                    <td>
                      <select [(ngModel)]="k.mode" (ngModelChange)="onModeChange(k)">
                        <option value="orbit">orbit</option>
                        <option value="free">free</option>
                      </select>
                    </td>
                    <td class="triple">
                      <input type="number" step="1" [(ngModel)]="k.target[0]">
                      <input type="number" step="1" [(ngModel)]="k.target[1]">
                      <input type="number" step="1" [(ngModel)]="k.target[2]">
                    </td>
                    <td class="triple">
                      @if (k.mode === 'orbit') {
                        <input type="number" step="0.1" [(ngModel)]="k.alpha" title="azimuth rad">
                        <input type="number" step="0.05" [(ngModel)]="k.beta" title="elevation rad">
                        <input type="number" step="50" [(ngModel)]="k.radius" title="distance">
                      } @else {
                        <input type="number" step="1" [ngModel]="k.position?.[0]" (ngModelChange)="setPos(k, 0, $event)">
                        <input type="number" step="1" [ngModel]="k.position?.[1]" (ngModelChange)="setPos(k, 1, $event)">
                        <input type="number" step="1" [ngModel]="k.position?.[2]" (ngModelChange)="setPos(k, 2, $event)">
                      }
                    </td>
                    <td>
                      <select [(ngModel)]="k.easing">
                        <option value="ease">ease</option>
                        <option value="linear">linear</option>
                      </select>
                    </td>
                    <td class="row-actions">
                      <button class="ghost xs" (click)="captureInto(k)" [disabled]="capturing()" title="Set from client view">⊙</button>
                      <button class="ghost xs" (click)="duplicateKey($index)" title="Duplicate">⎘</button>
                      <button class="ghost xs" (click)="deleteKey($index)" title="Delete">×</button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
            <div class="key-actions">
              <button (click)="addKey()">+ Keyframe</button>
              <button (click)="captureAppend()" [disabled]="capturing()">
                {{ capturing() ? 'Capturing…' : '+ Capture from client' }}
              </button>
            </div>
          </div>

          <div class="divider"></div>
          <div class="keys">
            <div class="cue-head">
              <strong>Attack cues</strong>
              <span class="muted">— timed spawns that attack a base during playback</span>
            </div>
            <table>
              <thead>
                <tr><th>t (ms)</th><th>spawn X/Y</th><th>units</th><th>type</th><th>target base</th><th></th></tr>
              </thead>
              <tbody>
                @for (c of (pl.cues ?? []); track $index) {
                  <tr>
                    <td><input type="number" step="100" [(ngModel)]="c.time" style="width:70px;"></td>
                    <td class="triple">
                      <input type="number" step="1" [(ngModel)]="c.x">
                      <input type="number" step="1" [(ngModel)]="c.y">
                    </td>
                    <td><input type="number" min="1" [(ngModel)]="c.count" style="width:56px;"></td>
                    <td><input type="number" [(ngModel)]="c.baseItemTypeId" placeholder="auto" style="width:60px;"></td>
                    <td>
                      <select [(ngModel)]="c.targetBaseId">
                        <option [ngValue]="null">auto (first bot)</option>
                        @for (b of bases(); track b.baseId) {
                          <option [ngValue]="b.baseId">{{ b.name }} #{{ b.baseId }} ({{ b.character }})</option>
                        }
                      </select>
                    </td>
                    <td><button class="ghost xs" (click)="deleteCue($index)" title="Delete">×</button></td>
                  </tr>
                } @empty {
                  <tr><td colspan="6" class="muted">No cues — add one, or use Stage battle for an immediate fight.</td></tr>
                }
              </tbody>
            </table>
            <div class="key-actions">
              <button (click)="addCue()">+ Attack cue</button>
              <button class="ghost" (click)="reloadBases()">↻ Bases</button>
            </div>
          </div>
        } @else {
          <div class="placeholder">Pick a plan or click <strong>+ New plan</strong>.</div>
        }
      </section>

      <!-- ===== Right: transport ===== -->
      <aside class="right">
        <header><h3>Transport</h3></header>
        @if (plan(); as pl) {
          <div class="transport">
            <button class="primary block" (click)="loadInClient()" [disabled]="busy()">⤓ Load in client</button>
            <div class="row">
              <button (click)="cmd('PLAY')">▶ Play</button>
              <button (click)="cmd('PAUSE')">⏸ Pause</button>
              <button (click)="cmd('STOP')">⏹ Stop</button>
            </div>
            <div class="seek">
              <span class="muted">Seek</span>
              <input type="range" min="0" [max]="pl.durationMs" step="100" [(ngModel)]="seekMs" (input)="onSeek()">
              <span class="muted">{{ seekMs }} ms</span>
            </div>
            <div class="divider"></div>
            <div class="row">
              <button class="rec" (click)="startRecord()">● Record</button>
              <button (click)="cmd('RECORD_STOP')">■ Stop rec</button>
            </div>
            <div class="hint">
              Record plays from the start and downloads a WebM in the CLIENT tab
              when the plan ends. <strong>Stop</strong> hands camera control back
              to the client so you can scroll / re-frame again.
            </div>
          </div>
        } @else {
          <div class="placeholder small">No plan loaded.</div>
        }

        <div class="divider"></div>
        <header><h3>Stage battle</h3></header>
        <div class="transport">
          <div class="row">
            <label class="muted">X <input type="number" step="10" [(ngModel)]="stageX" (ngModelChange)="saveStage()" style="width:74px;"></label>
            <label class="muted">Y <input type="number" step="10" [(ngModel)]="stageY" (ngModelChange)="saveStage()" style="width:74px;"></label>
          </div>
          <div class="row">
            <label class="muted">Units <input type="number" min="1" [(ngModel)]="stageCount" (ngModelChange)="saveStage()" style="width:56px;"></label>
            <label class="muted">Type <input type="number" [(ngModel)]="stageTypeId" (ngModelChange)="saveStage()" placeholder="auto" style="width:64px;"></label>
          </div>
          <div class="row">
            <label class="muted" style="flex:1;">Target
              <select [(ngModel)]="stageTargetId" style="width:100%;">
                <option [ngValue]="null">auto (first bot)</option>
                @for (b of bases(); track b.baseId) {
                  <option [ngValue]="b.baseId">{{ b.name }} #{{ b.baseId }} ({{ b.character }})</option>
                }
              </select>
            </label>
          </div>
          <button class="block" (click)="createBase()" [disabled]="staging()">
            🏠 Create my base at X/Y
          </button>
          <button class="primary block" (click)="stageAttack()" [disabled]="staging()">
            {{ staging() ? 'Staging…' : '⚔ Green force → attack bot' }}
          </button>
          @if (stageInfo(); as info) { <div class="hint">{{ info }}</div> }
          <div class="hint">
            Spawns a green (your-base) strike force at (X,Y) and orders it to
            attack the bot. Needs your base to exist in the client; place X/Y
            near the bot.
          </div>
        </div>
      </aside>
    </div>
  `,
  styles: [`
    :host { position: absolute; inset: 0; display: block; }
    .director { position: absolute; inset: 0; display: grid; grid-template-columns: 220px 1fr 240px; gap: 1px; background: #2a2e35; }
    aside, .editor { background: #15171c; overflow: auto; display: flex; flex-direction: column; }
    header { display: flex; align-items: center; padding: 8px 12px; border-bottom: 1px solid #2a2e35; gap: 8px; flex-shrink: 0; }
    h3 { margin: 0; font-size: 11px; font-weight: 600; letter-spacing: 0.08em; text-transform: uppercase; color: #aaa; }
    .plan-list { list-style: none; margin: 0; padding: 6px 0; flex: 1; overflow-y: auto; }
    .plan-list li { display: flex; align-items: center; gap: 6px; padding: 7px 12px; font-size: 12px; color: #ddd; cursor: pointer; border-left: 3px solid transparent; }
    .plan-list li:hover { background: #1c1f25; }
    .plan-list li.active { background: #232830; border-left-color: #4a9eff; }
    .plan-list li.empty { color: #666; font-style: italic; cursor: default; }
    .name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    button { padding: 6px 10px; border-radius: 4px; border: 1px solid #4a9eff; background: rgba(74,158,255,0.18); color: #eee; cursor: pointer; font-size: 12px; }
    button:hover:not(:disabled) { background: rgba(74,158,255,0.32); }
    button.primary { background: rgba(74,158,255,0.4); }
    button.ghost { background: transparent; border-color: rgba(255,255,255,0.2); color: #aaa; }
    button.sm { padding: 3px 8px; font-size: 11px; }
    button.xs { padding: 0 6px; font-size: 13px; line-height: 1; }
    button.block { margin: 8px 12px; }
    button.rec { border-color: #ff5a5a; background: rgba(255,90,90,0.2); color: #ffb3b3; }
    button:disabled { opacity: 0.5; cursor: default; }
    .status.error { margin: 8px 12px; padding: 8px 10px; border-radius: 4px; font-size: 11px; background: rgba(255,90,90,0.12); color: #ff8a8a; border: 1px solid rgba(255,90,90,0.3); }
    .hint { margin: 8px 12px; font-size: 10px; color: #667; line-height: 1.5; }
    .hint code { color: #9cf; }
    .editor-header { display: flex; align-items: center; gap: 10px; padding: 8px 12px; border-bottom: 1px solid #2a2e35; }
    .rename { padding: 5px 8px; font-size: 13px; background: rgba(0,0,0,0.3); border: 1px solid rgba(255,255,255,0.18); border-radius: 3px; color: #eee; min-width: 180px; font-family: inherit; }
    .spacer { flex: 1; }
    .muted { color: #888; font-size: 11px; }
    .keys { padding: 8px 12px; overflow: auto; }
    table { width: 100%; border-collapse: collapse; font-size: 11px; }
    th { text-align: left; color: #888; font-weight: 500; padding: 4px 6px; border-bottom: 1px solid #2a2e35; }
    td { padding: 3px 6px; vertical-align: middle; }
    td.triple { display: flex; gap: 3px; }
    td.row-actions { white-space: nowrap; }
    input, select { padding: 3px 5px; font-size: 11px; background: rgba(0,0,0,0.3); border: 1px solid rgba(255,255,255,0.18); border-radius: 3px; color: #eee; font-variant-numeric: tabular-nums; font-family: inherit; width: 56px; }
    input[type=range] { width: 100%; }
    input:focus, select:focus { outline: none; border-color: #4a9eff; }
    .key-actions { display: flex; gap: 8px; margin-top: 10px; }
    .cue-head { margin-bottom: 6px; font-size: 11px; color: #ccc; }
    .transport { padding: 10px 12px; display: flex; flex-direction: column; gap: 10px; }
    .transport .row { display: flex; gap: 6px; }
    .transport .row button { flex: 1; }
    .seek { display: flex; flex-direction: column; gap: 4px; }
    .divider { height: 1px; background: #2a2e35; }
    .placeholder { flex: 1; display: flex; align-items: center; justify-content: center; color: #777; font-size: 13px; padding: 30px; text-align: center; }
    .placeholder.small { padding: 12px; font-size: 11px; }
  `]
})
export class DirectorTaskComponent implements OnInit {
  protected readonly storage = inject(DirectorStorageService);

  readonly current = signal<DirectorPlanSummary | null>(null);
  readonly plan = signal<DirectorPlan | null>(null);
  readonly busy = signal(false);
  readonly capturing = signal(false);
  readonly bases = signal<DirectorBaseInfo[]>([]);
  currentName = '';
  seekMs = 0;

  // Stage-battle controls
  stageX = 2560;
  stageY = 2560;
  stageCount = 5;
  stageTypeId: number | null = null;
  stageTargetId: number | null = null;
  readonly staging = signal(false);
  readonly stageInfo = signal<string>('');

  private static readonly STAGE_KEY = 'razarion.studio.director.stage';

  async ngOnInit(): Promise<void> {
    this.loadStage();
    await this.reload().catch(() => {});
    await this.reloadBases();
  }

  async reloadBases(): Promise<void> {
    this.bases.set(await this.storage.listBases());
  }

  private loadStage(): void {
    try {
      const raw = localStorage.getItem(DirectorTaskComponent.STAGE_KEY);
      if (!raw) return;
      const s = JSON.parse(raw);
      if (typeof s.x === 'number') this.stageX = s.x;
      if (typeof s.y === 'number') this.stageY = s.y;
      if (typeof s.count === 'number') this.stageCount = s.count;
      this.stageTypeId = typeof s.typeId === 'number' ? s.typeId : null;
    } catch { /* ignore */ }
  }

  saveStage(): void {
    localStorage.setItem(DirectorTaskComponent.STAGE_KEY, JSON.stringify({
      x: +this.stageX, y: +this.stageY, count: +this.stageCount, typeId: this.stageTypeId,
    }));
  }

  async reload(): Promise<void> {
    await this.storage.list().catch(() => {});
  }

  async openPlan(id: number): Promise<void> {
    this.busy.set(true);
    try {
      const {summary, plan} = await this.storage.read(id);
      if (!plan.cues) plan.cues = []; // older plans may lack cues
      this.current.set(summary);
      this.currentName = summary.name;
      this.plan.set(plan);
      this.seekMs = 0;
    } finally {
      this.busy.set(false);
    }
  }

  async createPlan(): Promise<void> {
    this.busy.set(true);
    try {
      const name = `Plan ${this.storage.plans().length + 1}`;
      const summary = await this.storage.create(name, emptyPlan());
      await this.openPlan(summary.id);
    } finally {
      this.busy.set(false);
    }
  }

  async save(): Promise<void> {
    const cur = this.current();
    const pl = this.plan();
    if (!cur || !pl) return;
    this.busy.set(true);
    try {
      const updated = await this.storage.save(cur.id, this.currentName, pl);
      this.current.set(updated);
    } finally {
      this.busy.set(false);
    }
  }

  async deletePlan(p: DirectorPlanSummary, event: Event): Promise<void> {
    event.stopPropagation();
    if (!confirm(`Delete plan "${p.name}"?`)) return;
    await this.storage.delete(p.id);
    if (this.current()?.id === p.id) {
      this.current.set(null);
      this.plan.set(null);
    }
  }

  // ===== Keyframe editing =====

  onModeChange(k: any): void {
    if (k.mode === 'free' && !k.position) {
      k.position = [...k.target];
    }
    if (k.mode === 'orbit') {
      k.alpha ??= 0;
      k.beta ??= 0.55;
      k.radius ??= 1600;
    }
  }

  setPos(k: any, idx: number, value: number): void {
    if (!k.position) k.position = [...k.target];
    k.position[idx] = +value;
  }

  addKey(): void {
    const pl = this.plan();
    if (!pl) return;
    const last = pl.cameraKeys[pl.cameraKeys.length - 1];
    const time = last ? last.time + 2000 : 0;
    pl.cameraKeys.push({time, mode: 'orbit', target: [2560, 30, 2560], alpha: 0, beta: 0.55, radius: 1600, easing: 'ease'});
    this.plan.set({...pl});
  }

  duplicateKey(index: number): void {
    const pl = this.plan();
    if (!pl) return;
    const src = pl.cameraKeys[index];
    const copy = JSON.parse(JSON.stringify(src));
    copy.time = src.time + 2000;
    pl.cameraKeys.splice(index + 1, 0, copy);
    this.plan.set({...pl});
  }

  deleteKey(index: number): void {
    const pl = this.plan();
    if (!pl) return;
    pl.cameraKeys.splice(index, 1);
    this.plan.set({...pl});
  }

  // ===== Attack cues (timed, saved with the plan) =====

  addCue(): void {
    const pl = this.plan();
    if (!pl) return;
    if (!pl.cues) pl.cues = [];
    const last = pl.cues[pl.cues.length - 1];
    const time = last ? last.time + 2000 : 0;
    pl.cues.push({
      time,
      action: 'attack',
      x: +this.stageX,
      y: +this.stageY,
      count: +this.stageCount,
      baseItemTypeId: this.stageTypeId,
      targetBaseId: this.stageTargetId,
    });
    this.plan.set({...pl});
  }

  deleteCue(index: number): void {
    const pl = this.plan();
    if (!pl?.cues) return;
    pl.cues.splice(index, 1);
    this.plan.set({...pl});
  }

  /** Capture the client's current camera into an existing keyframe (free mode). */
  async captureInto(k: any): Promise<void> {
    const pose = await this.doCapture();
    if (!pose) return;
    k.mode = 'free';
    k.position = [pose.posX, pose.posY, pose.posZ];
    k.target = [pose.targetX, pose.targetY, pose.targetZ];
    this.plan.update(p => p ? {...p} : p);
  }

  /** Capture the client's current camera as a new keyframe appended at the end. */
  async captureAppend(): Promise<void> {
    const pl = this.plan();
    if (!pl) return;
    const pose = await this.doCapture();
    if (!pose) return;
    const last = pl.cameraKeys[pl.cameraKeys.length - 1];
    const time = last ? last.time + 2000 : 0;
    pl.cameraKeys.push({
      time,
      mode: 'free',
      position: [pose.posX, pose.posY, pose.posZ],
      target: [pose.targetX, pose.targetY, pose.targetZ],
      easing: 'ease',
    });
    if (time > pl.durationMs) pl.durationMs = time;
    this.plan.set({...pl});
  }

  private async doCapture() {
    this.capturing.set(true);
    try {
      const pose = await this.storage.captureCameraFromClient();
      if (!pose) {
        this.storage.lastError.set('No camera from client — is /director open?');
      }
      return pose;
    } finally {
      this.capturing.set(false);
    }
  }

  // ===== Transport =====

  /** Save (so the client can fetch by id) then tell the client to load it. */
  async loadInClient(): Promise<void> {
    await this.save();
    const cur = this.current();
    if (!cur) return;
    await this.storage.sendCommand('LOAD_PLAN', {planId: cur.id});
  }

  async cmd(type: 'PLAY' | 'PAUSE' | 'STOP' | 'RECORD_STOP'): Promise<void> {
    await this.storage.sendCommand(type);
  }

  async onSeek(): Promise<void> {
    await this.storage.sendCommand('SEEK', {timeMs: +this.seekMs});
  }

  async startRecord(): Promise<void> {
    const name = (this.currentName || 'director').replace(/[^a-z0-9-_]+/gi, '_').toLowerCase();
    await this.storage.sendCommand('RECORD_START', {fileName: `${name}.webm`});
  }

  async createBase(): Promise<void> {
    this.staging.set(true);
    this.stageInfo.set('');
    try {
      const baseId = await this.storage.createBase(+this.stageX, +this.stageY);
      this.stageInfo.set(`Created green base #${baseId} at (${this.stageX}, ${this.stageY})`);
    } catch (e: any) {
      this.stageInfo.set('Create base failed: ' + (e?.error?.message ?? e?.message ?? 'error'));
    } finally {
      this.staging.set(false);
    }
  }

  async stageAttack(): Promise<void> {
    this.staging.set(true);
    this.stageInfo.set('');
    try {
      const r = await this.storage.stageAttack({
        x: +this.stageX,
        y: +this.stageY,
        count: +this.stageCount,
        baseItemTypeId: this.stageTypeId != null && `${this.stageTypeId}` !== '' ? +this.stageTypeId : undefined,
        targetBaseId: this.stageTargetId != null ? +this.stageTargetId : undefined,
      });
      this.stageInfo.set(
        `Spawned ${r.spawned} ${r.attackerType} → attacking base #${r.targetBaseId}`
        + (r.errors?.length ? ` (${r.errors.length} failed)` : ''));
    } catch (e: any) {
      this.stageInfo.set('Failed: ' + (e?.error?.message ?? e?.message ?? 'error'));
    } finally {
      this.staging.set(false);
    }
  }
}
