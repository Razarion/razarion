import {Injectable} from '@angular/core';
import {DecimalPosition, GwtAngularFacade, MarkerConfig, QuestConfig} from '../../gwtangular/GwtAngularFacade';
import {TipTaskFactory} from './tip-task.factory';
import {TipTaskContainer} from './tip-task.container';
import {BabylonRenderServiceAccessImpl} from '../renderer/babylon-render-service-access-impl.service';
import {ItemCockpitComponent} from '../cockpit/item/item-cockpit.component';
import {ViewField, ViewFieldListener} from '../renderer/view-field';
import {GwtAngularService} from '../../gwtangular/GwtAngularService';
import {SelectionService} from '../selection.service';
import {UiSettingsService} from '../ui-settings.service';
import {TipStallTrackerService} from './tip-stall-tracker.service';
import {GwtHelper} from '../../gwtangular/GwtHelper';

@Injectable({
  providedIn: 'root'
})
export class TipService implements ViewFieldListener {
  private tipTaskContainer: TipTaskContainer | null = null;
  private itemCockpit: ItemCockpitComponent | null = null;
  private currentViewField: ViewField | null = null;
  private outOfViewTarget: DecimalPosition | null = null;
  private outOfViewActive = false;
  /** Quest the running tip belongs to; reported with a stall so it lines up with the funnel. */
  private currentQuestId: number | null = null;

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
    public readonly selectionService: SelectionService,
    private readonly uiSettingsService: UiSettingsService,
    private readonly tipStallTrackerService: TipStallTrackerService
  ) {
    this.renderService.addViewFieldListener(this);
    // Scrolling the actor out and back in gives it a new BabylonBaseItemImpl. The running task
    // put its selection, click and idle callbacks on the old one, so it has to hand them over -
    // otherwise the tip stays on screen while nothing it listens to can ever fire again.
    this.renderService.addBaseItemCreatedListener(babylonBaseItem => {
      if (!this.tipTaskContainer?.hasTip()
        || babylonBaseItem.getId() !== this.tipTaskContainer.tipTaskContext.getActorId()) {
        return;
      }
      this.tipTaskContainer.getCurrentTask().onBecameVisible();
    });
    // Turning tips off (e.g. for clean director footage) clears any active tip.
    this.uiSettingsService.tipsVisible$.subscribe(visible => {
      if (!visible) {
        this.deactivate();
      }
    });
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
    if (!this.uiSettingsService.tipsVisible) {
      return; // tips disabled (e.g. director/filming mode)
    }

    this.currentQuestId = GwtHelper.gwtIssueNumber(questConfig.getId());
    this.tipTaskContainer = TipTaskFactory.create(questConfig, this);
    if (this.tipTaskContainer) {
      this.startTipTask();
    } else {
      console.warn(`Unable to start tip for quest '${questConfig.getInternalName()}' (${questConfig.getId()})`)
    }
  }

  public deactivate(): void {
    this.setOutOfViewTarget(null);
    this.tipStallTrackerService.stop();
    this.currentQuestId = null;
    if (this.tipTaskContainer) {
      this.tipTaskContainer.clean();
      this.tipTaskContainer = null;
    }
  }

  public onSucceed() {
    try {
      this.tipStallTrackerService.taskEnded(true);
      // A timer of a task that ended after the chain was gone: nothing left to advance.
      if (!this.tipTaskContainer) {
        return;
      }
      this.tipTaskContainer.next();
      this.startTipTask();
    } catch (exception) {
      // Everything below this point runs on a timer or a renderer callback, so an exception here
      // has no other way out: it used to end up in the console and leave the player with a dead
      // tip chain and the tracking empty - the one state that reports nothing at all.
      this.tipStallTrackerService.reportChainError(this.currentQuestId, 'onSucceed');
      console.warn("GameTipManager.onSucceed()");
      console.error(exception);
    }
  }

  public onTaskFailed() {
    try {
      // Not a resolution: the chain goes back a step and the player is no further than before.
      this.tipStallTrackerService.taskEnded(false);
      if (!this.tipTaskContainer) {
        return;
      }
      this.tipTaskContainer.backtrackTask();
      this.startTipTask();
    } catch (exception) {
      this.tipStallTrackerService.reportChainError(this.currentQuestId, 'onTaskFailed');
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

  /**
   * Starts the first task of the chain that still has something to ask of the player, switching
   * to the fallback chain and finally giving up when nothing is left.
   *
   * Skipping used to be a single unguarded step: skipping the last task indexed past the end,
   * armed the watchdog with an undefined source - which can never report anything - and threw
   * into the catch above. No tip, no record, permanently.
   */
  private startTipTask() {
    const container = this.tipTaskContainer;
    if (!container) {
      return;
    }
    this.skipFulfilledTasks(container);
    if (!container.hasTip()) {
      container.activateFallback();
      this.skipFulfilledTasks(container);
    }
    if (!container.hasTip()) {
      this.tipTaskContainer = null;
      this.tipStallTrackerService.stop();
      return;
    }
    const currentTipTask = container.getCurrentTask();
    // Armed before start(): a task that immediately drops into a retry loop is exactly the case
    // this watches, and start() never returns to a place where arming would still be right.
    this.tipStallTrackerService.taskStarted(this.currentQuestId, currentTipTask);
    currentTipTask.start();
  }

  private skipFulfilledTasks(container: TipTaskContainer): void {
    while (container.hasTip() && container.getCurrentTask().isFulfilled()) {
      container.next();
    }
  }
}
