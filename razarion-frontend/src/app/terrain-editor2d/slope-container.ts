import {Feature, Polygon} from "@turf/turf";
import {TerrainSlopePosition} from "../generated/razarion-share";
import {SaveContext} from "./save-context";
import {Slope} from "./slope";
import {HoverContext} from "./hover-context";
import {Controls} from "./controls";

export class SlopeContainer {
  private rootSlopes: Slope[] = [];
  private hoverContext?: HoverContext;
  private saveContext = new SaveContext();

  constructor() {
  }

  setTerrainSlopePositions(terrainSlopePositions: TerrainSlopePosition[]) {
    terrainSlopePositions.forEach(terrainSlopePosition => {
      this.rootSlopes.push(new Slope(terrainSlopePosition));
    });
  }

  draw(ctx: CanvasRenderingContext2D) {
    this.rootSlopes.forEach(slopes => {
      slopes.draw(ctx);
    });
  }

  recalculateHoverContext(cursorPolygon: Feature<Polygon, any> | undefined) {
    this.hoverContext = new HoverContext();
    if (!cursorPolygon) {
      return;
    }
    this.rootSlopes.forEach(slope => {
      slope.detectHover(cursorPolygon, this.hoverContext!);
    });
  }

  manipulate(controls: Controls, cursorPolygon?: Feature<Polygon, any>) {
    if (!cursorPolygon) {
      return;
    }

    if (this.hoverContext?.getIntersectSlope() && !this.hoverContext?.getInsideOf()) {
      this.manipulateExisting(cursorPolygon);
    } else {
      this.createNew(controls, cursorPolygon);
    }
  }

  private manipulateExisting(cursorPolygon: Feature<Polygon, any>) {
    this.hoverContext!.getIntersectSlope()!.append(cursorPolygon);
    this.saveContext.onManipulated(this.hoverContext!.getIntersectSlope()!);
  }

  private createNew(controls: Controls, cursorPolygon: Feature<Polygon, any>) {
    let parentId = this.hoverContext?.getInsideOf()?.terrainSlopePosition.id
    let terrainSlopePosition = new class implements TerrainSlopePosition {
      children = [];
      editorParentIdIfCreated = parentId || null;
      id = null;
      inverted = false;
      polygon = [];
      slopeConfigId = controls.newSlopeConfigId!;
    };
    let slope = new Slope(terrainSlopePosition);
    slope.createNew(cursorPolygon!);
    this.hoverContext = new HoverContext();
    this.hoverContext.setIntersectSlope(slope)
    this.saveContext.onCreated(slope);

    if (this.hoverContext?.getInsideOf()) {
      this.hoverContext?.getInsideOf()?.addChild(slope);
    } else {
      this.rootSlopes.push(slope);
    }
  }

  getSaveContext(): SaveContext {
    return this.saveContext;
  }

  getHoverContext(): HoverContext | undefined {
    return this.hoverContext;
  }

  findParentSlope(slope: Slope): Slope | undefined {
    for (const rootSlopes of this.rootSlopes) {
      if (rootSlopes === slope) {
        return undefined;
      }
      let parentSlope = rootSlopes.findParentSlope(slope);
      if (parentSlope) {
        return parentSlope;
      }
    }
    return undefined;
  }

  deleteSlope(deleteSlope: Slope) {
    const index = this.rootSlopes.indexOf(deleteSlope);
    if (index >= 0) {
      this.rootSlopes.splice(index, 1);
    } else {
      let slopes = this.findParentSlope(deleteSlope)!.getChildren();
      slopes.splice(slopes.indexOf(deleteSlope), 1);
    }

    this.saveContext.onDeleted(deleteSlope)
  }

  onChangeSlopeConfigId(slope: Slope) {
    this.saveContext.slopeConfigConfigIdChanged(slope)
  }
}
