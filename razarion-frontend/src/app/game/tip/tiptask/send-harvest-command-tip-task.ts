import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BabylonResourceItemImpl} from '../../renderer/babylon-resource-item.impl';
import {GwtInstance} from '../../../gwtangular/GwtInstance';
import {TipStallReason, TipTaskName} from '../tip-stall';

export class SendHarvestCommandTipTask extends AbstractTipTask {
  private resource: BabylonResourceItemImpl | null = null;
  private selectionListener: (() => void) | null = null;
  private resourceRemovedListener: ((babylonResourceItem: BabylonResourceItemImpl) => void) | null = null;
  private retryTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  isFulfilled(): boolean {
    return false;
  }

  getTaskName(): string {
    return TipTaskName.SEND_HARVEST_COMMAND;
  }

  start(): void {
    // start() is also called again on re-target and when the target scrolls back into view
    if (this.retryTimeout !== null) {
      clearTimeout(this.retryTimeout);
      this.retryTimeout = null;
    }
    this.resource = this.findVisibleResource();

    // Register global selection listener
    if (!this.selectionListener) {
      this.selectionListener = () => this.onSelectionChanged();
      this.tipService.selectionService.addSelectionListener(this.selectionListener);
    }

    // The resource we point at can be harvested empty or streamed out while the tip is up
    if (!this.resourceRemovedListener) {
      this.resourceRemovedListener = removed => this.onResourceRemoved(removed);
      this.tipService.renderService.addResourceRemovedListener(this.resourceRemovedListener);
    }

    if (!this.resource) {
      // No visible resource found - check if there's a resource out of view
      const nearestResourcePosition = this.findNearestResourcePosition();
      if (nearestResourcePosition) {
        // Resource exists but is out of view - point the marker at it and keep polling: the tile
        // may still be streaming in, and once the target is on screen there is no further
        // onBecameVisible() to wake us up.
        this.stallReason = TipStallReason.RESOURCE_OUT_OF_VIEW;
        this.tipService.setOutOfViewTarget(
          GwtInstance.newDecimalPosition(nearestResourcePosition.x, nearestResourcePosition.y)
        );
        this.retryTimeout = setTimeout(() => this.start(), 1000);
        return;
      }

      // No resource found at all - retry after delay (server may not be synchronized)
      this.stallReason = TipStallReason.NO_RESOURCE;
      this.retryTimeout = setTimeout(() => this.start(), 1000);
      return;
    }

    this.stallReason = TipStallReason.AWAIT_HARVEST_CLICK;
    this.resource.setItemClickCallback(() => {
      this.onSucceed();
    });
    this.resource.showSelectPromptVisualization("Click to harvest", "150px");

    // Set OutOfView target for when user scrolls away
    const resourcePosition = this.resource.getPosition();
    if (resourcePosition) {
      this.tipService.setOutOfViewTarget(
        GwtInstance.newDecimalPosition(resourcePosition.getX(), resourcePosition.getY())
      );
    }
  }

  cleanup(): void {
    this.cancelSelectionLossGrace();
    if (this.retryTimeout !== null) {
      clearTimeout(this.retryTimeout);
      this.retryTimeout = null;
    }
    // Remove global selection listener
    if (this.selectionListener) {
      this.tipService.selectionService.removeSelectionListener(this.selectionListener);
      this.selectionListener = null;
    }
    if (this.resourceRemovedListener) {
      this.tipService.renderService.removeResourceRemovedListener(this.resourceRemovedListener);
      this.resourceRemovedListener = null;
    }
    this.tipService.setOutOfViewTarget(null);
    if (this.resource) {
      this.resource.hideSelectPromptVisualization();
      this.resource.setItemClickCallback(null);
    }
  }

  private onSelectionChanged(): void {
    // A lost selection only fails the task if it is still lost after the grace period
    if (!this.tipService.selectionService.hasOwnSelection()) {
      this.onSelectionLost(() => !this.tipService.selectionService.hasOwnSelection());
    }
  }

  /**
   * Our target is gone: dropping the reference before restarting is essential, cleanup() would
   * otherwise call into the disposed item. Previously the prompt and the click callback died with
   * the mesh and the tip stayed silent for the rest of the quest - and tipped quests have the
   * regular quest markers switched off, so nothing else pointed anywhere.
   */
  private onResourceRemoved(removed: BabylonResourceItemImpl): void {
    if (removed !== this.resource) {
      return;
    }
    this.resource = null;
    this.tipService.setOutOfViewTarget(null);
    this.start();
  }

  /** Nearest resource inside the current view field; off-screen ones are left to the out-of-view marker. */
  private findVisibleResource(): BabylonResourceItemImpl | null {
    const resources = this.tipService.renderService.getBabylonResourceItemImpls();
    const harvesterPosition = this.tipTaskContext.babylonBaseItemImpl?.getPosition();
    if (resources.length === 0 || !harvesterPosition) {
      return null;
    }
    const viewField = this.tipService.renderService.getCurrentViewField();
    let resourceFound: BabylonResourceItemImpl | null = null;
    let minDistance = Number.POSITIVE_INFINITY;
    for (const resource of resources) {
      const resourcePosition = resource.getPosition();
      if (!resourcePosition) {
        continue;
      }
      if (viewField && !viewField.contains(GwtInstance.newDecimalPosition(resourcePosition.getX(), resourcePosition.getY()))) {
        continue;
      }
      const distance = resourcePosition.distance(harvesterPosition);
      if (distance < minDistance) {
        resourceFound = resource;
        minDistance = distance;
      }
    }
    return resourceFound;
  }

  private findNearestResourcePosition(): { x: number, y: number } | null {
    const harvesterPosition = this.tipTaskContext.babylonBaseItemImpl!.getPosition();
    if (!harvesterPosition) {
      return null;
    }

    const resourceUiService = this.tipService.gwtAngularFacade.resourceUiService;
    if (!resourceUiService) {
      console.warn('ResourceUiService not available');
      return null;
    }

    const nearestPosition = resourceUiService.getNearestResourcePosition(
      harvesterPosition.getX(),
      harvesterPosition.getY()
    );

    if (nearestPosition) {
      return { x: nearestPosition.getX(), y: nearestPosition.getY() };
    }
    return null;
  }
}
