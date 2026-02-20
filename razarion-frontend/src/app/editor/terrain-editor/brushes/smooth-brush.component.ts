import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {PointerEventTypes, Vector2, Vector3} from '@babylonjs/core';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {BabylonRenderServiceAccessImpl} from '../../../game/renderer/babylon-render-service-access-impl.service';
import {HeightMapCursor} from './height-map-cursor';
import {BrushType, BrushValues} from './fix-height-brush.component';
import {SpatialHeight} from './abstract-brush';

export class SmoothBrushValues {
  type: BrushType = BrushType.ROUND;
  size: number = 30;
  strength: number = 0.5;
  kernelRadius: number = 5;
}

@Component({
  selector: 'smooth-brush',
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
        <input type="number" [(ngModel)]="brushValues.strength" [step]="0.05" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.strength" [step]="0.05" [min]="0" [max]="1"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Kernel radius [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.kernelRadius" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.kernelRadius" [step]="1" [min]="1" [max]="20"></p-slider>
      </div>
    </div>
  `
})
export class SmoothBrushComponent extends AbstractBrush implements OnInit, OnDestroy {
  BrushType = BrushType;
  brushValues = new SmoothBrushValues();
  private heightMapCursor: HeightMapCursor | null = null;
  private spatialGrid: Map<string, SpatialHeight[]> | null = null;
  private gridCellSize: number = 1;

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
    return this.isInBrushArea(mousePosition, oldPosition);
  }

  override isContextDependent(): boolean {
    return true;
  }

  override preCalculate(mousePosition: Vector3): void {
    if (!this.brushContext) {
      return;
    }
    // Build spatial grid from context heights for fast neighbor lookup
    this.gridCellSize = Math.max(1, this.brushValues.kernelRadius);
    this.spatialGrid = new Map<string, SpatialHeight[]>();
    for (const sh of this.brushContext.getSpatialHeights()) {
      const key = this.gridKey(sh.x, sh.z);
      let cell = this.spatialGrid.get(key);
      if (!cell) {
        cell = [];
        this.spatialGrid.set(key, cell);
      }
      cell.push(sh);
    }
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    if (!this.isInBrushArea(mousePosition, oldPosition)) {
      return null;
    }

    if (!this.spatialGrid) {
      return null;
    }

    const kr = this.brushValues.kernelRadius;
    const sigma = kr / 2;
    const twoSigmaSq = 2 * sigma * sigma;

    let weightedSum = 0;
    let totalWeight = 0;

    // Search neighboring grid cells
    const cellX = Math.floor(oldPosition.x / this.gridCellSize);
    const cellZ = Math.floor(oldPosition.z / this.gridCellSize);
    const searchRadius = Math.ceil(kr / this.gridCellSize);

    for (let dz = -searchRadius; dz <= searchRadius; dz++) {
      for (let dx = -searchRadius; dx <= searchRadius; dx++) {
        const key = `${cellX + dx},${cellZ + dz}`;
        const cell = this.spatialGrid.get(key);
        if (!cell) continue;
        for (const sh of cell) {
          const ddx = sh.x - oldPosition.x;
          const ddz = sh.z - oldPosition.z;
          const distSq = ddx * ddx + ddz * ddz;
          if (distSq <= kr * kr) {
            const weight = Math.exp(-distSq / twoSigmaSq);
            weightedSum += sh.height * weight;
            totalWeight += weight;
          }
        }
      }
    }

    if (totalWeight === 0) {
      return null;
    }

    const smoothedHeight = weightedSum / totalWeight;

    // Apply brush strength: blend between original and smoothed
    const radius = this.brushValues.size / 2;
    const dx = oldPosition.x - mousePosition.x;
    const dz = oldPosition.z - mousePosition.z;
    const distance = Math.sqrt(dx * dx + dz * dz);
    const edgeFalloff = Math.max(0, 1 - distance / radius);
    const effectiveStrength = this.brushValues.strength * edgeFalloff;

    return oldPosition.y + (smoothedHeight - oldPosition.y) * effectiveStrength;
  }

  private isInBrushArea(mousePosition: Vector3, position: Vector3): boolean {
    const radius = this.brushValues.size / 2;
    switch (this.brushValues.type) {
      case BrushType.ROUND:
        return Vector2.Distance(
          new Vector2(position.x, position.z),
          new Vector2(mousePosition.x, mousePosition.z)
        ) < radius;
      case BrushType.SQUARE:
        return Math.abs(position.x - mousePosition.x) < radius &&
          Math.abs(position.z - mousePosition.z) < radius;
    }
  }

  private gridKey(x: number, z: number): string {
    return `${Math.floor(x / this.gridCellSize)},${Math.floor(z / this.gridCellSize)}`;
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
      if (pointerInfo.type === PointerEventTypes.POINTERMOVE && this.heightMapCursor) {
        const pickingInfo = this.renderService.setupTerrainPickPoint();
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
          const fallbackPosition = this.renderService.setupPointerZeroLevelPosition();
          if (fallbackPosition && isFinite(fallbackPosition.x) && isFinite(fallbackPosition.z)) {
            this.heightMapCursor.update(fallbackPosition, cursorBrushValues);
          }
        }
      }
    });
  }
}
