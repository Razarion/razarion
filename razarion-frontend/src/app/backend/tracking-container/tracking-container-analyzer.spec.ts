import {TrackingContainerAnalyzer} from './tracking-container-analyzer';
import {
  PageRequest,
  PageRequestType,
  TrackingContainer,
  UserActivity,
  UserActivityType
} from '../../generated/razarion-share';

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
   * whole chain the funnel walks: a home visit, a game visit under the same click id, the user
   * created in that http session, and a base.
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
   * The distinct-home step keys on the click id, so every player would collapse into one row if
   * they shared one. Each gets their own.
   */
  function statistics(players: ReturnType<typeof player>[]) {
    const analyzer = new TrackingContainerAnalyzer();
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
