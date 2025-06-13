import {Injectable} from '@angular/core';
import {QuestConfig} from '../../gwtangular/GwtAngularFacade';
import {TipTaskFactory} from './tip-task.factory';
import {TipTaskContainer} from './tip-task.container';
import {BabylonRenderServiceAccessImpl} from '../renderer/babylon-render-service-access-impl.service';
import {ItemCockpitComponent} from '../cockpit/item/item-cockpit.component';

@Injectable({
  providedIn: 'root'
})
export class TipService {
  private tipTaskContainer: TipTaskContainer | null = null;
  private itemCockpit: ItemCockpitComponent | null = null;

  constructor(public readonly renderService: BabylonRenderServiceAccessImpl) {

  }

  public activate(questConfig: QuestConfig): void {
    this.deactivate();

    this.tipTaskContainer = TipTaskFactory.create(questConfig, this);
    if (this.tipTaskContainer) {
      this.startTipTask();
    } else {
      console.warn(`Unable to start tip for quest '${questConfig.getInternalName()}' (${questConfig.getId()})`)
    }
  }

  public deactivate(): void {

  }

  public onSucceed() {
    try {
      this.tipTaskContainer!.next();
      if (!this.tipTaskContainer!.hasTip()) {
        this.tipTaskContainer!.activateFallback();
        if (!this.tipTaskContainer!.hasTip()) {
          this.tipTaskContainer = null;
          return;
        }
      }
      this.startTipTask();
    } catch (exception) {
      console.warn("GameTipManager.onSucceed()");
      console.error(exception);
    }
  }

  public onTaskFailed() {
    try {
      this.tipTaskContainer!.backtrackTask();
      this.startTipTask();
    } catch (exception) {
      console.warn("GameTipManager.onTaskFailed()");
      console.error(exception);
    }
  }

  public setItemCockpit(itemCockpit: ItemCockpitComponent | null) {
    this.itemCockpit = itemCockpit;
  }

  public getItemCockpit(): ItemCockpitComponent | null {
    return this.itemCockpit;
  }

  private startTipTask() {
    let currentTipTask = this.tipTaskContainer!.getCurrentTask();
    if (currentTipTask.isFulfilled()) {
      this.tipTaskContainer!.next();
      currentTipTask = this.tipTaskContainer!.getCurrentTask();
    }
    currentTipTask.start();
  }
}
