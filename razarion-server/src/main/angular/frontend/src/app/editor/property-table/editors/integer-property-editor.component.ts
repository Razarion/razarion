import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'integer-property-editor',
  template: '<p-inputNumber [ngModel]="angularTreeNodeData.value" (onInput)="onchange($event)"></p-inputNumber>'
})
export class IntegerPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;

  onchange(value: any) {
    this.angularTreeNodeData.setValue(value.value);
  }
}