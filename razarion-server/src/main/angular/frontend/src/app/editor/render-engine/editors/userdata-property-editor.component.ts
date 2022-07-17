import {Component, OnInit} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'userdata-property-editor',
  template: `
    <small *ngIf="invalidClass" class="p-error block">JSON Invalid</small>
    <textarea pInputTextarea [(ngModel)]="userDataJson" (input)="onChange()" [class]="invalidClass"></textarea>
  `
})
export class UserdataPropertyEditorComponent implements OnInit {
  angularTreeNodeData!: AngularTreeNodeData;

  userDataJson: string = '';
  invalidClass: string = '';

  ngOnInit(): void {
    if (this.angularTreeNodeData.value) {
      this.userDataJson = JSON.stringify(this.angularTreeNodeData.value)
    } else {
      this.userDataJson = "{}";
    }
  }

  onChange() {
    try {
      this.angularTreeNodeData.setValue(JSON.parse(this.userDataJson))
      this.invalidClass = '';
    } catch (error) {
      this.invalidClass = 'ng-invalid ng-dirty';
    }
  }
}
