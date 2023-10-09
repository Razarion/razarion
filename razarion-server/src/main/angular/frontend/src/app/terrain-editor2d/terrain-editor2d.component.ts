import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {TerrainEditor} from "./terrain-editor";
import {READ_TERRAIN_SLOPE_POSITIONS} from "../common";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {TerrainSlopePosition} from "../generated/razarion-share";
import {Controls} from "./model";

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

  constructor(private httpClient: HttpClient,
              private messageService: MessageService) {
  }

  ngOnInit(): void {
    this.terrainEditor = new TerrainEditor(this.canvas.nativeElement,
      this.canvasDiv.nativeElement,
      this.controls,
      {x: 100, y: 100});

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

}
