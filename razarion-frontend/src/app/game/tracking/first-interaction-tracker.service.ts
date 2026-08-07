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
