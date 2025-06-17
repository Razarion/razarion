import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {MarkerConfig} from '../../../gwtangular/GwtAngularFacade';
import {TipService} from '../tip.service';
import {BabylonResourceItemImpl} from '../../renderer/babylon-resource-item.impl';

export class SendHarvestCommandTipTask extends AbstractTipTask {
  private resource: BabylonResourceItemImpl | null = null;

  constructor(tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  isFulfilled(): boolean {
    return false;
  }

  start(): void {
    this.tipTaskContext.babylonBaseItemImpl!.setSelectionCallback((active: boolean) => {
      if (!active) {
        this.onFailed();
      }
    });
    this.resource = null;
    const resources = this.tipService.renderService.getBabylonResourceItemImpls();
    if (resources.length > 0) {
      const harvesterPosition = this.tipTaskContext.babylonBaseItemImpl!.getPosition()!;
      let minDistance: number | null = null;
      for (const resource of resources) {
        const distance = resource.getPosition()?.distance(harvesterPosition)!;
        if (minDistance !== null) {
          if (minDistance > distance) {
            this.resource = resource;
            minDistance = distance;
          }
        } else {
          this.resource = resource;
          minDistance = distance;
        }
      }

      let markerConfig = new class implements MarkerConfig {
        radius = 10;
        nodesMaterialId = 11;
        placeNodesMaterialId = 10;
        outOfViewNodesMaterialId = 0;
        outOfViewSize = 0;
        outOfViewDistanceFromCamera = 0;
      }
      this.resource!.mark(markerConfig);
      this.resource!.setItemClickCallback(() => {
        this.onSucceed();
      });
    }
  }

  cleanup(): void {
    this.tipTaskContext.babylonBaseItemImpl!.setSelectionCallback(null);
    if (this.resource) {
      this.resource.mark(null);
      this.resource.setItemClickCallback(null);
    }
  }
}
