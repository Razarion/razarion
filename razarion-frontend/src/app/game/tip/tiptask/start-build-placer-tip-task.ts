import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BaseItemPlacerPresenterEvent} from '../../renderer/base-item-placer-presenter.impl';
import {Diplomacy} from '../../../gwtangular/GwtAngularFacade';
import {TipStallReason, TipTaskName} from '../tip-stall';

export class StartBuildPlacerTipTask extends AbstractTipTask {
  /**
   * Right after the actor is selected the cockpit needs a frame or two, so the first polls are
   * quick. A wait that outlives that is not a rendering race any more but a button that stays
   * disabled until the game state changes, and polling it four times a second buys nothing.
   */
  private static readonly FAST_RETRY_MILLIS = 200;
  private static readonly SLOW_RETRY_MILLIS = 1000;
  private static readonly FAST_RETRY_DURATION_MILLIS = 5000;
  private retryTimeout: ReturnType<typeof setTimeout> | null = null;
  private firstRetryTime = 0;

  constructor(private readonly toBeBuiltItemTypeId: number, tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  isFulfilled(): boolean {
    return !!this.tipTaskContext.babylonBaseItemImpl?.isSelected() &&
      !!this.tipService.renderService.getBabylonBaseItemByDiplomacyItemType(Diplomacy.OWN, this.toBeBuiltItemTypeId);
  }

  getTaskName(): string {
    return TipTaskName.START_BUILD_PLACER;
  }

  start(): void {
    // Before the branches below: the actor has to be pointed at while the cockpit is missing too -
    // that is precisely the case where it is missing *because* the builder drove out of the view.
    this.trackActor();
    this.startActorTracking();
    this.tipTaskContext.babylonBaseItemImpl?.setSelectionCallback((active: boolean) => {
      if (!active) {
        // Only fail if the builder is still deselected after the grace period
        this.onSelectionLost(() => !this.tipTaskContext.babylonBaseItemImpl?.isSelected());
      }
    });
    this.checkSelectionLost();
    this.tipService.renderService.setBaseItemPlacerCallback((event) => {
      switch (event) {
        case BaseItemPlacerPresenterEvent.ACTIVATED:
          this.onSucceed();
          break;
        case BaseItemPlacerPresenterEvent.PLACED:
          this.onSucceed();
          break;
        case BaseItemPlacerPresenterEvent.DEACTIVATED:
          this.onFailed();
          break;
      }
    });
    const itemCockpit = this.tipService.getItemCockpit();
    if (itemCockpit) {
      if (itemCockpit.showBuildupTip(this.toBeBuiltItemTypeId)) {
        this.stallReason = TipStallReason.AWAIT_PLACER;
        this.firstRetryTime = 0;
        // Keep checking rather than just waiting for the placer: the popover hangs on a cockpit
        // button, and every selection event rebuilds the cockpit and takes that button with it.
        // Returning here left the player waiting on a prompt that was no longer on screen.
        this.retryTimeout = setTimeout(() => this.start(), StartBuildPlacerTipTask.SLOW_RETRY_MILLIS);
        return;
      }
      // Why the button cannot be pointed at is the whole point of the report: a disabled button
      // is a quest the player cannot fulfil, a missing cockpit is a rendering problem.
      this.stallReason = itemCockpit.getBuildupTipBlockReason(this.toBeBuiltItemTypeId)
        ?? TipStallReason.COCKPIT_NOT_READY;
    } else {
      this.stallReason = TipStallReason.COCKPIT_NOT_READY;
    }
    this.retryTimeout = setTimeout(() => this.start(), this.nextRetryMillis());
  }

  /**
   * The selection callback lives on the item instance, and the item is gone while it is out of
   * view - deselecting the builder out there fires nothing at all, and what comes back into view
   * is a fresh instance that never saw the event. The task then waits on a cockpit that cannot
   * appear without a selection. So look at the state instead of waiting to be told.
   *
   * Not while the placer runs: it clears the selection on its way, and that is the step this task
   * is waiting for. Not while the actor is out of view either - out there its selection state is
   * whatever it was when it left, and the stall is reported as ACTOR_OUT_OF_VIEW anyway.
   */
  private checkSelectionLost(): void {
    const actor = this.tipTaskContext.babylonBaseItemImpl;
    if (!actor || actor.isSelected() || this.tipService.renderService.baseItemPlacerActive) {
      return;
    }
    this.onSelectionLost(() => !this.tipTaskContext.babylonBaseItemImpl?.isSelected());
  }

  private nextRetryMillis(): number {
    const now = Date.now();
    if (this.firstRetryTime === 0) {
      this.firstRetryTime = now;
    }
    return now - this.firstRetryTime < StartBuildPlacerTipTask.FAST_RETRY_DURATION_MILLIS
      ? StartBuildPlacerTipTask.FAST_RETRY_MILLIS
      : StartBuildPlacerTipTask.SLOW_RETRY_MILLIS;
  }

  cleanup(): void {
    this.cancelSelectionLossGrace();
    this.stopActorTracking();
    this.firstRetryTime = 0;
    if (this.retryTimeout !== null) {
      clearTimeout(this.retryTimeout);
      this.retryTimeout = null;
    }
    this.tipTaskContext.babylonBaseItemImpl?.setSelectionCallback(null);
    this.tipService.setOutOfViewTarget(null);
    this.tipService.renderService.setBaseItemPlacerCallback(null);
    if (this.tipService.getItemCockpit()) {
      this.tipService.getItemCockpit()!.showBuildupTip(null);
    }
  }
}
