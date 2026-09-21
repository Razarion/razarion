import {GwtAngularFacade, MarkerConfig, PlaceConfig, QuestConfig, TipItemState} from '../../../gwtangular/GwtAngularFacade';
import {BabylonRenderServiceAccessImpl} from '../../renderer/babylon-render-service-access-impl.service';
import {BaseItemPlacerPresenterEvent} from '../../renderer/base-item-placer-presenter.impl';
import {ItemCockpitComponent} from '../../cockpit/item/item-cockpit.component';
import {SelectionService} from '../../selection.service';
import {ActionService, OrderNote} from '../../action.service';
import {TipStallTrackerService} from '../tip-stall-tracker.service';
import {FirstInteractionTrackerService} from '../../tracking/first-interaction-tracker.service';
import {TipStallSource, TipTaskName} from '../tip-stall';
import {GwtHelper} from '../../../gwtangular/GwtHelper';
import {GwtInstance} from '../../../gwtangular/GwtInstance';
import {ViewField, ViewFieldListener} from '../../renderer/view-field';
import {decide, Decision, PendingOrder, ResourcePoint, TipKind, TipQuestSpec} from './tip-decision';
import {GuidanceView} from './guidance-view';
import {TipRegion} from './tip-region';

export interface TipGuideDeps {
  renderService: BabylonRenderServiceAccessImpl;
  selectionService: SelectionService;
  actionService: ActionService;
  facade: () => GwtAngularFacade;
  itemCockpit: () => ItemCockpitComponent | null;
  stallTracker: TipStallTrackerService;
  firstInteractionTracker: FirstInteractionTrackerService;
  outOfViewMarkerConfig: MarkerConfig;
}

/** An order as the guide remembers it, until the engine has carried it out or never took it up. */
interface RememberedOrder extends PendingOrder {
  at: number;
}

/**
 * Runs the quest tip: every half second - and at once on a selection change, a camera move or an
 * order - it takes a snapshot of the world by id, decides what the player should be told, and puts
 * that on the screen. See docs/architecture/quest-tip-redesign.md.
 */
export class TipGuide implements ViewFieldListener {
  private static readonly EVALUATE_MILLIS = 500;
  /**
   * How long an order may take to show up in the engine before it is taken as never arrived. The
   * transport decides how long a tick takes: without SharedArrayBuffer (Meta webview) it is seconds.
   */
  private static readonly ORDER_ARRIVAL_MILLIS = 15000;
  /** Order of the steps, to tell the stall tracker a step forward from a step back. */
  private static readonly STEP_RANK: Record<string, number> = {
    [TipTaskName.SELECT]: 0,
    [TipTaskName.SELECT_GROUP]: 1,
    [TipTaskName.START_BUILD_PLACER]: 2,
    [TipTaskName.SEND_FABRICATE_COMMAND]: 2,
    [TipTaskName.SEND_HARVEST_COMMAND]: 2,
    [TipTaskName.SEND_ATTACK_COMMAND]: 2,
    [TipTaskName.SEND_BUILD_COMMAND]: 3,
    [TipTaskName.IDLE_ITEM]: 4
  };

  private quest: TipQuestSpec | null = null;
  private placeConfig: PlaceConfig | null = null;
  private region: TipRegion | null = null;
  private timer: ReturnType<typeof setTimeout> | null = null;
  private readonly orders = new Map<number, RememberedOrder>();
  private markedTargetId: number | null = null;
  private placerTypeId: number | null = null;
  private decision: Decision | null = null;
  private readonly view: GuidanceView;
  private readonly stallSource: TipStallSource = {
    getTaskName: () => this.decision?.taskName ?? TipTaskName.SELECT,
    getStallReason: () => this.decision?.reason ?? 'WAITING',
    asksPlayer: () => this.decision?.guidance.kind !== 'quiet'
  };
  private readonly selectionListener = () => this.evaluate();
  private readonly orderListener = (order: OrderNote) => this.onOrder(order);

