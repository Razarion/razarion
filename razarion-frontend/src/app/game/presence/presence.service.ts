import {Injectable, NgZone, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';

export interface OnlinePlayer {
  name: string;
  /** Their base on this planet, or null while they have none. */
  baseId: number | null;
}

export interface Presence {
  /** Everybody else with the game open - more than {@link players} lists once there are many. */
  count: number;
  players: OnlinePlayer[];
}

const POLL_MILLIS = 20000;

/**
 * Who else is playing right now (PresenceController on the server): the count in the top bar and
 * the names on the mini map. The others were there - in the first ten minutes of 91 % of new
 * players somebody else had the game open (2026-11-18..24) - but nothing said so, and the players
 * who wrote "hi" into the chat to find them got no answer.
 */
@Injectable({
  providedIn: 'root'
})
export class PresenceService {
  readonly presence = signal<Presence>({count: 0, players: []});
  private onlineBaseIds = new Map<number, string>();
  private timer: ReturnType<typeof setInterval> | null = null;

  constructor(private httpClient: HttpClient, private zone: NgZone) {
  }

  start(): void {
    if (this.timer) {
      return;
    }
    this.poll();
    // Outside Angular: a timer every 20 s inside the zone would run change detection for nothing.
    this.zone.runOutsideAngular(() => this.timer = setInterval(() => this.poll(), POLL_MILLIS));
  }

  stop(): void {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }

  /** "Cobalt-31, Eagle-62 and 3 more are playing" - the tooltip of the online count. */
  namesText(): string {
    const {count, players} = this.presence();
    const shown = players.slice(0, 6).map(p => p.name);
    const more = count - shown.length;
    if (shown.length === 0) {
      return count + ' other players are online';
    }
    return shown.join(', ') + (more > 0 ? ' and ' + more + ' more' : '') + (count === 1 ? ' is playing right now' : ' are playing right now');
  }

  /** The name of the online player who owns this base, or undefined when nobody online does. */
  onlineName(baseId: number): string | undefined {
    return this.onlineBaseIds.get(baseId);
  }

  private async poll(): Promise<void> {
    try {
      const presence = await firstValueFrom(this.httpClient.get<Presence>('/rest/presence'));
      this.apply(presence);
    } catch (e) {
      // Nice to have, never a condition: the game goes on without it.
    }
  }

  apply(presence: Presence | null | undefined): void {
    const players = presence?.players ?? [];
    const ids = new Map<number, string>();
    for (const player of players) {
      if (player.baseId != null) {
        ids.set(player.baseId, player.name);
      }
    }
    this.onlineBaseIds = ids;
    this.zone.run(() => this.presence.set({count: presence?.count ?? 0, players}));
  }
}
