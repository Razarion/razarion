import {
  PageRequest,
  PageRequestType,
  StartupTaskJson,
  StartupTerminatedJson,
  TrackingContainer,
  TrackingPlatform,
  UserActivity,
  UserActivityType
} from '../../generated/razarion-share';
import {ProgressStatistic} from './progress-statistic';
import {classifyDevice, DeviceClass, isAppFetch} from './first-interaction-analyzer';

/**
 * Which visitors the funnel is about: the ones a platform brought, or everyone.
 * <p>
 * "All" is not a third platform, it is the absence of the filter - it also counts the visitor no
 * platform can be derived for.
 */
export type FunnelView = TrackingPlatform | 'all';

/**
 * Which view the funnel opens on. X, because that is where the traffic is: in a sample day not one
 * of 200 home visits carried a Reddit click id and 197 carried an X one, so opening on Reddit
 * showed an empty funnel and read as "nobody plays".
 */
export const DEFAULT_FUNNEL_VIEW: FunnelView = TrackingPlatform.X;

/**
 * Which device the funnel is about. The same classification the Controls tab uses, so the two
 * agree on what a phone is - see classifyDevice(), including what it gets wrong.
 * <p>
 * 'all' is the absence of the filter and includes Unknown, which is not a device but a missing
 * user agent: the beacons of a cookie-less browser arrive without one, and a crawler sends none
 * either. Selecting it is the only way to see how big that gap is.
 */
export type DeviceFilter = DeviceClass | 'all';

/**
 * Everything the funnel correlates: page requests and the two startup records. They carry the same
 * campaign fields, which is what lets one visitor be followed from the landing page into the game.
 */
interface Correlatable {
  rdtCid?: string;
  twclid?: string;
  fbclid?: string;
  utmSource?: string;
  httpSessionId?: string;
  /** Startup records only: the run of the client itself, from the first beacon to the last. */
  gameSessionUuid?: string;
  /** Page requests and startup tasks carry one; the terminated record does not. */
  userAgent?: string;
}

/**
 * One visitor, assembled from every record that belongs to them.
 * <p>
 * Not one row per record and not one row per http session: a visitor without cookies gets a fresh
 * session on every request, and correlating those by session alone splits one arrival into several.
 * See {@link TrackingContainerAnalyzer#handlesOf}.
 */
interface Visitor {
  key: string;
  httpSessionIds: Set<string>;
  /** They asked for the landing page. Recorded with or without campaign parameters. */
  landing: boolean;
  /** The pixel fired - this visitor arrived with a query string. */
  home: boolean;
  playClicked: boolean;
  /** They opened the game - proven by the page request, or by a startup record where there is none. */
  game: boolean;
  /** The client booted all the way through, so the engine ran and they could play. */
  engineRunning: boolean;
  rdtCid: boolean;
  twclid: boolean;
  fbclid: boolean;
  utmSources: string[];
  /** The referrer of the landing page request, the only one that sees where the visitor came from. */
  landingReferers: string[];
  /** What the client read for itself, asked only when there was no landing page in between. */
  clientReferrers: string[];
  /** Every user agent seen for this visitor; the first one that says anything decides the device. */
  userAgents: string[];
}

/**
 * Which handles belong to the same visitor.
 * <p>
 * A record can carry several of them and each record is a statement that its handles are one
 * person: a startup beacon carrying a click id and a game session says the two describe the same
 * visitor, so every other record of that game session belongs to that click id as well. Chains of
 * such statements are followed to the end - which no single lookup table can do, because the link
 * that joins two records may only appear in a third.
 */
class HandleGroups {
  private parents = new Map<string, string>();

  /** Records the statement that these handles are one visitor. */
  join(handles: string[]) {
    for (let index = 1; index < handles.length; index++) {
      this.union(handles[0], handles[index]);
    }
  }

