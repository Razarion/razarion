import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  QueryList,
  ViewChild,
  ViewChildren
} from '@angular/core';

import {DecimalPipe} from '@angular/common';
import {Carousel} from 'primeng/carousel';
import {ButtonModule} from 'primeng/button';
import {ProgressBar} from 'primeng/progressbar';
import {UserService} from '../../../auth/user.service';
import {TipService} from '../../tip/tip.service';
import {BuildupItemModel, ItemCockpitService} from './item-cockpit.service';
import {TipStallReason} from '../../tip/tip-stall';

/** Where the build tip sits: viewport coordinates of the top centre of its button. */
export interface BuildTipAnchor {
  x: number;
  y: number;
}

@Component({
  selector: 'item-cockpit',
  templateUrl: 'item-cockpit.component.html',
  styleUrls: ['item-cockpit.component.scss'],
  imports: [
    DecimalPipe,
    Carousel,
    ButtonModule,
    ProgressBar
]
})
export class ItemCockpitComponent implements AfterViewInit, OnDestroy {
  /**
   * The build tip, or null while none is up. Its own element rather than a p-popover: the tip has
   * to look like the prompt that sits on the builder out in the world - same green box with the
   * orange border, same mouse, same bounce - and it has to stand above the button it points at,
   * which a popover puts its panel below.
   */
  buildTip: BuildTipAnchor | null = null;
  @ViewChild('buildupCarousel')
  buildupCarousel?: Carousel;
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

  /**
   * Anchors the "Click to build" tip to a buildup button.
   *
   * Returns false while the button cannot be clicked at all - not rendered yet, or disabled by
   * item limit, house space, missing Razarion or a full factory queue. The tip tasks retry, so
   * the tip shows up as soon as the click does something. Reporting success for a dead button
   * left the player with a blinking prompt that swallowed every click.
   */
  showBuildupTip(itemTypeId: number | null): boolean {
    if (itemTypeId == null) {
      this.buildTip = null;
      return true;
    }

    const blockReason = this.getBuildupTipBlockReason(itemTypeId);
    if (blockReason !== null) {
      // A button that exists but cannot be clicked keeps a stale tip otherwise; the two
      // "not there yet" reasons have nothing to hide and no anchor to correct.
      if (blockReason !== TipStallReason.COCKPIT_NOT_READY && blockReason !== TipStallReason.BUTTON_NOT_RENDERED) {
        this.buildTip = null;
      }
      return false;
    }

    // All non-null by now - getBuildupTipBlockReason() returned null, which rules the other cases out.
    const buildupItems = this.itemCockpitService.ownItemCockpit!.buildupItems!;
    this.scrollBuildupCarouselTo(buildupItems.findIndex(item => item.itemTypeId === itemTypeId));
    const button: HTMLElement = this.findBuildupItemDiv(itemTypeId)!.nativeElement;
    // Re-measured on every call, and the tip tasks call this every second: every selection event
    // rebuilds the cockpit model and the carousel re-renders, so the button this points at is
    // regularly a different element than a moment ago - re-selecting the builder was enough to
    // leave the tip hanging over a node no longer in the document.
    const rect = button.getBoundingClientRect();
    this.buildTip = {x: rect.left + rect.width / 2, y: rect.top};
    return true;
  }

  /**
   * Why the tip cannot be put on the button, null when it can. Public because a stalled tip
   * reports it: the retry loop looks identical from the outside whether the cockpit is one frame
   * late or the quest asks for something this player is not allowed to build.
   */
  getBuildupTipBlockReason(itemTypeId: number): string | null {
    if (!this.buildupItemDiv) {
      return TipStallReason.COCKPIT_NOT_READY;
    }
    const buildupItems = this.itemCockpitService.ownItemCockpit?.buildupItems;
    const itemIndex = buildupItems ? buildupItems.findIndex(item => item.itemTypeId === itemTypeId) : -1;
    if (itemIndex < 0) {
      return TipStallReason.NOT_BUILDABLE;
    }
    if (!buildupItems![itemIndex].enabled) {
      return TipStallReason.BUTTON_DISABLED;
    }
    if (this.itemCockpitService.ownItemCockpit!.factoryQueueFull) {
      return TipStallReason.FACTORY_QUEUE_FULL;
    }
    if (!this.findBuildupItemDiv(itemTypeId)) {
      return TipStallReason.BUTTON_NOT_RENDERED;
    }
    return null;
  }

  private findBuildupItemDiv(itemTypeId: number): ElementRef | undefined {
    return this.buildupItemDiv?.find(div =>
      (div.nativeElement as HTMLElement).getAttribute('data-item-type-id') === itemTypeId.toString());
  }

  /**
   * The buildup buttons live in a paged carousel - the Builder alone can build 6 types at 5 per
   * page. On a page that is scrolled out of the viewport the tip anchors to a clipped element
   * and is never seen, so page to the target first.
   */
  private scrollBuildupCarouselTo(itemIndex: number): void {
    if (!this.buildupCarousel) {
      return;
    }
    const itemsPerPage = this.buildupCarousel.numScroll || this.buildupCarousel.numVisible || 1;
    const page = Math.floor(itemIndex / itemsPerPage);
    if (this.buildupCarousel.page !== page) {
      this.buildupCarousel.page = page;
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
