import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {FirstInteractionJson, TrackerControllerImplClient} from '../../generated/razarion-share';
import {TypescriptGenerator} from '../../backend/typescript-generator';

/**
 * What the player did with the controls, the first time they did it.
 *
 * The funnel counts how many mobile players stop before the first quest but cannot say whether
 * they were able to steer at all. Until touch support existed the camera answered only to WASD and
 * the mouse wheel, and the evidence for that was indirect - tips complaining their target was off
 * screen. Absence is the signal here: a session that never reports CAMERA_PAN_TOUCH never found
 * the gesture, against the denominator of sessions that started successfully. That is a yes/no
 * question and needs days of data, not the weeks a rate comparison needs.
 */
export type InteractionKind =
/**
 * A pointer went down on the game field, before anything has been decided about what it was. The
 * only kind here that is not an outcome.
 * <p>
 * Every other kind records something that worked, so a finger that touches the screen and gets no
 * answer leaves no trace at all - and the paid mobile cohort produces exactly that: 37 sessions
 * with a running game in seven days, one interaction between them, no base. That number cannot
 * tell "never reached for it" from "reached for it and the game did not respond", and those are
 * opposite repairs. Against this it can.
 * <p>
 * Reported for a mouse as well as a finger. The question is mobile, but a kind that exists only on
 * one device cannot be checked against the other.
 */
  | 'POINTER_DOWN'
/**
 * A pointer went down anywhere on the page, seen from the window in the capture phase rather than
 * from the canvas.
 * <p>
 * The pair POINTER_DOWN / POINTER_DOWN_PAGE is the point: the second without the first means the
 * touch arrived and something else took it, and neither means the browser never delivered it. See
 * {@link PagePointerProbe}, which carries the landing site in the detail.
 */
  | 'POINTER_DOWN_PAGE'
/**
 * Which build of the client the browser is running, as the bundle's file name. Not a player action
 * at all - see {@link clientBuildStamp} for why a deploy is not proof that anybody is running it.
 */
  | 'CLIENT_BUILD'
/**
 * The running game engine hit something it could not carry out. Not a player action, and the only
 * kind here that is a defect rather than an observation.
 * <p>
 * It exists because the engine's own failures were console-only, and on a phone in an in-app
 * browser there is no console. The Meta cohort renders terrain, resources and the bot ground area
 * but never a single unit, building or bot - and the tick stream, which is the one channel that
 * carries those, answers "could not build a tick" in silence when it fails. The detail names what
 * failed.
 */
  | 'ENGINE_ERROR'
/**
 * Where the seconds before the game went: downloading, parsing, booting. Not a player action.
 * <p>
 * Half of the in-app sessions are lost between PAGE_LOADED and WASM_BOOTSTRAP, and "make it
 * smaller" and "show something sooner" are different repairs. This is the split that decides which.
 * See {@link StartupTiming}.
 */
  | 'STARTUP_TIMING'
/**
 * What the start downloaded, by category, at three moments. Not a player action.
 * <p>
 * STARTUP_TIMING says how long the download took; this says what was in it. Of roughly 18.8 MB per
 * start, only the 2.4 MB of JavaScript and 1.3 MB of WebAssembly can be weighed in the repository -
 * the models, textures and audio live in the database, and four fifths of a start has therefore
 * never been measured at all. See {@link StartupPayload}.
 */
  | 'STARTUP_PAYLOAD'
/**
 * The base placer appeared on screen. Not a player action at all - the only kind here that the
 * player did not cause - and it is here because the question it answers cannot be asked without it.
 * <p>
 * Placing the starting base is the first thing the game asks of anybody, and 63 of the 95 players
 * who reached a running game and built nothing had reported no interaction of any kind. That number
 * cannot say whether they were shown the placer and ignored it, or never got one.
 */
  | 'PLACER_SHOWN'
/**
 * The player tapped a spot the placer refused - occupied ground, wrong terrain, an enemy too near,
 * outside the allowed area. They reached for the one thing the game wants from them and were told
 * no, which is a different failure from never reaching at all and needs a different repair.
 */
  | 'PLACER_REJECTED'
