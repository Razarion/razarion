import {DecimalPosition, TerrainSlopeCorner, TerrainSlopePosition} from "../generated/razarion-share";
import * as turf from "@turf/turf";
import {Feature, Polygon} from "@turf/turf";

import {HoverContext} from "./hover-context";
import {Controls, Corner} from "./controls";
import {Driveway} from "./driveway";
import {Vector2} from "@babylonjs/core";
import {SlopeTerrainEditorComponent} from "../editor/terrain-editor/slope-terrain-editor.component";
import {Mode} from "./terrain-editor";
import {Geometry} from "../common/geometry";

export class Slope {
  private readonly _terrainSlopePosition: TerrainSlopePosition;
  private children: Slope[] = [];
  private polygon!: Feature<Polygon, any>;
  private driveways: Driveway[] = [];

  constructor(terrainSlopePosition: TerrainSlopePosition) {
    this._terrainSlopePosition = terrainSlopePosition;

    this.setupPolygonAndDriveway(terrainSlopePosition);

    if (terrainSlopePosition.children) {
      terrainSlopePosition.children.forEach(child => {
        this.children.push(new Slope(child))
      })
    }
  }

  private setupPolygonAndDriveway(terrainSlopePosition: TerrainSlopePosition) {
    if (!terrainSlopePosition.polygon.length) {
      return <any>undefined;
    }

    let activeDriveway: Driveway | undefined = undefined;

    let points: any = [];
    for (let i = 0; i < terrainSlopePosition.polygon.length; i++) {
      const terrainSlopeCorner = terrainSlopePosition.polygon[i];
      points.push([terrainSlopeCorner.position.x, terrainSlopeCorner.position.y]);
      if (terrainSlopeCorner.slopeDrivewayId) {
        if (!activeDriveway) {
          activeDriveway = new Driveway(terrainSlopeCorner.slopeDrivewayId, this);
          this.driveways.push(activeDriveway);
        }
        activeDriveway.addIndex(i);
      } else {
        activeDriveway = undefined;
      }
    }
    points.push([points[0][0], points[0][1]])

    this.polygon = turf.polygon([
      points
    ]);
  }

  draw(ctx: CanvasRenderingContext2D, controls: Controls, hoverContext: HoverContext | undefined, mode: Mode) {
    if (controls.selectedSlope === this) {
      ctx.fillStyle = "#adad00";
    } else if (hoverContext?.getIntersectSlope() === this) {
      ctx.fillStyle = "blue";
    } else {
      ctx.fillStyle = "green";
    }

    ctx.beginPath();
    ctx.moveTo(this.polygon.geometry.coordinates[0][0][0], this.polygon.geometry.coordinates[0][0][1]);
    for (let i = 1; i < this.polygon.geometry.coordinates[0].length - 1; i++) {
      ctx.lineTo(this.polygon.geometry.coordinates[0][i][0], this.polygon.geometry.coordinates[0][i][1])
    }
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    this.children.forEach(child => child.draw(ctx, controls, hoverContext, mode))

    this.driveways.forEach(driveway => driveway.draw(ctx, controls));

    if ((mode === Mode.CORNER_DELETE || mode === Mode.CORNER_MOVE)
      && hoverContext?.getIntersectSlope() === this && hoverContext?.getIntersectCornerIndex() !== undefined) {
      ctx.save();
      ctx.beginPath();
      ctx.arc(
        this.polygon.geometry.coordinates[0][hoverContext!.getIntersectCornerIndex()!][0],
        this.polygon.geometry.coordinates[0][hoverContext!.getIntersectCornerIndex()!][1],
        5,
        0,
        2 * Math.PI,
        false);
      ctx.fillStyle = "#fd6d14";
      ctx.fill();
      ctx.closePath();
      ctx.restore();
    }

    if (mode === Mode.SELECT) {
      if (controls.selectedSlope === this && controls.selectedCorner !== undefined) {
        ctx.save();
        ctx.beginPath();
        ctx.arc(
          controls.selectedCorner.x,
          controls.selectedCorner.y,
          5,
          0,
          2 * Math.PI,
          false);
        ctx.fillStyle = "#8d5734";
        ctx.fill();
        ctx.closePath();
        ctx.restore();
      } else if (hoverContext?.getIntersectSlope() === this && hoverContext?.getIntersectCornerIndex() !== undefined) {
        ctx.save();
        ctx.beginPath();
        ctx.arc(
          this.polygon.geometry.coordinates[0][hoverContext!.getIntersectCornerIndex()!][0],
          this.polygon.geometry.coordinates[0][hoverContext!.getIntersectCornerIndex()!][1],
          5,
          0,
          2 * Math.PI,
          false);
        ctx.fillStyle = "#346e8d";
        ctx.fill();
        ctx.closePath();
        ctx.restore();
      }
    }
  }

