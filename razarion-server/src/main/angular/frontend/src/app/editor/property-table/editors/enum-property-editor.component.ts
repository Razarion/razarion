import {Component, OnInit} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'enum-property-editor',
  template: '<p-dropdown [options]="options" [(ngModel)]="selected" (onChange)="onChange()" placeholder="&nbsp;" [style]="{width: \'10em\'}"></p-dropdown>\n'
})
export class EnumPropertyEditorComponent implements OnInit {
  angularTreeNodeData!: AngularTreeNodeData;
  options: any[] = [];
  selected: string = '';

  ngOnInit(): void {
    this.angularTreeNodeData.options.forEach(option => this.options.push({
      'label': option,
      'value': option
    }));
    this.selected = this.angularTreeNodeData.value;
  }

  onChange() {
    this.angularTreeNodeData.setValue(this.selected)
  }
}
