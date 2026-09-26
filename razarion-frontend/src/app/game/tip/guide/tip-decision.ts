import {TipItemState} from '../../../gwtangular/GwtAngularFacade';
import {TipStallReason, TipTaskName} from '../tip-stall';

/**
 * What the quest tip decides, as a pure function of the world.
 *
 * Replaces the chain of tip tasks (docs/architecture/quest-tip-redesign.md). Nothing in here holds
 * an item instance, waits for an event or remembers a position in a chain: every evaluation looks at
 * the world by id and says what the player should be told now. A unit that scrolled out and back, a
 * target that died, a site that was destroyed, a selection that changed while nobody looked - the
 * next evaluation simply sees it.
 */

/**
 * LOAD, SAIL and UNLOAD are the three steps off the noob island (quest 392 split in three): the
 * actor is the unit that crosses, the container is whatever can carry it.
 */
export type TipKind = 'BUILD' | 'FABRICATE' | 'HARVEST' | 'ATTACK' | 'LOAD' | 'SAIL' | 'UNLOAD' | 'SELL';

export interface TipQuestSpec {
  questId: number | null;
  tip: TipKind;
  actorTypeId: number;
  /** BUILD: the building; FABRICATE: the unit; ATTACK: the enemy type, null for any. */
  targetTypeId: number | null;
  /** Ask for a group before the attack (TipConfig.group). */
  group: boolean;
  /**
   * BUILD, when the quest counts the finished buildings that stand in its region (SYNC_ITEM_POSITION;
   * 386, and 395/396 with the Phase 2 start region): all it asks for, in order. The tip asks for the
   * first type the region still has too few of, and the player's own choice among the missing ones
   * is just as good. Without a region the whole base counts.
   */
  buildTargets?: { typeId: number, count: number }[];
}

/** An order this client sent that the engine may not have taken up yet (W8). */
export interface PendingOrder {
  kind: 'attack' | 'harvest' | 'build' | 'finalize' | 'fabricate' | 'move' | 'load' | 'unload' | 'other';
  /** The quest-relevant type of the order: what is attacked, built or fabricated. */
  targetTypeId: number | null;
  /** The engine has reported the unit busy since the order - it arrived. */
  arrived: boolean;
}

export interface Point {
  x: number;
  y: number;
}

/** A resource field the harvest tip can point at. An id only when it is rendered, for the prompt. */
export interface ResourcePoint extends Point {
  id: number | null;
}

export interface DecisionInput {
  quest: TipQuestSpec;
  /** Own selectable items and the enemies of the quest's type, over the whole planet. */
  items: TipItemState[];
  selectedIds: ReadonlySet<number>;
  /** Orders this client sent, by unit id, that are still under way or being carried out. */
  orders: ReadonlyMap<number, PendingOrder>;
  /**
   * Whether a ground point is on the player's screen and could carry the prompt named by the
   * text - a long one needs more room beside an item near an edge than a short one.
   */
  onScreen: (x: number, y: number, text?: string) => boolean;
  viewCenter: Point;
  placer: { active: boolean, typeId: number | null };
  /** Why the cockpit button for this type cannot be pointed at; null when it can. */
  buttonBlock: (itemTypeId: number) => string | null;
  /** Item types that can fabricate the given type. */
  factoriesFor: (itemTypeId: number) => number[];
  /** HARVEST: fields on screen, and the nearest field anywhere as the worker knows it. */
  resourcesOnScreen: ResourcePoint[];
  nearestResource: (from: Point) => Point | null;
  /** BUILD with a region (386): where the arrow goes and whether the region is in view. */
  region: { point: Point | null, inView: boolean } | null;
  /**
   * LOAD/SAIL/UNLOAD: how far a point is from the quest's region, 0 inside it; null without a
   * region. The region's point above is taken from the loaded container, or the unit when none is.
   */
  regionDistance: (point: Point) => number | null;
  /** Container types that can carry the given type: the ones the player owns or can fabricate. */
  containerTypesFor: (itemTypeId: number) => number[];
  /** How far from a container of this type a unit can be put down. */
  containerRange: (itemTypeId: number) => number;
  /** Why the Unload button cannot be pointed at; null when it can. */
  unloadBlock: () => string | null;
  /** Why the sell button cannot be pointed at; null when it can. */
  sellBlock: () => string | null;
  /** How many of the type the player's level allows. */
  itemLimit: (itemTypeId: number) => number;
  /** Whether items of the type harvest Razarion. */
  isHarvester: (itemTypeId: number) => boolean;
  /** Whether the base has the Razarion for one of the type - asked before the actor is selected. */
  canAfford: (itemTypeId: number) => boolean;
  /** The enemy the attack prompt stood on last time, kept while it lives so the prompt does not jump. */
  markedTargetId: number | null;
}

