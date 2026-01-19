import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {Diplomacy} from '../../../gwtangular/GwtAngularFacade';
import {GwtInstance} from '../../../gwtangular/GwtInstance';

export class SendAttackCommandTipTask extends AbstractTipTask {
  private enemy: BabylonBaseItemImpl | null = null;
  private selectionListener: (() => void) | null = null;

  constructor(private enemyItemTypeId: number | null, tipService: TipService, tipTaskContext: TipTaskContext) {
    super(tipService, tipTaskContext);
  }

  isFulfilled(): boolean {
    return false;
  }

  start(): void {
    this.enemy = this.findVisibleEnemy();

    // Register global selection listener
    if (!this.selectionListener) {
      this.selectionListener = () => this.onSelectionChanged();
      this.tipService.gwtAngularFacade.selectionService.addSelectionListener(this.selectionListener);
    }

    if (!this.enemy) {
      // No visible enemy found - check if there's an enemy out of view
      const nearestEnemyPosition = this.findNearestEnemyPosition();
      if (nearestEnemyPosition) {
        // Enemy exists but is out of view - set OutOfView target and wait
        this.tipService.setOutOfViewTarget(
          GwtInstance.newDecimalPosition(nearestEnemyPosition.x, nearestEnemyPosition.y)
        );
        return;
      }

      // No enemy found at all - retry after delay (server may not be synchronized)
      setTimeout(() => {
        this.start();
      }, 1000);
      return;
    }

    this.enemy.setItemClickCallback(() => {
      this.onSucceed();
    });
    this.enemy.showSelectPromptVisualization("Click to attack", "150px");

    // Set OutOfView target for when user scrolls away
    const enemyPosition = this.enemy.getPosition();
    if (enemyPosition) {
      this.tipService.setOutOfViewTarget(
        GwtInstance.newDecimalPosition(enemyPosition.getX(), enemyPosition.getY())
      );
    }
  }

  cleanup(): void {
    // Remove global selection listener
    if (this.selectionListener) {
      this.tipService.gwtAngularFacade.selectionService.removeSelectionListener(this.selectionListener);
      this.selectionListener = null;
    }
    this.tipService.setOutOfViewTarget(null);
    if (this.enemy) {
      this.enemy.hideSelectPromptVisualization();
      this.enemy.setItemClickCallback(null);
    }
  }

  private onSelectionChanged(): void {
    // Check if own selection is still present
    if (!this.tipService.gwtAngularFacade.selectionService.hasOwnSelection()) {
      this.onFailed();
    }
  }

  private findVisibleEnemy(): BabylonBaseItemImpl | null {
    let enemies = this.tipService.renderService.getBabylonBaseItemsByDiplomacy(Diplomacy.ENEMY);
    if (this.enemyItemTypeId !== null) {
      enemies = enemies.filter(enemy => enemy.itemType.getId() === this.enemyItemTypeId);
    }
    if (enemies.length === 0) {
      return null;
    }

    const attackerPosition = this.tipTaskContext.babylonBaseItemImpl!.getPosition()!;
    let enemyFound: BabylonBaseItemImpl | null = null;
    let minDistance: number | null = null;
    for (const enemy of enemies) {
      const distance = enemy.getPosition()?.distance(attackerPosition)!;
      if (minDistance !== null) {
        if (minDistance > distance) {
          enemyFound = enemy;
          minDistance = distance;
        }
      } else {
        enemyFound = enemy;
        minDistance = distance;
      }
    }
    return enemyFound;
  }

  private findNearestEnemyPosition(): { x: number, y: number } | null {
    const attackerPosition = this.tipTaskContext.babylonBaseItemImpl!.getPosition();
    if (!attackerPosition) {
      return null;
    }

    const baseItemUiService = this.tipService.gwtAngularFacade.baseItemUiService;
    if (!baseItemUiService) {
      console.warn('BaseItemUiService not available');
      return null;
    }

    const nearestPosition = baseItemUiService.getNearestEnemyPosition(
      attackerPosition.getX(),
      attackerPosition.getY(),
      this.enemyItemTypeId ?? 0,
      this.enemyItemTypeId !== null
    );

    if (nearestPosition) {
      return { x: nearestPosition.getX(), y: nearestPosition.getY() };
    }
    return null;
  }
}
