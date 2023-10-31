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

  manipulateSlope(controls: Controls, increaseMode: boolean, cursorPolygon?: Feature<Polygon, any>) {
    if (!cursorPolygon) {
      return;
    }

    if (this.hoverContext?.getIntersectSlope()) {
      this.manipulateExistingSlope(cursorPolygon, increaseMode);
    } else {
      if (increaseMode) {
        this.createNewSlope(controls, cursorPolygon);
      }
    }
  }

  private manipulateExistingSlope(cursorPolygon: Feature<Polygon, any>, increaseMode: boolean) {
    if (increaseMode) {
      this.hoverContext!.getIntersectSlope()!.append(cursorPolygon);
    } else {
      this.hoverContext!.getIntersectSlope()!.remove(cursorPolygon);
    }
    this.saveContext.onManipulated(this.hoverContext!.getIntersectSlope()!);
  }

  manipulateDriveway(controls: Controls, increaseMode: boolean, cursorPolygon: Feature<Polygon, any> | undefined) {
    if (!cursorPolygon) {
      return;
    }

    if (increaseMode) {
      this.hoverContext!.getIntersectSlope()!.appendDriveway(cursorPolygon, controls);
    } else {
      // TODO
    }
    this.saveContext.onManipulated(this.hoverContext!.getIntersectSlope()!);
  }

  private createNewSlope(controls: Controls, cursorPolygon: Feature<Polygon, any>) {
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