  /**
   * The one name shared by every record of this visitor. Null when the record names nobody at all -
   * it is then left out rather than counted, because an absent key is a perfectly good Map key and
   * a perfectly good match for every other absent one: all of them used to collapse into a single
   * phantom visitor with a plausible funnel below it.
   */
  keyOf(handles: string[]): string | null {
    return handles.length > 0 ? this.root(handles[0]) : null;
  }

  private union(left: string, right: string) {
    const leftRoot = this.root(left);
    const rightRoot = this.root(right);
    if (leftRoot !== rightRoot) {
      this.parents.set(rightRoot, leftRoot);
    }
  }

  private root(handle: string): string {
    let current = handle;
    let parent = this.parents.get(current);
    if (parent === undefined) {
      this.parents.set(current, current);
      return current;
    }
    while (parent !== current) {
      current = parent;
      parent = this.parents.get(current)!;
    }
    // Point straight at the root, so a long chain is walked once rather than once per lookup.
    this.parents.set(handle, current);
    return current;
  }
}

export class TrackingContainerAnalyzer {
  private trackingContainer!: TrackingContainer;
  private view: FunnelView = DEFAULT_FUNNEL_VIEW;
  private device: DeviceFilter = 'all';
  /** Built from the container alone, so it survives a change of view or device. */
  private visitorCache: Visitor[] | null = null;

  setTrackingContainer(trackingContainer: TrackingContainer) {
    this.trackingContainer = trackingContainer;
    this.visitorCache = null;
  }

  setView(view: FunnelView) {
    this.view = view;
  }

  getView(): FunnelView {
    return this.view;
  }

  setDevice(device: DeviceFilter) {
    this.device = device;
  }

  /** Visitors of the selected platform and device; both filters are off in their 'all' setting. */
  private visitors(): Visitor[] {
    if (this.visitorCache === null) {
      this.visitorCache = this.buildVisitors();
    }
    return this.visitorCache.filter(visitor =>
      (this.view === 'all' || TrackingContainerAnalyzer.platformOf(visitor) === this.view)
      && (this.device === 'all' || TrackingContainerAnalyzer.deviceOf(visitor) === this.device));
  }

  /**
   * What the visitor was holding. The first user agent that says anything decides it: the later
   * beacons of one visit come from the same browser, and the ones that carry nothing at all - the
   * beacons of a cookie-less browser - would otherwise turn a known device into an unknown one.
   */
  private static deviceOf(visitor: Visitor): DeviceClass {
    for (const userAgent of visitor.userAgents) {
      const device = classifyDevice(userAgent);
      if (device !== 'Unknown') {
        return device;
      }
    }
    return 'Unknown';
  }

  countHome(): number {
    return this.visitors().filter(visitor => visitor.home).length;
  }

  countPlayClicked(): number {
    return this.visitors().filter(visitor => visitor.playClicked).length;
  }

  /**
   * Visitors who fired the pixel and then opened the game. The landing page's own conversion, and
   * the only game count that may be measured against Home: everything above it needs the pixel, so
   * a visitor who never fired one cannot be a share of it.
   */
  countGameFromHome(): number {
    return this.visitors().filter(visitor => visitor.home && visitor.game).length;
  }

  countGame(): number {
    return this.visitors().filter(visitor => visitor.game).length;
  }

  countEngineRunning(): number {
    return this.visitors().filter(visitor => visitor.engineRunning).length;
  }

  /**
   * Visitors who asked for the landing page. Context only, never a funnel stage and never a
   * percentage base: LANDING is recorded without the pixel and therefore without a query string,
   * so it describes a wider population than HOME does - dividing by it would move every percentage
   * without anything about the visitors having changed. DailyProgressService skips it for the same
   * reason.
   * <p>
   * Shown in the 'all' view only, where it answers "how many did we not see": a visitor with no
   * parameters and no referrer belongs to no platform by definition, so a platform view could only
   * ever show a part of it.
   */
  countLanding(): number {
    return this.visitors().filter(visitor => visitor.landing).length;
  }

