import {Component, OnInit} from '@angular/core';
import {MathUtils} from "three";

@Component({
  selector: 'vector3-property-editor',
  template: `
    X
    <p-inputNumber [(ngModel)]="angularTreeNodeData.value.x" [size]="1" [maxlength]="3"
                   [showButtons]="true"></p-inputNumber>
    Y
    <p-inputNumber [(ngModel)]="angularTreeNodeData.value.y" [size]="1" [maxlength]="3"
                   [showButtons]="true"></p-inputNumber>
    Z
    <p-inputNumber [(ngModel)]="angularTreeNodeData.value.z" [size]="1" [maxlength]="3"
                   [showButtons]="true"></p-inputNumber>
  `
})
export class Vector3PropertyEditorComponent {
  angularTreeNodeData: any;
}
