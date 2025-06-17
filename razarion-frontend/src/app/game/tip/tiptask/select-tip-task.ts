import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {Diplomacy, MarkerConfig, TipConfig} from '../../../gwtangular/GwtAngularFacade';
import {TipService} from '../tip.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {GwtHelper} from '../../../gwtangular/GwtHelper';

export class SelectTipTask extends AbstractTipTask {
  private babylonItem: BabylonBaseItemImpl | null = null;

  constructor(private tipConfig: TipConfig, tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  override isFulfilled(): boolean {
    var babylonItem = this.findActor()
    return !!babylonItem?.isSelectOrHove();
  }

  override start(): void {
    this.babylonItem = this.findActor();
    if (!this.babylonItem) {
      // Client engine is may not synchronized with the server
      setTimeout(() => {
        this.start();
      }, 1000);
      return;
    }

    this.tipTaskContext.babylonBaseItemImpl = this.babylonItem;
    this.babylonItem.setSelectionCallback((active: boolean) => {
      this.onSucceed();
    })
    let markerConfig = new class implements MarkerConfig {
      radius = 10;
      nodesMaterialId = 11;
      placeNodesMaterialId = 10;
      outOfViewNodesMaterialId = 0;
      outOfViewSize = 0;
      outOfViewDistanceFromCamera = 0;
    }
    this.babylonItem.mark(markerConfig);
  }

  override cleanup(): void {
    if (this.babylonItem) {
      this.babylonItem.setSelectionCallback(null);
      this.babylonItem.mark(null);
    }
  }

  private findActor(): BabylonBaseItemImpl {
    const actorItemTypeId = GwtHelper.gwtIssueNumber(this.tipConfig.getActorItemTypeId());
    return <BabylonBaseItemImpl>this.tipService.renderService.getBabylonBaseItemImpl(Diplomacy.OWN, actorItemTypeId);
  }

}
