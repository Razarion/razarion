import {CanvasController} from "./canvas-controller";
import {SlopeContainer} from "./slope-container";
import {TerrainSlopePosition} from "./generated/razarion-share";
import {Cursor} from "./cursor";

export class TerrainEditor {
    private readonly canvasController: CanvasController;
    private readonly slopeContainer: SlopeContainer;
    private readonly cursor: Cursor;


    constructor() {
        this.slopeContainer = new SlopeContainer;
        this.cursor = new Cursor;
        this.canvasController = new CanvasController(this.slopeContainer, this.cursor);
    }

    setTerrainSlopePositions(terrainSlopePosition: TerrainSlopePosition[]) {
        this.slopeContainer.setTerrainSlopePositions(terrainSlopePosition);
    }

}