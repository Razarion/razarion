import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { EditorPanel } from "../editor-model";
import { ObjectTerrainEditorComponent } from "./object-terrain-editor.component";
import { ShapeTerrainEditorComponent } from "./shape-terrain-editor.component";

@Component({
  selector: 'terrain-editor',
  templateUrl: './terrain-editor.component.html'
})
export class TerrainEditorComponent extends EditorPanel implements AfterViewInit {
  @ViewChild("objectEditor")
  objectTerrainEditor!: ObjectTerrainEditorComponent;
  @ViewChild("shapeEditor")
  shapeTerrainEditor!: ShapeTerrainEditorComponent;

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
}
