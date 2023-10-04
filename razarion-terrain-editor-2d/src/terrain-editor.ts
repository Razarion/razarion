import {CanvasController} from "./canvas-controller";
import {SlopeContainer} from "./slope-container";
import {TerrainSlopePosition} from "./generated/razarion-share";

export class TerrainEditor {
    private readonly canvasController: CanvasController;
    private readonly slopeContainer: SlopeContainer;


    constructor() {
        this.slopeContainer = new SlopeContainer;
        this.canvasController = new CanvasController(this.slopeContainer);
    }

    setTerrainSlopePositions(terrainSlopePosition: TerrainSlopePosition[]) {
        this.slopeContainer.setTerrainSlopePositions(terrainSlopePosition);
    }

}