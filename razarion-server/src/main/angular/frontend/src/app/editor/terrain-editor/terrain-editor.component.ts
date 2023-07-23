import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {ObjectEditorComponent} from "./object-editor.component";
import {SlopeEditorComponent} from "./slope-editor.component";

@Component({
  selector: 'terrain-editor',
  templateUrl: './terrain-editor.component.html'
})
export class TerrainEditorComponent extends EditorPanel implements AfterViewInit {
  @ViewChild("objectEditor")
  objectEditor!: ObjectEditorComponent;
  @ViewChild("slopeEditor")
  slopeEditor!: SlopeEditorComponent;

  ngAfterViewInit(): void {
    this.onTabViewChangeEvent(0);
  }

  onTabViewChangeEvent(index: number) {
    if (index === 0) {
      this.objectEditor.activate();
      this.slopeEditor.deactivate();
    } else {
      this.objectEditor.deactivate();
      this.slopeEditor.activate();
    }
  }
}
