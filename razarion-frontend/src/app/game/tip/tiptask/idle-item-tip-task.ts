import {AbstractTipTask} from './abstract-tip-task';
import {TipStallReason, TipTaskName} from '../tip-stall';

/**
 * Waits for the actor to run out of things to do. First task of every fallback chain, which is
 * where a tip lands after the player has been through it once - so it is regularly entered while
 * the actor is somewhere off screen.
 */
export class IdleItemTipTask extends AbstractTipTask {
  /**
   * How long an actor that was never once seen working is given before its idle state is taken at
   * face value. This is the safety net, not the rule: an order that never landed has to re-engage
   * the chain eventually, or the tip goes quiet for a player who is genuinely stuck.
   * <p>
   * It used to be the rule, at three seconds, and that is a race rather than a test. An actor that
   * has just been told to do something still reports idle until the worker has taken the order up,
   * and how long that takes is a property of the transport: in the Meta in-app browser there is no
   * SharedArrayBuffer, so the tick runs through the postMessage fallback and arrives late. Losing
   * the race put the prompt back on the very step the player had just completed - observed on PROD
   * on 2026-09-13 with a harvester that had just been sent to a razarion field.
   */
  private static readonly NEVER_TOOK_ORDER_MILLIS = 15000;
  private pollTimeout: ReturnType<typeof setTimeout> | null = null;
  private startedAt = 0;
  /**
   * Whether the actor has been seen working since this task started. That is the real test: an
   * actor that was busy and is idle again has finished, while one that has never been busy has
   * either not started yet or never will, and only the clock can tell those two apart.
   */
  private sawBusy = false;
  /** Set by cleanup(), so a pass that ended the task does not arm the next poll on its way out. */
  private stopped = false;

  isFulfilled(): boolean {
    return false;
  }

  getTaskName(): string {
    return TipTaskName.IDLE_ITEM;
  }

  start(): void {
    this.stopped = false;
    this.startedAt = Date.now();
    this.sawBusy = false;
    this.refreshActor();
    this.pollActor();
  }

  /**
   * The callback slot lives on the item instance and does not survive the actor leaving the view,
   * and setIdle() only fires on a change - an item that went idle while nobody was listening
   * never tells anyone. So re-attach and read the current state on every pass.
   */
  private refreshActor(sampleMillis = 0): void {
    const actor = this.tipTaskContext.babylonBaseItemImpl;
    this.trackActor(sampleMillis);
    if (!actor) {
      // Was null-asserted here, which threw and left the whole fallback chain dead.
      this.stallReason = this.lastKnownActorPosition === null
        ? TipStallReason.ACTOR_NOT_FOUND
        : TipStallReason.ACTOR_OUT_OF_VIEW;
      return;
    }
    this.stallReason = TipStallReason.AWAIT_IDLE;
    actor.setIdleCallback(idle => {
      if (!idle) {
        this.sawBusy = true;
        return;
      }
      if (this.idleCounts()) {
        this.onSucceed();
      }
    });
    if (actor.getIdle()) {
      if (this.idleCounts()) {
        this.onSucceed();
      }
    } else {
      this.sawBusy = true;
    }
  }

  /**
   * Whether an idle reading can be believed. Seeing the actor work at any point since the task
   * started is the evidence that the order arrived, so a later idle means it is done; without that
   * evidence only the clock is left.
   */
  private idleCounts(): boolean {
    return this.sawBusy || Date.now() - this.startedAt >= IdleItemTipTask.NEVER_TOOK_ORDER_MILLIS;
  }

  private pollActor(): void {
    if (this.pollTimeout !== null || this.stopped) {
      return;
    }
    this.pollTimeout = setTimeout(() => {
      this.pollTimeout = null;
      this.refreshActor(AbstractTipTask.ACTOR_TRACK_MILLIS);
      // Guarded against a double timer: refreshActor() can end this task and start the next one.
      this.pollActor();
    }, AbstractTipTask.ACTOR_TRACK_MILLIS);
  }

  cleanup(): void {
    this.stopped = true;
    if (this.pollTimeout !== null) {
      clearTimeout(this.pollTimeout);
      this.pollTimeout = null;
    }
    this.stopActorTracking();
    this.tipService.setOutOfViewTarget(null);
    this.tipTaskContext.babylonBaseItemImpl?.setIdleCallback(null);
  }
}
