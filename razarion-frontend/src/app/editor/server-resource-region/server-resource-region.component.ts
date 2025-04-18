import {Component, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {ResourceRegionConfig, ServerGameEngineConfig} from "../../generated/razarion-share";
import {EditorService} from "../editor-service";
import {PlaceConfigComponent} from '../common/place-config/place-config.component';
import {NgIf} from '@angular/common';
import {ResourceItemTypeComponent} from '../common/resource-item-type/resource-item-type.component';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {Divider} from 'primeng/divider';
import {Button} from 'primeng/button';
import {DropdownModule} from 'primeng/dropdown';

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
    DropdownModule
  ],
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
