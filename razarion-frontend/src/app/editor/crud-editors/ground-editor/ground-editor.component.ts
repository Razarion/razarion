import {Component} from '@angular/core';
import {
  BaseItemTypeEditorControllerClient,
  GroundConfig,
  GroundEditorControllerClient
} from "../../../generated/razarion-share";
import {CrudContainerChild} from "../crud-container/crud-container.component";

@Component({
  selector: 'app-ground-editor',
  templateUrl: './ground-editor.component.html'
})
export class GroundEditorComponent implements CrudContainerChild<GroundConfig> {
  static editorControllerClient = GroundEditorControllerClient;
  groundConfig!: GroundConfig


  init(groundConfig: GroundConfig): void {
    this.groundConfig = groundConfig;
  }

  exportConfig(): GroundConfig {
    return this.groundConfig;
  }

  getId(): number {
    return this.groundConfig.id;
  }

}
