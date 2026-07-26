import {fakeAsync, tick} from '@angular/core/testing';
import {IdleItemTipTask} from './idle-item-tip-task';
import {TipTaskContext} from './abstract-tip-task';
import {SelectTipTask} from './select-tip-task';
import {TipService} from '../tip.service';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {TipConfig} from '../../../gwtangular/GwtAngularFacade';
import {TipStallReason} from '../tip-stall';

/**
 * First task of every fallback chain - the one a tip lands in after the player has been through it
 * once. It is therefore regularly entered while the actor is somewhere off screen, where the item
 * does not exist at all: the null assertion it used to make threw and left the chain dead.
 */
describe('IdleItemTipTask', () => {
  const ITEM_TYPE_ID = 4;

  function createActor() {
    const actor = {
      idle: false,
      getId: () => 7,
      getPosition: () => ({getX: () => 10, getY: () => 20}),
      getIdle: () => actor.idle,
      isSelected: () => false,
      isSelectPromptVisible: () => true,
      showSelectPromptVisualization: () => {
      },
      hideSelectPromptVisualization: () => {
      },
      setIdleCallback: jasmine.createSpy('setIdleCallback')
    };
    return actor;
  }

  function createHarness(rendered: { current: BabylonBaseItemImpl | null }) {
    const renderService = {
      getBabylonBaseItemById: () => rendered.current,
      getBabylonBaseItemByDiplomacyItemType: () => rendered.current,
      getCurrentViewField: () => ({contains: () => true})
    } as unknown as BabylonRenderServiceAccessImpl;
    const outOfViewTargets: ({ x: number, y: number } | null)[] = [];
    const onSucceed = jasmine.createSpy('onSucceed');
    const tipService = {
      renderService,
      onSucceed,
      selectionService: {
        addSelectionListener: () => {
        },
        removeSelectionListener: () => {
        },
        hasOwnSelection: () => false
      },
      setOutOfViewTarget: (position: any) => {
        outOfViewTargets.push(position === null ? null : {x: position.getX(), y: position.getY()});
      }
    } as unknown as TipService;
    const context = new TipTaskContext(renderService);
    return {tipService, context, outOfViewTargets, onSucceed};
  }

  it('survives being entered while the actor is out of view', fakeAsync(() => {
    const rendered: { current: BabylonBaseItemImpl | null } = {current: null};
    const {tipService, context} = createHarness(rendered);
    context.setActor({getId: () => 7} as unknown as BabylonBaseItemImpl);
    const task = new IdleItemTipTask(tipService, context);

    expect(() => task.start()).not.toThrow();
    // Nothing has ever seen this actor, so it is not "out of view" but "not there yet".
    expect(task.getStallReason()).toBe(TipStallReason.ACTOR_NOT_FOUND);
    task.cleanup();
  }));

  it('picks up an actor that went idle while nobody was listening', fakeAsync(() => {
    // setIdle() only fires on a change, and the callback slot dies with the item as it streams
    // out - an item that went idle out there would never tell anyone.
    const actor = createActor();
    const rendered: { current: BabylonBaseItemImpl | null } = {current: null};
    const {tipService, context, onSucceed} = createHarness(rendered);
    context.setActor(actor as unknown as BabylonBaseItemImpl);
    const task = new IdleItemTipTask(tipService, context);

    task.start();
    actor.idle = true;
    rendered.current = actor as unknown as BabylonBaseItemImpl;
    tick(4000); // past the settle window, so the idle state counts

    expect(onSucceed).toHaveBeenCalled();
    task.cleanup();
  }));

  it('does not take the moment right after a command for being idle', fakeAsync(() => {
    // A factory that has just been sent a fabricate command still reports idle for a tick or two.
    // Believing it ended this task at once and the chain put the prompt straight back on the
    // button the player had just clicked.
    const actor = createActor();
    actor.idle = true;
    const rendered: { current: BabylonBaseItemImpl | null } = {current: actor as unknown as BabylonBaseItemImpl};
    const {tipService, context, onSucceed} = createHarness(rendered);
    context.setActor(actor as unknown as BabylonBaseItemImpl);
    const task = new IdleItemTipTask(tipService, context);

    task.start();
    expect(onSucceed).not.toHaveBeenCalled();
    tick(2000);
    expect(onSucceed).not.toHaveBeenCalled();

    // Still idle once it has had its chance: then it really has nothing to do.
    tick(2000);
    expect(onSucceed).toHaveBeenCalled();
    task.cleanup();
  }));

  it('points at where another task of the chain last saw the actor', fakeAsync(() => {
    // The fallback tasks are separate instances. Entered while the actor is already off screen,
    // one of them has nothing of its own to point at - the position has to be the chain's.
    const actor = createActor();
    const rendered: { current: BabylonBaseItemImpl | null } = {current: actor as unknown as BabylonBaseItemImpl};
    const {tipService, context, outOfViewTargets} = createHarness(rendered);
    const tipConfig = {getActorItemTypeId: () => ITEM_TYPE_ID} as unknown as TipConfig;

    const seenByAnotherTask = new SelectTipTask(tipConfig, tipService, context);
    seenByAnotherTask.start();
    seenByAnotherTask.cleanup();

    rendered.current = null; // gone by the time the fallback chain starts
    const task = new IdleItemTipTask(tipService, context);
    task.start();

    expect(outOfViewTargets[outOfViewTargets.length - 1]).toEqual({x: 10, y: 20});
    expect(task.getStallReason()).toBe(TipStallReason.ACTOR_OUT_OF_VIEW);
    task.cleanup();
  }));
});
