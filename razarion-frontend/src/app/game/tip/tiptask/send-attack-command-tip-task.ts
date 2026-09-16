import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {Diplomacy} from '../../../gwtangular/GwtAngularFacade';
import {GwtInstance} from '../../../gwtangular/GwtInstance';
import {TipStallReason, TipTaskName} from '../tip-stall';

export class SendAttackCommandTipTask extends AbstractTipTask {
  /**
   * How often the chosen target is checked for still being there.
   *
   * The same second the two failure branches already retry on: a target that dies is the same
   * silence as a target that was never found, and the player waits it out the same way.
   */
  private static readonly TARGET_POLL_MILLIS = 1000;

  private enemy: BabylonBaseItemImpl | null = null;
  private selectionListener: (() => void) | null = null;
  private retryTimeout: ReturnType<typeof setTimeout> | null = null;

  constructor(private enemyItemTypeId: number | null, tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  isFulfilled(): boolean {
    return false;
  }

  getTaskName(): string {
    return TipTaskName.SEND_ATTACK_COMMAND;
  }

  start(): void {
    // start() is also called again from the retry below and when the target scrolls back into view
    if (this.retryTimeout !== null) {
      clearTimeout(this.retryTimeout);
      this.retryTimeout = null;
    }
    this.enemy = this.findVisibleEnemy();

    // Register global selection listener
    if (!this.selectionListener) {
      this.selectionListener = () => this.onSelectionChanged();
      this.tipService.selectionService.addSelectionListener(this.selectionListener);
    }

    if (!this.enemy) {
      // No visible enemy found - check if there's an enemy out of view
      const nearestEnemyPosition = this.findNearestEnemyPosition();
      if (nearestEnemyPosition) {
        /*
         * The enemy exists but is off screen: point the marker at it and keep polling, the way
         * the harvest tip does. Waiting for onBecameVisible() alone means waiting for the player
         * to move the camera of his own accord, and the whole reason this tip is up is that he
         * does not know he has to.
         *
         * Measured on PROD over 21 days on quest 379, which asks for the bot refinery: 60 stalls
         * on this reason, 41 of them never resolved. The refinery stands 111 units from the
         * player's base while the bot's teslas respawn at 33, so the player fights what is in
         * front of him - 2403 teslas killed by the 52 players who failed the quest, and not one
         * refinery. The target also moves and dies, so the position is re-read on every pass
         * rather than frozen at the moment the tip opened.
         */
        this.stallReason = TipStallReason.ENEMY_OUT_OF_VIEW;
        this.tipService.setOutOfViewTarget(
          GwtInstance.newDecimalPosition(nearestEnemyPosition.x, nearestEnemyPosition.y)
        );
        this.retryTimeout = setTimeout(() => this.start(), 1000);
        return;
      }

      // Two different silences, and they were both reported as NO_ENEMY. Without an attacker
      // there is nothing to measure a distance from, so no enemy can be found however many there
      // are - saying so is the difference between "the world is empty" and "I lost sight of the
      // unit this tip is about".
      this.stallReason = this.attackerGroundPosition() ? TipStallReason.NO_ENEMY : TipStallReason.ACTOR_NOT_FOUND;
      this.retryTimeout = setTimeout(() => this.start(), 1000);
      return;
    }

    this.stallReason = TipStallReason.AWAIT_ATTACK_CLICK;
    this.enemy.setItemClickCallback(() => {
      this.onSucceed();
    });
    this.enemy.showSelectPromptVisualization("Click to attack");

    // Set OutOfView target for when user scrolls away
    const enemyPosition = this.enemy.getPosition();
    if (enemyPosition) {
      this.tipService.setOutOfViewTarget(
        GwtInstance.newDecimalPosition(enemyPosition.getX(), enemyPosition.getY())
      );
    }

    /*
     * Keep watching the target. Having found one used to end this task's own clock: the click
     * callback and the prompt hang off one BabylonBaseItemImpl, and nothing tells the task when
     * that item dies. There is no removed-listener on the render service at all - only
     * addBaseItemCreatedListener, and it fires for the actor, not for the enemy - so the only way
     * back into start() was onBecameVisible, which is a camera move, which is the very thing the
     * player does not know he has to do. The tip then stood on a disposed mesh: text on screen,
     * no prompt, nothing to click.
     *
     * Measured on PROD 14.-15.09.2026, when a bot respawned a fresh tesla at one spot every three
     * seconds for 25 hours: quest 365 fell from 83% to 32%, and of the players who failed it, not
     * one destroyed anything at all while all of them were giving orders. Their stall rate on the
     * quest doubled. The runaway only made this loud - any target that dies leaves the tip dead,
     * whether another unit killed it or it walked out of view.
     */
    this.retryTimeout = setTimeout(
      () => this.reviewTarget(), SendAttackCommandTipTask.TARGET_POLL_MILLIS);
  }

  /**
   * Re-aims when the target is gone, and otherwise looks again in a second.
   */
  private reviewTarget(): void {
    this.retryTimeout = null;
    if (this.isTargetStillThere()) {
      this.retryTimeout = setTimeout(
        () => this.reviewTarget(), SendAttackCommandTipTask.TARGET_POLL_MILLIS);
      return;
    }
    /*
     * Dropped without taking its prompt and callback down: they live on the item's own mesh and
     * went with it. Reaching into a disposed BabylonBaseItemImpl is the thing being avoided here,
     * and cleanup() reads this field, so nulling it is what keeps the restart from doing it.
     */
    this.enemy = null;
    this.start();
  }

  /**
   * Whether the chosen enemy is still among the rendered ones.
   *
   * By id rather than by instance: scrolling an item out and back in gives it a fresh
   * BabylonBaseItemImpl, and the tip is about the unit, not about the object holding it. Losing
   * sight of it counts as gone on purpose - start() then falls into the out-of-view branch, which
   * asks the worker where the nearest enemy is and points the marker there, rather than leaving a
   * prompt on an item the player cannot see.
   */
  private isTargetStillThere(): boolean {
    const enemy = this.enemy;
    if (!enemy) {
      return false;
    }
    return this.tipService.renderService
      .getBabylonBaseItemsByDiplomacy(Diplomacy.ENEMY)
      .some(candidate => candidate.getId() === enemy.getId());
  }

  cleanup(): void {
    this.cancelSelectionLossGrace();
    if (this.retryTimeout !== null) {
      clearTimeout(this.retryTimeout);
      this.retryTimeout = null;
    }
    // Remove global selection listener
    if (this.selectionListener) {
      this.tipService.selectionService.removeSelectionListener(this.selectionListener);
      this.selectionListener = null;
    }
    this.tipService.setOutOfViewTarget(null);
    if (this.enemy) {
      this.enemy.hideSelectPromptVisualization();
      this.enemy.setItemClickCallback(null);
    }
  }

  private onSelectionChanged(): void {
    // A lost selection only fails the task if it is still lost after the grace period
    if (!this.tipService.selectionService.hasOwnSelection()) {
      this.onSelectionLost(() => !this.tipService.selectionService.hasOwnSelection());
    }
  }

  /**
   * Where the attacker is, or was last seen, as plain ground coordinates.
   *
   * The live instance is null whenever the actor is off screen - scrolling it out of view disposes
   * it, see TipTaskContext - and this task is restarted by onBecameVisible, which is to say on a
   * camera move, which is to say on exactly that event. Asserting the instance away threw a
   * TypeError out of the view-field listener chain, and because that chain is a forEach, every
   * listener behind this one stopped updating for as long as the tip kept throwing. Measured on
   * PROD on 2026-08-30 at 19:01:57, on a phone, after the player's attacker had died.
   *
   * Ground coordinates rather than the Vertex the renderer hands out: the remembered position is
   * two-dimensional, and comparing distances on the ground is what this task is doing anyway.
   */
  private attackerGroundPosition(): { x: number, y: number } | null {
    const live = this.tipTaskContext.babylonBaseItemImpl?.getPosition();
    if (live) {
      return {x: live.getX(), y: live.getY()};
    }
    const remembered = this.lastKnownActorPosition;
    return remembered ? {x: remembered.getX(), y: remembered.getY()} : null;
  }

  private findVisibleEnemy(): BabylonBaseItemImpl | null {
    let enemies = this.tipService.renderService.getBabylonBaseItemsByDiplomacy(Diplomacy.ENEMY);
    if (this.enemyItemTypeId !== null) {
      enemies = enemies.filter(enemy => enemy.itemType.getId() === this.enemyItemTypeId);
    }
    if (enemies.length === 0) {
      return null;
    }

    const attacker = this.attackerGroundPosition();
    if (!attacker) {
      return null;
    }
    let enemyFound: BabylonBaseItemImpl | null = null;
    let minDistance: number | null = null;
    for (const enemy of enemies) {
      const position = enemy.getPosition();
      // An enemy without a position is one that has scrolled out of view. Before, it entered the
      // comparison as undefined, and every arithmetic test against undefined is false - so the
      // first such enemy became the answer and no later one could replace it.
      if (!position) {
        continue;
      }
      const dx = position.getX() - attacker.x;
      const dy = position.getY() - attacker.y;
      // Squared: the nearest by this is the nearest by distance, and there is no root to take.
      const distance = dx * dx + dy * dy;
      if (minDistance === null || distance < minDistance) {
        enemyFound = enemy;
        minDistance = distance;
      }
    }
    return enemyFound;
  }

  private findNearestEnemyPosition(): { x: number, y: number } | null {
    const attackerPosition = this.attackerGroundPosition();
    if (!attackerPosition) {
      return null;
    }

    const baseItemUiService = this.tipService.gwtAngularFacade.baseItemUiService;
    if (!baseItemUiService) {
      console.warn('BaseItemUiService not available');
      return null;
    }

    const nearestPosition = baseItemUiService.getNearestEnemyPosition(
      attackerPosition.x,
      attackerPosition.y,
      this.enemyItemTypeId ?? 0,
      this.enemyItemTypeId !== null
    );

    if (nearestPosition) {
      return { x: nearestPosition.getX(), y: nearestPosition.getY() };
    }
    return null;
  }
}
