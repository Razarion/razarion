import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

// Session-only UI preferences. Values reset on page reload by design — the
// user picked "Session" persistence, so no localStorage round-trip.
@Injectable({providedIn: 'root'})
export class UiSettingsService {
  unitNamesVisible$ = new BehaviorSubject<boolean>(true);

  get unitNamesVisible(): boolean {
    return this.unitNamesVisible$.value;
  }

  set unitNamesVisible(value: boolean) {
    if (this.unitNamesVisible$.value !== value) {
      this.unitNamesVisible$.next(value);
    }
  }
}
