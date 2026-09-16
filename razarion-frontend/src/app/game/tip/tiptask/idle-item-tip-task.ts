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
  /**
   * How long the actor has been watchable since this task started - not how long the task has
   * been running.
   *
   * The safety net below is a statement about an actor that was looked at and never seen working.
   * An actor off screen is not being looked at: its instance is gone, so neither busy nor idle can
   * be read, and counting that time would let the net fire on evidence nobody gathered.
   *
   * That is not hypothetical. Reported from a phone on 2026-09-16: the player sent a viper to
   * attack, scrolled to the extractor so the viper was off screen for the whole walk, and the
   * chain put the select arrow back on the viper the moment it arrived. The same quest behaves
   * with a harvester because the field and the harvester are both on screen, so the harvester is
   * seen working within the first poll.
   */
  private observedMillis = 0;
  /** When the actor was last seen. Null while it is out of view, which is what stops the clock. */
  private lastSeenAt: number | null = null;
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
    this.observedMillis = 0;
    this.lastSeenAt = null;
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
      // The clock stops with the sighting: while this is null nothing can be observed, so the
      // gap must not count towards the safety net below.
      this.lastSeenAt = null;
      this.stallReason = this.lastKnownActorPosition === null
        ? TipStallReason.ACTOR_NOT_FOUND
        : TipStallReason.ACTOR_OUT_OF_VIEW;
      return;
    }
    const now = Date.now();
    if (this.lastSeenAt !== null) {
      this.observedMillis += now - this.lastSeenAt;
    }
    this.lastSeenAt = now;
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
   * evidence only the clock is left - and the clock counts watched time, not wall time.
   *
   * An actor that is never watchable therefore never trips the net. That is the honest state: the
   * tip waits, and the stall watchdog reports ACTOR_OUT_OF_VIEW after its thirty seconds, which
   * says what is actually wrong. Sending the player back to a step he has already done says
   * something false.
   */
  private idleCounts(): boolean {
    return this.sawBusy || this.observedMillis >= IdleItemTipTask.NEVER_TOOK_ORDER_MILLIS;
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
