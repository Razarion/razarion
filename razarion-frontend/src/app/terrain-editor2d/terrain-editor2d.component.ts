import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {Mode, TerrainEditor} from "./terrain-editor";
import {READ_TERRAIN_SLOPE_POSITIONS, SLOPE_EDITOR_PATH, UPDATE_SLOPES_TERRAIN_EDITOR} from "../common";
import {HttpClient} from "@angular/common/http";
import {MenuItem, MessageService} from "primeng/api";
import {TerrainSlopePosition} from "../generated/razarion-share";
import {ObjectNameId} from "../gwtangular/GwtAngularFacade";
import {Controls} from "./controls";
import {EditorService} from "../editor/editor-service";
import {ActivatedRoute} from "@angular/router";

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
  terrainEditor?: TerrainEditor;
  controls: Controls = new Controls();
  slopeConfigs: any[] = [];
  menuItems: MenuItem[] = [];

  constructor(private httpClient: HttpClient,
              private messageService: MessageService,
              private editorService: EditorService,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.terrainEditor = new TerrainEditor(this.canvas.nativeElement,
      this.canvasDiv.nativeElement,
      this.controls);

    this.route.queryParams.subscribe(params => {
      this.planetId = parseInt(params[TerrainEditor2dComponent.PLANET_ID_PARAM]);
      this.loadTerrainSlopePositions();
      this.loadSlopeObjectNameIds();
      this.editorService.readPlanetConfig(this.planetId)
        .then(planetConfig => {
          this.planetSize = {x: planetConfig.size.x, y: planetConfig.size.y}
          this.terrainEditor!.setPlanetSize(planetConfig.size.x, planetConfig.size.y);
        })
    });
    this.menuItems = [
      {
        label: 'Select',
        icon: 'pi pi-check',
        styleClass: this.getStyleClass(Mode.SELECT),
        command: () => {
          this.setMode(Mode.SELECT);
        },

      },
      {
        label: 'Panning',
        icon: 'pi pi-arrows-h',
        styleClass: this.getStyleClass(Mode.PANNING),
        command: () => {
          this.setMode(Mode.PANNING);
        },
      },
      {
        label: 'Slope +',
        styleClass: this.getStyleClass(Mode.SLOPE_INCREASE),
        icon: 'pi pi-plus-circle',
        command: () => {
          this.setMode(Mode.SLOPE_INCREASE);
        },
      },
      {
        label: 'Slope -',
        icon: 'pi pi-circle',
        styleClass: this.getStyleClass(Mode.SLOPE_DECREASE),
        command: () => {
          this.setMode(Mode.SLOPE_DECREASE);
        },
      },
      {
        label: 'Driveway +',
        styleClass: this.getStyleClass(Mode.DRIVEWAY_INCREASE),
        icon: 'pi pi-chevron-circle-up',
        command: () => {
          this.setMode(Mode.DRIVEWAY_INCREASE);
        },
      },
      {
        label: 'Driveway -',
        styleClass: this.getStyleClass(Mode.DRIVEWAY_DECREASE),
        icon: 'pi pi-minus-circle',
        command: () => {
          this.setMode(Mode.DRIVEWAY_DECREASE);
        },
      },
    ];
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
          severity: 'Slope Load Error',
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

  private getStyleClass(mode: Mode): string {
    if (mode === this.terrainEditor?.mode) {
      return 'surface-300';
    } else {
      return '';
    }
  }

  private setMode(mode: Mode) {
    this.terrainEditor!.mode = mode;
    this.menuItems.forEach((item, index) => {
      item.styleClass = this.getStyleClass(index);
    });
  }

  restartPlanetWarm() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_WARM);
  }

  restartPlanetCold() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_COLD);
  }

  onDelete() {
    this.terrainEditor!.onDelete(this.controls.selectedSLope!);
    this.controls.selectedSLope = undefined;
  }

  onChangeSlopeConfigId() {
    this.terrainEditor!.onChangeSlopeConfigId(this.controls.selectedSLope!);
  }

}
