import {Component, EventEmitter, Input, Output} from "@angular/core";
import {Vector3} from "three/src/math/Vector3";

@Component({
  selector: 'vertex-editor',
  template: `
    <div class="inline-flex">
      <div class="mr-2">
        <p-inputNumber [ngModel]="vertex.x" prefix="x: " [minFractionDigits]="1"
                       [size]=5 (onInput)="onChangeX($event)"
                       [maxFractionDigits]="6">
        </p-inputNumber>
      </div>
      <div class="mr-2">
        <p-inputNumber [ngModel]="vertex.y" prefix="y: " [minFractionDigits]="1"
                       [size]=5 (onInput)="onChangeY($event)"
                       [maxFractionDigits]="6">
        </p-inputNumber>
      </div>
      <div class="mr-2">
        <p-inputNumber [ngModel]="vertex.z" prefix="z: " [minFractionDigits]="1"
                       [size]=5 (onInput)="onChangeZ($event)"
                       [maxFractionDigits]="6">
        </p-inputNumber>
      </div>
    </div>
  `
})
export class VertexEditorComponent {
  @Input()
  vertex!: Vector3;
  @Output()
  onInput = new EventEmitter<Vector3>();

  onChangeX(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.vertex.x = event.value;
    this.updateModel();
  }

  onChangeY(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.vertex.y = event.value;
    this.updateModel();
  }

  onChangeZ(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.vertex.z = event.value;
    this.updateModel();
  }

  private updateModel() {
    this.onInput.emit(this.vertex);
  }
}
