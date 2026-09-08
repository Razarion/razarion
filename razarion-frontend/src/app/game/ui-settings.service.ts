import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

// Session-only UI preferences. Values reset on page reload by design — the
// user picked "Session" persistence, so no localStorage round-trip.
@Injectable({providedIn: 'root'})
export class UiSettingsService {
  unitNamesVisible$ = new BehaviorSubject<boolean>(true);
  /** In-game quest tips (tip tasks, out-of-view direction markers). */
  tipsVisible$ = new BehaviorSubject<boolean>(true);
  /** In-world quest area/place marker (the glowing ground zone). */
  questVisualizationVisible$ = new BehaviorSubject<boolean>(true);
  /**
   * The chat panel. Independent of CockpitDisplayService.showChatCockpit, which says whether the
   * game offers a chat at all — this says whether the player wants to see the one that is offered.
   */
  chatVisible$ = new BehaviorSubject<boolean>(true);

  /**
   * While true, unit names stay off no matter what anyone asks for.
   *
   * The name plate carries the player's own name and is drawn into the canvas, which is what the
   * recorder captures - so it is the one piece of a person that can end up in a published clip.
   * A default of "off in director mode" was not enough: a preference that can be switched back on
   * is one settings dialog away from a name in a reel. The preference itself is left alone and
   * comes back when filming stops.
   */
  private namesLockedOff = false;
  /** What the user actually wants, remembered across a lock. */
  private unitNamesPreference = true;

  get unitNamesVisible(): boolean {
    return this.unitNamesVisible$.value;
  }

  set unitNamesVisible(value: boolean) {
    this.unitNamesPreference = value;
    const effective = value && !this.namesLockedOff;
    if (this.unitNamesVisible$.value !== effective) {
      this.unitNamesVisible$.next(effective);
    }
  }

  /** Lock names off for the duration of a recording, then restore what the user had. */
  setNamesLockedOff(locked: boolean): void {
    if (this.namesLockedOff === locked) {
      return;
    }
    this.namesLockedOff = locked;
    const effective = this.unitNamesPreference && !locked;
    if (this.unitNamesVisible$.value !== effective) {
      this.unitNamesVisible$.next(effective);
    }
  }

  get tipsVisible(): boolean {
    return this.tipsVisible$.value;
  }

  set tipsVisible(value: boolean) {
    if (this.tipsVisible$.value !== value) {
      this.tipsVisible$.next(value);
    }
  }

  get questVisualizationVisible(): boolean {
    return this.questVisualizationVisible$.value;
  }

  set questVisualizationVisible(value: boolean) {
    if (this.questVisualizationVisible$.value !== value) {
      this.questVisualizationVisible$.next(value);
    }
  }

  get chatVisible(): boolean {
    return this.chatVisible$.value;
  }

  set chatVisible(value: boolean) {
    if (this.chatVisible$.value !== value) {
      this.chatVisible$.next(value);
    }
  }
}
