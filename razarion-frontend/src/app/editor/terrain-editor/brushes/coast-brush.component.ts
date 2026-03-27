import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush, SpatialHeight} from './abstract-brush';
import {PointerEventTypes, Vector2, Vector3} from '@babylonjs/core';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {BabylonRenderServiceAccessImpl} from '../../../game/renderer/babylon-render-service-access-impl.service';
import {HeightMapCursor} from './height-map-cursor';
import {BrushType, BrushValues} from './fix-height-brush.component';

export class CoastBrushValues {
  type: BrushType = BrushType.ROUND;
  size: number = 100;
  beachWidth: number = 10;
  landHeight: number = 0.3;
  underwaterWidth: number = 15;
  underwaterDepth: number = 1.5;
  strength: number = 0.7;
}

@Component({
  selector: 'coast-brush',
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
        <p-slider [(ngModel)]="brushValues.size" [step]="1" [min]="20" [max]="500"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Beach width [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.beachWidth" [step]="0.5" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.beachWidth" [step]="0.5" [min]="1" [max]="50"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Land height [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.landHeight" [step]="0.01" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.landHeight" [step]="0.01" [min]="0.1" [max]="2.0"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Underwater width [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.underwaterWidth" [step]="0.5" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.underwaterWidth" [step]="0.5" [min]="1" [max]="50"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Underwater depth [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.underwaterDepth" [step]="0.1" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.underwaterDepth" [step]="0.1" [min]="0.5" [max]="5.0"></p-slider>
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
export class CoastBrushComponent extends AbstractBrush implements OnInit, OnDestroy {
  BrushType = BrushType;
  brushValues = new CoastBrushValues();
  private heightMapCursor: HeightMapCursor | null = null;
  private idealHeightMap: Map<string, number> | null = null;

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
      this.idealHeightMap = null;
      return;
    }

    const spatialHeights = this.brushContext.getSpatialHeights();
    if (spatialHeights.length === 0) {
      this.idealHeightMap = null;
      return;
    }

    // Step 1: Build height grid
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
      this.idealHeightMap = null;
      return;
    }

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

    // Step 2: Find waterline contour cells (sign change with neighbor)
    const isContour = new Uint8Array(gridW * gridH);
    let hasContour = false;

    for (let gz = 0; gz < gridH; gz++) {
      for (let gx = 0; gx < gridW; gx++) {
        const idx = gz * gridW + gx;
        if (count[idx] === 0) continue;
        const h = grid[idx];
        const neighbors = [
          gx > 0 ? idx - 1 : -1,
          gx < gridW - 1 ? idx + 1 : -1,
          gz > 0 ? idx - gridW : -1,
          gz < gridH - 1 ? idx + gridW : -1
        ];
        for (const ni of neighbors) {
          if (ni >= 0 && count[ni] > 0) {
            if ((h >= 0 && grid[ni] < 0) || (h < 0 && grid[ni] >= 0)) {
              isContour[idx] = 1;
              hasContour = true;
              break;
            }
          }
        }
      }
    }

    if (!hasContour) {
      this.idealHeightMap = null;
      return;
    }

    // Step 3: BFS signed distance from contour cells
    const signedDistance = new Float32Array(gridW * gridH);
    signedDistance.fill(Infinity);
    const queue: number[] = [];

    for (let i = 0; i < gridW * gridH; i++) {
      if (isContour[i]) {
        signedDistance[i] = 0;
        queue.push(i);
      }
    }

    let head = 0;
    while (head < queue.length) {
      const idx = queue[head++];
      const gx = idx % gridW;
      const gz = Math.floor(idx / gridW);
      const currentDist = signedDistance[idx];

      const neighbors = [
        gx > 0 ? idx - 1 : -1,
        gx < gridW - 1 ? idx + 1 : -1,
        gz > 0 ? idx - gridW : -1,
        gz < gridH - 1 ? idx + gridW : -1
      ];

      for (const ni of neighbors) {
        if (ni >= 0 && signedDistance[ni] === Infinity && count[ni] > 0) {
          signedDistance[ni] = currentDist + gridStep;
          queue.push(ni);
        }
      }
    }

    // Apply sign: negative for underwater
    for (let i = 0; i < gridW * gridH; i++) {
      if (grid[i] < 0) {
        signedDistance[i] = -signedDistance[i];
      }
    }

    // Step 4: Map signed distance to ideal coast height
    this.idealHeightMap = new Map<string, number>();
    for (let gz = 0; gz < gridH; gz++) {
      for (let gx = 0; gx < gridW; gx++) {
        const idx = gz * gridW + gx;
        if (count[idx] === 0) continue;
        const dist = signedDistance[idx];
        if (!isFinite(dist)) continue;

        const ideal = this.idealCoastHeight(dist);
        const worldX = minX + gx * gridStep;
        const worldZ = minZ + gz * gridStep;
        this.idealHeightMap.set(`${Math.round(worldX)},${Math.round(worldZ)}`, ideal);
      }
    }
  }

  private idealCoastHeight(signedDist: number): number {
    if (signedDist >= 0) {
      // Land side: smoothstep from 0 at waterline to landHeight at beachWidth
      const t = Math.min(signedDist / this.brushValues.beachWidth, 1.0);
      const s = t * t * (3 - 2 * t);
      return s * this.brushValues.landHeight;
    } else {
      // Water side: smoothstep from 0 at waterline to -underwaterDepth at underwaterWidth
      const t = Math.min(-signedDist / this.brushValues.underwaterWidth, 1.0);
      const s = t * t * (3 - 2 * t);
      return -s * this.brushValues.underwaterDepth;
    }
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    if (!this.isInBrushArea(mousePosition, oldPosition) || !this.idealHeightMap) {
      return null;
    }

    const key = `${Math.round(oldPosition.x)},${Math.round(oldPosition.z)}`;
    const idealHeight = this.idealHeightMap.get(key);

    if (idealHeight === undefined) {
      return null;
    }

    // Edge falloff: reduce effect near brush edge
    const radius = this.brushValues.size / 2;
    const dx = oldPosition.x - mousePosition.x;
    const dz = oldPosition.z - mousePosition.z;
    const distance = Math.sqrt(dx * dx + dz * dz);
    const edgeFalloff = Math.max(0, 1 - distance / radius);

    // Blend from current height toward ideal height
    const blendFactor = this.brushValues.strength * edgeFalloff;
    return oldPosition.y + (idealHeight - oldPosition.y) * blendFactor;
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
