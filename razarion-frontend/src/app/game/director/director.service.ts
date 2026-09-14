import {Injectable, inject, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';
import {Camera, Scene, Vector3} from '@babylonjs/core';
import {BabylonRenderServiceAccessImpl} from '../renderer/babylon-render-service-access-impl.service';
import {UiSettingsService} from '../ui-settings.service';
import {CombatTracker} from '../renderer/combat-tracker';

/**
 * Camera keyframe. `mode` decides how it's resolved to a camera pose:
 *  - 'free'   → position + target are used directly (fly-through).
 *  - 'orbit'  → position is derived from target + spherical {alpha, beta, radius}
 *               (cinematic orbit / push-in around a point).
 *  - 'follow' → same spherical framing, but around a base that moves: the target is read from
 *               the live world every frame instead of from `target`.
 * Coordinates are Babylon world space (x, z = ground plane, y = up).
 */
export interface DirectorCameraKey {
  time: number;
  mode: 'orbit' | 'free' | 'follow';
  target: [number, number, number];
  position?: [number, number, number];
  /** Orbit and follow: azimuth (rad), elevation (rad), distance. */
  alpha?: number;
  beta?: number;
  radius?: number;
  easing?: 'linear' | 'ease';
  /** Follow only: whose units to keep in frame. */
  followBaseId?: number | null;
  /**
   * Follow only. 'base' sits on the middle of everything that base owns; 'combat' prefers where
   * that base is currently fighting and falls back to the middle when the shooting stops - which
   * is the difference between filming a factory and filming a battle.
   */
  followWhat?: 'base' | 'combat';
  /**
   * Follow only: leave `radius` empty to frame the base by its own size. A base that grows, or a
   * strike force that spreads out, then stays in shot without anyone re-authoring the plan.
   */
  autoRadius?: boolean;
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
  width?: number;
  height?: number;
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
  /** See notifyViewField: often enough for streaming, rare enough not to be a per-frame bridge call. */
  private static readonly VIEW_FIELD_NOTIFY_MS = 100;
  private lastViewFieldNotify = 0;
  /** Time constant of the follow camera. Roughly: two thirds of the way to the target in this long. */
  private static readonly FOLLOW_SMOOTHING_MS = 600;
  /** Never frame a base closer than this, however small it is - one builder must not fill the screen. */
  private static readonly FOLLOW_MIN_RADIUS = 60;
  /** Nor further away than this: beyond it a base is a smudge and the clip is of nothing. */
  private static readonly FOLLOW_MAX_RADIUS = 450;
  /** Smoothed state of the follow camera; null until the first frame of a follow key. */
  private followTarget: Vector3 | null = null;
  private followRadius = 200;
  /** Cue indices already fired this play-through (reset on load/stop/seek). */
  private readonly firedCues = new Set<number>();
  private lastSeq = 0;
  /** The first answer from the channel is a standing order from before this client existed. */
  private firstPoll = true;
  /**
   * What the command channel is doing, for the badge in the client. 'waiting' until the first
   * answer arrives; after that either 'connected' or the reason it is not. The studio can only
   * see polls that got through - an unauthorised client never reaches the server at all - so this
   * side has to be able to say it out loud.
   */
  readonly channelState = signal<'waiting' | 'connected' | 'unauthorized' | 'disabled' | 'offline'>('waiting');
  /** Whether the last poll got through, so the console says so once rather than four times a second. */
  private pollHealthy = false;
  /** HTTP status of the failure already reported; -1 = nothing reported yet. */
  private pollFailureStatus = -1;
  private pollHandle: ReturnType<typeof setInterval> | null = null;
  /** See recordStart: how long the camera holds the first pose at the new size before the take. */
  private static readonly RECORD_PRE_ROLL_MS = 4000;
  private preRollHandle: ReturnType<typeof setTimeout> | null = null;
  private mediaRecorder: MediaRecorder | null = null;
  /** The observer that asks for one captured frame per render; removed on stop. */
  private frameObserver: ReturnType<Scene['onAfterRenderObservable']['add']> | null = null;
  private recordScene: Scene | null = null;
  /** Render state saved on record start, restored on stop. */
  private savedShadowEnabled: boolean | null = null;
  private savedRenderWidth: number | null = null;
  private savedRenderHeight: number | null = null;
  private savedFovMode: number | null = null;
  private savedFov: number | null = null;
  /** What the file comes out as, regardless of the window. The studio sends 1080x1920 for the
   *  portrait take the reels and Shorts are cut from, and 1920x1080 for the landscape one X gets. */
  recordWidth = 1920;
  recordHeight = 1080;

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
      // The query parameter is the client saying "I am here": the server records the moment and
      // the studio shows it, so "is anything listening?" has an answer other than watching the
      // camera not move.
      const cmd = await firstValueFrom(
        this.http.get<DirectorCommand | null>('/rest/director/command', {params: {client: 'render'}}));
      if (!this.pollHealthy) {
        this.pollHealthy = true;
        this.pollFailureStatus = -1;
        this.channelState.set('connected');
        console.log('[Director] command channel reachable — the studio can drive this client');
      }
      if (!cmd || cmd.seq <= this.lastSeq) return;
      this.lastSeq = cmd.seq;
      // The command slot holds the last thing the studio said, for as long as the server runs, and
      // a client that has just opened starts from seq 0 - so without this it obeys an instruction
      // that may be hours old. That is confusing for a camera flight and unacceptable for the rest:
      // a client opened after a recording session would start recording by itself. A fresh client
      // comes up as an ordinary game client, adopts the plan so it is ready, and waits to be told.
      if (this.firstPoll) {
        this.firstPoll = false;
        if (cmd.type === 'LOAD_PLAN' && cmd.planId != null) {
          await this.loadPlan(cmd.planId, /*quiet*/ true);
        } else {
          console.log('[Director] ignoring the standing', cmd.type, 'from before this client opened');
        }
        return;
      }
      await this.handle(cmd);
    } catch (e: any) {
      // Four failures a second must not fill the console, but silence is worse: swallowing this
      // is why a client that is merely signed in as the wrong user looks exactly like a client
      // that is not running at all. Report the first failure, and each change of reason.
      const status = e?.status ?? 0;
      if (this.pollFailureStatus !== status) {
        this.pollFailureStatus = status;
        this.pollHealthy = false;
        this.channelState.set(
          status === 401 || status === 403 ? 'unauthorized' : status === 404 ? 'disabled' : 'offline');
        console.warn(`[Director] command channel unreachable (HTTP ${status}). ${explainPollFailure(status)}`);
      }
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
      case 'RECORD_START': this.recordStart(cmd.fileName ?? 'director.webm', cmd.width, cmd.height); break;
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

  /**
   * @param quiet keep the plan but leave the camera alone — see the first-poll handling in poll().
   */
  async loadPlan(id: number, quiet = false): Promise<void> {
    const dto = await firstValueFrom(this.http.get<{jsonContent: string}>(`/rest/director/plan/${id}`));
    this.plan = JSON.parse(dto.jsonContent) as DirectorPlan;
    this.clockMs = 0;
    this.playing = false;
    this.firedCues.clear();
    if (quiet) {
      console.log('[Director] plan adopted, camera left alone:', id);
      return;
    }
    if (this.renderer) this.renderer.directorActive = true;
    // A jump, not a move: the whole view changed at once, so the engine hears about it at once.
    this.applyPose(0);
    this.notifyViewField(true);
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
    this.notifyViewField(true);
  }

  /**
   * Record the viewport while the plan plays.
   *
   * Two things are deliberately not Babylon's VideoRecorder, which this used to be.
   *
   * It asked the browser for a stream at a frame rate, and a canvas stream hands over a frame only
   * when the canvas is seen to change - so a held shot over a quiet stretch of world returns almost
   * no frames, and the file reads as broken rather than still. Frames are requested here instead,
   * one per render off Babylon's own loop, which is indifferent to whether anything moved.
   *
   * And the output size was whatever the window happened to be, divided by a scaling factor: a
   * 1280 window at scale 1.5 recorded 853x480, which is below what any of the networks want and
   * cannot be recovered afterwards. The size is set explicitly for the duration instead. The
   * viewport looks stretched while it runs; that is the trade for not keeping a second renderer.
   */
  recordStart(fileName: string, width = 1920, height = 1080): void {
    const r = this.renderer;
    if (!r || !this.plan) return;
    this.recordWidth = width;
    this.recordHeight = height;
    const engine = r.getEngine();
    const canvas = engine.getRenderingCanvas();
    const mime = pickClipMime();
    if (!canvas || !mime) {
      console.warn('[Director] this browser cannot record the viewport');
      return;
    }

    // The 4096 shadow map is re-rendered every frame and the recording is real time: any hitch is
    // baked into the file. Restored in recordStop.
    if (r.directionalLight) {
      this.savedShadowEnabled = r.directionalLight.shadowEnabled;
      r.directionalLight.shadowEnabled = false;
    }

    // Not a preference for the duration of the take: the name plate is the player's own name,
    // drawn into the canvas the recorder captures. Released again in recordStop.
    this.uiSettings.setNamesLockedOff(true);
    this.uiSettings.tipsVisible = false;
    this.uiSettings.questVisualizationVisible = false;

    this.savedRenderWidth = engine.getRenderWidth();
    this.savedRenderHeight = engine.getRenderHeight();
    // Babylon holds the vertical field of view when the aspect changes, so a plan framed in a
    // landscape window and recorded upright would lose both sides of every shot. Holding the
    // horizontal field instead keeps what was framed and adds sky and ground - the same choice the
    // studio makes for its portrait clips. Restored in recordStop.
    // Switching the mode alone is not enough: Babylon then reads the same `fov` as the horizontal
    // angle, and 0.8 rad across a portrait frame is a 1.6x zoom on a 16:9 window - the opposite of
    // holding the field. The angle is converted to the horizontal one the window actually had.
    const cam = r.getCamera();
    const windowAspect = this.savedRenderWidth / this.savedRenderHeight;
    if (cam && this.recordWidth / this.recordHeight < windowAspect) {
      this.savedFovMode = cam.fovMode;
      this.savedFov = cam.fov;
      if (cam.fovMode !== Camera.FOVMODE_HORIZONTAL_FIXED) {
        cam.fov = 2 * Math.atan(Math.tan(cam.fov / 2) * windowAspect);
      }
      cam.fovMode = Camera.FOVMODE_HORIZONTAL_FIXED;
    }
    engine.setSize(this.recordWidth, this.recordHeight);

    // Pre-roll. The new shape sees ground the window did not, and that ground is built one tile per
    // frame: recorded straight away, a portrait take spent its first four seconds at 8 fps. The
    // camera holds the first pose of the plan until it is there, and only then does the take start.
    this.recording.set(true);
    this.playing = false;
    this.clockMs = 0;
    r.directorActive = true;
    this.applyPose(0);
    this.notifyViewField(true);
    console.log(`[Director] pre-roll ${DirectorService.RECORD_PRE_ROLL_MS} ms at ${this.recordWidth}x${this.recordHeight}`);
    this.preRollHandle = setTimeout(() => {
      this.preRollHandle = null;
      const scene = r.getScene();
      const stream = canvas.captureStream(0);
      const frameTrack = stream.getVideoTracks()[0] as CanvasCaptureMediaStreamTrack;
      this.frameObserver = scene.onAfterRenderObservable.add(() => frameTrack.requestFrame());

      const chunks: Blob[] = [];
      const recorder = new MediaRecorder(stream, {mimeType: mime, videoBitsPerSecond: 12_000_000});
      recorder.ondataavailable = (e) => { if (e.data.size) chunks.push(e.data); };
      recorder.onstop = () => {
        stream.getTracks().forEach(track => track.stop());
        downloadBlob(new Blob(chunks, {type: mime}), withExtensionFor(fileName, mime));
      };
      recorder.start();

      this.mediaRecorder = recorder;
      this.recordScene = scene;
      this.clockMs = 0;
      this.play();
      console.log(`[Director] recording ${this.recordWidth}x${this.recordHeight} as ${mime}`);
    }, DirectorService.RECORD_PRE_ROLL_MS);
  }

  recordStop(): void {
    // Stopped during the pre-roll: nothing was recorded, so there is no file to hand over.
    if (this.preRollHandle !== null) {
      clearTimeout(this.preRollHandle);
      this.preRollHandle = null;
    }
    // The blob is assembled and downloaded in the recorder's onstop; stopping here only asks.
    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.stop();
    }
    this.mediaRecorder = null;
    // Left attached, this keeps asking a dead track for a frame on every frame the client draws
    // for the rest of the session.
    if (this.frameObserver && this.recordScene) {
      this.recordScene.onAfterRenderObservable.remove(this.frameObserver);
    }
    this.frameObserver = null;
    this.recordScene = null;

    // Restore render quality and the size the window actually is.
    const r = this.renderer;
    if (r) {
      if (this.savedShadowEnabled !== null && r.directionalLight) {
        r.directionalLight.shadowEnabled = this.savedShadowEnabled;
      }
      if (this.savedRenderWidth !== null && this.savedRenderHeight !== null) {
        r.getEngine().setSize(this.savedRenderWidth, this.savedRenderHeight);
        r.getEngine().resize();
      }
      const cam = r.getCamera();
      if (cam && this.savedFovMode !== null) cam.fovMode = this.savedFovMode;
      if (cam && this.savedFov !== null) cam.fov = this.savedFov;
    }
    this.savedFovMode = null;
    this.savedFov = null;
    this.savedShadowEnabled = null;
    this.savedRenderWidth = null;
    this.savedRenderHeight = null;
    // Hand the name plates back to whatever the operator had chosen.
    this.uiSettings.setNamesLockedOff(false);
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
        this.applyPose(this.clockMs, dtMs);
        if (this.recording()) this.recordStop();
        return;
      }
    }
    this.applyPose(this.clockMs, dtMs);
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

  private applyPose(t: number, dtMs: number | null = null): void {
    const cam = this.renderer?.getCamera();
    if (!cam || !this.plan || !this.plan.cameraKeys.length) return;
    const pose = interpolatePose(this.plan.cameraKeys, t, key => this.resolveFollow(key, dtMs));
    cam.position.copyFrom(pose.position);
    cam.setTarget(pose.target);
    this.notifyViewField();
  }

  /**
   * Where a follow key is looking, right now.
   *
   * Two things are smoothed rather than taken raw. The centre of a base jumps when a unit is built
   * at the far edge or destroyed, and the framing distance jumps with it; unsmoothed, the camera
   * twitches on every event, which reads as a broken camera rather than a moving base. The
   * smoothing is per-second so it behaves the same at 30 and at 144 frames.
   *
   * `dtMs` null means "do not smooth": a plan just loaded or seeked, and there is nothing to ease
   * from - easing there would start every recording with a slow drift out of the previous shot.
   */
  private resolveFollow(key: DirectorCameraKey, dtMs: number | null): { target: Vector3; radius: number } | null {
    const renderer = this.renderer;
    if (!renderer || key.followBaseId == null) {
      return null;
    }
    const extent = renderer.baseExtent(key.followBaseId);
    // A base with nothing in it is a base that has just been destroyed. Hold the last framing
    // rather than snapping to the origin, so the shot ends on the wreckage.
    const wanted = (key.followWhat === 'combat'
      ? renderer.combatTracker.centre(CombatTracker.DEFAULT_WINDOW_MS, key.followBaseId)
      : null) ?? extent?.centre ?? this.followTarget;
    if (!wanted) {
      return null;
    }
    const wantedRadius = key.autoRadius && extent
      // Twice the spread plus a margin puts the far edge inside the frame rather than on it. The
      // ceiling is not politeness: past a few hundred units a base is a smudge on a map, and the
      // one thing a clip cannot survive is not being able to tell what it is of.
      ? clamp(extent.radius * 2.5 + 50, DirectorService.FOLLOW_MIN_RADIUS, DirectorService.FOLLOW_MAX_RADIUS)
      : clamp(key.radius ?? 200, DirectorService.FOLLOW_MIN_RADIUS, DirectorService.FOLLOW_MAX_RADIUS);

    if (dtMs == null || !this.followTarget) {
      this.followTarget = wanted.clone();
      this.followRadius = wantedRadius;
    } else {
      const f = 1 - Math.exp(-dtMs / DirectorService.FOLLOW_SMOOTHING_MS);
      this.followTarget = Vector3.Lerp(this.followTarget, wanted, f);
      this.followRadius += (wantedRadius - this.followRadius) * f;
    }
    return {target: this.followTarget, radius: this.followRadius};
  }

  /**
   * Tell the engine where the camera is looking.
   *
   * Moving a Babylon camera moves nothing else: the terrain streams, and items become visible,
   * because the engine is handed a view field - which the RTS controls do on every scroll and the
   * ResizeObserver does on every resize. Flying a plan does neither, so a filmed flight showed the
   * clear colour and nothing in it, and the only thing that ever fixed it was resizing the window,
   * which looks like a rendering bug and is a message that was never sent.
   *
   * Throttled rather than per frame: this crosses into the WASM engine, and terrain that streams
   * ten times a second keeps up with any camera move worth filming.
   */
  private notifyViewField(force = false): void {
    const now = performance.now();
    if (!force && now - this.lastViewFieldNotify < DirectorService.VIEW_FIELD_NOTIFY_MS) {
      return;
    }
    this.lastViewFieldNotify = now;
    this.renderer?.onViewFieldChanged();
  }
}

