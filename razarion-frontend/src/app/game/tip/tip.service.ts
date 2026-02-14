import {Injectable} from '@angular/core';
import {DecimalPosition, GwtAngularFacade, MarkerConfig, QuestConfig} from '../../gwtangular/GwtAngularFacade';
import {TipTaskFactory} from './tip-task.factory';
import {TipTaskContainer} from './tip-task.container';
import {BabylonRenderServiceAccessImpl} from '../renderer/babylon-render-service-access-impl.service';
import {ItemCockpitComponent} from '../cockpit/item/item-cockpit.component';
import {ViewField, ViewFieldListener} from '../renderer/view-field';
import {GwtAngularService} from '../../gwtangular/GwtAngularService';
import {SelectionService} from '../selection.service';

@Injectable({
  providedIn: 'root'
})
export class TipService implements ViewFieldListener {
  private tipTaskContainer: TipTaskContainer | null = null;
  private itemCockpit: ItemCockpitComponent | null = null;
  private currentViewField: ViewField | null = null;
  private outOfViewTarget: DecimalPosition | null = null;
  private outOfViewActive = false;

  // OutOfView configuration - can be loaded from server config later
  private readonly outOfViewMarkerConfig: MarkerConfig = {
    radius: 10,
    nodesMaterialId: null,
    placeNodesMaterialId: null,
    outOfViewNodesMaterialId: 12, // TODO: Load from config
    outOfViewSize: 1,
    outOfViewDistanceFromCamera: 3
  };

  constructor(
    public readonly renderService: BabylonRenderServiceAccessImpl,
    private readonly gwtAngularService: GwtAngularService,
    public readonly selectionService: SelectionService
  ) {
    this.renderService.addViewFieldListener(this);
  }

  get gwtAngularFacade(): GwtAngularFacade {
    return this.gwtAngularService.gwtAngularFacade;
  }

  onViewFieldChanged(viewField: ViewField): void {
    this.currentViewField = viewField;
    this.updateOutOfViewMarker();
  }

  setOutOfViewTarget(position: DecimalPosition | null): void {
    this.outOfViewTarget = position;
    // Ensure we have the current ViewField when setting a target
    if (position && !this.currentViewField) {
      this.currentViewField = this.renderService.getCurrentViewField();
    }
    this.updateOutOfViewMarker();
  }

  private updateOutOfViewMarker(): void {
    if (!this.outOfViewTarget || !this.currentViewField) {
      if (this.outOfViewActive) {
        this.renderService.showOutOfViewMarker(null, 0);
        this.outOfViewActive = false;
      }
      return;
    }

    const isVisible = this.currentViewField.contains(this.outOfViewTarget);
    if (isVisible) {
      if (this.outOfViewActive) {
        this.renderService.showOutOfViewMarker(null, 0);
        this.outOfViewActive = false;
        // Notify the current tip task that the target is visible again
        this.tipTaskContainer?.getCurrentTask()?.onBecameVisible();
      }
    } else {
      const angle = this.currentViewField.getAngleTo(this.outOfViewTarget);
      this.renderService.showOutOfViewMarker(this.outOfViewMarkerConfig, angle);
      this.outOfViewActive = true;
    }
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
    this.setOutOfViewTarget(null);
    if (this.tipTaskContainer) {
      this.tipTaskContainer.clean();
      this.tipTaskContainer = null;
    }
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
