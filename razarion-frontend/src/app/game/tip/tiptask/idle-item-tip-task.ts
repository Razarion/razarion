import {AbstractTipTask} from './abstract-tip-task';
import {TipStallReason, TipTaskName} from '../tip-stall';

/**
 * Waits for the actor to run out of things to do. First task of every fallback chain, which is
 * where a tip lands after the player has been through it once - so it is regularly entered while
 * the actor is somewhere off screen.
 */
export class IdleItemTipTask extends AbstractTipTask {
  /**
   * How long the actor is given to take up whatever it was just told to do before its idle state
   * counts. A factory that has just been sent a fabricate command still reports idle for a tick
   * or two - taking that at face value declared this task done immediately, and the chain went
   * straight on to put the prompt back on the button the player had just clicked.
   */
  private static readonly SETTLE_MILLIS = 3000;
  private pollTimeout: ReturnType<typeof setTimeout> | null = null;
  private startedAt = 0;
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
      if (idle && this.settled()) {
        this.onSucceed();
      }
    });
    if (actor.getIdle() && this.settled()) {
      this.onSucceed();
    }
  }

  /** Long enough since the start that the actor has had a chance to take up its order. */
  private settled(): boolean {
    return Date.now() - this.startedAt >= IdleItemTipTask.SETTLE_MILLIS;
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