/**
 * The container the browser will actually give us, best first. Chrome muxes H.264 straight into
 * MP4, which is what every network wants and saves the pipeline a re-encode; where that is missing
 * WebM is always there and ffmpeg converts it later. An unsupported codec must not lose the take.
 */
function pickClipMime(): string | null {
  const candidates = [
    'video/mp4;codecs=avc1.42E01E',
    'video/mp4',
    'video/webm;codecs=vp9',
    'video/webm;codecs=vp8',
    'video/webm',
  ];
  return candidates.find(type => MediaRecorder.isTypeSupported(type)) ?? null;
}

/** The name the operator asked for, with the extension the browser actually produced. */
function withExtensionFor(fileName: string, mime: string): string {
  const wanted = mime.startsWith('video/mp4') ? 'mp4' : 'webm';
  return fileName.replace(/\.(mp4|webm)$/i, '') + '.' + wanted;
}

function downloadBlob(blob: Blob, fileName: string): void {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = fileName;
  document.body.appendChild(a);
  a.click();
  a.remove();
  // The object URL owns the blob until revoked, and these are tens of megabytes.
  URL.revokeObjectURL(url);
  console.log('[Director] recording saved as', fileName, `(${(blob.size / 1024 / 1024).toFixed(1)} MB)`);
}

