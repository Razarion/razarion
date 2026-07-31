import {AttentionAnalyzer} from './attention-analyzer';
import {
  StartupTerminatedJson,
  TabHiddenJson,
  TrackingContainer,
  UserActivity,
  UserActivityType
} from '../../generated/razarion-share';

/**
 * The numbers here are read as evidence for what to build next, so the cases that must not blur
 * are the ones covered: a tab switch against a closed page, an abort the server only guessed at,
 * and a startup that cannot be traced to a player at all.
 */
describe('AttentionAnalyzer', () => {

  function aborted(gameSessionUuid: string, hidden: boolean | null): StartupTerminatedJson {
    return {gameSessionUuid, successful: false, aborted: true, hidden} as unknown as StartupTerminatedJson;
  }

  function successful(gameSessionUuid: string): StartupTerminatedJson {
    return {gameSessionUuid, successful: true, aborted: false} as unknown as StartupTerminatedJson;
  }

  function tabHidden(gameSessionUuid: string, millisSincePageLoad: number): TabHiddenJson {
    return {gameSessionUuid, millisSincePageLoad} as unknown as TabHiddenJson;
  }

  function activity(userActivityType: UserActivityType, userId: string, gameSessionUuid?: string): UserActivity {
    return {userActivityType, userId, gameSessionUuid} as unknown as UserActivity;
  }

  function report(trackingContainer: Partial<TrackingContainer>) {
    const attentionAnalyzer = new AttentionAnalyzer();
    attentionAnalyzer.setTrackingContainer({
      startupTerminatedJson: [],
      startupTaskJsons: [],
      userActivities: [],
      pageRequests: [],
      tabHiddenJsons: [],
      ...trackingContainer
    } as TrackingContainer);
    return attentionAnalyzer.createReport();
  }

  it('keeps a tab switch, a closed page and a guess apart', () => {
    const abandoned = report({
      startupTerminatedJson: [
        aborted('a', true),
        aborted('b', false),
        // Reconstructed from the tasks: no beacon ever arrived, so neither case can be claimed.
        aborted('c', null)
      ]
    }).abandoned;

    expect(abandoned.tabbedAway).toBe(1);
    expect(abandoned.leftPage).toBe(1);
    expect(abandoned.unknown).toBe(1);
  });

  it('counts a session that went to the background twice as one', () => {
    const timing = report({
      startupTerminatedJson: [successful('a')],
      tabHiddenJsons: [tabHidden('a', 4000), tabHidden('a', 90000)]
    }).timing;

    expect(timing.successfulStartups).toBe(1);
    expect(timing.backgrounded).toBe(1);
  });

  /**
   * The range picker cuts both collections at the same instants, but a game backgrounded shortly
   * after the range opens can have started before it. Counting that record would put it against a
   * population it is not part of - and could push the share above 100%.
   */
  it('ignores a background record whose startup is outside the range', () => {
    const timing = report({
      startupTerminatedJson: [successful('a')],
      tabHiddenJsons: [tabHidden('a', 1000), tabHidden('older-session', 1000)]
    }).timing;

    expect(timing.backgrounded).toBe(1);
  });

  it('reports the median wait and the slice each background falls into', () => {
    const timing = report({
      startupTerminatedJson: [successful('a'), successful('b'), successful('c')],
      tabHiddenJsons: [tabHidden('a', 2000), tabHidden('b', 30000), tabHidden('c', 600000)]
    }).timing;

    expect(timing.medianSeconds).toBe(30);
    expect(timing.buckets.map(bucket => bucket.count)).toEqual([1, 1, 0, 1]);
  });

  it('has no median when nobody looked away', () => {
    expect(report({startupTerminatedJson: [successful('a')]}).timing.medianSeconds).toBeNull();
  });

  it('splits the players by whether they watched, and says what each group achieved', () => {
    const attentionReport = report({
      startupTerminatedJson: [successful('looked-away'), successful('watched')],
      tabHiddenJsons: [tabHidden('looked-away', 5000)],
      userActivities: [
        activity(UserActivityType.GAME_SESSION_STARTED, 'user-1', 'looked-away'),
        activity(UserActivityType.GAME_SESSION_STARTED, 'user-2', 'watched'),
        activity(UserActivityType.BASE_CREATED, 'user-2'),
        activity(UserActivityType.QUEST_PASSED, 'user-2')
      ]
    });

    const [backgrounded, stayed] = attentionReport.cohorts;
    expect(backgrounded.users).toBe(1);
    expect(backgrounded.baseCreated).toBe(0);
    expect(stayed.users).toBe(1);
    expect(stayed.baseCreated).toBe(1);
    expect(stayed.questPassed).toBe(1);
  });

  /**
   * Sessions recorded before GAME_SESSION_STARTED existed carry no user. Dropping them silently
   * would leave two tiny cohorts that look like the whole picture.
   */
  it('counts the startups that cannot be traced to a player', () => {
    const attentionReport = report({
      startupTerminatedJson: [successful('traceable'), successful('orphan')],
      userActivities: [activity(UserActivityType.GAME_SESSION_STARTED, 'user-1', 'traceable')]
    });

    expect(attentionReport.unlinkedSessions).toBe(1);
    expect(attentionReport.cohorts.reduce((sum, cohort) => sum + cohort.sessions, 0)).toBe(1);
  });
});
