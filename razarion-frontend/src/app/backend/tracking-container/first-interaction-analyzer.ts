import {
  FirstInteractionJson,
  StartupTaskJson,
  TrackingContainer,
  UserActivity,
  UserActivityType
} from '../../generated/razarion-share';

/**
 * Whether the players found the controls, split by the device they were holding.
 *
 * The funnel counts how many stop before the first quest but cannot say whether they could steer
 * at all, and the attention view only says whether they were looking. Absence is the signal here:
 * a session that never reports CAMERA_PAN_TOUCH never found the gesture. The denominator is
 * therefore every session that got a running game, not the sessions that reported something -
 * measuring the reporters against themselves would hide exactly the players this exists to find.
 */

/** The kinds the client reports, in the order they are shown. */
export const INTERACTION_KINDS = [
  'POINTER_DOWN',
  'POINTER_DOWN_PAGE',
  'CLIENT_BUILD',
  'ENGINE_ERROR',
  'PLACER_SHOWN',
  'PLACER_REJECTED',
  'PLACER_CONFIRMED',
  'PLACER_NO_TERRAIN',
  'CAMERA_PAN_TOUCH',
  'CAMERA_PINCH',
  'CAMERA_KEYBOARD',
  'CAMERA_WHEEL',
  'SELECT',
  'COMMAND'
] as const;

export type InteractionKind = typeof INTERACTION_KINDS[number];

/**
 * The kinds fall into three classes, and every share on this page depends on keeping them apart.
 *
 * These are not the player at all: the game put the base placer on screen, the placer had to
 * position itself without terrain, the client said which build it is, and the engine reported a
 * failure of its own. Both are recorded because the questions they answer - "was this
 * player ever asked to place a base", "did the ground exist when we asked" - cannot otherwise be
 * asked. But a session in which only these happened is a session in which the player did nothing,
 * and must still read as silent.
 */
const NOT_THE_PLAYER: InteractionKind[] = ['PLACER_SHOWN', 'PLACER_NO_TERRAIN', 'CLIENT_BUILD',
  'ENGINE_ERROR'];

/**
 * The player reached and got nothing out of it: a finger on the game field that led nowhere, a
 * finger that reached the page but never the canvas, or a tap on a spot where a base cannot go. Both are the opposite of PLACER_CONFIRMED and must not be
 * counted as having achieved anything - that would hide exactly the players this view exists for.
 */
const REACHED_WITHOUT_RESULT: InteractionKind[] = ['POINTER_DOWN', 'POINTER_DOWN_PAGE', 'PLACER_REJECTED'];

/** Did the player do anything at all, however fruitless? */
function reachedForIt(session: SessionFacts): boolean {
  for (const kind of session.firstMillis.keys()) {
    if (!NOT_THE_PLAYER.includes(kind)) {
      return true;
    }
  }
  return false;
}

/** Did this session do something that worked, as opposed to merely reaching for it? */
function didSomething(session: SessionFacts): boolean {
  for (const kind of session.firstMillis.keys()) {
    if (!NOT_THE_PLAYER.includes(kind) && !REACHED_WITHOUT_RESULT.includes(kind)) {
      return true;
    }
  }
  return false;
}

export type DeviceClass = 'Mobile' | 'Tablet' | 'Desktop' | 'Unknown';

/** The order the device rows are shown in; Unknown last because it is a gap, not a group. */
const DEVICE_CLASSES: DeviceClass[] = ['Mobile', 'Tablet', 'Desktop', 'Unknown'];

/** One control, against the sessions of one device class. */
export interface InteractionRow {
  kind: InteractionKind;
  sessions: number;
  percent: number | null;
  /** Seconds from page load to the first time it happened, median. Null when it never happened. */
  medianSeconds: number | null;
}

/**
 * The steps in order, each one the players of the step above it who also did this. Nested on
 * purpose: read as two independent shares, the rows produce percentages over 100% - more sessions
 * select than move the camera, because tapping a unit needs no panning, and more users pass a
 * quest than were seen giving an order, because a quest is recorded per user and needs no
 * interaction record of its own. The unconditional counts stay in the per-control table, so
 * nesting the funnel loses nothing.
 * <p>
 * "Moved the camera" is deliberately not a step. It is not something the later steps require, and
 * whether the gesture was found is exactly what the per-control table already answers.
 */
export interface InteractionFunnel {
  started: number;
  /**
   * Touched the game field at all, whatever came of it. The gap between this and
   * {@link interacted} is the finding: a player who reached for the game and got no answer.
   */
  touched: number;
  /**
   * Reported a control that did something - the touch itself does not count. The rest of the chain
   * is a subset of this.
   */
  interacted: number;
  selected: number;
  commanded: number;
  questPassed: number;
}

