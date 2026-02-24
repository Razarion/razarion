import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {PointerEventTypes, Vector2, Vector3} from '@babylonjs/core';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {BabylonRenderServiceAccessImpl} from '../../../game/renderer/babylon-render-service-access-impl.service';
import {HeightMapCursor} from './height-map-cursor';
import {BrushType, BrushValues} from './fix-height-brush.component';
import {PerlinNoise} from './perlin-noise';

export class NoiseBrushValues {
  type: BrushType = BrushType.ROUND;
  size: number = 100;
  amplitude: number = 2;
  zoom: number = 50;
  octaves: number = 4;
  seed: number = 42;
}

@Component({
  selector: 'noise-brush',
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
      <span class="col-span-5">Amplitude [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.amplitude" [step]="0.1" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.amplitude" [step]="0.1" [min]="0.1" [max]="10"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Zoom [m]</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.zoom" [step]="1" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.zoom" [step]="1" [min]="2" [max]="1000"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Octaves</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.octaves" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.octaves" [step]="1" [min]="1" [max]="8"></p-slider>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Seed</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="brushValues.seed" class="w-full"/>
        <p-slider [(ngModel)]="brushValues.seed" [step]="1" [min]="0" [max]="9999"></p-slider>
      </div>
    </div>
  `
})
export class NoiseBrushComponent extends AbstractBrush implements OnInit, OnDestroy {
  BrushType = BrushType;
  brushValues = new NoiseBrushValues();
  private heightMapCursor: HeightMapCursor | null = null;
  private perlinNoise: PerlinNoise;

  constructor(private renderService: BabylonRenderServiceAccessImpl) {
    super();
    this.perlinNoise = new PerlinNoise(this.brushValues.seed);
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

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    const radius = this.brushValues.size / 2;
    const dx = oldPosition.x - mousePosition.x;
    const dz = oldPosition.z - mousePosition.z;

    let inside = false;
    switch (this.brushValues.type) {
      case BrushType.ROUND:
        inside = (dx * dx + dz * dz) < radius * radius;
        break;
      case BrushType.SQUARE:
        inside = Math.abs(dx) < radius && Math.abs(dz) < radius;
        break;
    }

    if (!inside) {
      return null;
    }

    // Recreate noise if seed changed
    this.perlinNoise = new PerlinNoise(this.brushValues.seed);

    // Use world coordinates for coherent noise, zoom stretches the noise
    const frequency = 1 / this.brushValues.zoom;
    const noiseValue = this.perlinNoise.fbm(
      oldPosition.x * frequency,
      oldPosition.z * frequency,
      this.brushValues.octaves
    );

    // Edge falloff for smooth blending
    const distance = Math.sqrt(dx * dx + dz * dz);
    const edgeFalloff = Math.max(0, 1 - distance / radius);
    const smoothFalloff = edgeFalloff * edgeFalloff * (3 - 2 * edgeFalloff); // smoothstep

    return oldPosition.y + noiseValue * this.brushValues.amplitude * smoothFalloff;
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
