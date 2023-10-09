import {CanvasController} from "./canvas-controller";
import {SlopeContainer} from "./slope-container";
import {Cursor} from "./cursor";
import {TerrainSlopePosition} from "../generated/razarion-share";
import {Controls} from "./model";

export class TerrainEditor {
  private readonly canvasController: CanvasController;
  private readonly slopeContainer: SlopeContainer;
  private readonly cursor: Cursor;


  constructor(canvas: HTMLCanvasElement,
              canvasDiv: HTMLDivElement,
              controls: Controls,
              planetSize: { x: number, y: number }) {
    this.slopeContainer = new SlopeContainer;
    this.cursor = new Cursor;
    this.canvasController = new CanvasController(canvas,
      canvasDiv,
      this.slopeContainer,
      this.cursor,
      controls,
      planetSize);
  }

  setTerrainSlopePositions(terrainSlopePosition: TerrainSlopePosition[]) {
    this.slopeContainer.setTerrainSlopePositions(terrainSlopePosition);
  }

}
