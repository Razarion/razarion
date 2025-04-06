import { Component, OnInit } from '@angular/core';
import { EditorPanel } from '../editor-model';
import { BoxRegionConfig, ServerGameEngineConfig } from 'src/app/generated/razarion-share';
import { EditorService } from '../editor-service';
import {PlaceConfigComponent} from '../common/place-config/place-config.component';
import {Button} from 'primeng/button';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {NgIf} from '@angular/common';
import {BoxItemTypeComponent} from '../common/box-item-type/box-item-type.component';
import {Divider} from 'primeng/divider';
import {DropdownModule} from 'primeng/dropdown';

@Component({
  selector: 'box-region',
  imports: [
    PlaceConfigComponent,
    Button,
    InputNumber,
    FormsModule,
    NgIf,
    BoxItemTypeComponent,
    Divider,
    DropdownModule
  ],
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
