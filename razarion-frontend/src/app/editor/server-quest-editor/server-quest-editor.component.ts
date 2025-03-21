import {Component, OnInit} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {EditorService} from "../editor-service";
import {
  ConditionTrigger,
  QuestConfig,
  ServerGameEngineConfig,
  ServerLevelQuestConfig
} from "../../generated/razarion-share";
import {QuestCockpitComponent} from 'src/app/game/cockpit/quest/quest-cockpit.component';

@Component({
    selector: 'server-quest-editor',
    templateUrl: './server-quest-editor.component.html',
    standalone: false
})
export class ServerQuestEditorComponent extends EditorPanel implements OnInit {
  serverGameEngineConfig!: ServerGameEngineConfig;
  options: { label: string, value: ServerLevelQuestConfig }[] = [];
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
      ConditionTrigger.UNLOCKED,
    ];

  constructor(public editorService: EditorService) {
    super();
  }

  ngOnInit(): void {
    this.editorService.readServerGameEngineConfig().then(serverGameEngineConfig => {
      this.serverGameEngineConfig = serverGameEngineConfig;
      this.loadOptions();
    });
  }

  private loadOptions(): void {
    this.options.length = 0;
    let tmpSelectedLevelQuest = this.selectedLevelQuest;
    let levelIdMap = new Map<number, string>();
    this.editorService.readLevelObjectNameIds().then(objectNameIds => {
      objectNameIds.forEach(objectNameId => {
        levelIdMap.set(objectNameId.id, objectNameId.internalName);
      });
      this.serverGameEngineConfig.serverLevelQuestConfigs.forEach(levelQuest => {
        if ((levelQuest.minimalLevelId || levelQuest.minimalLevelId === 0) && levelIdMap.has(levelQuest.minimalLevelId)) {
          this.options.push({label: levelIdMap.get(levelQuest.minimalLevelId)!, value: levelQuest});
        } else {
          this.options.push({label: "?", value: levelQuest});
        }
      });

      this.options.sort((a, b) => {
        const numA = parseInt(a.label);
        const numB = parseInt(b.label);

        if (numA < numB) {
          return -1;
        } else if (numA > numB) {
          return 1;
        } else {
          return 0;
        }
      });

      this.selectedLevelQuest = tmpSelectedLevelQuest;
    });
  }

  onSave() {
    this.editorService.updateServerLevelQuestConfig(this.serverGameEngineConfig.serverLevelQuestConfigs).then(() => {
      this.loadOptions();
    });
  }

  onCreate() {
    this.selectedLevelQuest = {
      id: null,
      internalName: "New",
      minimalLevelId: null,
      questConfigs: []
    };
    this.serverGameEngineConfig!.serverLevelQuestConfigs.push(this.selectedLevelQuest);
    this.loadOptions();
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
          includeExisting: false
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

  onQuestUp(questConfig: QuestConfig): void {
    const index = this.selectedLevelQuest!.questConfigs.indexOf(questConfig);
    if (index > 0) {
      const temp = this.selectedLevelQuest!.questConfigs[index];
      this.selectedLevelQuest!.questConfigs[index] = this.selectedLevelQuest!.questConfigs[index - 1];
      this.selectedLevelQuest!.questConfigs[index - 1] = temp;
    }
  }

  onQuestDown(questConfig: QuestConfig) {
    const index = this.selectedLevelQuest!.questConfigs.indexOf(questConfig);
    if (index < this.selectedLevelQuest!.questConfigs.length - 1) {
      const temp = this.selectedLevelQuest!.questConfigs[index];
      this.selectedLevelQuest!.questConfigs[index] = this.selectedLevelQuest!.questConfigs[index + 1];
      this.selectedLevelQuest!.questConfigs[index + 1] = temp;
    }
  }

  conditionTriggerToTitleWrapper(questConfig: QuestConfig): string {
    return ServerQuestEditorComponent.conditionTriggerToTitle(questConfig);
  }

  static conditionTriggerToTitle(questConfig: QuestConfig): string {
    return (questConfig.title ? `'${questConfig.title}'` : "")
      + " "
      + QuestCockpitComponent.conditionTriggerToTitle(questConfig.conditionConfig.conditionTrigger!)!
      + " ( "
      + (questConfig.razarion ? `R:${questConfig.razarion} ` : '')
      + (questConfig.xp ? `X:${questConfig.xp} ` : '')
      + (questConfig.crystal ? `C:${questConfig.crystal} ` : '')
      + ")";
  }
}
