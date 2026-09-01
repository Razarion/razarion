import {SendAttackCommandTipTask} from './send-attack-command-tip-task';
import {TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {TipStallReason} from '../tip-stall';

/**
 * The attacker this tip is about is held by id and looked up live, so it is null whenever the unit
 * is off screen - and this task is restarted by onBecameVisible, which is to say on a camera move,
 * which is to say on exactly that event.
 *
 * PROD, 2026-08-30 19:01:57, on a phone: the player's attacker had died, they panned the camera,
 * and asserting the instance away threw a TypeError out of the view-field listener chain. Because
 * that chain is a forEach, every listener behind this one stopped updating - which is how a broken
 * attack tip stopped the quest markers from being drawn.
 */
describe('SendAttackCommandTipTask without a live attacker', () => {
  /** Which enemy the task ended up pointing at - the outcome, rather than a status that
   *  ACTOR_OUT_OF_VIEW is documented to override. */
  let prompted: number[];

  beforeEach(() => prompted = []);

  function enemyAt(id: number, x: number, y: number): BabylonBaseItemImpl {
    return {
      getId: () => id,
      getPosition: () => ({getX: () => x, getY: () => y, distance: () => 0}),
      itemType: {getId: () => 42},
      setItemClickCallback: () => {
      },
      showSelectPromptVisualization: () => prompted.push(id),
      hideSelectPromptVisualization: () => {
      }
    } as unknown as BabylonBaseItemImpl;
  }

  function task(enemies: BabylonBaseItemImpl[], liveActor: BabylonBaseItemImpl | null,
                nearest: { x: number, y: number } | null = null) {
    const renderService = {
      getBabylonBaseItemsByDiplomacy: () => enemies,
      getBabylonBaseItemById: () => liveActor
    } as unknown as BabylonRenderServiceAccessImpl;
    const context = new TipTaskContext(renderService);
    context.setActor({getId: () => 1} as unknown as BabylonBaseItemImpl);
    const tipService = {
      renderService,
      selectionService: {addSelectionListener: () => {
      }, removeSelectionListener: () => {
      }},
      setOutOfViewTarget: () => {
      },
      gwtAngularFacade: {
        baseItemUiService: {
          getNearestEnemyPosition: () => nearest === null ? null
            : {getX: () => nearest.x, getY: () => nearest.y}
        }
      }
    } as unknown as TipService;
    return {task: new SendAttackCommandTipTask(null, tipService, context), context};
  }

  it('does not throw when the attacker is off screen', () => {
    const {task: attackTask} = task([enemyAt(9, 100, 100)], null);

    // Before the fix this threw "Cannot read properties of null (reading 'getPosition')" - out of
    // the view-field listener chain, on every camera move.
    expect(() => attackTask.start()).not.toThrow();
    attackTask.cleanup();
  });

  /**
   * Off screen is not gone. The chain remembers where the actor was last seen precisely so a task
   * entered while it is out of view still has something to measure from.
   */
  it('measures from the remembered position when the attacker is out of view', () => {
    const {task: attackTask, context} = task([enemyAt(9, 500, 500), enemyAt(10, 20, 20)], null);
    context.rememberActorPosition({getX: () => 10, getY: () => 10} as any);

    attackTask.start();

    // The near one, measured from the remembered position - not "no enemy at all", and not the
    // far one that a broken comparison would have settled on.
    expect(prompted).toEqual([10]);
    attackTask.cleanup();
  });

  /**
   * Two different silences that used to be reported as one. "No enemy in the world" and "I lost
   * sight of the unit this tip is about" call for different repairs, and the funnel counts them.
   */
  it('says the actor is missing rather than blaming the enemies', () => {
    const {task: attackTask} = task([enemyAt(9, 100, 100)], null);

    attackTask.start();

    expect(attackTask.getStallReason()).toBe(TipStallReason.ACTOR_NOT_FOUND);
    attackTask.cleanup();
  });

  /**
   * An enemy that is itself off screen has no position. It used to enter the comparison as
   * undefined, and every arithmetic test against undefined is false - so the first such enemy
   * became the answer and no nearer one could replace it.
   */
  it('skips enemies without a position instead of settling on them', () => {
    const positionless = enemyAt(8, 0, 0);
    (positionless as any).getPosition = () => null;
    const near = enemyAt(9, 11, 11);
    const {task: attackTask, context} = task([positionless, near], null);
    context.rememberActorPosition({getX: () => 10, getY: () => 10} as any);

    attackTask.start();

    expect(prompted).toEqual([9]);
    attackTask.cleanup();
  });
});
