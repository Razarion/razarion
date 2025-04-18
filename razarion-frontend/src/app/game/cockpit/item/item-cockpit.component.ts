﻿import {Component, NgZone} from '@angular/core';
import {
  AngularZoneRunner,
  ItemCockpitFrontend,
  OtherItemCockpit,
  OwnItemCockpit,
  OwnMultipleIteCockpit
} from "../../../gwtangular/GwtAngularFacade";
import {NgIf} from '@angular/common';
import {Carousel} from 'primeng/carousel';
import {Button} from 'primeng/button';


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
  showCockpit: boolean = false;
  ownItemCockpit?: OwnItemCockpit;
  ownMultipleIteCockpits?: OwnMultipleIteCockpit[];
  otherItemCockpit?: OtherItemCockpit;
  count?: number;

  constructor(private zone: NgZone) {
  }

  displayOwnSingleType(count: number, ownItemCockpit: OwnItemCockpit): void {
    setTimeout(()=> { // p-carousel strange behavior with this.zone.run()
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
