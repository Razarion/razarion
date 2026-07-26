import {fakeAsync, tick} from '@angular/core/testing';
import {SelectTipTask} from './select-tip-task';
import {TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {TipConfig} from '../../../gwtangular/GwtAngularFacade';
import {TipStallReason} from '../tip-stall';

/**
 * The actor of the select tip is the only one that moves - a builder drives off on its own while
 * the tip asks for it to be selected. Its position was read once at start, so the out-of-view
 * marker kept pointing at the spot the builder had left, the prompt died with the item as it
 * streamed out, and the stall was recorded as "player did not act".
 */
describe('SelectTipTask', () => {
  const ITEM_TYPE_ID = 4;

  function createActor(x: number, y: number) {
    const actor = {
      x,
      y,
      promptVisible: false,
      getId: () => 7,
      getPosition: () => ({getX: () => actor.x, getY: () => actor.y}),
      isSelected: () => false,
      isSelectPromptVisible: () => actor.promptVisible,
      showSelectPromptVisualization: jasmine.createSpy('showSelectPromptVisualization')
        .and.callFake(() => actor.promptVisible = true),
      hideSelectPromptVisualization: jasmine.createSpy('hideSelectPromptVisualization')
        .and.callFake(() => actor.promptVisible = false)
    };
    return actor;
  }

  /** @param rendered what the renderer answers for the actor - null models "streamed out" */
  function createTask(rendered: { current: BabylonBaseItemImpl | null }) {
    const renderService = {
      getBabylonBaseItemByDiplomacyItemType: () => rendered.current,
      getBabylonBaseItemById: () => rendered.current,
      getCurrentViewField: () => ({contains: () => true})
    } as unknown as BabylonRenderServiceAccessImpl;
    const outOfViewTargets: ({ x: number, y: number } | null)[] = [];
    const tipService = {
      renderService,
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
    const tipConfig = {getActorItemTypeId: () => ITEM_TYPE_ID} as unknown as TipConfig;
    const task = new SelectTipTask(tipConfig, tipService, new TipTaskContext(renderService));
    return {task, outOfViewTargets};
  }

  it('follows the actor as it drives, so the marker can rise when it leaves the view', fakeAsync(() => {
    const actor = createActor(10, 20);
    const rendered: {current: BabylonBaseItemImpl | null} = {current: actor as unknown as BabylonBaseItemImpl};
    const {task, outOfViewTargets} = createTask(rendered);

    task.start();
    expect(outOfViewTargets).toEqual([{x: 10, y: 20}]);

    actor.x = 300;
    actor.y = 400;
    tick(1000);

    // Re-set even though the camera never moved: only setting the target recomputes the marker.
    expect(outOfViewTargets[outOfViewTargets.length - 1]).toEqual({x: 300, y: 400});
    task.cleanup();
  }));

  it('keeps pointing at the last known position while the actor is off screen', fakeAsync(() => {
    const actor = createActor(10, 20);
    const rendered: {current: BabylonBaseItemImpl | null} = {current: actor as unknown as BabylonBaseItemImpl};
    const {task, outOfViewTargets} = createTask(rendered);

    task.start();
    actor.x = 300;
    actor.y = 400;
    tick(1000);
    // Out of the view field the item is not rendered at all - nothing left but where it was.
    rendered.current = null;
    tick(1000);

    expect(outOfViewTargets[outOfViewTargets.length - 1]).toEqual({x: 300, y: 400});
    expect(task.getStallReason()).toBe(TipStallReason.ACTOR_OUT_OF_VIEW);
    task.cleanup();
  }));

  it('separates an actor that drove off from one that never existed', fakeAsync(() => {
    const rendered: {current: BabylonBaseItemImpl | null} = {current: null};
    const {task, outOfViewTargets} = createTask(rendered);

    // Nothing ever seen: the client engine may still be syncing, not a stall the player can fix.
    task.start();
    expect(task.getStallReason()).toBe(TipStallReason.ACTOR_NOT_FOUND);
    expect(outOfViewTargets).toEqual([]);
    task.cleanup();
  }));

  it('blames the wait on the actor that was gone for most of it, not on the player', fakeAsync(() => {
    const actor = createActor(10, 20);
    const rendered: { current: BabylonBaseItemImpl | null } = {current: actor as unknown as BabylonBaseItemImpl};
    const {task} = createTask(rendered);

    task.start();
    rendered.current = null;
    tick(15000);
    // Back on screen when the watchdog fires - which used to be all it ever saw.
    rendered.current = actor as unknown as BabylonBaseItemImpl;
    tick(2000);

    expect(task.getStallReason()).toBe(TipStallReason.ACTOR_OUT_OF_VIEW);

    // Long enough back to be the player's turn again.
    tick(15000);
    expect(task.getStallReason()).toBe(TipStallReason.AWAIT_SELECTION);
    task.cleanup();
  }));

  it('puts the prompt back on the same instance that came back into view', fakeAsync(() => {
    // Scrolling away disposes the prompt with the item. What comes back is not always a new
    // object, so "same instance" must not be taken for "prompt is still up" - that left the
    // builder standing there unmarked while the task reported AWAIT_SELECTION.
    const actor = createActor(10, 20);
    const rendered: { current: BabylonBaseItemImpl | null } = {current: actor as unknown as BabylonBaseItemImpl};
    const {task} = createTask(rendered);

    task.start();
    expect(actor.showSelectPromptVisualization).toHaveBeenCalledTimes(1);

    rendered.current = null;
    actor.promptVisible = false; // died with the item as it left the view
    tick(1000);
    rendered.current = actor as unknown as BabylonBaseItemImpl;
    tick(1000);

    expect(actor.showSelectPromptVisualization).toHaveBeenCalledTimes(2);
    task.cleanup();
  }));

  it('puts the prompt back up on the instance that streamed back in', fakeAsync(() => {
    const first = createActor(10, 20);
    const rendered: {current: BabylonBaseItemImpl | null} = {current: first as unknown as BabylonBaseItemImpl};
    const {task} = createTask(rendered);

    task.start();
    expect(first.showSelectPromptVisualization).toHaveBeenCalledTimes(1);

    rendered.current = null;
    tick(1000);
    // Same id, new instance - the prompt of the old one went down with it.
    const second = createActor(10, 20);
    rendered.current = second as unknown as BabylonBaseItemImpl;
    tick(1000);

    expect(second.showSelectPromptVisualization).toHaveBeenCalledTimes(1);
    expect(task.getStallReason()).toBe(TipStallReason.AWAIT_SELECTION);
    task.cleanup();
  }));
});
