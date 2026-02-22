import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {PointerEventTypes, Vector2, Vector3} from '@babylonjs/core';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {BabylonRenderServiceAccessImpl} from '../../../game/renderer/babylon-render-service-access-impl.service';
import {HeightMapCursor} from './height-map-cursor';
import {BrushType, BrushValues} from './fix-height-brush.component';
import {EditorTerrainTile} from '../editor-terrain-tile';
import {TerrainType} from '../../../gwtangular/GwtAngularFacade';

export class FixBoundaryBrushValues {
  type: BrushType = BrushType.ROUND;
  size: number = 100;
  strength: number = 0.5;
  iterations: number = 3;
}

@Component({
  selector: 'fix-boundary-brush',
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
      <span class="col-span-5">Strength</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.strength" [step]="0.05" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.strength" [step]="0.05" [min]="0" [max]="1"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Iterations</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.iterations" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.iterations" [step]="1" [min]="1" [max]="10"></p-slider>
      </div>
    </div>
  `
})
export class FixBoundaryBrushComponent extends AbstractBrush implements OnInit, OnDestroy {
  BrushType = BrushType;
  brushValues = new FixBoundaryBrushValues();
  private heightMapCursor: HeightMapCursor | null = null;
  private deltaMap: Map<string, number> | null = null;

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
      this.deltaMap = null;
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

    const gridStep = 1;
    const gridW = Math.ceil((maxX - minX) / gridStep) + 1;
    const gridH = Math.ceil((maxZ - minZ) / gridStep) + 1;

    if (gridW <= 2 || gridH <= 2) {
      this.deltaMap = null;
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

    for (let i = 0; i < grid.length; i++) {
      if (count[i] > 1) grid[i] /= count[i];
    }

    // Save original grid for delta computation
    const originalGrid = new Float32Array(grid);

    // Classify quads and fix boundary violations iteratively
    const quadW = gridW - 1;
    const quadH = gridH - 1;

    for (let iter = 0; iter < this.brushValues.iterations; iter++) {
      // Classify each quad
      const quadTypes = new Uint8Array(quadW * quadH); // 0=WATER, 1=LAND, 2=BLOCKED
      for (let qz = 0; qz < quadH; qz++) {
        for (let qx = 0; qx < quadW; qx++) {
          const bl = grid[qz * gridW + qx];
          const br = grid[qz * gridW + qx + 1];
          const tl = grid[(qz + 1) * gridW + qx];
          const tr = grid[(qz + 1) * gridW + qx + 1];
          const tt = EditorTerrainTile.setupTerrainType(bl, br, tr, tl);
          if (tt === TerrainType.WATER) quadTypes[qz * quadW + qx] = 0;
          else if (tt === TerrainType.LAND) quadTypes[qz * quadW + qx] = 1;
          else quadTypes[qz * quadW + qx] = 2; // BLOCKED
        }
      }

      // Find violations: BLOCKED adjacent to WATER (4-connected)
      const waterQuads = new Set<number>();
      const blockedQuads = new Set<number>();

      for (let qz = 0; qz < quadH; qz++) {
        for (let qx = 0; qx < quadW; qx++) {
          const qi = qz * quadW + qx;
          const type = quadTypes[qi];
          if (type !== 2) continue; // only check BLOCKED quads

          // Check 4 neighbors for WATER
          const neighbors = [
            qx > 0 ? qi - 1 : -1,
            qx < quadW - 1 ? qi + 1 : -1,
            qz > 0 ? qi - quadW : -1,
            qz < quadH - 1 ? qi + quadW : -1
          ];

          for (const ni of neighbors) {
            if (ni >= 0 && quadTypes[ni] === 0) {
              blockedQuads.add(qi);
              waterQuads.add(ni);
            }
          }
        }
      }

      if (waterQuads.size === 0 && blockedQuads.size === 0) {
        break; // No violations found
      }

      // Collect boundary vertices from water-side quads: raise above WATER_LEVEL
      const waterVertices = new Set<number>();
      for (const qi of waterQuads) {
        const qx = qi % quadW;
        const qz = Math.floor(qi / quadW);
        waterVertices.add(qz * gridW + qx);
        waterVertices.add(qz * gridW + qx + 1);
        waterVertices.add((qz + 1) * gridW + qx);
        waterVertices.add((qz + 1) * gridW + qx + 1);
      }

      const targetAboveWater = 0.1; // just above WATER_LEVEL (0)
      for (const vi of waterVertices) {
        if (grid[vi] < targetAboveWater) {
          grid[vi] = grid[vi] + (targetAboveWater - grid[vi]) * this.brushValues.strength;
        }
      }

      // Collect boundary vertices from blocked-side quads: smooth toward quad average
      for (const qi of blockedQuads) {
        const qx = qi % quadW;
        const qz = Math.floor(qi / quadW);
        const blIdx = qz * gridW + qx;
        const brIdx = qz * gridW + qx + 1;
        const tlIdx = (qz + 1) * gridW + qx;
        const trIdx = (qz + 1) * gridW + qx + 1;

        const avg = (grid[blIdx] + grid[brIdx] + grid[tlIdx] + grid[trIdx]) / 4;

        const indices = [blIdx, brIdx, tlIdx, trIdx];
        for (const vi of indices) {
          grid[vi] = grid[vi] + (avg - grid[vi]) * this.brushValues.strength;
        }
      }
    }

    // Compute delta map
    this.deltaMap = new Map<string, number>();
    for (let gz = 0; gz < gridH; gz++) {
      for (let gx = 0; gx < gridW; gx++) {
        const idx = gz * gridW + gx;
        const delta = grid[idx] - originalGrid[idx];
        if (Math.abs(delta) > 0.0001) {
          const worldX = minX + gx * gridStep;
          const worldZ = minZ + gz * gridStep;
          this.deltaMap.set(`${Math.round(worldX)},${Math.round(worldZ)}`, delta);
        }
      }
    }
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    if (!this.isInBrushArea(mousePosition, oldPosition) || !this.deltaMap) {
      return null;
    }

    const key = `${Math.round(oldPosition.x)},${Math.round(oldPosition.z)}`;
    const delta = this.deltaMap.get(key);

    if (delta === undefined) {
      return null;
    }

    // Apply edge falloff
    const radius = this.brushValues.size / 2;
    const dx = oldPosition.x - mousePosition.x;
    const dz = oldPosition.z - mousePosition.z;
    const distance = Math.sqrt(dx * dx + dz * dz);
    const edgeFalloff = Math.max(0, 1 - distance / radius);

    return oldPosition.y + delta * edgeFalloff;
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
