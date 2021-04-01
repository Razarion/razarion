import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'rectangle-2d-property-editor',
  template: '<input type="number" step="1" pInputText [ngModel]="angularTreeNodeData.value"/>'
})
export class Rectangle2dPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;
}
