import {Component} from '@angular/core';
import {CrudContainerChild} from "../crud-container/crud-container.component";
import {TerrainObjectConfig, TerrainObjectEditorControllerClient} from "../../../generated/razarion-share";
import {Model3dComponent} from '../../common/model3d/model3d.component';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'terrain-object-editor',
  imports: [
    Model3dComponent,
    InputNumber,
    FormsModule
  ],
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
