import {classifyDevice, FirstInteractionAnalyzer} from './first-interaction-analyzer';
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
});
