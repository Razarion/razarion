import {Component, Input, OnChanges} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CommonModule, DatePipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {TableModule} from 'primeng/table';
import {ButtonModule} from 'primeng/button';
import {ToggleSwitch} from 'primeng/toggleswitch';
import {RippleModule} from 'primeng/ripple';
import {
  PlayerSessionInfo,
  PlayerSessionOutcome,
  TrackerControllerImplClient,
  TrackingPlatform
} from '../../generated/razarion-share';
import {TypescriptGenerator} from '../typescript-generator';

/**
 * What the range contains, for the line above the table. Answering "what went on in the last 24
 * hours" should not require scrolling.
 */
interface SessionSummary {
  attempts: number;
  successful: number;
  aborted: number;
  noStartup: number;
  players: number;
  withBase: number;
}

/**
 * The history view: one row per attempt to start the game.
 * <p>
 * The attempt is the row and not the player, because an attempt has exactly one point in time and
 * this is a timeline. Someone who started three times is three rows - which is also the only way to
 * see that the second attempt followed a failed one. The player is a column, with the attempt
 * counter next to it.
 */
@Component({
  selector: 'player-sessions',
  imports: [CommonModule, DatePipe, FormsModule, TableModule, ButtonModule, ToggleSwitch, RippleModule],
  templateUrl: './player-sessions.component.html'
})
export class PlayerSessionsComponent implements OnChanges {
  @Input() fromDate!: Date;
  @Input() toDate!: Date;

  sessions: PlayerSessionInfo[] = [];
  loading = false;
  /** Hides everything that started fine - the failures are what this view is for. */
  failuresOnly = false;
  summary: SessionSummary = {attempts: 0, successful: 0, aborted: 0, noStartup: 0, players: 0, withBase: 0};
  private trackerControllerImplClient: TrackerControllerImplClient;

