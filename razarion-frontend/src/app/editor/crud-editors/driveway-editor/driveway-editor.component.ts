import { Component } from '@angular/core';
import { DrivewayConfig } from "../../../generated/razarion-share";
import { EditorService } from "../../editor-service";
import { DRIVEWAY_EDITOR_PATH } from "../../../common";
import { Tools } from "@babylonjs/core";
import { CrudContainerChild } from '../crud-container/crud-container.component';

@Component({
  selector: 'app-driveway-editor',
  templateUrl: './driveway-editor.component.html'
})
export class DrivewayEditorComponent implements CrudContainerChild<DrivewayConfig> {
  public static readonly editorUrl = DRIVEWAY_EDITOR_PATH;
  drivewayConfig!: DrivewayConfig;

  constructor(private editorService: EditorService) {
  }

  init(drivewayConfig: DrivewayConfig): void {
    this.drivewayConfig = drivewayConfig;
  }

  exportConfig(): DrivewayConfig {
    return this.drivewayConfig;
  }

  getId(): number {
    return this.drivewayConfig.id;
  }

  angleChanged(angle: number) {
    this.drivewayConfig.angle = Tools.ToRadians(angle);
  }

  restartPlanetWarm() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_WARM);
  }

  restartPlanetCold() {
    this.editorService.executeServerCommand(EditorService.RESTART_PLANET_COLD);
  }

}
