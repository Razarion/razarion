import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'polygon-2d-property-editor',
  template: '<polygon-2d-editor [polygon]="angularTreeNodeData.value" (change)="angularTreeNodeData.setValue($event)"></polygon-2d-editor>'
})
export class Polygon2dPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;

}
