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
  ownItemCockpit?: OwnItemCockpit;
  ownMultipleIteCockpits?: OwnMultipleIteCockpit[];
  otherItemCockpit?: OtherItemCockpit;
  count?: number;

  constructor(private zone: NgZone) {
  }

  displayOwnSingleType(count: number, ownItemCockpit: OwnItemCockpit): void {
    this.zone.run(() => {
      this.showCockpit = true;
      this.ownItemCockpit = ownItemCockpit;
      this.ownMultipleIteCockpits = undefined;
      this.otherItemCockpit = undefined;
      this.count = count;
    });
  }

  displayOwnMultipleItemTypes(ownMultipleIteCockpits: OwnMultipleIteCockpit[]): void {
    this.zone.run(() => {
      this.showCockpit = true;
      this.ownItemCockpit = undefined;
      this.ownMultipleIteCockpits = ownMultipleIteCockpits;
      this.otherItemCockpit = undefined;
    });
  }

  displayOtherItemType(otherItemCockpit: OtherItemCockpit): void {
    this.zone.run(() => {
      this.showCockpit = true;
      this.ownItemCockpit = undefined;
      this.ownMultipleIteCockpits = undefined;
      this.otherItemCockpit = otherItemCockpit;
    });
  }

  dispose(): void {
    this.zone.run(() => {
      this.showCockpit = false;
      this.ownItemCockpit = undefined;
      this.ownMultipleIteCockpits = undefined;
      this.otherItemCockpit = undefined;
    });
  }
}
