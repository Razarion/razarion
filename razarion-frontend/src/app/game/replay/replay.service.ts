import {Injectable, inject, signal} from '@angular/core';
import {DirectorService} from '../director/director.service';
import {BabylonRenderServiceAccessImpl} from '../renderer/babylon-render-service-access-impl.service';

/** What razarion-recorder's replay proxy says about the replay it plays to this client. */
interface ReplayStatus {
  baseId: number;
  playhead: number;
  connected: boolean;
  /** Where the followed base is, or fights - see DirectorService.setFollowHint. */
  focus: { x: number, y: number, radius: number, fight: boolean, lock?: boolean } | null;
}

/**
 * Replay mode (/replay route). The client is served by razarion-recorder's replay proxy, whose
 * game connection plays a recorded stretch of the planet instead of the live one; this side only
 * keeps the watcher's own things out of the picture and puts the camera on the watched base.
 *
 * Nothing the watcher does reaches a game: the proxy ignores every order and every write.
 */
@Injectable({providedIn: 'root'})
export class ReplayService {
  private readonly directorService = inject(DirectorService);
  /** The recorded moment on screen, for the badge; null until the proxy has answered. */
  readonly playhead = signal<number | null>(null);
  readonly baseId = signal<number | null>(null);
  private followedBaseId: number | null = null;
  private renderer: BabylonRenderServiceAccessImpl | null = null;
  private pollHandle: ReturnType<typeof setInterval> | null = null;

  activate(renderer: BabylonRenderServiceAccessImpl): void {
    if (this.renderer) return;
    this.renderer = renderer;
    // For razarion-recorder filming a moment headless (lib/moments.mjs): whether the camera is on
    // the base yet, and the director's canvas recording - canvas only, names locked off.
    (window as any).razReplay = {
      ready: () => this.followedBaseId != null,
      record: (fileName: string, width: number, height: number) => this.directorService.recordStart(fileName, width, height),
      stop: () => this.directorService.recordStop(),
      recording: () => this.directorService.recording(),
    };
    this.pollHandle = setInterval(() => this.poll(), 1000);
    this.poll();
  }

  private async poll(): Promise<void> {
    let status: ReplayStatus | null;
    try {
      // The token names the replay this client plays (razarion-recorder sessionToken); none is the
      // one a person opened.
      const token = localStorage.getItem('app.token');
      const response = await fetch('/replay/status' + (token ? '?session=' + encodeURIComponent(token) : ''), {cache: 'no-store'});
      status = (await response.json()) as ReplayStatus | null;
    } catch {
      // Not served by the replay proxy: a /replay page on the live game shows the live game
      if (this.pollHandle) {
        clearInterval(this.pollHandle);
        this.pollHandle = null;
      }
      return;
    }
    if (!status) return;
    this.playhead.set(status.playhead);
    this.baseId.set(status.baseId);
    this.directorService.setFollowHint(status.focus);
    // The camera takes over once, and again only when the recorder plays another base - and not
    // before there is something to look at: the follow camera without a target looks at the
    // planet's corner, where there is nothing to draw.
    if (this.renderer?.getCamera() && status.focus && status.baseId !== this.followedBaseId) {
      try {
        this.directorService.followBase(this.renderer, status.baseId);
        this.followedBaseId = status.baseId;
      } catch (error) {
        console.warn('[Replay] camera not taken over yet, next try in a second', error);
      }
    }
  }
}
