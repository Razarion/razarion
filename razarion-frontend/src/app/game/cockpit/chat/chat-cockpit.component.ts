import {AfterViewInit, Component, ElementRef, NgZone, OnDestroy, ViewChild} from '@angular/core';
import {TableModule} from 'primeng/table';
import {ChatControllerClient, ChatMessage} from '../../../generated/razarion-share';
import {Button} from 'primeng/button';
import {InputText} from 'primeng/inputtext';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

import {ScrollPanel, ScrollPanelModule} from 'primeng/scrollpanel';
import {HttpClient} from '@angular/common/http';
import {TypescriptGenerator} from '../../../backend/typescript-generator';
import {ChatCockpit, ChatMessage as GwtAngularChatMessage} from '../../../gwtangular/GwtAngularFacade';
import {CompactLayoutService} from '../compact-layout.service';
import {FirstInteractionTrackerService} from '../../tracking/first-interaction-tracker.service';

/**
 * A post or reel on Instagram, as the server keeps it (ChatService.normalizeInstagramUrl). Checked
 * here as well: the link becomes an attribute of Instagram's embed, and only such a link may.
 */
const INSTAGRAM_URL = /^https:\/\/www\.instagram\.com\/(reel|p)\/([A-Za-z0-9_-]{5,40})\/$/;
const INSTAGRAM_ORIGIN = 'https://www.instagram.com';

/**
 * Instagram's own embed page of a reel, caption included - what its embed.js would put into an
 * iframe. Null for a link the chat may not embed.
 */
export function reelEmbedUrl(url: string | null | undefined): string | null {
  const match = url ? INSTAGRAM_URL.exec(url) : null;
  return match ? `${INSTAGRAM_ORIGIN}/${match[1]}/${match[2]}/embed/captioned/` : null;
}

/** The shortcode of a reel link, or null when it is not one the chat may embed. */
export function reelShortcode(url: string | null | undefined): string | null {
  const match = url ? INSTAGRAM_URL.exec(url) : null;
  return match ? match[2] : null;
}

/** "14:05" for today, "10.11. 14:05" before that; empty without a time. */
export function chatTime(timestamp: number | null | undefined, now: Date = new Date()): string {
  if (!timestamp) {
    return '';
  }
  const date = new Date(timestamp);
  const two = (n: number) => String(n).padStart(2, '0');
  const time = `${two(date.getHours())}:${two(date.getMinutes())}`;
  return date.toDateString() === now.toDateString() ? time : `${two(date.getDate())}.${two(date.getMonth() + 1)}. ${time}`;
}

@Component({
  selector: 'chat-cockpit',
  imports: [
    TableModule,
    Button,
    InputText,
    ReactiveFormsModule,
    FormsModule,
    ScrollPanelModule
],
  templateUrl: './chat-cockpit.component.html',
  styleUrl: './chat-cockpit.component.scss'
})
export class ChatCockpitComponent implements ChatCockpit, AfterViewInit, OnDestroy {
  @ViewChild('scrollPanel')
  scrollPanel!: ScrollPanel;
  @ViewChild('chatMessageDiv')
  chatMessageDiv!: ElementRef<HTMLDivElement>;
  chatMessages: ChatMessage[] = [];
  messageToSend = ""
  sending = false;
  sendError: string | null = null;
  /**
   * The reel open over the game, as its link, or null. Nothing of Instagram is loaded before a
   * player taps the button: the embed and Instagram's script come in only then.
   */
  openReelUrl: string | null = null;
  private reelHost?: ElementRef<HTMLDivElement>;
  private chatControllerClient: ChatControllerClient;
  /**
   * No dot before the chat has loaded (10 s after the start): the dot says "there is something in
   * the chat", and before the load there is not. A live message in those seconds is in the load.
   */
  private loaded = false;
  /** The page went to the background while a reel was open, and has not come back yet. */
  private reelLeft = false;
  private readonly onVisibilityChange = () => this.reelVisibilityChanged();
  private readonly onWindowBlur = () => this.reelTouched();
  private readonly onEmbedMessage = (event: MessageEvent) => this.embedMessage(event);
  private reelFrame: HTMLIFrameElement | null = null;