  /**
   * The players who built their first base, one entry per player.
   * <p>
   * A user is reached through the http session they were created in, which is why the visitor
   * keeps every session it was assembled from rather than only the one that happened to be first.
   */
  getBaseCreatedUserIds(): string[] {
    const usersPerSession = this.usersPerSession();
    const basesBuilt = this.userIdsWith(UserActivityType.BASE_CREATED);
    const seenUserIds = new Set<string>();
    const baseCreated: string[] = [];
    // Only visitors who opened the game: the row is a share of the one above it, and a user is
    // created by the game client, so anything else would be a user created out of nowhere.
    this.visitors().filter(visitor => visitor.game).forEach(visitor => {
      visitor.httpSessionIds.forEach(httpSessionId => {
        const userId = usersPerSession.get(httpSessionId);
        // Count only the initial base per user (a user who lost and re-created a base must not be
        // counted again), and only once however many sessions of theirs are in this visitor.
        if (userId !== undefined && !seenUserIds.has(userId) && basesBuilt.has(userId)) {
          seenUserIds.add(userId);
          baseCreated.push(userId);
        }
      });
    });
    return baseCreated;
  }

  /**
   * The level and quest rows below the funnel.
   * <p>
   * Levels are a chain - nobody reaches level 3 without level 2 - so each level is measured
   * against the one before it. The quests inside a level are not: a player passes them in their
   * own order, and the rows are shown by size rather than by that order. Every quest is therefore
   * measured against the players who reached its level, which is a fixed reference and reads the
   * same however the rows are sorted.
   * <p>
   * They used to be chained to each other instead, in the order the quest ids happened to appear
   * in the activity list, and then re-sorted by count for display - so a row's percentage referred
   * to whichever quest came before it in the raw data, not to the row above it. A quest passed
   * more often than its accidental predecessor showed over 100%.
   */
  generateLevelQuestStatistics(baseCreatedCount: number) {
    let levels: number[] = [];
    let levelQuests: Map<number, Map<number, number>> = new Map<number, Map<number, number>>()
    let maxLevelNumber = 0;


    this.getBaseCreatedUserIds().forEach((userId) => {
      this.getLevelUpsUserId(userId).forEach(userActivity => {
        const levelNumber = Number(userActivity.detail);
        let count = levels[levelNumber];
        if (count === undefined) {
          count = 0;
        }
        count++;
        levels[levelNumber] = count;
        if (maxLevelNumber < levelNumber) {
          maxLevelNumber = levelNumber;
        }
      });
      this.getQuestsPassedUserId(userId).forEach(userActivity => {
        if (userActivity.detail2 !== undefined) {
          const levelNumber = Number(userActivity.detail2);
          if (maxLevelNumber < levelNumber) {
            maxLevelNumber = levelNumber;
          }
          let questCount = levelQuests.get(levelNumber);
          if (questCount === undefined) {
            questCount = new Map<number, number>();
            levelQuests.set(levelNumber, questCount);
          }
          const questId = Number(userActivity.detail);
          let count = questCount.get(questId);
          if (count === undefined) {
            count = 0;
          }
          count++;
          questCount.set(questId, count);
        }
      });
    });

    let progressStatistics: ProgressStatistic[] = [];
    // Level 1 emits no LEVEL_UP - it comes with the first base - so the players who reached it are
    // the players who built one, and its quests are measured against that.
    let levelReached = baseCreatedCount;
    for (let levelNumber = 1; levelNumber <= maxLevelNumber; levelNumber++) {
      const levelUpCount = levels[levelNumber];
      if (levelUpCount !== undefined) {
        progressStatistics.push(new ProgressStatistic(`Level ${levelNumber}`, levelUpCount, levelReached));
        levelReached = levelUpCount;
      }
      const levelQuestMap = levelQuests.get(levelNumber);
      if (levelQuestMap !== undefined) {
        let questProgressStatistics: ProgressStatistic[] = []
        levelQuestMap.forEach((count, questId) => {
          if (count !== undefined) {
            questProgressStatistics.push(
              new ProgressStatistic(`Quest ${questId} (Level ${levelNumber})`, count, levelReached));
          }
        });
        // Display order only - it no longer moves any percentage.
        questProgressStatistics.sort((a, b) => b.count - a.count);
        progressStatistics.push(...questProgressStatistics);
      }
    }

    return progressStatistics;
  }

