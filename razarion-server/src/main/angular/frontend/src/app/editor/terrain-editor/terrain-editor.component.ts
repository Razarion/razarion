import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {ObjectEditorComponent} from "./object-editor.component";
import {SlopeTerrainEditorComponent} from "./slope-terrain-editor.component";

@Component({
  selector: 'terrain-editor',
  templateUrl: './terrain-editor.component.html'
})
export class TerrainEditorComponent extends EditorPanel implements AfterViewInit {
  @ViewChild("objectEditor")
  objectEditor!: ObjectEditorComponent;
  @ViewChild("slopeTerrainEditor")
  slopeTerrainEditor!: SlopeTerrainEditorComponent;

  ngAfterViewInit(): void {
    this.onTabViewChangeEvent(0);
  }

  onTabViewChangeEvent(index: number) {
    if (index === 0) {
      this.objectEditor.activate();
      this.slopeTerrainEditor.deactivate();
    } else {
      this.objectEditor.deactivate();
      this.slopeTerrainEditor.activate();
    }
  }
}
