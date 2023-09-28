import {Component, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {EditorService} from "../editor-service";
import {
  ConditionTrigger,
  QuestConfig,
  ServerGameEngineConfig,
  ServerLevelQuestConfig
} from "../../generated/razarion-share";

@Component({
  selector: 'server-quest-editor',
  templateUrl: './server-quest-editor.component.html'
})
export class ServerQuestEditorComponent extends EditorPanel implements OnInit {
  serverGameEngineConfig?: ServerGameEngineConfig;
  selectedLevelQuest?: ServerLevelQuestConfig;

  protected readonly EditorService = EditorService;
  protected readonly CONDITION_TRIGGERS =
    [ConditionTrigger.SYNC_ITEM_KILLED,
      ConditionTrigger.HARVEST,
      ConditionTrigger.SYNC_ITEM_CREATED,
      ConditionTrigger.BASE_KILLED,
      ConditionTrigger.SYNC_ITEM_POSITION,
      ConditionTrigger.BOX_PICKED,
      ConditionTrigger.INVENTORY_ITEM_PLACED,
    ];

  constructor(public editorService: EditorService) {
    super();
  }

  ngOnInit(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfig = serverGameEngineConfig;
    })
  }

  onSave() {
    this.editorService.updateServerLevelQuestConfig(this.serverGameEngineConfig?.serverLevelQuestConfigs)
  }

  onCreate() {
    this.selectedLevelQuest = {
      id: null,
      internalName: "New",
      minimalLevelId: null,
      questConfigs: []
    };
    this.serverGameEngineConfig!.serverLevelQuestConfigs.push(this.selectedLevelQuest)
  }

  onDelete() {
    this.serverGameEngineConfig!.serverLevelQuestConfigs.splice(this.serverGameEngineConfig!.serverLevelQuestConfigs.findIndex(l => l === this.selectedLevelQuest), 1);
    this.selectedLevelQuest = undefined;
  }

  onCreateQuest() {
    this.selectedLevelQuest!.questConfigs.push({
      conditionConfig: {
        comparisonConfig: {
          count: null,
          typeCount: {},
          timeSeconds: null,
          placeConfig: null,
          botIds: [],
        },
        conditionTrigger: null
      },
      crystal: 0,
      description: "",
      hidePassedDialog: false,
      id: 0,
      internalName: "",
      passedMessage: "",
      razarion: 0,
      title: "",
      xp: 0
    })
  }

  onDeleteQuest(questConfig: QuestConfig) {
    this.selectedLevelQuest!.questConfigs.splice(this.selectedLevelQuest!.questConfigs.findIndex(b => b === questConfig), 1);
  }
}
