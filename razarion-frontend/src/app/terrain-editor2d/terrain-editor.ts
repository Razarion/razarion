import {CanvasController} from "./canvas-controller";
import {SlopeContainer} from "./slope-container";
import {Cursor} from "./cursor";
import {PlanetConfig, TerrainSlopePosition} from "../generated/razarion-share";
import {SaveContext} from "./save-context";
import {Controls} from "./controls";
import {Slope} from "./slope";
import {EditorService} from "../editor/editor-service";
import {LOADING_COLOR, MISSING_CONFIG_COLOR} from "./colors";

export enum Mode {
  SELECT,
  PANNING,
  SLOPE_INCREASE,
  SLOPE_DECREASE,
  DRIVEWAY_INCREASE,
  DRIVEWAY_DECREASE,
  CORNER_ADD,
  CORNER_MOVE,
  CORNER_DELETE,
}

export class TerrainEditor {
  private readonly canvasController: CanvasController;
  private readonly slopeContainer: SlopeContainer;
  private readonly cursor: Cursor;
  private slopeGroundColors = new Map<number, string | null>();
  mode: Mode = Mode.SELECT;


  constructor(canvas: HTMLCanvasElement,
              canvasDiv: HTMLDivElement,
              controls: Controls,
              private editorService: EditorService) {
    this.slopeContainer = new SlopeContainer(this);
    this.cursor = new Cursor(controls);
    this.canvasController = new CanvasController(canvas,
      canvasDiv,
      this.slopeContainer,
      this.cursor,
      controls,
      this,
      editorService);
  }

  setTerrainSlopePositions(terrainSlopePosition: TerrainSlopePosition[]) {
    this.slopeContainer.setTerrainSlopePositions(terrainSlopePosition, this);
  }

  getSaveContext(): SaveContext {
    return this.slopeContainer.getSaveContext();
  }

  setPlanetConfig(planetConfig: PlanetConfig) {
    this.canvasController.setPlanetConfig(planetConfig);
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

  getGroundColor(slopeConfigId: number): string | null {
    let slopeGroundColor = this.slopeGroundColors.get(slopeConfigId);
    if (slopeGroundColor === undefined) {
      slopeGroundColor = LOADING_COLOR
      this.slopeGroundColors.set(slopeConfigId, LOADING_COLOR); // Add a marker for loading

      this.editorService.readSlopeConfig(slopeConfigId)
        .then(slopeConfig => {
          if (slopeConfig.waterConfigId !== null) {
            this.editorService.readWaterConfig(slopeConfig.waterConfigId)
              .then(waterConfig => {
                if (waterConfig.color) {
                  this.slopeGroundColors.set(slopeConfigId, waterConfig.color);
                } else {
                  this.slopeGroundColors.set(slopeConfigId, MISSING_CONFIG_COLOR);
                }
              });
          } else {
            if (slopeConfig.groundConfigId !== null) {
              this.editorService.readGroundConfig(slopeConfig.groundConfigId)
                .then(groundConfig => {
                  this.slopeGroundColors.set(slopeConfigId, groundConfig.color);
                });
            } else {
              this.slopeGroundColors.set(slopeConfigId, null);
            }
          }
        });
    }
    return slopeGroundColor
  }
}
