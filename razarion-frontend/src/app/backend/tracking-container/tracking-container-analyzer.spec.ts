import {
  DEFAULT_FUNNEL_VIEW,
  FunnelView,
  TrackingContainerAnalyzer
} from './tracking-container-analyzer';
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
import {createStatistics, ProgressStatistic} from './progress-statistic';

/**
 * The percentages in this table are read as evidence for where the players stop, so a number that
 * refers to the wrong row is worse than no number. What is pinned down here is which population
 * each row is a share of - and that sorting the rows for display cannot change it.
 */
describe('TrackingContainerAnalyzer level and quest statistics', () => {
  const CLICK_ID = 'rdt-1';

  function pageRequest(pageRequestType: PageRequestType, httpSessionId: string): PageRequest {
    return {pageRequestType, httpSessionId, rdtCid: CLICK_ID} as unknown as PageRequest;
  }

  function activity(userActivityType: UserActivityType, userId: string,
                    detail?: string, detail2?: string, httpSessionId?: string): UserActivity {
    return {userActivityType, userId, detail, detail2, httpSessionId} as unknown as UserActivity;
  }

  /**
   * One player who got as far as the given activities. Reaching the quest rows at all takes the
   * whole chain the funnel walks: a home visit, a game visit of the same visitor, the user created
   * in that http session, and a base.
   */
  function player(userId: string, ...activities: UserActivity[]) {
    const httpSessionId = `session-${userId}`;
    return {
      pageRequests: [
        pageRequest(PageRequestType.HOME, httpSessionId),
        pageRequest(PageRequestType.GAME, httpSessionId)
      ],
      userActivities: [
        activity(UserActivityType.USER_CREATED, userId, undefined, undefined, httpSessionId),
        activity(UserActivityType.BASE_CREATED, userId),
        ...activities
      ]
    };
  }

  function questPassed(userId: string, questId: number, level: number): UserActivity {
    return activity(UserActivityType.QUEST_PASSED, userId, String(questId), String(level));
  }

  function levelUp(userId: string, level: number): UserActivity {
    return activity(UserActivityType.LEVEL_UP, userId, String(level));
  }

  /**
   * A visitor is keyed by their click id where there is one, so every player would collapse into
   * one row if they shared it. Each gets their own.
   */
  function statistics(players: ReturnType<typeof player>[]) {
    const analyzer = new TrackingContainerAnalyzer();
    analyzer.setView(TrackingPlatform.REDDIT);
    analyzer.setTrackingContainer({
      pageRequests: players.flatMap((one, index) => one.pageRequests
        .map(request => ({...request, rdtCid: `${CLICK_ID}-${index}`}))),
      userActivities: players.flatMap(one => one.userActivities),
      startupTerminatedJson: [],
      startupTaskJsons: [],
      tabHiddenJsons: []
    } as unknown as TrackingContainer);
    return analyzer.generateLevelQuestStatistics(players.length);
  }

  function row(rows: ReturnType<typeof statistics>, name: string) {
    return rows.find(progressStatistic => progressStatistic.name === name);
  }

  /**
   * The regression this guards: the quest rows used to be chained to each other in the order the
   * quest ids appeared in the activity list and only then sorted by size, so the bigger of two
   * quests was divided by the smaller and showed over 100%.
   */
  it('measures a level 1 quest against the players who built a base, not against another quest', () => {
    // Quest 359 appears first in the stream and is passed less often than 358.
    const rows = statistics([
      player('u1', questPassed('u1', 359, 1), questPassed('u1', 358, 1)),
      player('u2', questPassed('u2', 358, 1)),
      player('u3', questPassed('u3', 358, 1)),
      player('u4')
    ]);

    expect(row(rows, 'Quest 358 (Level 1)')!.count).toBe(3);
    // 3 of 4 players with a base - not 3 of the single 359 pass, which was 300%.
    expect(row(rows, 'Quest 358 (Level 1)')!.percent).toBe(75);
    expect(row(rows, 'Quest 359 (Level 1)')!.percent).toBe(25);
  });

  it('measures a quest against the players who reached its level', () => {
    const rows = statistics([
      player('u1', levelUp('u1', 2), questPassed('u1', 363, 2)),
      player('u2', levelUp('u2', 2)),
      player('u3')
    ]);

    // Two of three players reached level 2...
    expect(row(rows, 'Level 2')!.percent).toBe(67);
    // ...and one of those two passed the quest. Against all three it would read 33%.
    expect(row(rows, 'Quest 363 (Level 2)')!.percent).toBe(50);
  });

  it('keeps a level measured against the level before it', () => {
    const rows = statistics([
      player('u1', levelUp('u1', 2), levelUp('u1', 3)),
      player('u2', levelUp('u2', 2)),
      player('u3', levelUp('u3', 2)),
      player('u4')
    ]);

    expect(row(rows, 'Level 2')!.percent).toBe(75);
    // 1 of the 3 who reached level 2, not 1 of the 4 who have a base.
    expect(row(rows, 'Level 3')!.percent).toBe(33);
  });

  it('shows the quests of a level by size without that changing a percentage', () => {
    const rows = statistics([
      player('u1', questPassed('u1', 359, 1), questPassed('u1', 358, 1)),
      player('u2', questPassed('u2', 358, 1))
    ]);

    const questNames = rows.map(progressStatistic => progressStatistic.name)
      .filter(name => name.startsWith('Quest'));
    expect(questNames).toEqual(['Quest 358 (Level 1)', 'Quest 359 (Level 1)']);
    expect(row(rows, 'Quest 358 (Level 1)')!.percent).toBe(100);
    expect(row(rows, 'Quest 359 (Level 1)')!.percent).toBe(50);
  });
});

