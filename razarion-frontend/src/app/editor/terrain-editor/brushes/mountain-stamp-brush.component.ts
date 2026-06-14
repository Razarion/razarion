import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {PointerEventTypes, Vector3} from '@babylonjs/core';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {BabylonRenderServiceAccessImpl} from '../../../game/renderer/babylon-render-service-access-impl.service';
import {HeightMapCursor} from './height-map-cursor';
import {BrushType, BrushValues} from './fix-height-brush.component';
import {HeightmapStamp} from './heightmap-stamp';

export class MountainStampValues {
  preset: string = 'peak';
  size: number = 140;        // footprint diameter [m]
  height: number = 35;       // summit altitude [m] (the stamp's 1.0 maps to this; foot = 0)
  rotation: number = 0;      // [degrees]
  seed: number = 1;          // reroll for a different instance of the same preset
}

/**
 * Mountain heightmap-stamp brush (World-Creator style). Picks a procedurally generated mountain
 * heightmap (see HeightmapStamp — ridged-multifractal, so it has real fractal relief and an
 * irregular outline), then scales / rotates / amplitude-maps it and stamps it into the terrain on a
 * single click, blended by max (raises only). This is the "instant realistic mountain" path that
 * complements the freehand Sculpt brush.
 *
 * The stamp is absolute and idempotent: its summit value 1.0 maps to `height` metres (foot at 0,
 * independent of mousePosition.y), so re-clicking the same spot reproduces the identical mountain.
 * The grid is sampled in world space relative to the stamp center + rotation, so shared tile-edge
 * vertices match (no cracks). The radial edge mask makes the foot blend seamlessly into the ground.
 */
@Component({
  selector: 'mountain-stamp-brush',
  imports: [
    Slider,
    FormsModule,
    Select
  ],
  template: `
    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Shape</span>
      <div class="col-span-7">
        <p-select [options]="presets" [(ngModel)]="brushValues.preset"
                  optionLabel="label" optionValue="value"
                  [style]="{ width: '100%' }">
        </p-select>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Size [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.size" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.size" [step]="1" [min]="20" [max]="600"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Height [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.height" [step]="1" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.height" [step]="1" [min]="1" [max]="120"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Rotation [°]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.rotation" [step]="5" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.rotation" [step]="5" [min]="0" [max]="360"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Seed</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.seed" [step]="1" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.seed" [step]="1" [min]="0" [max]="9999"></p-slider>
      </div>
    </div>
  `
})
export class MountainStampComponent extends AbstractBrush implements OnInit, OnDestroy {
  presets = HeightmapStamp.PRESETS;
  brushValues = new MountainStampValues();
  private heightMapCursor: HeightMapCursor | null = null;
  private cursorPointerObserver: any = null;

  // Cached generated stamp (regenerated only when preset or seed changes).
  private stamp: HeightmapStamp | null = null;
  private stampKey = '';

  // Per-stamp transform (computed in preCalculate).
  private ready = false;
  private cx = 0;
  private cz = 0;
  private halfSize = 0;
  private cosA = 1;
  private sinA = 0;

  constructor(private renderService: BabylonRenderServiceAccessImpl) {
    super();
  }

  ngOnInit(): void {
    this.initEditorCursor();
  }

  ngOnDestroy(): void {
    if (this.cursorPointerObserver) {
      this.renderService.getScene().onPointerObservable.remove(this.cursorPointerObserver);
      this.cursorPointerObserver = null;
    }
    if (this.heightMapCursor) {
      this.heightMapCursor.dispose();
      this.heightMapCursor = null;
    }
  }

  override isStampMode(): boolean {
    return true;
  }

  override getEffectiveRadius(): number {
    // Rotated square footprint: half-diagonal.
    return this.brushValues.size / 2 * Math.SQRT2 + 2;
  }

  override preCalculate(mousePosition: Vector3): void {
    const key = `${this.brushValues.preset}|${this.brushValues.seed}`;
    if (!this.stamp || key !== this.stampKey) {
      this.stamp = new HeightmapStamp(this.brushValues.preset, this.brushValues.seed);
      this.stampKey = key;
    }
    this.cx = mousePosition.x;
    this.cz = mousePosition.z;
    this.halfSize = Math.max(1, this.brushValues.size / 2);
    const a = -this.brushValues.rotation * Math.PI / 180; // rotate sample point into stamp space
    this.cosA = Math.cos(a);
    this.sinA = Math.sin(a);
    this.ready = this.brushValues.height > 0;
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    if (!this.ready || !this.stamp) {
      return null;
    }

    // World -> stamp-local (rotate around center), then to normalized [0,1] UV.
    const dx = oldPosition.x - this.cx;
    const dz = oldPosition.z - this.cz;
    const lx = dx * this.cosA - dz * this.sinA;
    const lz = dx * this.sinA + dz * this.cosA;
    const u = lx / (2 * this.halfSize) + 0.5;
    const v = lz / (2 * this.halfSize) + 0.5;
    if (u < 0 || u > 1 || v < 0 || v > 1) {
      return null;
    }

    const h = this.stamp.sample(u, v) * this.brushValues.height;
    if (h <= 0) {
      return null;
    }

    // Absolute, only-raise: foot (sample 0) sits at altitude 0 and blends via max.
    return Math.max(oldPosition.y, h);
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

  private cursorValues(): BrushValues {
    return {
      type: BrushType.ROUND,
      height: 0,
      size: this.brushValues.size,
      maxSlopeWidth: 0,
      random: 0
    };
  }

  private initEditorCursor() {
    const scene = this.renderService.getScene();
    this.heightMapCursor = new HeightMapCursor(scene, this.cursorValues());

    this.cursorPointerObserver = scene.onPointerObservable.add((pointerInfo) => {
      if (pointerInfo.type === PointerEventTypes.POINTERMOVE && this.heightMapCursor) {
        const pickingInfo = this.renderService.setupTerrainPickPoint();
        if (pickingInfo.hit) {
          this.heightMapCursor.update(pickingInfo.pickedPoint!, this.cursorValues());
        } else {
          const fallbackPosition = this.renderService.setupPointerZeroLevelPosition();
          if (fallbackPosition && isFinite(fallbackPosition.x) && isFinite(fallbackPosition.z)) {
            this.heightMapCursor.update(fallbackPosition, this.cursorValues());
          }
        }
      }
    });
  }
}
