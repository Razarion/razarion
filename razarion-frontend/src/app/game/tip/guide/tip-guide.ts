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
import {decide, Decision, PendingOrder, PROMPT, ResourcePoint, TipKind, TipQuestSpec} from './tip-decision';
import {GuidanceView} from './guidance-view';
import {TipRegion} from './tip-region';
import {QuestMarker, QuestMarkerService} from '../../cockpit/main/radar/quest-marker.service';

export interface TipGuideDeps {
  renderService: BabylonRenderServiceAccessImpl;
  selectionService: SelectionService;
  actionService: ActionService;
  facade: () => GwtAngularFacade;
  itemCockpit: () => ItemCockpitComponent | null;
  stallTracker: TipStallTrackerService;
  firstInteractionTracker: FirstInteractionTrackerService;
  outOfViewMarkerConfig: MarkerConfig;
  /** The minimap's copy of where the tip points. Optional: nothing else depends on it. */
  questMarker?: QuestMarkerService;
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
  /** After a camera flight: long enough for the terrain tiles at the target to be built. */
  private static readonly GROUND_SETTLE_MILLIS = 400;
  /** Order of the steps, to tell the stall tracker a step forward from a step back. */
  private static readonly STEP_RANK: Record<string, number> = {
    [TipTaskName.SELECT]: 0,
    [TipTaskName.SELECT_GROUP]: 1,
    [TipTaskName.START_BUILD_PLACER]: 2,
    [TipTaskName.SEND_FABRICATE_COMMAND]: 2,
    [TipTaskName.SEND_HARVEST_COMMAND]: 2,
    [TipTaskName.SEND_ATTACK_COMMAND]: 2,
    [TipTaskName.SEND_LOAD_COMMAND]: 2,
    [TipTaskName.SEND_MOVE_COMMAND]: 3,
    [TipTaskName.START_UNLOAD_PLACER]: 4,
    [TipTaskName.SEND_UNLOAD_COMMAND]: 5,
    [TipTaskName.SEND_SELL_COMMAND]: 2,
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
  /** The world as the last evaluation saw it, for the build preparation between two evaluations. */
  private lastItems: TipItemState[] = [];
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
    const tip = tipConfig.getTipString() as TipKind;
    this.placeConfig = questConfig.getConditionConfig()?.getComparisonConfig().getPlaceConfig() ?? null;
    // SYNC_ITEM_POSITION counts the finished buildings in the region (a start region comes as one),
    // whenever they were built.
    const trigger = questConfig.getConditionConfig()?.getConditionTrigger();
    const countsTheBase = (tip === 'BUILD' || tip === 'FABRICATE')
      && !!trigger && GwtHelper.gwtIssue(trigger) === 'SYNC_ITEM_POSITION';
    this.quest = {
      questId: GwtHelper.gwtIssueNumberNull(questConfig.getId()) ?? null,
      tip,
      actorTypeId: GwtHelper.gwtIssueNumber(tipConfig.getActorItemTypeId()),
      targetTypeId,
      group: !!tipConfig.isGroup?.(),
      buildTargets: countsTheBase
        ? typeCount.map(entry => ({typeId: GwtHelper.gwtIssueNumber(entry[0]), count: GwtHelper.gwtIssueNumber(entry[1])}))
        : undefined
    };
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
    this.deps.itemCockpit()?.setBuildPreparation(null);
    this.view.clear();
    this.deps.questMarker?.set('tip', null);
    this.deps.stallTracker.stop();
    this.quest = null;
    this.decision = null;
    this.lastItems = [];
    this.orders.clear();
    this.markedTargetId = null;
    this.placerTypeId = null;
  }

  /**
   * Where the camera has to go before the build placer for this type opens: a point in the quest's
   * region when the quest counts that building there and the region is out of view, else null.
   * <p>
   * The Dockyard of quest 386 has to stand in the water, the player stands inland, and the placer
   * opens in the middle of the picture - on land. On PROD every player who failed 386 in a week
   * never placed one (2026-09-30); the arrow to the coast asked them to scroll with an open placer,
   * which is the navigation they had not understood.
   */
  buildRegionTarget(itemTypeId: number): { x: number, y: number } | null {
    const quest = this.quest;
    const wanted = quest !== null && (quest.targetTypeId === itemTypeId
      || !!quest.buildTargets?.some(target => target.typeId === itemTypeId));
    if (!quest || quest.tip !== 'BUILD' || !this.region || !wanted) {
      return null;
    }
    const viewField = this.deps.renderService.getCurrentViewField();
    if (!viewField || this.region.inView(viewField)) {
      return null;
    }
    // The part of the region nearest to the builder, which has to drive there - not to the middle of
    // the picture. From the picture, a base in the west was sent to a corner of the coast behind a
    // cliff the builder could not cross (phone test, 2026-10-02).
    const selected = new Set(this.deps.selectionService.getSelectedOwnItemIds());
    const builders = this.lastItems.filter(item => item.own && item.itemTypeId === quest.actorTypeId);
    const builder = builders.find(item => selected.has(item.id)) ?? builders[0];
    const center = viewField.getScreenCenter();
    return this.region.pointFor(builder ? {x: builder.x, y: builder.y} : {x: center.getX(), y: center.getY()});
  }

