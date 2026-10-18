import {Injectable, NgZone} from '@angular/core';
import {BabylonRenderServiceAccessImpl} from './renderer/babylon-render-service-access-impl.service';
import {SelectionService} from './selection.service';
import {Diplomacy} from '../gwtangular/GwtAngularFacade';
import {FirstInteractionTrackerService} from './tracking/first-interaction-tracker.service';

/**
 * The builder of a freshly placed start base comes selected, so the factory button of quest 358 is
 * on screen at once. On PROD 14.-16.10.2026 more than half of the players who left at 358 never
 * selected the builder (34 of 59), while all but one of those who passed did. Building stays the
 * player's own step (feedback: the first build is not automated) - only the tap that finds the unit
 * is taken away.
 * <p>
 * Only while nothing has been selected since the base was placed: a player who already picked
 * something keeps it.
 */
@Injectable({
  providedIn: 'root'
})
export class StartBuilderSelectService {
  private static readonly POLL_MILLIS = 250;
  /** The builder spawns a moment after the placement; give up after this. */
  private static readonly WAIT_MILLIS = 20000;
  private timer: ReturnType<typeof setInterval> | null = null;

  constructor(private readonly renderService: BabylonRenderServiceAccessImpl,
              private readonly selectionService: SelectionService,
              private readonly tracker: FirstInteractionTrackerService,
              private readonly zone: NgZone) {
    this.renderService.startBasePlacedListener = () => this.onStartBasePlaced();
  }

  private onStartBasePlaced(): void {
    this.stop();
    const placedAt = performance.now();
    const selectionCount = this.selectionService.ownSelectionCount;
    this.timer = this.zone.runOutsideAngular(() => setInterval(() => {
      const waited = performance.now() - placedAt;
      if (this.selectionService.ownSelectionCount !== selectionCount || this.selectionService.hasOwnSelection()
        || waited > StartBuilderSelectService.WAIT_MILLIS) {
        this.stop();
        return;
      }
      const builder = this.renderService.getBabylonBaseItemsByDiplomacy(Diplomacy.OWN)
        .find(item => item.getBaseItemType().getBuilderType() != null);
      if (!builder) {
        return;
      }
      this.stop();
      // Inside Angular: the item cockpit opens on the selection.
      this.zone.run(() => this.selectionService.selectOwnItems([builder], true));
      this.tracker.report('BUILDER_AUTO_SELECTED', 'after=' + (Math.round(waited / 100) / 10) + 's');
    }, StartBuilderSelectService.POLL_MILLIS));
  }

  private stop(): void {
    if (this.timer !== null) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }
}
