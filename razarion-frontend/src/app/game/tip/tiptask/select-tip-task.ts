import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {Diplomacy, TipConfig} from '../../../gwtangular/GwtAngularFacade';
import {TipService} from '../tip.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {GwtHelper} from '../../../gwtangular/GwtHelper';
import {GwtInstance} from '../../../gwtangular/GwtInstance';

export class SelectTipTask extends AbstractTipTask {
  private babylonItem: BabylonBaseItemImpl | null = null;
  private selectionListener: (() => void) | null = null;
  private actorItemTypeId: number;

  constructor(private tipConfig: TipConfig, tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
    this.actorItemTypeId = GwtHelper.gwtIssueNumber(this.tipConfig.getActorItemTypeId());
  }

  override isFulfilled(): boolean {
    let babylonItem = this.findActor()
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

    // Register global selection listener
    if (!this.selectionListener) {
      this.selectionListener = () => this.onSelectionChanged();
      this.tipService.gwtAngularFacade.selectionService.addSelectionListener(this.selectionListener);
    }

    this.babylonItem.showSelectPromptVisualization();

    // Set OutOfView target
    const itemPosition = this.babylonItem.getPosition();
    if (itemPosition) {
      this.tipService.setOutOfViewTarget(
        GwtInstance.newDecimalPosition(itemPosition.getX(), itemPosition.getY())
      );
    }
  }

  override cleanup(): void {
    // Remove global selection listener
    if (this.selectionListener) {
      this.tipService.gwtAngularFacade.selectionService.removeSelectionListener(this.selectionListener);
      this.selectionListener = null;
    }
    this.tipService.setOutOfViewTarget(null);
    if (this.babylonItem) {
      this.babylonItem.hideSelectPromptVisualization();
    }
  }

  private onSelectionChanged(): void {
    // Check if the correct item (actor) is now selected
    if (this.tipService.gwtAngularFacade.selectionService.hasOwnSelection()) {
      // Check if the selected item is the actor we're looking for
      const babylonItem = this.findActor();
      if (babylonItem?.isSelectOrHove()) {
        this.onSucceed();
      }
    }
  }

  private findActor(): BabylonBaseItemImpl | null {
    return this.tipService.renderService.getBabylonBaseItemByDiplomacyItemType(Diplomacy.OWN, this.actorItemTypeId);
  }

}
