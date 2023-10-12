import {CanvasController} from "./canvas-controller";
import {SlopeContainer} from "./slope-container";
import {Cursor} from "./cursor";
import {TerrainSlopePosition} from "../generated/razarion-share";
import {SaveContext} from "./save-context";
import {Controls} from "./controls";

export class TerrainEditor {
  private readonly canvasController: CanvasController;
  private readonly slopeContainer: SlopeContainer;
  private readonly cursor: Cursor;

  constructor(canvas: HTMLCanvasElement,
              canvasDiv: HTMLDivElement,
              controls: Controls) {
    this.slopeContainer = new SlopeContainer();
    this.cursor = new Cursor;
    this.canvasController = new CanvasController(canvas,
      canvasDiv,
      this.slopeContainer,
      this.cursor,
      controls);
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
}
