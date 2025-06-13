import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BaseItemPlacerPresenterEvent} from '../../renderer/base-item-placer-presenter.impl';
import {Diplomacy} from '../../../gwtangular/GwtAngularFacade';

export class StartBuildPlacerTipTask extends AbstractTipTask {
  constructor(private readonly toBeBuiltItemTypeId: number, tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  isFulfilled(): boolean {
    return !!this.tipTaskContext.babylonBaseItemImpl?.isSelectOrHove() &&
      !!this.tipService.renderService.getBabylonBaseItemImpl(Diplomacy.OWN, this.toBeBuiltItemTypeId);
  }

  start(): void {
    this.tipTaskContext.babylonBaseItemImpl?.setSelectionCallback((active: boolean) => {
      if (!active) {
        this.onFailed();
      }
    });
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
    if (this.tipService.getItemCockpit()) {
      const success = this.tipService.getItemCockpit()!.showBuildupTip(this.toBeBuiltItemTypeId);
      if (success) {
        return;
      }
    }
    setTimeout(() => this.start(), 200);
  }

  cleanup(): void {
    this.tipTaskContext.babylonBaseItemImpl?.setSelectionCallback(null);
    this.tipService.renderService.setBaseItemPlacerCallback(null);
    if (this.tipService.getItemCockpit()) {
      this.tipService.getItemCockpit()!.showBuildupTip(null);
    }

  }
}
