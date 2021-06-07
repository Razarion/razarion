import {Component, OnDestroy, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {TerrainEditorService} from "../../gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";

@Component({
  selector: 'app-terrain-editor',
  templateUrl: './terrain-editor.component.html',
  styleUrls: ['./terrain-editor.component.scss']
})
export class TerrainEditorComponent extends EditorPanel implements OnInit, OnDestroy {
  terrainEditorService: TerrainEditorService;
  slopes: any[] = [];
  selectedSlope: any;

  constructor(private gwtAngularService: GwtAngularService) {
    super();
    this.terrainEditorService = gwtAngularService.gwtAngularFacade.editorFrontendProvider.getTerrainEditorService();
  }

  ngOnInit(): void {
    this.terrainEditorService.activate();
    this.terrainEditorService.setSlopeMode(true);
    this.terrainEditorService.getAllSlopes().then(slopes => {
      this.slopes = [];
      slopes.forEach(slope => {
        this.slopes.push({name: slope.toString(), objectNameId: slope})
      });
      this.terrainEditorService.setSlope4New(this.slopes[0].objectNameId);
      this.selectedSlope = this.slopes[0];
    })
  }

  ngOnDestroy(): void {
    this.terrainEditorService.deactivate();
  }

  onTabSelected(event: any) {
    if (event.index === 0) {
      this.terrainEditorService.setSlopeMode(true);
    } else {
      this.terrainEditorService.setSlopeMode(false);
    }
  }

  onSlopeChange(event: any) {
    this.terrainEditorService.setSlope4New(event.value.objectNameId);
  }
}
