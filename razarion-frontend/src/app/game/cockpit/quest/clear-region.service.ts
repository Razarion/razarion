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

/**
 * The quest that clears a region - level 9 clears the noob island (REGION_CLEARED, 2026-10-14) - with
 * one button that sells everything of the player's in it. About eight things stand there when level 9
 * starts, a long way from the new base, and selling them one by one is housekeeping, not something
 * to learn; it comes after 20-30 minutes of play, when the players who leave are already leaving.
 */
@Injectable({
  providedIn: 'root'
})
export class ClearRegionService {
  /** The engine's sell factor (BaseItemService.ITEM_SELL_FACTOR). */
  private static readonly SELL_FACTOR = 0.5;
  private static readonly POLL_MILLIS = 500;
  readonly offer = signal<ClearOffer | null>(null);
  /**
   * The card in the middle of the screen that offers the sale when the quest starts. The button in
   * the quest line alone would be missed - it is narrow, and level 9 is where attention runs out.
   * Closed, it stays closed for this quest; the quest line keeps its button.
   */
  readonly cardClosed = signal(false);
  private region: TipRegion | null = null;
  private timer: ReturnType<typeof setInterval> | null = null;

  constructor(private readonly gwtAngularService: GwtAngularService,
              private readonly tracker: FirstInteractionTrackerService,
              private readonly zone: NgZone) {
  }

  activate(placeConfig: PlaceConfig | null): void {
    this.deactivate();
    if (!placeConfig) {
      return;
    }
    this.region = new TipRegion(placeConfig);
    this.cardClosed.set(false);
    this.update();
    // Outside Angular: the signal tells the templates when the offer changes.
    this.timer = this.zone.runOutsideAngular(() => setInterval(() => this.update(), ClearRegionService.POLL_MILLIS));
  }

  deactivate(): void {
    if (this.timer !== null) {
      clearInterval(this.timer);
      this.timer = null;
    }
    this.region = null;
    this.offer.set(null);
  }

  closeCard(): void {
    this.cardClosed.set(true);
    this.tracker.report('CLEAR_REGION_CARD_CLOSED');
  }

  /** @param from which button: the card, the phone's quest line or the desktop quest panel. */
  sellAll(from: 'card' | 'strip' | 'panel'): void {
    const ids = this.itemsInRegion().map(item => item.id);
    if (ids.length === 0) {
      return;
    }
    this.cardClosed.set(true);
    this.tracker.report('CLEAR_REGION_SOLD', 'count=' + ids.length + ' from=' + from);
    this.gwtAngularService.gwtAngularFacade.itemCockpitBridge.sellItems(ids);
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