export interface DeviceReport {
  device: DeviceClass;
  /** Sessions whose game came up - the denominator for every number in the report. */
  started: number;
  /** Sessions that reported nothing at all, not even a touch. */
  silent: number;
  /**
   * Sessions that touched the game field and never got anything out of it. Where {@link silent}
   * leaves open whether the player was ever there, this does not: they reached for it.
   */
  touchedWithoutEffect: number;
  rows: InteractionRow[];
  funnel: InteractionFunnel;
}

export interface FirstInteractionReport {
  devices: DeviceReport[];
  /**
   * Sessions whose userAgent says desktop while the session reported a touch gesture. Samsung and
   * the desktop-site setting send a desktop userAgent from a phone, so the device split undercounts
   * mobile by however many these are - shown rather than silently folded into mobile, because the
   * correction is a guess and the size of it is the useful part.
   */
  touchOnDesktopUserAgent: number;
  /**
   * Interactions whose session has no PAGE_LOADED task in the range. A session that started before
   * the range began lands here, so a short range is expected to have some.
   */
  unlinkedInteractions: number;
}

/** What one session did, assembled from the records that mention it. */
interface SessionFacts {
  device: DeviceClass;
  userId?: string;
  /** Kind to the earliest millisSincePageLoad reported for it. */
  firstMillis: Map<InteractionKind, number>;
}

export class FirstInteractionAnalyzer {
  private trackingContainer?: TrackingContainer;

  setTrackingContainer(trackingContainer: TrackingContainer) {
    this.trackingContainer = trackingContainer;
  }

  createReport(): FirstInteractionReport {
    const sessions = this.sessionFacts();
    const questPassedUserIds = this.userIdsWith(UserActivityType.QUEST_PASSED);

    const devices = DEVICE_CLASSES
      .map(device => this.deviceReport(device, sessions, questPassedUserIds))
      // A device nobody used is noise, not a zero worth reading.
      .filter(deviceReport => deviceReport.started > 0);

    return {
      devices,
      touchOnDesktopUserAgent: this.touchOnDesktopUserAgent(sessions),
      unlinkedInteractions: this.unlinkedInteractions()
    };
  }

  /**
   * Every session that reached a running game, with whatever it went on to report.
   * <p>
   * RUN_GAME rather than a finished startup: it is the task that means the engine is up and the
   * player could act, which is the only population where "never touched anything" says something
   * about the controls rather than about the loading.
   */
  private sessionFacts(): Map<string, SessionFacts> {
    const deviceBySession = new Map<string, DeviceClass>();
    for (const startupTaskJson of this.startupTaskJsons()) {
      if (startupTaskJson.taskEnum !== 'PAGE_LOADED' || !startupTaskJson.gameSessionUuid) {
        continue;
      }
      // First one wins: a session reloads its page tasks but stays the same device.
      if (!deviceBySession.has(startupTaskJson.gameSessionUuid)) {
        deviceBySession.set(startupTaskJson.gameSessionUuid, classifyDevice(startupTaskJson.userAgent));
      }
    }

    const sessions = new Map<string, SessionFacts>();
    for (const startupTaskJson of this.startupTaskJsons()) {
      if (startupTaskJson.taskEnum !== 'RUN_GAME' || !startupTaskJson.gameSessionUuid) {
        continue;
      }
      if (!sessions.has(startupTaskJson.gameSessionUuid)) {
        sessions.set(startupTaskJson.gameSessionUuid, {
          device: deviceBySession.get(startupTaskJson.gameSessionUuid) ?? 'Unknown',
          firstMillis: new Map<InteractionKind, number>()
        });
      }
    }

    for (const firstInteractionJson of this.firstInteractionJsons()) {
      const session = sessions.get(firstInteractionJson.gameSessionUuid);
      if (!session) {
        continue;
      }
      session.userId = firstInteractionJson.userId ?? session.userId;
      const kind = firstInteractionJson.kind as InteractionKind;
      const millis = firstInteractionJson.millisSincePageLoad;
      if (millis == null) {
        // Still counts as "it happened" - only the timing is missing.
        if (!session.firstMillis.has(kind)) {
          session.firstMillis.set(kind, Number.NaN);
        }
        continue;
      }
      const known = session.firstMillis.get(kind);
      if (known === undefined || Number.isNaN(known) || millis < known) {
        session.firstMillis.set(kind, millis);
      }
    }
    return sessions;
  }