  detectHover(cursorPolygon: Feature<Polygon, any>, cursorPosition: DecimalPosition, hoverContext: HoverContext) {
    if (turf.intersect(this.polygon, cursorPolygon)) {
      if (turf.booleanWithin(cursorPolygon, this.polygon)) {
        hoverContext.setInsideOf(this);
      } else {
        hoverContext.setIntersectSlope(this);
        hoverContext.setIntersectDriveway(this.detectHoverDriveway(cursorPolygon, this));
        hoverContext.setIntersectCornerIndex(this.detectIntersectCornerIndex(cursorPosition))
      }

      this.children.forEach(child => child.detectHover(cursorPolygon, cursorPosition, hoverContext));
    }
  }

  append(polygon: Feature<Polygon, any>) {
    this.polygon = <any>turf.union(polygon, this.polygon);
  }

  remove(polygon: Feature<Polygon, any>) {
    let newPolygon = <any>turf.difference(this.polygon, polygon);
    if (newPolygon && newPolygon.geometry.coordinates.length === 1) {
      this.polygon = newPolygon;
    }
  }

  appendDriveway(polygon: Feature<Polygon, any>, controls: Controls) {
    let driveway = this.findDriveway(polygon);
    if (driveway) {
      driveway.append(polygon);
    } else {
      let newDriveway = new Driveway(controls.newDrivewayConfigId!, this);
      newDriveway.append(polygon);
      this.driveways.push(newDriveway);
    }
  }

  removeDriveway(polygon: Feature<Polygon, any>) {
    let driveway = this.findDriveway(polygon);
    if (driveway) {
      driveway.remove(polygon);
      if (driveway.isEmpty()) {
        this.driveways.splice(this.driveways.indexOf(driveway), 1);
      }
    }
  }

  generateTerrainSlopePosition(): TerrainSlopePosition {
    this._terrainSlopePosition.polygon = this.generateTerrainSlopeCorners();
    return this._terrainSlopePosition;
  }

  private generateTerrainSlopeCorners(): TerrainSlopeCorner[] {
    let terrainSlopeCorners: TerrainSlopeCorner[] = [];
    for (let index = 0; index < this.polygon.geometry.coordinates[0].length - 1; index++) {
      const position = this.polygon.geometry.coordinates[0][index];
      let slopeDrivewayId = this.findDrivewayId(index);

      terrainSlopeCorners.push(new class implements TerrainSlopeCorner {
        position: DecimalPosition = new class implements DecimalPosition {
          x: number = position[0];
          y: number = position[1];
        };
        slopeDrivewayId = slopeDrivewayId;
      })
    }
    return terrainSlopeCorners;
  }

  private findDrivewayId(index: number): number | null {
    let driveway = this.driveways.find(driveway => driveway.containsIndex(index));
    return driveway ? driveway.drivewayConfigId : null;
  }

  createNew(polygon: Feature<Polygon, any>) {
    this.polygon = turf.clone(polygon);
  }

  get terrainSlopePosition(): TerrainSlopePosition {
    return this._terrainSlopePosition;
  }

  addChild(slope: Slope) {
    this.children.push(slope);
  }

  getPolygon(): Feature<Polygon, any> | undefined {
    return this.polygon;
  }

