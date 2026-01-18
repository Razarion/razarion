import {TipService} from '../tip.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';

export class TipTaskContext {
  babylonBaseItemImpl: BabylonBaseItemImpl | null = null;
}

export abstract class AbstractTipTask {

  constructor(protected readonly tipService: TipService, protected readonly tipTaskContext: TipTaskContext) {

  }

  abstract isFulfilled(): boolean;

  abstract start(): void;

  abstract cleanup(): void;

  /**
   * Called when the tip target becomes visible again after being out of view.
   * Override to re-show visualizations.
   */
  onBecameVisible(): void {
    this.cleanup();
    this.start();
  }

  protected onFailed(): void {
    this.cleanup();
    this.tipService.onTaskFailed();
  }

  protected onSucceed(): void {
    this.cleanup();
    this.tipService.onSucceed();
  }


}