  // ---------------------------------------------------------------- assembling the visitors

  /**
   * One entry per visitor, built from every record in the container.
   * <p>
   * Two passes, because a record alone does not know who it belongs to: the game page request of a
   * visitor whose click id never reached the game url carries nothing but its http session, and
   * what ties that session to the click id may be stated by a third record entirely. The first
   * pass collects those statements, the second reads the answer off them.
   */
  private buildVisitors(): Visitor[] {
    const handles = new HandleGroups();
    this.eachRecord(record => handles.join(TrackingContainerAnalyzer.handlesOf(record)));
    const visitors = new Map<string, Visitor>();

    const visitorOf = (record: Correlatable): Visitor | null => {
      const key = handles.keyOf(TrackingContainerAnalyzer.handlesOf(record));
      if (key === null) {
        return null;
      }
      let visitor = visitors.get(key);
      if (visitor === undefined) {
        visitor = {
          key, httpSessionIds: new Set<string>(), landing: false, home: false, playClicked: false, game: false,
          engineRunning: false, rdtCid: false, twclid: false, fbclid: false, utmSources: [], landingReferers: [],
          clientReferrers: [], userAgents: []
        };
        visitors.set(key, visitor);
      }
      if (record.httpSessionId) {
        visitor.httpSessionIds.add(record.httpSessionId);
      }
      if (record.rdtCid) {
        visitor.rdtCid = true;
      }
      if (record.twclid) {
        visitor.twclid = true;
      }
      if (record.fbclid) {
        visitor.fbclid = true;
      }
      if (record.utmSource) {
        visitor.utmSources.push(record.utmSource);
      }
      if (record.userAgent) {
        visitor.userAgents.push(record.userAgent);
      }
      return visitor;
    };

    this.pageRequests().forEach(pageRequest => {
      const visitor = visitorOf(pageRequest);
      if (visitor === null) {
        return;
      }
      switch (pageRequest.pageRequestType) {
        case PageRequestType.HOME:
          visitor.home = true;
          break;
        case PageRequestType.HOME_PLAY_CLICKED:
          visitor.playClicked = true;
          break;
        case PageRequestType.GAME:
          visitor.game = true;
          break;
        case PageRequestType.LANDING:
          visitor.landing = true;
          // The only record that sees where the visitor came from: the pixel is a subresource of
          // the landing page and "Play Now" is a navigation to /game, so every referrer after this
          // one is razarion.com. See PlayerSessionService.origin().
          if (pageRequest.referer) {
            visitor.landingReferers.push(pageRequest.referer);
          }
          break;
      }
    });

    // A startup record proves the game was opened as surely as the page request does - and it is
    // the only proof for a visitor who arrived without a query string, because /game is recorded
    // only when it carries one (RequestInfoLoggingFilter). Those players reach level 7 and used to
    // be missing from every funnel row.
    this.startupTaskJsons().forEach(task => {
      const visitor = visitorOf(task);
      if (visitor === null) {
        return;
      }
      visitor.game = true;
      if (task.referrer) {
        visitor.clientReferrers.push(task.referrer);
      }
    });
    this.startupTerminatedJsons().forEach(terminated => {
      const visitor = visitorOf(terminated);
      if (visitor === null) {
        return;
      }
      visitor.game = true;
      if (terminated.referrer) {
        visitor.clientReferrers.push(terminated.referrer);
      }
      if (terminated.successful) {
        // The only startup stage worth a funnel row: the client booted through, so the engine runs
        // and the player could play. A terminated startup counts the failed boots too.
        visitor.engineRunning = true;
      }
    });

    return [...visitors.values()];
  }