  findParentSlope(slope: Slope): Slope | undefined {
    for (const childSlope of this.children) {
      if (childSlope === slope) {
        return this;
      }
      let found = childSlope.findParentSlope(slope);
      if (found) {
        return found;
      }
    }
    return undefined;
  }

  getChildren(): Slope[] {
    return this.children;
  }

  private findDriveway(polygon: Feature<Polygon, any>): Driveway | undefined {
    return this.driveways.find(driveway => driveway.drivewayIntersection(polygon));
  }

  getDrivewayCount(): number {
    return this.driveways.length;
  }

  private detectHoverDriveway(cursorPolygon: Feature<Polygon, any>, slope: Slope): Driveway | undefined {
    return slope.driveways.find(driveway => driveway.drivewayIntersection(cursorPolygon));
  }

  addCorner(position: DecimalPosition) {
    let index = this.projectPointToPolygon(position);
    if (!index && index !== 0) {
      throw new Error("Invalid Polygon");
    }
    this.polygon.geometry.coordinates[0].splice(index, 0, [position.x, position.y]);
  }

  private projectPointToPolygon(point: { x: number; y: number }): number | null {
    let closestPoint = null;
    let closestDistanceSquared = Infinity;

    let index = null;

    for (let i = 0; i < this.polygon.geometry.coordinates[0].length - 1; i++) {
      const currentLineStart: { x: number; y: number } = {
        x: this.polygon.geometry.coordinates[0][i][0],
        y: this.polygon.geometry.coordinates[0][i][1]
      };
      const currentLineEnd: { x: number; y: number } = {
        x: this.polygon.geometry.coordinates[0][i + 1][0],
        y: this.polygon.geometry.coordinates[0][i + 1][1]
      };
      const projectedPoint = this.projectPointOnLine(point, currentLineStart, currentLineEnd);

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
    return SlopeTerrainEditorComponent.getCorrectedIndex(index + 1, this.polygon.geometry.coordinates[0].length - 1);
  }

  private projectPointOnLine(point: { x: number; y: number }, lineStart: { x: number; y: number }, lineEnd: {
    x: number;
    y: number
  }): { x: number; y: number } {
    const line = [lineEnd.x - lineStart.x, lineEnd.y - lineStart.y];
    const lineLengthSquared = line[0] * line[0] + line[1] * line[1];

    if (lineLengthSquared === 0) {
      return lineStart;
    }

    const t = ((point.x - lineStart.x) * line[0] + (point.y - lineStart.y) * line[1]) / lineLengthSquared;
    let x = lineStart.x + t * line[0];
    let y = lineStart.y + t * line[1];
    x = SlopeTerrainEditorComponent.clamp(x, Math.min(lineStart.x, lineEnd.x), Math.max(lineStart.x, lineEnd.x))
    y = SlopeTerrainEditorComponent.clamp(y, Math.min(lineStart.y, lineEnd.y), Math.max(lineStart.y, lineEnd.y))
    return new Vector2(x, y);
  }

  private detectIntersectCornerIndex(position: DecimalPosition): number | undefined {
    let minDistance = Infinity;
    let indexFound;
    this.polygon.geometry.coordinates[0].forEach((corner, index) => {
      let distance = Geometry.pythagoras(position.x - corner[0], position.y - corner[1]);
      if (distance < minDistance) {
        minDistance = distance;
        indexFound = index
      }
    });
    return indexFound
  }

  moveCorner(index: number, position: DecimalPosition) {
    this.polygon.geometry.coordinates[0][index] = [position.x, position.y];
  }

  removeCorner(index: number) {
    this.polygon.geometry.coordinates[0].splice(index, 1);
  }

  createIntersectCorner(index: number | undefined, changeHandler: (slope: Slope) => void): Corner | undefined {
    if (index === undefined) {
      return undefined;
    }
    let position = this.polygon.geometry.coordinates[0][index];
    let slope = this;
    return new class implements Corner {
      get x(): number {
        return position[0];
      }

      set x(value: number) {
        position[0] = value;
        changeHandler(slope);
      }

      get y(): number {
        return position[1];
      }

      set y(value: number) {
        position[1] = value;
        changeHandler(slope);
      }
    }
  }
}

