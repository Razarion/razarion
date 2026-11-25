import {Injectable, NgZone, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';
import {GwtAngularService} from '../../gwtangular/GwtAngularService';
import {BabylonRenderServiceAccessImpl} from '../renderer/babylon-render-service-access-impl.service';
import {FirstInteractionTrackerService} from '../tracking/first-interaction-tracker.service';
import {EmoteEvent, NativeSyncBaseItemTickInfo} from '../../gwtangular/GwtAngularFacade';

export type EmoteId = 'WAVE' | 'THUMBS_UP' | 'LAUGH' | 'GG' | 'QUESTION';

export interface EmoteDef {
  id: EmoteId;
  glyph: string;
  label: string;
}

export const EMOTES: EmoteDef[] = [
  {id: 'WAVE', glyph: '👋', label: 'Hi'},
  {id: 'THUMBS_UP', glyph: '👍', label: 'Nice'},
  {id: 'LAUGH', glyph: '😂', label: 'Haha'},
  {id: 'GG', glyph: 'GG', label: 'Good game'},
  {id: 'QUESTION', glyph: '❓', label: 'Help?'},
];

export function emoteGlyph(id: string): string | null {
  return EMOTES.find(e => e.id === id)?.glyph ?? null;
}

/** Somebody waved at the player: offered as one tap for this long. */
const REPLY_OFFER_MILLIS = 9000;

export interface ReplyOffer {
  name: string;
  glyph: string;
  x: number;
  y: number;
}

/**
 * Signs over units (EmoteService on the server): a player picks one with the emote button, it shows
 * over the own unit nearest the middle of the screen - the one the player is looking at - for
 * everybody who has that unit in view. Somebody else's wave is offered back with one tap: a new
 * player does not know the emote button yet, and an answer is what makes a wave a meeting.
 * <p>
 * Everything is measured: EMOTE_SENT (emote, from button or reply), EMOTE_SEEN (another player's
 * sign shown on a unit in view) and EMOTE_REPLY_OFFERED - next to EMOTE_SENT in user_activity on
 * the server.
 */
@Injectable({
  providedIn: 'root'
})
export class EmoteService {
  readonly replyOffer = signal<ReplyOffer | null>(null);
  /**
   * The picker opened from a button somewhere else than the map corner - the phone's unit panel
   * head: where it stands on the screen, in pixels from the right and bottom edge.
   */
  readonly pickerAnchor = signal<{ right: number, bottom: number } | null>(null);
  readonly sending = signal(false);
  private replyTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(private httpClient: HttpClient,
              private zone: NgZone,
              private gwtAngularService: GwtAngularService,
              private renderService: BabylonRenderServiceAccessImpl,
              private tracker: FirstInteractionTrackerService) {
  }

  /** Whether the player has a unit to show a sign over. */
  hasOwnUnit(): boolean {
    try {
      return this.ownUnits().length > 0;
    } catch (e) {
      return false;
    }
  }

  async send(id: EmoteId): Promise<void> {
    await this.post(id, this.sender(), false);
  }

  /**
   * Who shows the sign: the selected unit when the player picked one; otherwise the unit nearest
   * the middle of the screen - a unit that drives before a building, so a wave does not come from
   * a factory because it happened to stand closer.
   */
  sender(): NativeSyncBaseItemTickInfo | undefined {
    const units = this.ownUnits();
    let selected: number[] = [];
    try {
      selected = this.renderService.getSelectedOwnItemIds();
    } catch (e) {
      // No renderer yet: no selection either.
    }
    const selectedUnits = units.filter(unit => selected.includes(unit.id));
    if (selectedUnits.length > 0) {
      return selectedUnits.find(unit => this.isMovable(unit)) ?? selectedUnits[0];
    }
    const center = this.renderService.getCurrentViewField()?.getCenter();
    return center ? this.nearest(center.getX(), center.getY()) : units[0];
  }

  /** "Builder", "Viper" ... - the picker says who will wave. */
  senderName(): string | null {
    const unit = this.sender();
    if (!unit) {
      return null;
    }
    try {
      return this.gwtAngularService.gwtAngularFacade.itemTypeService.getBaseItemTypeAngular(unit.itemTypeId).getName();
    } catch (e) {
      return null;
    }
  }

  async replyWave(): Promise<void> {
    const offer = this.replyOffer();
    if (!offer) {
      return;
    }
    this.clearReplyOffer();
    await this.post('WAVE', this.nearest(offer.x, offer.y), true);
  }

  /** Opens the picker over the button that was pressed, or closes it when it is open already. */
  togglePickerAt(event: Event): void {
    if (this.pickerAnchor()) {
      this.pickerAnchor.set(null);
      return;
    }
    const target = event.currentTarget as HTMLElement | null;
    const rect = target?.getBoundingClientRect();
    if (!rect) {
      return;
    }
    this.pickerAnchor.set({right: Math.max(4, window.innerWidth - rect.right), bottom: window.innerHeight - rect.top + 8});
  }

  closePicker(): void {
    if (this.pickerAnchor()) {
      this.pickerAnchor.set(null);
    }
  }

  dismissReply(): void {
    this.clearReplyOffer();
  }

  /** From the server, through the chat cockpit bridge: every emote of every player on the planet. */
  onEmote(event: EmoteEvent): void {
    const glyph = emoteGlyph(event.emote);
    if (!glyph) {
      return;
    }
    const item = this.renderService.getBabylonBaseItemById(event.syncItemId);
    if (!item) {
      // Not in view here: nothing to see, nothing to answer.
      return;
    }
    item.showEmote(glyph);
    const own = this.ownUnits().some(unit => unit.id === event.syncItemId);
    if (own) {
      return;
    }
    this.tracker.report('EMOTE_SEEN', 'emote=' + event.emote + (event.reply ? ',reply' : ''));
    if (event.emote === 'WAVE' && !event.reply && this.ownUnits().length > 0) {
      this.offerReply({name: event.name ?? 'Somebody', glyph, x: event.x, y: event.y});
    }
  }

  private offerReply(offer: ReplyOffer): void {
    this.zone.run(() => this.replyOffer.set(offer));
    this.tracker.report('EMOTE_REPLY_OFFERED');
    if (this.replyTimer) {
      clearTimeout(this.replyTimer);
    }
    this.replyTimer = setTimeout(() => this.clearReplyOffer(), REPLY_OFFER_MILLIS);
  }

  private clearReplyOffer(): void {
    if (this.replyTimer) {
      clearTimeout(this.replyTimer);
      this.replyTimer = null;
    }
    this.zone.run(() => this.replyOffer.set(null));
  }

  private async post(id: EmoteId, unit: NativeSyncBaseItemTickInfo | undefined, reply: boolean): Promise<void> {
    if (!unit || this.sending()) {
      return;
    }
    this.sending.set(true);
    try {
      await firstValueFrom(this.httpClient.post<{ result: string }>('/rest/emote', {emote: id, syncItemId: unit.id, reply}));
      this.tracker.report('EMOTE_SENT', 'emote=' + id + ',from=' + (reply ? 'reply' : 'button'));
    } catch (e) {
      console.warn('emote', e);
    } finally {
      this.sending.set(false);
    }
  }

  private ownUnits(): NativeSyncBaseItemTickInfo[] {
    return this.gwtAngularService.gwtAngularFacade?.baseItemUiService?.getMyOwnSyncItemTickInfos() ?? [];
  }

  /** The own unit nearest the point - one that drives if there is any, a building only otherwise. */
  private nearest(x: number, y: number): NativeSyncBaseItemTickInfo | undefined {
    const units = this.ownUnits();
    const movable = units.filter(unit => this.isMovable(unit));
    let best: NativeSyncBaseItemTickInfo | undefined;
    let bestDistance = Infinity;
    for (const unit of movable.length > 0 ? movable : units) {
      const distance = Math.hypot(unit.x - x, unit.y - y);
      if (distance < bestDistance) {
        bestDistance = distance;
        best = unit;
      }
    }
    return best;
  }

  private isMovable(unit: NativeSyncBaseItemTickInfo): boolean {
    try {
      return this.gwtAngularService.gwtAngularFacade.itemTypeService.getBaseItemTypeAngular(unit.itemTypeId)
        .getPhysicalAreaConfig().fulfilledMovable();
    } catch (e) {
      return false;
    }
  }
}
