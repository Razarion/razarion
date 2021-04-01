import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'index-property-editor',
  template: '<input type="text" pInputText [ngModel]="angularTreeNodeData.value"/>'
})
export class IndexPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;
}
