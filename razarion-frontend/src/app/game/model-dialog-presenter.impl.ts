import { NgZone } from "@angular/core";
import { ModelDialogPresenter } from "../gwtangular/GwtAngularFacade";

export class ModelDialogPresenterImpl implements ModelDialogPresenter {
    message?: string;
    private messageQueue: string[] = [];

    constructor(private zone: NgZone) {
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

    private post(message: string): void {
        if (this.message) {
            this.messageQueue.push(message);
        } else {
            this.displayMessage(message);
        }
    }

    private displayMessage(message: string): void {
        this.message = message;
        setTimeout(() => {
            this.message = undefined;
            if (this.messageQueue.length > 0) {
                this.displayMessage(this.messageQueue.shift()!);
            }
        }, 2000);
    }
}