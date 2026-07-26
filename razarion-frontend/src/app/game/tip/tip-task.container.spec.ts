import {TipTaskContainer} from './tip-task.container';
import {AbstractTipTask, TipTaskContext} from './tiptask/abstract-tip-task';
import {BabylonRenderServiceAccessImpl} from '../renderer/babylon-render-service-access-impl.service';

/**
 * Walking off the end of a chain used to be silent: the index ran past the last task, the
 * watchdog was armed with an undefined source - which can never report - and the TypeError that
 * followed landed in the catch in TipService. No tip, no record, for the rest of the quest.
 */
describe('TipTaskContainer end of chain', () => {
  function task(fulfilled: boolean): AbstractTipTask {
    return {
      isFulfilled: () => fulfilled,
      start: () => {
      },
      cleanup: () => {
      },
      getTaskName: () => 'TEST'
    } as unknown as AbstractTipTask;
  }

  function container(): TipTaskContainer {
    return new TipTaskContainer({} as unknown as BabylonRenderServiceAccessImpl);
  }

  it('reports having no tip once every task is done', () => {
    const tipTaskContainer = container();
    tipTaskContainer.add(task(true));
    tipTaskContainer.add(task(true));

    // What TipService does to skip finished steps - it must end at "nothing left", not at an
    // index pointing past the array.
    while (tipTaskContainer.hasTip() && tipTaskContainer.getCurrentTask().isFulfilled()) {
      tipTaskContainer.next();
    }

    expect(tipTaskContainer.hasTip()).toBeFalse();
  });

  it('has nothing left after a fallback chain that is done as well', () => {
    const tipTaskContainer = container();
    tipTaskContainer.add(task(true));
    tipTaskContainer.addFallback(task(true));

    while (tipTaskContainer.hasTip() && tipTaskContainer.getCurrentTask().isFulfilled()) {
      tipTaskContainer.next();
    }
    expect(tipTaskContainer.hasTip()).toBeFalse();

    tipTaskContainer.activateFallback();
    while (tipTaskContainer.hasTip() && tipTaskContainer.getCurrentTask().isFulfilled()) {
      tipTaskContainer.next();
    }
    expect(tipTaskContainer.hasTip()).toBeFalse();
  });

  it('stops at the first task that still wants something', () => {
    const tipTaskContainer = container();
    const open = task(false);
    tipTaskContainer.add(task(true));
    tipTaskContainer.add(open);

    while (tipTaskContainer.hasTip() && tipTaskContainer.getCurrentTask().isFulfilled()) {
      tipTaskContainer.next();
    }

    expect(tipTaskContainer.getCurrentTask()).toBe(open);
  });
});
