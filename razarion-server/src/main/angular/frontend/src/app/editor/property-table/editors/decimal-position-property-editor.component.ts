import {Component, OnInit} from '@angular/core';
import {AngularTreeNodeData} from "../../../gwtangular/GwtAngularFacade";

@Component({
  selector: 'decimal-position-property-editor',
  template: `
    <div class="p-d-inline-flex">
      <div class="p-mr-2">
        <p-inputNumber [ngModel]="x" prefix="x: " [minFractionDigits]="1"
                       [size]=5
                       [maxFractionDigits]="6" (onInput)="onChangeX($event)">
        </p-inputNumber>
      </div>
      <div class="p-mr-2">
        <p-inputNumber [ngModel]="y" prefix="y: " [minFractionDigits]="1"
                       [size]=5
                       [maxFractionDigits]="6" (onInput)="onChangeY($event)">
        </p-inputNumber>
      </div>
    </div>`
})
export class DecimalPositionPropertyEditorComponent implements OnInit {
  angularTreeNodeData!: AngularTreeNodeData;
  x: any;
  y: any;

  ngOnInit(): void {
    if (this.angularTreeNodeData.value != undefined) {
      this.x = this.angularTreeNodeData.value.x;
      this.y = this.angularTreeNodeData.value.y;
    }
  }

  onChangeX(event: any) {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.x = event.value;
    this.updateModel();
  }

  onChangeY(event: any) {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.y = event.value;
    this.updateModel();
  }

  private updateModel() {
    if (this.x != undefined && this.y != undefined) {
      this.angularTreeNodeData.setValue({x: this.x, y: this.y});
    } else {
      this.angularTreeNodeData.setValue(null);
    }
  }
}