/**
 * The placer opened while the terrain under the screen centre was not there yet, so it had to be
 * put down on a position computed from the camera rather than picked off the ground.
 * <p>
 * Not a player action, and not in itself a failure - the placer is on screen and usable. It is
 * reported because the state used to be completely silent, and while it was, it cost every base in
 * the Meta webview: nothing positioned the placer at all back then, so it stood at the world origin
 * with its hint bubble attached to it, off screen, retrying once a second forever. Three sessions
 * reported PLACER_SHOWN, waited seventeen to fifty seconds and left without one finger reaching the
 * canvas.
 * <p>
 * Reported once per session like every other kind, though the condition is re-checked each second.
 */
  | 'PLACER_NO_TERRAIN'
/** The placement went through. The base exists from here on. */
  | 'PLACER_CONFIRMED'
/** The camera kinds name the input, not the effect. "The camera moved" is true on a desktop too
 *  and always has been, so it cannot answer whether the touch gesture was discovered. */
  | 'CAMERA_PAN_TOUCH'
  | 'CAMERA_PINCH'
  | 'CAMERA_KEYBOARD'
  | 'CAMERA_WHEEL'
  /** A unit or building was picked. On a phone this can only come from a tap: the marquee ignores
   *  touch pointers and there is no keyboard, so it doubles as proof the tap path works. */
  | 'SELECT'
  /** An order was actually issued - move, attack, harvest, load, finalize build. Selecting without
   *  ever commanding is a different defect from never selecting, and the two look identical in
   *  every other record we keep. */
  | 'COMMAND';

@Injectable({
  providedIn: 'root'
})
export class FirstInteractionTrackerService {
  private readonly trackerControllerImplClient: TrackerControllerImplClient;
  /**
   * What has already been sent for this page: a kind on its own, or a kind and its detail where
   * there is one. The camera moves on every frame while a finger is down, so reporting each
   * occurrence would be a firehose - and every question asked of the interaction kinds is "did it
   * happen at all, and how long did it take".
   * <p>
   * Keying on the detail as well exists for ENGINE_ERROR, where the reasons differ and the second
   * one is not a repeat of the first. {@link #MAX_PER_KIND} keeps that from becoming the firehose
   * this set was built to prevent: a broken tick repeats every tick, and after a handful of
   * distinct reasons nothing new is being learned.
   */
  private readonly reported = new Set<string>();
  private readonly countPerKind = new Map<InteractionKind, number>();
  private static readonly MAX_PER_KIND = 5;

  constructor(httpClient: HttpClient) {
    this.trackerControllerImplClient = new TrackerControllerImplClient(
      TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  /**
   * @param detail optional `name=value` pairs describing the circumstances, for the kinds whose
   *        existence is not the whole answer. See {@link FirstInteractionJson#detail}.
   */
  public report(kind: InteractionKind, detail?: string): void {
    const key = detail ? kind + '|' + detail : kind;
    if (this.reported.has(key)) {
      return;
    }
    const seen = this.countPerKind.get(kind) ?? 0;
    if (seen >= FirstInteractionTrackerService.MAX_PER_KIND) {
      return;
    }
    this.reported.add(key);
    this.countPerKind.set(kind, seen + 1);
    const global = window as any;
    const gameSessionUuid = global.RAZ_gameSessionUuid;
    if (!gameSessionUuid) {
      // Without it the record cannot be told apart by device, which is the whole point - the
      // userAgent lives on the PAGE_LOADED startup task and is keyed by the game session.
      return;
    }
    const pageLoadedAt = global.RAZ_pageLoadedAt;
    this.trackerControllerImplClient.firstInteraction({
      gameSessionUuid,
      kind,
      detail: detail ?? null,
      millisSincePageLoad: pageLoadedAt ? Date.now() - pageLoadedAt : null
    } as unknown as FirstInteractionJson)
      // Telemetry must never surface as a broken game.
      .catch(error => console.warn('FirstInteractionTrackerService', error));
  }
}