/**
 * What each failure actually means, because the status alone sends people to the wrong place.
 * 403 in particular looks like a broken setup and is almost always the plainest thing: the studio
 * and the game are different origins with different logins, so signing in to one signs in to
 * neither the other.
 */
function explainPollFailure(status: number): string {
  switch (status) {
    case 0:
      return 'No answer at all — is the server running?';
    case 401:
    case 403:
      return 'Signed in, but not as an admin (or not signed in here at all). The studio tab and '
        + 'this tab are separate origins with separate sessions — log in again in THIS tab.';
    case 404:
      return 'The endpoint is not there: director mode is switched off on this server '
        + '(razarion.director.enabled).';
    default:
      return 'Unexpected — see the network tab.';
  }
}

// ===== Pure interpolation helpers =====

/**
 * What a follow key is pointing at this frame, or null when it cannot be resolved (no base
 * chosen, base gone, no renderer). A null falls back to the key's stored target, so a plan
 * written against a base that no longer exists still produces a camera rather than an exception.
 */
type FollowResolver = (key: DirectorCameraKey) => { target: Vector3; radius: number } | null;

function resolvePose(k: DirectorCameraKey, follow?: FollowResolver): CameraPose {
  if (k.mode === 'follow' && follow) {
    const live = follow(k);
    if (live) {
      return orbitPose(live.target, k.alpha ?? 0, k.beta ?? Math.PI / 4, live.radius);
    }
  }
  const target = new Vector3(k.target[0], k.target[1], k.target[2]);
  if (k.mode === 'free' && k.position) {
    return {position: new Vector3(k.position[0], k.position[1], k.position[2]), target};
  }
  return orbitPose(target, k.alpha ?? 0, k.beta ?? Math.PI / 4, k.radius ?? 100);
}

/** Camera placed on a sphere around `target`: azimuth, elevation, distance. */
function clamp(value: number, low: number, high: number): number {
  return Math.max(low, Math.min(high, value));
}

function orbitPose(target: Vector3, alpha: number, beta: number, radius: number): CameraPose {
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
function interpolatePose(keys: DirectorCameraKey[], t: number, follow?: FollowResolver): CameraPose {
  const sorted = [...keys].sort((a, b) => a.time - b.time);
  if (t <= sorted[0].time) return resolvePose(sorted[0], follow);
  const last = sorted[sorted.length - 1];
  if (t >= last.time) return resolvePose(last, follow);

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
  const pa = resolvePose(a, follow);
  const pb = resolvePose(b, follow);
  return {
    position: Vector3.Lerp(pa.position, pb.position, f),
    target: Vector3.Lerp(pa.target, pb.target, f),
  };
}
