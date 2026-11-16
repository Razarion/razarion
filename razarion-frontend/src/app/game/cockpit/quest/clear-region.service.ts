import {Injectable, NgZone, signal} from '@angular/core';
import {PlaceConfig} from '../../../gwtangular/GwtAngularFacade';
import {GwtAngularService} from '../../../gwtangular/GwtAngularService';
import {TipRegion} from '../../tip/guide/tip-region';
import {FirstInteractionTrackerService} from '../../tracking/first-interaction-tracker.service';

/** What the button would sell: the own items in the region and about what they bring. */
export interface ClearOffer {
  count: number;
  refund: number;
}

/** What the automatic sale sold, for the notice that says so. */
export interface ClearSale {
  count: number;
  refund: number;
}

/**
 * The quest that clears a region - level 9 clears the noob island (REGION_CLEARED, 2026-10-14). About
 * eight things stand there when level 9 starts, a long way from the new base, and selling them one by
 * one is housekeeping, not something to learn; it comes after 20-30 minutes of play, when the players
 * who leave are already leaving.
 * <p>
 * Sold automatically since 2026-11-15, and a notice says what it brought. Until then a card in the
 * middle offered the sale: PROD 07.-15.11.2026, of 186 players who left the island 150 sold from the
 * card (91 % placed the factory of quest 395 next), 30 closed it, and 11 of those never found the
 * button in the quest line again - 1 of them reached 395. The quest stayed on "clear the island"
 * until their guest base was removed an hour later. The buttons in the quest line and the panel stay
 * as the fallback should a sale not go through.
 */
@Injectable({
  providedIn: 'root'
})
export class ClearRegionService {
  /** The engine's sell factor (BaseItemService.ITEM_SELL_FACTOR). */
  private static readonly SELL_FACTOR = 0.5;
  private static readonly POLL_MILLIS = 500;
  /** Sold, and something is still there: the sale is sent again after this. */
  private static readonly RETRY_MILLIS = 5000;
  /** How long the notice of the sale stays, unless it is tapped away. */
  private static readonly NOTICE_MILLIS = 6000;
  readonly offer = signal<ClearOffer | null>(null);
  /** The notice of the automatic sale, null when there is none to show. */
  readonly sold = signal<ClearSale | null>(null);
  private region: TipRegion | null = null;
  private timer: ReturnType<typeof setInterval> | null = null;
  private noticeTimer: ReturnType<typeof setTimeout> | null = null;
  private lastAutoSale = 0;
  /**
   * Whether something sits in the middle of the screen right now - the level-up splash comes with the
   * same quest. The sale waits for it, so the notice is not hidden under it.
   */
  private blocked: () => boolean = () => false;

  constructor(private readonly gwtAngularService: GwtAngularService,
              private readonly tracker: FirstInteractionTrackerService,
              private readonly zone: NgZone) {
  }

  setBlocked(blocked: () => boolean): void {
    this.blocked = blocked;
  }

  activate(placeConfig: PlaceConfig | null): void {
    this.deactivate();
    if (!placeConfig) {
      return;
    }
    this.region = new TipRegion(placeConfig);
    this.lastAutoSale = 0;
    this.update();
    // Outside Angular: the signal tells the templates when the offer changes.
    this.timer = this.zone.runOutsideAngular(() => setInterval(() => this.update(), ClearRegionService.POLL_MILLIS));
  }

  /** The quest is over; the notice of the sale stays for its time. */
  deactivate(): void {
    if (this.timer !== null) {
      clearInterval(this.timer);
      this.timer = null;
    }
    this.region = null;
    this.offer.set(null);
  }

  /** @param from which button: the phone's quest line or the desktop quest panel; 'auto' for the automatic sale. */
  sellAll(from: 'auto' | 'strip' | 'panel'): void {
    const ids = this.itemsInRegion().map(item => item.id);
    if (ids.length === 0) {
      return;
    }
    if (from === 'auto') {
      this.tracker.report('CLEAR_REGION_AUTO_SOLD', 'count=' + ids.length);
    } else {
      this.tracker.report('CLEAR_REGION_SOLD', 'count=' + ids.length + ' from=' + from);
    }
    this.gwtAngularService.gwtAngularFacade.itemCockpitBridge.sellItems(ids);
  }

  dismissNotice(): void {
    if (this.noticeTimer !== null) {
      clearTimeout(this.noticeTimer);
      this.noticeTimer = null;
    }
    this.sold.set(null);
  }

  private autoSell(offer: ClearOffer): void {
    const now = Date.now();
    if (now - this.lastAutoSale < ClearRegionService.RETRY_MILLIS || this.blocked()) {
      return;
    }
    const first = this.lastAutoSale === 0;
    this.lastAutoSale = now;
    this.sellAll('auto');
    if (first) {
      this.zone.run(() => {
        this.dismissNotice();
        this.sold.set({count: offer.count, refund: offer.refund});
        this.noticeTimer = setTimeout(() => this.zone.run(() => this.dismissNotice()), ClearRegionService.NOTICE_MILLIS);
      });
    }
  }

  private update(): void {
    if (!this.region) {
      return;
    }
    const items = this.itemsInRegion();
    const itemTypeService = this.gwtAngularService.gwtAngularFacade.itemTypeService;
    const refund = Math.floor(items.reduce((sum, item) =>
      sum + itemTypeService.getBaseItemTypeAngular(item.itemTypeId).getPrice() * item.buildup * ClearRegionService.SELL_FACTOR, 0));
    const now = this.offer();
    if (!now || now.count !== items.length || now.refund !== refund) {
      this.offer.set(items.length > 0 ? {count: items.length, refund} : null);
    }
    if (items.length > 0) {
      this.autoSell({count: items.length, refund});
    }
  }

  /** Own items standing in the region; what is loaded into a container is not in the snapshot. */
  private itemsInRegion() {
    const region = this.region;
    if (!region) {
      return [];
    }
    try {
      return this.gwtAngularService.gwtAngularFacade.baseItemUiService.getTipItemStates(-1)
        .filter(item => item.own && region.distanceTo({x: item.x, y: item.y}) === 0);
    } catch (e) {
      return [];
    }
  }
}
