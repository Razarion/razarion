import { Component } from '@angular/core';
import { AbstractBrush } from './abstract-brush';
import { Vector2, Vector3 } from '@babylonjs/core';
import {Slider} from 'primeng/slider';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'fix-height-brush',
  imports: [
    Slider,
    FormsModule
  ],
  template: `
    <div class="field grid align-items-center">
      <span class="col">Diameter</span>
      <div class="col">
        <input type="text" pInputText [(ngModel)]="diameter" class="w-full"/>
        <p-slider [(ngModel)]="diameter" [step]="0.01" [min]="1" [max]="100"></p-slider>
      </div>
    </div>
  `
})
export class FlattenBrushComponent extends AbstractBrush {
  diameter: number = 10;

  constructor() {
    super();
  }

  override isInRadius(mousePosition: Vector3, oldPosition: Vector3): boolean {
    return Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z)) < (this.diameter / 2.0);
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null {
    const radius = this.diameter / 2.0;
    const distance = Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z));
    if (distance < radius) {
      const force = (radius - distance) / radius;
      return (this.brushContext!.getAvgHeight() - oldPosition.y) * force + oldPosition.y;
    } else {
      return null;
    }
  }

  override isContextDependent(): boolean {
    return true;
  }
}
