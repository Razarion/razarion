import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'integer-property-editor',
  template: '<input type="number" step="1" pInputText [ngModel]="angularTreeNodeData.value"/>'
})
export class IntegerPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;
}