/** What a record carries of the campaign it came from - whichever of these survived the trip. */
interface Campaign {
  rdtCid?: string;
  twclid?: string;
  utmSource?: string;
  referer?: string;
  userAgent?: string;
}

const PHONE = 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_7 like Mac OS X) AppleWebKit/605.1.15';
const DESKTOP = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:153.0) Gecko/20100101 Firefox/153.0';

function request(pageRequestType: PageRequestType, httpSessionId: string,
                 campaign: Campaign = {}): PageRequest {
  return {pageRequestType, httpSessionId, ...campaign} as unknown as PageRequest;
}

function task(httpSessionId: string, campaign: Campaign = {},
              gameSessionUuid = 'game-1'): StartupTaskJson {
  return {httpSessionId, gameSessionUuid, taskEnum: 'PAGE_LOADED', ...campaign} as unknown as StartupTaskJson;
}

function startup(successful: boolean, httpSessionId: string, campaign: Campaign = {},
                 gameSessionUuid = 'game-1'): StartupTerminatedJson {
  return {successful, httpSessionId, gameSessionUuid, ...campaign} as unknown as StartupTerminatedJson;
}

function userCreated(userId: string, httpSessionId: string): UserActivity {
  return {userActivityType: UserActivityType.USER_CREATED, userId, httpSessionId} as unknown as UserActivity;
}

function baseCreated(userId: string): UserActivity {
  return {userActivityType: UserActivityType.BASE_CREATED, userId} as unknown as UserActivity;
}

function reachedLevel(userId: string, level: number): UserActivity {
  return {userActivityType: UserActivityType.LEVEL_UP, userId, detail: String(level)} as unknown as UserActivity;
}

function analyzerFor(view: FunnelView, pageRequests: PageRequest[],
                     startupTerminatedJson: StartupTerminatedJson[] = [],
                     userActivities: UserActivity[] = [],
                     startupTaskJsons: StartupTaskJson[] = []): TrackingContainerAnalyzer {
  const analyzer = new TrackingContainerAnalyzer();
  analyzer.setTrackingContainer({
    pageRequests,
    userActivities,
    startupTerminatedJson,
    startupTaskJsons,
    tabHiddenJsons: []
  } as unknown as TrackingContainer);
  analyzer.setView(view);
  return analyzer;
}

function stage(rows: ProgressStatistic[], name: string) {
  return rows.find(progressStatistic => progressStatistic.name === name);
}

/**
 * Which platform a visitor is counted under. The funnel used to ask for the click id alone, and a
 * click id is only handed out on a paid click - so everyone arriving from an organic post or a
 * shared link was invisible in the platform view while the history, which asks three questions,
 * listed them as X. That is how the funnel came to top out at level 2 with players on level 7 in
 * the table next to it.
 */
