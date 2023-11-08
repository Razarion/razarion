import {Component, ElementRef, HostBinding, OnInit, ViewChild} from '@angular/core';
import {Mode, TerrainEditor} from "./terrain-editor";
import {
  DRIVEWAY_EDITOR_PATH,
  READ_TERRAIN_SLOPE_POSITIONS,
  SLOPE_EDITOR_PATH,
  UPDATE_SLOPES_TERRAIN_EDITOR
} from "../common";
import {HttpClient} from "@angular/common/http";
import {MenuItem, MessageService} from "primeng/api";
import {TerrainSlopePosition} from "../generated/razarion-share";
import {ObjectNameId} from "../gwtangular/GwtAngularFacade";
import {Controls} from "./controls";
import {EditorService} from "../editor/editor-service";
import {ActivatedRoute} from "@angular/router";

const MENU_HIGHLIGHT = 'surface-300';

@Component({
  selector: 'app-terrain-editor2d',
  templateUrl: './terrain-editor2d.component.html',
  styleUrls: ['./terrain-editor2d.component.scss']
})
export class TerrainEditor2dComponent implements OnInit {
  static PLANET_ID_PARAM = "PLANET_ID";
  private planetId?: number;
  planetSize?: {
    x: number,
    y: number
  }
  @ViewChild('canvas', {static: true})
  canvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('canvasDiv', {static: true})
  canvasDiv!: ElementRef<HTMLDivElement>;
  @HostBinding("style.--cursor")
  cursor: string = 'default';
  terrainEditor?: TerrainEditor;
  controls: Controls = new Controls();
  slopeConfigs: any[] = [];
  drivewayConfigs: any[] = [];
  menuItems: MenuItem[] = [];
  Mode = Mode;

  constructor(private httpClient: HttpClient,
              private messageService: MessageService,
              private editorService: EditorService,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.terrainEditor = new TerrainEditor(this.canvas.nativeElement,
      this.canvasDiv.nativeElement,
      this.controls,
      this.editorService);

    this.route.queryParams.subscribe(params => {
      this.planetId = parseInt(params[TerrainEditor2dComponent.PLANET_ID_PARAM]);
      this.loadTerrainSlopePositions();
      this.loadSlopeObjectNameIds();
      this.loadDrivewayObjectNameIds();
      this.editorService.readPlanetConfig(this.planetId)
        .then(planetConfig => {
          this.terrainEditor!.setPlanetConfig(planetConfig);
        })
    });

    this.addMenuItem('Select', 'pi pi-check', Mode.SELECT);
    this.menuItems.push({separator: true,})
    this.addMenuItem('Panning', 'pi pi-arrows-h', Mode.PANNING);
    this.menuItems.push({separator: true,})
    this.addMenuItem('Slope +', 'pi pi-circle-fill', Mode.SLOPE_INCREASE);
    this.addMenuItem('Slope -', 'pi pi-circle', Mode.SLOPE_DECREASE);
    this.menuItems.push({separator: true,})
    this.addMenuItem('Driveway +', 'pi pi-chevron-circle-up', Mode.DRIVEWAY_INCREASE);
    this.addMenuItem('Driveway -', 'pi pi-minus-circle', Mode.DRIVEWAY_DECREASE);
    this.menuItems.push({separator: true,})
    this.addMenuItem('Corner +', 'pi pi-plus-circle', Mode.CORNER_ADD);
    this.addMenuItem('Corner move', 'pi pi-exclamation-circle', Mode.CORNER_MOVE);
    this.addMenuItem('Corner -', 'pi pi-times-circle', Mode.CORNER_DELETE);

    this.setCursor();
  }

  private loadTerrainSlopePositions() {
    const url = `${READ_TERRAIN_SLOPE_POSITIONS}/${this.planetId}`;
    this.httpClient.get(url).subscribe({
      next: (value) => {
        this.terrainEditor!.setTerrainSlopePositions(<TerrainSlopePosition[]>value);
      },
      error: error => {
        console.error(error);
        this.messageService.add({
          severity: 'Slope Load Error',
          summary: `Error calling: ${url}`,
          detail: error,
          sticky: true
        });
      }
    });
  }

