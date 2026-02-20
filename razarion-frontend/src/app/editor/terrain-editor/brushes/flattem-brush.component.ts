import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {PointerEventTypes, Vector2, Vector3} from '@babylonjs/core';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {BabylonRenderServiceAccessImpl} from '../../../game/renderer/babylon-render-service-access-impl.service';
import {HeightMapCursor} from './height-map-cursor';
import {BrushType, BrushValues} from './fix-height-brush.component';
import {Select} from 'primeng/select';

export class FlattenBrushValues {
  type: BrushType = BrushType.SQUARE;
  size: number = 10;
  strength: number = 0.5;
}

@Component({
  selector: 'fix-height-brush',
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
          [style]="{ width: '100%' }">
        </p-select>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Size [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.size" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.size" [step]="1" [min]="1"
                  [max]="500"></p-slider>
      </div>
    </div>
    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Strength</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.strength" [step]="0.1" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.strength" [step]="0.1" [min]="0" [max]="1"></p-slider>
      </div>
    </div>
  `
})
export class FlattenBrushComponent extends AbstractBrush implements OnInit, OnDestroy {
  BrushType = BrushType;
  brushValues = new FlattenBrushValues();
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

  override getEffectiveRadius(): number {
    return this.brushValues.size / 2;
  }

  override isInRadius(mousePosition: Vector3, oldPosition: Vector3): boolean {
    return Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z)) < (this.brushValues.size / 2.0);
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    const radius = this.brushValues.size / 2.0;
    const distance = Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z));
    if (distance < radius) {
      const force = (radius - distance) / radius * this.brushValues.strength;
      return (this.brushContext!.getAvgHeight() - oldPosition.y) * force + oldPosition.y;
    } else {
      return null;
    }
  }

  override isContextDependent(): boolean {
    return true;
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

  private initEditorCursor() {
    const scene = this.renderService.getScene();
    const cursorBrushValues: BrushValues = {
      type: this.brushValues.type,
      height: 0,
      size: this.brushValues.size,
      maxSlopeWidth: 0,
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
              maxSlopeWidth: 0,
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
