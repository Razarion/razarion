import {Component, NgZone} from "@angular/core";
import {
  ConditionConfig,
  QuestCockpit,
  QuestConfig,
  QuestDescriptionConfig,
  QuestProgressInfo,
  Tip,
  TipConfig
} from "../../../gwtangular/GwtAngularFacade";
import {GwtHelper} from "../../../gwtangular/GwtHelper";
import {GwtAngularService} from "../../../gwtangular/GwtAngularService";
import {ConditionTrigger} from "src/app/generated/razarion-share";
import {QuestDialogComponent} from "./quest-dialog/quest-dialog.component";
import {Dialog} from 'primeng/dialog';
import {CommonModule} from '@angular/common';
import {Button} from 'primeng/button';
import {ToggleSwitchModule} from 'primeng/toggleswitch';
import {FormsModule} from '@angular/forms';
import {CockpitDisplayService} from '../cockpit-display.service';
import {TipService} from '../../tip/tip.service';

@Component({
  selector: 'quest-cockpit',
  templateUrl: 'quest-cockpit.component.html',
  imports: [
    ToggleSwitchModule,
    Dialog,
    CommonModule,
    Button,
    QuestDialogComponent,
    FormsModule,
  ],
  styleUrls: ['quest-cockpit.component.scss']
})
export class QuestCockpitComponent implements QuestCockpit {
  title?: string
  progressRows: { text: string, done: boolean }[] = [];
  timeRow?: string = "";
  showQuestSelectionButton: boolean = false;
  showQuestDialog: boolean = false;
  showQuestInGameVisualisation: boolean = true;
  private questDescriptionConfig?: QuestDescriptionConfig;
  private conditionConfig?: ConditionConfig;
  private questProgressInfo?: QuestProgressInfo;

  constructor(private gwtAngularService: GwtAngularService,
              private cockpitDisplayService: CockpitDisplayService,
              private tipService: TipService,
              private zone: NgZone) {
  }

  showQuestSideBar(questDescriptionConfig: QuestDescriptionConfig | null, showQuestSelectionButton: boolean): void {
    this.zone.run(() => {
      try {
        this.questDescriptionConfig = questDescriptionConfig || undefined;
        this.conditionConfig = this.setupConditionConfig();
        this.questProgressInfo = undefined;
        this.setupTitle();
        this.setupProgress();
        this.showQuestSelectionButton = showQuestSelectionButton;
        this.cockpitDisplayService.showQuestCockpit = !!questDescriptionConfig;
        if (questDescriptionConfig && questDescriptionConfig.getTipConfig()) {
          this.tipService.activate(<QuestConfig>questDescriptionConfig)
          this.showQuestInGameVisualisation = false;
          this.onShowQuestInGameVisualisation(false);
        } else {
          this.showQuestInGameVisualisation = true;
          this.showQuestDialog = false;
          this.tipService.deactivate()
        }
      } catch (e) {
        console.warn(e);
      }
    });
  }

  // TODO unknown called from AbstractTipTask
  setShowQuestInGameVisualisation(): void {
  }

  onShowQuestInGameVisualisation(visible: boolean): void {
    try {
      this.gwtAngularService.gwtAngularFacade.inGameQuestVisualizationService.setVisible(visible);
    } catch (e) {
      console.warn(e);
    }
  }

  onQuestProgress(questProgressInfo: QuestProgressInfo | null): void {
    this.zone.run(() => {
      try {
        this.questProgressInfo = questProgressInfo || undefined;
        this.setupProgress();
      } catch (e) {
        console.warn(e);
      }
    });
  }

  private setupConditionConfig(): ConditionConfig | undefined {
    if (!this.questDescriptionConfig) {
      return undefined
    }
    if ((<QuestConfig>this.questDescriptionConfig).getConditionConfig && (<QuestConfig>this.questDescriptionConfig).getConditionConfig()) {
      return (<QuestConfig>this.questDescriptionConfig).getConditionConfig()!;
    } else {
      return undefined
    }
  }

  private setupTitle(): void {
    if (!this.questDescriptionConfig) {
      return undefined
    }
    if (this.conditionConfig?.getConditionTrigger()) {
      let conditionTrigger = GwtHelper.gwtIssue(this.conditionConfig?.getConditionTrigger());
      this.title = QuestCockpitComponent.conditionTriggerToTitle(conditionTrigger);
      if (!this.title) {
        console.warn(`Unknown ConditionTrigger ${conditionTrigger}`);
        this.title = `Unknown ConditionTrigger ${conditionTrigger}`;
      }
    } else {
      this.title = "Deploy unit";
    }
  }

