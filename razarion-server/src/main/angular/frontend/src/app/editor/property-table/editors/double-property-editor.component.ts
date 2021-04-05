import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'double-property-editor',
  template: '<p-inputNumber [ngModel]="angularTreeNodeData.value" (onInput)="onchange($event)" [minFractionDigits]="2" [maxFractionDigits]="5"></p-inputNumber>'
})
export class DoublePropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;

  onchange(value: any) {
    this.angularTreeNodeData.setValue(value.value);
  }
}
