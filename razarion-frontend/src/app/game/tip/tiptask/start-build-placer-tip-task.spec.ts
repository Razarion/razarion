import {fakeAsync, tick} from '@angular/core/testing';
import {StartBuildPlacerTipTask} from './start-build-placer-tip-task';
import {TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {TipStallReason} from '../tip-stall';

/**
 * This tip points at a cockpit button while the item it belongs to is out in the world. Scrolling
 * the builder away takes the selection and with it the cockpit, so the task waited on
 * COCKPIT_NOT_READY forever - reported as a rendering defect, and with no marker pointing back at
 * the builder, because the marker was only set on the branch where the cockpit was already there.
 */
describe('StartBuildPlacerTipTask', () => {
  const TO_BE_BUILT_ITEM_TYPE_ID = 4;

  function createActor(x: number, y: number) {
    const actor = {
      x,
      y,
      selected: true,
      getId: () => 7,
      getPosition: () => ({getX: () => actor.x, getY: () => actor.y}),
      isSelected: () => actor.selected,
      setSelectionCallback: () => {
      }
    };
    return actor;
  }

  /** @param cockpit null models the cockpit not being rendered - the out of view case */
  function createTask(rendered: { current: BabylonBaseItemImpl | null }, cockpit: any = null) {
    const renderService = {
      getBabylonBaseItemById: () => rendered.current,
      getBabylonBaseItemByDiplomacyItemType: () => rendered.current,
      getCurrentViewField: () => ({contains: () => true}),
      setBaseItemPlacerCallback: () => {
      }
    } as unknown as BabylonRenderServiceAccessImpl;
    const outOfViewTargets: ({ x: number, y: number } | null)[] = [];
    const onTaskFailed = jasmine.createSpy('onTaskFailed');
    const tipService = {
      renderService,
      getItemCockpit: () => cockpit,
      onTaskFailed,
      setOutOfViewTarget: (position: any) => {
        outOfViewTargets.push(position === null ? null : {x: position.getX(), y: position.getY()});
      }
    } as unknown as TipService;
    const context = new TipTaskContext(renderService);
    context.setActor({getId: () => 7} as unknown as BabylonBaseItemImpl);
    const task = new StartBuildPlacerTipTask(TO_BE_BUILT_ITEM_TYPE_ID, tipService, context);
    return {task, outOfViewTargets, onTaskFailed};
  }

  it('points back at the actor while the cockpit is missing', fakeAsync(() => {
    const actor = createActor(10, 20);
    const rendered: { current: BabylonBaseItemImpl | null } = {current: actor as unknown as BabylonBaseItemImpl};
    const {task, outOfViewTargets} = createTask(rendered);

    task.start();
    expect(outOfViewTargets).toEqual([{x: 10, y: 20}]);

    // Drives off and out of the view field, where it is not rendered at all any more.
    actor.x = 300;
    actor.y = 400;
    tick(1000);
    rendered.current = null;
    tick(1000);

    expect(outOfViewTargets[outOfViewTargets.length - 1]).toEqual({x: 300, y: 400});
    task.cleanup();
  }));

  it('reports the builder that left, not a broken cockpit', fakeAsync(() => {
    const actor = createActor(10, 20);
    const rendered: { current: BabylonBaseItemImpl | null } = {current: actor as unknown as BabylonBaseItemImpl};
    const {task} = createTask(rendered);

    task.start();
    expect(task.getStallReason()).toBe(TipStallReason.COCKPIT_NOT_READY);

    rendered.current = null;
    tick(1000);

    // The cockpit is empty *because* the actor is gone - the same root cause, not two defects.
    expect(task.getStallReason()).toBe(TipStallReason.ACTOR_OUT_OF_VIEW);
    task.cleanup();
  }));

  it('keeps the marker on the actor once the cockpit is up', fakeAsync(() => {
    const actor = createActor(10, 20);
    const rendered: { current: BabylonBaseItemImpl | null } = {current: actor as unknown as BabylonBaseItemImpl};
    const cockpit = {
      showBuildupTip: () => true,
      getBuildupTipBlockReason: () => null
    };
    const {task, outOfViewTargets} = createTask(rendered, cockpit);

    task.start();
    expect(task.getStallReason()).toBe(TipStallReason.AWAIT_PLACER);

    actor.x = 300;
    actor.y = 400;
    tick(1000);

    expect(outOfViewTargets[outOfViewTargets.length - 1]).toEqual({x: 300, y: 400});
    task.cleanup();
  }));

  it('keeps asserting the prompt while it waits for the placer', fakeAsync(() => {
    const actor = createActor(10, 20);
    const rendered: { current: BabylonBaseItemImpl | null } = {current: actor as unknown as BabylonBaseItemImpl};
    const cockpit = {
      showBuildupTip: jasmine.createSpy('showBuildupTip').and.returnValue(true),
      getBuildupTipBlockReason: () => null
    };
    const {task} = createTask(rendered, cockpit);

    // Waiting is not enough: a selection event rebuilds the cockpit and the popover is left on a
    // button that is no longer in the document, with nothing to notice it.
    task.start();
    tick(3000);

    expect(cockpit.showBuildupTip).toHaveBeenCalledTimes(4);
    task.cleanup();
  }));

  it('goes back a step when the actor was deselected out of view', fakeAsync(() => {
    // The deselection happens while the item - and with it the selection callback - does not
    // exist. Nothing fires, so the task has to notice by looking, or it waits on a cockpit that
    // cannot appear without a selection.
    const actor = createActor(10, 20);
    const rendered: { current: BabylonBaseItemImpl | null } = {current: actor as unknown as BabylonBaseItemImpl};
    const {task, onTaskFailed} = createTask(rendered);

    task.start();
    rendered.current = null;
    tick(1000);
    actor.selected = false; // deselected out there, unnoticed
    rendered.current = actor as unknown as BabylonBaseItemImpl;
    tick(1000);

    // Grace period: a stray click or the short deselect while a placer starts must not count.
    expect(onTaskFailed).not.toHaveBeenCalled();
    tick(1500);
    expect(onTaskFailed).toHaveBeenCalled();
    task.cleanup();
  }));

  it('stays put while the actor is out of view', fakeAsync(() => {
    // Out there the selection state is whatever it was when the item left - not something to
    // tear the chain down over.
    const actor = createActor(10, 20);
    actor.selected = false;
    const rendered: { current: BabylonBaseItemImpl | null } = {current: null};
    const {task, onTaskFailed} = createTask(rendered);

    task.start();
    tick(5000);

    expect(onTaskFailed).not.toHaveBeenCalled();
    task.cleanup();
  }));

  it('falls back to the block reason when the button goes away again', fakeAsync(() => {
    const actor = createActor(10, 20);
    const rendered: { current: BabylonBaseItemImpl | null } = {current: actor as unknown as BabylonBaseItemImpl};
    const cockpit = {
      pointable: true,
      showBuildupTip: () => cockpit.pointable,
      getBuildupTipBlockReason: () => cockpit.pointable ? null : TipStallReason.BUTTON_DISABLED
    };
    const {task} = createTask(rendered, cockpit);

    task.start();
    expect(task.getStallReason()).toBe(TipStallReason.AWAIT_PLACER);

    // Razarion spent elsewhere in the meantime: the tip has to say so instead of standing there.
    cockpit.pointable = false;
    tick(1000);

    expect(task.getStallReason()).toBe(TipStallReason.BUTTON_DISABLED);
    task.cleanup();
  }));
});
