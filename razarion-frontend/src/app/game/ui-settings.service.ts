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

  get unitNamesVisible(): boolean {
    return this.unitNamesVisible$.value;
  }

  set unitNamesVisible(value: boolean) {
    if (this.unitNamesVisible$.value !== value) {
      this.unitNamesVisible$.next(value);
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
