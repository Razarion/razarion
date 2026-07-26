import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {TipStallReason, TipTaskName} from '../tip-stall';

export class SendFabricateCommandTipTask extends AbstractTipTask {
  /** Same reasoning as in StartBuildPlacerTipTask: quick while the cockpit renders, then slow. */
  private static readonly FAST_RETRY_MILLIS = 200;
  private static readonly SLOW_RETRY_MILLIS = 1000;
  private static readonly FAST_RETRY_DURATION_MILLIS = 5000;
  private retryTimeout: ReturnType<typeof setTimeout> | null = null;
  private firstRetryTime = 0;

  constructor(private readonly toBeBuiltItemTypeId: number, tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  isFulfilled(): boolean {
    return false;
  }

  getTaskName(): string {
    return TipTaskName.SEND_FABRICATE_COMMAND;
  }

  start(): void {
    // Before the branches below: the actor has to be pointed at while the cockpit is missing too -
    // that is precisely the case where it is missing *because* the factory left the view.
    this.trackActor();
    this.startActorTracking();
    this.tipTaskContext.babylonBaseItemImpl?.setSelectionCallback((active: boolean) => {
      if (!active) {
        // Only fail if the factory is still deselected after the grace period
        this.onSelectionLost(() => !this.tipTaskContext.babylonBaseItemImpl?.isSelected());
      }
    });
    // Same as in StartBuildPlacerTipTask: a deselection while the actor is out of view fires
    // nothing - the instance that carried the callback no longer exists - and the task would then
    // wait forever on a cockpit that cannot appear without a selection.
    const actor = this.tipTaskContext.babylonBaseItemImpl;
    if (actor && !actor.isSelected()) {
      this.onSelectionLost(() => !this.tipTaskContext.babylonBaseItemImpl?.isSelected());
    }

    const itemCockpit = this.tipService.getItemCockpit();
    if (itemCockpit) {
      itemCockpit.setBuildClickCallback(cockpit => {
        if (cockpit.itemTypeId == this.toBeBuiltItemTypeId) {
          this.onSucceed();
        } else {
          console.warn(`SendFabricateCommandTipTask: itemTypeId mismatch: cockpit=${cockpit.itemTypeId} (${typeof cockpit.itemTypeId}) expected=${this.toBeBuiltItemTypeId} (${typeof this.toBeBuiltItemTypeId})`);
        }
      });
      if (itemCockpit.showBuildupTip(this.toBeBuiltItemTypeId)) {
        this.stallReason = TipStallReason.AWAIT_FABRICATE_CLICK;
        this.firstRetryTime = 0;
        // Same reason as in StartBuildPlacerTipTask: a selection event rebuilds the cockpit and
        // the popover is left anchored to a button that is no longer in the document.
        this.retryTimeout = setTimeout(() => this.start(), SendFabricateCommandTipTask.SLOW_RETRY_MILLIS);
        return;
      }
      this.stallReason = itemCockpit.getBuildupTipBlockReason(this.toBeBuiltItemTypeId)
        ?? TipStallReason.COCKPIT_NOT_READY;
    } else {
      this.stallReason = TipStallReason.COCKPIT_NOT_READY;
    }
    this.retryTimeout = setTimeout(() => this.start(), this.nextRetryMillis());
  }

  private nextRetryMillis(): number {
    const now = Date.now();
    if (this.firstRetryTime === 0) {
      this.firstRetryTime = now;
    }
    return now - this.firstRetryTime < SendFabricateCommandTipTask.FAST_RETRY_DURATION_MILLIS
      ? SendFabricateCommandTipTask.FAST_RETRY_MILLIS
      : SendFabricateCommandTipTask.SLOW_RETRY_MILLIS;
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
    if (this.tipService.getItemCockpit()) {
      this.tipService.getItemCockpit()!.setBuildClickCallback(null);
      this.tipService.getItemCockpit()!.showBuildupTip(null);
    }
  }

}
