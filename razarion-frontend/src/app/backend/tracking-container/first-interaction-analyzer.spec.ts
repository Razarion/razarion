import {classifyDevice, FirstInteractionAnalyzer, isAppFetch} from './first-interaction-analyzer';
import {
  FirstInteractionJson,
  StartupTaskJson,
  TrackingContainer,
  UserActivity,
  UserActivityType
} from '../../generated/razarion-share';

/**
 * The point of this view is that a control nobody used has to read as a number rather than as a
 * missing row, so the cases covered are the ones where that could silently break: a session that
 * reported nothing, a denominator taken from the reporters instead of from the started sessions,
 * and a phone that sends a desktop userAgent.
 */
describe('FirstInteractionAnalyzer', () => {

  const PHONE = 'Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Mobile Safari/537.36';
  const DESKTOP = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/149 Safari/537.36';

  function pageLoaded(gameSessionUuid: string, userAgent: string): StartupTaskJson {
    return {gameSessionUuid, taskEnum: 'PAGE_LOADED', userAgent} as unknown as StartupTaskJson;
  }

  function runGame(gameSessionUuid: string): StartupTaskJson {
    return {gameSessionUuid, taskEnum: 'RUN_GAME'} as unknown as StartupTaskJson;
  }

  function interaction(gameSessionUuid: string, kind: string, millisSincePageLoad: number,
                       userId = 'user-' + gameSessionUuid): FirstInteractionJson {
    return {gameSessionUuid, kind, millisSincePageLoad, userId} as unknown as FirstInteractionJson;
  }

  function activity(userActivityType: UserActivityType, userId: string): UserActivity {
    return {userActivityType, userId} as unknown as UserActivity;
  }

  function report(trackingContainer: Partial<TrackingContainer>) {
    const firstInteractionAnalyzer = new FirstInteractionAnalyzer();
    firstInteractionAnalyzer.setTrackingContainer({
      startupTerminatedJson: [],
      startupTaskJsons: [],
      userActivities: [],
      pageRequests: [],
      tabHiddenJsons: [],
      firstInteractionJsons: [],
      ...trackingContainer
    } as TrackingContainer);
    return firstInteractionAnalyzer.createReport();
  }

  function device(trackingContainer: Partial<TrackingContainer>, name: string) {
    return report(trackingContainer).devices.find(deviceReport => deviceReport.device === name)!;
  }

  /**
   * The placer being shown is the game asking, not the player answering. A session where only that
   * happened is a session in which nobody did anything, and it has to keep reading as silent -
   * otherwise the one number this view was built for goes to zero the day the event ships.
   */
  it('does not mistake the game asking for the player answering', () => {
    const mobile = device({
      startupTaskJsons: [pageLoaded('a', PHONE), runGame('a')],
      firstInteractionJsons: [interaction('a', 'PLACER_SHOWN', 9000)]
    }, 'Mobile');

    expect(mobile.started).toBe(1);
    expect(mobile.silent).toBe(1);
    expect(mobile.funnel.touched).toBe(0);
    expect(mobile.funnel.interacted).toBe(0);
    // The row still says it happened - that is the point of recording it.
    expect(mobile.rows.find(row => row.kind === 'PLACER_SHOWN')!.sessions).toBe(1);
  });

  /**
   * Tapping a spot where a base cannot go is reaching and getting nothing - the same shape as a
   * finger that led nowhere, and the opposite of a placement that went through.
   */
  it('counts a refused placement as reaching, not as achieving', () => {
    const mobile = device({
      startupTaskJsons: [pageLoaded('a', PHONE), runGame('a')],
      firstInteractionJsons: [
        interaction('a', 'PLACER_SHOWN', 9000),
        interaction('a', 'PLACER_REJECTED', 11000)
      ]
    }, 'Mobile');

    expect(mobile.silent).toBe(0);
    expect(mobile.funnel.touched).toBe(1);
    expect(mobile.funnel.interacted).toBe(0);
    expect(mobile.touchedWithoutEffect).toBe(1);
  });

  /** A placement that went through is an achievement like any other. */
  it('counts a confirmed placement as achieving', () => {
    const mobile = device({
      startupTaskJsons: [pageLoaded('a', PHONE), runGame('a')],
      firstInteractionJsons: [
        interaction('a', 'PLACER_SHOWN', 9000),
        interaction('a', 'PLACER_CONFIRMED', 12000)
      ]
    }, 'Mobile');

    expect(mobile.funnel.touched).toBe(1);
    expect(mobile.funnel.interacted).toBe(1);
    expect(mobile.touchedWithoutEffect).toBe(0);
  });

  /**
   * The distinction the POINTER_DOWN kind exists for. Session 'a' reached for the game and got
   * nothing back; session 'b' never reached at all. Both used to be one number - "reported
   * nothing" - and they call for opposite repairs: a control that does not answer, against a
   * player who was never interested.
   */
  it('separates a finger that got no answer from a player who never reached for it', () => {
    const mobile = device({
      startupTaskJsons: [
        pageLoaded('a', PHONE), runGame('a'),
        pageLoaded('b', PHONE), runGame('b')
      ],
      firstInteractionJsons: [interaction('a', 'POINTER_DOWN', 4000)]
    }, 'Mobile');

    expect(mobile.started).toBe(2);
    // 'a' touched the screen, so it is not silent - and it achieved nothing, so it is not counted
    // as an interaction either.
    expect(mobile.silent).toBe(1);
    expect(mobile.touchedWithoutEffect).toBe(1);
    expect(mobile.funnel.touched).toBe(1);
    expect(mobile.funnel.interacted).toBe(0);
  });

  /** A touch that led somewhere is not a touch that led nowhere, however loudly it was reported. */
  it('does not count a touch that worked as a touch that went nowhere', () => {
    const mobile = device({
      startupTaskJsons: [pageLoaded('a', PHONE), runGame('a')],
      firstInteractionJsons: [
        interaction('a', 'POINTER_DOWN', 4000),
        interaction('a', 'SELECT', 4200)
      ]
    }, 'Mobile');

    expect(mobile.touchedWithoutEffect).toBe(0);
    expect(mobile.funnel.touched).toBe(1);
    expect(mobile.funnel.interacted).toBe(1);
    expect(mobile.funnel.selected).toBe(1);
  });

  it('counts a session that reported nothing against the control, not out of the population', () => {
    // The whole reason the collection exists: silence is the finding. If the denominator were the
    // reporting sessions, both of these would read 100% and the silent player would vanish.
    const mobile = device({
      startupTaskJsons: [
        pageLoaded('a', PHONE), runGame('a'),
        pageLoaded('b', PHONE), runGame('b')
      ],
      firstInteractionJsons: [interaction('a', 'CAMERA_PAN_TOUCH', 5000)]
    }, 'Mobile');

    expect(mobile.started).toBe(2);
    expect(mobile.silent).toBe(1);
    expect(mobile.rows.find(row => row.kind === 'CAMERA_PAN_TOUCH')!.percent).toBe(50);
  });

  it('leaves a control the device cannot produce at zero rather than dropping the row', () => {
    const mobile = device({
      startupTaskJsons: [pageLoaded('a', PHONE), runGame('a')],
      firstInteractionJsons: [interaction('a', 'CAMERA_PAN_TOUCH', 5000)]
    }, 'Mobile');

    const wheel = mobile.rows.find(row => row.kind === 'CAMERA_WHEEL')!;
    expect(wheel.sessions).toBe(0);
    expect(wheel.percent).toBe(0);
    expect(wheel.medianSeconds).toBeNull();
  });

  it('ignores a session that never got a running game', () => {
    // A player who left while loading says nothing about the controls - they never saw them.
    const report1 = report({
      startupTaskJsons: [pageLoaded('a', PHONE)],
      firstInteractionJsons: [interaction('a', 'SELECT', 1000)]
    });

    expect(report1.devices).toEqual([]);
  });

  it('measures each funnel step against the step above it', () => {
    const mobile = device({
      startupTaskJsons: [
        pageLoaded('a', PHONE), runGame('a'),
        pageLoaded('b', PHONE), runGame('b'),
        pageLoaded('c', PHONE), runGame('c'),
        pageLoaded('d', PHONE), runGame('d')
      ],
      firstInteractionJsons: [
        interaction('a', 'CAMERA_PAN_TOUCH', 1000), interaction('a', 'SELECT', 2000),
        interaction('a', 'COMMAND', 3000),
        interaction('b', 'CAMERA_PAN_TOUCH', 1000), interaction('b', 'SELECT', 2000),
        interaction('c', 'CAMERA_PAN_TOUCH', 1000)
      ],
      userActivities: [activity(UserActivityType.QUEST_PASSED, 'user-a')]
    }, 'Mobile');

    expect(mobile.funnel.started).toBe(4);
    expect(mobile.funnel.interacted).toBe(3);
    expect(mobile.funnel.selected).toBe(2);
    expect(mobile.funnel.commanded).toBe(1);
    expect(mobile.funnel.questPassed).toBe(1);
  });

  it('never lets a step exceed the one above it', () => {
    // Both of these produced a percentage over 100% against real data: selecting needs no panning,
    // and a quest is recorded per user whether or not that session was seen giving an order.
    const mobile = device({
      startupTaskJsons: [
        pageLoaded('a', PHONE), runGame('a'),
        pageLoaded('b', PHONE), runGame('b')
      ],
      firstInteractionJsons: [
        // Tapped a unit without ever moving the camera.
        interaction('a', 'SELECT', 2000),
        interaction('b', 'CAMERA_PAN_TOUCH', 1000)
      ],
      userActivities: [
        // Passed a quest without a COMMAND ever being recorded for the session.
        activity(UserActivityType.QUEST_PASSED, 'user-a'),
        activity(UserActivityType.QUEST_PASSED, 'user-b')
      ]
    }, 'Mobile');

    const funnel = mobile.funnel;
    expect(funnel.interacted).toBeLessThanOrEqual(funnel.started);
    expect(funnel.selected).toBeLessThanOrEqual(funnel.interacted);
    expect(funnel.commanded).toBeLessThanOrEqual(funnel.selected);
    expect(funnel.questPassed).toBeLessThanOrEqual(funnel.commanded);
    expect(funnel.selected).toBe(1);
    expect(funnel.commanded).toBe(0);
    expect(funnel.questPassed).toBe(0);
  });

  it('takes the earliest report of a kind and reports the median in seconds', () => {
    const mobile = device({
      startupTaskJsons: [
        pageLoaded('a', PHONE), runGame('a'),
        pageLoaded('b', PHONE), runGame('b')
      ],
      firstInteractionJsons: [
        interaction('a', 'SELECT', 30000),
        // A duplicate must not move the median, and the earlier one is the first use.
        interaction('a', 'SELECT', 10000),
        interaction('b', 'SELECT', 20000)
      ]
    }, 'Mobile');

    const select = mobile.rows.find(row => row.kind === 'SELECT')!;
    expect(select.sessions).toBe(2);
    expect(select.medianSeconds).toBe(15);
  });

  it('counts a touch gesture under a desktop userAgent so the undercount is visible', () => {
    // Samsung and the desktop-site setting send a desktop userAgent from a phone. The session stays
    // in Desktop - correcting it would be a guess - but the size of the error must be on the page.
    const full = report({
      startupTaskJsons: [pageLoaded('a', DESKTOP), runGame('a')],
      firstInteractionJsons: [interaction('a', 'CAMERA_PAN_TOUCH', 5000)]
    });

    expect(full.touchOnDesktopUserAgent).toBe(1);
    expect(full.devices.find(deviceReport => deviceReport.device === 'Desktop')!.started).toBe(1);
  });

  it('reports interactions whose session started before the range instead of hiding them', () => {
    const full = report({
      startupTaskJsons: [pageLoaded('a', PHONE), runGame('a')],
      firstInteractionJsons: [interaction('a', 'SELECT', 1000), interaction('older', 'SELECT', 1000)]
    });

    expect(full.unlinkedInteractions).toBe(1);
  });

  it('classifies a tablet before a phone and admits when it cannot tell', () => {
    // An Android tablet carries "Android" too, so the order of the tests is what decides this.
    expect(classifyDevice('Mozilla/5.0 (Linux; Android 14; Tablet) Safari/537.36')).toBe('Tablet');
    expect(classifyDevice('Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) Safari/605.1')).toBe('Tablet');
    expect(classifyDevice(PHONE)).toBe('Mobile');
    expect(classifyDevice(DESKTOP)).toBe('Desktop');
    expect(classifyDevice(undefined)).toBe('Unknown');
  });

  /**
   * The user agent that made every Meta conversion rate wrong. The Facebook app fetching a link for
   * itself sends its own token and no browser string, so it contains none of the words the
   * classification looks for and landed in Desktop - 937 of 952 over seven days, all of them
   * phones. The screen is right there in the string: FBDM says 1080 by 2340.
   *
   * Kept in step with TrackingDevice.of() on the server, which answers the same question for the
   * Daily table.
   */
  it('reads the Facebook app own fetch as the phone it is', () => {
    expect(classifyDevice('[FBAN/FB4A;FBAV/575.1.0.55.73;FBDM/{density=2.8125,width=1080,height=2340};]'))
      .toBe('Mobile');
    expect(classifyDevice('[FBAN/FBIOS;FBAV/510.0.0.44.107;FBBV/1234;]')).toBe('Mobile');
  });

  /**
   * And it is not a visit: it fires the pixel like any other render but can never click, so left in
   * the funnel it sits in the denominator of every rate while being incapable of appearing in any
   * numerator.
   */
  it('does not count the app own fetch as somebody looking at the page', () => {
    expect(isAppFetch('[FBAN/FB4A;FBAV/575.1.0.55.73;]')).toBeTrue();
    expect(isAppFetch('  [FBAN/FBIOS;FBAV/510.0.0.44.107;]')).toBeTrue();
  });

  /**
   * The in-app browser is the opposite case and must stay in - a person really is looking. It sends
   * the whole browser string and appends the token, so what separates them is whether the token
   * stands alone.
   */
  it('keeps the in-app browser, which is a visitor like any other', () => {
    const inAppBrowser = 'Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) '
      + 'Chrome/151.0.0.0 Mobile Safari/537.36 [FBAN/FBAV;FBAV/575.0.0.48.76;]';
    expect(isAppFetch(inAppBrowser)).toBeFalse();
    expect(classifyDevice(inAppBrowser)).toBe('Mobile');
  });

  it('leaves ordinary user agents alone', () => {
    expect(isAppFetch(undefined)).toBeFalse();
    expect(isAppFetch('')).toBeFalse();
    expect(isAppFetch('Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/149 Safari/537.36')).toBeFalse();
    expect(classifyDevice('Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/149 Safari/537.36')).toBe('Desktop');
  });
});
