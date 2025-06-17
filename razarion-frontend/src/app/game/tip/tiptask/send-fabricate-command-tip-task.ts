import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';

export class SendFabricateCommandTipTask extends AbstractTipTask {
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
        if (cockpit.itemTypeId === this.toBeBuiltItemTypeId) {
          this.onSucceed();
        }
      });
    }
    const success = this.tipService.getItemCockpit()!.showBuildupTip(this.toBeBuiltItemTypeId);
    if (success) {
      return;
    }
    setTimeout(() => this.start(), 200);
  }

  cleanup(): void {
    this.tipTaskContext.babylonBaseItemImpl?.setSelectionCallback(null);
    if (this.tipService.getItemCockpit()) {
      this.tipService.getItemCockpit()!.setBuildClickCallback(null);
      this.tipService.getItemCockpit()!.showBuildupTip(null);
    }
  }

}