export type Guidance =
  { kind: 'quiet' } |
  { kind: 'prompt', itemId: number, resource: boolean, text: string } |
  { kind: 'arrow', x: number, y: number } |
  { kind: 'button', itemTypeId: number } |
  { kind: 'placeMarker' } |
  { kind: 'unload' } |
  /** The hint on the sell button; the building it sells is where the minimap marks. */
  { kind: 'sell', x: number, y: number } |
  { kind: 'group', arrow: Point | null };

export interface Decision {
  guidance: Guidance;
  /** For the stall watchdog: the task name and reason of today's tasks, so tip_stall stays comparable. */
  taskName: string;
  reason: string;
}

export const PROMPT = {
  SELECT: 'Click to select',
  ATTACK: 'Click to attack',
  HARVEST: 'Click to harvest',
  CONTINUE_BUILDING: 'Click to continue building',
  LOAD: 'Click to load'
} as const;

export function decide(input: DecisionInput): Decision {
  switch (input.quest.tip) {
    case 'ATTACK':
      return decideAttack(input);
    case 'BUILD':
      return decideBuild(input);
    case 'FABRICATE':
      return input.quest.buildTargets
        ? decideFabricateTargets(input)
        : decideFabricate(input, input.quest.actorTypeId, input.quest.targetTypeId!);
    case 'HARVEST':
      return decideHarvest(input);
    case 'LOAD':
    case 'SAIL':
    case 'UNLOAD':
      return decideTransport(input);
    case 'SELL':
      return decideSell(input);
  }
}

// --- ATTACK -----------------------------------------------------------------------------------------

function decideAttack(input: DecisionInput): Decision {
  const quest = input.quest;
  const attackers = own(input, quest.actorTypeId);
  if (attackers.length === 0) {
    // The last one died on the way (Q1): build another, then attack again.
    const factory = ownOfTypes(input, input.factoriesFor(quest.actorTypeId));
    if (factory.length > 0) {
      return decideFabricate(input, factory[0].itemTypeId, quest.actorTypeId);
    }
    return quiet(TipTaskName.SELECT, TipStallReason.ACTOR_NOT_FOUND);
  }
  if (attackers.some(attacker => worksOn(input, attacker, ['attack'], quest.targetTypeId))) {
    return quiet(TipTaskName.IDLE_ITEM, TipStallReason.AWAIT_IDLE);
  }
  const targets = input.items.filter(item => !item.own
    && (quest.targetTypeId === null || item.itemTypeId === quest.targetTypeId));
  if (targets.length === 0) {
    return quiet(TipTaskName.SEND_ATTACK_COMMAND, TipStallReason.NO_ENEMY); // Q7
  }
  const selected = attackers.filter(attacker => input.selectedIds.has(attacker.id));
  if (selected.length === 0) {
    return selectStep(input, attackers);
  }
  if (quest.group && attackers.length >= 2 && selected.length < 2) {
    const onScreen = attackers.some(attacker => input.onScreen(attacker.x, attacker.y));
    return {
      guidance: {kind: 'group', arrow: onScreen ? null : nearestTo(attackers, input.viewCenter)},
      taskName: TipTaskName.SELECT_GROUP,
      reason: TipStallReason.AWAIT_GROUP
    };
  }
  const marked = targets.find(target => target.id === input.markedTargetId);
  const target = marked ?? nearestTo(targets, centroid(selected));
  return pointAt(input, target, false, PROMPT.ATTACK, TipTaskName.SEND_ATTACK_COMMAND,
    TipStallReason.AWAIT_ATTACK_CLICK, TipStallReason.ENEMY_OUT_OF_VIEW);
}

// --- BUILD ------------------------------------------------------------------------------------------

