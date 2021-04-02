import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'string-property-editor',
  template: '<input type="text" pInputText [ngModel]="angularTreeNodeData.value" (ngModelChange)="onchange($event)"/>'
})
export class StringPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;

  onchange(value: string) {
    this.angularTreeNodeData.setValue(value);
  }
}
