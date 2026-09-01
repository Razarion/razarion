import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {Diplomacy} from '../../../gwtangular/GwtAngularFacade';
import {GwtInstance} from '../../../gwtangular/GwtInstance';
import {TipStallReason, TipTaskName} from '../tip-stall';

export class SendAttackCommandTipTask extends AbstractTipTask {
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
        // Enemy exists but is out of view - set OutOfView target and wait
        this.stallReason = TipStallReason.ENEMY_OUT_OF_VIEW;
        this.tipService.setOutOfViewTarget(
          GwtInstance.newDecimalPosition(nearestEnemyPosition.x, nearestEnemyPosition.y)
        );
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
