import {Component, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {ResourceRegionConfig, ServerGameEngineConfig} from "../../generated/razarion-share";
import {EditorService} from "../editor-service";

@Component({
  selector: 'server-resource-region',
  templateUrl: './server-resource-region.component.html'
})
export class ServerResourceRegionComponent extends EditorPanel implements OnInit {
  serverGameEngineConfig?: ServerGameEngineConfig;
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
    this.editorService.updateResourceRegionConfig(this.serverGameEngineConfig?.resourceRegionConfigs)
  }


  protected readonly EditorService = EditorService;
}