  constructor(private readonly deps: TipGuideDeps) {
    this.view = new GuidanceView(deps.renderService, deps.itemCockpit, deps.outOfViewMarkerConfig,
      () => this.placeMarkerConfig());
  }

  activate(questConfig: QuestConfig): void {
    this.deactivate();
    const tipConfig = questConfig.getTipConfig()!;
    const typeCount = questConfig.getConditionConfig()?.getComparisonConfig().toTypeCountAngular() ?? [];
    const targetTypeId = typeCount.length > 0 ? GwtHelper.gwtIssueNumber(typeCount[0][0]) : null;
    this.quest = {
      questId: GwtHelper.gwtIssueNumberNull(questConfig.getId()) ?? null,
      tip: tipConfig.getTipString() as TipKind,
      actorTypeId: GwtHelper.gwtIssueNumber(tipConfig.getActorItemTypeId()),
      targetTypeId,
      group: !!tipConfig.isGroup?.()
    };
    this.placeConfig = questConfig.getConditionConfig()?.getComparisonConfig().getPlaceConfig() ?? null;
    this.region = this.placeConfig ? new TipRegion(this.placeConfig) : null;
    this.deps.selectionService.addSelectionListener(this.selectionListener);
    this.deps.actionService.addOrderListener(this.orderListener);
    this.deps.renderService.addViewFieldListener(this);
    this.deps.renderService.setBaseItemPlacerCallback(event => this.onPlacerEvent(event));
    this.evaluate();
  }

  deactivate(): void {
    if (this.timer !== null) {
      clearTimeout(this.timer);
      this.timer = null;
    }
    if (!this.quest) {
      return;
    }
    this.deps.selectionService.removeSelectionListener(this.selectionListener);
    this.deps.actionService.removeOrderListener(this.orderListener);
    this.deps.renderService.removeViewFieldListener(this);
    this.deps.renderService.setBaseItemPlacerCallback(null);
    this.deps.itemCockpit()?.setBuildClickCallback(null);
    this.view.clear();
    this.deps.stallTracker.stop();
    this.quest = null;
    this.decision = null;
    this.orders.clear();
    this.markedTargetId = null;
    this.placerTypeId = null;
  }

  onViewFieldChanged(_viewField: ViewField): void {
    this.view.onViewFieldChanged();
    this.evaluate();
  }

  private evaluate(): void {
    const quest = this.quest;
    if (!quest) {
      return;
    }
    if (this.timer !== null) {
      clearTimeout(this.timer);
    }
    this.timer = setTimeout(() => {
      this.timer = null;
      this.evaluate();
    }, TipGuide.EVALUATE_MILLIS);
    try {
      this.evaluateNow(quest);
    } catch (exception) {
      // Runs on a timer and on renderer callbacks: an exception has no other way out. The next
      // evaluation is already armed above, so a passing fault costs one half second, not the tip.
      this.deps.stallTracker.reportChainError(quest.questId, 'evaluate');
      console.error(exception);
    }
  }

