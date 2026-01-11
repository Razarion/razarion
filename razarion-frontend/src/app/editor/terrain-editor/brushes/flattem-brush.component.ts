import {Component, OnDestroy, OnInit} from '@angular/core';
import {AbstractBrush} from './abstract-brush';
import {PointerEventTypes, Vector2, Vector3} from '@babylonjs/core';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';
import {BabylonRenderServiceAccessImpl} from '../../../game/renderer/babylon-render-service-access-impl.service';
import {HeightMapCursor} from './height-map-cursor';
import {BrushType, BrushValues} from './fix-height-brush.component';

@Component({
  selector: 'fix-height-brush',
  imports: [
    Slider,
    FormsModule
  ],
  template: `
    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Diameter</span>
      <div class="col-span-7">
        <input type="text" pInputText [(ngModel)]="diameter" class="w-full"/>
        <p-slider [(ngModel)]="diameter" [step]="0.01" [min]="1" [max]="100"></p-slider>
      </div>
    </div>
    <div class="grid grid-cols-12 gap-1 p-1">
      <span class="col-span-5">Strength</span>
      <div class="col-span-7">
        <input type="number" [(ngModel)]="strength" [step]="0.1" class="w-full"/>
        <p-slider [(ngModel)]="strength" [step]="0.1" [min]="0" [max]="1"></p-slider>
      </div>
    </div>
  `
})
export class FlattenBrushComponent extends AbstractBrush implements OnInit, OnDestroy {
  diameter: number = 10;
  strength: number = 0.5;
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

  override isInRadius(mousePosition: Vector3, oldPosition: Vector3): boolean {
    return Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z)) < (this.diameter / 2.0);
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    const radius = this.diameter / 2.0;
    const distance = Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z));
    if (distance < radius) {
      const force = (radius - distance) / radius * this.strength;
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

  private createBrushValues(): BrushValues {
    return {
      type: BrushType.ROUND,
      height: 0,
      size: this.diameter,
      maxSlopeWidth: 0,
      random: 0
    };
  }

  private initEditorCursor() {
    const scene = this.renderService.getScene();
    this.heightMapCursor = new HeightMapCursor(scene, this.createBrushValues());

    scene.onPointerObservable.add((pointerInfo) => {
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERMOVE: {
          const pickingInfo = this.renderService.setupTerrainPickPoint();
          if (this.heightMapCursor) {
            const brushValues = this.createBrushValues();
            if (pickingInfo.hit) {
              this.heightMapCursor.update(pickingInfo.pickedPoint!, brushValues);
            } else {
              // Fallback: use ground position at y=0 when terrain pick fails
              const fallbackPosition = this.renderService.setupPointerZeroLevelPosition();
              if (fallbackPosition && isFinite(fallbackPosition.x) && isFinite(fallbackPosition.z)) {
                this.heightMapCursor.update(fallbackPosition, brushValues);
              }
            }
          }
          break;
        }
      }
    });
  }
}
