import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from "@angular/core";
import {MathUtils} from "three";
import {Euler} from "three/src/math/Euler";

@Component({
  selector: 'angle-vertex-editor',
  template: `
    <div class="inline-flex">
      <div class="mr-2">
        <p-inputNumber [ngModel]="x" (onInput)="onChangeX($event)"
                       [prefix]="'x: '" [size]="5"
                       [minFractionDigits]="1" [maxFractionDigits]="6"
                       [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                       [showButtons]="true"></p-inputNumber>
      </div>
      <div class="mr-2">
        <p-inputNumber [ngModel]="y" (onInput)="onChangeY($event)"
                       [prefix]="'y: '" [size]="5"
                       [minFractionDigits]="1" [maxFractionDigits]="6"
                       [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                       [showButtons]="true"></p-inputNumber>
      </div>
      <div class="mr-2">
        <p-inputNumber [ngModel]="z" (onInput)="onChangeZ($event)"
                       [prefix]="'z: '" [size]="5"
                       [minFractionDigits]="1" [maxFractionDigits]="6"
                       [incrementButtonClass]="'p-button-text'" [decrementButtonClass]="'p-button-text'"
                       [showButtons]="true"></p-inputNumber>
      </div>
    </div>
  `
})
export class AngleVertexEditorComponent implements OnInit {
  @Input()
  euler!: Euler;
  @Output()
  onInput = new EventEmitter<Euler>();

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
    this.onInput.emit(this.euler);
  }
}
