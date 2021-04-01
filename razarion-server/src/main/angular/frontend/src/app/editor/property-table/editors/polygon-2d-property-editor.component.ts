import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'polygon-2d-property-editor',
  template: '<input type="number" step="1" pInputText [ngModel]="angularTreeNodeData.value"/>'
})
export class Polygon2dPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;
}
