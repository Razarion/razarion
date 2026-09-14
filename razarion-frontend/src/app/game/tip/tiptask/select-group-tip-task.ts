import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {TipConfig} from '../../../gwtangular/GwtAngularFacade';
import {TipService} from '../tip.service';
import {GwtHelper} from '../../../gwtangular/GwtHelper';
import {TipStallReason, TipTaskName} from '../tip-stall';

/**
 * Asks the player to hold more than one unit at once.
 * <p>
 * It is the only task in the whole chain that does. {@link SelectTipTask} points at a single unit
 * found by item type, and every one of the twelve quests before this uses it - so by the time the
 * game needs an army the player has been taught eleven times that selecting means tapping one
 * thing. On a phone that is not even a bad guess: a finger is the only pointer there is, and it
 * belongs to the camera, so the box has to be armed from the icon bar first and nothing has ever
 * mentioned that icon.
 * <p>
 * Quest 379 is where it stops being optional. The refinery it asks for is defenceless and dies to
 * three shots from a single viper, but the tesla guarding the way out-ranges a viper - 15 against
 * 10 - and kills it in two hits, so one unit dies crossing the gap while three kill the tesla in
 * two seconds. Measured on PROD over 21 days: the 52 players who failed the quest killed 2403
 * teslas, lost 193 vipers, and destroyed not one refinery.
 * <p>
 * The prompt is drawn by the game component, which owns the icon: a tip about a control has to sit
 * on that control, and pointing a bubble at a button that only exists in the compact layout from
 * anywhere else means looking it up in the document and re-measuring it forever.
 */
export class SelectGroupTipTask extends AbstractTipTask {
  /**
   * What counts as a group. Two rather than the three the quest hands out, because the lesson is
   * "a box holds more than one", not an inventory check - and a player whose third viper died on
   * the way should not be locked out of the tip that would have saved it.
   */
  private static readonly GROUP_SIZE = 2;
  /**
   * The selection changes under a finger, so this polls rather than waiting for the listener
   * alone: the box is released outside Angular's zone and the count that matters is the one after
   * the drag, not the ones during it.
   */
  private static readonly POLL_MILLIS = 500;

  private readonly actorItemTypeId: number;
  private selectionListener: (() => void) | null = null;
  private pollTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(tipConfig: TipConfig, tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
    this.actorItemTypeId = GwtHelper.gwtIssueNumber(tipConfig.getActorItemTypeId());
  }

  override getTaskName(): string {
    return TipTaskName.SELECT_GROUP;
  }

  /**
   * Fulfilled once a group is held - or straight away when the player does not own enough units to
   * form one. Blocking there would be a tip that cannot be obeyed, which is the failure mode the
   * whole chain exists to avoid.
   */
  override isFulfilled(): boolean {
    if (this.ownedOfType() < SelectGroupTipTask.GROUP_SIZE) {
      return true;
    }
    return this.selectedOfType() >= SelectGroupTipTask.GROUP_SIZE;
  }

  override start(): void {
    if (!this.selectionListener) {
      this.selectionListener = () => this.check();
      this.tipService.selectionService.addSelectionListener(this.selectionListener);
    }
    this.check();
    this.poll();
  }

  private check(): void {
    if (this.ownedOfType() < SelectGroupTipTask.GROUP_SIZE) {
      // Nothing to demonstrate. Reported rather than silently skipped: a quest that asks a player
      // who owns one unit for a group is a content problem, and it looks exactly like a player who
      // did not understand unless the two are named apart. The stall reason alone would not do it -
      // a task that succeeds at once never waits the thirty seconds the watchdog needs.
      this.stallReason = TipStallReason.TOO_FEW_TO_GROUP;
      this.report('skipped');
      this.tipService.renderService.touchSelectionMode.setAsked(false);
      this.onSucceed();
      return;
    }
    if (this.selectedOfType() >= SelectGroupTipTask.GROUP_SIZE) {
      this.tipService.renderService.touchSelectionMode.setAsked(false);
      this.onSucceed();
      return;
    }
    this.stallReason = TipStallReason.AWAIT_GROUP;
    this.report('asked');
    this.tipService.renderService.touchSelectionMode.setAsked(true);
  }

  /**
   * Says what this tip decided, once per session and state. Repeats are free - the tracker keys on
   * the kind and its detail - so this can sit on the polling path.
   */
  private report(state: 'asked' | 'skipped'): void {
    this.tipService.firstInteractionTracker.report('GROUP_TIP', 'state=' + state);
  }

  private poll(): void {
    if (this.pollTimeout !== null) {
      return;
    }
    this.pollTimeout = setTimeout(() => {
      this.pollTimeout = null;
      // Guarded against a double timer: check() can end this task and start the next one, whose
      // own start() runs before this line.
      this.check();
      this.poll();
    }, SelectGroupTipTask.POLL_MILLIS);
  }

  /** How many units of the actor type the player owns, over the whole planet rather than the view. */
  private ownedOfType(): number {
    const baseItemUiService = this.tipService.gwtAngularFacade.baseItemUiService;
    if (!baseItemUiService) {
      // Without the engine there is nothing to count, and a tip that cannot count must not block.
      return 0;
    }
    return baseItemUiService.getMyItemCount(this.actorItemTypeId);
  }

  private selectedOfType(): number {
    return this.tipService.selectionService.getSelectedOwnItems()
      .filter(item => item.getBaseItemType().getId() === this.actorItemTypeId)
      .length;
  }

  override cleanup(): void {
    this.cancelSelectionLossGrace();
    if (this.pollTimeout !== null) {
      clearTimeout(this.pollTimeout);
      this.pollTimeout = null;
    }
    if (this.selectionListener) {
      this.tipService.selectionService.removeSelectionListener(this.selectionListener);
      this.selectionListener = null;
    }
    // Must not outlive the task: the prompt hangs on an icon and would otherwise keep asking for
    // a group while the next tip asks for something else.
    this.tipService.renderService.touchSelectionMode.setAsked(false);
    this.tipService.setOutOfViewTarget(null);
  }
}
