import { Component, OnInit } from '@angular/core';
import { AbstractBrush } from './abstract-brush';
import { UpDownMode } from "../shape-terrain-editor.component";

@Component({
  selector: 'fix-height-brush',
  template: `
    <div class="field grid align-items-center">
      <span class="col">Height</span>
      <div class="col">
        <input type="text" pInputText [(ngModel)]="height" class="w-full"/>
        <p-slider [(ngModel)]="height" [step]="0.01" [min]="-2" [max]="23"></p-slider>
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
        <p-selectButton [options]="upDownOptions" [(ngModel)]="upDownValue" optionLabel="label"
                        optionValue="value"></p-selectButton>
      </div>
    </div>
  `
})
export class FixHeightBrushComponentComponent extends AbstractBrush implements OnInit {
  height: number = 1;
  diameter: number = 10;
  falloff: number = 0;
  random: number = 0;
  upDownOptions: any = [
    { value: UpDownMode.UP, label: " Up " },
    { value: UpDownMode.DOWN, label: "Down" },
    { value: UpDownMode.OFF, label: "Off" }];
  upDownValue: any = UpDownMode.UP;

  constructor() {
    super();
  }

  ngOnInit(): void {
  }

}
