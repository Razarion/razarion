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

  protected onFailed(): void {
    this.cleanup();
    this.tipService.onTaskFailed();
  }

  protected onSucceed(): void {
    this.cleanup();
    this.tipService.onSucceed();
  }


}