  private evaluateNow(quest: TipQuestSpec): void {
    const facade = this.deps.facade();
    const baseItemUiService = facade.baseItemUiService;
    if (!baseItemUiService) {
      return;
    }
    const items = baseItemUiService.getTipItemStates(quest.tip === 'ATTACK' ? (quest.targetTypeId ?? 0) : -1);
    this.updateOrders(items);
    // Hooked on every evaluation: the cockpit component comes and goes with the game view.
    this.deps.itemCockpit()?.setBuildClickCallback(model => this.onFabricateClick(GwtHelper.gwtIssueNumber(model.itemTypeId)));

    const renderService = this.deps.renderService;
    const viewField = renderService.getCurrentViewField();
    const onScreen = (x: number, y: number) => !!viewField && viewField.contains(GwtInstance.newDecimalPosition(x, y));
    const viewCenter = viewField ? {x: viewField.getScreenCenter().getX(), y: viewField.getScreenCenter().getY()} : {x: 0, y: 0};
    const builder = items.find(item => item.own && item.itemTypeId === quest.actorTypeId) ?? null;

    const decision = decide({
      quest,
      items,
      selectedIds: new Set(this.deps.selectionService.getSelectedOwnItemIds()),
      orders: this.orders,
      onScreen,
      viewCenter,
      placer: {active: renderService.baseItemPlacerActive, typeId: this.placerTypeId},
      buttonBlock: itemTypeId => {
        const cockpit = this.deps.itemCockpit();
        return cockpit ? cockpit.getBuildupTipBlockReason(itemTypeId) : 'COCKPIT_NOT_READY';
      },
      factoriesFor: itemTypeId => this.factoriesFor(facade, items, itemTypeId),
      resourcesOnScreen: quest.tip === 'HARVEST' ? this.resourcesOnScreen(onScreen) : [],
      nearestResource: from => {
        const position = facade.resourceUiService?.getNearestResourcePosition(from.x, from.y);
        return position ? {x: position.getX(), y: position.getY()} : null;
      },
      region: this.region && viewField
        ? {point: this.region.pointFor(builder ? {x: builder.x, y: builder.y} : null), inView: this.region.inView(viewField)}
        : null,
      markedTargetId: this.markedTargetId
    });

    if (decision.guidance.kind === 'prompt' && quest.tip === 'ATTACK' && !decision.guidance.resource) {
      this.markedTargetId = decision.guidance.itemId;
    }
    this.reportStep(decision);
    this.reportGroupTip(quest, items, decision);
    this.view.show(decision.guidance, this.placeConfig);
  }

  /**
   * The GROUP_TIP record of the first-interaction tracking, as the group task of the old chain
   * wrote it: asked when the tip asks for a group, skipped when the quest wants one and the player
   * owns too few to form it. The tracker keeps the first of each, so this can run on every pass.
   */
  private reportGroupTip(quest: TipQuestSpec, items: TipItemState[], decision: Decision): void {
    if (!quest.group) {
      return;
    }
    if (decision.taskName === TipTaskName.SELECT_GROUP) {
      this.deps.firstInteractionTracker.report('GROUP_TIP', 'state=asked');
    } else if (decision.taskName === TipTaskName.SEND_ATTACK_COMMAND
      && items.filter(item => item.own && item.itemTypeId === quest.actorTypeId).length < 2) {
      this.deps.firstInteractionTracker.report('GROUP_TIP', 'state=skipped');
    }
  }

  /**
   * An order stays remembered while it is on its way or being carried out: seen busy means it
   * arrived, idle after that means it is done, and idle for too long means it never arrived.
   */
  private updateOrders(items: TipItemState[]): void {
    const now = Date.now();
    for (const [unitId, order] of [...this.orders]) {
      const item = items.find(candidate => candidate.id === unitId);
      if (!item) {
        this.orders.delete(unitId);
        continue;
      }
      const busy = order.kind === 'fabricate'
        ? item.factoryBuildQueue.includes(order.targetTypeId ?? -1)
        : !item.idle;
      if (busy) {
        order.arrived = true;
      } else if (order.arrived || now - order.at > TipGuide.ORDER_ARRIVAL_MILLIS) {
        this.orders.delete(unitId);
      }
    }
  }

  private onOrder(order: OrderNote): void {
    const kind: PendingOrder['kind'] = order.kind === 'attack' || order.kind === 'harvest'
    || order.kind === 'finalize' || order.kind === 'move' ? order.kind : 'other';
    this.remember(order.unitIds, kind, order.targetTypeId);
  }

  /** Placing starts a build order for the selected builders - the placer is not an ActionService command. */
  private onPlacerEvent(event: BaseItemPlacerPresenterEvent): void {
    if (event === BaseItemPlacerPresenterEvent.PLACED && this.quest) {
      const builderIds = this.selectedOfType(this.quest.actorTypeId);
      this.remember(builderIds, 'build', this.placerTypeId ?? this.quest.targetTypeId);
    }
    if (event === BaseItemPlacerPresenterEvent.DEACTIVATED) {
      this.placerTypeId = null;
    }
    this.evaluate();
  }

