import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'integer-map-property-editor',
  template: '<input type="text" pInputText [ngModel]="angularTreeNodeData.value"/>'
})
export class IntegerMapPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;
}
