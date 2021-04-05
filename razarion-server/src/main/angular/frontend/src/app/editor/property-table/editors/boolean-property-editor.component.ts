import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'boolean-property-editor',
  template: '<p-inputSwitch [ngModel]="angularTreeNodeData.value" (onChange)="onChange($event)"></p-inputSwitch>'
})
export class BooleanPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;

  onChange(event: any) {
    this.angularTreeNodeData.setValue(event.checked);
  }
}
