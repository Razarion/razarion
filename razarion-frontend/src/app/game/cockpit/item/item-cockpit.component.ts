import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  QueryList,
  ViewChild,
  ViewChildren
} from '@angular/core';

import {Carousel} from 'primeng/carousel';
import {ButtonModule} from 'primeng/button';
import {UserService} from '../../../auth/user.service';
import {Popover, PopoverModule} from 'primeng/popover';
import {TipService} from '../../tip/tip.service';
import {BuildupItemModel, ItemCockpitService} from './item-cockpit.service';

@Component({
  selector: 'item-cockpit',
  templateUrl: 'item-cockpit.component.html',
  styleUrls: ['item-cockpit.component.scss'],
  imports: [
    Carousel,
    ButtonModule,
    PopoverModule
]
})
export class ItemCockpitComponent implements AfterViewInit, OnDestroy {
  @ViewChild('tipPopover')
  tipPopover!: Popover;
  @ViewChildren('buildupItemDiv')
  buildupItemDiv?: QueryList<ElementRef>;
  private buildClickCallback: ((model: BuildupItemModel) => void) | null = null;

  constructor(public itemCockpitService: ItemCockpitService,
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

  buildTooltip(buildupItem: BuildupItemModel): string {
    if (buildupItem.buildHouseSpaceReached) {
      return `Build of ${buildupItem.itemTypeName} not possible. House space exceeded. Build more houses!`;
    } else if (buildupItem.buildLimitReached) {
      return `Build of ${buildupItem.itemTypeName} not possible. Item limit exceeded. Go to the next level!`;
    } else if (buildupItem.buildNoMoney) {
      return `Build off ${buildupItem.itemTypeName} not possible. Not enough Razarion. Earn more Razarion!`;
    } else {
      return `Build ${buildupItem.itemTypeName}`;
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

  onBuildClick(buildupItem: BuildupItemModel) {
    try {
      this.itemCockpitService.onBuild(buildupItem.itemTypeId);
    } catch (e) {
      console.error('onBuild() failed', e);
    }
    if (this.buildClickCallback) {
      this.buildClickCallback(buildupItem);
    }
  }

  setBuildClickCallback(buildClickCallback: ((model: BuildupItemModel) => void) | null) {
    this.buildClickCallback = buildClickCallback;
  }

  onUnloadClick() {
    const containerId = this.itemCockpitService.ownItemCockpit?.containerId;
    if (containerId != null) {
      this.itemCockpitService.onUnload(containerId);
    }
  }

}
