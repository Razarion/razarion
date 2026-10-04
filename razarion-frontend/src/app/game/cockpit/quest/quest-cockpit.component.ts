import {Component, NgZone, OnDestroy, TemplateRef, ViewChild} from "@angular/core";
import {
  ConditionConfig,
  QuestCockpit,
  QuestConfig,
  QuestDescriptionConfig,
  QuestProgressInfo,
  TipItemState
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
import {BabylonAudioService} from '../../renderer/babylon-audio.service';
import {CompactLayoutService} from '../compact-layout.service';
import {QuestMarkerService} from '../main/radar/quest-marker.service';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {inProgressOf, questMeter, QuestMeter} from './quest-meter';

export interface QuestProgressRow {
  text: string;
  done: boolean;
  actual: number;
  target: number;
  meter: QuestMeter | null;
  /** The item type whose construction fills the meter, for a quest that counts created items. */
  constructionTypeId?: number;
}

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
export class QuestCockpitComponent implements QuestCockpit, OnDestroy {
  /** The meter of one progress row - also drawn in the phone strip, which GameComponent owns. */
  @ViewChild('questMeter', {static: true}) meterTemplate!: TemplateRef<unknown>;
  title?: string
  progressRows: QuestProgressRow[] = [];
  timeRow?: string = "";
  showQuestSelectionButton: boolean = false;
  showQuestInGameVisualisation: boolean = true;
  /** Classes of the glow on the quest panel and the phone strip - see _quest-flash.scss. */
  flashClass = '';
  private flashAlternate = false;
  private lastFlashTime = 0;
  private pendingTickFlash: ReturnType<typeof setTimeout> | null = null;
  private clearFlashTimer: ReturnType<typeof setTimeout> | null = null;
  /** The quest the player has already been shown, so a reconnect to the same one does not glow. */
  private shownQuestId: number | null = null;
  /**
   * Progress as last seen, kept across showQuestSideBar() for the same quest. That call resets
   * the rows to zero, and the server resends the old count right after it - compared against the
   * reset rows, that would glow as progress on every scene switch and reconnect.
   */
  private knownProgress: { done: boolean, actual: number }[] = [];
  /**
   * Harvest progress arrives once a second for as long as the harvesters work. Glowing at that
   * rate turns the panel into a strobe, so a counter step glows at most this often; the steps in
   * between are folded into the next one.
   */
  private static readonly TICK_FLASH_INTERVAL_MS = 3000;
  /** A little longer than the longest glow, then the class goes so a re-rendered strip does not replay it. */
  private static readonly FLASH_CLEAR_MS = 1300;
  /** How often the meter reads the construction under way - a factory site takes 8 s. */
  private static readonly CONSTRUCTION_POLL_MS = 400;
  private constructionPoll: ReturnType<typeof setInterval> | null = null;
  private questDescriptionConfig?: QuestDescriptionConfig;
  private conditionConfig?: ConditionConfig;
  private questProgressInfo?: QuestProgressInfo;

  constructor(private gwtAngularService: GwtAngularService,
              public cockpitDisplayService: CockpitDisplayService,
              private tipService: TipService,
              private babylonAudioService: BabylonAudioService,
              private compactLayout: CompactLayoutService,
              private questMarkerService: QuestMarkerService,
              private renderService: BabylonRenderServiceAccessImpl,
              private zone: NgZone) {
  }

  /** The quest list dialog. 30vw is a desktop measure; on a phone it is narrower than its header. */
  get dialogWidth(): string {
    return this.compactLayout.compact() ? '96vw' : '30vw';
  }

  /**
   * The whole progress on one line, for the phone's quest strip - the only place the quest shows
   * there since the quest panel is gone from the compact layout. Every row, not only the open one:
   * a quest like "Radar and Powerplant" has two, and the strip has to say which is done.
   */
  get progressSummary(): string {
    const rows = this.progressRows.map(row => (row.done ? '✓ ' : '') + row.text);
    if (this.timeRow) {
      rows.push(this.timeRow);
    }
    return rows.join(' · ');
  }

  showQuestSideBar(questDescriptionConfig: QuestDescriptionConfig | null, showQuestSelectionButton: boolean): void {
    this.zone.run(() => {
      try {
        this.questDescriptionConfig = questDescriptionConfig || undefined;
        this.conditionConfig = this.setupConditionConfig();
        this.questProgressInfo = undefined;
        this.setupTitle();
        this.setupProgress();
        const questId = questDescriptionConfig ? GwtHelper.gwtIssueNumber(questDescriptionConfig.getId()) : null;
        if (questId !== null && questId !== this.shownQuestId) {
          this.shownQuestId = questId;
          this.knownProgress = this.snapshotProgress();
          this.flash('quest');
        }
        this.showQuestSelectionButton = showQuestSelectionButton;
        this.cockpitDisplayService.showQuestCockpit = !!questDescriptionConfig;
        // The quest's region on the minimap, for a quest without a tip - with one, the tip marks
        // what the player has to look at next, which may be the region or a unit on the way to it.
        this.questMarkerService.set('quest', questDescriptionConfig && !questDescriptionConfig.getTipConfig()
          ? QuestMarkerService.fromPlaceConfig(this.conditionConfig?.getComparisonConfig().getPlaceConfig())
          : null);
        if (questDescriptionConfig) {
          this.babylonAudioService.playQuestActivatedAudio();
        }
        if (questDescriptionConfig && questDescriptionConfig.getTipConfig()) {
          this.tipService.activate(<QuestConfig>questDescriptionConfig)
          this.showQuestInGameVisualisation = false;
          this.onShowQuestInGameVisualisation(false);
        } else {
          this.showQuestInGameVisualisation = true;
          this.cockpitDisplayService.showQuestDialog = false;
          this.tipService.deactivate()
        }
      } catch (e) {
        console.warn(e);
      }
    });
  }

  /**
   * A tap on the quest: the camera goes to what the quest wants now - where the tip points, or the
   * quest's region. It used to open the quest list, and a player looking for help there picked
   * another quest and lost the guided one (486 -> 392 on PROD, 2026-09-30). The list has its own
   * button now, and only for a quest that may be left.
   */
  goToQuestTarget(): void {
    const viewField = this.renderService.getCurrentViewField();
    const from = viewField ? {x: viewField.getScreenCenter().getX(), y: viewField.getScreenCenter().getY()} : null;
    const target = QuestMarkerService.jumpPoint(this.questMarkerService.get(), from);
    if (target) {
      this.renderService.setViewFieldCenter(target.x, target.y);
    }
    this.renderService.reportFirstInteraction('QUEST_JUMP', target ? undefined : 'target=none');
    // Something happens on every tap, also without a place to go: the quest itself lights up.
    this.flash('quest');
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
        if (questProgressInfo) {
          this.flashOnProgress();
        }
      } catch (e) {
        console.warn(e);
      }
    });
  }

  ngOnDestroy(): void {
    this.clearTimer(this.pendingTickFlash);
    this.clearTimer(this.clearFlashTimer);
    this.stopConstructionPoll();
  }

  /**
   * Glow only where the player got closer to the goal: a counter going up, or a row turning done.
   * The remaining time ticks every second and a unit leaving the region takes a count back down -
   * neither is progress to point at.
   */
  private flashOnProgress(): void {
    let rowDone = false;
    let counted = false;
    this.progressRows.forEach((row, index) => {
      const known = this.knownProgress[index];
      if (row.done && !known?.done) {
        rowDone = true;
      }
      if (row.actual > (known?.actual ?? 0)) {
        counted = true;
      }
    });
    this.knownProgress = this.snapshotProgress();
    if (rowDone) {
      this.flash('done');
    } else if (counted) {
      this.requestTickFlash();
    }
  }

  private snapshotProgress(): { done: boolean, actual: number }[] {
    return this.progressRows.map(row => ({done: row.done, actual: row.actual}));
  }

  private requestTickFlash(): void {
    const wait = this.lastFlashTime + QuestCockpitComponent.TICK_FLASH_INTERVAL_MS - Date.now();
    if (wait <= 0) {
      this.flash('tick');
    } else if (!this.pendingTickFlash) {
      this.pendingTickFlash = setTimeout(() => {
        this.pendingTickFlash = null;
        this.flash('tick');
      }, wait);
    }
  }

  private flash(level: 'tick' | 'done' | 'quest'): void {
    // A stronger glow, or a new quest, stands in for a counter step still waiting its turn.
    this.clearTimer(this.pendingTickFlash);
    this.pendingTickFlash = null;
    this.lastFlashTime = Date.now();
    this.flashAlternate = !this.flashAlternate;
    this.flashClass = `quest-flash-${level} quest-flash-${this.flashAlternate ? 'a' : 'b'}`;
    this.clearTimer(this.clearFlashTimer);
    this.clearFlashTimer = setTimeout(() => {
      this.clearFlashTimer = null;
      this.flashClass = '';
    }, QuestCockpitComponent.FLASH_CLEAR_MS);
  }

  private clearTimer(timer: ReturnType<typeof setTimeout> | null): void {
    if (timer) {
      clearTimeout(timer);
    }
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
      case ConditionTrigger.SYNC_ITEM_LOADED: {
        this.specificOrCount("Units loaded", "in the Transporter");
        break;
      }
      case ConditionTrigger.LOADED_CONTAINER_POSITION: {
        this.specificOrCount("Loaded units on position", "with cargo on region");
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
        this.progressRows.push({text: `???`, done: false, actual: 0, target: 0, meter: null})
      }
    }
    this.updateConstruction();
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
      done: actualCount >= expectedCount,
      actual: actualCount,
      target: expectedCount,
      meter: questMeter(actualCount, expectedCount)
    });
  }

  private specificOrCount(textCount: string, textSpecific: string) {
    if (this.conditionConfig?.getComparisonConfig().getCount()) {
      this.setupSingleCount(textCount);
    } else if (this.conditionConfig?.getComparisonConfig().toTypeCountAngular()?.length) {
      const counted = GwtHelper.gwtIssue(this.conditionConfig.getConditionTrigger()) === ConditionTrigger.SYNC_ITEM_CREATED;
      this.conditionConfig.getComparisonConfig().toTypeCountAngular().forEach((itemTypeIdCount) => {
        const itemTypeId = GwtHelper.gwtIssueNumber(itemTypeIdCount[0]);
        let actualCount = this.findCurrentItemTypeCount(itemTypeIdCount[0]);
        let itemTypeName = this.gwtAngularService.gwtAngularFacade.itemTypeService.getBaseItemTypeAngular(itemTypeId).getName();
        this.progressRows.push({
          text: `${itemTypeName} ${textSpecific} ${actualCount} of ${itemTypeIdCount[1]}`,
          done: actualCount >= itemTypeIdCount[1],
          actual: actualCount,
          target: itemTypeIdCount[1],
          meter: questMeter(actualCount, itemTypeIdCount[1]),
          constructionTypeId: counted ? itemTypeId : undefined
        });
      });
    }
  }

  /**
   * Fills the meters with what is under construction, and keeps reading it for as long as a row
   * counts created items and is not done yet. Only those rows: a unit on its way into a region is
   * not progress the way a factory turning out that unit is.
   */
  private updateConstruction(): void {
    const tracked = this.progressRows.filter(row => row.constructionTypeId !== undefined && !row.done);
    if (tracked.length === 0) {
      this.stopConstructionPoll();
      return;
    }
    let items: TipItemState[];
    try {
      items = this.gwtAngularService.gwtAngularFacade.baseItemUiService.getTipItemStates(-1);
    } catch (e) {
      // Before the engine is up there is nothing under construction yet.
      items = [];
    }
    for (const row of tracked) {
      row.meter = questMeter(row.actual, row.target, inProgressOf(row.constructionTypeId!, items));
    }
    if (!this.constructionPoll) {
      this.constructionPoll = setInterval(() => this.updateConstruction(), QuestCockpitComponent.CONSTRUCTION_POLL_MS);
    }
  }

  private stopConstructionPoll(): void {
    if (this.constructionPoll) {
      clearInterval(this.constructionPoll);
      this.constructionPoll = null;
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
      // The first quests in the game that are not one click on one thing: the title has to say
      // the gesture, because nothing else on screen does.
      case ConditionTrigger.SYNC_ITEM_LOADED: {
        return "Load into the Transporter";
      }
      case ConditionTrigger.LOADED_CONTAINER_POSITION: {
        return "Sail to the marked region";
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