  private loadSlopeObjectNameIds() {
    this.slopeConfigs = [];
    const url = `${SLOPE_EDITOR_PATH}/objectNameIds`;
    this.httpClient.get<ObjectNameId[]>(url).subscribe({
      next: objectNameIds => {
        objectNameIds.forEach(objectNameId => this.slopeConfigs.push({
          label: `${objectNameId.internalName} '${objectNameId.id}'`,
          value: objectNameId.id,
        }));
        this.controls.newSlopeConfigId = objectNameIds[0].id;
      },
      error: error => {
        console.error(error);
        this.messageService.add({
          severity: 'Slope load error',
          summary: `Error getObjectNameIds: ${url}`,
          detail: error,
          sticky: true
        });
      }
    });
  }

  private loadDrivewayObjectNameIds() {
    this.drivewayConfigs = [];
    const url = `${DRIVEWAY_EDITOR_PATH}/objectNameIds`;
    this.httpClient.get<ObjectNameId[]>(url).subscribe({
      next: objectNameIds => {
        objectNameIds.forEach(objectNameId => this.drivewayConfigs.push({
          label: `${objectNameId.internalName} '${objectNameId.id}'`,
          value: objectNameId.id,
        }));
        this.controls.newDrivewayConfigId = objectNameIds[0].id;
      },
      error: error => {
        console.error(error);
        this.messageService.add({
          severity: 'Driveway load error',
          summary: `Error getObjectNameIds: ${url}`,
          detail: error,
          sticky: true
        });
      }
    });
  }

  save() {
    const url = `${UPDATE_SLOPES_TERRAIN_EDITOR}/${this.planetId}`;
    this.httpClient.put(url, this.terrainEditor!.getSaveContext().generateSlopeTerrainEditorUpdate()).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          life: 300,
          summary: `Terrain saved`
        });
        this.terrainEditor!.getSaveContext().clear();
      },
      error: error => {
        console.error(error);
        this.messageService.add({
          severity: 'Terrain save failed',
          summary: `Error calling: ${url}`,
          detail: error,
          sticky: true
        });
      }

    })
  }

  private addMenuItem(label: string, icon: string, mode: Mode) {
    let menuItem: MenuItem =
      {
        label: label,
        styleClass: this.getStyleClass(mode),
        icon: icon,
        command: () => {
          this.setMode(mode, menuItem);
        }
      };
    this.menuItems.push(menuItem);
  }

  private getStyleClass(mode: Mode): string {
    if (mode === this.terrainEditor?.mode) {
      return MENU_HIGHLIGHT;
    } else {
      return '';
    }
  }

  private setMode(mode: Mode, menuItem: MenuItem) {
    this.terrainEditor!.mode = mode;
    this.controls.clearSelection();
    this.clearMenuHighlight();
    this.setCursor();
    menuItem.styleClass = MENU_HIGHLIGHT;
  }

  private clearMenuHighlight() {
    this.menuItems.forEach((item) => {
      if (!item.separator) {
        item.styleClass = '';
      }
    });
  }

  restartPlanetWarm() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_WARM);
  }

  restartPlanetCold() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_COLD);
  }

  onDelete() {
    this.terrainEditor!.onDelete(this.controls.selectedSlope!);
    this.controls.selectedSlope = undefined;
  }

  onChangeSlopeConfigId() {
    this.terrainEditor!.onChangeSlopeConfigId(this.controls.selectedSlope!);
  }

  private setCursor() {
    switch (this.terrainEditor?.mode) {
      case Mode.SELECT: {
        this.cursor = "pointer";
        return;
      }
      case Mode.PANNING: {
        this.cursor = "move";
        return;
      }
      case Mode.SLOPE_INCREASE:
      case Mode.SLOPE_DECREASE: {
        this.cursor = "none";
        return;
      }
      case Mode.DRIVEWAY_INCREASE:
      case Mode.DRIVEWAY_DECREASE: {
        this.cursor = "none";
        return;
      }
      case Mode.CORNER_ADD:
      case Mode.CORNER_MOVE:
      case Mode.CORNER_DELETE: {
        this.cursor = "default";
        return;
      }
      default: {
        this.cursor = "default";
        return;
      }
    }
  }
}