  /** Every record the funnel correlates, whichever collection it came from. */
  private eachRecord(visit: (record: Correlatable) => void) {
    this.pageRequests().forEach(visit);
    this.startupTaskJsons().forEach(visit);
    this.startupTerminatedJsons().forEach(visit);
  }

  /**
   * Everything a record says about whose it is. Records that share any one of these belong to the
   * same visitor - none of the three is enough on its own:
   * <ul>
   *   <li>the click id survives a session that does not. A browser that accepts no cookies is
   *   handed a fresh http session on every single request - in a week of production eleven
   *   thousand of them for a few hundred arrivals.</li>
   *   <li>the game session ties one run of the client together. It is what holds the startup
   *   beacons of that same cookie-less browser together: one boot arrived as a dozen sessions of
   *   one task each, which read as a dozen visitors who each opened the game and never got past
   *   the first step.</li>
   *   <li>the http session carries everyone who has neither: an organic visitor never gets a
   *   click id, and their landing page fires no startup beacon.</li>
   * </ul>
   */
  private static handlesOf(record: Correlatable): string[] {
    const handles: string[] = [];
    if (record.rdtCid) {
      handles.push('rdtCid:' + record.rdtCid);
    }
    if (record.twclid) {
      handles.push('twclid:' + record.twclid);
    }
    if (record.fbclid) {
      handles.push('fbclid:' + record.fbclid);
    }
    if (record.gameSessionUuid) {
      handles.push('game:' + record.gameSessionUuid);
    }
    if (record.httpSessionId) {
      handles.push('session:' + record.httpSessionId);
    }
    return handles;
  }

  /**
   * Which platform brought this visitor, in the same three steps the history uses - see
   * PlayerSessionService.source(), which this deliberately mirrors: a funnel that answers "X" for
   * a visitor the history calls organic, or the other way round, is worse than either answer.
   * <p>
   * The click id is the strongest signal but only a paid click carries one. The campaign the visit
   * names itself comes next, and last the site it came from, which needs no parameter to have
   * survived at all: t.co is X's own shortener, and a visit arriving from it is X traffic.
   */
  private static platformOf(visitor: Visitor): TrackingPlatform | null {
    if (visitor.rdtCid) {
      return TrackingPlatform.REDDIT;
    }
    if (visitor.twclid) {
      return TrackingPlatform.X;
    }
    if (visitor.fbclid) {
      return TrackingPlatform.META;
    }
    for (const utmSource of visitor.utmSources) {
      const platform = TrackingContainerAnalyzer.platformOfUtmSource(utmSource);
      if (platform !== null) {
        return platform;
      }
    }
    // The landing page first, and only then what the client read: pressing "Play Now" is a
    // navigation to /game, so from there on every referrer the browser reports is razarion.com.
    // It is a real origin for someone who opened the game url directly, and nothing for everyone
    // else - which platformOfOrigin answers with null.
    for (const referer of [...visitor.landingReferers, ...visitor.clientReferrers]) {
      const platform = TrackingContainerAnalyzer.platformOfOrigin(referer);
      if (platform !== null) {
        return platform;
      }
    }
    return null;
  }

  /**
   * The campaign a visitor names, for the ones whose click id did not survive the trip - a reload
   * without it, a link passed on, an in-app browser that strips the parameter. An organic post is
   * never tagged with a click id at all and has nothing else.
   */
  private static platformOfUtmSource(utmSource: string): TrackingPlatform | null {
    const normalized = utmSource.toLowerCase();
    if (normalized.includes('reddit')) {
      return TrackingPlatform.REDDIT;
    }
    if (normalized.includes('twitter') || normalized === 'x') {
      return TrackingPlatform.X;
    }
    // Facebook and Instagram are one advertiser account and one campaign, so both names answer
    // with the same platform - see TrackingPlatforms.ofUtmSource(), which this mirrors.
    if (normalized.includes('instagram') || normalized.includes('facebook')
      || normalized === 'meta' || normalized.startsWith('meta_') || normalized.startsWith('meta-')
      || normalized === 'ig' || normalized === 'fb') {
      return TrackingPlatform.META;
    }
    return null;
  }

