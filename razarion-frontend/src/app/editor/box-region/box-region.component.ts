import {Component, OnInit} from '@angular/core';
import {EditorPanel} from '../editor-model';
import {BoxRegionConfig, ServerGameEngineConfigEntity} from 'src/app/generated/razarion-share';
import {EditorService} from '../editor-service';
import {PlaceConfigComponent} from '../common/place-config/place-config.component';
import {Button} from 'primeng/button';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {NgIf} from '@angular/common';
import {BoxItemTypeComponent} from '../common/box-item-type/box-item-type.component';
import {Divider} from 'primeng/divider';
import {SelectModule} from 'primeng/select';
import {MessageService} from 'primeng/api';
import {ScrollPanelModule} from 'primeng/scrollpanel';

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
    SelectModule,
    ScrollPanelModule
  ],
  templateUrl: './box-region.component.html'
})
export class BoxRegionComponent extends EditorPanel implements OnInit {
  protected readonly EditorService = EditorService;
  serverGameEngineConfigEntity!: ServerGameEngineConfigEntity;
  selectedBoxRegionConfig?: BoxRegionConfig;

  constructor(public editorService: EditorService, private messageService: MessageService) {
    super();
  }

  ngOnInit(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfigEntity = serverGameEngineConfig;
    })
  }

  onSave() {
    this.editorService.updateBoxRegionConfig(this.serverGameEngineConfigEntity.boxRegionConfigs).catch(error => {
      console.error(error);
      this.messageService.add({
        severity: 'error',
        summary: `Can not save`,
        detail: error.message,
        sticky: true
      });
    });
  }

  onCreate() {
    this.serverGameEngineConfigEntity!.boxRegionConfigs.push({
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
    this.serverGameEngineConfigEntity!.boxRegionConfigs.splice(this.serverGameEngineConfigEntity!.boxRegionConfigs.findIndex(b => b === this.selectedBoxRegionConfig), 1);
    this.selectedBoxRegionConfig = undefined;
  }
}
