import {Component, OnInit} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'vertex-property-editor',
  template: `
    <p-inputNumber [ngModel]="x" (onInput)="onChangeX($event.value)"
                   [prefix]="'x: '" [size]="5"
                   [minFractionDigits]="1" [maxFractionDigits]="6"
                   [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                   [showButtons]="true"></p-inputNumber>
    <p-inputNumber [ngModel]="y" (onInput)="onChangeY($event.value)"
                   [prefix]="'y: '" [size]="5"
                   [minFractionDigits]="1" [maxFractionDigits]="6"
                   [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                   [showButtons]="true"></p-inputNumber>
    <p-inputNumber [ngModel]="z" (onInput)="onChangeZ($event.value)"
                   [prefix]="'z: '" [size]="5"
                   [minFractionDigits]="1" [maxFractionDigits]="6"
                   [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                   [showButtons]="true"></p-inputNumber>
  `
})
export class VertexPropertyEditorComponent implements OnInit {
  angularTreeNodeData!: AngularTreeNodeData;
  x: number | undefined
  y: number | undefined
  z: number | undefined

  ngOnInit(): void {
    if (this.angularTreeNodeData.value != undefined) {
      this.x = this.angularTreeNodeData.value.x;
      this.y = this.angularTreeNodeData.value.y;
      this.z = this.angularTreeNodeData.value.z;
    }
  }

  onChangeX(value: number) {
    this.x = value;
    this.updateModel();
  }

  onChangeY(value: number) {
    this.y = value;
    this.updateModel();
  }

  onChangeZ(value: number) {
    this.z = value;
    this.updateModel();
  }

  private updateModel() {
    if (this.x != undefined && this.y != undefined && this.z != undefined) {
      this.angularTreeNodeData.setValue({x: this.x, y: this.y, z: this.z});
    } else {
      this.angularTreeNodeData.setValue(null);
    }
  }
}
