import {Component, EventEmitter, Input, Output} from "@angular/core";
import {Vector3} from "three/src/math/Vector3";

@Component({
  selector: 'vertex-editor',
  template: `
    <div class="inline-flex">
      <div class="mr-2">
        <p-inputNumber [ngModel]="vertex.x" (onInput)="onChangeX($event)"
                       [prefix]="'x: '" [size]="5"
                       [minFractionDigits]="1" [maxFractionDigits]="6"
                       [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                       [showButtons]="true"></p-inputNumber>
      </div>
      <div class="mr-2">
        <p-inputNumber [ngModel]="vertex.y" (onInput)="onChangeY($event)"
                       [prefix]="'y: '" [size]="5"
                       [minFractionDigits]="1" [maxFractionDigits]="6"
                       [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                       [showButtons]="true"></p-inputNumber>
      </div>
      <div class="mr-2">
        <p-inputNumber [ngModel]="vertex.z" (onInput)="onChangeZ($event)"
                       [prefix]="'z: '" [size]="5"
                       [minFractionDigits]="1" [maxFractionDigits]="6"
                       [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                       [showButtons]="true"></p-inputNumber>
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