  constructor(httpClient: HttpClient) {
    this.trackerControllerImplClient =
      new TrackerControllerImplClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnChanges(): void {
    this.load();
  }

  load(): void {
    if (!this.fromDate || !this.toDate) {
      return;
    }
    this.loading = true;
    this.trackerControllerImplClient.loadPlayerSessions({fromDate: this.fromDate, toDate: this.toDate})
      .then(sessions => {
        this.sessions = sessions;
        this.summary = PlayerSessionsComponent.summarize(sessions);
        this.loading = false;
      })
      .catch(reason => {
        this.loading = false;
        console.error(reason);
      });
  }

  get rows(): PlayerSessionInfo[] {
    return this.failuresOnly
      ? this.sessions.filter(session => session.outcome !== PlayerSessionOutcome.SUCCESSFUL)
      : this.sessions;
  }

  outcomeLabel(session: PlayerSessionInfo): string {
    switch (session.outcome) {
      case PlayerSessionOutcome.SUCCESSFUL:
        return 'started';
      case PlayerSessionOutcome.FAILED:
        return 'failed';
      case PlayerSessionOutcome.ABORTED:
        return session.tabbedAway ? 'tabbed away' : 'abandoned';
      case PlayerSessionOutcome.RUNNING:
        return 'starting';
      case PlayerSessionOutcome.NO_STARTUP:
        return 'no startup';
    }
  }

  /** Green for a start that worked, red for one that did not, grey for one still in flight. */
  outcomeColor(session: PlayerSessionInfo): string {
    switch (session.outcome) {
      case PlayerSessionOutcome.SUCCESSFUL:
        return '#63dba9';
      case PlayerSessionOutcome.RUNNING:
        return '#737373';
      default:
        return '#ff7f8a';
    }
  }

  outcomeTitle(session: PlayerSessionInfo): string {
    switch (session.outcome) {
      case PlayerSessionOutcome.SUCCESSFUL:
        return 'The startup reported its own successful end';
      case PlayerSessionOutcome.FAILED:
        return 'The startup ran to its end and reported a failure';
      case PlayerSessionOutcome.ABORTED:
        return session.tabbedAway
          ? `The player switched tabs while the game was still loading (stuck at ${session.lastTaskEnum})`
          : `Nobody ever reported an end - the player left or a task never returned (stuck at ${session.lastTaskEnum})`;
      case PlayerSessionOutcome.RUNNING:
        return 'Still booting; too young to call it abandoned';
      case PlayerSessionOutcome.NO_STARTUP:
        return 'The game page was opened and not a single startup task arrived - the browser died before the first one could report';
    }
  }

  sourceLabel(session: PlayerSessionInfo): string {
    switch (session.source) {
      case TrackingPlatform.REDDIT:
        return 'Reddit';
      case TrackingPlatform.X:
        return 'X';
      default:
        return 'organic';
    }
  }

  /**
   * Browser and form factor, which is what a failed startup is usually about - WebAssembly GC
   * needs Chrome 119+, Firefox 120+ or Safari 18.2+. The full string is in the expanded row.
   */
  device(session: PlayerSessionInfo): string {
    const userAgent = session.userAgent;
    if (!userAgent) {
      return '—';
    }
    const mobile = /Mobile|Android|iPhone|iPad/.test(userAgent);
    // Order matters: Edge and Chrome both claim Chrome, Chrome claims Safari.
    const browser = /Edg\//.test(userAgent) ? 'Edge'
      : /OPR\//.test(userAgent) ? 'Opera'
        : /Chrome\//.test(userAgent) ? 'Chrome'
          : /Firefox\//.test(userAgent) ? 'Firefox'
            : /Safari\//.test(userAgent) ? 'Safari'
              : 'other';
    const version = PlayerSessionsComponent.version(userAgent, browser);
    return `${browser}${version ? ' ' + version : ''}${mobile ? ' mobile' : ''}`;
  }

  player(session: PlayerSessionInfo): string {
    if (session.name) {
      return session.name;
    }
    if (session.userId) {
      // The full id is in the expanded row; a table of uuids reads like noise.
      return session.userId.substring(0, 8);
    }
    return '—';
  }

  playerTitle(session: PlayerSessionInfo): string {
    if (!session.userId) {
      return 'This attempt could not be tied to a player - it predates the server stamping the user onto startup records';
    }
    return session.userId + (session.name ? '' : ' (never set a name)');
  }

  duration(millis: number | null | undefined): string {
    if (millis === null || millis === undefined) {
      return '—';
    }
    return millis < 1000 ? `${millis} ms` : `${(millis / 1000).toFixed(1)} s`;
  }

  private static version(userAgent: string, browser: string): string {
    const pattern = browser === 'Edge' ? /Edg\/(\d+)/
      : browser === 'Opera' ? /OPR\/(\d+)/
        : browser === 'Chrome' ? /Chrome\/(\d+)/
          : browser === 'Firefox' ? /Firefox\/(\d+)/
            : browser === 'Safari' ? /Version\/(\d+)/
              : null;
    const match = pattern ? userAgent.match(pattern) : null;
    return match ? match[1] : '';
  }

  private static summarize(sessions: PlayerSessionInfo[]): SessionSummary {
    const players = new Set<string>();
    const withBase = new Set<string>();
    sessions.forEach(session => {
      if (session.userId) {
        players.add(session.userId);
        if (session.baseCreated || session.baseAlive) {
          withBase.add(session.userId);
        }
      }
    });
    return {
      attempts: sessions.length,
      successful: sessions.filter(session => session.outcome === PlayerSessionOutcome.SUCCESSFUL).length,
      aborted: sessions.filter(session => session.outcome === PlayerSessionOutcome.ABORTED).length,
      noStartup: sessions.filter(session => session.outcome === PlayerSessionOutcome.NO_STARTUP).length,
      players: players.size,
      withBase: withBase.size
    };
  }
}
