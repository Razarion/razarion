import {TipService} from '../tip.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {TipStallReason, TipStallSource} from '../tip-stall';
import {GwtInstance} from '../../../gwtangular/GwtInstance';
import {DecimalPosition} from '../../../gwtangular/GwtAngularFacade';

/**
 * The item a tip chain acts on - the builder to select, the factory to fabricate in.
 *
 * Holds the id, not the instance. Scrolling the actor off screen disposes its
 * BabylonBaseItemImpl and scrolling back creates a new one for the same id, so a stored
 * instance turns into a corpse that still answers isSelected() with the state it had when
 * it died and reports the position it was last seen at. That silently pinned the tip chain: the
 * selection loss check asked the dead object, got "still selected", and never recovered.
 */
export class TipTaskContext {
  private actorId: number | null = null;
  private actorPosition: DecimalPosition | null = null;

  constructor(private readonly renderService: BabylonRenderServiceAccessImpl) {
  }

  setActor(babylonBaseItemImpl: BabylonBaseItemImpl | null): void {
    const actorId = babylonBaseItemImpl?.getId() ?? null;
    if (actorId !== this.actorId) {
      this.actorPosition = null;
    }
    this.actorId = actorId;
  }

  getActorId(): number | null {
    return this.actorId;
  }

  rememberActorPosition(position: DecimalPosition): void {
    this.actorPosition = position;
  }

  /**
   * Where the actor was last seen, by any task of this chain.
   *
   * Shared rather than per task on purpose: out of view the item does not exist and its position
   * cannot be read, so a task entered while the actor is already off screen has nothing of its
   * own to point at. That is the normal case in the fallback chain, whose tasks are separate
   * instances - the tip knew the builder was out of view and still could not say where.
   */
  getActorPosition(): DecimalPosition | null {
    return this.actorPosition;
  }

  /** The instance as it exists right now, or null while the actor is out of view. */
  get babylonBaseItemImpl(): BabylonBaseItemImpl | null {
    return this.actorId === null ? null : this.renderService.getBabylonBaseItemById(this.actorId);
  }
}

export abstract class AbstractTipTask implements TipStallSource {
  /**
   * A lost selection only counts as a failure if it stays lost this long. Failing instantly tore
   * the tip chain down on every stray click on empty ground and on the short deselect while a
   * placer starts, which made the prompts flicker and threw the player back to "select the
   * builder" although nothing was wrong.
   */
  private static readonly SELECTION_LOSS_GRACE_MILLIS = 1500;
  /**
   * How often the actor is sampled. Neither its position nor its disappearance has an event to
   * hang off, and the out-of-view marker is only recomputed when a target is set or the view field
   * changes - a builder driving out from under a standing camera raises nothing at all.
   */
  protected static readonly ACTOR_TRACK_MILLIS = 1000;
  /**
   * How long the actor has to have been gone for the stall to be about that rather than about the
   * player. The watchdog only looks once, at the end, and a driving builder is regularly back on
   * screen by then - which recorded the wait as "tip was up, player did not act" although the tip
   * pointed at nothing for most of it.
   */
  private static readonly OUT_OF_VIEW_DOMINATES_MILLIS = 10000;
  private selectionLossTimeout: ReturnType<typeof setTimeout> | null = null;
  private actorTrackTimeout: ReturnType<typeof setTimeout> | null = null;
  /** Time the actor was not rendered during this run; see OUT_OF_VIEW_DOMINATES_MILLIS. */
  private outOfViewMillis = 0;
  /**
   * What this task is currently waiting for. Kept up to date by the task itself as it moves
   * between its waiting states; read only when the stall watchdog fires, so it always describes
   * the situation the player is actually stuck in.
   */
  protected stallReason: string = TipStallReason.WAITING;

  constructor(protected readonly tipService: TipService, protected readonly tipTaskContext: TipTaskContext) {

  }

  abstract isFulfilled(): boolean;

  abstract start(): void;

  abstract cleanup(): void;

  /** Reported name of this task; a constant string, class names do not survive minification. */
  abstract getTaskName(): string;

  getStallReason(): string {
    // A prompt for an item the player cannot see is not a player taking their time. Without this
    // the two are indistinguishable in the data, and the waiting reasons are exactly the ones
    // read as "the tip did its job".
    //
    // COCKPIT_NOT_READY belongs in the same group even though it names a defect: an actor out in
    // the world is not selected and has no cockpit, so a tip waiting on a cockpit button reports
    // a rendering problem when all that happened is that its builder drove off.
    const blamesTheWait = this.stallReason.startsWith('AWAIT_')
      || this.stallReason === TipStallReason.COCKPIT_NOT_READY;
    if (blamesTheWait && (this.isActorOutOfView() || this.actorWasOutOfViewForLong())) {
      return TipStallReason.ACTOR_OUT_OF_VIEW;
    }
    return this.stallReason;
  }

