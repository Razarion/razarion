import {Component, Input, OnInit, SimpleChanges} from "@angular/core";
import {MathUtils} from "three";
import {Euler} from "three/src/math/Euler";

@Component({
  selector: 'angle-vertex-editor',
  template: `
    <div class="inline-flex">
      <div class="mr-2">
        <p-inputNumber [ngModel]="x" prefix="x: " [minFractionDigits]="1"
                       [size]=5 (onInput)="onChangeX($event)"
                       [maxFractionDigits]="6">
        </p-inputNumber>
      </div>
      <div class="mr-2">
        <p-inputNumber [ngModel]="y" prefix="y: " [minFractionDigits]="1"
                       [size]=5 (onInput)="onChangeY($event)"
                       [maxFractionDigits]="6">
        </p-inputNumber>
      </div>
      <div class="mr-2">
        <p-inputNumber [ngModel]="z" prefix="z: " [minFractionDigits]="1"
                       [size]=5 (onInput)="onChangeZ($event)"
                       [maxFractionDigits]="6">
        </p-inputNumber>
      </div>
    </div>
  `
})
export class AngleVertexEditorComponent implements OnInit {
  @Input()
  euler!: Euler;

  x!: number;
  y!: number;
  z!: number;

  ngOnInit(): void {
    if (this.euler) {
      this.update();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    this.update();
  }

  onChangeX(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.euler.x = MathUtils.degToRad(event.value);
    this.updateModel();
  }

  onChangeY(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.euler.y = MathUtils.degToRad(event.value);
    this.updateModel();
  }

  onChangeZ(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.euler.z = MathUtils.degToRad(event.value);
    this.updateModel();
  }

  private update(): void {
    this.x = MathUtils.radToDeg(this.euler.x);
    this.y = MathUtils.radToDeg(this.euler.y);
    this.z = MathUtils.radToDeg(this.euler.z);
  }

  private updateModel() {
    // if (this.x != undefined && this.y != undefined && this.z != undefined) {
    //   this.euler.vertex = GwtInstance.newVertex(
    //     MathUtils.degToRad(this.x),
    //     MathUtils.degToRad(this.y),
    //     MathUtils.degToRad(this.z));
    // }
  }
}