function decideBuild(input: DecisionInput): Decision {
  const quest = input.quest;
  const missing = missingBuildTypes(input);
  if (missing.length === 0) {
    return quiet(TipTaskName.SEND_BUILD_COMMAND, TipStallReason.AWAIT_BUILD_FINALIZE); // the quest is passing
  }
  // One the level's limit still allows first; the others wait for their old building to be sold.
  const buildingTypeId = missing.find(typeId => oldBuildings(input, typeId).length === 0) ?? missing[0];
  const builders = own(input, quest.actorTypeId);
  if (builders.length === 0) {
    return quiet(TipTaskName.SELECT, TipStallReason.ACTOR_NOT_FOUND); // prepared against: graceful
  }
  const site = ownOfTypes(input, missing).find(item => item.buildup < 1 && countsForQuest(input, item));
  if (site) {
    if (builders.some(builder => missing.some(typeId => worksOn(input, builder, ['build', 'finalize'], typeId)))) {
      return quiet(TipTaskName.SEND_BUILD_COMMAND, TipStallReason.AWAIT_BUILD_FINALIZE);
    }
    if (!builders.some(builder => input.selectedIds.has(builder.id))) {
      return selectStep(input, builders);
    }
    // At once, not after ten seconds: the builder is not coming back by itself (BLD-09).
    return pointAt(input, site, false, PROMPT.CONTINUE_BUILDING, TipTaskName.SEND_BUILD_COMMAND,
      TipStallReason.BUILD_NOT_PROGRESSING, TipStallReason.TARGET_OUT_OF_VIEW);
  }
  if (builders.some(builder => missing.some(typeId => worksOn(input, builder, ['build'], typeId)))) {
    return quiet(TipTaskName.SEND_BUILD_COMMAND, TipStallReason.AWAIT_BUILD_SITE);
  }
  // A placer of unknown type counts as ours: the placer starts before the cockpit reports the click.
  if (input.placer.active && (input.placer.typeId === null || missing.includes(input.placer.typeId))) {
    const region = input.region;
    if (region && region.inView) {
      return {guidance: {kind: 'placeMarker'}, taskName: TipTaskName.SEND_BUILD_COMMAND, reason: TipStallReason.AWAIT_PLACEMENT};
    }
    if (region && region.point) {
      return {
        guidance: {kind: 'arrow', x: region.point.x, y: region.point.y},
        taskName: TipTaskName.SEND_BUILD_COMMAND,
        reason: TipStallReason.TARGET_OUT_OF_VIEW
      };
    }
    // The placer itself is the guidance.
    return quiet(TipTaskName.SEND_BUILD_COMMAND, TipStallReason.AWAIT_PLACEMENT);
  }
  const old = oldBuildings(input, buildingTypeId);
  if (old.length > 0) {
    return sellStep(input, old);
  }
  // Before the builder: the harvester the money step selects would otherwise send the tip back to
  // "select the builder", and the builder back to the harvester.
  if (!input.canAfford(buildingTypeId)) {
    return moneyStep(input, TipStallReason.NO_MONEY);
  }
  if (!builders.some(builder => input.selectedIds.has(builder.id))) {
    return selectStep(input, builders);
  }
  return buttonStep(input, buildingTypeId, TipTaskName.START_BUILD_PLACER, TipStallReason.AWAIT_PLACER);
}

/**
 * The buildings the quest still wants, the next one first. A quest that counts only what is built
 * after it started (SYNC_ITEM_CREATED) cannot be read off the base: its one type stays missing
 * until the quest passes.
 */
function missingBuildTypes(input: DecisionInput): number[] {
  const targets = input.quest.buildTargets;
  if (!targets) {
    return [input.quest.targetTypeId!];
  }
  return targets
    .filter(target => own(input, target.typeId)
      .filter(item => item.buildup >= 1 && countsForQuest(input, item)).length < target.count)
    .map(target => target.typeId);
}

/** SYNC_ITEM_POSITION counts what stands in the quest's region; without a region, all of the base. */
function countsForQuest(input: DecisionInput, item: TipItemState): boolean {
  const distance = input.regionDistance(item);
  return distance === null || distance <= 0;
}

/**
 * Items of the type outside the quest's region that keep the level's limit full, so that a new one
 * cannot be made: 396 wants radar and powerplant on the new island, 400 a harvester and six vipers,
 * level 9 allows one, one and six, and the old ones are still on the island the player has left.
 */
