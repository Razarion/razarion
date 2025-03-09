import {HttpClient} from '@angular/common/http';
import {Component, OnInit} from '@angular/core';
import {MessageService} from 'primeng/api';
import {TypescriptGenerator} from 'src/app/backend/typescript-generator';
import {ConditionTrigger, QuestConfig, QuestControllerClient} from 'src/app/generated/razarion-share';
import {GwtAngularService} from 'src/app/gwtangular/GwtAngularService';
import {QuestCockpitComponent} from "../quest-cockpit.component";

@Component({
    selector: 'quest-dialog',
    templateUrl: './quest-dialog.component.html',
    standalone: false
})
export class QuestDialogComponent implements OnInit {
  quests: { title: string, description: string, rewards: string[], questId: number }[] = [];
  private questControllerClient: QuestControllerClient;

  constructor(httpClient: HttpClient, private messageService: MessageService, private gwtAngularService: GwtAngularService) {
    this.questControllerClient = new QuestControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.questControllerClient.readMyOpenQuests()
      .then(questConfigs => {
        this.quests = [];
        questConfigs.forEach(questConfig => {
          this.quests.push({
            title: QuestCockpitComponent.conditionTriggerToTitle(questConfig.conditionConfig.conditionTrigger!)!,
            description: this.setupDescription(questConfig),
            rewards: this.setupRewards(questConfig),
            questId: questConfig.id!
          });
        });
      }).catch(reason => {
        console.error(reason);
        this.messageService.add({
          severity: 'error',
          summary: "Loading quests failed",
          detail: reason.message,
          sticky: true
        });
      });
  }

  onActivate(questId: number): void {
    this.questControllerClient.activateQuest(questId)
      .catch(reason => {
        this.messageService.add({
          severity: 'error',
          summary: "Activate quest failed",
          detail: reason.message,
          sticky: true
        });
      });
  }

  private setupRewards(questConfig: QuestConfig): string[] {
    let rewards: string[] = [];
    if (questConfig.crystal > 0) {
      rewards.push(`Crystal: ${questConfig.crystal}`);
    }
    if (questConfig.xp > 0) {
      rewards.push(`XP: ${questConfig.xp}`);
    }
    if (questConfig.razarion > 0) {
      rewards.push(`Razarion: ${questConfig.razarion}`);
    }
    return rewards;
  }

  private setupDescription(questConfig: QuestConfig): string {
    switch (questConfig.conditionConfig.conditionTrigger!) {
      case ConditionTrigger.SYNC_ITEM_KILLED: {
        return this.specificOrCount("units or buildings destroyed", "destroyed", questConfig);
      }
      case ConditionTrigger.HARVEST: {
        return `Razarion harvested ${questConfig.conditionConfig.comparisonConfig.count}`;
      }
      case ConditionTrigger.SYNC_ITEM_CREATED: {
        return this.specificOrCount("units or buildings created", "created", questConfig);
      }
      case ConditionTrigger.BASE_KILLED: {
        return `Bases killed ${questConfig.conditionConfig.comparisonConfig.count}`;
      }
      case ConditionTrigger.SYNC_ITEM_POSITION: {
        return this.specificOrCount("units or buildings on position", "on region", questConfig);
      }
      case ConditionTrigger.BOX_PICKED: {
        return `Boxes picked ${questConfig.conditionConfig.comparisonConfig.count}`;
      }
      case ConditionTrigger.INVENTORY_ITEM_PLACED: {
        return `Inventory item placed ${questConfig.conditionConfig.comparisonConfig.count}`;
      }
      case ConditionTrigger.UNLOCKED: {
        return `Item unlocked ${questConfig.conditionConfig.comparisonConfig.count}`;
      }
      default: {
        console.warn(`Unknown ConditionTrigger ${questConfig.conditionConfig.conditionTrigger}`);
        return "";
      }
    }
  }

  private specificOrCount(textCount: string, textSpecific: string, questConfig: QuestConfig): string {
    if (questConfig.conditionConfig.comparisonConfig.count) {
      return `${questConfig.conditionConfig.comparisonConfig.count} ${textCount}`;
    } else if (questConfig.conditionConfig.comparisonConfig.typeCount) {
      let result: string[] = []
      for (const key in questConfig.conditionConfig.comparisonConfig.typeCount) {
        const count = questConfig.conditionConfig.comparisonConfig.typeCount[key];
        let itemTypeI8nName = this.gwtAngularService.gwtAngularFacade.itemTypeService.getBaseItemTypeAngular(parseInt(key)).getI18nName();
        if (itemTypeI8nName) {
          let itemTypeName = itemTypeI8nName.getString();
          result.push(`${count} ${itemTypeName} ${textSpecific}`);
        } else {
          result.push(`${count} ??? ${textSpecific}`);
        }
      }
      return result.join(", ");
    } else {
      return "";
    }
  }

}
