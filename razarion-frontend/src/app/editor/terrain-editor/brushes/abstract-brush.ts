import {Vector3} from "@babylonjs/core";
import {HeightMapTerrainEditorComponent} from "../height-map-terrain-editor.component";

export class BrushContext {
  private heights: number[] = [];
  private avgHeight?: number

  constructor(public readonly brush: AbstractBrush) {
    this.brush = brush;
  }

  isInRadius(mousePosition: Vector3, oldPosition: Vector3): boolean {
    return this.brush.isInRadius(mousePosition, oldPosition);
  }

  addHeight(height: number) {
    this.heights.push(height);
  }

  finishPrepare() {
    let totalHeight = 0;
    this.heights.forEach(height => {
      totalHeight += height;
    });
    this.avgHeight = totalHeight / this.heights.length;
  }

  getAvgHeight(): number {
    return this.avgHeight!;
  }
}

export abstract class AbstractBrush {
  shapeTerrainEditorComponent!: HeightMapTerrainEditorComponent;
  brushContext: BrushContext | null = null;

  abstract calculateHeight(mousePosition: Vector3, oldPosition: Vector3): number | null;

  isInRadius(mousePosition: Vector3, oldPosition: Vector3): boolean {
    return false;
  }

  isContextDependent(): boolean {
    return false;
  }

  setBrushContext(brushContext: BrushContext | null) {
    this.brushContext = brushContext;
  }

  getBrushContext(): BrushContext | null {
    return this.brushContext;
  }


}
