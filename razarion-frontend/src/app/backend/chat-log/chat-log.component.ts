import {Component, Input, OnChanges, OnDestroy} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CommonModule, DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {TableModule} from 'primeng/table';
import {ToggleSwitch} from 'primeng/toggleswitch';

/** One row of /rest/chat-controller/admin (ChatService.Entry). userId is null for the assistant. */
interface ChatEntry {
  id: number;
  timestamp: string | number;
  userId: string | null;
  userName: string;
  message: string;
  instagramUrl: string | null;
}

const ASSISTANT_NAME = 'Razarion AI';
const NEWS_NAME = 'Razarion';
const LIVE_MS = 15_000;

/**
 * The planet chat as the players see it, for watching it: who wrote what, the assistant's answers
 * in between. What the assistant made of a message that it did not answer is only in the recorder
 * (its chat_log), the server holds just what was posted.
 */
@Component({
  selector: 'chat-log',
  imports: [CommonModule, DatePipe, FormsModule, TableModule, ToggleSwitch],
  templateUrl: './chat-log.component.html'
})
export class ChatLogComponent implements OnChanges, OnDestroy {
  @Input() fromDate!: Date;
  @Input() toDate!: Date;

  entries: ChatEntry[] = [];
  loading = false;
  error: string | null = null;
  /** Polls with the span ending now, so new messages show up without touching the date fields. */
  live = false;
  private timer: any;

  constructor(private httpClient: HttpClient) {
  }

  ngOnChanges(): void {
    this.load();
  }

  ngOnDestroy(): void {
    clearInterval(this.timer);
  }

  onLiveChange(): void {
    clearInterval(this.timer);
    if (this.live) {
      this.load();
      this.timer = setInterval(() => this.load(), LIVE_MS);
    }
  }

  load(): void {
    if (!this.fromDate || !this.toDate) {
      return;
    }
    const to = this.live ? Math.max(Date.now(), this.toDate.getTime()) : this.toDate.getTime();
    this.loading = true;
    this.httpClient.get<ChatEntry[]>('/rest/chat-controller/admin', {
      params: {from: this.fromDate.getTime(), to}
    }).subscribe({
      next: entries => {
        this.entries = entries;
        this.error = null;
        this.loading = false;
      },
      error: reason => {
        this.loading = false;
        this.error = `Could not load the chat (${reason.status ?? reason})`;
        console.error(reason);
      }
    });
  }

  isAssistant(entry: ChatEntry): boolean {
    return !entry.userId && entry.userName === ASSISTANT_NAME;
  }

  /** Posted by the planet itself: a player left the noob island. */
  isNews(entry: ChatEntry): boolean {
    return !entry.userId && entry.userName === NEWS_NAME;
  }

  get news(): number {
    return this.entries.filter(e => this.isNews(e)).length;
  }

  get players(): number {
    return new Set(this.entries.filter(e => e.userId).map(e => e.userId)).size;
  }

  get answers(): number {
    return this.entries.filter(e => this.isAssistant(e)).length;
  }
}
