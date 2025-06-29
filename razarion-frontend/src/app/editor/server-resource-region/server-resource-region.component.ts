import {Component, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {ResourceRegionConfig, ServerGameEngineConfigEntity} from "../../generated/razarion-share";
import {EditorService} from "../editor-service";
import {PlaceConfigComponent} from '../common/place-config/place-config.component';
import {NgIf} from '@angular/common';
import {ResourceItemTypeComponent} from '../common/resource-item-type/resource-item-type.component';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {Divider} from 'primeng/divider';
import {Button} from 'primeng/button';
import {SelectModule} from 'primeng/select';
import {MessageService} from 'primeng/api';

@Component({
  selector: 'server-resource-region',
  imports: [
    PlaceConfigComponent,
    NgIf,
    ResourceItemTypeComponent,
    InputNumber,
    FormsModule,
    Divider,
    Button,
    SelectModule
  ],
  templateUrl: './server-resource-region.component.html'
})
export class ServerResourceRegionComponent extends EditorPanel implements OnInit {
  serverGameEngineConfigEntity!: ServerGameEngineConfigEntity;
  selectedResourceRegion?: ResourceRegionConfig;

  constructor(public editorService: EditorService, private messageService: MessageService) {
    super();
  }

  ngOnInit(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfigEntity = serverGameEngineConfig;
    })
  }

  onSave() {
    this.editorService.updateResourceRegionConfig(this.serverGameEngineConfigEntity.resourceRegionConfigs).catch(error => {
      console.error(error);
      this.messageService.add({
        severity: 'error',
        summary: `Can not save`,
        detail: error.message,
        sticky: true
      });
    });
  }


  protected readonly EditorService = EditorService;

  onCreate() {
    this.serverGameEngineConfigEntity!.resourceRegionConfigs.push({
      id: null,
      count: 0,
      internalName: "New",
      minDistanceToItems: 0,
      region: null,
      resourceItemTypeId: null
    });
  }

  onDelete() {
    this.serverGameEngineConfigEntity!.resourceRegionConfigs.splice(this.serverGameEngineConfigEntity!.resourceRegionConfigs.findIndex(b => b === this.selectedResourceRegion), 1);
    this.selectedResourceRegion = undefined;
  }
}
