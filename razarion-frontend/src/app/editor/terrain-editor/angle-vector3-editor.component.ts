import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {Tools, Vector3} from "@babylonjs/core";
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'angle-vertex3-editor',
  imports: [
    InputNumber,
    FormsModule
  ],
  template: `
    <div class="inline-flex">
      <div class="mr-2">
        <p-inputNumber [ngModel]="x" (onInput)="onChangeX($event)"
                       [prefix]="'x: '" [size]="'small'"
                       [minFractionDigits]="1" [maxFractionDigits]="6"
                       [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                       [showButtons]="true"></p-inputNumber>
      </div>
      <div class="mr-2">
        <p-inputNumber [ngModel]="y" (onInput)="onChangeY($event)"
                       [prefix]="'y: '" [size]="'small'"
                       [minFractionDigits]="1" [maxFractionDigits]="6"
                       [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                       [showButtons]="true"></p-inputNumber>
      </div>
      <div class="mr-2">
        <p-inputNumber [ngModel]="z" (onInput)="onChangeZ($event)"
                       [prefix]="'z: '" [size]="'small'"
                       [minFractionDigits]="1" [maxFractionDigits]="6"
                       [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                       [showButtons]="true"></p-inputNumber>
      </div>
    </div>
  `
})
export class AngleVector3EditorComponent implements OnInit {
  @Output()
  onInput = new EventEmitter<Vector3>();
  private _euler: Vector3 | null = null;

  x: number | null = null;
  y: number | null = null;
  z: number | null = null;

  @Input() set euler(value: Vector3 | null) {
    this._euler = value;
    if (this._euler) {
      this.update();
    } else {
      this.x = null;
      this.y = null;
      this.z = null;
    }
  }


  ngOnInit(): void {
    if (this._euler) {
      this.update();
    }
  }

  onChangeX(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this._euler!.x = Tools.ToRadians(event.value);
    this.updateModel();
  }

  onChangeY(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this._euler!.y = Tools.ToRadians(event.value);
    this.updateModel();
  }

  onChangeZ(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this._euler!.z = Tools.ToRadians(event.value);
    this.updateModel();
  }

  private update(): void {
    this.x = Tools.ToDegrees(this._euler!.x);
    this.y = Tools.ToDegrees(this._euler!.y);
    this.z = Tools.ToDegrees(this._euler!.z);
  }

  private updateModel() {
    this.onInput.emit(this._euler!);
  }
}
