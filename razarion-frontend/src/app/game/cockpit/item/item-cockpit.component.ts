import {
  AfterViewInit,
  Component,
  ElementRef,
  NgZone,
  OnDestroy,
  QueryList,
  ViewChild,
  ViewChildren
} from '@angular/core';
import {
  AngularZoneRunner,
  BuildupItemCockpit,
  ItemCockpitFrontend,
  ItemContainerCockpit,
  OtherItemCockpit,
  OwnItemCockpit,
  OwnMultipleIteCockpit
} from "../../../gwtangular/GwtAngularFacade";
import {CommonModule} from '@angular/common';
import {Carousel} from 'primeng/carousel';
import {ButtonModule} from 'primeng/button';
import {CockpitDisplayService} from '../cockpit-display.service';
import {UserService} from '../../../auth/user.service';
import {Popover, PopoverModule} from 'primeng/popover';
import {TipService} from '../../tip/tip.service';

@Component({
  selector: 'item-cockpit',
  templateUrl: 'item-cockpit.component.html',
  styleUrls: ['item-cockpit.component.scss'],
  imports: [
    CommonModule,
    Carousel,
    ButtonModule,
    PopoverModule
  ]
})
export class ItemCockpitComponent implements ItemCockpitFrontend, AfterViewInit, OnDestroy {
  ownItemCockpit?: OwnItemCockpit;
  ownMultipleIteCockpits?: OwnMultipleIteCockpit[];
  otherItemCockpit?: OtherItemCockpit;
  count?: number;
  @ViewChild('tipPopover')
  tipPopover!: Popover;
  @ViewChildren('buildupItemDiv')
  buildupItemDiv?: QueryList<ElementRef>;
  private buildClickCallback: ((cockpit: BuildupItemCockpit) => void) | null = null;

  constructor(private zone: NgZone,
              private cockpitDisplayService: CockpitDisplayService,
              private userService: UserService,
              private tipService: TipService) {
  }

  ngAfterViewInit(): void {
    this.tipService.setItemCockpit(this)
  }

  ngOnDestroy(): void {
    this.tipService.setItemCockpit(null)
  }

  isAdmin(): boolean {
    return this.userService.isAdmin();
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
      if (ownItemCockpit.itemContainerInfo) {
        ownItemCockpit.itemContainerInfo.setAngularZoneRunner(new class implements AngularZoneRunner {
          runInAngularZone(callback: any): void {
            zone.run(() => {
              callback();
            });
          }
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

  showBuildupTip(itemTypeId: number | null): boolean {
    if (itemTypeId != null) {
      if (!this.buildupItemDiv) {
        return false;
      }

      const itemTypeDiv = this.buildupItemDiv.find(div => {
          return (div.nativeElement as HTMLElement).getAttribute('data-item-type-id') === itemTypeId.toString()
        }
      );
      if (itemTypeDiv) {
        if (this.tipPopover) {
          this.tipPopover.show(null, itemTypeDiv.nativeElement);
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else {
      if (this.tipPopover) {
        this.tipPopover.hide();
      }
      return true;
    }
  }

  onBuildClick(buildupItemCockpit: BuildupItemCockpit) {
    buildupItemCockpit.onBuild();
    if (this.buildClickCallback) {
      this.buildClickCallback(buildupItemCockpit);
    }
  }

  setBuildClickCallback(buildClickCallback: ((cockpit: BuildupItemCockpit) => void) | null) {
    this.buildClickCallback = buildClickCallback;
  }

  onUnloadClick(itemContainerCockpit: ItemContainerCockpit) {
    itemContainerCockpit.onUnload();
  }

}