function oldBuildings(input: DecisionInput, typeId: number): TipItemState[] {
  if (!input.quest.buildTargets) {
    return [];
  }
  const all = own(input, typeId);
  if (all.length < input.itemLimit(typeId)) {
    return [];
  }
  return all.filter(item => !countsForQuest(input, item));
}

// --- FABRICATE --------------------------------------------------------------------------------------

function decideFabricate(input: DecisionInput, factoryTypeId: number, unitTypeId: number): Decision {
  const factories = own(input, factoryTypeId).filter(item => item.buildup >= 1);
  if (factories.length === 0) {
    return quiet(TipTaskName.SELECT, TipStallReason.ACTOR_NOT_FOUND); // prepared against: graceful
  }
  // The queue comes from the tick infos: on screen or not makes no difference (FAB-07).
  if (factories.some(factory => factory.factoryBuildQueue.includes(unitTypeId)
    || worksOn(input, factory, ['fabricate'], unitTypeId))) {
    return quiet(TipTaskName.IDLE_ITEM, TipStallReason.AWAIT_IDLE);
  }
  if (!input.canAfford(unitTypeId)) {
    return moneyStep(input, TipStallReason.NO_MONEY);
  }
  if (!factories.some(factory => input.selectedIds.has(factory.id))) {
    return selectStep(input, factories);
  }
  return buttonStep(input, unitTypeId, TipTaskName.SEND_FABRICATE_COMMAND, TipStallReason.AWAIT_FABRICATE_CLICK);
}

/**
 * FABRICATE for a quest that counts what stands in its region (400: a harvester and six vipers on
 * the Phase 2 island). The same traps as the rebuild (396): the level allows one harvester and six
 * vipers, the old ones on the noob island fill that limit, and a land unit cannot get across - it
 * has to be sold before the new one can be made.
 */
function decideFabricateTargets(input: DecisionInput): Decision {
  const factories = own(input, input.quest.actorTypeId).filter(item => item.buildup >= 1);
  if (factories.length === 0) {
    return quiet(TipTaskName.SELECT, TipStallReason.ACTOR_NOT_FOUND); // prepared against: graceful
  }
  const missing = missingBuildTypes(input);
  if (missing.length === 0) {
    return quiet(TipTaskName.IDLE_ITEM, TipStallReason.AWAIT_IDLE); // the quest is passing
  }
  // A queue with any of them in it is the player's choice at work, whatever the tip would have asked.
  if (factories.some(factory => factory.factoryBuildQueue.some(typeId => missing.includes(typeId))
    || missing.some(typeId => worksOn(input, factory, ['fabricate'], typeId)))) {
    return quiet(TipTaskName.IDLE_ITEM, TipStallReason.AWAIT_IDLE);
  }
  const unitTypeId = missing.find(typeId => oldBuildings(input, typeId).length === 0) ?? missing[0];
  const old = oldBuildings(input, unitTypeId);
  if (old.length > 0) {
    return sellStep(input, old);
  }
  return decideFabricate(input, input.quest.actorTypeId, unitTypeId);
}

// --- HARVEST ----------------------------------------------------------------------------------------

function decideHarvest(input: DecisionInput): Decision {
  const harvesters = own(input, input.quest.actorTypeId);
  if (harvesters.length === 0) {
    return quiet(TipTaskName.SELECT, TipStallReason.ACTOR_NOT_FOUND); // prepared against: graceful
  }
  if (harvesters.some(harvester => worksOn(input, harvester, ['harvest'], null))) {
    return quiet(TipTaskName.IDLE_ITEM, TipStallReason.AWAIT_IDLE);
  }
  return harvestStep(input, harvesters);
}

