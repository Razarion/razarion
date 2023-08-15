import {Component} from "@angular/core";
import {
  ConditionTrigger,
  QuestCockpit,
  QuestConfig,
  QuestDescriptionConfig,
  QuestProgressInfo
} from "../../../gwtangular/GwtAngularFacade";
import {GwtHelper} from "../../../gwtangular/GwtHelper";
import {GwtAngularService} from "../../../gwtangular/GwtAngularService";

@Component({
  selector: 'quest-cockpit',
  templateUrl: 'quest-cockpit.component.html',
  styleUrls: ['quest-cockpit.component.scss']
})
export class QuestCockpitComponent implements QuestCockpit {
  showCockpit: boolean = false;
  questDescriptionConfig?: QuestDescriptionConfig;
  progressTable: string[] = [];
  questProgressInfo?: QuestProgressInfo;

  constructor(private gwtAngularService: GwtAngularService) {
  }

  showQuestSideBar(questDescriptionConfig: QuestDescriptionConfig | null, questProgressInfo: QuestProgressInfo | null, showQuestSelectionButton: boolean): void {
    try {
      this.showCockpit = !!questDescriptionConfig;
      this.questDescriptionConfig = questDescriptionConfig || undefined;
      this.questProgressInfo = questProgressInfo || undefined;
      this.setupProgress();
    } catch (e) {
      console.warn(e);
    }
  }

  setShowQuestInGameVisualisation(): void {
  }

  onQuestProgress(questProgressInfo: QuestProgressInfo | null): void {
    try {
      this.questProgressInfo = questProgressInfo || undefined;
      this.setupProgress();
    } catch (e) {
      console.warn(e);
    }
  }

  setBotSceneIndicationInfos(): void {
  }

  private setupProgress() {
    this.progressTable = [];
    if (!this.questDescriptionConfig || !this.questProgressInfo) {
      return;
    }

    if (!('getConditionConfig' in this.questDescriptionConfig)) {
      return
    }

    let conditionConfig = (<QuestConfig>this.questDescriptionConfig).getConditionConfig();
    if (!conditionConfig) {
      return;
    }
    if (conditionConfig) {
      switch (GwtHelper.gwtIssueStringEnum(conditionConfig?.getConditionTrigger(), ConditionTrigger)) {
        case ConditionTrigger.SYNC_ITEM_KILLED: {
          this.setupSingleOrMulti("Einheiten zerstört");
          break;
        }
        case ConditionTrigger.HARVEST: {
          this.setupSingleCount("Ressourcen gesammelt");
          break;
        }
        case ConditionTrigger.SYNC_ITEM_CREATED: {
          this.setupSingleOrMulti("Einheiten gebaut");
          break;
        }
        case ConditionTrigger.BASE_KILLED: {
          this.setupSingleCount("Basen ausgelöscht");
          break;
        }
        case ConditionTrigger.SYNC_ITEM_POSITION: {
          this.setupSingleOrMulti("Einheiten vorhanden");
          break;
        }
        case ConditionTrigger.BOX_PICKED: {
          this.setupSingleCount("Boxen gesammelt");
          break;
        }
        case ConditionTrigger.INVENTORY_ITEM_PLACED: {
          this.setupSingleCount("Inventar eingesetzt");
          break;
        }
        default: {
          console.warn(`Unknown ConditionTrigger ${conditionConfig.getConditionTrigger()}`)
          this.progressTable.push(`???`)
        }
      }
      if (conditionConfig.getComparisonConfig().getTimeSeconds()) {
        this.progressTable.push(`Verbleibende Zeit ${this.questProgressInfo?.getSecondsRemaining()}`)
      }
    }
  }

  private setupSingleCount(text: string) {
    this.progressTable.push(`${text} ${this.questProgressInfo?.getCount()}/${(<QuestConfig>this.questDescriptionConfig).getConditionConfig()?.getComparisonConfig().getCount()}`)
  }

  private setupSingleOrMulti(text: string) {
    let conditionConfig = (<QuestConfig>this.questDescriptionConfig)?.getConditionConfig();
    if (conditionConfig!.getComparisonConfig().getCount()) {
      this.setupSingleCount(text);
    } else if (conditionConfig!.getComparisonConfig().toTypeCountAngular()?.length) {
      this.progressTable.push(text);
      conditionConfig!.getComparisonConfig().toTypeCountAngular().forEach((itemTypeIdCount) => {
        let itemTypeName = this.gwtAngularService.gwtAngularFacade.itemTypeService.getBaseItemType(itemTypeIdCount[0]).getI18nName().getString(this.gwtAngularService.gwtAngularFacade.language);
        this.progressTable.push(`${itemTypeName} ${this.findCurrentItemTypeCount(itemTypeIdCount[0])}/${itemTypeIdCount[1]}`)
      });
    }
  }

  private findCurrentItemTypeCount(itemTypeId: number) {
    let typeCounts = this.questProgressInfo?.toTypeCountAngular();
    if (!typeCounts) {
      return "?";
    }
    let typeCount = typeCounts.find((ty) => ty[0] === itemTypeId);
    if (!typeCount) {
      return "?";
    }
    return typeCount[1];
  }
}
