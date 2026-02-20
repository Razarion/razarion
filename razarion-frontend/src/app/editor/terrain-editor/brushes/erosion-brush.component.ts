import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {PointerEventTypes, Vector2, Vector3} from '@babylonjs/core';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {BabylonRenderServiceAccessImpl} from '../../../game/renderer/babylon-render-service-access-impl.service';
import {HeightMapCursor} from './height-map-cursor';
import {BrushType, BrushValues} from './fix-height-brush.component';

export class ErosionBrushValues {
  type: BrushType = BrushType.ROUND;
  size: number = 100;
  iterations: number = 100;
  erosionRate: number = 0.3;
  depositionRate: number = 0.3;
  strength: number = 0.5;
}

@Component({
  selector: 'erosion-brush',
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
        <p-slider [(ngModel)]="brushValues.size" [step]="1" [min]="20"
                  [max]="500"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Iterations</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.iterations" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.iterations" [step]="10" [min]="10" [max]="500"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Erosion rate</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.erosionRate" [step]="0.05" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.erosionRate" [step]="0.05" [min]="0.05" [max]="1"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Deposition rate</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.depositionRate" [step]="0.05" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.depositionRate" [step]="0.05" [min]="0.05" [max]="1"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Strength</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.strength" [step]="0.05" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.strength" [step]="0.05" [min]="0" [max]="1"></p-slider>
      </div>
    </div>
  `
})
export class ErosionBrushComponent extends AbstractBrush implements OnInit, OnDestroy {
  BrushType = BrushType;
  brushValues = new ErosionBrushValues();
  private heightMapCursor: HeightMapCursor | null = null;

  // Erosion simulation results
  private erosionMap: Map<string, number> | null = null;

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

  override isStampMode(): boolean {
    return true;
  }

  override isContextDependent(): boolean {
    return true;
  }

  override isInRadius(mousePosition: Vector3, oldPosition: Vector3): boolean {
    return this.isInBrushArea(mousePosition, oldPosition);
  }

  override preCalculate(mousePosition: Vector3): void {
    if (!this.brushContext) {
      return;
    }

    const spatialHeights = this.brushContext.getSpatialHeights();
    if (spatialHeights.length === 0) {
      this.erosionMap = null;
      return;
    }

    // Build a 2D height grid from spatial data
    let minX = Infinity, minZ = Infinity, maxX = -Infinity, maxZ = -Infinity;
    for (const sh of spatialHeights) {
      if (sh.x < minX) minX = sh.x;
      if (sh.z < minZ) minZ = sh.z;
      if (sh.x > maxX) maxX = sh.x;
      if (sh.z > maxZ) maxZ = sh.z;
    }

    const gridStep = 1; // 1 meter resolution
    const gridW = Math.ceil((maxX - minX) / gridStep) + 1;
    const gridH = Math.ceil((maxZ - minZ) / gridStep) + 1;

    if (gridW <= 2 || gridH <= 2) {
      this.erosionMap = null;
      return;
    }

    // Initialize height grid
    const grid = new Float32Array(gridW * gridH);
    const count = new Uint8Array(gridW * gridH);

    for (const sh of spatialHeights) {
      const gx = Math.round((sh.x - minX) / gridStep);
      const gz = Math.round((sh.z - minZ) / gridStep);
      if (gx >= 0 && gx < gridW && gz >= 0 && gz < gridH) {
        const idx = gz * gridW + gx;
        grid[idx] += sh.height;
        count[idx]++;
      }
    }

    // Average where multiple points fall into same cell
    for (let i = 0; i < grid.length; i++) {
      if (count[i] > 1) grid[i] /= count[i];
    }

    // Save original grid for delta computation
    const originalGrid = new Float32Array(grid);

    // Simulate hydraulic erosion: drop N water droplets
    const maxSteps = 64;
    const gravity = 4;
    const evaporation = 0.02;
    const minSlope = 0.01;

    for (let iter = 0; iter < this.brushValues.iterations; iter++) {
      // Random start position within the brush area
      let px = Math.random() * (gridW - 2) + 1;
      let pz = Math.random() * (gridH - 2) + 1;
      let dirX = 0, dirZ = 0;
      let speed = 1;
      let water = 1;
      let sediment = 0;

      for (let step = 0; step < maxSteps; step++) {
        const cellX = Math.floor(px);
        const cellZ = Math.floor(pz);
        if (cellX < 1 || cellX >= gridW - 1 || cellZ < 1 || cellZ >= gridH - 1) break;

        // Bilinear interpolation offsets
        const u = px - cellX;
        const v = pz - cellZ;
        const idx00 = cellZ * gridW + cellX;
        const idx10 = idx00 + 1;
        const idx01 = idx00 + gridW;
        const idx11 = idx01 + 1;

        // Current height
        const h00 = grid[idx00];
        const h10 = grid[idx10];
        const h01 = grid[idx01];
        const h11 = grid[idx11];
        const height = h00 * (1 - u) * (1 - v) + h10 * u * (1 - v) + h01 * (1 - u) * v + h11 * u * v;

        // Gradient
        const gradX = (h10 - h00) * (1 - v) + (h11 - h01) * v;
        const gradZ = (h01 - h00) * (1 - u) + (h11 - h10) * u;

        // Update direction with inertia
        dirX = dirX * 0.5 - gradX * gravity;
        dirZ = dirZ * 0.5 - gradZ * gravity;

        const dirLen = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (dirLen < 0.0001) break;
        dirX /= dirLen;
        dirZ /= dirLen;

        // Move droplet
        const newPx = px + dirX;
        const newPz = pz + dirZ;

        // New height at new position
        const newCellX = Math.floor(newPx);
        const newCellZ = Math.floor(newPz);
        if (newCellX < 0 || newCellX >= gridW - 1 || newCellZ < 0 || newCellZ >= gridH - 1) break;

        const nu = newPx - newCellX;
        const nv = newPz - newCellZ;
        const nIdx00 = newCellZ * gridW + newCellX;
        const newHeight = grid[nIdx00] * (1 - nu) * (1 - nv)
          + grid[nIdx00 + 1] * nu * (1 - nv)
          + grid[nIdx00 + gridW] * (1 - nu) * nv
          + grid[nIdx00 + gridW + 1] * nu * nv;

        const heightDiff = newHeight - height;

        if (heightDiff > 0) {
          // Uphill: deposit sediment
          const deposit = Math.min(sediment, heightDiff);
          sediment -= deposit;
          grid[idx00] += deposit * (1 - u) * (1 - v);
          grid[idx10] += deposit * u * (1 - v);
          grid[idx01] += deposit * (1 - u) * v;
          grid[idx11] += deposit * u * v;
        } else {
          // Downhill: erode
          const slope = Math.max(-heightDiff, minSlope);
          const capacity = slope * speed * water;
          if (sediment > capacity) {
            // Deposit excess
            const deposit = (sediment - capacity) * this.brushValues.depositionRate;
            sediment -= deposit;
            grid[idx00] += deposit * (1 - u) * (1 - v);
            grid[idx10] += deposit * u * (1 - v);
            grid[idx01] += deposit * (1 - u) * v;
            grid[idx11] += deposit * u * v;
          } else {
            // Erode
            const erode = Math.min((capacity - sediment) * this.brushValues.erosionRate, -heightDiff);
            sediment += erode;
            grid[idx00] -= erode * (1 - u) * (1 - v);
            grid[idx10] -= erode * u * (1 - v);
            grid[idx01] -= erode * (1 - u) * v;
            grid[idx11] -= erode * u * v;
          }
        }

        speed = Math.sqrt(Math.max(0, speed * speed - heightDiff * gravity));
        water *= (1 - evaporation);
        px = newPx;
        pz = newPz;

        if (water < 0.01) break;
      }
    }

    // Compute delta map (erosion - original) keyed by world position
    this.erosionMap = new Map<string, number>();
    for (let gz = 0; gz < gridH; gz++) {
      for (let gx = 0; gx < gridW; gx++) {
        const idx = gz * gridW + gx;
        const delta = grid[idx] - originalGrid[idx];
        if (Math.abs(delta) > 0.0001) {
          const worldX = minX + gx * gridStep;
          const worldZ = minZ + gz * gridStep;
          this.erosionMap.set(`${Math.round(worldX)},${Math.round(worldZ)}`, delta);
        }
      }
    }
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    if (!this.isInBrushArea(mousePosition, oldPosition) || !this.erosionMap) {
      return null;
    }

    const key = `${Math.round(oldPosition.x)},${Math.round(oldPosition.z)}`;
    const delta = this.erosionMap.get(key);

    if (delta === undefined) {
      return null;
    }

    // Apply strength and edge falloff
    const radius = this.brushValues.size / 2;
    const dx = oldPosition.x - mousePosition.x;
    const dz = oldPosition.z - mousePosition.z;
    const distance = Math.sqrt(dx * dx + dz * dz);
    const edgeFalloff = Math.max(0, 1 - distance / radius);

    return oldPosition.y + delta * this.brushValues.strength * edgeFalloff;
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
