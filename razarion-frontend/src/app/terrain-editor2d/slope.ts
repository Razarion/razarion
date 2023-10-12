import {DecimalPosition, TerrainSlopeCorner, TerrainSlopePosition} from "../generated/razarion-share";
import * as turf from "@turf/turf";
import {Feature, Polygon} from "@turf/turf";

import {SelectionContext} from "./selection-context";

export class Slope {
  private readonly _terrainSlopePosition: TerrainSlopePosition;
  private children: Slope[] = [];
  private polygon: Feature<Polygon, any>;
  private selected = false;

  constructor(terrainSlopePosition: TerrainSlopePosition) {
    this._terrainSlopePosition = terrainSlopePosition;

    this.polygon = this.createPolygon(terrainSlopePosition);

    if (terrainSlopePosition.children) {
      terrainSlopePosition.children.forEach(child => {
        this.children.push(new Slope(child))
      })
    }
  }

  private createPolygon(terrainSlopePosition: TerrainSlopePosition): Feature<Polygon, any> {
    if (!terrainSlopePosition.polygon.length) {
      return <any>undefined;
    }
    let points: any = [];
    terrainSlopePosition.polygon.forEach(terrainSlopeCorner => {
      points.push([terrainSlopeCorner.position.x, terrainSlopeCorner.position.y])
    })
    points.push([points[0][0], points[0][1]])

    return turf.polygon([
      points
    ]);
  }

  draw(ctx: CanvasRenderingContext2D) {
    if (this.selected) {
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
  }

  recalculateSelection(cursorPolygon: Feature<Polygon, any>, selectionContext: SelectionContext) {
    this.selected = false;
    if (turf.intersect(this.polygon, cursorPolygon)) {
      selectionContext.add(this);
      if (turf.booleanWithin(cursorPolygon, this.polygon)) {
        selectionContext.setInsideOf(this);
      } else {
        this.selected = true;
      }
    }
  }

  adjoin(polygon: Feature<Polygon, any>) {
    this.polygon = <any>turf.union(polygon, this.polygon);
  }

  generateTerrainSlopePosition(): TerrainSlopePosition {
    this._terrainSlopePosition.polygon = this.generateTerrainSlopeCorners();
    return this._terrainSlopePosition;
  }

  private generateTerrainSlopeCorners() {
    let terrainSlopeCorners: TerrainSlopeCorner[] = [];
    for (let i = 0; i < this.polygon.geometry.coordinates[0].length - 1; i++) {
      const position = this.polygon.geometry.coordinates[0][i];
      terrainSlopeCorners.push(new class implements TerrainSlopeCorner {
        position: DecimalPosition = new class implements DecimalPosition {
          x: number = position[0];
          y: number = position[1];
        };
        slopeDrivewayId = <any>null;
      })
    }
    return terrainSlopeCorners;
  }

  createNew(polygon: Feature<Polygon, any>) {
    this.polygon = turf.clone(polygon);
    this.selected = true;
  }


  get terrainSlopePosition(): TerrainSlopePosition {
    return this._terrainSlopePosition;
  }

  addChild(slope: Slope) {
    this.children.push(slope);
  }
}
