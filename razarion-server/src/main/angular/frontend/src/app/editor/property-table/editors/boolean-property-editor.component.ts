import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'boolean-property-editor',
  template: `
    <p-checkbox [ngModel]="angularTreeNodeData.value"
                (onChange)="onChange($event)"
                [binary]="true">
    </p-checkbox>
  `
})
export class BooleanPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;

  onChange(event: any) {
    this.angularTreeNodeData.setValue(event.checked);
  }
}