describe('TrackingContainerAnalyzer platform resolution', () => {
  it('opens on the platform that carries the traffic', () => {
    // Reddit was the default while every click id in the data was an X one.
    expect(DEFAULT_FUNNEL_VIEW).toBe(TrackingPlatform.X);
  });

  it('counts a visitor whose campaign is named only by utm_source', () => {
    const analyzer = analyzerFor(TrackingPlatform.X, [
      request(PageRequestType.HOME, 'session-1', {utmSource: 'twitter'}),
      request(PageRequestType.GAME, 'session-1', {utmSource: 'twitter'})
    ]);

    expect(analyzer.countHome()).toBe(1);
    expect(analyzer.countGame()).toBe(1);
  });

  it('counts a visitor known only by the site they came from', () => {
    // A profile or tweet link carries nothing at all; t.co is X's own shortener.
    const analyzer = analyzerFor(TrackingPlatform.X, [
      request(PageRequestType.LANDING, 'session-1', {referer: 'https://t.co/6TzmtWLVdT'}),
      request(PageRequestType.GAME, 'session-1')
    ]);

    expect(analyzer.countGame()).toBe(1);
  });

  it('does not read a platform out of a host that merely ends in the same letters', () => {
    const analyzer = analyzerFor(TrackingPlatform.X, [
      request(PageRequestType.LANDING, 'session-1', {referer: 'https://notx.com/'}),
      request(PageRequestType.GAME, 'session-1')
    ]);

    expect(analyzer.countGame()).toBe(0);
  });

  it('leaves a visitor of the other platform out', () => {
    const analyzer = analyzerFor(TrackingPlatform.REDDIT, [
      request(PageRequestType.HOME, 'session-1', {twclid: 'tw-1'}),
      request(PageRequestType.HOME, 'session-2', {utmSource: 'twitter'}),
      request(PageRequestType.HOME, 'session-3', {rdtCid: 'rdt-1'})
    ]);

    expect(analyzer.countHome()).toBe(1);
  });

  it('does not merge two visitors of another platform into one', () => {
    const analyzer = analyzerFor(TrackingPlatform.REDDIT, [
      request(PageRequestType.HOME, 'session-1', {twclid: 'tw-1'}),
      request(PageRequestType.HOME, 'session-2', {twclid: 'tw-2'})
    ]);

    // Not 1: the two share no click id, they share the absence of one. That phantom visitor used
    // to come with a whole plausible funnel below it on a platform with no traffic at all.
    expect(analyzer.countHome()).toBe(0);
  });

  it('reads zero for a platform without traffic instead of one phantom visitor', () => {
    const rows = createStatistics(analyzerFor(TrackingPlatform.REDDIT, [
        request(PageRequestType.HOME, 'session-1', {twclid: 'tw-1'}),
        request(PageRequestType.HOME_PLAY_CLICKED, 'session-1', {twclid: 'tw-1'}),
        request(PageRequestType.GAME, 'session-1', {twclid: 'tw-1'}),
        request(PageRequestType.HOME, 'session-2', {twclid: 'tw-2'})
      ],
      [startup(true, 'session-1', {twclid: 'tw-1'})],
      [userCreated('u1', 'session-1'), baseCreated('u1')]));

    expect(stage(rows, 'Home (landing pixel)')!.count).toBe(0);
    expect(stage(rows, 'Play clicked')!.count).toBe(0);
    expect(stage(rows, 'Game (total)')!.count).toBe(0);
    expect(stage(rows, 'Engine running')!.count).toBe(0);
    expect(stage(rows, 'Initial Base created')!.count).toBe(0);
  });
});

/**
 * What ties the records of one visitor together. Both answers are needed: the click id survives a
 * session that does not, and the session carries everyone who never got a click id.
 */
