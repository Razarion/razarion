import {Vector3} from "@babylonjs/core";
import {HeightMapTerrainEditorComponent} from "../height-map-terrain-editor.component";

export interface SpatialHeight {
  x: number;
  z: number;
  height: number;
}

export class BrushContext {
  private heights: number[] = [];
  private spatialHeights: SpatialHeight[] = [];
  private avgHeight?: number;

  constructor(public readonly brush: AbstractBrush) {
    this.brush = brush;
  }

  isInRadius(mousePosition: Vector3, oldPosition: Vector3): boolean {
    return this.brush.isInRadius(mousePosition, oldPosition);
  }

  addHeight(height: number) {
    this.heights.push(height);
  }

  addSpatialHeight(x: number, z: number, height: number) {
    this.spatialHeights.push({x, z, height});
    this.addHeight(height);
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

  getSpatialHeights(): SpatialHeight[] {
    return this.spatialHeights;
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

  /** Returns the effective radius in meters for tile-culling. */
  getEffectiveRadius(): number {
    return 0;
  }

  /** If true, brush applies only on click, not on drag. */
  isStampMode(): boolean {
    return false;
  }

  /** Pre-calculation step called before vertex iteration (e.g. erosion simulation). */
  preCalculate(mousePosition: Vector3): void {
  }

  setBrushContext(brushContext: BrushContext | null) {
    this.brushContext = brushContext;
  }

  getBrushContext(): BrushContext | null {
    return this.brushContext;
  }

  showCursor() {
  }

  hideCursor() {
  }
}
