import {fakeAsync, tick} from '@angular/core/testing';
import {SendBuildCommandTipTask} from './send-build-command-tip-task';
import {TipTaskContext} from './abstract-tip-task';
import {TipService} from '../tip.service';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {BabylonBaseItemImpl} from '../../renderer/babylon-base-item.impl';
import {TipStallReason} from '../tip-stall';
import {BaseItemPlacerPresenterEvent} from '../../renderer/base-item-placer-presenter.impl';

/**
 * The build tip follows a construction site until it stands. Ending on the click instead let the
 * player walk away from a half built factory with the quest open, the tip chain finished and
 * nothing recorded anywhere.
 */
describe('SendBuildCommandTipTask', () => {
  const ITEM_TYPE_ID = 4;

  /** @param buildup null models the moment after creation, before the engine reports one */
  function createSite(buildup: number | null, inView = true) {
    const site = {
      buildup,
      inView,
      getId: () => 1,
      itemType: {getId: () => ITEM_TYPE_ID},
      getBuildup: () => site.buildup ?? 1.0,
      getBuildupOrNull: () => site.buildup,
      getPosition: () => ({getX: () => 10, getY: () => 20}),
      promptVisible: false,
      isSelectPromptVisible: () => site.promptVisible,
      showSelectPromptVisualization: jasmine.createSpy('showSelectPromptVisualization')
        .and.callFake(() => site.promptVisible = true),
      hideSelectPromptVisualization: jasmine.createSpy('hideSelectPromptVisualization')
        .and.callFake(() => site.promptVisible = false),
      setItemClickCallback: jasmine.createSpy('setItemClickCallback')
    };
    return site;
  }

  /** The builder the tip acts on: it drives to the spot and only there lays the site down. */
  function createBuilder() {
    return {
      idle: false,
      getId: () => 99,
      getPosition: () => ({getX: () => 1, getY: () => 2}),
      getIdle: function () {
        return this.idle;
      },
      isSelected: () => true
    };
  }

  function createTask(sites: any[], selection = {own: true}, builder: any = null) {
    let placerCallback: ((event: BaseItemPlacerPresenterEvent) => void) | null = null;
    const renderService = {
      getBabylonBaseItemsByDiplomacy: () => sites,
      getBabylonBaseItemById: (id: number) =>
        (builder && builder.getId() === id ? builder : sites.find(site => site.getId() === id)) ?? null,
      getCurrentViewField: () => ({contains: () => sites[0]?.inView ?? true}),
      addViewFieldListener: () => {
      },
      removeViewFieldListener: () => {
      },
      showOutOfViewMarker: () => {
      },
      setBaseItemPlacerCallback: (callback: ((event: BaseItemPlacerPresenterEvent) => void) | null) => {
        placerCallback = callback;
      },
      showPlaceMarker: () => {
      },
      baseItemPlacerActive: false
    } as unknown as BabylonRenderServiceAccessImpl;
    const visualConfig = {
      getRadius: () => 1,
      getNodesMaterialId: () => 1,
      getPlaceNodesMaterialId: () => 1,
      getOutOfViewNodesMaterialId: () => 1,
      getOutOfViewSize: () => 1,
      getOutOfViewDistanceFromCamera: () => 1
    };
    const tipService = {
      renderService,
      selectionService: {
        addSelectionListener: () => {
        },
        removeSelectionListener: () => {
        },
        hasOwnSelection: () => selection.own
      },
      gwtAngularFacade: {
        gameUiControl: {
          getColdGameUiContext: () => ({getInGameQuestVisualConfig: () => visualConfig})
        }
      },
      setOutOfViewTarget: jasmine.createSpy('setOutOfViewTarget'),
      onSucceed: jasmine.createSpy('onSucceed'),
      onTaskFailed: jasmine.createSpy('onTaskFailed')
    } as unknown as TipService;
    const context = new TipTaskContext(renderService);
    if (builder) {
      context.setActor(builder);
    }
    const task = new SendBuildCommandTipTask(ITEM_TYPE_ID, null, tipService, context);
    return {
      task,
      tipService,
      /** The placer reports PLACED and, in the same click, DEACTIVATED right after it. */
      firePlacerEvent: (event: BaseItemPlacerPresenterEvent) => placerCallback?.(event)
    };
  }

  it('goes back to the build button when the order was dropped before it took hold', fakeAsync(() => {
    // The site is laid down by the builder on arrival, not on placement. A move command in
    // between drops the order: the builder stands idle and no site ever appears. Waiting for one
    // left the tip pointing at nothing at all - and the placer button is free again, because
    // without a site nothing of the type counts against the item limit.
    const sites: any[] = [];
    const builder = createBuilder();
    const {task, tipService, firePlacerEvent} = createTask(sites, {own: true}, builder);
    task.start();
    firePlacerEvent(BaseItemPlacerPresenterEvent.PLACED);

    // On its way there: nothing to complain about, however long it takes.
    tick(10000);
    expect(tipService.onTaskFailed).not.toHaveBeenCalled();

    builder.idle = true; // move command done, no build order left
    tick(1000);

    expect(tipService.onTaskFailed).toHaveBeenCalled();
    task.cleanup();
  }));

  it('gives the builder a moment to take up the order before believing its idle state', fakeAsync(() => {
    const sites: any[] = [];
    const builder = createBuilder();
    builder.idle = true; // still idle for a tick or two right after the placement
    const {task, tipService, firePlacerEvent} = createTask(sites, {own: true}, builder);
    task.start();
    firePlacerEvent(BaseItemPlacerPresenterEvent.PLACED);

    tick(2000);
    expect(tipService.onTaskFailed).not.toHaveBeenCalled();
    task.cleanup();
  }));

  it('asks for the builder once the site stops going up with nothing selected', fakeAsync(() => {
    // "Click to continue building" does nothing without the builder selected, and there is no
    // event to wait for: the placer clears the selection on its way out and a deselection out of
    // view fires nothing. The step back lands on "select the builder".
    const site = createSite(0.3);
    const {task, tipService} = createTask([site], {own: false});
    task.start();

    // While it is going up nothing is asked of the player - demanding a selection would be noise.
    tick(5000);
    expect(tipService.onTaskFailed).not.toHaveBeenCalled();

    tick(11000); // no progress for longer than NO_PROGRESS_MILLIS, plus the selection grace
    expect(tipService.onTaskFailed).toHaveBeenCalled();
    task.cleanup();
  }));

  it('keeps quiet while the builder is working on the site', fakeAsync(() => {
    // "Click to continue building" is an instruction - it may only be up while there is something
    // to do. It used to sit on every building under construction.
    const site = createSite(0.1);
    const {task} = createTask([site]);
    task.start();

    for (let i = 0; i < 15; i++) {
      site.buildup = (site.buildup ?? 0) + 0.02;
      tick(1000);
    }

    expect(site.showSelectPromptVisualization).not.toHaveBeenCalled();
    task.cleanup();
  }));

  it('speaks up once the site stops going up', fakeAsync(() => {
    const site = createSite(0.3);
    const {task} = createTask([site]);
    task.start();

    tick(5000);
    expect(site.showSelectPromptVisualization).not.toHaveBeenCalled();

    tick(6000); // past NO_PROGRESS_MILLIS without any change
    expect(site.showSelectPromptVisualization).toHaveBeenCalled();

    // And goes quiet again as soon as the builder is back at work.
    site.buildup = 0.4;
    tick(1000);
    expect(site.hideSelectPromptVisualization).toHaveBeenCalled();
    task.cleanup();
  }));

  it('leaves a site that is going up alone even with nothing selected', fakeAsync(() => {
    const site = createSite(0.3);
    const {task, tipService} = createTask([site], {own: false});
    task.start();

    for (let i = 0; i < 15; i++) {
      site.buildup = (site.buildup ?? 0) + 0.02;
      tick(1000);
    }

    expect(tipService.onTaskFailed).not.toHaveBeenCalled();
    task.cleanup();
  }));

  it('is not done while the building is still going up', () => {
    const site = createSite(0.4);
    const {task} = createTask([site]);
    task.start();
    expect(task.isFulfilled()).toBeFalse();
    task.cleanup();
  });

  it('is done once the site it follows stands', () => {
    const site = createSite(0.4);
    const {task} = createTask([site]);
    task.start();
    site.buildup = 1.0;
    expect(task.isFulfilled()).toBeTrue();
    task.cleanup();
  });

  it('is not done just because some other finished building of the type exists', () => {
    // "Build another factory" quests start with one already standing - answering "fulfilled"
    // would end the tip before it ever pointed at anything.
    const {task} = createTask([createSite(1.0)]);
    expect(task.isFulfilled()).toBeFalse();
  });

  it('survives the placement and follows the site that appears', fakeAsync(() => {
    const sites: any[] = [];
    const {task, tipService, firePlacerEvent} = createTask(sites);
    task.start();

    // Placing deactivates the placer, so both events arrive on the same click. Handling
    // DEACTIVATED as a failure here tore the tip down right after a successful placement.
    firePlacerEvent(BaseItemPlacerPresenterEvent.PLACED);
    firePlacerEvent(BaseItemPlacerPresenterEvent.DEACTIVATED);
    expect(tipService.onTaskFailed).not.toHaveBeenCalled();

    // The server creates the item a moment later, still without a reported buildup - which
    // getBuildup() answers as 1.0 and which used to end the tip on the spot.
    const site = createSite(null);
    sites.push(site);
    tick(1000);
    // No prompt yet: the builder is working on it, nothing is asked of the player.
    expect(site.showSelectPromptVisualization).not.toHaveBeenCalled();
    expect(tipService.onSucceed).not.toHaveBeenCalled();

    site.buildup = 0.3;
    tick(1000);
    expect(tipService.onSucceed).not.toHaveBeenCalled();

    site.buildup = 1.0;
    tick(1000);
    expect(tipService.onSucceed).toHaveBeenCalledTimes(1);
    task.cleanup();
  }));

  it('keeps prompting while the site sits half built and succeeds when it stands', fakeAsync(() => {
    const site = createSite(0.4);
    const {task, tipService} = createTask([site]);

    task.start();
    // The old task counted the click as done here. Two minutes of a builder that drove off:
    tick(120000);
    expect(site.showSelectPromptVisualization).toHaveBeenCalled();
    expect(tipService.onSucceed).not.toHaveBeenCalled();

    site.buildup = 1.0;
    tick(1000);
    expect(tipService.onSucceed).toHaveBeenCalledTimes(1);
    task.cleanup();
  }));

  it('names a frozen construction site instead of blaming the player', fakeAsync(() => {
    const site = createSite(0.4);
    const {task} = createTask([site]);

    task.start();
    tick(5000);
    expect(task.getStallReason()).toBe(TipStallReason.AWAIT_BUILD_FINALIZE);

    // Buildup has not moved: the builder left, and no click on the site will change that until
    // the player sends it back.
    tick(10000);
    expect(task.getStallReason()).toBe(TipStallReason.BUILD_NOT_PROGRESSING);
    task.cleanup();
  }));

  it('reports the site being off screen rather than an idle player', fakeAsync(() => {
    const site = createSite(0.4, false);
    const {task} = createTask([site]);

    task.start();
    tick(2000);
    expect(task.getStallReason()).toBe(TipStallReason.TARGET_OUT_OF_VIEW);
    task.cleanup();
  }));

  it('takes the prompt down when it is cleaned up', fakeAsync(() => {
    const site = createSite(0.4);
    const {task} = createTask([site]);

    task.start();
    task.cleanup();
    expect(site.hideSelectPromptVisualization).toHaveBeenCalled();
    // No timer may outlive the task - it would keep polling a site nobody points at any more.
    tick(5000);
  }));
});
