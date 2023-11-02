import * as turf from '@turf/turf';
import {Feature, Polygon} from '@turf/turf';
import {Controls} from "./controls";

export class Cursor {
  constructor(private controls: Controls) {

  }

  private cursor?: Feature<Polygon, any>;
  private x?: number
  private y?: number

  move(x: number, y: number) {
    this.x = x;
    this.y = y;
    this.moveInner(x, y);
  }

  private moveInner(x: number, y: number) {
    let deltaAngle = 2 * Math.PI / this.controls.cursorCorners;
    let points = [];
    for (let i = 0; i < this.controls.cursorCorners; i++) {
      let angleInRadians = i * deltaAngle + (this.controls.cursorAngleDegree * (Math.PI / 180));
      const newX = x + this.controls.cursorDiameter / 2.0 * Math.cos(angleInRadians);
      const newY = y + this.controls.cursorDiameter / 2.0 * Math.sin(angleInRadians);
      points.push([newX, newY])
    }
    points.push([points[0][0], points[0][1]])

    this.cursor = turf.polygon([
      points
    ]);
  }

  getPolygon(): Feature<Polygon, any> | undefined {
    return this.cursor;
  }

  draw(ctx: CanvasRenderingContext2D) {
    if (!this.cursor) {
      return;
    }
    ctx.beginPath();
    ctx.moveTo(this.cursor.geometry.coordinates[0][0][0], this.cursor.geometry.coordinates[0][0][1]);
    for (let i = 1; i < this.cursor.geometry.coordinates[0].length - 1; i++) {
      ctx.lineTo(this.cursor.geometry.coordinates[0][i][0], this.cursor.geometry.coordinates[0][i][1])
    }
    ctx.closePath();
    ctx.stroke();
  }

  redraw() {
    if (this.x !== undefined && this.y !== undefined) {
      this.moveInner(this.x, this.y);
    }
  }
}
