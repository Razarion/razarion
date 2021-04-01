import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'enum-property-editor',
  template: '<input type="text" pInputText [ngModel]="angularTreeNodeData.value"/>'
})
export class EnumPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;
}