  private setupProgress() {
    this.progressRows = [];
    if (!this.conditionConfig) {
      return;
    }

    switch (GwtHelper.gwtIssue(this.conditionConfig?.getConditionTrigger())) {
      case ConditionTrigger.SYNC_ITEM_KILLED: {
        this.specificOrCount("Units or buildings destroyed", "destroyed");
        break;
      }
      case ConditionTrigger.HARVEST: {
        this.setupSingleCount("Razarion harvested");
        break;
      }
      case ConditionTrigger.SYNC_ITEM_CREATED: {
        this.specificOrCount("Units or buildings created", "created");
        break;
      }
      case ConditionTrigger.BASE_KILLED: {
        this.setupSingleCount("Bases killed");
        break;
      }
      case ConditionTrigger.SYNC_ITEM_POSITION: {
        this.specificOrCount("Units or buildings on position", "on region");
        break;
      }
      case ConditionTrigger.BOX_PICKED: {
        this.setupSingleCount("Box picked");
        break;
      }
      case ConditionTrigger.INVENTORY_ITEM_PLACED: {
        this.setupSingleCount("Inventory items placed");
        break;
      }
      case ConditionTrigger.UNLOCKED: {
        this.setupSingleCount("Item unlocked");
        break;
      }
      case ConditionTrigger.SELL: {
        this.specificOrCount("Units or buildings sold", "sold");
        break;
      }
      default: {
        console.warn(`Unknown ConditionTrigger ${this.conditionConfig.getConditionTrigger()}`)
        this.progressRows.push({text: `???`, done: false})
      }
    }
    if (this.conditionConfig.getComparisonConfig().getTimeSeconds()) {
      if (this.questProgressInfo?.getSecondsRemaining()) {
        this.timeRow = `Time remaining: ${this.questProgressInfo?.getSecondsRemaining()} seconds`;
      } else {
        this.timeRow = `Time remaining: ${this.conditionConfig.getComparisonConfig().getTimeSeconds()} seconds`;
      }
    } else {
      this.timeRow = undefined
    }
  }

  private setupSingleCount(text: string) {
    let actualCount = GwtHelper.gwtIssueNumberNull(this.questProgressInfo?.getCount()) || 0;
    let expectedCount = GwtHelper.gwtIssueNumberNull((<QuestConfig>this.questDescriptionConfig).getConditionConfig()?.getComparisonConfig().getCount()) || 0;
    this.progressRows.push({
      text: `${text} ${actualCount} of ${expectedCount}`,
      done: actualCount >= expectedCount
    });
  }

  private specificOrCount(textCount: string, textSpecific: string) {
    if (this.conditionConfig?.getComparisonConfig().getCount()) {
      this.setupSingleCount(textCount);
    } else if (this.conditionConfig?.getComparisonConfig().toTypeCountAngular()?.length) {
      this.conditionConfig.getComparisonConfig().toTypeCountAngular().forEach((itemTypeIdCount) => {
        let actualCount = this.findCurrentItemTypeCount(itemTypeIdCount[0]);
        let itemTypeName = this.gwtAngularService.gwtAngularFacade.itemTypeService.getBaseItemTypeAngular(GwtHelper.gwtIssueNumber(itemTypeIdCount[0])).getName();
        this.progressRows.push({
          text: `${itemTypeName} ${textSpecific} ${actualCount} of ${itemTypeIdCount[1]}`,
          done: actualCount >= itemTypeIdCount[1]
        });
      });
    }
  }

  private findCurrentItemTypeCount(itemTypeId: number) {
    if (this.questProgressInfo) {
      let typeCounts = this.questProgressInfo.toTypeCountAngular();
      if (!typeCounts) {
        return 0;
      }
      let typeCount = typeCounts.find((ty) => ty[0] === itemTypeId);
      if (!typeCount) {
        return 0;
      }
      return typeCount[1];
    } else {
      return 0;
    }
  }

  static conditionTriggerToTitle(conditionTrigger: ConditionTrigger | null): string | undefined {
    if (!conditionTrigger) {
      return "???Undefined???"
    }
    switch (conditionTrigger) {
      case ConditionTrigger.SYNC_ITEM_KILLED: {
        return "Destroy";
      }
      case ConditionTrigger.HARVEST: {
        return "Harvest";
      }
      case ConditionTrigger.SYNC_ITEM_CREATED: {
        return "Build";
      }
      case ConditionTrigger.BASE_KILLED: {
        return "Destroy bases";
      }
      case ConditionTrigger.SYNC_ITEM_POSITION: {
        return "Region";
      }
      case ConditionTrigger.BOX_PICKED: {
        return "Pick box";
      }
      case ConditionTrigger.INVENTORY_ITEM_PLACED: {
        return "Inventory";
      }
      case ConditionTrigger.UNLOCKED: {
        return "Unlock";
      }
      case ConditionTrigger.SELL: {
        return "Sell";
      }
      default: {
        console.warn(`Unknown conditionTrigger ${conditionTrigger}`)
      }
    }
    return undefined
  }
}
