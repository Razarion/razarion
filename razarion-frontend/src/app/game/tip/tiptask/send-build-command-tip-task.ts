import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {BaseItemPlacerPresenterEvent} from '../../renderer/base-item-placer-presenter.impl';
import {Diplomacy, MarkerConfig} from '../../../gwtangular/GwtAngularFacade';
import {TipService} from '../tip.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';

export class SendBuildCommandTipTask extends AbstractTipTask {
  private toBeFinalized: BabylonBaseItemImpl | null = null;

  constructor(private readonly toBeBuiltItemTypeId: number, tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  isFulfilled(): boolean {
    return false;
  }

  start(): void {
    this.toBeFinalized = this.tipService.renderService.getBabylonBaseItemByDiplomacyItemType(Diplomacy.OWN, this.toBeBuiltItemTypeId);
    if (this.toBeFinalized) {
      this.toBeFinalized.setItemClickCallback(() => this.onSucceed());
      this.tipTaskContext.babylonBaseItemImpl!.setSelectionCallback(active => {
        if (!active) {
          this.onFailed();
        }
      });
      let markerConfig = new class implements MarkerConfig {
        radius = 10;
        nodesMaterialId = 11;
        placeNodesMaterialId = 10;
        outOfViewNodesMaterialId = 0;
        outOfViewSize = 0;
        outOfViewDistanceFromCamera = 0;
      }
      this.toBeFinalized.mark(markerConfig);
    } else {
      this.tipService.renderService.setBaseItemPlacerCallback((event) => {
        switch (event) {
          case BaseItemPlacerPresenterEvent.PLACED:
            this.onSucceed();
            break;
          case BaseItemPlacerPresenterEvent.DEACTIVATED:
            this.onFailed();
            break;
        }
      });
    }
  }

  cleanup(): void {
    this.tipService.renderService.setBaseItemPlacerCallback(null);
    this.tipTaskContext.babylonBaseItemImpl!.setSelectionCallback(null);
    if (this.toBeFinalized) {
      this.toBeFinalized.setItemClickCallback(null);
      this.toBeFinalized.mark(null);
    }
  }

}
