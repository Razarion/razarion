import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { EditorPanel } from "../editor-model";
import { ObjectTerrainEditorComponent } from "./object-terrain-editor.component";
import { ShapeTerrainEditorComponent } from "./shape-terrain-editor.component";
import { getUpdateMiniMapPlanetUrl } from "../../common";
import { HttpClient } from "@angular/common/http";
import { MessageService } from "primeng/api";
import { GwtAngularService } from 'src/app/gwtangular/GwtAngularService';

@Component({
  selector: 'terrain-editor',
  templateUrl: './terrain-editor.component.html'
})
export class TerrainEditorComponent extends EditorPanel implements AfterViewInit {
  @ViewChild("objectEditor")
  objectTerrainEditor!: ObjectTerrainEditorComponent;
  @ViewChild("shapeEditor")
  shapeTerrainEditor!: ShapeTerrainEditorComponent;
  @ViewChild('miniMapCanvas', { static: true })
  miniMapCanvas!: ElementRef<HTMLCanvasElement>;
  displayMiniMap = false;

  constructor(private httpClient: HttpClient,
    private messageService: MessageService,
    private gwtAngularService: GwtAngularService) {
    super();
  }

  ngAfterViewInit(): void {
    this.onTabViewChangeEvent(0);
  }

  onTabViewChangeEvent(index: number) {
    if (index === 0) {
      this.shapeTerrainEditor.activate();
      this.objectTerrainEditor.deactivate();
    } else {
      this.objectTerrainEditor.activate();
      this.shapeTerrainEditor.deactivate();
    }
  }

  onShowMiniMapDialog() {
    this.shapeTerrainEditor.generateMiniMap(this.miniMapCanvas.nativeElement);
    this.displayMiniMap = true;
  }

  saveMiniMap() {
    const planetId = this.gwtAngularService.gwtAngularFacade.gameUiControl.getPlanetConfig().getId();
    let dataUrl = this.miniMapCanvas.nativeElement.toDataURL("image/png");
    this.httpClient.put(getUpdateMiniMapPlanetUrl(planetId), dataUrl).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'MiniMap saved',
        });
      },
      error: error => {
        console.error(error);
        this.messageService.add({
          severity: 'error',
          summary: 'MiniMap saved failed',
          detail: error.message,
          sticky: true
        });
      }
    });
  }
}
