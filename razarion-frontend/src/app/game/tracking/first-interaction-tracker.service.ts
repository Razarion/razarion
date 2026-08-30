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
   * Kinds already sent for this page. The camera moves on every frame while a finger is down, so
   * reporting each occurrence would be a firehose - and every question asked of this data is
   * "did it happen at all, and how long did it take".
   */
  private readonly reported = new Set<InteractionKind>();

  constructor(httpClient: HttpClient) {
    this.trackerControllerImplClient = new TrackerControllerImplClient(
      TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  public report(kind: InteractionKind): void {
    if (this.reported.has(kind)) {
      return;
    }
    this.reported.add(kind);
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
      millisSincePageLoad: pageLoadedAt ? Date.now() - pageLoadedAt : null
    } as unknown as FirstInteractionJson)
      // Telemetry must never surface as a broken game.
      .catch(error => console.warn('FirstInteractionTrackerService', error));
  }
}
