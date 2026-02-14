import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BabylonResourceItemImpl} from '../../renderer/babylon-resource-item.impl';
import {GwtInstance} from '../../../gwtangular/GwtInstance';

export class SendHarvestCommandTipTask extends AbstractTipTask {
  private resource: BabylonResourceItemImpl | null = null;
  private selectionListener: (() => void) | null = null;
  private retryTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  isFulfilled(): boolean {
    return false;
  }

  start(): void {
    this.resource = this.findVisibleResource();

    // Register global selection listener
    if (!this.selectionListener) {
      this.selectionListener = () => this.onSelectionChanged();
      this.tipService.selectionService.addSelectionListener(this.selectionListener);
    }

    if (!this.resource) {
      // No visible resource found - check if there's a resource out of view
      const nearestResourcePosition = this.findNearestResourcePosition();
      if (nearestResourcePosition) {
        // Resource exists but is out of view - set OutOfView target and wait
        this.tipService.setOutOfViewTarget(
          GwtInstance.newDecimalPosition(nearestResourcePosition.x, nearestResourcePosition.y)
        );
        return;
      }

      // No resource found at all - retry after delay (server may not be synchronized)
      this.retryTimeout = setTimeout(() => this.start(), 1000);
      return;
    }

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
    if (this.retryTimeout !== null) {
      clearTimeout(this.retryTimeout);
      this.retryTimeout = null;
    }
    // Remove global selection listener
    if (this.selectionListener) {
      this.tipService.selectionService.removeSelectionListener(this.selectionListener);
      this.selectionListener = null;
    }
    this.tipService.setOutOfViewTarget(null);
    if (this.resource) {
      this.resource.hideSelectPromptVisualization();
      this.resource.setItemClickCallback(null);
    }
  }

  private onSelectionChanged(): void {
    // Check if own selection is still present
    if (!this.tipService.selectionService.hasOwnSelection()) {
      this.onFailed();
    }
  }

  private findVisibleResource(): BabylonResourceItemImpl | null {
    const resources = this.tipService.renderService.getBabylonResourceItemImpls();
    let resourceFound: BabylonResourceItemImpl | null = null;
    if (resources.length > 0) {
      const harvesterPosition = this.tipTaskContext.babylonBaseItemImpl!.getPosition()!;
      let minDistance: number | null = null;
      for (const resource of resources) {
        const distance = resource.getPosition()?.distance(harvesterPosition)!;
        if (minDistance !== null) {
          if (minDistance > distance) {
            resourceFound = resource;
            minDistance = distance;
          }
        } else {
          resourceFound = resource;
          minDistance = distance;
        }
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