/** Select a harvester, then the nearest field: on screen a prompt on it, off screen the arrow. */
function harvestStep(input: DecisionInput, harvesters: TipItemState[]): Decision {
  const selected = harvesters.filter(harvester => input.selectedIds.has(harvester.id));
  const from = selected.length > 0 ? centroid(selected) : centroid(harvesters);
  const visible = input.resourcesOnScreen.length > 0 ? nearestTo(input.resourcesOnScreen, from) : null;
  const field = visible ?? input.nearestResource(from);
  if (!field) {
    return quiet(TipTaskName.SEND_HARVEST_COMMAND, TipStallReason.NO_RESOURCE); // Q7
  }
  if (selected.length === 0) {
    return selectStep(input, harvesters);
  }
  if (visible && visible.id !== null) {
    return {
      guidance: {kind: 'prompt', itemId: visible.id, resource: true, text: PROMPT.HARVEST},
      taskName: TipTaskName.SEND_HARVEST_COMMAND,
      reason: TipStallReason.AWAIT_HARVEST_CLICK
    };
  }
  return {
    guidance: {kind: 'arrow', x: field.x, y: field.y},
    taskName: TipTaskName.SEND_HARVEST_COMMAND,
    reason: TipStallReason.RESOURCE_OUT_OF_VIEW
  };
}

// --- LOAD / SAIL / UNLOAD -----------------------------------------------------------------------------

/**
 * One function for the three quests, because each of them can find the world in the state of an
 * earlier one: a player on the sailing quest who put the unit down on the wrong shore has to load it
 * again, and a transporter sunk by the bot's hydras has to be built again before anything else.
 */
function decideTransport(input: DecisionInput): Decision {
  const quest = input.quest;
  const cargoTypeId = quest.actorTypeId;
  const containerTypes = input.containerTypesFor(cargoTypeId);
  const containers = ownOfTypes(input, containerTypes).filter(container => container.buildup >= 1);
  const loaded = containers.filter(container => (container.cargo ?? []).includes(cargoTypeId));
  if (loaded.length === 0) {
    return decideLoad(input, containers, containerTypes);
  }
  if (quest.tip === 'LOAD') {
    return quiet(TipTaskName.SEND_LOAD_COMMAND, TipStallReason.AWAIT_IDLE); // passes with the next tick
  }
  const selectedShip = loaded.find(container => input.selectedIds.has(container.id));
  const ship = selectedShip ?? nearestTo(loaded, input.viewCenter);
  const distance = input.regionDistance(ship);
  if (distance === null) {
    return quiet(TipTaskName.SEND_MOVE_COMMAND, TipStallReason.WAITING); // no region: nothing to point at
  }
  if (quest.tip === 'SAIL') {
    if (distance <= 0) {
      return quiet(TipTaskName.SEND_MOVE_COMMAND, TipStallReason.AWAIT_IDLE); // passes with the next tick
    }
    return sailStep(input, ship, !!selectedShip);
  }
  // UNLOAD. The placer takes only spots in the ship's reach (JsItemCockpitBridge.requestUnload), so
  // with the region out of reach the ship has to go closer first. Its hull is the margin.
  if (distance > input.containerRange(ship.itemTypeId) - 2) {
    return sailStep(input, ship, !!selectedShip);
  }
  const order = input.orders.get(ship.id);
  if (order && order.kind === 'unload') {
    return quiet(TipTaskName.SEND_UNLOAD_COMMAND, TipStallReason.AWAIT_UNLOAD);
  }
  if (!selectedShip) {
    return selectStep(input, [ship]);
  }
  if (input.placer.active) {
    const region = input.region;
    if (region && !region.inView && region.point) {
      return {
        guidance: {kind: 'arrow', x: region.point.x, y: region.point.y},
        taskName: TipTaskName.SEND_UNLOAD_COMMAND,
        reason: TipStallReason.TARGET_OUT_OF_VIEW
      };
    }
    return {guidance: {kind: 'placeMarker'}, taskName: TipTaskName.SEND_UNLOAD_COMMAND, reason: TipStallReason.AWAIT_PLACEMENT};
  }
  const block = input.unloadBlock();
  if (block === null || block === TipStallReason.ITEM_PANEL_CLOSED) {
    return {guidance: {kind: 'unload'}, taskName: TipTaskName.START_UNLOAD_PLACER, reason: block ?? TipStallReason.AWAIT_UNLOAD_CLICK};
  }
  return quiet(TipTaskName.START_UNLOAD_PLACER, block);
}

