import { Vector2 } from "@babylonjs/core";
import { Helpers } from "./helpers";
import { MathUtils } from "./math-utils";
import EAR_CUT from 'earcut';
export class Geometry {


  static pythagoras(x: number, y: number): number {
    return Math.sqrt(x * x + y * y);
  }

  public static projectPointToPolygon(point: Vector2, polygon: Vector2[]): number | null {
    let closestPoint = null;
    let closestDistanceSquared = Infinity;

    let index = null;

    for (let i = 0; i < polygon.length; i++) {
      const currentLineStart = polygon[i];
      const currentLineEnd = Geometry.getArrayCorrectedIndex(i + 1, polygon);

      const projectedPoint = Geometry.projectPointOnLine(point, currentLineStart, currentLineEnd);

      const distanceSquared = (point.x - projectedPoint.x) * (point.x - projectedPoint.x) +
        (point.y - projectedPoint.y) * (point.y - projectedPoint.y);

      if (distanceSquared < closestDistanceSquared) {
        closestPoint = projectedPoint;
        closestDistanceSquared = distanceSquared;
        index = i;
      }
    }

    if (index == null) {
      return null;
    }
    return Helpers.getCorrectedIndex(index + 1, polygon.length);
  }

  private static getArrayCorrectedIndex(index: number, array: any[]): any {
    return array[Helpers.getCorrectedIndex(index, array.length)];
  }

  private static projectPointOnLine(point: Vector2, lineStart: Vector2, lineEnd: Vector2): Vector2 {
    const line = [lineEnd.x - lineStart.x, lineEnd.y - lineStart.y];
    const lineLengthSquared = line[0] * line[0] + line[1] * line[1];

    if (lineLengthSquared === 0) {
      return lineStart;
    }

    const t = ((point.x - lineStart.x) * line[0] + (point.y - lineStart.y) * line[1]) / lineLengthSquared;
    let x = lineStart.x + t * line[0];
    let y = lineStart.y + t * line[1];
    x = MathUtils.clamp(x, Math.min(lineStart.x, lineEnd.x), Math.max(lineStart.x, lineEnd.x))
    y = MathUtils.clamp(y, Math.min(lineStart.y, lineEnd.y), Math.max(lineStart.y, lineEnd.y))
    return new Vector2(x, y);
  }

}
