import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {BaseItemPlacerPresenterEvent} from '../../renderer/base-item-placer-presenter.impl';
import {DecimalPosition, Diplomacy, MarkerConfig, PlaceConfig} from '../../../gwtangular/GwtAngularFacade';
import {TipService} from '../tip.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {GwtInstance} from '../../../gwtangular/GwtInstance';
import {ViewField, ViewFieldListener} from '../../renderer/view-field';

interface Rectangle {
  x: number;
  y: number;
  width: number;
  height: number;
}

export class SendBuildCommandTipTask extends AbstractTipTask implements ViewFieldListener {
  private toBeFinalized: BabylonBaseItemImpl | null = null;
  private selectionListener: (() => void) | null = null;
  private placeConfigCenter: DecimalPosition | null = null;
  private placeConfigBoundaryRect: Rectangle | null = null;
  private markerConfig: MarkerConfig | null = null;

  constructor(
    private readonly toBeBuiltItemTypeId: number,
    private readonly placeConfig: PlaceConfig | null,
    tipService: TipService,
    tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  isFulfilled(): boolean {
    return false;
  }

  start(): void {
    // Register global selection listener
    if (!this.selectionListener) {
      this.selectionListener = () => this.onSelectionChanged();
      this.tipService.gwtAngularFacade.selectionService.addSelectionListener(this.selectionListener);
    }

    this.markerConfig = this.getMarkerConfig();

    this.toBeFinalized = this.tipService.renderService.getBabylonBaseItemByDiplomacyItemType(Diplomacy.OWN, this.toBeBuiltItemTypeId);
    if (this.toBeFinalized) {
      this.toBeFinalized.setItemClickCallback(() => this.onSucceed());
      this.toBeFinalized.showSelectPromptVisualization("Click to finish building", "200px", "250px");
    } else {
      // Setup place config visualization if configured
      if (this.placeConfig) {
        this.placeConfigBoundaryRect = this.calculatePlaceConfigBoundaryRect();
        this.placeConfigCenter = this.calculatePlaceConfigCenter();
        this.tipService.renderService.addViewFieldListener(this);
        this.updatePlaceVisualization(this.tipService.renderService.getCurrentViewField());
      }

      this.tipService.renderService.setBaseItemPlacerCallback((event) => {
        switch (event) {
          case BaseItemPlacerPresenterEvent.PLACED:
            this.onSucceed();
            break;
          case BaseItemPlacerPresenterEvent.DEACTIVATED:
            this.onFailed();
            break;
        }
      });
    }
  }

  cleanup(): void {
    // Remove global selection listener
    if (this.selectionListener) {
      this.tipService.gwtAngularFacade.selectionService.removeSelectionListener(this.selectionListener);
      this.selectionListener = null;
    }
    this.tipService.renderService.removeViewFieldListener(this);
    this.tipService.renderService.showOutOfViewMarker(null, 0);
    this.tipService.renderService.setBaseItemPlacerCallback(null);
    this.tipService.renderService.showPlaceMarker(null, null);
    if (this.toBeFinalized) {
      this.toBeFinalized.setItemClickCallback(null);
      this.toBeFinalized.hideSelectPromptVisualization();
    }
  }

  onViewFieldChanged(viewField: ViewField): void {
    if (this.placeConfig && this.placeConfigCenter) {
      this.updatePlaceVisualization(viewField);
    }
  }

  private updatePlaceVisualization(viewField: ViewField): void {
    if (!this.placeConfigCenter || !this.markerConfig || !this.placeConfigBoundaryRect) {
      return;
    }

    const innerAabb = viewField.calculateInnerAabbRectangle();
    const outOfView = !this.placeConfigAdjoinsRect(innerAabb);

    if (outOfView) {
      // Show OutOfView, hide place marker
      const angle = viewField.getAngleTo(this.placeConfigCenter);
      this.tipService.renderService.showOutOfViewMarker(this.markerConfig, angle);
      this.tipService.renderService.showPlaceMarker(null, null);
    } else {
      // Show place marker, hide OutOfView
      this.tipService.renderService.showOutOfViewMarker(null, 0);
      this.tipService.renderService.showPlaceMarker(this.placeConfig, this.markerConfig);
    }
  }

  private onSelectionChanged(): void {
    if (!this.tipService.gwtAngularFacade.selectionService.hasOwnSelection()) {
      this.onFailed();
    }
  }

  private getMarkerConfig(): MarkerConfig {
    const config = this.tipService.gwtAngularFacade.gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig();
    return {
      radius: config.getRadius(),
      nodesMaterialId: config.getNodesMaterialId(),
      placeNodesMaterialId: config.getPlaceNodesMaterialId(),
      outOfViewNodesMaterialId: config.getOutOfViewNodesMaterialId(),
      outOfViewSize: config.getOutOfViewSize(),
      outOfViewDistanceFromCamera: config.getOutOfViewDistanceFromCamera()
    };
  }

  private calculatePlaceConfigCenter(): DecimalPosition | null {
    if (!this.placeConfigBoundaryRect) {
      return null;
    }
    const rect = this.placeConfigBoundaryRect;
    return GwtInstance.newDecimalPosition(
      rect.x + rect.width / 2,
      rect.y + rect.height / 2
    );
  }

  private calculatePlaceConfigBoundaryRect(): Rectangle | null {
    if (!this.placeConfig) {
      return null;
    }

    if (this.placeConfig.getPosition()) {
      const pos = this.placeConfig.getPosition()!;
      const radius = this.placeConfig.toRadiusAngular() || 1;
      return {
        x: pos.getX() - radius,
        y: pos.getY() - radius,
        width: radius * 2,
        height: radius * 2
      };
    } else if (this.placeConfig.getPolygon2D()) {
      const corners = this.placeConfig.getPolygon2D()!.toCornersAngular();
      let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;
      for (const corner of corners) {
        minX = Math.min(minX, corner.getX());
        minY = Math.min(minY, corner.getY());
        maxX = Math.max(maxX, corner.getX());
        maxY = Math.max(maxY, corner.getY());
      }
      return {
        x: minX,
        y: minY,
        width: maxX - minX,
        height: maxY - minY
      };
    }

    return null;
  }

  private placeConfigAdjoinsRect(rect: Rectangle): boolean {
    if (!this.placeConfig) {
      return false;
    }

    if (this.placeConfig.getPosition()) {
      const pos = this.placeConfig.getPosition()!;
      const radius = this.placeConfig.toRadiusAngular();
      if (radius) {
        // Check if circle adjoins rectangle
        return this.rectAdjoinsCircle(rect, pos.getX(), pos.getY(), radius);
      } else {
        // Check if point is inside rectangle
        return this.rectContainsPoint(rect, pos.getX(), pos.getY());
      }
    } else if (this.placeConfig.getPolygon2D()) {
      // Check if polygon adjoins rectangle
      const corners = this.placeConfig.getPolygon2D()!.toCornersAngular();
      // Check if any corner is inside the rectangle
      for (const corner of corners) {
        if (this.rectContainsPoint(rect, corner.getX(), corner.getY())) {
          return true;
        }
      }
      // Check if any rectangle corner is inside the polygon (simplified check)
      const rectCorners = [
        {x: rect.x, y: rect.y},
        {x: rect.x + rect.width, y: rect.y},
        {x: rect.x + rect.width, y: rect.y + rect.height},
        {x: rect.x, y: rect.y + rect.height}
      ];
      for (const rc of rectCorners) {
        if (this.pointInPolygon(rc.x, rc.y, corners)) {
          return true;
        }
      }
      return false;
    }

    return false;
  }

  private rectAdjoinsCircle(rect: Rectangle, cx: number, cy: number, radius: number): boolean {
    // Find the closest point on the rectangle to the circle center
    const closestX = Math.max(rect.x, Math.min(cx, rect.x + rect.width));
    const closestY = Math.max(rect.y, Math.min(cy, rect.y + rect.height));
    const distanceSquared = (closestX - cx) * (closestX - cx) + (closestY - cy) * (closestY - cy);
    return distanceSquared < radius * radius;
  }

  private rectContainsPoint(rect: Rectangle, x: number, y: number): boolean {
    return x >= rect.x && x <= rect.x + rect.width && y >= rect.y && y <= rect.y + rect.height;
  }

  private pointInPolygon(x: number, y: number, corners: DecimalPosition[]): boolean {
    let inside = false;
    for (let i = 0, j = corners.length - 1; i < corners.length; j = i++) {
      const xi = corners[i].getX(), yi = corners[i].getY();
      const xj = corners[j].getX(), yj = corners[j].getY();
      if (((yi > y) !== (yj > y)) && (x < (xj - xi) * (y - yi) / (yj - yi) + xi)) {
        inside = !inside;
      }
    }
    return inside;
  }
}