  /**
   * Before the build placer opens: the camera travels to the quest's region when it is out of view,
   * then waits a moment for the ground there to arrive - the placer can only judge ground on screen.
   */
  private prepareBuild(itemTypeId: number): Promise<void> | null {
    const target = this.buildRegionTarget(itemTypeId);
    if (!target) {
      return null;
    }
    this.deps.firstInteractionTracker.report('BUILD_CAMERA_FLIGHT', 'type=' + itemTypeId);
    return this.deps.renderService.flyViewFieldCenter(target.x, target.y)
      .then(() => new Promise<void>(resolve => setTimeout(resolve, TipGuide.GROUND_SETTLE_MILLIS)));
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
    this.lastItems = items;
    this.updateOrders(items);
    // Hooked on every evaluation: the cockpit component comes and goes with the game view.
    this.deps.itemCockpit()?.setBuildClickCallback(model => this.onFabricateClick(GwtHelper.gwtIssueNumber(model.itemTypeId)));
    this.deps.itemCockpit()?.setBuildPreparation(itemTypeId => this.prepareBuild(GwtHelper.gwtIssueNumber(itemTypeId)));

    const renderService = this.deps.renderService;
    const viewField = renderService.getCurrentViewField();
    // Inside the view field AND in the part of the picture where a prompt can be read - the view
    // field alone reaches under the HUD and to the very top edge (see isPromptReadable). Every
    // step that asks this question is about to put a prompt somewhere or send the player after it.
    const onScreen = (x: number, y: number, text?: string) => !!viewField
      && viewField.contains(GwtInstance.newDecimalPosition(x, y))
      && renderService.isPromptReadable(x, y, text);
    const viewCenter = viewField ? {x: viewField.getScreenCenter().getX(), y: viewField.getScreenCenter().getY()} : {x: 0, y: 0};
    const transport = TipGuide.isTransport(quest);
    const builder = items.find(item => item.own && item.itemTypeId === quest.actorTypeId) ?? null;
    // Crossing the water: the arrow goes from the loaded container, which is what sails.
    const regionAnchor = transport
      ? (items.find(item => item.own && (item.cargo ?? []).includes(quest.actorTypeId)) ?? builder)
      : builder;

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
      // Every tip may need a field: one that finds too little Razarion sends a harvester out.
      resourcesOnScreen: this.resourcesOnScreen((x, y) => onScreen(x, y, PROMPT.HARVEST)),
      nearestResource: from => {
        const position = facade.resourceUiService?.getNearestResourcePosition(from.x, from.y);
        return position ? {x: position.getX(), y: position.getY()} : null;
      },
      region: this.region && viewField
        ? {point: this.region.pointFor(regionAnchor ? {x: regionAnchor.x, y: regionAnchor.y} : null), inView: this.region.inView(viewField)}
        : null,
      regionDistance: point => this.region ? this.region.distanceTo(point) : null,
      containerTypesFor: itemTypeId => transport ? this.containerTypesFor(facade, items, itemTypeId) : [],
      containerRange: itemTypeId => {
        try {
          return facade.itemTypeService.getBaseItemTypeAngular(itemTypeId).getItemContainerType()?.getRange() ?? 0;
        } catch (e) {
          return 0;
        }
      },
      unloadBlock: () => {
        const cockpit = this.deps.itemCockpit();
        return cockpit ? cockpit.getUnloadTipBlockReason() : 'COCKPIT_NOT_READY';
      },
      sellBlock: () => {
        const cockpit = this.deps.itemCockpit();
        return cockpit ? cockpit.getSellTipBlockReason() : 'COCKPIT_NOT_READY';
      },
      canAfford: itemTypeId => {
        try {
          return baseItemUiService.getResources() >= facade.itemTypeService.getBaseItemTypeAngular(itemTypeId).getPrice();
        } catch (e) {
          return true;
        }
      },
      isHarvester: itemTypeId => {
        try {
          return !!facade.itemTypeService.getBaseItemTypeAngular(itemTypeId).getHarvesterType();
        } catch (e) {
          return false;
        }
      },
      itemLimit: itemTypeId => {
        try {
          return facade.gameUiControl.getMyLimitation4ItemType(itemTypeId);
        } catch (e) {
          return Number.MAX_SAFE_INTEGER;
        }
      },
      markedTargetId: this.markedTargetId
    });

    if (decision.guidance.kind === 'prompt' && quest.tip === 'ATTACK' && !decision.guidance.resource) {
      this.markedTargetId = decision.guidance.itemId;
    }
    // Companion of the renderer's RAZ_promptProbe: that one says whether a prompt could be seen
    // at a point, this one says which point the guide asked about and what it decided. Neither
    // is visible from the outside - a prompt that is not there and a step that never runs look
    // the same on the screen.
    if ((window as any).RAZ_promptProbe) {
      console.log('[TipProbe] ' + JSON.stringify({
        task: decision.taskName, reason: decision.reason, guidance: decision.guidance,
        selected: [...this.deps.selectionService.getSelectedOwnItemIds()],
        items: items.filter(item => item.own || item.itemTypeId === quest.targetTypeId)
          .map(item => `${item.id}:${item.itemTypeId}${item.own ? '' : '(enemy)'}@${Math.round(item.x)},${Math.round(item.y)}${item.idle ? '' : ' busy'}`)
      }));
    }
    this.reportStep(decision);
    this.reportGroupTip(quest, items, decision);
    this.view.show(decision.guidance, this.placeConfig);
    this.deps.questMarker?.set('tip', this.markerFor(decision, items));
  }

  /**
   * Where the minimap marks the tip's target: what the arrow or the prompt points at, or the quest's
   * region. The arrow at the screen edge gives a direction and no distance; the map gives both, and a
   * tap on it takes the camera there. Nothing to mark while the tip only waits and the quest has no
   * region - a marker on the unit that is already working would only add noise.
   */
  private markerFor(decision: Decision, items: TipItemState[]): QuestMarker | null {
    const guidance = decision.guidance;
    const region = QuestMarkerService.fromPlaceConfig(this.placeConfig);
    switch (guidance.kind) {
      case 'arrow':
        return {kind: 'point', x: guidance.x, y: guidance.y};
      case 'group':
        return guidance.arrow ? {kind: 'point', x: guidance.arrow.x, y: guidance.arrow.y} : region;
      case 'prompt': {
        if (guidance.resource) {
          const resource = this.deps.renderService.getBabylonResourceItemImpls().find(item => item.getId() === guidance.itemId);
          const position = resource?.getPosition();
          return position ? {kind: 'point', x: position.getX(), y: position.getY()} : region;
        }
        const item = items.find(candidate => candidate.id === guidance.itemId);
        return item ? {kind: 'point', x: item.x, y: item.y} : region;
      }
      case 'sell':
        // The building to sell, on the island the player has just left.
        return {kind: 'point', x: guidance.x, y: guidance.y};
      default:
        return region;
    }
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
      // A factory's queue holds only what waits behind the unit in production (SyncFactory.buildQueue),
      // so a single click never shows up in it: the order was only ever dropped by the 15 s timeout,
      // and the hint for the next viper came ten seconds after the last one stood (quest 369,
      // 2026-09-27). Producing, the factory reports busy - that is the order arriving.
      const busy = order.kind === 'fabricate'
        ? !item.idle || item.factoryBuildQueue.includes(order.targetTypeId ?? -1)
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
    || order.kind === 'finalize' || order.kind === 'move' || order.kind === 'load' ? order.kind : 'other';
    this.remember(order.unitIds, kind, order.targetTypeId);
  }

  /** Placing starts a build order for the selected builders - the placer is not an ActionService command. */
  private onPlacerEvent(event: BaseItemPlacerPresenterEvent): void {
    if (event === BaseItemPlacerPresenterEvent.PLACED && this.quest) {
      if (this.quest.tip === 'UNLOAD') {
        // The unload placer: the order goes to the selected container, not to a builder.
        this.remember(this.deps.selectionService.getSelectedOwnItemIds(), 'unload', null);
      } else {
        const builderIds = this.selectedOfType(this.quest.actorTypeId);
        this.remember(builderIds, 'build', this.placerTypeId ?? this.quest.targetTypeId);
      }
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

  private static isTransport(quest: TipQuestSpec): boolean {
    return quest.tip === 'LOAD' || quest.tip === 'SAIL' || quest.tip === 'UNLOAD';
  }

  /**
   * Container types that can carry the unit: the ones the player owns, and the ones an own factory
   * can fabricate - a transporter sunk on the way has to be found again in the dockyard's menu.
   */
  private containerTypesFor(facade: GwtAngularFacade, items: TipItemState[], itemTypeId: number): number[] {
    const canCarry = (typeId: number) => {
      try {
        return !!facade.itemTypeService.getBaseItemTypeAngular(typeId).getItemContainerType()?.isAbleToContain(itemTypeId);
      } catch (e) {
        return false;
      }
    };
    const candidates = new Set<number>();
    for (const typeId of new Set(items.filter(item => item.own).map(item => item.itemTypeId))) {
      candidates.add(typeId);
      try {
        facade.itemTypeService.getBaseItemTypeAngular(typeId).getFactoryType()?.getAbleToBuildIds()
          .forEach(buildable => candidates.add(buildable));
      } catch (e) {
        // not a type the client knows: nothing to add
      }
    }
    return [...candidates].filter(canCarry);
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
