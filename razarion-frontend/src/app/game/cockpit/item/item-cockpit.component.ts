import {
  AfterViewInit,
  Component,
  DoCheck,
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
import {BuildupItemModel, ItemCockpitService, OwnItemCockpitModel} from './item-cockpit.service';
import {TipStallReason} from '../../tip/tip-stall';
import {CompactLayoutService} from '../compact-layout.service';

/**
 * Where the build tip sits: viewport coordinates of the top centre of its button, plus what it is
 * pointing at - the buildup button itself, or the icon that opens the collapsed item panel.
 */
export interface BuildTipAnchor {
  x: number;
  y: number;
  mode: 'build' | 'open-menu';
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
export class ItemCockpitComponent implements AfterViewInit, DoCheck, OnDestroy {
  /**
   * The build tip, or null while none is up. Its own element rather than a p-popover: the tip has
   * to look like the prompt that sits on the builder out in the world - same green box with the
   * orange border, same mouse, same bounce - and it has to stand above the button it points at,
   * which a popover puts its panel below.
   */
  buildTip: BuildTipAnchor | null = null;
  /** Horizontal correction that keeps the tip box on screen; the arrow is not moved with it. */
  buildTipShift = 0;
  @ViewChild('buildTipBox')
  buildTipBox?: ElementRef<HTMLElement>;
  @ViewChild('buildupCarousel')
  buildupCarousel?: Carousel;
  @ViewChildren('buildupItemDiv')
  buildupItemDiv?: QueryList<ElementRef>;
  private buildClickCallback: ((model: BuildupItemModel) => void) | null = null;

  /**
   * A mouse with a blinking left button says nothing to a thumb, and "Click" is not what the
   * player does. Read once - a device does not grow a mouse mid-session.
   */
  protected readonly touch = typeof window !== 'undefined'
    && !!window.matchMedia?.('(pointer: coarse)').matches;

  private static readonly BLOCKED_MESSAGE_MILLIS = 5000;
  private static readonly SELL_ARM_MILLIS = 3000;

  /** Why the last tapped buildup button did nothing, or null. */
  protected buildBlockedMessage: string | null = null;
  /** True while the sell button waits for its confirming second tap. */
  protected sellArmed = false;
  private blockedMessageTimeout: ReturnType<typeof setTimeout> | null = null;
  private sellArmTimeout: ReturnType<typeof setTimeout> | null = null;
  private lastSeenCockpit: OwnItemCockpitModel | null = null;

  constructor(public itemCockpitService: ItemCockpitService,
              private userService: UserService,
              private tipService: TipService,
              protected compactLayout: CompactLayoutService) {
  }

  ngAfterViewInit(): void {
    this.tipService.setItemCockpit(this)
  }

  ngOnDestroy(): void {
    this.tipService.setItemCockpit(null)
    this.clearBlockedMessage();
    this.disarmSell();
  }

  /**
   * A new selection is a new context: a reason left over from the previous unit would be a lie, and
   * an armed sell button would be a trap. The component survives selection changes - only the model
   * behind it is rebuilt - so the identity of that model is the signal.
   */
  ngDoCheck(): void {
    const model = this.itemCockpitService.ownItemCockpit;
    if (model !== this.lastSeenCockpit) {
      this.lastSeenCockpit = model;
      this.clearBlockedMessage();
      this.disarmSell();
    }
  }

  isAdmin(): boolean {
    return this.userService.isAdmin();
  }

  buildTooltip(buildupItem: BuildupItemModel): string {
    return this.buildBlockReason(buildupItem) ?? `Build ${buildupItem.itemTypeName}`;
  }

  /** True while this type can actually be built right now. */
  isBuildable(buildupItem: BuildupItemModel): boolean {
    return this.buildBlockReason(buildupItem) === null;
  }

  /**
   * Why this type cannot be built, null when it can. One sentence, because it is shown to the
   * player and not only hovered: on a phone this is the only place the answer ever appears.
   */
  private buildBlockReason(buildupItem: BuildupItemModel): string | null {
    if (buildupItem.buildHouseSpaceReached) {
      return `${buildupItem.itemTypeName}: house space exceeded. Build more houses!`;
    } else if (buildupItem.buildLimitReached) {
      // Two different situations wear the same red: the limit is used up, or this type is not
      // unlocked at this level at all. "0 of 0" is the second one and telling the player to
      // destroy something would be wrong advice.
      return buildupItem.itemLimit > 0
        ? `${buildupItem.itemTypeName}: limit reached (${buildupItem.itemCount} of ${buildupItem.itemLimit}). Go to the next level!`
        : `${buildupItem.itemTypeName}: not unlocked yet. Go to the next level!`;
    } else if (buildupItem.buildNoMoney) {
      return `${buildupItem.itemTypeName}: not enough Razarion (costs ${buildupItem.price}). Earn more!`;
    } else if (this.itemCockpitService.ownItemCockpit?.factoryQueueFull) {
      return `${buildupItem.itemTypeName}: the build queue is full. Wait for it to empty!`;
    }
    return null;
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
    if (blockReason === TipStallReason.ITEM_PANEL_CLOSED) {
      // The button is fine, it is just behind a collapsed panel. Pointing at a hidden button was
      // the old behaviour and it left a prompt bouncing over an empty stretch of screen; point at
      // the icon that brings the panel back instead.
      this.anchorToCompactBuildIcon();
      return false;
    }
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
    this.buildTip = {x: rect.left + rect.width / 2, y: rect.top, mode: 'build'};
    this.updateBuildTipShift(this.buildTip.x);
    return true;
  }

  /**
   * How far the box has to move sideways to stay on screen. The box is measured rather than
   * guessed, but it is only in the document once the tip is up - the estimate covers the first
   * call, and the tip tasks re-anchor every second, so a fresh measurement follows immediately.
   */
  private updateBuildTipShift(anchorX: number): void {
    const MARGIN = 8;
    const ESTIMATED_WIDTH = 220;
    const width = this.buildTipBox?.nativeElement.offsetWidth || ESTIMATED_WIDTH;
    const left = anchorX - width / 2;
    const right = anchorX + width / 2;
    if (left < MARGIN) {
      this.buildTipShift = MARGIN - left;
    } else if (right > window.innerWidth - MARGIN) {
      this.buildTipShift = window.innerWidth - MARGIN - right;
    } else {
      this.buildTipShift = 0;
    }
  }

  /**
   * Puts the tip on the wrench in the compact icon bar - the one tap that stands between the
   * player and the build menu. Looked up in the document because that button belongs to the game
   * component while the tip is drawn here, and re-measured on every call like the other anchor.
   */
  private anchorToCompactBuildIcon(): void {
    const icon = document.getElementById('compact-build-icon');
    if (!icon) {
      this.buildTip = null;
      return;
    }
    const rect = icon.getBoundingClientRect();
    this.buildTip = {x: rect.left + rect.width / 2, y: rect.top, mode: 'open-menu'};
    this.updateBuildTipShift(this.buildTip.x);
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
    // Last, so a real blocker is still reported as itself: the panel being collapsed only matters
    // for a button that would otherwise be clickable. The panel stays in the DOM when hidden - it
    // is registered with the GWT facade - so everything above measures the same either way.
    if (this.compactLayout.compact() && !this.compactLayout.isOpen('item')) {
      return TipStallReason.ITEM_PANEL_CLOSED;
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
    const blockReason = this.buildBlockReason(buildupItem);
    if (blockReason !== null) {
      // The button answers instead of doing nothing. Five identical red tiles and no way to hover
      // is the whole problem this exists to solve.
      this.showBlockedMessage(blockReason);
      return;
    }
    this.clearBlockedMessage();
    try {
      this.itemCockpitService.onBuild(buildupItem.itemTypeId);
    } catch (e) {
      console.error('onBuild() failed', e);
    }
    if (this.buildClickCallback) {
      this.buildClickCallback(buildupItem);
    }
  }

  private showBlockedMessage(message: string): void {
    this.buildBlockedMessage = message;
    if (this.blockedMessageTimeout !== null) {
      clearTimeout(this.blockedMessageTimeout);
    }
    this.blockedMessageTimeout = setTimeout(() => {
      this.buildBlockedMessage = null;
      this.blockedMessageTimeout = null;
    }, ItemCockpitComponent.BLOCKED_MESSAGE_MILLIS);
  }

  private clearBlockedMessage(): void {
    if (this.blockedMessageTimeout !== null) {
      clearTimeout(this.blockedMessageTimeout);
      this.blockedMessageTimeout = null;
    }
    this.buildBlockedMessage = null;
  }

  /**
   * Selling cannot be undone, so the first tap only arms the button. It disarms itself, otherwise a
   * player who tapped once and moved on would sell on their next visit to this corner.
   */
  onSellClick(): void {
    if (!this.sellArmed) {
      this.sellArmed = true;
      this.sellArmTimeout = setTimeout(() => {
        this.sellArmed = false;
        this.sellArmTimeout = null;
      }, ItemCockpitComponent.SELL_ARM_MILLIS);
      return;
    }
    this.disarmSell();
    this.itemCockpitService.onSell();
  }

  private disarmSell(): void {
    if (this.sellArmTimeout !== null) {
      clearTimeout(this.sellArmTimeout);
      this.sellArmTimeout = null;
    }
    this.sellArmed = false;
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
