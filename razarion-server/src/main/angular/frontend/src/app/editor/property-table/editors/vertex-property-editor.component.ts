import {Component, OnInit} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'vertex-property-editor',
  template: `
    X
    <p-inputNumber [ngModel]="x" [size]="1" [maxlength]="3"
                   [showButtons]="true" (onInput)="onChangeX($event.value)"></p-inputNumber>
    Y
    <p-inputNumber [ngModel]="y" [size]="1" [maxlength]="3"
                   [showButtons]="true" (onInput)="onChangeY($event.value)"></p-inputNumber>
    Z
    <p-inputNumber [ngModel]="z" [size]="1" [maxlength]="3"
                   [showButtons]="true" (onInput)="onChangeZ($event.value)"></p-inputNumber>
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
