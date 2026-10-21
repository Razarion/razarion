import {ChangeDetectionStrategy, Component, NgZone, OnDestroy, OnInit, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CommunityTickerControllerClient, CommunityTickerEntry} from '../../../generated/razarion-share';
import {TypescriptGenerator} from '../../../backend/typescript-generator';
import {tickerAge, tickerText} from './community-ticker-text';

interface TickerLine {
  seq: number;
  text: string;
  age: string | null;
  leaving: boolean;
}

/**
 * A few lines over the game field of what other players just did. One to three players are on at
 * the same time (PROD, 10/2026); without this nobody sees that the planet is shared.
 * <p>
 * Timers run outside Angular and the state is in signals: change detection over the whole cockpit
 * cost 40 % of a phone's main thread once (2026-10-12), and this ticks forever.
 */
@Component({
  selector: 'community-ticker',
  templateUrl: './community-ticker.component.html',
  styleUrl: './community-ticker.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CommunityTickerComponent implements OnInit, OnDestroy {
  private static readonly FIRST_POLL_MS = 8_000;
  private static readonly POLL_MS = 20_000;
  /** One new line at most this often, however many arrived. */
  private static readonly SHOW_EVERY_MS = 6_000;
  private static readonly LINE_MS = 30_000;
  private static readonly LEAVE_MS = 600;
  private static readonly MAX_LINES = 4;
  /** From before the player came: only a few, so the field does not fill with history. As old as the
   server keeps (6 h): with one to three players on, a shorter look back leaves it empty for most. */
  private static readonly FIRST_BACKLOG = 3;
  private static readonly FIRST_BACKLOG_MAX_AGE_MS = 6 * 3_600_000;
  readonly lines = signal<TickerLine[]>([]);
  private readonly client: CommunityTickerControllerClient;
  private readonly queue: CommunityTickerEntry[] = [];
  private readonly timers: ReturnType<typeof setTimeout>[] = [];
  private pollTimer: ReturnType<typeof setTimeout> | null = null;
  private showTimer: ReturnType<typeof setInterval> | null = null;
  private lastSeq = 0;
  private firstPoll = true;
  private destroyed = false;

  constructor(httpClient: HttpClient, private zone: NgZone) {
    this.client = new CommunityTickerControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.zone.runOutsideAngular(() => {
      this.pollTimer = setTimeout(() => this.poll(), CommunityTickerComponent.FIRST_POLL_MS);
      this.showTimer = setInterval(() => this.showNext(), CommunityTickerComponent.SHOW_EVERY_MS);
    });
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    if (this.pollTimer) {
      clearTimeout(this.pollTimer);
    }
    if (this.showTimer) {
      clearInterval(this.showTimer);
    }
    this.timers.forEach(timer => clearTimeout(timer));
  }

  private poll(): void {
    this.client.recent({after: this.lastSeq})
      .then(entries => this.take(entries ?? []))
      .catch(() => {
        // A ticker that misses a round says nothing; the next poll asks for the same range again.
      })
      .finally(() => {
        if (!this.destroyed) {
          this.pollTimer = setTimeout(() => this.poll(), CommunityTickerComponent.POLL_MS);
        }
      });
  }

  private take(entries: CommunityTickerEntry[]): void {
    if (entries.length > 0) {
      this.lastSeq = Math.max(this.lastSeq, ...entries.map(entry => entry.seq));
    }
    let fresh = entries;
    if (this.firstPoll) {
      this.firstPoll = false;
      const now = Date.now();
      fresh = entries
        .filter(entry => now - entry.timeMillis < CommunityTickerComponent.FIRST_BACKLOG_MAX_AGE_MS)
        .slice(-CommunityTickerComponent.FIRST_BACKLOG);
    }
    this.queue.push(...fresh.filter(entry => tickerText(entry) !== null));
    // A backlog after a long pause is not news any more: keep the newest few.
    if (this.queue.length > CommunityTickerComponent.MAX_LINES * 2) {
      this.queue.splice(0, this.queue.length - CommunityTickerComponent.MAX_LINES * 2);
    }
  }

  private showNext(): void {
    const entry = this.queue.shift();
    if (!entry) {
      return;
    }
    const line: TickerLine = {seq: entry.seq, text: tickerText(entry)!, age: tickerAge(entry.timeMillis, Date.now()), leaving: false};
    this.lines.update(lines => [...lines, line].slice(-CommunityTickerComponent.MAX_LINES));
    this.later(() => {
      this.lines.update(lines => lines.map(other => other.seq === line.seq ? {...other, leaving: true} : other));
      this.later(() => this.lines.update(lines => lines.filter(other => other.seq !== line.seq)), CommunityTickerComponent.LEAVE_MS);
    }, CommunityTickerComponent.LINE_MS);
  }

  private later(action: () => void, ms: number): void {
    const timer = setTimeout(() => {
      this.timers.splice(this.timers.indexOf(timer), 1);
      action();
    }, ms);
    this.timers.push(timer);
  }
}
