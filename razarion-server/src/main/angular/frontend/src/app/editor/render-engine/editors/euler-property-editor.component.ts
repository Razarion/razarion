import {Component, OnInit} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";
import {MathUtils} from "three";

@Component({
  selector: 'euler-property-editor',
  template: `
    X
    <p-inputNumber [ngModel]="x" [size]="1" [maxlength]="3" (onInput)="onUpdateX($event)"></p-inputNumber>
    Y
    <p-inputNumber [ngModel]="y" [size]="1" [maxlength]="3" (onInput)="onUpdateY($event)"></p-inputNumber>
    Z
    <p-inputNumber [ngModel]="z" [size]="1" [maxlength]="3" (onInput)="onUpdateZ($event)"></p-inputNumber>
  `
})
export class EulerPropertyEditorComponent implements OnInit {
  angularTreeNodeData!: AngularTreeNodeData;

  x: number = 0;
  y: number = 0;
  z: number = 0;

  ngOnInit(): void {
    this.x = MathUtils.radToDeg(this.angularTreeNodeData.value.x);
    this.y = MathUtils.radToDeg(this.angularTreeNodeData.value.y);
    this.z = MathUtils.radToDeg(this.angularTreeNodeData.value.z);
  }

  onUpdateX(event: any) {
    this.angularTreeNodeData.value.x = MathUtils.degToRad(event.value)
  }

  onUpdateY(event: any) {
    this.angularTreeNodeData.value.y = MathUtils.degToRad(event.value)
  }

  onUpdateZ(event: any) {
    this.angularTreeNodeData.value.z = MathUtils.degToRad(event.value)
  }
}
