import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';

export class SendFabricateCommandTipTask extends AbstractTipTask {
  private retryTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(private readonly toBeBuiltItemTypeId: number, tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  isFulfilled(): boolean {
    return false;
  }

  start(): void {
    this.tipTaskContext.babylonBaseItemImpl?.setSelectionCallback((active: boolean) => {
      if (!active) {
        this.onFailed();
      }
    });

    if (this.tipService.getItemCockpit()) {
      this.tipService.getItemCockpit()!.setBuildClickCallback(cockpit => {
        if (cockpit.itemTypeId == this.toBeBuiltItemTypeId) {
          this.onSucceed();
        } else {
          console.warn(`SendFabricateCommandTipTask: itemTypeId mismatch: cockpit=${cockpit.itemTypeId} (${typeof cockpit.itemTypeId}) expected=${this.toBeBuiltItemTypeId} (${typeof this.toBeBuiltItemTypeId})`);
        }
      });
      const success = this.tipService.getItemCockpit()!.showBuildupTip(this.toBeBuiltItemTypeId);
      if (success) {
        return;
      }
    }
    this.retryTimeout = setTimeout(() => this.start(), 200);
  }

  cleanup(): void {
    if (this.retryTimeout !== null) {
      clearTimeout(this.retryTimeout);
      this.retryTimeout = null;
    }
    this.tipTaskContext.babylonBaseItemImpl?.setSelectionCallback(null);
    if (this.tipService.getItemCockpit()) {
      this.tipService.getItemCockpit()!.setBuildClickCallback(null);
      this.tipService.getItemCockpit()!.showBuildupTip(null);
    }
  }

}
