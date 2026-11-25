import {Component, effect, inject, Injector, NgZone, OnDestroy, OnInit, signal} from '@angular/core';
import {EMOTES, EmoteDef, EmoteService} from './emote.service';
import {CompactLayoutService} from '../cockpit/compact-layout.service';

/**
 * The emote button and, when somebody waves at the player, the one-tap answer. The button opens
 * the five signs, upwards from the corner of the mini map; the picker says which unit will show it
 * (EmoteService.sender: the selected one, else the nearest unit that drives).
 */
@Component({
  selector: 'emote-bar',
  template: `
    @if (emoteService.replyOffer(); as offer) {
      <div class="emote-reply" role="status">
        <span class="emote-reply-text"><b>{{ offer.name }}</b> waves at you {{ offer.glyph }}</span>
        <button type="button" class="emote-reply-btn" (click)="emoteService.replyWave()">👋 Wave back</button>
        <button type="button" class="emote-reply-close" aria-label="Close" (click)="emoteService.dismissReply()">✕</button>
      </div>
    }
    <!-- On a phone an open panel (the build menu above all, with the first quest's tip on it)
         has the space; the button waits until it is closed. -->
    @if (emoteService.pickerAnchor(); as anchor) {
      <div class="emote-picker emote-picker-anchored" [style.right.px]="anchor.right" [style.bottom.px]="anchor.bottom">
        @if (senderName()) {
          <span class="emote-sender">{{ senderName() }} waves</span>
        }
        @for (emote of emotes; track emote.id) {
          <button type="button" class="emote-pick" [attr.aria-label]="emote.label" [title]="emote.label"
                  [disabled]="emoteService.sending()" (click)="pickAnchored(emote)">{{ emote.glyph }}</button>
        }
      </div>
    }
    @if (hasUnit() && !(compactLayout.compact() && compactLayout.openPanel())) {
      <div class="emote-dock">
        @if (open()) {
          <div class="emote-picker">
            @if (senderName()) {
              <span class="emote-sender">{{ senderName() }} waves</span>
            }
            @for (emote of emotes; track emote.id) {
              <button type="button" class="emote-pick" [attr.aria-label]="emote.label" [title]="emote.label"
                      [disabled]="emoteService.sending()" (click)="pick(emote)">{{ emote.glyph }}</button>
            }
          </div>
        }
        <button type="button" class="emote-toggle" [class.emote-toggle-open]="open()"
                aria-label="Emotes" title="Wave to other players" (click)="toggle()">👋</button>
      </div>
    }
  `,
  styleUrl: './emote-bar.component.scss'
})
export class EmoteBarComponent implements OnInit, OnDestroy {
  readonly emoteService = inject(EmoteService);
  readonly compactLayout = inject(CompactLayoutService);
  private readonly zone = inject(NgZone);
  private readonly injector = inject(Injector);
  readonly emotes = EMOTES;
  readonly open = signal(false);
  /** Asked of the engine every two seconds, not on every change detection. */
  readonly hasUnit = signal(false);
  /** Who will show the sign, said in the picker - read when it opens, not on every change detection. */
  readonly senderName = signal<string | null>(null);
  private timer: ReturnType<typeof setInterval> | null = null;

  ngOnInit(): void {
    this.refresh();
    // Says who waves when the picker opens from the unit panel too.
    effect(() => {
      if (this.emoteService.pickerAnchor()) {
        this.senderName.set(this.emoteService.senderName());
      }
    }, {injector: this.injector});
    this.zone.runOutsideAngular(() => this.timer = setInterval(() => this.refresh(), 2000));
  }

  ngOnDestroy(): void {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  toggle(): void {
    if (!this.open()) {
      this.senderName.set(this.emoteService.senderName());
    }
    this.open.set(!this.open());
  }

  pick(emote: EmoteDef): void {
    this.open.set(false);
    this.emoteService.send(emote.id);
  }

  pickAnchored(emote: EmoteDef): void {
    this.emoteService.closePicker();
    this.emoteService.send(emote.id);
  }

  private refresh(): void {
    // The anchored picker belongs to the unit panel: gone with it.
    if (this.emoteService.pickerAnchor() && !this.compactLayout.isOpen('item')) {
      this.zone.run(() => this.emoteService.closePicker());
    }
    const has = this.emoteService.hasOwnUnit();
    if (has !== this.hasUnit()) {
      this.zone.run(() => this.hasUnit.set(has));
    }
  }
}
