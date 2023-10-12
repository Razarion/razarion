import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {TerrainEditor} from "./terrain-editor";
import {READ_TERRAIN_SLOPE_POSITIONS, SLOPE_EDITOR_PATH, UPDATE_SLOPES_TERRAIN_EDITOR} from "../common";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {TerrainSlopePosition} from "../generated/razarion-share";
import {ObjectNameId} from "../gwtangular/GwtAngularFacade";
import {Controls} from "./controls";
import {EditorService} from "../editor/editor-service";

@Component({
  selector: 'app-terrain-editor2d',
  templateUrl: './terrain-editor2d.component.html',
  styleUrls: ['./terrain-editor2d.component.scss']
})
export class TerrainEditor2dComponent implements OnInit {
  readonly PLANET_ID = 117; //TODO get from url
  // readonly PLANET_ID = 1; //TODO get from url
  @ViewChild('canvas', {static: true})
  canvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('canvasDiv', {static: true})
  canvasDiv!: ElementRef<HTMLDivElement>;
  private terrainEditor?: TerrainEditor;
  controls: Controls = new Controls();
  slopeConfigs: any[] = [];

  constructor(private httpClient: HttpClient,
              private messageService: MessageService,
              private editorService: EditorService) {
  }

  ngOnInit(): void {
    this.terrainEditor = new TerrainEditor(this.canvas.nativeElement,
      this.canvasDiv.nativeElement,
      this.controls,
      {x: 100, y: 100});

    this.loadTerrainSlopePositions();
    this.loadSlopeObjectNameIds();
  }

  private loadTerrainSlopePositions() {
    const url = `${READ_TERRAIN_SLOPE_POSITIONS}/${this.PLANET_ID}`;
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
    const url = `${UPDATE_SLOPES_TERRAIN_EDITOR}/${this.PLANET_ID}`;
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

  restartPlanetWarm() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_WARM);
  }

  restartPlanetCold() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_COLD);
  }
}