  constructor(httpClient: HttpClient,
              private zone: NgZone,
              private compactLayout: CompactLayoutService,
              private tracker: FirstInteractionTrackerService) {
    this.chatControllerClient = new ChatControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  /** The overlay's host appears with the overlay; the embed goes in as soon as it is there. */
  @ViewChild('reelHost')
  set reelHostRef(ref: ElementRef<HTMLDivElement> | undefined) {
    this.reelHost = ref;
    if (ref && this.openReelUrl) {
      this.embedReel(ref.nativeElement, this.openReelUrl);
    }
  }

  onMessage(gwtAngularChatMessage: GwtAngularChatMessage): void {
    this.zone.run(() => {
      this.chatMessages.push({
        userName: gwtAngularChatMessage.userName,
        message: gwtAngularChatMessage.message,
        instagramUrl: gwtAngularChatMessage.instagramUrl!,
        timestamp: gwtAngularChatMessage.timestamp!
      });
      this.scrollToBottom();
      // Every message, the island announcements and their reels included: they show a new player
      // that the planet is alive and that the chat is where that is told.
      if (this.loaded) {
        this.compactLayout.markChatUnread();
      }
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.loadChatMessages();
    }, 10000);
  }

  ngOnDestroy(): void {
    this.stopWatchingReel();
  }

  private loadChatMessages() {
    this.chatControllerClient.getAllMessages()
      .then((chatMessages) => {
        this.chatMessages.length = 0;
        this.chatMessages = [...chatMessages];
        this.scrollToBottom();
        this.loaded = true;
        if (chatMessages.length > 0) {
          this.compactLayout.markChatUnread();
        }
      })
      .catch(error => {
        console.warn(error);
      });
  }

  /**
   * Everybody may write: a player without a name writes under their ticker call sign, e.g. "Falcon-42" (ChatService). It took an
   * account and a name before, and so the questions of the anonymous players never arrived.
   */
  onSend() {
    const text = this.messageToSend.trim();
    if (!text || this.sending) {
      return;
    }
    this.sending = true;
    this.sendError = null;
    this.chatControllerClient.send(text)
      .then(() => {
        this.messageToSend = "";
        this.scrollToBottom();
      })
      .catch(error => {
        this.sendError = error?.status === 429 ? 'Too many messages, please wait a moment' : 'Could not send, please try again';
        console.warn(error);
      })
      .finally(() => this.sending = false);
  }

  time(timestamp: number | null | undefined): string {
    return chatTime(timestamp);
  }

  hasReel(chatMessage: ChatMessage): boolean {
    return reelShortcode(chatMessage.instagramUrl) !== null;
  }

  /**
   * Opens the reel over the game in Instagram's own embed, its caption included. The embed has its
   * own ways out to Instagram (the profile, "more on Instagram", like, comment); they cannot be
   * switched off, so they are measured instead - see the REEL_* kinds of the interaction tracker.
   */
  openReel(url: string) {
    const shortcode = reelShortcode(url);
    if (!shortcode) {
      return;
    }
    this.openReelUrl = url;
    this.reelLeft = false;
    this.tracker.report('REEL_OPENED', shortcode);
    document.addEventListener('visibilitychange', this.onVisibilityChange);
    window.addEventListener('blur', this.onWindowBlur);
  }

  closeReel() {
    const shortcode = reelShortcode(this.openReelUrl);
    if (shortcode) {
      this.tracker.report('REEL_CLOSED', shortcode);
    }
    this.openReelUrl = null;
    this.stopWatchingReel();
  }

  private stopWatchingReel() {
    document.removeEventListener('visibilitychange', this.onVisibilityChange);
    window.removeEventListener('blur', this.onWindowBlur);
    window.removeEventListener('message', this.onEmbedMessage);
    this.reelFrame = null;
  }

  /**
   * Hidden with a reel open: in Instagram's or Facebook's browser that is the app taking over after
   * a tap inside the embed. Visible again: the player came back to the game.
   */
  private reelVisibilityChanged() {
    const shortcode = reelShortcode(this.openReelUrl);
    if (!shortcode) {
      return;
    }
    if (document.hidden) {
      this.reelLeft = true;
      this.tracker.report('REEL_LEFT', shortcode);
    } else if (this.reelLeft) {
      this.reelLeft = false;
      this.tracker.report('REEL_RETURNED', shortcode);
    }
  }

  /** The window loses the focus to Instagram's frame: the player tapped into the embed. */
  private reelTouched() {
    const shortcode = reelShortcode(this.openReelUrl);
    if (shortcode && document.activeElement instanceof HTMLIFrameElement) {
      this.tracker.report('REEL_TOUCHED', shortcode);
    }
  }

  /**
   * The reel in an iframe of Instagram's embed page, made here rather than by Instagram's embed.js:
   * the game page is cross-origin isolated (COEP credentialless, which the WASM client needs), and
   * there an iframe from another site only loads when it is credentialless itself - embed.js makes
   * one without, and it stays an empty white strip. Credentialless also means no Instagram cookies:
   * the player sees the reel logged out. The embed page reports its height (MEASURE), which the
   * frame then takes.
   */
  private embedReel(host: HTMLElement, url: string) {
    const src = reelEmbedUrl(url);
    host.replaceChildren();
    if (!src) {
      return;
    }
    const frame = document.createElement('iframe');
    // Before the source, or the first load goes out with credentials and is blocked.
    frame.setAttribute('credentialless', '');
    frame.allow = 'autoplay; encrypted-media; fullscreen; picture-in-picture';
    frame.title = 'Reel on Instagram';
    frame.style.cssText = 'display:block;width:100%;max-width:540px;min-width:300px;height:640px;border:0;border-radius:3px;background:#fff;';
    frame.src = src;
    this.reelFrame = frame;
    window.addEventListener('message', this.onEmbedMessage);
    host.appendChild(frame);
  }

  private embedMessage(event: MessageEvent) {
    if (event.origin !== INSTAGRAM_ORIGIN || !this.reelFrame || event.source !== this.reelFrame.contentWindow) {
      return;
    }
    try {
      const data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
      const height = Number(data?.details?.height);
      if (data?.type === 'MEASURE' && height > 0) {
        this.reelFrame.style.height = `${Math.ceil(height)}px`;
      }
    } catch (ignored) {
      // Not one of the embed's messages.
    }
  }

  /**
   * After the new message has been laid out, not merely rendered: measured at once, the height
   * still lacked the last message - with a two-line reel message and its button the newest one sat
   * just below the edge on a phone. The browser clamps an overshoot to the bottom.
   */
  private scrollToBottom() {
    setTimeout(() => requestAnimationFrame(() => {
      const chatMessageDivHeight = this.chatMessageDiv.nativeElement.offsetHeight;
      this.scrollPanel.scrollTop(chatMessageDivHeight + 1000);
    }), 0);
  }
}
