import {Component, OnInit} from '@angular/core';
import {ServerGameEngineConfig, StartRegionConfig} from "../../generated/razarion-share";
import {EditorService} from "../editor-service";
import {EditorPanel} from "../editor-model";
import {PlaceConfigComponent} from '../common/place-config/place-config.component';
import {DecimalPositionComponent} from '../common/decimal-position/decimal-position.component';
import {LevelComponent} from '../common/level/level.component';
import {FormsModule} from '@angular/forms';
import {InputNumber} from 'primeng/inputnumber';
import {Divider} from 'primeng/divider';
import {Button} from 'primeng/button';
import {DropdownModule} from 'primeng/dropdown';
import {NgIf} from '@angular/common';

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
    DropdownModule,
    NgIf
  ],
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
