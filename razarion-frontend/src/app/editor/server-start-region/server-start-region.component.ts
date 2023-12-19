import {Component, OnInit} from '@angular/core';
import {ServerGameEngineConfig, StartRegionConfig} from "../../generated/razarion-share";
import {EditorService} from "../editor-service";
import {EditorPanel} from "../editor-model";

@Component({
  selector: 'server-start-region',
  templateUrl: './server-start-region.component.html'
})
export class ServerStartRegionComponent extends EditorPanel implements OnInit {
  serverGameEngineConfig!: ServerGameEngineConfig;
  selectedStartRegion?: StartRegionConfig;

  constructor(private editorService: EditorService) {
    super();
  }

  ngOnInit(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfig = serverGameEngineConfig;
    })
  }

  onSave() {
    this.editorService.updateStartRegionConfig(this.serverGameEngineConfig.startRegionConfigs)
  }

  onCreate() {
    this.selectedStartRegion = {
      id: null,
      internalName: "New",
      minimalLevelId: null,
      noBaseViewPosition: null,
      region: null
    };
    this.serverGameEngineConfig!.startRegionConfigs.push(this.selectedStartRegion);
  }

  onDelete() {
    this.serverGameEngineConfig!.startRegionConfigs.splice(this.serverGameEngineConfig!.startRegionConfigs.findIndex(b => b === this.selectedStartRegion), 1);
    this.selectedStartRegion = undefined;
  }
}
