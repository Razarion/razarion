import {DecimalPosition, TerrainSlopeCorner, TerrainSlopePosition} from "../generated/razarion-share";
import * as turf from "@turf/turf";
import {Feature, Polygon} from "@turf/turf";

import {HoverContext} from "./hover-context";
import {Controls} from "./controls";
import {Driveway} from "./driveway";

export class Slope {
  private readonly _terrainSlopePosition: TerrainSlopePosition;
  private children: Slope[] = [];
  private polygon!: Feature<Polygon, any>;
  private driveways: Driveway[] = [];
  private hover = false;

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

  draw(ctx: CanvasRenderingContext2D) {
    if (this.hover) {
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

    this.children.forEach(child => child.draw(ctx))

    this.driveways.forEach(driveway => driveway.draw(ctx));
  }

  detectHover(cursorPolygon: Feature<Polygon, any>, hoverContext: HoverContext) {
    this.hover = false;
    if (turf.intersect(this.polygon, cursorPolygon)) {
      if (turf.booleanWithin(cursorPolygon, this.polygon)) {
        hoverContext.setInsideOf(this);
      } else {
        hoverContext.getIntersectSlope() && hoverContext.getIntersectSlope()!.clearHover();
        hoverContext.setIntersectSlope(this);
        this.hover = true;
      }
      this.children.forEach(child => child.detectHover(cursorPolygon, hoverContext));
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
    return driveway ? driveway.drivewayId : null;
  }

  createNew(polygon: Feature<Polygon, any>) {
    this.polygon = turf.clone(polygon);
    this.hover = true;
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

  clearHover() {
    this.hover = false;
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
}

