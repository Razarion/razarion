import {Component, NgZone} from '@angular/core';
import {
  AngularZoneRunner,
  BuildupItemCockpit,
  ItemCockpitFrontend,
  OtherItemCockpit,
  OwnItemCockpit,
  OwnMultipleIteCockpit
} from "../../../gwtangular/GwtAngularFacade";
import {NgIf} from '@angular/common';
import {Carousel} from 'primeng/carousel';
import {Button} from 'primeng/button';
import {CockpitDisplayService} from '../cockpit-display.service';
import {AuthService} from '../../../auth/auth.service';


@Component({
  selector: 'item-cockpit',
  templateUrl: 'item-cockpit.component.html',
  styleUrls: ['item-cockpit.component.scss'],
  imports: [
    NgIf,
    Carousel,
    Button
  ]
})
export class ItemCockpitComponent implements ItemCockpitFrontend {
  ownItemCockpit?: OwnItemCockpit;
  ownMultipleIteCockpits?: OwnMultipleIteCockpit[];
  otherItemCockpit?: OtherItemCockpit;
  count?: number;

  constructor(private zone: NgZone,
              private cockpitDisplayService: CockpitDisplayService,
              private authService: AuthService) {
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  displayOwnSingleType(count: number, ownItemCockpit: OwnItemCockpit): void {
    setTimeout(() => { // p-carousel strange behavior with this.zone.run()
      this.cockpitDisplayService.showItemCockpit = true;
      this.ownItemCockpit = ownItemCockpit;
      this.ownMultipleIteCockpits = undefined;
      this.otherItemCockpit = undefined;
      this.count = count;
      let zone = this.zone;
      if (ownItemCockpit.buildupItemInfos) {
        ownItemCockpit.buildupItemInfos.forEach(buildupItemInfo => {
          buildupItemInfo.setAngularZoneRunner(new class implements AngularZoneRunner {
            runInAngularZone(callback: any): void {
              zone.run(() => {
                callback();
              });
            }
          });
        });
      }
    });
  }

  displayOwnMultipleItemTypes(ownMultipleIteCockpits: OwnMultipleIteCockpit[]): void {
    this.zone.run(() => {
      this.cockpitDisplayService.showItemCockpit = true;
      this.ownItemCockpit = undefined;
      this.ownMultipleIteCockpits = ownMultipleIteCockpits;
      this.otherItemCockpit = undefined;
    });
  }

  displayOtherItemType(otherItemCockpit: OtherItemCockpit): void {
    this.zone.run(() => {
      this.cockpitDisplayService.showItemCockpit = true;
      this.ownItemCockpit = undefined;
      this.ownMultipleIteCockpits = undefined;
      this.otherItemCockpit = otherItemCockpit;
    });
  }

  dispose(): void {
    this.zone.run(() => {
      this.cockpitDisplayService.showItemCockpit = false;
      this.ownItemCockpit = undefined;
      this.ownMultipleIteCockpits = undefined;
      this.otherItemCockpit = undefined;
    });
  }

  buildTooltip(buildupItemCockpit: BuildupItemCockpit): string {
    if (buildupItemCockpit.buildHouseSpaceReached) {
      return `Build of ${buildupItemCockpit.itemTypeName} not possible. House space exceeded. Build more houses!`;
    } else if (buildupItemCockpit.buildLimitReached) {
      return `Build of ${buildupItemCockpit.itemTypeName} not possible. Item limit exceeded. Go to the next level!`;
    } else if (buildupItemCockpit.buildNoMoney) {
      return `Build off ${buildupItemCockpit.itemTypeName} not possible. Not enough Razarion. Earn more Razarion!`;
    } else {
      return `Build ${buildupItemCockpit.itemTypeName}`;
    }
  }
}
