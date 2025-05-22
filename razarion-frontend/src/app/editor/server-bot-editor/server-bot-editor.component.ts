import {Component, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {EditorService} from "../editor-service";
import {
  BotConfig,
  BotEnragementStateConfig,
  BotItemConfig,
  ServerGameEngineConfigEntity
} from "../../generated/razarion-share";
import {PlaceConfigComponent} from '../common/place-config/place-config.component';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {Checkbox} from 'primeng/checkbox';
import {BaseItemTypeComponent} from '../common/base-item-type/base-item-type.component';
import {Button} from 'primeng/button';
import {Accordion, AccordionModule,} from 'primeng/accordion';
import {NgForOf, NgIf} from '@angular/common';
import {BabylonMaterialComponent} from '../common/babylon-material/babylon-material.component';
import {Divider} from 'primeng/divider';
import {SelectModule} from 'primeng/select';

@Component({
  selector: 'server-bot-editor',
  imports: [
    PlaceConfigComponent,
    InputNumber,
    FormsModule,
    Checkbox,
    BaseItemTypeComponent,
    Button,
    Accordion,
    NgForOf,
    NgIf,
    BabylonMaterialComponent,
    Divider,
    SelectModule,
    AccordionModule,
  ],
  templateUrl: './server-bot-editor.component.html'
})
export class ServerBotEditorComponent extends EditorPanel implements OnInit {
  serverGameEngineConfigEntity!: ServerGameEngineConfigEntity;
  selectedBot?: BotConfig;

  constructor(public editorService: EditorService) {
    super();
  }

  ngOnInit(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfigEntity = serverGameEngineConfig;
    })
  }

  onSave() {
    this.editorService.updateBotConfig(this.serverGameEngineConfigEntity.botConfigs)
  }

  protected readonly EditorService = EditorService;

  onCreate() {
    this.selectedBot = {
      id: <any>null,
      actionDelay: 3000,
      autoAttack: false,
      auxiliaryId: 0,
      botEnragementStateConfigs: [],
      internalName: "New",
      maxActiveMs: null,
      maxInactiveMs: null,
      minActiveMs: null,
      minInactiveMs: null,
      name: "",
      npc: false,
      realm: null,
      groundBabylonMaterialId: null
    }
    this.serverGameEngineConfigEntity!.botConfigs.push(this.selectedBot)
  }

  onDelete() {
    this.serverGameEngineConfigEntity!.botConfigs.splice(this.serverGameEngineConfigEntity!.botConfigs.findIndex(b => b === this.selectedBot), 1);
    this.selectedBot = undefined;
  }

  onCreateEnragementState() {
    this.selectedBot?.botEnragementStateConfigs.push({botItems: [], enrageUpKills: 0, name: ""})
  }

  onDeleteEnragementState(botEnragementStateConfig: BotEnragementStateConfig) {
    this.selectedBot?.botEnragementStateConfigs.splice(this.selectedBot?.botEnragementStateConfigs.findIndex(b => b === botEnragementStateConfig), 1);
  }

  onCreateBotItem(botEnragementStateConfig: BotEnragementStateConfig) {
    botEnragementStateConfig.botItems.push({
      angle: 0,
      baseItemTypeId: null,
      count: 0,
      createDirectly: false,
      idleTtl: null,
      moveRealmIfIdle: false,
      noRebuild: false,
      noSpawn: false,
      place: null,
      rePopTime: 0
    })
  }

  onDeleteBotItem(botItem: BotItemConfig, botEnragementStateConfig: BotEnragementStateConfig) {
    botEnragementStateConfig.botItems.splice(botEnragementStateConfig.botItems.findIndex(b => b === botItem), 1);
  }
}
