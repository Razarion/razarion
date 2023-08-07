import {Component, NgZone} from '@angular/core';
import {MainCockpit, RadarState, Rectangle} from "../../../gwtangular/GwtAngularFacade";


@Component({
  selector: 'main-cockpit',
  templateUrl: 'main-cockpit.component.html',
  styleUrls: ['main-cockpit.component.scss']
})
export class MainCockpitComponent implements MainCockpit {
  showCockpit: boolean = false;
  admin: boolean = false;
  editorDialog: boolean = false;
  levelNumber!: number;
  xp!: number;
  xp2LevelUp!: number;
  resources: number = 0;
  itemCount: number = 0;
  houseSpace: number = 0;
  consuming!: number;
  generating!: number;
  radarState!: RadarState;

  constructor(private zone: NgZone) {
  }

  show(admin: boolean): void {
    this.zone.run(() => {
      this.admin = admin;
      this.showCockpit = true;
    });
  }

  hide(): void {
    this.zone.run(() => {
      this.showCockpit = false;
    });
  }

  showRadar(radarState: RadarState): void {
    this.zone.run(() => {
      this.radarState = radarState;
    });
  }

  clean(): void {
    // TODO
  }

  displayEnergy(consuming: number, generating: number): void {
    this.zone.run(() => {
      this.consuming = consuming;
      this.generating = generating;
    });
  }

  displayItemCount(itemCount: number, houseSpace: number): void {
    this.zone.run(() => {
      this.itemCount = itemCount;
      this.houseSpace = houseSpace;
    });
  }

  displayLevel(levelNumber: number): void {
    this.zone.run(() => {
      this.levelNumber = levelNumber;
    });
  }

  displayResources(resources: number): void {
    this.zone.run(() => {
      this.resources = resources;
    });
  }

  displayXps(xp: number, xp2LevelUp: number): void {
    this.zone.run(() => {
      this.xp = xp;
      this.xp2LevelUp = xp2LevelUp;
    });
  }

  getInventoryDialogButtonLocation(): Rectangle {
    return {};
  }

  getScrollHomeButtonLocation(): Rectangle {
    return {};
  }
}
