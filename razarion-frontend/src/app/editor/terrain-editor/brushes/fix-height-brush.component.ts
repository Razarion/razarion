import { Component, OnInit } from '@angular/core';
import { AbstractBrush } from './abstract-brush';
import { UpDownMode } from "../shape-terrain-editor.component";
import { Vector2, Vector3 } from '@babylonjs/core';

@Component({
  selector: 'fix-height-brush',
  template: `
    <div class="field grid align-items-center">
      <span class="col">Height</span>
      <div class="col">
        <input type="text" pInputText [(ngModel)]="height" class="w-full"/>
        <p-slider [(ngModel)]="height" [step]="0.1" [min]="-20" [max]="50"></p-slider>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Diameter</span>
      <div class="col">
        <input type="text" pInputText [(ngModel)]="diameter" class="w-full"/>
        <p-slider [(ngModel)]="diameter" [step]="0.01" [min]="1" [max]="100"></p-slider>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Falloff</span>
      <div class="col">
        <input type="text" pInputText [(ngModel)]="falloff" class="w-full"/>
        <p-slider [(ngModel)]="falloff" [step]="0.01" [min]="0" [max]="50"></p-slider>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Random (Falloff)</span>
      <div class="col">
        <input type="text" pInputText [(ngModel)]="random" class="w-full"/>
        <p-slider [(ngModel)]="random" [step]="0.01" [min]="0" [max]="5"></p-slider>
      </div>
    </div>

    <div class="field grid align-items-center">
      <span class="col">Up or down</span>
      <div class="col">
        <p-selectButton [options]="upDownOptions" [(ngModel)]="upDownMode" optionLabel="label"
                        optionValue="value"></p-selectButton>
      </div>
    </div>
  `
})
export class FixHeightBrushComponent extends AbstractBrush {
  height: number = 1;
  diameter: number = 10;
  falloff: number = 0;
  random: number = 0;
  upDownOptions: any = [
    { value: UpDownMode.UP, label: " Up " },
    { value: UpDownMode.DOWN, label: "Down" },
    { value: UpDownMode.OFF, label: "Off" }];
  upDownMode: any = UpDownMode.UP;

  constructor() {
    super();
  }

  calculateHeight(mousePosition: Vector3, oldPosition: Vector3, avgHeight: number | undefined): number | null {
    const radius = this.diameter / 2.0;
    let distance = Vector2.Distance(new Vector2(oldPosition.x, oldPosition.z), new Vector2(mousePosition.x, mousePosition.z));
    if (distance < (radius + this.falloff)) {
      let newHeight: number | null = null;
      if (distance <= radius) {
        newHeight = this.height;
      } else {
        const newValue = (this.height / this.falloff) * (this.falloff + radius - distance) + this.random * (Math.random() - 0.5) * 2.0;
        if (this.upDownMode === UpDownMode.DOWN) {
          if (oldPosition.y > newValue) {
            newHeight = newValue;
          }
          if (oldPosition.y < this.height) {
            newHeight = this.height;
          }
        } else if (this.upDownMode === UpDownMode.UP) {
          if (oldPosition.y < newValue) {
            newHeight = newValue;
          }
          if (oldPosition.y > this.height) {
            newHeight = this.height;
          }
        } else {
          newHeight = newValue;
        }
      }
      // TODO if (vP.y < this._minY) {
      //   vP.y = this._minY;
      // } else if (vP.y > this._maxY) {
      //   vP.y = this._maxY;
      // }
      return newHeight
    } else {
      return null;
    }
  }

}