  private actorWasOutOfViewForLong(): boolean {
    return this.outOfViewMillis >= AbstractTipTask.OUT_OF_VIEW_DOMINATES_MILLIS;
  }

  /** True when the actor is not rendered at all or sits outside the current view field. */
  private isActorOutOfView(): boolean {
    const actorId = this.tipTaskContext.getActorId();
    if (actorId === null) {
      return false; // No actor to look at - the reason the task set stands on its own.
    }
    const actor = this.tipTaskContext.babylonBaseItemImpl;
    if (!actor) {
      return true; // Streamed out, which only happens outside the view field.
    }
    const position = actor.getPosition();
    const viewField = this.tipService.renderService.getCurrentViewField();
    if (!position || !viewField) {
      return false;
    }
    return !viewField.contains(GwtInstance.newDecimalPosition(position.getX(), position.getY()));
  }

  /**
   * One look at the actor: keeps the out-of-view marker on it while it moves, remembers where it
   * was when it leaves the view, and records how long it has been gone.
   *
   * The marker target is re-set on every pass even when the position did not change - setting it
   * is what makes TipService recompute the marker at all.
   *
   * @param sampleMillis time this look accounts for; 0 for a look outside the tracking interval
   */
  protected trackActor(sampleMillis = 0): void {
    const actor = this.tipTaskContext.babylonBaseItemImpl;
    const position = actor?.getPosition();
    if (position) {
      this.tipTaskContext.rememberActorPosition(
        GwtInstance.newDecimalPosition(position.getX(), position.getY()));
    }
    // Counts up while the actor is gone and back down while it is there, so a short scroll away
    // fades out again and only a wait mostly spent staring at nothing keeps the mark.
    this.outOfViewMillis = actor
      ? Math.max(0, this.outOfViewMillis - sampleMillis)
      : this.outOfViewMillis + sampleMillis;
    const lastKnown = this.lastKnownActorPosition;
    if (lastKnown) {
      this.tipService.setOutOfViewTarget(lastKnown);
    }
  }

  /** Where the actor was last seen by any task of this chain; null until one has seen it. */
  protected get lastKnownActorPosition(): DecimalPosition | null {
    return this.tipTaskContext.getActorPosition();
  }

  /** Starts the actor sampling if it is not already running. Every start() may call this. */
  protected startActorTracking(): void {
    if (this.actorTrackTimeout !== null) {
      return;
    }
    this.actorTrackTimeout = setTimeout(() => {
      this.actorTrackTimeout = null;
      this.trackActor(AbstractTipTask.ACTOR_TRACK_MILLIS);
      // Guarded against a double timer: trackActor() can bring the actor back into view, which
      // restarts this task through TipService and arms the tracking again on its way.
      this.startActorTracking();
    }, AbstractTipTask.ACTOR_TRACK_MILLIS);
  }

  /** Must be called from every cleanup() of a task that tracks its actor. */
  protected stopActorTracking(): void {
    if (this.actorTrackTimeout !== null) {
      clearTimeout(this.actorTrackTimeout);
      this.actorTrackTimeout = null;
    }
  }

  /**
   * Called when the tip target becomes visible again after being out of view.
   * Override to re-show visualizations.
   */
  onBecameVisible(): void {
    this.cleanup();
    this.start();
  }

  /**
   * Reports a lost selection, but only after the grace period and only if `stillLost` still holds
   * by then - the selection usually comes back within a few hundred milliseconds.
   */
  protected onSelectionLost(stillLost: () => boolean): void {
    if (this.selectionLossTimeout !== null) {
      return;
    }
    this.selectionLossTimeout = setTimeout(() => {
      this.selectionLossTimeout = null;
      if (stillLost()) {
        this.onFailed();
      }
    }, AbstractTipTask.SELECTION_LOSS_GRACE_MILLIS);
  }

  /** Must be called from every cleanup(): a pending grace timer would fail an already torn down task. */
  protected cancelSelectionLossGrace(): void {
    if (this.selectionLossTimeout !== null) {
      clearTimeout(this.selectionLossTimeout);
      this.selectionLossTimeout = null;
    }
  }

  protected onFailed(): void {
    this.cleanup();
    this.tipService.onTaskFailed();
  }

  protected onSucceed(): void {
    this.cleanup();
    this.tipService.onSucceed();
  }


}
