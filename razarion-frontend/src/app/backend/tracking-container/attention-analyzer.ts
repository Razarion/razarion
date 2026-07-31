import {
  StartupTerminatedJson,
  TabHiddenJson,
  TrackingContainer,
  UserActivity,
  UserActivityType
} from '../../generated/razarion-share';

/**
 * What the players did with their attention, which the startup tracking alone cannot say.
 *
 * A startup that finished counts as a success there even when nobody was watching it, and an
 * abandoned one looks the same whether the player closed the page or only switched tabs. Both
 * distinctions change what to do about them, so both are measured here.
 */

/** How a startup that never finished ended. */
export interface AbandonedStartups {
  /** The page only went to the background - the tab may well still be open. */
  tabbedAway: number;
  /** The page went away: closed, navigated off, browser gone. */
  leftPage: number;
  /**
   * Reconstructed from the startup tasks because no beacon ever arrived, so which of the two
   * happened is unknown. Counted apart rather than folded into either - guessing would make the
   * other two numbers say something the data does not.
   */
  unknown: number;
}

/** A slice of the wait between the page load and the moment the tab went to the background. */
export interface AttentionBucket {
  label: string;
  count: number;
}

/** Whether the players who got a running game actually watched it. */
export interface BackgroundTiming {
  successfulStartups: number;
  /** Successful startups that went to the background at least once afterwards. */
  backgrounded: number;
  /** Seconds from page load to backgrounding, median. Null when nothing was backgrounded. */
  medianSeconds: number | null;
  buckets: AttentionBucket[];
}

/**
 * One group of players and what became of them. Sessions rather than users is the unit that is
 * measured, but the outcomes are per user - the same person can start twice.
 */
export interface AttentionCohort {
  name: string;
  sessions: number;
  users: number;
  baseCreated: number;
  questPassed: number;
}

/** The whole picture, computed once per loaded range. */
export interface AttentionReport {
  abandoned: AbandonedStartups;
  timing: BackgroundTiming;
  cohorts: AttentionCohort[];
  /**
   * Successful startups whose session could not be traced to a user. Every session recorded
   * before GAME_SESSION_STARTED existed lands here, so an old range is unlinkable throughout -
   * shown rather than hidden, because a small cohort next to a large unlinked count means the
   * cohorts describe almost nobody.
   */
  unlinkedSessions: number;
}

const EMPTY_ABANDONED: AbandonedStartups = {tabbedAway: 0, leftPage: 0, unknown: 0};

export class AttentionAnalyzer {
  private trackingContainer?: TrackingContainer;

  setTrackingContainer(trackingContainer: TrackingContainer) {
    this.trackingContainer = trackingContainer;
  }

  createReport(): AttentionReport {
    return {
      abandoned: this.abandonedStartups(),
      timing: this.backgroundTiming(),
      cohorts: this.cohorts(),
      unlinkedSessions: this.unlinkedSessions()
    };
  }

  private abandonedStartups(): AbandonedStartups {
    const aborted = this.startupTerminatedJsons().filter(startupTerminatedJson => startupTerminatedJson.aborted);
    if (aborted.length === 0) {
      return EMPTY_ABANDONED;
    }
    return {
      // The generated type says boolean, but the field is nullable on the server and null is a
      // third case here, so the comparison has to be explicit rather than truthiness.
      tabbedAway: aborted.filter(startupTerminatedJson => this.hidden(startupTerminatedJson) === true).length,
      leftPage: aborted.filter(startupTerminatedJson => this.hidden(startupTerminatedJson) === false).length,
      unknown: aborted.filter(startupTerminatedJson => this.hidden(startupTerminatedJson) == null).length
    };
  }

  private backgroundTiming(): BackgroundTiming {
    const successful = this.successfulStartups();
    const successfulUuids = new Set(successful
      .map(startupTerminatedJson => startupTerminatedJson.gameSessionUuid)
      .filter(uuid => !!uuid));
    // Only the ones whose startup is in this range: a record whose session started before the
    // range began would be counted against a population it is not part of.
    const tabHiddenJsons = this.tabHiddenJsons()
      .filter(tabHiddenJson => successfulUuids.has(tabHiddenJson.gameSessionUuid));

    const backgroundedUuids = new Set(tabHiddenJsons.map(tabHiddenJson => tabHiddenJson.gameSessionUuid));
    const seconds = tabHiddenJsons
      .map(tabHiddenJson => tabHiddenJson.millisSincePageLoad)
      .filter((millis): millis is number => millis != null)
      .map(millis => millis / 1000);

    return {
      successfulStartups: successful.length,
      backgrounded: backgroundedUuids.size,
      medianSeconds: this.median(seconds),
      buckets: this.buckets(seconds)
    };
  }

