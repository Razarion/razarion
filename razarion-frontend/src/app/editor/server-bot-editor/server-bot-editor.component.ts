import {Component, OnDestroy, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {EditorService} from "../editor-service";
import {
  BotConfig,
  BotEnragementStateConfig,
  BotItemConfig,
  ServerGameEngineConfigEntity
} from "../../generated/razarion-share";
import {PlaceConfigComponent} from '../common/place-config/place-config.component';
import {InputNumberModule} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {Checkbox} from 'primeng/checkbox';
import {BaseItemTypeComponent} from '../common/base-item-type/base-item-type.component';
import {ButtonModule} from 'primeng/button';
import {Accordion, AccordionModule,} from 'primeng/accordion';
import {CommonModule, NgForOf} from '@angular/common';
import {BabylonMaterialComponent} from '../common/babylon-material/babylon-material.component';
import {Divider} from 'primeng/divider';
import {SelectModule} from 'primeng/select';
import {MessageService} from 'primeng/api';
import {ScrollPanelModule} from 'primeng/scrollpanel';
import {Model3dComponent} from '../common/model3d/model3d.component';
import {BotGroundEditorService} from './bot-ground-editor.service';
import {ToggleButtonModule} from 'primeng/togglebutton';

@Component({
  selector: 'server-bot-editor',
  imports: [
    PlaceConfigComponent,
    InputNumberModule,
    FormsModule,
    Checkbox,
    BaseItemTypeComponent,
    ButtonModule,
    Accordion,
    NgForOf,
    CommonModule,
    BabylonMaterialComponent,
    Divider,
    SelectModule,
    AccordionModule,
    ScrollPanelModule,
    Model3dComponent,
    ToggleButtonModule
  ],
  templateUrl: './server-bot-editor.component.html'
})
export class ServerBotEditorComponent extends EditorPanel implements OnInit, OnDestroy {
  serverGameEngineConfigEntity!: ServerGameEngineConfigEntity;
  selectedBot?: BotConfig;
  showGroundEditor = false;
  slopeModeGroundEditor = false;

  constructor(public editorService: EditorService,
              private messageService: MessageService,
              private botGroundEditorService: BotGroundEditorService) {
    super();
  }

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    if (this.showGroundEditor) {
      this.botGroundEditorService.deactivate(this.selectedBot!);
    }
  }

  private load(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfigEntity = serverGameEngineConfig;
    })
  }

  onSave() {
    this.editorService.updateBotConfig(this.serverGameEngineConfigEntity.botConfigs).catch(error => {
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
      groundBoxHeight: null,
      groundBoxModel3DEntityId: null,
      groundBoxPositions: [],
      botGroundSlopeBoxes: [],
    }
    this.serverGameEngineConfigEntity!.botConfigs.push(this.selectedBot)
  }

  onCopy() {
    this.serverGameEngineConfigEntity.botConfigs.push(this.selectedBot!);
    this.editorService.updateBotConfig(this.serverGameEngineConfigEntity.botConfigs).then(() => {
      this.load();
    }).catch(error => {
      console.error(error);
      this.messageService.add({
        severity: 'error',
        summary: `Can not save`,
        detail: error.message,
        sticky: true
      });
    });
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

  onOpenGroundEditor() {
    if (this.showGroundEditor) {
      this.botGroundEditorService.activate(this.selectedBot!);
    } else {
      this.botGroundEditorService.deactivate(this.selectedBot!);
    }
  }

  onHeightInput(height: number | string | null) {
    this.botGroundEditorService.setHeight(this.selectedBot!, height === null ? 0 : <number>height);
  }

  onSlopeModeGroundEditor() {
    this.botGroundEditorService.setSlopeMode(this.selectedBot!, this.slopeModeGroundEditor);
  }

  onRotationSlopeGroundEditor() {
    this.botGroundEditorService.rotationSlope(this.selectedBot!);
  }
}