  /**
   * A click on a cockpit button: for a factory an order, for a builder the start of the placer. The
   * placer's own event does not say which type it places, this does.
   */
  private onFabricateClick(itemTypeId: number): void {
    const quest = this.quest;
    if (!quest) {
      return;
    }
    if (quest.tip === 'BUILD') {
      this.placerTypeId = itemTypeId;
    } else {
      const factoryIds = this.deps.selectionService.getSelectedOwnItemIds().filter(id => {
        const type = this.deps.selectionService.getBaseItemTypeForId(id);
        return !!type?.getFactoryType()?.getAbleToBuildIds().includes(itemTypeId);
      });
      this.remember(factoryIds, 'fabricate', itemTypeId);
    }
    this.evaluate();
  }

  private remember(unitIds: number[], kind: PendingOrder['kind'], targetTypeId: number | null): void {
    const at = Date.now();
    unitIds.forEach(unitId => this.orders.set(unitId, {kind, targetTypeId, arrived: false, at}));
  }

  private selectedOfType(itemTypeId: number): number[] {
    return this.deps.selectionService.getSelectedOwnItemIdsOfType(itemTypeId);
  }

  private factoriesFor(facade: GwtAngularFacade, items: TipItemState[], itemTypeId: number): number[] {
    const ownTypes = new Set(items.filter(item => item.own).map(item => item.itemTypeId));
    return [...ownTypes].filter(typeId => {
      try {
        return !!facade.itemTypeService.getBaseItemTypeAngular(typeId).getFactoryType()?.getAbleToBuildIds().includes(itemTypeId);
      } catch (e) {
        return false;
      }
    });
  }

  private resourcesOnScreen(onScreen: (x: number, y: number) => boolean): ResourcePoint[] {
    const result: ResourcePoint[] = [];
    for (const resource of this.deps.renderService.getBabylonResourceItemImpls()) {
      const position = resource.getPosition();
      if (position && onScreen(position.getX(), position.getY())) {
        result.push({id: resource.getId(), x: position.getX(), y: position.getY()});
      }
    }
    return result;
  }

  /** Tells the stall watchdog when the step changes, as today's tasks did when one ended. */
  private reportStep(decision: Decision): void {
    const previous = this.decision;
    this.decision = decision;
    if (previous && previous.taskName === decision.taskName) {
      return;
    }
    if (previous) {
      // Leaving IDLE_ITEM is never a step back: the unit finished what it was doing, and the next
      // round of a quest that asks for ten Razarion or three vipers starts below it by design
      // (HRV-06, FAB-06). Counting it as a failure reported the harvest quests as a restart loop
      // (X-09; PROD 363/366 SEND_HARVEST_COMMAND|CHAIN_THRASHING, 18.-20.09.2026).
      const forward = previous.taskName === TipTaskName.IDLE_ITEM
        || (TipGuide.STEP_RANK[decision.taskName] ?? 0) >= (TipGuide.STEP_RANK[previous.taskName] ?? 0);
      this.deps.stallTracker.taskEnded(forward);
    }
    this.deps.stallTracker.taskStarted(this.quest?.questId ?? null, this.stallSource);
  }

  private placeMarkerConfig(): MarkerConfig {
    const config = this.deps.facade().gameUiControl.getColdGameUiContext().getInGameQuestVisualConfig();
    return {
      radius: config.getRadius(),
      nodesMaterialId: config.getNodesMaterialId(),
      placeNodesMaterialId: config.getPlaceNodesMaterialId(),
      outOfViewNodesMaterialId: config.getOutOfViewNodesMaterialId(),
      outOfViewSize: config.getOutOfViewSize(),
      outOfViewDistanceFromCamera: config.getOutOfViewDistanceFromCamera()
    };
  }
}
