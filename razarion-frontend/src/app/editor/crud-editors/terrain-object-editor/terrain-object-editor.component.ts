import {Component} from '@angular/core';
import {CrudContainerChild} from "../crud-container/crud-container.component";
import {TerrainObjectConfig, TerrainObjectEditorControllerClient} from "../../../generated/razarion-share";

@Component({
    selector: 'terrain-object-editor',
    templateUrl: './terrain-object-editor.component.html'
})
export class TerrainObjectEditorComponent implements CrudContainerChild<TerrainObjectConfig> {
  static editorControllerClient = TerrainObjectEditorControllerClient;
  terrainObjectConfig!: TerrainObjectConfig;

  init(config: TerrainObjectConfig): void {
    this.terrainObjectConfig = config;
  }

  exportConfig(): TerrainObjectConfig {
    return this.terrainObjectConfig;
  }

  getId(): number {
    return this.terrainObjectConfig.id;
  }

}
