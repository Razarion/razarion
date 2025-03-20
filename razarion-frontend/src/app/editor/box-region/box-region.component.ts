import { Component, OnInit } from '@angular/core';
import { EditorPanel } from '../editor-model';
import { BoxRegionConfig, ServerGameEngineConfig } from 'src/app/generated/razarion-share';
import { EditorService } from '../editor-service';

@Component({
    selector: 'box-region',
    templateUrl: './box-region.component.html'
})
export class BoxRegionComponent extends EditorPanel implements OnInit {
  protected readonly EditorService = EditorService;
  serverGameEngineConfig!: ServerGameEngineConfig;
  selectedBoxRegionConfig?: BoxRegionConfig;

  constructor(public editorService: EditorService) {
    super();
  }

  ngOnInit(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfig = serverGameEngineConfig;
    })
  }

  onSave() {
    this.editorService.updateBoxRegionConfig(this.serverGameEngineConfig.boxRegionConfigs)
  }

  onCreate() {
    this.serverGameEngineConfig!.boxRegionConfigs.push({
      id: null,
      internalName: "New",
      boxItemTypeId: null,
      minInterval: 30,
      maxInterval: 30,
      count: 1,
      minDistanceToItems: 1,
      region: null,
    });
  }

  onDelete() {
    this.serverGameEngineConfig!.boxRegionConfigs.splice(this.serverGameEngineConfig!.boxRegionConfigs.findIndex(b => b === this.selectedBoxRegionConfig), 1);
    this.selectedBoxRegionConfig = undefined;
  }
}
