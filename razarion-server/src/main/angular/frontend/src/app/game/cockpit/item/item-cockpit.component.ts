import {Component, EventEmitter, Input, NgZone, Output} from '@angular/core';
import {ItemCockpitPanel} from "../../../gwtangular/GwtAngularFacade";
import {Sidebar} from "primeng/sidebar";


@Component({
  selector: 'item-cockpit',
  templateUrl: 'item-cockpit.component.html',
  styleUrls: ['item-cockpit.component.scss']
})
export class ItemCockpitComponent implements ItemCockpitPanel {
  showCockpit: boolean = false;

  constructor(private zone: NgZone) {
  }

  cleanPanels(): void {
    console.info("cleanPanels");
  }

  maximizeMinButton(): void {
    console.info("maximizeMinButton");
  }

  setBuildupItemPanel(buildupItemPanel: any): void {
    console.info("setBuildupItemPanel");
  }

  setInfoPanel(infoPanel: any): void {
    console.info("setInfoPanel");
  }

  setItemContainerPanel(itemContainerPanel: any): void {
    console.info("setItemContainerPanel");
  }

  showPanel(visible: boolean): void {
    // Change comes from outside Angular zone
    this.zone.run(() => {
      this.showCockpit = visible;
    });
  }
}
