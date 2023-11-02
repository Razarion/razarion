import {CanvasController} from "./canvas-controller";
import {SlopeContainer} from "./slope-container";
import {Cursor} from "./cursor";
import {TerrainSlopePosition} from "../generated/razarion-share";
import {SaveContext} from "./save-context";
import {Controls} from "./controls";
import {Slope} from "./slope";

export enum Mode {
  SELECT,
  PANNING,
  SLOPE_INCREASE,
  SLOPE_DECREASE,
  DRIVEWAY_INCREASE,
  DRIVEWAY_DECREASE,
}

export class TerrainEditor {
  private readonly canvasController: CanvasController;
  private readonly slopeContainer: SlopeContainer;
  private readonly cursor: Cursor;
  mode: Mode = Mode.SELECT;


  constructor(canvas: HTMLCanvasElement,
              canvasDiv: HTMLDivElement,
              controls: Controls) {
    this.slopeContainer = new SlopeContainer();
    this.cursor = new Cursor(controls);
    this.canvasController = new CanvasController(canvas,
      canvasDiv,
      this.slopeContainer,
      this.cursor,
      controls,
      this);
  }

  setTerrainSlopePositions(terrainSlopePosition: TerrainSlopePosition[]) {
    this.slopeContainer.setTerrainSlopePositions(terrainSlopePosition);
  }

  getSaveContext(): SaveContext {
    return this.slopeContainer.getSaveContext();
  }

  setPlanetSize(x: number, y: number) {
    this.canvasController.setPlanetSize(x, y);
  }

  findParentSlope(slope: Slope | undefined): Slope | undefined {
    if (!slope) {
      return undefined;
    }
    return this.slopeContainer.findParentSlope(slope!);
  }

  onDelete(slope: Slope) {
    this.slopeContainer.deleteSlope(slope);
  }

  onChangeSlopeConfigId(slope: Slope) {
    this.slopeContainer.onChangeSlopeConfigId(slope);
  }

  onCursorChanged() {
    this.cursor.redraw();
  }
}
