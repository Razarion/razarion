import {Feature, Polygon} from "@turf/turf";
import {TerrainSlopePosition} from "../generated/razarion-share";
import {SaveContext} from "./save-context";
import {Slope} from "./slope";
import {SelectionContext} from "./selection-context";
import {Controls} from "./controls";

export class SlopeContainer {
  private slopes: Slope[] = [];
  private selectionContext?: SelectionContext;
  private saveContext = new SaveContext();

  constructor() {
  }

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

  manipulate(controls: Controls, cursorPolygon?: Feature<Polygon, any>) {
    if (!cursorPolygon) {
      return;
    }

    if (this.selectionContext?.getIntersect() && !this.selectionContext?.getInsideOf()) {
      this.selectionContext!.getIntersect()!.append(cursorPolygon);
      this.saveContext.onManipulated(this.selectionContext!.getIntersect()!);
    } else {
      let parentId = this.selectionContext?.getInsideOf()?.terrainSlopePosition.id
      let terrainSlopePosition =new class implements TerrainSlopePosition {
        children=[];
        editorParentIdIfCreated= <any>null;
        id= <any>parentId;
        inverted=false;
        polygon=[];
        slopeConfigId= controls.newSlopeConfigId!;
      };
      let slope = new Slope(terrainSlopePosition);
      slope.createNew(cursorPolygon!);
      this.slopes.push(slope);
      this.selectionContext = new SelectionContext();
      this.selectionContext.setIntersect(slope)
      this.saveContext.onCreated(slope);

      this.selectionContext?.getInsideOf() && this.selectionContext?.getInsideOf()?.addChild(slope);
    }
  }

  getSaveContext(): SaveContext {
    return this.saveContext;
  }

  getSelectionContext(): SelectionContext | undefined {
    return this.selectionContext;
  }
}
