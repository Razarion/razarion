import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {ObjectTerrainEditorComponent} from "./object-terrain-editor.component";
import {SlopeTerrainEditorComponent} from "./slope-terrain-editor.component";

@Component({
  selector: 'terrain-editor',
  templateUrl: './terrain-editor.component.html'
})
export class TerrainEditorComponent extends EditorPanel implements AfterViewInit {
  @ViewChild("objectTerrainEditor")
  objectTerrainEditor!: ObjectTerrainEditorComponent;
  @ViewChild("slopeTerrainEditor")
  slopeTerrainEditor!: SlopeTerrainEditorComponent;

  ngAfterViewInit(): void {
    this.onTabViewChangeEvent(0);
  }

  onTabViewChangeEvent(index: number) {
    if (index === 0) {
      this.objectTerrainEditor.activate();
      this.slopeTerrainEditor.deactivate();
    } else {
      this.objectTerrainEditor.deactivate();
      this.slopeTerrainEditor.activate();
    }
  }
}