describe('TrackingContainerAnalyzer visitor correlation', () => {
  it('keeps a visitor whose every request opened a new session as one visitor', () => {
    // A browser that accepts no cookies is handed a fresh http session per request - in a week of
    // production eleven thousand of them for a few hundred arrivals.
    const analyzer = analyzerFor(TrackingPlatform.X, [
      request(PageRequestType.HOME, 'session-1', {twclid: 'tw-1'}),
      request(PageRequestType.HOME, 'session-2', {twclid: 'tw-1'}),
      request(PageRequestType.GAME, 'session-3', {twclid: 'tw-1'})
    ]);

    expect(analyzer.countHome()).toBe(1);
    expect(analyzer.countGame()).toBe(1);
  });

  it('keeps two visitors without a click id apart by their http session', () => {
    const analyzer = analyzerFor('all', [
      request(PageRequestType.GAME, 'session-1'),
      // The same visitor reloading the game page is not a second visitor.
      request(PageRequestType.GAME, 'session-1'),
      request(PageRequestType.GAME, 'session-2')
    ]);

    expect(analyzer.countGame()).toBe(2);
  });

  it('joins a record that lost the click id to the visitor by its session', () => {
    // The click id reached the landing page and not the game url - a reload without it is enough.
    const analyzer = analyzerFor(TrackingPlatform.X, [
      request(PageRequestType.HOME, 'session-1', {twclid: 'tw-1'}),
      request(PageRequestType.GAME, 'session-1')
    ]);

    expect(analyzer.countGameFromHome()).toBe(1);
    expect(analyzer.countGame()).toBe(1);
  });

  it('skips a record that identifies nobody', () => {
    const analyzer = analyzerFor('all', [
      {pageRequestType: PageRequestType.GAME} as unknown as PageRequest,
      {pageRequestType: PageRequestType.GAME} as unknown as PageRequest
    ]);

    // Neither counted nor folded together: an absent key matches every other absent key.
    expect(analyzer.countGame()).toBe(0);
  });

  it('holds one boot together when every beacon of it opened a new session', () => {
    // The same cookie-less browser again: one run of the client arrived as a dozen http sessions
    // of one task each, and read as a dozen visitors who all opened the game and got no further.
    const analyzer = analyzerFor('all',
      [],
      [startup(true, 'session-12', {}, 'game-1')],
      [],
      [task('session-10', {}, 'game-1'), task('session-11', {}, 'game-1')]);

    expect(analyzer.countGame()).toBe(1);
    expect(analyzer.countEngineRunning()).toBe(1);
  });

  it('does not credit a visitor with another session\'s startup', () => {
    const analyzer = analyzerFor('all',
      [request(PageRequestType.GAME, 'session-1')],
      [startup(true, 'session-2', {}, 'game-2')]);

    // Two visitors, and only the one that reported a startup has an engine running.
    expect(analyzer.countGame()).toBe(2);
    expect(analyzer.countEngineRunning()).toBe(1);
  });
});

/**
 * The game stage. A visitor who arrives without a query string produces no /game record at all -
 * the server only writes one when the url carries parameters - so the startup records are the only
 * proof that they opened the game. They were missing from every funnel row while the history had
 * them on level 7.
 */
describe('TrackingContainerAnalyzer game stage', () => {
  const landingOnlyVisitor = () => analyzerFor('all',
    [request(PageRequestType.LANDING, 'session-1', {referer: 'https://t.co/6TzmtWLVdT'})],
    [startup(true, 'session-1')],
    [userCreated('u1', 'session-1'), baseCreated('u1'), reachedLevel('u1', 2)],
    [task('session-1')]);

  it('counts a game opened without a page request of its own', () => {
    const rows = createStatistics(landingOnlyVisitor());

    expect(stage(rows, 'Game (total)')!.count).toBe(1);
    expect(stage(rows, 'Engine running')!.count).toBe(1);
    expect(stage(rows, 'Initial Base created')!.count).toBe(1);
    expect(stage(rows, 'Level 2')!.count).toBe(1);
  });

  it('counts that visitor under the platform their referrer names', () => {
    const analyzer = landingOnlyVisitor();
    analyzer.setView(TrackingPlatform.X);

    expect(analyzer.countGame()).toBe(1);
    expect(analyzer.getBaseCreatedUserIds().length).toBe(1);
  });

  it('does not build a base for a visitor who never opened the game', () => {
    const analyzer = analyzerFor('all',
      [request(PageRequestType.HOME, 'session-1', {twclid: 'tw-1'})],
      [],
      [userCreated('u1', 'session-1'), baseCreated('u1')]);

    expect(analyzer.getBaseCreatedUserIds().length).toBe(0);
  });
});

/**
 * The table has two halves and they do not describe the same population: everything above the game
 * rests on the landing pixel, everything below it also holds the visitors who never fired one.
 */