  private deviceReport(device: DeviceClass,
                       sessions: Map<string, SessionFacts>,
                       questPassedUserIds: Set<string>): DeviceReport {
    const own: SessionFacts[] = [];
    sessions.forEach(session => {
      if (session.device === device) {
        own.push(session);
      }
    });

    const rows = INTERACTION_KINDS.map(kind => {
      const withKind = own.filter(session => session.firstMillis.has(kind));
      const seconds = withKind
        .map(session => session.firstMillis.get(kind)!)
        .filter(millis => !Number.isNaN(millis))
        .map(millis => millis / 1000);
      return {
        kind,
        sessions: withKind.length,
        percent: own.length > 0 ? Math.round(withKind.length / own.length * 100) : null,
        medianSeconds: median(seconds)
      };
    });

    // Each step narrows the one before it, so every share is of a population this one is part of.
    const touched = own.filter(reachedForIt);
    const interacted = own.filter(didSomething);
    const selected = interacted.filter(session => session.firstMillis.has('SELECT'));
    const commanded = selected.filter(session => session.firstMillis.has('COMMAND'));
    // By user, because that is how a quest is recorded - the session it happened in is not on the
    // record. Narrowed to the sessions that gave an order like every other step.
    const questPassed = commanded.filter(session => !!session.userId && questPassedUserIds.has(session.userId));

    return {
      device,
      started: own.length,
      silent: own.length - touched.length,
      touchedWithoutEffect: touched.length - interacted.length,
      rows,
      funnel: {
        started: own.length,
        touched: touched.length,
        interacted: interacted.length,
        selected: selected.length,
        commanded: commanded.length,
        questPassed: questPassed.length
      }
    };
  }

  /** See {@link FirstInteractionReport.touchOnDesktopUserAgent}. */
  private touchOnDesktopUserAgent(sessions: Map<string, SessionFacts>): number {
    let count = 0;
    sessions.forEach(session => {
      if (session.device !== 'Desktop') {
        return;
      }
      if (session.firstMillis.has('CAMERA_PAN_TOUCH') || session.firstMillis.has('CAMERA_PINCH')) {
        count++;
      }
    });
    return count;
  }

  private unlinkedInteractions(): number {
    const known = new Set(this.startupTaskJsons()
      .filter(startupTaskJson => startupTaskJson.taskEnum === 'PAGE_LOADED')
      .map(startupTaskJson => startupTaskJson.gameSessionUuid));
    return this.firstInteractionJsons()
      .filter(firstInteractionJson => !known.has(firstInteractionJson.gameSessionUuid))
      .length;
  }

  private userIdsWith(userActivityType: UserActivityType): Set<string> {
    return new Set(this.userActivities()
      .filter(userActivity => userActivity.userActivityType === userActivityType)
      .map(userActivity => userActivity.userId));
  }

  private firstInteractionJsons(): FirstInteractionJson[] {
    return this.trackingContainer?.firstInteractionJsons || [];
  }

  private startupTaskJsons(): StartupTaskJson[] {
    return this.trackingContainer?.startupTaskJsons || [];
  }

  private userActivities(): UserActivity[] {
    return this.trackingContainer?.userActivities || [];
  }
}

/**
 * Which device sent this, as far as the userAgent admits it.
 * <p>
 * Tablet before mobile: an Android tablet carries "Android" too, and iPadOS sends a Macintosh
 * userAgent with no phone token at all, so it lands in Desktop - see
 * {@link FirstInteractionReport.touchOnDesktopUserAgent} for the size of that error.
 */
export function classifyDevice(userAgent?: string): DeviceClass {
  if (!userAgent) {
    return 'Unknown';
  }
  if (/iPad|Tablet/i.test(userAgent)) {
    return 'Tablet';
  }
  if (/Mobile|Android|iPhone|iPod/i.test(userAgent)) {
    return 'Mobile';
  }
  // The Facebook app fetching for itself sends only its own token - no browser string, and so none
  // of the words above. FB4A is Facebook for Android, FBIOS is Facebook for iOS, so the device is
  // known exactly; without this the whole lot counted as desktops. See isAppFetch.
  if (/^\[FBAN\/(FB4A|FBIOS)/i.test(userAgent.trim())) {
    return 'Mobile';
  }
  return 'Desktop';
}

/**
 * Whether the Facebook app fetched this page for itself rather than a browser showing it to
 * somebody. Such a request carries the app's token alone, with no browser string in front of it - a
 * real in-app browser sends the full Mozilla string and appends the token.
 *
 * Over seven days of PROD these 952 requests produced no play click, no exit event, no game page
 * and no client start, and not one was ever reported visible. They are the app warming a link, and
 * counting them as landing page views made every Meta conversion rate a fifth too small.
 *
 * Kept in step with TrackingDevice.isAppFetch() on the server.
 */
export function isAppFetch(userAgent?: string): boolean {
  return !!userAgent && /^\[FBAN/i.test(userAgent.trim());
}

/** Median of an unsorted list, or null when there is nothing to take one of. */
function median(values: number[]): number | null {
  if (values.length === 0) {
    return null;
  }
  const sorted = [...values].sort((left, right) => left - right);
  const middle = Math.floor(sorted.length / 2);
  return sorted.length % 2 === 0
    ? (sorted[middle - 1] + sorted[middle]) / 2
    : sorted[middle];
}