  /**
   * The comparison the whole feature exists for: of the players whose game came up, did the ones
   * who looked away ever get anywhere?
   * <p>
   * Both cohorts are read against the same activity list, so they are directly comparable. A
   * player who started twice and backgrounded only one of those starts appears in both - the
   * unit being compared is the start, not the person.
   */
  private cohorts(): AttentionCohort[] {
    const usersBySession = this.usersBySession();
    const backgroundedUuids = new Set(this.tabHiddenJsons().map(tabHiddenJson => tabHiddenJson.gameSessionUuid));

    const backgrounded = {sessions: 0, users: new Set<string>()};
    const stayed = {sessions: 0, users: new Set<string>()};

    for (const startupTerminatedJson of this.successfulStartups()) {
      const userId = usersBySession.get(startupTerminatedJson.gameSessionUuid);
      if (!userId) {
        continue;
      }
      const cohort = backgroundedUuids.has(startupTerminatedJson.gameSessionUuid) ? backgrounded : stayed;
      cohort.sessions++;
      cohort.users.add(userId);
    }

    return [
      // Not "switched tabs": most browsers hide the page before they unload it, so closing the
      // game lands here as well. The record says the player stopped looking, not what they did
      // next, and the label has to stay within that.
      this.cohort('Looked away at some point', backgrounded),
      this.cohort('Never looked away', stayed)
    ];
  }

  private cohort(name: string, raw: { sessions: number, users: Set<string> }): AttentionCohort {
    return {
      name,
      sessions: raw.sessions,
      users: raw.users.size,
      baseCreated: this.countUsersWith(raw.users, UserActivityType.BASE_CREATED),
      questPassed: this.countUsersWith(raw.users, UserActivityType.QUEST_PASSED)
    };
  }

  private countUsersWith(userIds: Set<string>, userActivityType: UserActivityType): number {
    const acted = new Set(this.userActivities()
      .filter(userActivity => userActivity.userActivityType === userActivityType)
      .map(userActivity => userActivity.userId));
    let count = 0;
    userIds.forEach(userId => {
      if (acted.has(userId)) {
        count++;
      }
    });
    return count;
  }

  private unlinkedSessions(): number {
    const usersBySession = this.usersBySession();
    return this.successfulStartups()
      .filter(startupTerminatedJson => !usersBySession.get(startupTerminatedJson.gameSessionUuid))
      .length;
  }

  /**
   * The one join between a startup and the player it belonged to. GAME_SESSION_STARTED is the
   * only activity that carries both ids; without it the startup records and everything the
   * player does afterwards are two unconnected halves.
   */
  private usersBySession(): Map<string, string> {
    const usersBySession = new Map<string, string>();
    for (const userActivity of this.userActivities()) {
      if (userActivity.userActivityType === UserActivityType.GAME_SESSION_STARTED
        && userActivity.gameSessionUuid && userActivity.userId) {
        usersBySession.set(userActivity.gameSessionUuid, userActivity.userId);
      }
    }
    return usersBySession;
  }

  /**
   * Fixed slices rather than a curve: the question is not how long people looked but whether they
   * looked at all, and the first bucket is the one that answers it - the game is barely on screen
   * before it is gone.
   */
  private buckets(seconds: number[]): AttentionBucket[] {
    return [
      {label: 'under 10 s', count: seconds.filter(second => second < 10).length},
      {label: '10 - 60 s', count: seconds.filter(second => second >= 10 && second < 60).length},
      {label: '1 - 5 min', count: seconds.filter(second => second >= 60 && second < 300).length},
      {label: 'over 5 min', count: seconds.filter(second => second >= 300).length}
    ];
  }

  private median(values: number[]): number | null {
    if (values.length === 0) {
      return null;
    }
    const sorted = [...values].sort((a, b) => a - b);
    const middle = Math.floor(sorted.length / 2);
    const median = sorted.length % 2 === 0
      ? (sorted[middle - 1] + sorted[middle]) / 2
      : sorted[middle];
    return Math.round(median * 10) / 10;
  }

  private hidden(startupTerminatedJson: StartupTerminatedJson): boolean | null {
    const hidden = startupTerminatedJson.hidden as boolean | null | undefined;
    return hidden ?? null;
  }

  private successfulStartups(): StartupTerminatedJson[] {
    return this.startupTerminatedJsons().filter(startupTerminatedJson => startupTerminatedJson.successful);
  }

  private startupTerminatedJsons(): StartupTerminatedJson[] {
    return this.trackingContainer?.startupTerminatedJson ?? [];
  }

  private tabHiddenJsons(): TabHiddenJson[] {
    return this.trackingContainer?.tabHiddenJsons ?? [];
  }

  private userActivities(): UserActivity[] {
    return this.trackingContainer?.userActivities ?? [];
  }
}
