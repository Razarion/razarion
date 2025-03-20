import {Component, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {ResourceRegionConfig, ServerGameEngineConfig} from "../../generated/razarion-share";
import {EditorService} from "../editor-service";

@Component({
    selector: 'server-resource-region',
    templateUrl: './server-resource-region.component.html'
})
export class ServerResourceRegionComponent extends EditorPanel implements OnInit {
  serverGameEngineConfig!: ServerGameEngineConfig;
  selectedResourceRegion?: ResourceRegionConfig;

  constructor(public editorService: EditorService) {
    super();
  }

  ngOnInit(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfig = serverGameEngineConfig;
    })
  }

  onSave() {
    this.editorService.updateResourceRegionConfig(this.serverGameEngineConfig.resourceRegionConfigs)
  }


  protected readonly EditorService = EditorService;

  onCreate() {
    this.serverGameEngineConfig!.resourceRegionConfigs.push({
      id: null,
      count: 0,
      internalName: "New",
      minDistanceToItems: 0,
      region: null,
      resourceItemTypeId: null
    });
  }

  onDelete() {
    this.serverGameEngineConfig!.resourceRegionConfigs.splice(this.serverGameEngineConfig!.resourceRegionConfigs.findIndex(b => b === this.selectedResourceRegion), 1);
    this.selectedResourceRegion = undefined;
  }
}
