import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Vertex } from 'src/app/generated/razarion-share';

@Component({
  selector: 'vertex-editor',
  templateUrl: './vertex-editor.component.html'
})
export class VertexEditorComponent implements OnInit {
  @Input("vertex")
  vertex: Vertex | null = null;
  @Output()
  vertexChange = new EventEmitter<Vertex | null>();
  @Input("readOnly")
  readOnly: boolean = false;

  x?: number
  y?: number
  z?: number

  constructor() { }

  ngOnInit(): void {
    if (this.vertex) {
      this.x = this.vertex.x;
      this.y = this.vertex.y;
      this.z = this.vertex.z;
    }
  }

  onX(value: any) {
    this.x = value;
    this.fireDecimalPosition();
  }

  onY(value: any) {
    this.y = value;
    this.fireDecimalPosition();
  }

  onZ(value: any) {
    this.z = value;
    this.fireDecimalPosition();
  }

  private fireDecimalPosition() {
    if ((this.x || this.x === 0) && (this.y || this.y === 0) && (this.z || this.z === 0)) {
      this.vertex = {
        x: this.x!,
        y: this.y!,
        z: this.z!,
      };
    } else {
      this.vertex = null;
    }
    this.vertexChange.emit(this.vertex);
  }

}