/** Nothing loaded: get a container, pick the unit, tap the container. */
function decideLoad(input: DecisionInput, containers: TipItemState[], containerTypes: number[]): Decision {
  const quest = input.quest;
  const units = own(input, quest.actorTypeId);
  if (units.length === 0) {
    return quiet(TipTaskName.SELECT, TipStallReason.ACTOR_NOT_FOUND); // prepared against: graceful
  }
  if (quest.tip === 'UNLOAD' && units.some(unit => input.regionDistance(unit) === 0)) {
    return quiet(TipTaskName.SEND_UNLOAD_COMMAND, TipStallReason.AWAIT_IDLE); // passes with the next tick
  }
  if (containers.length === 0) {
    // Sunk on the way, or never built: build one, as the attack tip does with a dead attacker.
    for (const containerType of containerTypes) {
      const factories = ownOfTypes(input, input.factoriesFor(containerType));
      if (factories.length > 0) {
        return decideFabricate(input, factories[0].itemTypeId, containerType);
      }
    }
    return quiet(TipTaskName.SEND_LOAD_COMMAND, TipStallReason.ACTOR_NOT_FOUND);
  }
  if (units.some(unit => worksOn(input, unit, ['load'], null))) {
    return quiet(TipTaskName.SEND_LOAD_COMMAND, TipStallReason.AWAIT_LOAD);
  }
  const selected = units.filter(unit => input.selectedIds.has(unit.id));
  if (selected.length === 0) {
    return selectStep(input, units);
  }
  const container = nearestTo(containers, centroid(selected));
  return pointAt(input, container, false, PROMPT.LOAD, TipTaskName.SEND_LOAD_COMMAND,
    TipStallReason.AWAIT_LOAD_CLICK, TipStallReason.CONTAINER_OUT_OF_VIEW);
}

/** The loaded container, selected and told where to go: the region when it is in view, else the way there. */
function sailStep(input: DecisionInput, ship: TipItemState, selected: boolean): Decision {
  if (worksOn(input, ship, ['move'], null)) {
    return quiet(TipTaskName.SEND_MOVE_COMMAND, TipStallReason.AWAIT_ARRIVAL);
  }
  if (!selected) {
    return selectStep(input, [ship]);
  }
  const region = input.region;
  if (region && region.inView) {
    return {guidance: {kind: 'placeMarker'}, taskName: TipTaskName.SEND_MOVE_COMMAND, reason: TipStallReason.AWAIT_MOVE_CLICK};
  }
  if (region && region.point) {
    return {
      guidance: {kind: 'arrow', x: region.point.x, y: region.point.y},
      taskName: TipTaskName.SEND_MOVE_COMMAND,
      reason: TipStallReason.TARGET_OUT_OF_VIEW
    };
  }
  return quiet(TipTaskName.SEND_MOVE_COMMAND, TipStallReason.WAITING);
}

// --- SELL -----------------------------------------------------------------------------------------

/**
 * Selling a building to move the base (393 the factory, 401 the dockyard). The building stands on
 * the island the player has just left, about 300 units behind him - the select step's arrow and the
 * minimap marker lead back to it. Then the hint on the sell button, which wants two taps.
 */
function decideSell(input: DecisionInput): Decision {
  const buildings = own(input, input.quest.actorTypeId).filter(item => item.buildup >= 1);
  if (buildings.length === 0) {
    return quiet(TipTaskName.SEND_SELL_COMMAND, TipStallReason.ACTOR_NOT_FOUND); // sold: passes with the next tick
  }
  return sellStep(input, buildings);
}

/** Select one of the buildings, then the sell button, which wants two taps. */
function sellStep(input: DecisionInput, buildings: TipItemState[]): Decision {
  const selected = buildings.find(building => input.selectedIds.has(building.id));
  if (!selected) {
    return selectStep(input, buildings);
  }
  const block = input.sellBlock();
  if (block === null || block === TipStallReason.ITEM_PANEL_CLOSED) {
    return {
      guidance: {kind: 'sell', x: selected.x, y: selected.y},
      taskName: TipTaskName.SEND_SELL_COMMAND,
      reason: block ?? TipStallReason.AWAIT_SELL_CLICK
    };
  }
  return quiet(TipTaskName.SEND_SELL_COMMAND, block);
}

// --- Shared steps -----------------------------------------------------------------------------------

/**
 * Nothing of the kind is selected: point at the one nearest to the middle of the screen (Q6). The
 * callers only get here when none of the candidates is selected - by id, off screen included.
 */
