import {Component, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {EditorService} from "../editor-service";
import {
  BotConfig,
  BotEnragementStateConfig,
  BotItemConfig,
  ServerGameEngineConfig
} from "../../generated/razarion-share";

@Component({
  selector: 'server-bot-editor',
  templateUrl: './server-bot-editor.component.html'
})
export class ServerBotEditorComponent extends EditorPanel implements OnInit {
  serverGameEngineConfig!: ServerGameEngineConfig;
  selectedBot?: BotConfig;

  constructor(public editorService: EditorService) {
    super();
  }

  ngOnInit(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfig = serverGameEngineConfig;
    })
  }

  onSave() {
    this.editorService.updateBotConfig(this.serverGameEngineConfig.botConfigs)
  }

  protected readonly EditorService = EditorService;

  onCreate() {
    this.selectedBot = {
      id: null,
      actionDelay: 0,
      autoAttack: false,
      auxiliaryId: 0,
      botEnragementStateConfigs: [],
      internalName: "New",
      maxActiveMs: 0,
      maxInactiveMs: 0,
      minActiveMs: 0,
      minInactiveMs: 0,
      name: "",
      npc: false,
      realm: null
    }
    this.serverGameEngineConfig!.botConfigs.push(this.selectedBot)
  }

  onDelete() {
    this.serverGameEngineConfig!.botConfigs.splice(this.serverGameEngineConfig!.botConfigs.findIndex(b => b === this.selectedBot), 1);
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
      idleTtl: 0,
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
