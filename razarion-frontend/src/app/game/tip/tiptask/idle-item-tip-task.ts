import {AbstractTipTask} from './abstract-tip-task';

export class IdleItemTipTask extends AbstractTipTask {
  isFulfilled(): boolean {
    return false;
  }

  start(): void {
    this.tipTaskContext.babylonBaseItemImpl!.setIdleCallback(idle => {
      if (idle) {
        this.onSucceed();
      }
    })
  }

  cleanup(): void {
    this.tipTaskContext.babylonBaseItemImpl!.setIdleCallback(null);
  }
}
