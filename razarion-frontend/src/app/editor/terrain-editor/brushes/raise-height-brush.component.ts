import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {PointerEventTypes, Vector2, Vector3} from '@babylonjs/core';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {BabylonRenderServiceAccessImpl} from '../../../game/renderer/babylon-render-service-access-impl.service';
import {HeightMapCursor} from './height-map-cursor';
import {BrushType, BrushValues} from './fix-height-brush.component';

export class RaiseBrushValues {
  type: BrushType = BrushType.SQUARE;
  heightIncrement: number = 0.5;
  size: number = 10;
  maxSlopeWidth: number = 5;
  minHeight: number = -20;
  maxHeight: number = 50;
}

@Component({
  selector: 'raise-height-brush',
  imports: [
    Slider,
    FormsModule,
    Select
  ],
  template: `
    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Type</span>
      <div class="col-span-7">
        <p-select
          [options]="[{label: 'Square', value: BrushType.SQUARE}, {label: 'Round', value: BrushType.ROUND}]"
          [(ngModel)]="brushValues.type"
          (ngModelChange)="onBrushValuesChanged()"
          [style]="{ width: '100%' }">
        </p-select>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Size [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.size" (ngModelChange)="onBrushValuesChanged()" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.size" (ngModelChange)="onBrushValuesChanged()" [step]="1" [min]="1"
                  [max]="100"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Height increment [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.heightIncrement" [step]="0.1" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.heightIncrement" [step]="0.1" [min]="-5" [max]="5"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Max slope width [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.maxSlopeWidth" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.maxSlopeWidth" [step]="1" [min]="0" [max]="50"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Min height [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.minHeight" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.minHeight" [step]="1" [min]="-50" [max]="0"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Max height [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.maxHeight" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.maxHeight" [step]="1" [min]="0" [max]="100"></p-slider>
      </div>
    </div>
  `
})
export class RaiseHeightBrushComponent extends AbstractBrush implements OnInit, OnDestroy {
  BrushType = BrushType;
  brushValues = new RaiseBrushValues();
  private heightMapCursor: HeightMapCursor | null = null;

  constructor(private renderService: BabylonRenderServiceAccessImpl) {
    super();
  }

  ngOnInit(): void {
    this.initEditorCursor();
  }

  ngOnDestroy(): void {
    if (this.heightMapCursor) {
      this.heightMapCursor.dispose();
      this.heightMapCursor = null;
    }
  }

  override showCursor() {
    if (this.heightMapCursor) {
      this.heightMapCursor.setVisibility(true);
    }
  }

  override hideCursor() {
    if (this.heightMapCursor) {
      this.heightMapCursor.setVisibility(false);
    }
  }

  onBrushValuesChanged() {
    // Cursor wird automatisch bei POINTERMOVE aktualisiert
  }

  override calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    const currentHeight = oldPosition.y;
    return this.calculateRaisedHeight(mousePosition, oldPosition, currentHeight);
  }

  private calculateRaisedHeight(centerPosition: Vector3, position: Vector3, currentHeight: number): number | null {
    switch (this.brushValues.type) {
      case BrushType.ROUND:
        return this.calculateHeightRound(centerPosition, position, currentHeight);
      case BrushType.SQUARE:
        return this.calculateHeightSquare(centerPosition, position, currentHeight);
    }
  }

  private calculateHeightRound(centerPosition: Vector3, position: Vector3, currentHeight: number): number | null {
    const radius = this.brushValues.size / 2.0;
    const distance = Vector2.Distance(new Vector2(position.x, position.z), new Vector2(centerPosition.x, centerPosition.z));

    if (distance < (radius + this.brushValues.maxSlopeWidth)) {
      let increment = this.brushValues.heightIncrement;

      if (distance > radius) {
        // Apply slope falloff
        const slopeDistance = distance - radius;
        const falloff = 1 - (slopeDistance / this.brushValues.maxSlopeWidth);
        increment *= falloff;
      }

      const newHeight = currentHeight + increment;
      return this.clampHeight(newHeight);
    }

    return null;
  }

  private calculateHeightSquare(centerPosition: Vector3, position: Vector3, currentHeight: number): number | null {
    const distanceX = Math.abs(centerPosition.x - position.x);
    const distanceZ = Math.abs(centerPosition.z - position.z);
    const halfSize = this.brushValues.size / 2.0;

    if (distanceX <= halfSize && distanceZ <= halfSize) {
      // Inside the main square area - full increment
      const newHeight = currentHeight + this.brushValues.heightIncrement;
      return this.clampHeight(newHeight);
    } else if (distanceX <= halfSize + this.brushValues.maxSlopeWidth &&
      distanceZ <= halfSize + this.brushValues.maxSlopeWidth) {
      // In the slope area
      const maxDistance = Math.max(distanceX, distanceZ);
      const slopeDistance = maxDistance - halfSize;
      const falloff = 1 - (slopeDistance / this.brushValues.maxSlopeWidth);
      const increment = this.brushValues.heightIncrement * falloff;

      const newHeight = currentHeight + increment;
      return this.clampHeight(newHeight);
    }

    return null;
  }

  private clampHeight(height: number): number {
    return Math.max(this.brushValues.minHeight, Math.min(this.brushValues.maxHeight, height));
  }

  private initEditorCursor() {
    const scene = this.renderService.getScene();
    // Create a BrushValues compatible object for the cursor
    const cursorBrushValues: BrushValues = {
      type: this.brushValues.type,
      height: 0,
      size: this.brushValues.size,
      maxSlopeWidth: this.brushValues.maxSlopeWidth,
      random: 0
    };
    this.heightMapCursor = new HeightMapCursor(scene, cursorBrushValues);

    scene.onPointerObservable.add((pointerInfo) => {
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERMOVE: {
          const pickingInfo = this.renderService.setupTerrainPickPoint();
          if (this.heightMapCursor) {
            const cursorBrushValues: BrushValues = {
              type: this.brushValues.type,
              height: 0,
              size: this.brushValues.size,
              maxSlopeWidth: this.brushValues.maxSlopeWidth,
              random: 0
            };
            if (pickingInfo.hit) {
              this.heightMapCursor.update(pickingInfo.pickedPoint!, cursorBrushValues);
            } else {
              // Fallback: use ground position at y=0 when terrain pick fails
              const fallbackPosition = this.renderService.setupPointerZeroLevelPosition();
              if (fallbackPosition && isFinite(fallbackPosition.x) && isFinite(fallbackPosition.z)) {
                this.heightMapCursor.update(fallbackPosition, cursorBrushValues);
              }
            }
          }
          break;
        }
      }
    });
  }
}
