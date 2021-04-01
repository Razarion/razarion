import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'image-property-editor',
  template: '<input type="text" pInputText [ngModel]="angularTreeNodeData.value"/>'
})
export class ImagePropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;
}
