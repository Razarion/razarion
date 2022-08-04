import {Component, Input, OnInit} from "@angular/core";
import {Vertex} from "../../gwtangular/GwtAngularFacade";
import {GwtInstance} from "../../gwtangular/GwtInstance";
import {MathUtils} from "three";

export class VertexHolder {
  constructor(vertex: Vertex) {
    this._vertex = vertex;
  }

  private _vertex: Vertex;

  get vertex(): Vertex {
    return this._vertex;
  }

  set vertex(value: Vertex) {
    this._vertex = value;
  }
}

@Component({
  selector: 'angle-3-editor',
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
export class Angle3EditorComponent implements OnInit {
  @Input()
  vertexHolder!: VertexHolder;

  x!: number;
  y!: number;
  z!: number;

  ngOnInit(): void {
    if (this.vertexHolder != undefined) {
      this.x = MathUtils.radToDeg(this.vertexHolder.vertex.getX());
      this.y = MathUtils.radToDeg(this.vertexHolder.vertex.getY());
      this.z = MathUtils.radToDeg(this.vertexHolder.vertex.getZ());
    }
  }

  onChangeX(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.x = event.value;
    this.updateModel();
  }

  onChangeY(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.y = event.value;
    this.updateModel();
  }

  onChangeZ(event: any): void {
    if (event.value != null && typeof event.value !== "number") {
      return;
    }
    this.z = event.value;
    this.updateModel();
  }

  private updateModel() {
    if (this.x != undefined && this.y != undefined && this.z != undefined) {
      this.vertexHolder.vertex = GwtInstance.newVertex(
        MathUtils.degToRad(this.x),
        MathUtils.degToRad(this.y),
        MathUtils.degToRad(this.z));
    }
  }
}
