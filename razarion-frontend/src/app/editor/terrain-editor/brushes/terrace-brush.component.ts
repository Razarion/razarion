import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {PointerEventTypes, Vector2, Vector3} from '@babylonjs/core';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {BabylonRenderServiceAccessImpl} from '../../../game/renderer/babylon-render-service-access-impl.service';
import {HeightMapCursor} from './height-map-cursor';
import {BrushType, BrushValues} from './fix-height-brush.component';

export enum TerraceMode {
  STEP = 'STEP',    // hard quantize: flat benches, sharp (near-vertical) risers
  ROUND = 'ROUND'   // inverse/soft: benches still form but risers stay smooth ramps (gentler)
}

export class TerraceBrushValues {
  type: BrushType = BrushType.ROUND;
  mode: TerraceMode = TerraceMode.STEP;
  size: number = 80;
  strength: number = 0.5;    // 0..1 how far each pass pulls toward the stepped height
  stepHeight: number = 4;    // altitude between terraces [m]
  falloff: number = 0.7;     // 0 = hard edge, 1 = smooth edge
}

/**
 * Terrace brush — quantizes height into flat steps under the cursor (pattern from
 * heightmap-generator's terrace filter: stepped = round(h/step)*step; h += (stepped-h)*strength).
 * Painted over an already-sculpted mountain it carves real terraces that follow the mountain's
 * irregular contours — flat benches with steep risers between them — which is the natural "layered"
 * look WITHOUT the circular wedding-cake of a parametric stamp. The risers come out >= the rock
 * threshold so they render as rock; benches are walkable. Apply repeatedly to deepen the steps.
 *
 * Drag-applied (not stamp), so it benefits from the host's stroke interpolation.
 */
@Component({
  selector: 'terrace-brush',
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
      <span class="col-span-5">Mode</span>
      <div class="col-span-7">
        <p-select
          [options]="[{label: 'Step (hard)', value: TerraceMode.STEP}, {label: 'Round (soft)', value: TerraceMode.ROUND}]"
          [(ngModel)]="brushValues.mode"
          [style]="{ width: '100%' }">
        </p-select>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Size [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.size" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.size" [step]="1" [min]="1" [max]="500"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Step height [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.stepHeight" [step]="0.5" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.stepHeight" [step]="0.5" [min]="1" [max]="20"></p-slider>
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
      <span class="col-span-5">Falloff</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.falloff" [step]="0.05" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.falloff" [step]="0.05" [min]="0" [max]="1"></p-slider>
      </div>
    </div>
  `
})
export class TerraceBrushComponent extends AbstractBrush implements OnInit, OnDestroy {
  BrushType = BrushType;
  TerraceMode = TerraceMode;
  brushValues = new TerraceBrushValues();
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

  override calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    const radius = this.brushValues.size / 2.0;
    const step = this.brushValues.stepHeight;
    if (radius <= 0 || step <= 0) {
      return null;
    }

    let t: number;
    if (this.brushValues.type === BrushType.SQUARE) {
      const dx = Math.abs(oldPosition.x - mousePosition.x);
      const dz = Math.abs(oldPosition.z - mousePosition.z);
      t = Math.max(dx, dz) / radius;
    } else {
      t = Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z),
        new Vector2(mousePosition.x, mousePosition.z)) / radius;
    }

    const weight = this.falloffWeight(t);
    if (weight <= 0) {
      return null;
    }

    const ratio = oldPosition.y / step;
    let stepped: number;
    if (this.brushValues.mode === TerraceMode.ROUND) {
      // Inverse / soft: smoothstep the fractional part so values near a level still flatten into a
      // bench, but mid-riser stays a smooth ramp instead of collapsing to a near-vertical edge.
      const base = Math.floor(ratio);
      const frac = ratio - base;
      const soft = frac * frac * (3 - 2 * frac);
      stepped = (base + soft) * step;
    } else {
      // Hard: snap to the nearest level -> flat benches, sharp risers.
      stepped = Math.round(ratio) * step;
    }
    return oldPosition.y + (stepped - oldPosition.y) * this.brushValues.strength * weight;
  }

  private falloffWeight(t: number): number {
    if (t >= 1) {
      return 0;
    }
    if (t <= 0) {
      return 1;
    }
    const plateau = 1 - this.brushValues.falloff;
    if (t <= plateau) {
      return 1;
    }
    const u = (t - plateau) / (1 - plateau);
    return 1 - u * u * (3 - 2 * u);
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
      type: this.brushValues.type,
      height: 0,
      size: this.brushValues.size,
      maxSlopeWidth: 0,
      random: 0
    };
  }

  private initEditorCursor() {
    const scene = this.renderService.getScene();
    this.heightMapCursor = new HeightMapCursor(scene, this.cursorValues());

    scene.onPointerObservable.add((pointerInfo) => {
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
