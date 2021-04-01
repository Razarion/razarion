import {Component} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'i18n-string-property-editor',
  template: '<input type="number" step="1" pInputText [ngModel]="angularTreeNodeData.value"/>'
})
export class I18nStringPropertyEditorComponent {
  angularTreeNodeData!: AngularTreeNodeData;
}
