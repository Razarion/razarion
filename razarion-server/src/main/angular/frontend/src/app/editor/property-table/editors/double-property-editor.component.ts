import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'double-property-editor',
  template: '<p-inputNumber [ngModel]="angularTreeNodeData.value" (onInput)="onchange($event)" [minFractionDigits]="2" [maxFractionDigits]="5"></p-inputNumber>'
})
export class DoublePropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;

  onchange(event: any) {
    if(typeof event.value !== "number") {
      return;
    }
    this.angularTreeNodeData.setValue(event.value);
  }
}
