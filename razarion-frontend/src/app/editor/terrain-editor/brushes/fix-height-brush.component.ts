import { Component } from '@angular/core';
import { AbstractBrush } from './abstract-brush';
import { Vector2, Vector3 } from '@babylonjs/core';

@Component({
  selector: 'fix-height-brush',
  template: `
    <div class="field grid align-items-center">
      <span class="col">Height [m]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="height" class="w-full"/>
        <p-slider [(ngModel)]="height" [step]="0.1" [min]="-20" [max]="50"></p-slider>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Diameter [m]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="diameter" class="w-full"/>
        <p-slider [(ngModel)]="diameter" [step]="0.01" [min]="1" [max]="100"></p-slider>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Max slope width [m]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="maxSlopeWidth" class="w-full"/>
        <p-slider [(ngModel)]="maxSlopeWidth" [step]="0.01" [min]="0" [max]="100"></p-slider>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Slope [&deg;]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="slope" class="w-full"/>
        <p-slider [(ngModel)]="slope" [step]="0.01" [min]="0" [max]="90"></p-slider>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Random (Slope) [m]</span>
      <div class="col">
        <input type="number" pInputText [(ngModel)]="random" class="w-full"/>
        <p-slider [(ngModel)]="random" [step]="0.01" [min]="0" [max]="5"></p-slider>
      </div>
    </div>
  `
})
export class FixHeightBrushComponent extends AbstractBrush {
  height: number = 1;
  diameter: number = 10;
  maxSlopeWidth: number = 10;
  slope: number = 30;
  random: number = 0;

  constructor() {
    super();
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3, avgHeight: number | undefined): number | null {
    const radius = this.diameter / 2.0;
    let distance = Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z));
    if (distance < (radius + this.maxSlopeWidth)) {
      let newHeight: number | null = null;
      if (distance <= radius) {
        newHeight = this.height;
      } else {
        let slopeDistance = distance - radius;
        let deltaHeight = Math.tan(this.slope * Math.PI / 180) * slopeDistance;
        let direction = this.height - oldPosition.y;
        let random = (this.random * (Math.random() - 0.5) * 2.0)
        if (direction > 0) {
          // up
          let calculatedHeight = this.height + random - deltaHeight;
          if (calculatedHeight > oldPosition.y) {
            newHeight = calculatedHeight;
          }
        } else if (direction < 0) {
          // down
          let calculatedHeight = this.height + random + deltaHeight;
          if (calculatedHeight < oldPosition.y) {
            newHeight = calculatedHeight;
          }
        }
      }
      return newHeight
    } else {
      return null;
    }
  }

}
