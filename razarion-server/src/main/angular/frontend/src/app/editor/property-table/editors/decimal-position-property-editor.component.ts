import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'decimal-position-property-editor',
  template: '<input type="text" pInputText [ngModel]="angularTreeNodeData.value"/>'
})
export class DecimalPositionPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;
}
