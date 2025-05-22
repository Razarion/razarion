import {Component, OnInit} from '@angular/core';
import {ServerGameEngineConfigEntity, StartRegionConfig} from "../../generated/razarion-share";
import {EditorService} from "../editor-service";
import {EditorPanel} from "../editor-model";
import {PlaceConfigComponent} from '../common/place-config/place-config.component';
import {DecimalPositionComponent} from '../common/decimal-position/decimal-position.component';
import {LevelComponent} from '../common/level/level.component';
import {FormsModule} from '@angular/forms';
import {InputNumber} from 'primeng/inputnumber';
import {Divider} from 'primeng/divider';
import {Button} from 'primeng/button';
import {NgIf} from '@angular/common';
import {SelectModule} from 'primeng/select';

@Component({
  selector: 'server-start-region',
  imports: [
    PlaceConfigComponent,
    DecimalPositionComponent,
    LevelComponent,
    FormsModule,
    InputNumber,
    Divider,
    Button,
    NgIf,
    SelectModule
  ],
  templateUrl: './server-start-region.component.html'
})
export class ServerStartRegionComponent extends EditorPanel implements OnInit {
  serverGameEngineConfigEntity!: ServerGameEngineConfigEntity;
  selectedStartRegion?: StartRegionConfig;

  constructor(private editorService: EditorService) {
    super();
  }

  ngOnInit(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfigEntity = serverGameEngineConfig;
    })
  }

  onSave() {
    this.editorService.updateStartRegionConfig(this.serverGameEngineConfigEntity.startRegionConfigs)
  }

  onCreate() {
    this.selectedStartRegion = {
      id: null,
      internalName: "New",
      minimalLevelId: null,
      noBaseViewPosition: null,
      region: null
    };
    this.serverGameEngineConfigEntity!.startRegionConfigs.push(this.selectedStartRegion);
  }

  onDelete() {
    this.serverGameEngineConfigEntity!.startRegionConfigs.splice(this.serverGameEngineConfigEntity!.startRegionConfigs.findIndex(b => b === this.selectedStartRegion), 1);
    this.selectedStartRegion = undefined;
  }
}
