import {NgZone} from "@angular/core";
import {BaseItemType, BoxContent, ModelDialogPresenter} from "../gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../gwtangular/GwtAngularService";
import {CockpitDisplayService} from "./cockpit/cockpit-display.service";

export class ModelDialogPresenterImpl implements ModelDialogPresenter {
  private static readonly SPLASH_DISPLAY_DURATION_MS = 1000;
  /**
   * How long the line naming the next quest stays, counted from the splash - it outlasts the big
   * "Quest passed", so the last thing left on screen is what to do now.
   */
  private static readonly NEXT_DISPLAY_DURATION_MS = 3000;
  title?: string;
  messageLines?: string[] = [];
  /**
   * The splash says what comes next - GameComponent adds the quest the strip now shows. Most
   * players who leave on the beginners' island leave within seconds of passing a quest (PROD,
   * 27.09.-04.10.2026), and a big "Quest passed" alone reads like an ending.
   */
  showNext = false;
  private nextTimer: ReturnType<typeof setTimeout> | null = null;
  private queue: { title: string, messageLines?: string[], showNext: boolean }[] = [];

  constructor(private zone: NgZone,
              private cockpitDisplayService: CockpitDisplayService) {
  }

  showQuestPassed(): void {
    this.zone.run(() => {
      this.post("Quest passed", undefined, true);
    });
  }

  showAllQuestsCompleted(): void {
    this.zone.run(() => {
      this.cockpitDisplayService.openInfoDialog('quest-completed');
    });
  }

  showBaseLost(): void {
    this.zone.run(() => {
      this.post("Base lost");
    });
  }

  showLevelUp(): void {
    this.zone.run(() => {
      // Arrives some 100 ms after the quest that earned it. A second splash after that one kept
      // the field covered for another second; it is a line of the one already showing instead.
      if (this.title === "Quest passed") {
        this.messageLines = [...(this.messageLines ?? []), "Level up"];
        return;
      }
      this.post("Level up", undefined, true);
    });
  }

  showUseInventoryItemLimitExceeded(baseItemType: BaseItemType): void {
    this.zone.run(() => {
      this.post("Item limit exceeded", [baseItemType.getName()]);
    });
  }

  showUseInventoryHouseSpaceExceeded(): void {
    this.zone.run(() => {
      this.post("House space exceeded");
    });
  }

  showRegisterDialog(): void {
  }

  showSetUserNameDialog(): void {
  }

  showBoxPicked(boxContent: BoxContent): void {
    this.zone.run(() => {
      let messgaeLine: string[] = [];
      if (boxContent.getCrystals()) {
        messgaeLine.push("Crystals: " + boxContent.getCrystals());
      }
      if (boxContent.toInventoryItemArray() && boxContent.toInventoryItemArray().length > 0) {
        boxContent.toInventoryItemArray().map(inventoryItem => {
          messgaeLine.push(`${inventoryItem.getI18nName()?.getString() || inventoryItem.getInternalName()}`);
        });
      }
      this.post("Box picked", messgaeLine);
    });
  }

  /**
   * Public because the cockpit posts its own splashes - the Razarion warnings are decided in
   * Angular from the balance the cockpit already receives, not signalled by the engine.
   */
  post(title: string, messageLines?: string[], showNext = false): void {
    if (this.title) {
      this.queue.push({title: title, messageLines: messageLines, showNext: showNext});
    } else {
      this.displayMessage(title, messageLines, showNext);
    }
  }

  private displayMessage(title: string, messageLines: string[] | undefined, showNext: boolean): void {
    this.title = title;
    this.messageLines = messageLines;
    if (showNext) {
      this.showNext = true;
      if (this.nextTimer) {
        clearTimeout(this.nextTimer);
      }
      this.nextTimer = setTimeout(() => {
        this.nextTimer = null;
        this.showNext = false;
      }, ModelDialogPresenterImpl.NEXT_DISPLAY_DURATION_MS);
    }
    setTimeout(() => {
      this.title = undefined;
      this.messageLines = undefined;
      if (this.queue.length > 0) {
        let queueEntry = this.queue.shift()!;
        this.displayMessage(queueEntry.title, queueEntry.messageLines, queueEntry.showNext);
      }
    }, ModelDialogPresenterImpl.SPLASH_DISPLAY_DURATION_MS);
  }
}