describe('TrackingContainerAnalyzer funnel table', () => {
  const rows = () => createStatistics(analyzerFor('all', [
      // One visitor with the pixel, all the way through.
      request(PageRequestType.LANDING, 'session-1', {twclid: 'tw-1'}),
      request(PageRequestType.HOME, 'session-1', {twclid: 'tw-1'}),
      request(PageRequestType.HOME_PLAY_CLICKED, 'session-1', {twclid: 'tw-1'}),
      request(PageRequestType.GAME, 'session-1', {twclid: 'tw-1'}),
      // One who fired the pixel and stopped there.
      request(PageRequestType.HOME, 'session-2', {twclid: 'tw-2'}),
      // And one who arrived over a plain link: no pixel, no game record, only a startup.
      request(PageRequestType.LANDING, 'session-3', {referer: 'https://t.co/6TzmtWLVdT'})
    ],
    [startup(true, 'session-1', {twclid: 'tw-1'}, 'game-1'), startup(true, 'session-3', {}, 'game-3')]));

  it('measures the landing rows against the visitors who fired the pixel', () => {
    expect(stage(rows(), 'Home (landing pixel)')!.count).toBe(2);
    expect(stage(rows(), 'Play clicked')!.percent).toBe(50);
    // One of the two pixel visitors reached the game - the third visitor is not in this number,
    // and dividing them by Home would say 150%.
    expect(stage(rows(), 'Game (from Home)')!.count).toBe(1);
    expect(stage(rows(), 'Game (from Home)')!.percent).toBe(50);
  });

  it('states the total game count without a percentage', () => {
    expect(stage(rows(), 'Game (total)')!.count).toBe(2);
    expect(stage(rows(), 'Game (total)')!.percent).toBeUndefined();
  });

  it('measures the game rows against the total', () => {
    expect(stage(rows(), 'Engine running')!.count).toBe(2);
    expect(stage(rows(), 'Engine running')!.percent).toBe(100);
  });

  it('states the landing sessions as context in the all view only', () => {
    const context = 'Landing (context, not a funnel stage)';
    const landing = stage(rows(), context)!;
    // Two sessions asked for the landing page, and no percentage anywhere refers to them.
    expect(landing.count).toBe(2);
    expect(landing.percent).toBeUndefined();

    const platformRows = createStatistics(analyzerFor(TrackingPlatform.X,
      [request(PageRequestType.LANDING, 'session-1', {twclid: 'tw-1'})]));
    expect(stage(platformRows, context)).toBeUndefined();
  });
});

/**
 * The device split. Whether the phones stop where the desktops do is the question the funnel could
 * not answer at all - and it is the one that moved most this year.
 */
describe('TrackingContainerAnalyzer device filter', () => {
  function visitors() {
    return analyzerFor('all', [
        request(PageRequestType.HOME, 'session-1', {twclid: 'tw-1', userAgent: PHONE}),
        request(PageRequestType.GAME, 'session-1', {twclid: 'tw-1', userAgent: PHONE}),
        request(PageRequestType.HOME, 'session-2', {twclid: 'tw-2', userAgent: DESKTOP}),
        request(PageRequestType.GAME, 'session-2', {twclid: 'tw-2', userAgent: DESKTOP}),
        // A visit whose records carry no user agent at all - not a device, a gap.
        request(PageRequestType.GAME, 'session-3', {twclid: 'tw-3'})
      ],
      [startup(true, 'session-1', {twclid: 'tw-1'}, 'game-1')],
      [userCreated('u1', 'session-1'), baseCreated('u1'), reachedLevel('u1', 2),
        userCreated('u2', 'session-2'), baseCreated('u2')]);
  }

  function countsFor(device: 'all' | 'Mobile' | 'Desktop' | 'Unknown') {
    const analyzer = visitors();
    analyzer.setDevice(device);
    return {home: analyzer.countHome(), game: analyzer.countGame(),
      bases: analyzer.getBaseCreatedUserIds().length};
  }

  it('counts every device when the filter is off', () => {
    expect(countsFor('all')).toEqual({home: 2, game: 3, bases: 2});
  });

  it('counts the phones on their own', () => {
    expect(countsFor('Mobile')).toEqual({home: 1, game: 1, bases: 1});
  });

  it('counts the desktops on their own', () => {
    expect(countsFor('Desktop')).toEqual({home: 1, game: 1, bases: 1});
  });

  it('keeps a visit without any user agent apart rather than folding it into a device', () => {
    expect(countsFor('Unknown')).toEqual({home: 0, game: 1, bases: 0});
  });

  it('reads the device off the first record that names one', () => {
    // The later beacons of a cookie-less browser arrive without a user agent; the visit is still
    // the phone that fired the first one.
    const analyzer = analyzerFor('all',
      [request(PageRequestType.GAME, 'session-1', {twclid: 'tw-1', userAgent: PHONE})],
      [startup(true, 'session-2', {twclid: 'tw-1'}, 'game-1')]);
    analyzer.setDevice('Mobile');

    expect(analyzer.countGame()).toBe(1);
    expect(analyzer.countEngineRunning()).toBe(1);
  });

  it('applies both filters at once', () => {
    const analyzer = visitors();
    analyzer.setView(TrackingPlatform.X);
    analyzer.setDevice('Mobile');

    expect(analyzer.countGame()).toBe(1);
  });
});
