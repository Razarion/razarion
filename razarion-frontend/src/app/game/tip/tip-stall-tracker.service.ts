import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {TipStallJson, TrackerControllerImplClient} from '../../generated/razarion-share';
import {TypescriptGenerator} from '../../backend/typescript-generator';
import {TipStallReason, TipStallSource} from './tip-stall';

/**
 * Watches the running tip task and reports the ones that stop making progress.
 *
 * The tip tasks retry forever by design - the client engine may still be syncing, a button may
 * still be disabled, a resource tile may still be streaming in. That is the right behaviour, but
 * it used to be completely silent: a player stuck in front of a tip that can never advance looked
 * exactly like a player taking their time, and quests with a tip have the regular quest
 * visualisation switched off, so a stuck tip is a dead end. This turns that silence into one
 * document per stall.
 *
 * Reports twice for the same stall: once when the watchdog fires and again if the player gets
 * past it. Stalls without a resolution are the interesting ones.
 */
@Injectable({
  providedIn: 'root'
})
export class TipStallTrackerService {
  /**
   * How long a single tip task may run before it counts as stalled. Generous: reading the prompt,
   * scrolling to the target and clicking is easily fifteen seconds for a new player, and a false
   * stall is worse than a late one - it would put the blame on tips that work.
   */
  public static readonly STALL_MILLIS = 30000;
  /**
   * A tip chain that keeps falling back a step is broken, and it is invisible to the watchdog:
   * every restart arms a fresh timer, so the 30 seconds are never reached and not a single record
   * is written. A build tip that failed itself on every placement went unnoticed exactly this way.
   *
   * Counted in backtracks rather than in task starts. Counting starts needs a threshold high
   * enough to clear a chain running forward, and a loop pacing itself at the 1500 ms selection
   * grace stayed just under it - invisible again. A chain going forward never backtracks at all,
   * so three of them inside the window is nothing but a loop.
   */
  public static readonly THRASHING_FAILURES = 3;
  public static readonly THRASHING_WINDOW_MILLIS = 10000;
  private readonly trackerControllerImplClient: TrackerControllerImplClient;
  private questId: number | null = null;
  private source: TipStallSource | null = null;
  private taskStartTime = 0;
  private watchdogTimeout: ReturnType<typeof setTimeout> | null = null;
  /** Set once a stall was reported for the running task; the resolution refers back to it. */
  private stallUuid: string | null = null;
  /** Backtrack times of the recent past, for the restart-loop check. */
  private recentFailureTimes: number[] = [];
  private thrashingReported = false;

  constructor(httpClient: HttpClient) {
    this.trackerControllerImplClient = new TrackerControllerImplClient(
      TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  /** A tip task was started. Ends the watch on the previous one without reporting a resolution. */
  public taskStarted(questId: number | null, source: TipStallSource): void {
    this.reset();
    this.questId = questId;
    this.source = source;
    this.taskStartTime = Date.now();
    this.armWatchdog();
  }

  /** Reports a chain that keeps falling back, once per burst rather than once per backtrack. */
  private checkThrashing(): void {
    const now = Date.now();
    this.recentFailureTimes = this.recentFailureTimes
      .filter(failureTime => now - failureTime < TipStallTrackerService.THRASHING_WINDOW_MILLIS);
    this.recentFailureTimes.push(now);
    if (this.recentFailureTimes.length < TipStallTrackerService.THRASHING_FAILURES) {
      this.thrashingReported = false;
      return;
    }
    if (this.thrashingReported) {
      return;
    }
    this.thrashingReported = true;
    this.report({
      stallUuid: TipStallTrackerService.newUuid(),
      questId: this.questId ?? undefined,
      tipTaskName: this.source?.getTaskName(),
      reason: TipStallReason.CHAIN_THRASHING,
      waitMillis: now - this.recentFailureTimes[0],
      resolved: false
    });
  }

  /**
   * The running task is over. A task that stalled and then succeeded reports the resolution;
   * one that failed does not - the chain backtracks and the player is still where they were.
   */
  public taskEnded(succeeded: boolean): void {
    if (succeeded && this.stallUuid) {
      this.report({
        stallUuid: this.stallUuid,
        resolved: true,
        resolvedMillis: Date.now() - this.taskStartTime
      });
    }
    if (!succeeded) {
      // Before reset(): the report names the task that gave up.
      this.checkThrashing();
    }
    this.reset();
  }

  /**
   * The tip chain threw. Everything in it runs on a timer or a renderer callback, so such an
   * exception has nowhere to surface: the chain dies, the player is left without guidance and the
   * tracking stays empty - the one failure that looks exactly like "no problem here".
   */
  public reportChainError(questId: number | null, where: string): void {
    this.report({
      stallUuid: TipStallTrackerService.newUuid(),
      questId: questId ?? undefined,
      tipTaskName: this.source?.getTaskName() ?? where,
      reason: TipStallReason.CHAIN_ERROR,
      waitMillis: this.taskStartTime ? Date.now() - this.taskStartTime : 0,
      resolved: false
    });
    this.reset();
  }

  /** No tip is running any more (quest done, tips switched off, chain gone). */
  public stop(): void {
    this.recentFailureTimes = [];
    this.thrashingReported = false;
    this.reset();
  }

  private armWatchdog(): void {
    this.watchdogTimeout = setTimeout(() => this.onWatchdog(), TipStallTrackerService.STALL_MILLIS);
  }

  private onWatchdog(): void {
    this.watchdogTimeout = null;
    if (!this.source) {
      return;
    }
    // A backgrounded tab is not a stalled tip. The player is not looking at the prompt at all,
    // so the wait says nothing about the tip - keep watching instead of blaming it.
    if (typeof document !== 'undefined' && document.visibilityState === 'hidden') {
      this.armWatchdog();
      return;
    }
    this.stallUuid = TipStallTrackerService.newUuid();
    this.report({
      stallUuid: this.stallUuid,
      questId: this.questId ?? undefined,
      tipTaskName: this.source.getTaskName(),
      reason: this.source.getStallReason(),
      waitMillis: Date.now() - this.taskStartTime,
      resolved: false
    });
  }

  /**
   * Partial on purpose: the generated DTO has every field required, while a stall and a
   * resolution each fill in their own half. The server reads what is there.
   */
  private report(tipStallJson: Partial<TipStallJson>): void {
    try {
      const gameSessionUuid = (window as any).RAZ_gameSessionUuid;
      this.trackerControllerImplClient
        .tipStall({...tipStallJson, gameSessionUuid} as TipStallJson)
        // Tracking must never surface in the game; a lost report only costs one data point.
        .catch(error => console.warn('TipStallTrackerService', error));
    } catch (error) {
      console.warn('TipStallTrackerService', error);
    }
  }

  private reset(): void {
    if (this.watchdogTimeout !== null) {
      clearTimeout(this.watchdogTimeout);
      this.watchdogTimeout = null;
    }
    this.questId = null;
    this.source = null;
    this.stallUuid = null;
  }

  private static newUuid(): string {
    // randomUUID needs a secure context - present on the live site and on localhost, but not on
    // a plain http test host, and a missing id would lose the link to the resolution.
    if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
      return crypto.randomUUID();
    }
    return `TS${Date.now().toString(36)}${Math.random().toString(36).substring(2, 10)}`.toUpperCase();
  }
}
