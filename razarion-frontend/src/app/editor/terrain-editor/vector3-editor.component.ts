import {Component, EventEmitter, Input, Output} from "@angular/core";
import {Vector3} from "@babylonjs/core";
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'vector3-editor',
  imports: [
    InputNumber,
    FormsModule
  ],
  template: `
    <div class="inline-flex">
      <div class="mr-2">
        <p-inputNumber [ngModel]="vector3?.x" (onInput)="onChangeX($event)"
                       [prefix]="'x: '" [size]="'small'"
                       [minFractionDigits]="1" [maxFractionDigits]="6"
                       [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                       [showButtons]="true"></p-inputNumber>
      </div>
      <div class="mr-2">
        <p-inputNumber [ngModel]="vector3?.y" (onInput)="onChangeY($event)"
                       [prefix]="'y: '" [size]="'small'"
                       [minFractionDigits]="1" [maxFractionDigits]="6"
                       [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                       [showButtons]="true"></p-inputNumber>
      </div>
      <div class="mr-2">
        <p-inputNumber [ngModel]="vector3?.z" (onInput)="onChangeZ($event)"
                       [prefix]="'z: '" [size]="'small'"
                       [minFractionDigits]="1" [maxFractionDigits]="6"
                       [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                       [showButtons]="true"></p-inputNumber>
      </div>
    </div>
  `
})
export class Vector3EditorComponent {
  @Input()
  vector3: Vector3 | null = null;
  @Output()
  onInput = new EventEmitter<Vector3>();

  onChangeX(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.vector3!.x = event.value;
    this.updateModel();
  }

  onChangeY(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.vector3!.y = event.value;
    this.updateModel();
  }

  onChangeZ(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.vector3!.z = event.value;
    this.updateModel();
  }

  private updateModel() {
    this.onInput.emit(this.vector3!);
  }
}
