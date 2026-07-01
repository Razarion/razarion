import {Injectable, inject, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';
import {Vector3} from '@babylonjs/core';
import {VideoRecorder} from '@babylonjs/core/Misc/videoRecorder';
import {BabylonRenderServiceAccessImpl} from '../renderer/babylon-render-service-access-impl.service';
import {UiSettingsService} from '../ui-settings.service';

/**
 * Camera keyframe. `mode` decides how it's resolved to a camera pose:
 *  - 'free'  → position + target are used directly (fly-through).
 *  - 'orbit' → position is derived from target + spherical {alpha, beta, radius}
 *              (cinematic orbit / push-in around a point).
 * Coordinates are Babylon world space (x, z = ground plane, y = up).
 */
export interface DirectorCameraKey {
  time: number;
  mode: 'orbit' | 'free';
  target: [number, number, number];
  position?: [number, number, number];
  /** Orbit only: azimuth (rad), elevation (rad), distance. */
  alpha?: number;
  beta?: number;
  radius?: number;
  easing?: 'linear' | 'ease';
}

/**
 * A timed action on the plan timeline. v1: 'attack' — spawn a green strike
 * force at (x,y) and order it to attack a bot base (server /stage-attack).
 */
export interface DirectorCue {
  time: number;
  action: 'attack';
  x: number;
  y: number;
  count: number;
  baseItemTypeId?: number | null;
  /** Base id to attack; null/undefined = first bot base. */
  targetBaseId?: number | null;
}

export interface DirectorPlan {
  version: number;
  durationMs: number;
  cameraKeys: DirectorCameraKey[];
  cues?: DirectorCue[];
}

interface DirectorCommand {
  seq: number;
  type: 'LOAD_PLAN' | 'PLAY' | 'PAUSE' | 'STOP' | 'SEEK' | 'RECORD_START' | 'RECORD_STOP' | 'CAPTURE';
  planId?: number;
  timeMs?: number;
  fileName?: string;
}

interface CameraPose { position: Vector3; target: Vector3; }

/**
 * Director mode runtime (rendering plane). Lives in the main game client so it
 * can fly the LIVE camera over the real running world (bots, resources, boxes,
 * units). The studio has no engine, so it drives this through the server command
 * channel (POST /rest/director/command); this service polls GET .../command and
 * executes: load a plan, play/seek the camera flight, and record to WebM.
 *
 * DEV-ONLY: the backing REST endpoints are absent on prod (see DirectorController),
 * so poll() just no-ops there.
 */
@Injectable({providedIn: 'root'})
export class DirectorService {
  private readonly http = inject(HttpClient);
  private readonly uiSettings = inject(UiSettingsService);
  private renderer: BabylonRenderServiceAccessImpl | null = null;
  private plan: DirectorPlan | null = null;
  private clockMs = 0;
  private playing = false;
  /** True while a WebM recording is in progress (drives the client REC badge). */
  readonly recording = signal(false);
  private maxZApplied = false;
  /** Cue indices already fired this play-through (reset on load/stop/seek). */
  private readonly firedCues = new Set<number>();
  private lastSeq = 0;
  private pollHandle: ReturnType<typeof setInterval> | null = null;
  private recorder: VideoRecorder | null = null;
  /** Render state saved on record start, restored on stop (load reduction). */
  private savedShadowEnabled: boolean | null = null;
  private savedScaling: number | null = null;
  /** Extra internal-resolution downscale while recording (1 = off). Lower render
   *  + encode cost → far fewer baked-in stutters, at slightly softer output. */
  recordScale = 1.5;

  /** Wire up the camera tick + start polling the command channel. Idempotent. */
  activate(renderer: BabylonRenderServiceAccessImpl): void {
    if (this.renderer) return;
    this.renderer = renderer;
    // Clean footage: hide unit names, quest tips and the quest area marker.
    // These stay toggleable in the in-game settings dialog.
    this.uiSettings.unitNamesVisible = false;
    this.uiSettings.tipsVisible = false;
    this.uiSettings.questVisualizationVisible = false;
    renderer.directorCameraTick = (dt) => this.tick(dt);
    if (!this.pollHandle) {
      this.pollHandle = setInterval(() => this.poll(), 250);
    }
    console.log('[Director] activated — polling /rest/director/command (250ms)');
  }

  private async poll(): Promise<void> {
    try {
      const cmd = await firstValueFrom(this.http.get<DirectorCommand | null>('/rest/director/command'));
      if (!cmd || cmd.seq <= this.lastSeq) return;
      this.lastSeq = cmd.seq;
      await this.handle(cmd);
    } catch {
      /* server down or endpoint disabled (prod) — ignore */
    }
  }

  private async handle(cmd: DirectorCommand): Promise<void> {
    console.log('[Director] command', cmd.type, cmd);
    switch (cmd.type) {
      case 'LOAD_PLAN': if (cmd.planId != null) await this.loadPlan(cmd.planId); break;
      case 'PLAY': this.play(); break;
      case 'PAUSE': this.playing = false; break;
      case 'STOP': this.stop(); break;
      case 'SEEK': this.seek(cmd.timeMs ?? 0); break;
      case 'RECORD_START': this.recordStart(cmd.fileName ?? 'director.webm'); break;
      case 'RECORD_STOP': this.recordStop(); break;
      case 'CAPTURE': await this.captureCamera(); break;
    }
  }

  /**
   * Publish the live camera pose so the studio can author a keyframe from the
   * operator's current framing. Works whether or not a plan is playing (the
   * operator usually frames manually with RTS controls, then captures).
   */
  private async captureCamera(): Promise<void> {
    const cam = this.renderer?.getCamera();
    if (!cam) return;
    const pos = cam.position;
    const tgt = cam.getFrontPosition(200); // a point along the view direction
    await firstValueFrom(this.http.post('/rest/director/camera', {
      posX: pos.x, posY: pos.y, posZ: pos.z,
      targetX: tgt.x, targetY: tgt.y, targetZ: tgt.z,
    })).catch(() => { /* ignore */ });
    console.log('[Director] camera captured');
  }

  async loadPlan(id: number): Promise<void> {
    const dto = await firstValueFrom(this.http.get<{jsonContent: string}>(`/rest/director/plan/${id}`));
    this.plan = JSON.parse(dto.jsonContent) as DirectorPlan;
    this.clockMs = 0;
    this.playing = false;
    this.firedCues.clear();
    if (this.renderer) this.renderer.directorActive = true;
    this.applyPose(0);
    console.log('[Director] plan loaded:', this.plan);
  }

  play(): void {
    if (!this.plan || !this.renderer) return;
    this.renderer.directorActive = true;
    this.playing = true;
  }

  /** Stop playback and HAND CONTROL BACK to the RTS camera so the operator can
   *  scroll/zoom/re-frame again. Rewinds the clock; the camera stays where it
   *  is (the next Play re-takes control and starts from the first keyframe). */
  stop(): void {
    this.playing = false;
    this.clockMs = 0;
    this.firedCues.clear();
    if (this.renderer) this.renderer.directorActive = false;
  }

  seek(timeMs: number): void {
    const dur = this.plan?.durationMs ?? 0;
    this.clockMs = Math.max(0, Math.min(dur, timeMs));
    // Cues at/before the seek point are considered already done, so they don't
    // retro-fire when playback resumes from here.
    this.firedCues.clear();
    this.plan?.cues?.forEach((cue, idx) => {
      if (cue.time <= this.clockMs) this.firedCues.add(idx);
    });
    // Seeking implies director control — otherwise RTS scroll would override
    // the seeked pose on the very next frame.
    if (this.renderer) this.renderer.directorActive = true;
    this.applyPose(this.clockMs);
  }

  recordStart(fileName: string): void {
    const r = this.renderer;
    if (!r || !this.plan) return;
    const engine = r.getEngine();
    if (!VideoRecorder.IsSupported(engine)) {
      console.warn('[Director] VideoRecorder not supported in this browser');
      return;
    }
    // Recording is real-time — any per-frame hitch is baked into the WebM.
    // Cut the load so the capture stays smooth: shadows off (the 4096 shadow
    // map is re-rendered every frame) + lower internal resolution (cheaper to
    // render AND encode). Both are restored in recordStop.
    if (r.directionalLight) {
      this.savedShadowEnabled = r.directionalLight.shadowEnabled;
      r.directionalLight.shadowEnabled = false;
    }
    this.savedScaling = engine.getHardwareScalingLevel();
    if (this.recordScale > 1) {
      engine.setHardwareScalingLevel(this.savedScaling * this.recordScale);
    }

    this.recorder = new VideoRecorder(engine, {fps: 30});
    // maxDuration 0 = record until stopRecording(); we stop at playback end.
    void this.recorder.startRecording(fileName, 0);
    this.recording.set(true);
    this.clockMs = 0;
    this.play();
    console.log('[Director] recording →', fileName);
  }

  recordStop(): void {
    if (this.recorder) {
      void this.recorder.stopRecording(); // triggers the file download
      this.recorder = null;
    }
    // Restore render quality.
    const r = this.renderer;
    if (r) {
      if (this.savedShadowEnabled !== null && r.directionalLight) {
        r.directionalLight.shadowEnabled = this.savedShadowEnabled;
      }
      if (this.savedScaling !== null) {
        r.getEngine().setHardwareScalingLevel(this.savedScaling);
      }
    }
    this.savedShadowEnabled = null;
    this.savedScaling = null;
    this.recording.set(false);
  }

  // ===== Per-frame camera flight =====

  private tick(dtMs: number): void {
    const r = this.renderer;
    if (!r || !this.plan) return;
    const cam = r.getCamera();
    if (!cam) return;
    // Wide cinematic shots over a 5km planet need far more than the gameplay
    // maxZ (800) or distant terrain clips out.
    if (!this.maxZApplied) {
      cam.maxZ = 6000;
      this.maxZApplied = true;
    }
    if (this.playing) {
      this.clockMs += dtMs;
      const ended = this.clockMs >= this.plan.durationMs;
      if (ended) this.clockMs = this.plan.durationMs;
      this.fireDueCues();
      if (ended) {
        this.playing = false;
        this.applyPose(this.clockMs);
        if (this.recording()) this.recordStop();
        return;
      }
    }
    this.applyPose(this.clockMs);
  }

  /** Fire any attack cue whose time the clock has now reached (once each). */
  private fireDueCues(): void {
    const cues = this.plan?.cues;
    if (!cues) return;
    cues.forEach((cue, idx) => {
      if (!this.firedCues.has(idx) && cue.time <= this.clockMs) {
        this.firedCues.add(idx);
        this.fireCue(cue);
      }
    });
  }

  private fireCue(cue: DirectorCue): void {
    console.log('[Director] firing attack cue @', cue.time, 'ms');
    this.http.post('/rest/director/stage-attack', {
      x: cue.x, y: cue.y, count: cue.count,
      baseItemTypeId: cue.baseItemTypeId ?? undefined,
      targetBaseId: cue.targetBaseId ?? undefined,
    }).subscribe({error: (e) => console.warn('[Director] cue attack failed', e)});
  }

  private applyPose(t: number): void {
    const cam = this.renderer?.getCamera();
    if (!cam || !this.plan || !this.plan.cameraKeys.length) return;
    const pose = interpolatePose(this.plan.cameraKeys, t);
    cam.position.copyFrom(pose.position);
    cam.setTarget(pose.target);
  }
}

// ===== Pure interpolation helpers =====

function resolvePose(k: DirectorCameraKey): CameraPose {
  const target = new Vector3(k.target[0], k.target[1], k.target[2]);
  if (k.mode === 'free' && k.position) {
    return {position: new Vector3(k.position[0], k.position[1], k.position[2]), target};
  }
  const alpha = k.alpha ?? 0;
  const beta = k.beta ?? Math.PI / 4;
  const radius = k.radius ?? 100;
  const position = new Vector3(
    target.x + radius * Math.cos(beta) * Math.sin(alpha),
    target.y + radius * Math.sin(beta),
    target.z + radius * Math.cos(beta) * Math.cos(alpha),
  );
  return {position, target};
}

function easeFactor(f: number, type: 'linear' | 'ease' | undefined): number {
  return type === 'linear' ? f : f * f * (3 - 2 * f); // smoothstep by default
}

function lerp(a: number, b: number, f: number): number {
  return a + (b - a) * f;
}

/** Interpolate the camera pose at time `t` across the (unsorted) keyframes. */
function interpolatePose(keys: DirectorCameraKey[], t: number): CameraPose {
  const sorted = [...keys].sort((a, b) => a.time - b.time);
  if (t <= sorted[0].time) return resolvePose(sorted[0]);
  const last = sorted[sorted.length - 1];
  if (t >= last.time) return resolvePose(last);

  let a = sorted[0];
  let b = sorted[1];
  for (let i = 0; i < sorted.length - 1; i++) {
    if (t >= sorted[i].time && t <= sorted[i + 1].time) {
      a = sorted[i];
      b = sorted[i + 1];
      break;
    }
  }
  const span = (b.time - a.time) || 1;
  const f = easeFactor((t - a.time) / span, b.easing);

  // Orbit→orbit: interpolate the orbit params so the camera actually arcs.
  if (a.mode === 'orbit' && b.mode === 'orbit') {
    return resolvePose({
      time: t,
      mode: 'orbit',
      target: [
        lerp(a.target[0], b.target[0], f),
        lerp(a.target[1], b.target[1], f),
        lerp(a.target[2], b.target[2], f),
      ],
      alpha: lerp(a.alpha ?? 0, b.alpha ?? 0, f),
      beta: lerp(a.beta ?? Math.PI / 4, b.beta ?? Math.PI / 4, f),
      radius: lerp(a.radius ?? 100, b.radius ?? 100, f),
    });
  }

  // Mixed / free: resolve both ends to poses and lerp position + target.
  const pa = resolvePose(a);
  const pb = resolvePose(b);
  return {
    position: Vector3.Lerp(pa.position, pb.position, f),
    target: Vector3.Lerp(pa.target, pb.target, f),
  };
}