  /**
   * The platform a referring site belongs to. Only the two that are advertised on are mapped: a
   * visit from a search engine is genuinely organic, and saying so is the honest answer - not a
   * third platform invented to fill the cell.
   */
  private static platformOfOrigin(referer: string): TrackingPlatform | null {
    const host = TrackingContainerAnalyzer.host(referer);
    if (TrackingContainerAnalyzer.isHost(host, 't.co')
      || TrackingContainerAnalyzer.isHost(host, 'x.com')
      || TrackingContainerAnalyzer.isHost(host, 'twitter.com')) {
      return TrackingPlatform.X;
    }
    if (TrackingContainerAnalyzer.isHost(host, 'reddit.com')
      || TrackingContainerAnalyzer.isHost(host, 'redd.it')) {
      return TrackingPlatform.REDDIT;
    }
    // m.facebook.com and l.facebook.com - the mobile site and Facebook's own link shim - are
    // subdomains and covered by the same entry.
    if (TrackingContainerAnalyzer.isHost(host, 'facebook.com')
      || TrackingContainerAnalyzer.isHost(host, 'instagram.com')
      || TrackingContainerAnalyzer.isHost(host, 'fb.com')
      || TrackingContainerAnalyzer.isHost(host, 'fb.me')
      || TrackingContainerAnalyzer.isHost(host, 'fb.watch')) {
      return TrackingPlatform.META;
    }
    return null;
  }

  /** The domain itself or a subdomain of it, never a host that merely ends in the same letters. */
  private static isHost(host: string, domain: string): boolean {
    return host === domain || host.endsWith('.' + domain);
  }

  private static host(url: string): string {
    try {
      return new URL(url).host.toLowerCase();
    } catch (e) {
      return '';
    }
  }

  // ---------------------------------------------------------------- the raw collections

  /**
   * Every page request except the ones the Facebook app made for itself. Filtered here, in the one
   * accessor, rather than at each count: such a request fires the pixel like any other render, so
   * it can enter a denominator but never a numerator, and one missed site would quietly put a fifth
   * of the Meta traffic back into the base of every rate on the page. See isAppFetch.
   */
  private pageRequests(): PageRequest[] {
    return (this.trackingContainer.pageRequests ?? []).filter(pageRequest => !isAppFetch(pageRequest.userAgent));
  }

  private startupTaskJsons(): StartupTaskJson[] {
    return this.trackingContainer.startupTaskJsons ?? [];
  }

  private startupTerminatedJsons(): StartupTerminatedJson[] {
    return this.trackingContainer.startupTerminatedJson ?? [];
  }

  private userActivities(): UserActivity[] {
    return this.trackingContainer.userActivities ?? [];
  }

  /** Which user was created in which http session - the join from a visitor to a player. */
  private usersPerSession(): Map<string, string> {
    const usersPerSession = new Map<string, string>();
    this.userActivities().forEach(userActivity => {
      if (userActivity.userActivityType === UserActivityType.USER_CREATED
        && userActivity.httpSessionId && userActivity.userId
        && !usersPerSession.has(userActivity.httpSessionId)) {
        usersPerSession.set(userActivity.httpSessionId, userActivity.userId);
      }
    });
    return usersPerSession;
  }

  private userIdsWith(userActivityType: UserActivityType): Set<string> {
    const userIds = new Set<string>();
    this.userActivities().forEach(userActivity => {
      if (userActivity.userActivityType === userActivityType && userActivity.userId) {
        userIds.add(userActivity.userId);
      }
    });
    return userIds;
  }

  private getLevelUpsUserId(userId: string) {
    return this.userActivities().filter(userActivity => userActivity.userActivityType === UserActivityType.LEVEL_UP && userActivity.userId === userId);
  }

  private getQuestsPassedUserId(userId: string) {
    return this.userActivities().filter(userActivity => userActivity.userActivityType === UserActivityType.QUEST_PASSED && userActivity.userId === userId);
  }
}
