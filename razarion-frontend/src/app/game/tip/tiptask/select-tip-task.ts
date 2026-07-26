import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {Diplomacy, TipConfig} from '../../../gwtangular/GwtAngularFacade';
import {TipService} from '../tip.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {GwtHelper} from '../../../gwtangular/GwtHelper';
import {TipStallReason, TipTaskName} from '../tip-stall';

export class SelectTipTask extends AbstractTipTask {
  private babylonItem: BabylonBaseItemImpl | null = null;
  private selectionListener: (() => void) | null = null;
  private pollTimeout: ReturnType<typeof setTimeout> | null = null;
  private actorItemTypeId: number;

  constructor(private tipConfig: TipConfig, tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
    this.actorItemTypeId = GwtHelper.gwtIssueNumber(this.tipConfig.getActorItemTypeId());
  }

  override isFulfilled(): boolean {
    let babylonItem = this.findActor()
    if(!!babylonItem?.isSelected()) {
      if(this.tipTaskContext.babylonBaseItemImpl === null) {
        this.tipTaskContext.setActor(babylonItem);
      }
      return true;
    } else {
      return false;
    }
  }

  override getTaskName(): string {
    return TipTaskName.SELECT;
  }

  override start(): void {
    // Register global selection listener
    if (!this.selectionListener) {
      this.selectionListener = () => this.onSelectionChanged();
      this.tipService.selectionService.addSelectionListener(this.selectionListener);
    }

    this.refreshActor();
    this.pollActor();
  }

  /**
   * Picks up the actor as it is right now: a new instance after it streamed back in, a new
   * position after it drove somewhere, or nothing at all while it is off screen.
   */
  private refreshActor(sampleMillis = 0): void {
    const actor = this.findActor();
    if (actor) {
      if (actor !== this.babylonItem) {
        this.babylonItem = actor;
        this.tipTaskContext.setActor(actor);
      }
      // Asked of the item, not inferred from it being a new instance: the prompt goes down with
      // the item every time it leaves the view, and whether what comes back is the same object or
      // a new one is not something this task can tell reliably. Scrolling back to the builder
      // left it standing there unmarked while the task reported AWAIT_SELECTION.
      if (!actor.isSelectPromptVisible()) {
        actor.showSelectPromptVisualization();
      }
      this.stallReason = TipStallReason.AWAIT_SELECTION;
    } else {
      this.babylonItem = null;
    }
    this.trackActor(sampleMillis);
    if (!actor) {
      // Never seen means the client engine is still syncing; seen before means it drove off.
      this.stallReason = this.lastKnownActorPosition === null
        ? TipStallReason.ACTOR_NOT_FOUND
        : TipStallReason.ACTOR_OUT_OF_VIEW;
    }
  }

  private pollActor(): void {
    if (this.pollTimeout !== null) {
      return;
    }
    this.pollTimeout = setTimeout(() => {
      this.pollTimeout = null;
      this.refreshActor(AbstractTipTask.ACTOR_TRACK_MILLIS);
      // Guarded against a double timer: refreshActor() can bring the actor back into view, which
      // restarts this task and arms the poll again on its way.
      this.pollActor();
    }, AbstractTipTask.ACTOR_TRACK_MILLIS);
  }

  override cleanup(): void {
    if (this.pollTimeout !== null) {
      clearTimeout(this.pollTimeout);
      this.pollTimeout = null;
    }
    // Remove global selection listener
    if (this.selectionListener) {
      this.tipService.selectionService.removeSelectionListener(this.selectionListener);
      this.selectionListener = null;
    }
    this.tipService.setOutOfViewTarget(null);
    if (this.babylonItem) {
      this.babylonItem.hideSelectPromptVisualization();
      // Dropped so a restart puts the prompt back up instead of taking the item for unchanged.
      this.babylonItem = null;
    }
  }

  private onSelectionChanged(): void {
    // Check if the correct item (actor) is now selected
    if (this.tipService.selectionService.hasOwnSelection()) {
      // Check if the selected item is the actor we're looking for
      const babylonItem = this.findActor();
      if (babylonItem?.isSelected()) {
        this.onSucceed();
      }
    }
  }

  private findActor(): BabylonBaseItemImpl | null {
    return this.tipService.renderService.getBabylonBaseItemByDiplomacyItemType(Diplomacy.OWN, this.actorItemTypeId);
  }

}
