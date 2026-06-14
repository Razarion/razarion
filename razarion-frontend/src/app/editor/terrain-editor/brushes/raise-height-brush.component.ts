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
  type: BrushType = BrushType.ROUND;
  strength: number = 0.5;    // height added at the brush center per application [m] (negative lowers)
  size: number = 40;         // brush diameter [m]; the falloff reaches zero at size/2
  falloff: number = 0.8;     // 0 = hard flat disc, 1 = fully smooth dome (smooth radial falloff)
  minHeight: number = -50;
  maxHeight: number = 200;
}

/**
 * Sculpt brush (raise / lower). Modelled on the Unreal/Unity landscape "Sculpt" tool: instead of
 * stamping a flat-topped disc, each application adds `strength` metres at the center and tapers to
 * zero at the edge along a SMOOTH radial falloff curve. So a single dab is a rounded bump and
 * holding / dragging the cursor naturally builds up smooth hills and mountains (the center is hit by
 * more overlapping dabs than the rim, so it domes up by itself) — you sculpt by eye with live
 * feedback rather than dialing in a parametric shape.
 *
 * `falloff` (0..1) is the single shape control: 0 keeps a hard flat disc (old behaviour), 1 is a
 * fully smooth dome with no flat top. Negative `strength` lowers the terrain with the same curve.
 *
 * Applies continuously on drag (not stamp mode), once per pointer sample.
 */
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
      <span class="col-span-5">Strength [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.strength" [step]="0.05" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.strength" [step]="0.05" [min]="-5" [max]="5"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Falloff</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.falloff" [step]="0.05" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.falloff" [step]="0.05" [min]="0" [max]="1"></p-slider>
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
        <p-slider [(ngModel)]="brushValues.maxHeight" [step]="1" [min]="0" [max]="200"></p-slider>
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

  override getEffectiveRadius(): number {
    return this.brushValues.size / 2;
  }

  override calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    const radius = this.brushValues.size / 2.0;
    if (radius <= 0) {
      return null;
    }

    // Normalized distance 0 (center) .. 1 (edge), round or square.
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

    const newHeight = oldPosition.y + this.brushValues.strength * weight;
    return Math.max(this.brushValues.minHeight, Math.min(this.brushValues.maxHeight, newHeight));
  }

  /**
   * Smooth radial falloff in [0,1]. An inner plateau of ratio (1 - falloff) stays at full weight,
   * then a smoothstep curve eases to zero at the edge. falloff=0 -> hard flat disc;
   * falloff=1 -> smooth dome peaking only at the exact center.
   */
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
    const u = (t - plateau) / (1 - plateau); // 0..1 across the falloff band
    return 1 - u * u * (3 - 2 * u);          // smoothstep, 1 -> 0
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

  private cursorValues(): BrushValues {
    return {
      type: this.brushValues.type,
      height: 0,
      size: this.brushValues.size,
      maxSlopeWidth: 0,
      random: 0
    };
  }
}
