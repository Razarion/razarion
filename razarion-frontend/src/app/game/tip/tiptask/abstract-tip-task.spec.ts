import {AbstractTipTask, TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {TipStallReason} from '../tip-stall';

/**
 * The actor is held by id and looked up live. Scrolling an item off screen disposes its
 * BabylonBaseItemImpl and scrolling back creates a new one for the same id - a stored instance
 * would keep answering with the state it had when it died.
 */
describe('TipTaskContext actor', () => {
  function item(id: number): BabylonBaseItemImpl {
    return {getId: () => id} as unknown as BabylonBaseItemImpl;
  }

  it('resolves to the instance the renderer has right now', () => {
    const live = new Map<number, BabylonBaseItemImpl>();
    const renderService = {
      getBabylonBaseItemById: (id: number) => live.get(id) ?? null
    } as unknown as BabylonRenderServiceAccessImpl;
    const context = new TipTaskContext(renderService);

    const first = item(7);
    live.set(7, first);
    context.setActor(first);
    expect(context.babylonBaseItemImpl).toBe(first);

    // Scrolled out: nothing to talk to, instead of a disposed object that still says "selected".
    live.delete(7);
    expect(context.babylonBaseItemImpl).toBeNull();

    // Scrolled back in: same id, new instance.
    const second = item(7);
    live.set(7, second);
    expect(context.babylonBaseItemImpl).toBe(second);
    expect(context.getActorId()).toBe(7);
  });
});

describe('AbstractTipTask stall reason', () => {
  class TestTipTask extends AbstractTipTask {
    isFulfilled(): boolean {
      return false;
    }

    start(): void {
    }

    cleanup(): void {
    }

    getTaskName(): string {
      return 'TEST';
    }

    setReason(reason: string): void {
      this.stallReason = reason;
    }
  }

  /**
   * @param actorPosition null means the actor is not rendered at all (streamed out)
   * @param viewFieldContains what the view field answers for the actor position
   */
  function createTask(actorPosition: { x: number, y: number } | null, viewFieldContains = true): TestTipTask {
    const actor = actorPosition === null ? null : {
      getId: () => 7,
      getPosition: () => ({getX: () => actorPosition.x, getY: () => actorPosition.y})
    } as unknown as BabylonBaseItemImpl;
    const renderService = {
      getBabylonBaseItemById: () => actor,
      getCurrentViewField: () => ({contains: () => viewFieldContains})
    } as unknown as BabylonRenderServiceAccessImpl;
    const context = new TipTaskContext(renderService);
    context.setActor({getId: () => 7} as unknown as BabylonBaseItemImpl);
    return new TestTipTask({renderService} as unknown as TipService, context);
  }

  it('reports the waiting reason while the actor is on screen', () => {
    const task = createTask({x: 10, y: 10});
    task.setReason(TipStallReason.AWAIT_FABRICATE_CLICK);
    expect(task.getStallReason()).toBe(TipStallReason.AWAIT_FABRICATE_CLICK);
  });

  it('calls out an actor that scrolled off screen', () => {
    // Otherwise this counts as "tip worked, player did not act" - the opposite of what happened.
    const task = createTask({x: 10, y: 10}, false);
    task.setReason(TipStallReason.AWAIT_FABRICATE_CLICK);
    expect(task.getStallReason()).toBe(TipStallReason.ACTOR_OUT_OF_VIEW);
  });

  it('treats a streamed out actor as off screen', () => {
    const task = createTask(null);
    task.setReason(TipStallReason.AWAIT_PLACER);
    expect(task.getStallReason()).toBe(TipStallReason.ACTOR_OUT_OF_VIEW);
  });

  it('never overwrites a reason that already names a defect', () => {
    const task = createTask(null);
    task.setReason(TipStallReason.BUTTON_DISABLED);
    expect(task.getStallReason()).toBe(TipStallReason.BUTTON_DISABLED);
  });
});