function selectStep(input: DecisionInput, candidates: TipItemState[]): Decision {
  const nearest = nearestTo(candidates, input.viewCenter);
  return pointAt(input, nearest, false, PROMPT.SELECT, TipTaskName.SELECT,
    TipStallReason.AWAIT_SELECTION, TipStallReason.ACTOR_OUT_OF_VIEW);
}

function buttonStep(input: DecisionInput, itemTypeId: number, taskName: string, waitingReason: string): Decision {
  const block = input.buttonBlock(itemTypeId);
  if (block === null) {
    return {guidance: {kind: 'button', itemTypeId}, taskName, reason: waitingReason};
  }
  // The compact layout's closed panel is still something to point at; the cockpit handles it.
  if (block === TipStallReason.ITEM_PANEL_CLOSED) {
    return {guidance: {kind: 'button', itemTypeId}, taskName, reason: block};
  }
  if (block === TipStallReason.NO_MONEY) {
    return moneyStep(input, block);
  }
  // Greyed out by a limit or the house space: nothing to point at, nothing wrong shown.
  return quiet(taskName, block);
}

/**
 * Too little Razarion for the button the quest needs: send a harvester out. The quests pay for the
 * way (Q3), but a player who lost the builder and bought a new one, or lost harvester and army in a
 * fight, stood in front of a greyed button with no word why (PROD tip_stall, 2026-09-25). While a
 * harvester is at work the tip waits; the button comes back once the money is there.
 */
function moneyStep(input: DecisionInput, reason: string): Decision {
  const harvesters = input.items.filter(item => item.own && item.buildup >= 1 && input.isHarvester(item.itemTypeId));
  if (harvesters.length === 0) {
    return quiet(TipTaskName.SEND_HARVEST_COMMAND, reason);
  }
  if (harvesters.some(harvester => worksOn(input, harvester, ['harvest'], null))) {
    return quiet(TipTaskName.IDLE_ITEM, reason);
  }
  return harvestStep(input, harvesters);
}

/** On screen: a prompt on it. Off screen: the arrow. Decided once, here, for every step. */
function pointAt(input: DecisionInput, item: Point & { id: number }, resource: boolean, text: string,
                 taskName: string, onScreenReason: string, offScreenReason: string): Decision {
  if (input.onScreen(item.x, item.y, text)) {
    return {guidance: {kind: 'prompt', itemId: item.id, resource, text}, taskName, reason: onScreenReason};
  }
  return {guidance: {kind: 'arrow', x: item.x, y: item.y}, taskName, reason: offScreenReason};
}

function quiet(taskName: string, reason: string): Decision {
  return {guidance: {kind: 'quiet'}, taskName, reason};
}

/**
 * Whether the unit is doing something for the quest: an order of that kind was sent and has not
 * been finished yet (under way or carried out), or the engine reports it busy with an order this
 * client never saw - one given before the quest started, which gets the benefit of the doubt.
 */
function worksOn(input: DecisionInput, item: TipItemState, kinds: PendingOrder['kind'][], targetTypeId: number | null): boolean {
  const order = input.orders.get(item.id);
  if (order) {
    return kinds.includes(order.kind) && (targetTypeId === null || order.targetTypeId === null
      || order.targetTypeId === targetTypeId);
  }
  return !item.idle;
}

function own(input: DecisionInput, itemTypeId: number): TipItemState[] {
  return input.items.filter(item => item.own && item.itemTypeId === itemTypeId);
}

function ownOfTypes(input: DecisionInput, itemTypeIds: number[]): TipItemState[] {
  return input.items.filter(item => item.own && itemTypeIds.includes(item.itemTypeId));
}

function nearestTo<T extends Point>(candidates: T[], from: Point): T {
  let best = candidates[0];
  let bestDistance = Infinity;
  for (const candidate of candidates) {
    const dx = candidate.x - from.x;
    const dy = candidate.y - from.y;
    const distance = dx * dx + dy * dy;
    if (distance < bestDistance) {
      best = candidate;
      bestDistance = distance;
    }
  }
  return best;
}

function centroid(points: Point[]): Point {
  const x = points.reduce((sum, point) => sum + point.x, 0) / points.length;
  const y = points.reduce((sum, point) => sum + point.y, 0) / points.length;
  return {x, y};
}
