import {SelectionContext, Slope} from "./model";
import {Feature, Polygon} from "@turf/turf";
import {TerrainSlopePosition} from "../generated/razarion-share";

export class SlopeContainer {
  private slopes: Slope[] = [];
  private selectionContext?: SelectionContext;

  setTerrainSlopePositions(terrainSlopePositions: TerrainSlopePosition[]) {
    terrainSlopePositions.forEach(terrainSlopePosition => {
      this.slopes.push(new Slope(terrainSlopePosition));
    });
  }

  draw(ctx: CanvasRenderingContext2D) {
    this.slopes.forEach(slopes => {
      slopes.draw(ctx);
    });
  }

  recalculateSelection(cursorPolygon: Feature<Polygon, any> | undefined) {
    this.selectionContext = new SelectionContext();
    if (!cursorPolygon) {
      return;
    }
    this.slopes.forEach(slope => {
      slope.recalculateSelection(cursorPolygon, this.selectionContext!);
    });

  }

  manipulate(cursorPolygon?: Feature<Polygon, any>) {
    if (!cursorPolygon) {
      return;
    }

    if (!this.selectionContext?.valid()) {
      return;
    }
    this.selectionContext.getSelectedSlope().adjoin(cursorPolygon);
  }
}