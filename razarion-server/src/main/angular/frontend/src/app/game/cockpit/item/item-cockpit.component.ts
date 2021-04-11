import {Component, NgZone} from '@angular/core';
import {
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
  infoPanel: any;

  constructor(private zone: NgZone) {
  }

  displayOwnSingleType(count: number, ownItemCockpit: OwnItemCockpit): void {
    this.zone.run(() => {
      this.showCockpit = true;
      console.info("displayOwnSingleType");
      console.info(count);
      console.info(ownItemCockpit);
    });
  }

  displayOwnMultipleItemTypes(ownMultipleIteCockpits: OwnMultipleIteCockpit[]): void {
    this.zone.run(() => {
      this.showCockpit = true;
      console.info("displayOwnMultipleItemTypes");
      console.info(ownMultipleIteCockpits);
    });
  }

  displayOtherItemType(otherItemCockpit: OtherItemCockpit): void {
    this.zone.run(() => {
      this.showCockpit = true;
      console.info("displayOtherItemType");
      console.info(otherItemCockpit);
    });
  }

  dispose(): void {
    this.zone.run(() => {
      this.showCockpit = false;
      console.info("dispose");
    });
  }

  maximizeMinButton(): void {
    console.info("maximizeMinButton");
  }
}
