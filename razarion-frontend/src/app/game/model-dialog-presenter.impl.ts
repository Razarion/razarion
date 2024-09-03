import { NgZone } from "@angular/core";
import { BaseItemType, BoxContent, ModelDialogPresenter } from "../gwtangular/GwtAngularFacade";
import { GwtAngularService } from "../gwtangular/GwtAngularService";

export class ModelDialogPresenterImpl implements ModelDialogPresenter {
    title?: string;
    messageLines?: string[] = [];
    private queue: { title: string, messageLines?: string[] }[] = [];

    constructor(private zone: NgZone, private gwtAngularService: GwtAngularService) {
    }

    showQuestPassed(): void {
        this.zone.run(() => {
            this.post("Quest passed");
        });
    }

    showBaseLost(): void {
        this.zone.run(() => {
            this.post("Base lost");
        });
    }

    showLevelUp(): void {
        this.zone.run(() => {
            this.post("Level up");
        });
    }

    showUseInventoryItemLimitExceeded(baseItemType: BaseItemType): void {
        this.zone.run(() => {
            this.post("Item limit exceeded", [baseItemType.getI18nName().getString(this.gwtAngularService.gwtAngularFacade.language)]);
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
                    messgaeLine.push(`${inventoryItem.getI18nName().getString(this.gwtAngularService.gwtAngularFacade.language)}`);
                });
            }
            this.post("Box picked", messgaeLine);
        });
    }

    private post(title: string, messageLines?: string[]): void {
        if (this.title) {
            this.queue.push({ title: title, messageLines: messageLines });
        } else {
            this.displayMessage(title, messageLines);
        }
    }

    private displayMessage(title: string, messageLines?: string[]): void {
        this.title = title;
        this.messageLines = messageLines;
        setTimeout(() => {
            this.title = undefined;
            this.messageLines = undefined;
            if (this.queue.length > 0) {
                let queueEntry = this.queue.shift()!;
                this.displayMessage(queueEntry.title, queueEntry.messageLines);
            }
        }, 2000);
    }
}
