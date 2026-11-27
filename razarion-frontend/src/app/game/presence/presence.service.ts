import {Injectable, NgZone, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {firstValueFrom} from 'rxjs';

export interface OnlinePlayer {
  name: string;
  /** Their base on this planet, or null while they have none. */
  baseId: number | null;
}

/** A greeting another player sent this one from the community ticker (CheerService on the server). */
export interface Cheer {
  id: number;
  from: string;
  /** WAVE or GG. */
  emote: string;
  /** It answers a greeting of this player. */
  reply: boolean;
  /** Razarion this player was paid for having welcomed the sender, who answered - else 0. */
  bonus: number;
}

/** How many crossed from the noob island to the mainland today, shown to the players still on it. */
export interface IslandGoal {
  crossedToday: number;
  goal: number;
}

export interface Presence {
  /** Everybody else with the game open - more than {@link players} lists once there are many. */
  count: number;
  players: OnlinePlayer[];
  /** Handed out once: the next poll does not repeat them. */
  cheers?: Cheer[];
  /** Null for a player who has left the noob island. */
  island?: IslandGoal | null;
  /** This player's own name, as the others see it over the units and in the chat. */
  me?: string | null;
}

/** 12 s rather than 20: a greeting comes with the poll, and the answer to it should not wait long. */
const POLL_MILLIS = 12000;

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
  readonly island = signal<IslandGoal | null>(null);
  /** This player's own name - nothing else in the game shows it. */
  readonly me = signal<string | null>(null);
  private cheerListener: ((cheers: Cheer[]) => void) | null = null;
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

  /** Who is told of the greetings that come with a poll (the community ticker). */
  onCheers(listener: ((cheers: Cheer[]) => void) | null): void {
    this.cheerListener = listener;
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
    this.zone.run(() => {
      this.presence.set({count: presence?.count ?? 0, players});
      this.island.set(presence?.island ?? null);
      this.me.set(presence?.me ?? null);
      const cheers = presence?.cheers ?? [];
      if (cheers.length > 0 && this.cheerListener) {
        this.cheerListener(cheers);
      }
    });
  }
}
