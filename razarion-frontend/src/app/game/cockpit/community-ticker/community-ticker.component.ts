import {ChangeDetectionStrategy, Component, NgZone, OnDestroy, OnInit, Signal, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';
import {CommunityTickerControllerClient, CommunityTickerEntry} from '../../../generated/razarion-share';
import {TypescriptGenerator} from '../../../backend/typescript-generator';
import {tickerAge, tickerText} from './community-ticker-text';
import {Cheer, IslandGoal, PresenceService} from '../../presence/presence.service';
import {FirstInteractionTrackerService} from '../../tracking/first-interaction-tracker.service';
import {EmoteId, emoteGlyph} from '../../emote/emote.service';

interface TickerLine {
  seq: number;
  text: string;
  age: string | null;
  leaving: boolean;
  /** WAVE or GG while the player the line is about has the game open (CheerService), else null. */
  cheer: string | null;
  /** Razarion this player may earn when the newcomer answers, else null. */
  cheerBonus: number | null;
  cheerState: 'none' | 'sending' | 'sent' | 'failed';
}

/** A greeting that reached this player, shown for a while like a line. */
interface CheerLine {
  key: number;
  cheer: Cheer;
  leaving: boolean;
  answered: boolean;
}

export function cheerGlyph(emote: string): string {
  return emoteGlyph(emote as EmoteId) ?? '👋';
}

/** "Jade-63 waves at you", "Jade-63 waved back · +100 Razarion for the welcome". */
export function cheerText(cheer: Cheer): string {
  const verb = cheer.emote === 'GG'
    ? (cheer.reply ? 'says GG back' : 'says GG to you')
    : (cheer.reply ? 'waved back' : 'waves at you');
  return `${cheer.from} ${verb}` + (cheer.bonus > 0 ? ` · +${cheer.bonus} Razarion for the welcome` : '');
}

/**
 * A few lines over the game field of what other players just did. One to three players are on at
 * the same time (PROD, 10/2026); without this nobody sees that the planet is shared.
 * <p>
 * A line about a player who has the game open carries a button: 👋 to somebody who landed, GG to
 * somebody who got on. It greets that player wherever the two are (CheerService on the server) -
 * the signs over units reach only who has the unit in view, and the newcomers are seldom that close
 * (2026-11-25..27: 60 signs, one ever seen outside the operator's visits). Greetings for this player
 * come with the presence poll and show here, with one tap to answer. Above the lines, the players
 * still on the noob island see the island goal: how many reached the mainland today.
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
  private static readonly CHEER_MS = 15_000;
  private static readonly LEAVE_MS = 600;
  private static readonly MAX_LINES = 4;
  /** From before the player came: only a few, so the field does not fill with history. As old as the
   server keeps (6 h): with one to three players on, a shorter look back leaves it empty for most. */
  private static readonly FIRST_BACKLOG = 3;
  private static readonly FIRST_BACKLOG_MAX_AGE_MS = 6 * 3_600_000;
  readonly lines = signal<TickerLine[]>([]);
  readonly cheers = signal<CheerLine[]>([]);
  readonly island: Signal<IslandGoal | null>;
  private readonly client: CommunityTickerControllerClient;
  private readonly queue: CommunityTickerEntry[] = [];
  private readonly timers: ReturnType<typeof setTimeout>[] = [];
  private pollTimer: ReturnType<typeof setTimeout> | null = null;
  private showTimer: ReturnType<typeof setInterval> | null = null;
  private lastSeq = 0;
  private firstPoll = true;
  private destroyed = false;
  private nextCheerKey = 1;
  private islandReported = false;

  constructor(private httpClient: HttpClient,
              private zone: NgZone,
              private presenceService: PresenceService,
              private tracker: FirstInteractionTrackerService) {
    this.client = new CommunityTickerControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    this.island = presenceService.island;
  }

  ngOnInit(): void {
    this.presenceService.onCheers(cheers => this.receive(cheers));
    this.zone.runOutsideAngular(() => {
      this.pollTimer = setTimeout(() => this.poll(), CommunityTickerComponent.FIRST_POLL_MS);
      this.showTimer = setInterval(() => this.showNext(), CommunityTickerComponent.SHOW_EVERY_MS);
    });
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.presenceService.onCheers(null);
    if (this.pollTimer) {
      clearTimeout(this.pollTimer);
    }
    if (this.showTimer) {
      clearInterval(this.showTimer);
    }
    this.timers.forEach(timer => clearTimeout(timer));
  }

  /** The island goal's bar, 0..100. */
  islandPercent(goal: IslandGoal): number {
    return Math.min(100, Math.round(100 * goal.crossedToday / Math.max(1, goal.goal)));
  }

  /** Reported once, the first time the goal is on screen. */
  islandShown(): boolean {
    if (!this.islandReported) {
      this.islandReported = true;
      this.tracker.report('ISLAND_GOAL_SHOWN');
    }
    return true;
  }

  glyph(emote: string): string {
    return cheerGlyph(emote);
  }

  text(cheer: Cheer): string {
    return cheerText(cheer);
  }

  /** One tap on a line: greets the player it is about. */
  greet(line: TickerLine): void {
    if (!line.cheer || line.cheerState !== 'none') {
      return;
    }
    this.setCheerState(line.seq, 'sending');
    this.tracker.report('CHEER_SENT', `emote=${line.cheer},from=ticker`);
    this.post({tickerSeq: line.seq})
      .then(result => this.setCheerState(line.seq, result === 'SENT' || result === 'ALREADY' ? 'sent' : 'failed'))
      .catch(() => this.setCheerState(line.seq, 'failed'));
  }

  /** One tap on a greeting: greets back. */
  answer(line: CheerLine): void {
    if (line.answered || line.cheer.reply) {
      return;
    }
    this.cheers.update(lines => lines.map(other => other.key === line.key ? {...other, answered: true} : other));
    this.tracker.report('CHEER_SENT', `emote=${line.cheer.emote},from=reply`);
    this.post({replyTo: line.cheer.id}).catch(() => {
      // An answer that did not go out keeps its tick - there is nothing better to offer.
    });
  }

  private post(body: { tickerSeq?: number, replyTo?: number }): Promise<string> {
    return firstValueFrom(this.httpClient.post<{ result: string }>('/rest/cheer', body))
      .then(response => response?.result);
  }

  private setCheerState(seq: number, state: TickerLine['cheerState']): void {
    this.lines.update(lines => lines.map(line => line.seq === seq ? {...line, cheerState: state} : line));
  }

  private receive(cheers: Cheer[]): void {
    for (const cheer of cheers) {
      this.tracker.report('CHEER_SEEN', `emote=${cheer.emote},reply=${cheer.reply ? 'yes' : 'no'},bonus=${cheer.bonus > 0 ? 'yes' : 'no'}`);
      const line: CheerLine = {key: this.nextCheerKey++, cheer, leaving: false, answered: false};
      this.cheers.update(lines => [...lines, line].slice(-3));
      this.later(() => {
        this.cheers.update(lines => lines.map(other => other.key === line.key ? {...other, leaving: true} : other));
        this.later(() => this.cheers.update(lines => lines.filter(other => other.key !== line.key)), CommunityTickerComponent.LEAVE_MS);
      }, CommunityTickerComponent.CHEER_MS);
    }
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
    const line: TickerLine = {
      seq: entry.seq,
      text: tickerText(entry)!,
      age: tickerAge(entry.timeMillis, Date.now()),
      leaving: false,
      // A queued line's player may have gone meanwhile; the server checks again on the tap.
      cheer: entry.cheer ?? null,
      cheerBonus: entry.cheerBonus ?? null,
      cheerState: 'none'
    };
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
