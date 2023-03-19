import {Component, NgZone} from '@angular/core';
import {
  AngularZoneRunner,
  ItemCockpitFrontend,
  OtherItemCockpit,
  OwnItemCockpit,
  OwnMultipleIteCockpit
} from "../../../gwtangular/GwtAngularFacade";


@Component({
  selector: 'item-cockpit',
  templateUrl: 'item-cockpit.component.html',
  styleUrls: ['item-cockpit.component.scss']
})
export class ItemCockpitComponent implements ItemCockpitFrontend {
  showCockpit: boolean = false;
  ownItemCockpit?: OwnItemCockpit;
  ownMultipleIteCockpits?: OwnMultipleIteCockpit[];
  otherItemCockpit?: OtherItemCockpit;
  count?: number;

  constructor(private zone: NgZone) {
  }

  displayOwnSingleType(count: number, ownItemCockpit: OwnItemCockpit): void {
    console.info("displayOwnSingleType")
    this.zone.run(() => {
      this.showCockpit = true;
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
    console.info("displayOwnMultipleItemTypes")
    this.zone.run(() => {
      this.showCockpit = true;
      this.ownItemCockpit = undefined;
      this.ownMultipleIteCockpits = ownMultipleIteCockpits;
      this.otherItemCockpit = undefined;
    });
  }

  displayOtherItemType(otherItemCockpit: OtherItemCockpit): void {
    console.info("displayOtherItemType")
    this.zone.run(() => {
      this.showCockpit = true;
      this.ownItemCockpit = undefined;
      this.ownMultipleIteCockpits = undefined;
      this.otherItemCockpit = otherItemCockpit;
    });
  }

  dispose(): void {
    console.info("dispose")
    this.zone.run(() => {
      this.showCockpit = false;
      this.ownItemCockpit = undefined;
      this.ownMultipleIteCockpits = undefined;
      this.otherItemCockpit = undefined;
    });
  }
}
