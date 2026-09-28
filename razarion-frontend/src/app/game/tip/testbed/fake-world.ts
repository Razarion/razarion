import {Vector3} from '@babylonjs/core/Maths/math.vector';
import {Diplomacy} from '../../../gwtangular/GwtAngularFacade';
import {ViewField, ViewFieldListener} from '../../renderer/view-field';
import {BaseItemPlacerPresenterEvent} from '../../renderer/base-item-placer-presenter.impl';
import {PROMPT_CLEARANCE_FRACTION} from '../../renderer/prompt-geometry';
import {TipStallReason} from '../tip-stall';
import {fakeBaseItemType, FakeItemTypeSpec, itemTypeSpec} from './fake-item-types';

/**
 * A small game world for the tip test bed, and the renderer, worker and cockpit the tip code sees
 * of it.
 *
 * The point is fidelity to the rules the tips have failed on (catalog section 2, W1-W12), not to
 * the game engine. Anything the tips cannot observe is simplified: units drive in straight lines,
 * money is withdrawn up front, fights are damage over time.
 */

export type Owner = 'own' | 'bot';

export type Order =
  { kind: 'move', x: number, y: number } |
  { kind: 'attack', targetId: number } |
  { kind: 'harvest', resourceId: number } |
  { kind: 'build', typeId: number, x: number, y: number } |
  { kind: 'finalize', siteId: number } |
  { kind: 'load', containerId: number };

export interface Unit {
  id: number;
  spec: FakeItemTypeSpec;
  owner: Owner;
  x: number;
  y: number;
  health: number;
  /** 1 when finished; construction sites and nothing else are below. */
  buildup: number;
  order: Order | null;
  reloadLeft: number;
  /** Factory: types waiting to be fabricated, the first one in progress. */
  queue: number[];
  queueProgress: number;
  /** Builder: the site its build order created, so it keeps building that one. */
  siteId: number | null;
  /** The container it sits in. A contained unit is nowhere: not rendered, not in the tip states. */
  containedIn: number | null;
  /** Container: the ids of the units inside. */
  cargo: number[];
}

export interface Resource {
  id: number;
  x: number;
  y: number;
  amount: number;
}

export interface Respawn {
  typeId: number;
  x: number;
  y: number;
  delayMillis: number;
}

export type QuestCondition =
  { kind: 'created', typeId: number, count: number, includeExisting: boolean } |
  { kind: 'killed', typeId: number | null, count: number } |
  { kind: 'harvested', amount: number } |
  { kind: 'createdIn', typeId: number, region: { x: number, y: number }[] } |
  /** SYNC_ITEM_LOADED */
  { kind: 'loaded', typeId: number } |
  /** LOADED_CONTAINER_POSITION */
  { kind: 'loadedIn', containerTypeId: number, region: { x: number, y: number }[] } |
  /** SYNC_ITEM_POSITION: contained units are nowhere and never count. */
  { kind: 'in', typeId: number, region: { x: number, y: number }[] } |
  /** SELL */
  { kind: 'sold', typeId: number, count: number } |
  /** SYNC_ITEM_POSITION with typed counts: the finished items in the region (the base without one), whenever built. */
  { kind: 'owns', types: { typeId: number, count: number }[], region?: { x: number, y: number }[] };

/** Half widths and depths of the visible trapezoid around the camera, in ground units. */
const VIEW = {near: 12, far: 18, nearHalfWidth: 14, farHalfWidth: 26};

/**
 * The camera of the live client, so a ground point can be turned into a height in the picture.
 *
 * Being inside the view field is not the same as being seen, and where exactly a point lands
 * cannot be guessed from the depth alone: the camera looks down at a fixed angle, which crowds
 * the far half of the ground into the top of the picture. Mapping the trapezoid linearly puts a
 * unit a few steps in front of the player down at 77 % of the height, where the real one is at
 * 53 % - the difference between "behind the HUD" and "perfectly visible".
 *
 * `new FreeCamera(position(0, 30, -35))` with `setTarget(0, 0, 0)` and Babylon's default vertical
 * field of view, at the standard zoom (`cameraTerrainDistance = 30`, which is where the players
 * sit: zoomP05 = zoomP50 = zoomP95 = 30.0 in the render telemetry). The ground it sees runs from
 * 15 to 94 units in front of the camera; this world's trapezoid is stretched onto that range.
 */
const CAMERA = {
  height: 30,
  pitch: Math.atan2(30, 35),
  halfFov: 0.4,
  nearGround: 15,
  farGround: 94
};

/**
 * What the picture costs the prompt, as fractions of its height.
 *
 * The HUD number is measured on the live client (a phone in portrait, backbuffer about 369x683,
 * bottom row about 180 px). The clearance comes from the prompt itself, so the bed cannot drift
 * away from what the renderer does.
 */
const SCREEN = {hudFraction: 0.26, clearanceFraction: PROMPT_CLEARANCE_FRACTION};

export class World {
  private nextId = 1;
  readonly units = new Map<number, Unit>();
  readonly resources = new Map<number, Resource>();
  private readonly respawns = new Map<number, Respawn>();
  private pendingCommands: { dueAt: number, unitIds: number[], apply: () => void }[] = [];
  private pendingRespawns: { dueAt: number, respawn: Respawn }[] = [];
  time = 0;
  razarion = 100;
  maxRazarion = 500;
  /** How long a command takes to reach the unit (W8). Raise it to model the Meta webview. */
  commandLatencyMillis = 200;
  /** Item limits of the current level; a type not listed is unlimited. */
  itemLimits = new Map<number, number>();
  cameraX = 0;
  cameraY = 0;
  /** Kills and harvest since the quest was activated, for its condition. */
  private questCondition: QuestCondition | null = null;
  private questKills: number[] = [];
  private questSold: number[] = [];
  private questHarvested = 0;
  private questExistingIds = new Set<number>();
  onQuestPassed: (() => void) | null = null;

  readonly renderer: FakeRenderer;

  constructor() {
    this.renderer = new FakeRenderer(this);
  }

  // --- Setting the scene ----------------------------------------------------------------------

  addUnit(typeId: number, owner: Owner, x: number, y: number, buildup = 1): Unit {
    const spec = itemTypeSpec(typeId);
    const unit: Unit = {
      id: this.nextId++, spec, owner, x, y, health: spec.health, buildup, order: null,
      reloadLeft: 0, queue: [], queueProgress: 0, siteId: null, containedIn: null, cargo: []
    };
    this.units.set(unit.id, unit);
    return unit;
  }

  /** A bot unit that comes back, with a new id, this long after it died (W11). */
  addBotUnit(typeId: number, x: number, y: number, respawnDelayMillis: number | null = null): Unit {
    const unit = this.addUnit(typeId, 'bot', x, y);
    if (respawnDelayMillis !== null) {
      this.respawns.set(unit.id, {typeId, x, y, delayMillis: respawnDelayMillis});
    }
    return unit;
  }

  addResource(x: number, y: number, amount = 50): Resource {
    const resource: Resource = {id: this.nextId++, x, y, amount};
    this.resources.set(resource.id, resource);
    return resource;
  }

  removeResource(resourceId: number): void {
    this.resources.delete(resourceId);
  }

  kill(unitId: number): void {
    const unit = this.units.get(unitId);
    if (unit) {
      this.removeUnit(unit, null);
    }
  }

  setQuestCondition(condition: QuestCondition | null): void {
    this.questCondition = condition;
    this.questKills = [];
    this.questSold = [];
    this.questHarvested = 0;
    this.questExistingIds = new Set([...this.units.values()].filter(u => u.owner === 'own').map(u => u.id));
  }

  // --- Camera and view field --------------------------------------------------------------------

  moveCamera(x: number, y: number): void {
    this.cameraX = x;
    this.cameraY = y;
    this.renderer.onCameraMoved();
  }

  viewField(): ViewField {
    const cx = this.cameraX;
    const cy = this.cameraY;
    return new ViewField(
      new Vector3(cx - VIEW.nearHalfWidth, 0, cy - VIEW.near),
      new Vector3(cx + VIEW.nearHalfWidth, 0, cy - VIEW.near),
      new Vector3(cx + VIEW.farHalfWidth, 0, cy + VIEW.far),
      new Vector3(cx - VIEW.farHalfWidth, 0, cy + VIEW.far),
      new Vector3(cx, 0, cy));
  }

  /**
   * Whether a circle touches the axis-aligned box around the view field - the test that decides
   * whether an item gets an instance at all (W1). Larger than the visible trapezoid.
   */
  inRenderBox(x: number, y: number, radius: number): boolean {
    const minX = this.cameraX - VIEW.farHalfWidth;
    const maxX = this.cameraX + VIEW.farHalfWidth;
    const minY = this.cameraY - VIEW.near;
    const maxY = this.cameraY + VIEW.far;
    const closestX = Math.max(minX, Math.min(x, maxX));
    const closestY = Math.max(minY, Math.min(y, maxY));
    const dx = closestX - x;
    const dy = closestY - y;
    return dx * dx + dy * dy < radius * radius;
  }

  /** Whether the point is on the player's screen - the visible trapezoid, not the render box. */
  onScreen(x: number, y: number): boolean {
    return this.viewField().contains({getX: () => x, getY: () => y} as any);
  }

  /**
   * Where the point sits in the picture, 0 at the top edge and 1 at the bottom. See {@link CAMERA}
   * for why this is not simply the depth.
   */
  screenFraction(y: number): number {
    const depth = (y - (this.cameraY - VIEW.near)) / (VIEW.near + VIEW.far);
    const distance = CAMERA.nearGround + depth * (CAMERA.farGround - CAMERA.nearGround);
    if (distance <= 0) {
      return 1;
    }
    const above = CAMERA.pitch - Math.atan(CAMERA.height / distance);
    return (1 - Math.tan(above) / Math.tan(CAMERA.halfFov)) / 2;
  }

  /** The camera row that puts this ground row at the given height in the picture - the inverse. */
  cameraYForScreenFraction(y: number, fraction: number): number {
    const above = Math.atan((1 - 2 * fraction) * Math.tan(CAMERA.halfFov));
    const distance = CAMERA.height / Math.tan(CAMERA.pitch - above);
    const depth = (distance - CAMERA.nearGround) / (CAMERA.farGround - CAMERA.nearGround);
    return y + VIEW.near - depth * (VIEW.near + VIEW.far);
  }

  /**
   * Whether a prompt anchored here would actually be read: on screen, not behind the bottom HUD,
   * and with room for the label on one side of the anchor or the other. See {@link SCREEN}.
   */
  promptReadable(x: number, y: number): boolean {
    if (!this.onScreen(x, y)) {
      return false;
    }
    const fraction = this.screenFraction(y);
    const floor = 1 - SCREEN.hudFraction;
    if (fraction > floor) {
      return false; // behind the HUD
    }
    return fraction >= SCREEN.clearanceFraction
      || fraction + SCREEN.clearanceFraction <= floor;
  }

  // --- Commands, as the game command service receives them ---------------------------------------

  command(unitIds: number[], order: Order): void {
    this.pendingCommands.push({
      dueAt: this.time + this.commandLatencyMillis,
      unitIds,
      apply: () => unitIds.forEach(id => {
        const unit = this.units.get(id);
        if (unit) {
          unit.order = order;
          if (order.kind !== 'build') {
            unit.siteId = null;
          }
        }
      })
    });
  }

  fabricate(factoryIds: number[], typeId: number): void {
    const price = itemTypeSpec(typeId).price;
    this.pendingCommands.push({
      dueAt: this.time + this.commandLatencyMillis,
      unitIds: factoryIds,
      apply: () => factoryIds.forEach(id => {
        const factory = this.units.get(id);
        if (factory && this.razarion >= price && !this.limitReached(typeId, 1)) {
          this.razarion -= price;
          factory.queue.push(typeId);
        }
      })
    });
  }

  /**
   * The unload order, as SyncItemContainer carries it out: everything comes out at the spot when the
   * spot is in the container's reach, and nothing happens at all when it is not - no answer either.
   */
  unload(containerId: number, x: number, y: number): void {
    this.pendingCommands.push({
      dueAt: this.time + this.commandLatencyMillis,
      unitIds: [containerId],
      apply: () => {
        const container = this.units.get(containerId);
        if (!container || !container.spec.container || container.cargo.length === 0
          || Math.hypot(x - container.x, y - container.y) > container.spec.container.range) {
          return;
        }
        container.cargo.forEach((unitId, index) => {
          const unit = this.units.get(unitId);
          if (unit) {
            unit.containedIn = null;
            unit.x = x + index * 1.5;
            unit.y = y;
          }
        });
        container.cargo = [];
      }
    });
  }

  /** ItemCockpitBridge.sellItems: the items are gone, the quest counts them. */
  sell(unitIds: number[]): void {
    this.pendingCommands.push({
      dueAt: this.time + this.commandLatencyMillis,
      unitIds,
      apply: () => unitIds.forEach(id => {
        const unit = this.units.get(id);
        if (unit && unit.owner === 'own') {
          this.units.delete(id);
          this.questSold.push(unit.spec.id);
        }
      })
    });
  }

  limitReached(typeId: number, adding: number): boolean {
    const limit = this.itemLimits.get(typeId);
    if (limit === undefined) {
      return false;
    }
    const existing = [...this.units.values()].filter(u => u.owner === 'own' && u.spec.id === typeId).length;
    const queued = [...this.units.values()].reduce((sum, u) => sum + u.queue.filter(t => t === typeId).length, 0);
    return existing + queued + adding > limit;
  }

  ownCount(typeId: number): number {
    return [...this.units.values()].filter(u => u.owner === 'own' && u.spec.id === typeId).length;
  }

  /** An order was given and has not reached the unit yet (W8). */
  hasPendingOrder(unitId: number): boolean {
    return this.pendingCommands.some(command => command.unitIds.includes(unitId));
  }

  isIdle(unit: Unit): boolean {
    if (unit.owner !== 'own') {
      return true;
    }
    return unit.order === null && unit.queue.length === 0;
  }

  // --- Simulation --------------------------------------------------------------------------------

  /** One step of the game engine followed by the renderer update, as the worker tick does it. */
  step(millis: number): void {
    this.time += millis;
    const seconds = millis / 1000;
    const due = this.pendingCommands.filter(command => command.dueAt <= this.time);
    this.pendingCommands = this.pendingCommands.filter(command => command.dueAt > this.time);
    due.forEach(command => command.apply());

    for (const unit of [...this.units.values()]) {
      if (!this.units.has(unit.id)) {
        continue; // killed earlier in this step
      }
      unit.reloadLeft = Math.max(0, unit.reloadLeft - seconds);
      if (unit.containedIn !== null) {
        continue; // carried: does nothing, is nowhere
      }
      if (unit.owner === 'bot') {
        this.botDefends(unit);
      } else {
        this.execute(unit, seconds);
      }
    }
    const respawnsDue = this.pendingRespawns.filter(entry => entry.dueAt <= this.time);
    this.pendingRespawns = this.pendingRespawns.filter(entry => entry.dueAt > this.time);
    respawnsDue.forEach(entry => {
      const reborn = this.addBotUnit(entry.respawn.typeId, entry.respawn.x, entry.respawn.y, entry.respawn.delayMillis);
      this.respawns.set(reborn.id, entry.respawn);
    });
    this.checkQuest();
    this.renderer.sync();
  }

  private execute(unit: Unit, seconds: number): void {
    if (unit.queue.length > 0) {
      unit.queueProgress += seconds / itemTypeSpec(unit.queue[0]).buildSeconds;
      if (unit.queueProgress >= 1) {
        const typeId = unit.queue.shift()!;
        unit.queueProgress = 0;
        this.addUnit(typeId, 'own', unit.x, unit.y - unit.spec.radius - 2);
      }
    }
    const order = unit.order;
    if (!order || unit.spec.speed === 0) {
      return;
    }
    switch (order.kind) {
      case 'move':
        if (this.driveTo(unit, order.x, order.y, 0.5, seconds)) {
          unit.order = null;
        }
        break;
      case 'attack': {
        const target = this.units.get(order.targetId);
        if (!target || !unit.spec.weapon) {
          unit.order = null;
          break;
        }
        if (this.driveTo(unit, target.x, target.y, unit.spec.weapon.range + target.spec.radius, seconds)) {
          this.fire(unit, target);
        }
        break;
      }
      case 'harvest': {
        const resource = this.resources.get(order.resourceId);
        if (!resource || !unit.spec.harvester) {
          unit.order = null;
          break;
        }
        if (this.driveTo(unit, resource.x, resource.y, unit.spec.harvester.range + 1, seconds)) {
          const amount = Math.min(resource.amount, unit.spec.harvester.progressPerSecond * seconds);
          resource.amount -= amount;
          this.razarion = Math.min(this.maxRazarion, this.razarion + amount);
          this.questHarvested += amount;
          if (resource.amount <= 0.0001) {
            this.resources.delete(resource.id);
            // Stops on the empty field and does not look for the next one.
            unit.order = null;
          }
        }
        break;
      }
      case 'build':
      case 'finalize':
        this.executeBuild(unit, order, seconds);
        break;
      case 'load': {
        // SyncBaseItem.putInContainer: walks until the container is within the container's range.
        const container = this.units.get(order.containerId);
        if (!container || !container.spec.container) {
          unit.order = null;
          break;
        }
        if (this.driveTo(unit, container.x, container.y, container.spec.container.range, seconds)) {
          unit.order = null;
          unit.containedIn = container.id;
          container.cargo.push(unit.id);
        }
        break;
      }
    }
  }

  private executeBuild(unit: Unit, order: Order & ({ kind: 'build' } | { kind: 'finalize' }), seconds: number): void {
    if (order.kind === 'build' && unit.siteId !== null && !this.units.has(unit.siteId)) {
      // Its site was destroyed: the order is over, it does not lay down a new one.
      unit.order = null;
      unit.siteId = null;
      return;
    }
    let site = order.kind === 'finalize' ? this.units.get(order.siteId) : (unit.siteId !== null ? this.units.get(unit.siteId) : undefined);
    const x = site ? site.x : (order as any).x;
    const y = site ? site.y : (order as any).y;
    const radius = site ? site.spec.radius : itemTypeSpec((order as any).typeId).radius;
    if (!this.driveTo(unit, x, y, radius + 2, seconds)) {
      return;
    }
    if (!site) {
      if (order.kind === 'finalize') {
        unit.order = null;
        return;
      }
      const spec = itemTypeSpec(order.typeId);
      if (this.razarion < spec.price) {
        unit.order = null;
        return;
      }
      this.razarion -= spec.price;
      site = this.addUnit(order.typeId, 'own', x, y, 0);
      unit.siteId = site.id;
    }
    site.buildup = Math.min(1, site.buildup + seconds / site.spec.buildSeconds);
    if (site.buildup >= 1) {
      unit.order = null;
      unit.siteId = null;
    }
  }

  /** Moves toward the point; true once within reach. */
  private driveTo(unit: Unit, x: number, y: number, reach: number, seconds: number): boolean {
    const dx = x - unit.x;
    const dy = y - unit.y;
    const distance = Math.sqrt(dx * dx + dy * dy);
    if (distance <= reach) {
      return true;
    }
    const step = Math.min(unit.spec.speed * seconds, distance - reach);
    unit.x += dx / distance * step;
    unit.y += dy / distance * step;
    return distance - step <= reach;
  }

  private fire(unit: Unit, target: Unit): void {
    if (unit.reloadLeft > 0) {
      return;
    }
    unit.reloadLeft = unit.spec.weapon!.reloadSeconds;
    target.health -= unit.spec.weapon!.damage;
    if (target.health <= 0) {
      this.removeUnit(target, unit);
    }
  }

  /** Armed bot units shoot the nearest own unit in range; they never move. */
  private botDefends(unit: Unit): void {
    const weapon = unit.spec.weapon;
    if (!weapon) {
      return;
    }
    let nearest: Unit | null = null;
    let nearestDistance = Infinity;
    for (const candidate of this.units.values()) {
      if (candidate.owner !== 'own') {
        continue;
      }
      const distance = Math.hypot(candidate.x - unit.x, candidate.y - unit.y);
      if (distance <= weapon.range + candidate.spec.radius && distance < nearestDistance) {
        nearest = candidate;
        nearestDistance = distance;
      }
    }
    if (nearest) {
      this.fire(unit, nearest);
    }
  }

  private removeUnit(unit: Unit, killer: Unit | null): void {
    this.units.delete(unit.id);
    if (killer && killer.owner === 'own' && unit.owner === 'bot') {
      this.questKills.push(unit.spec.id);
    }
    const respawn = this.respawns.get(unit.id);
    if (respawn) {
      this.respawns.delete(unit.id);
      this.pendingRespawns.push({dueAt: this.time + respawn.delayMillis, respawn});
    }
  }

  private checkQuest(): void {
    const condition = this.questCondition;
    if (!condition) {
      return;
    }
    let passed = false;
    switch (condition.kind) {
      case 'created':
        passed = [...this.units.values()].filter(u => u.owner === 'own' && u.spec.id === condition.typeId
          && u.buildup >= 1 && (condition.includeExisting || !this.questExistingIds.has(u.id))).length >= condition.count;
        break;
      case 'killed':
        passed = this.questKills.filter(typeId => condition.typeId === null || typeId === condition.typeId).length >= condition.count;
        break;
      case 'harvested':
        passed = this.questHarvested >= condition.amount;
        break;
      case 'createdIn':
        passed = [...this.units.values()].some(u => u.owner === 'own' && u.spec.id === condition.typeId
          && u.buildup >= 1 && u.containedIn === null && insidePolygon(u.x, u.y, condition.region));
        break;
      case 'loaded':
        passed = [...this.units.values()].some(u => u.owner === 'own' && u.spec.id === condition.typeId
          && u.containedIn !== null);
        break;
      case 'loadedIn':
        passed = [...this.units.values()].some(u => u.owner === 'own' && u.spec.id === condition.containerTypeId
          && u.cargo.length > 0 && insidePolygon(u.x, u.y, condition.region));
        break;
      case 'in':
        passed = [...this.units.values()].some(u => u.owner === 'own' && u.spec.id === condition.typeId
          && u.containedIn === null && insidePolygon(u.x, u.y, condition.region));
        break;
      case 'sold':
        passed = this.questSold.filter(typeId => typeId === condition.typeId).length >= condition.count;
        break;
      case 'owns':
        passed = condition.types.every(type => [...this.units.values()].filter(u => u.owner === 'own'
          && u.spec.id === type.typeId && u.buildup >= 1 && u.containedIn === null
          && (!condition.region || insidePolygon(u.x, u.y, condition.region))).length >= type.count);
        break;
    }
    if (passed) {
      this.questCondition = null;
      this.onQuestPassed?.();
    }
  }
}

export function insidePolygon(x: number, y: number, corners: { x: number, y: number }[]): boolean {
  let inside = false;
  for (let i = 0, j = corners.length - 1; i < corners.length; j = i++) {
    const a = corners[i];
    const b = corners[j];
    if ((a.y > y) !== (b.y > y) && x < (b.x - a.x) * (y - a.y) / (b.y - a.y) + a.x) {
      inside = !inside;
    }
  }
  return inside;
}

// --- What the renderer holds ----------------------------------------------------------------------

function vertex(x: number, y: number): any {
  return {
    getX: () => x,
    getY: () => y,
    getZ: () => 0,
    toXY: () => ({getX: () => x, getY: () => y}),
    distance: (other: { getX(): number, getY(): number }) => Math.hypot(other.getX() - x, other.getY() - y)
  };
}

/**
 * The renderer's view of one base item: what BabylonBaseItemImpl offers the tip code, the
 * selection and the action service. Dies with the view (W2) - a disposed instance keeps answering,
 * the way a stale reference in the game does, but nothing it shows is on screen any more.
 */
export class FakeBaseItem {
  readonly itemType: any;
  readonly diplomacy: Diplomacy;
  disposed = false;
  private x = 0;
  private y = 0;
  /** W4: a fresh instance reads idle=false until the engine reports the real state. */
  private idle = false;
  private buildup: number | null = null;
  private selected = false;
  private promptText: string | null = null;
  private itemClickCallback: (() => void) | null = null;
  private idleCallback: ((idle: boolean) => void) | null = null;
  private selectionCallback: ((active: boolean) => void) | null = null;

  constructor(private readonly id: number, typeId: number, owner: Owner, private readonly world: World) {
    this.itemType = fakeBaseItemType(typeId);
    this.diplomacy = owner === 'own' ? Diplomacy.OWN : Diplomacy.ENEMY;
  }

  getId(): number {
    return this.id;
  }

  getBaseItemType(): any {
    return this.itemType;
  }

  getBaseId(): number {
    return this.diplomacy === Diplomacy.OWN ? 1 : 2;
  }

  getPosition(): any {
    return vertex(this.x, this.y);
  }

  setPosition(x: number, y: number): void {
    this.x = x;
    this.y = y;
  }

  getIdle(): boolean {
    return this.idle;
  }

  setIdle(idle: boolean): void {
    if (this.idle !== idle && this.idleCallback) {
      this.idleCallback(idle);
    }
    this.idle = idle;
  }

  setIdleCallback(callback: ((idle: boolean) => void) | null): void {
    this.idleCallback = callback;
  }

  getBuildup(): number {
    return this.buildup ?? 1;
  }

  getBuildupOrNull(): number | null {
    return this.buildup;
  }

  setBuildup(buildup: number): void {
    this.buildup = buildup;
  }

  isSelected(): boolean {
    return this.selected;
  }

  select(active: boolean): void {
    this.selected = active;
    this.selectionCallback?.(active);
  }

  setSelectionCallback(callback: ((active: boolean) => void) | null): void {
    this.selectionCallback = callback;
  }

  showSelectPromptVisualization(text: string = 'Click to select'): void {
    this.promptText = text;
  }

  hideSelectPromptVisualization(): void {
    this.promptText = null;
  }

  isSelectPromptVisible(): boolean {
    return this.promptText !== null;
  }

  getPromptText(): string | null {
    return this.promptText;
  }

  setItemClickCallback(callback: (() => void) | null): void {
    this.itemClickCallback = callback;
  }

  /** What a click on the mesh does in the game: the tip's callback first, then the action. */
  click(onItemClicked: (itemType: any, id: number, diplomacy: Diplomacy, item: any) => void): void {
    this.itemClickCallback?.();
    onItemClicked(this.itemType, this.id, this.diplomacy, this);
  }

  /** Streamed out (W2). The prompt and every callback go with the mesh. */
  dispose(): void {
    this.disposed = true;
    this.promptText = null;
  }

  isOnScreen(): boolean {
    return !this.disposed && this.world.onScreen(this.x, this.y);
  }

  /**
   * Whether a prompt on this item would be read. The real renderer hides the label when it no
   * longer fits - see watchSelectPromptOnScreen - so a prompt put somewhere it cannot be seen
   * leaves the player with nothing, and the bed has to show that the same way.
   */
  promptReadable(): boolean {
    return !this.disposed && this.world.promptReadable(this.x, this.y);
  }
}

export class FakeResourceItem {
  readonly itemType = {getId: () => 100, getName: () => 'Razarion'};
  readonly diplomacy = Diplomacy.RESOURCE;
  disposed = false;
  private promptText: string | null = null;
  private itemClickCallback: (() => void) | null = null;

  constructor(private readonly id: number, private readonly x: number, private readonly y: number,
              private readonly world: World) {
  }

  getId(): number {
    return this.id;
  }

  getPosition(): any {
    return vertex(this.x, this.y);
  }

  select(_active: boolean): void {
  }

  showSelectPromptVisualization(text: string = 'Click to select'): void {
    this.promptText = text;
  }

  hideSelectPromptVisualization(): void {
    this.promptText = null;
  }

  isSelectPromptVisible(): boolean {
    return this.promptText !== null;
  }

  getPromptText(): string | null {
    return this.promptText;
  }

  setItemClickCallback(callback: (() => void) | null): void {
    this.itemClickCallback = callback;
  }

  click(onItemClicked: (itemType: any, id: number, diplomacy: Diplomacy, item: any) => void): void {
    this.itemClickCallback?.();
    onItemClicked(this.itemType, this.id, this.diplomacy, this);
  }

  dispose(): void {
    this.disposed = true;
    this.promptText = null;
  }

  isOnScreen(): boolean {
    return !this.disposed && this.world.onScreen(this.x, this.y);
  }

  /**
   * Whether a prompt on this item would be read. The real renderer hides the label when it no
   * longer fits - see watchSelectPromptOnScreen - so a prompt put somewhere it cannot be seen
   * leaves the player with nothing, and the bed has to show that the same way.
   */
  promptReadable(): boolean {
    return !this.disposed && this.world.promptReadable(this.x, this.y);
  }
}

/** The selection operations the renderer triggers when items come and go (W3, W7). */
export interface RendererSelectionHooks {
  tryReattachItem(item: any): void;

  removeItem(id: number): void;

  removeOther(id: number): void;

  disposeItem(id: number): void;

  disposeOther(id: number): void;
}

/**
 * The part of BabylonRenderServiceAccessImpl the tips use, fed by the world the way
 * BaseItemUiService feeds the real one: instances only inside the render box, created before the
 * engine state is set on them, removed from view or disposed with the matching selection call.
 */
export class FakeRenderer {
  private baseItems: FakeBaseItem[] = [];
  private resourceItems: FakeResourceItem[] = [];
  private readonly baseItemCreatedListeners: ((item: any) => void)[] = [];
  private readonly resourceRemovedListeners: ((item: any) => void)[] = [];
  private readonly viewFieldListeners: ViewFieldListener[] = [];
  private placerCallback: ((event: BaseItemPlacerPresenterEvent) => void) | null = null;
  selectionHooks: RendererSelectionHooks | null = null;
  baseItemPlacerActive = false;
  /** The direction arrow as the player sees it: null when down. */
  outOfViewAngle: number | null = null;
  /** Where the "go there" chip beside the arrow would take the camera; null without a chip. */
  outOfViewTarget: { x: number, y: number } | null = null;
  placeMarkerShown = false;
  readonly touchSelectionMode = {asked: false, setAsked: (asked: boolean) => this.touchSelectionMode.asked = asked};

  constructor(private readonly world: World) {
  }

  // --- The world pushes state in -----------------------------------------------------------------

  sync(): void {
    const leftovers = new Set(this.baseItems.map(item => item.getId()));
    for (const unit of this.world.units.values()) {
      if (unit.containedIn !== null || !this.world.inRenderBox(unit.x, unit.y, unit.spec.radius)) {
        continue;
      }
      let item = this.baseItems.find(candidate => candidate.getId() === unit.id);
      if (!item) {
        item = new FakeBaseItem(unit.id, unit.spec.id, unit.owner, this.world);
        item.setPosition(unit.x, unit.y);
        this.baseItems.push(item);
        this.selectionHooks?.tryReattachItem(item);
        // Before the engine state below: the listener sees idle=false (W4).
        this.baseItemCreatedListeners.forEach(listener => listener(item));
      }
      item.setIdle(this.world.isIdle(unit));
      item.setPosition(unit.x, unit.y);
      item.setBuildup(unit.buildup);
      leftovers.delete(unit.id);
    }
    for (const id of leftovers) {
      const item = this.baseItems.find(candidate => candidate.getId() === id)!;
      this.baseItems = this.baseItems.filter(candidate => candidate !== item);
      item.dispose();
      if (this.world.units.has(id)) {
        this.selectionHooks?.removeItem(id);
        this.selectionHooks?.removeOther(id);
      } else {
        this.selectionHooks?.disposeItem(id);
        this.selectionHooks?.disposeOther(id);
      }
    }

    const resourceLeftovers = new Set(this.resourceItems.map(item => item.getId()));
    for (const resource of this.world.resources.values()) {
      if (!this.world.inRenderBox(resource.x, resource.y, 1)) {
        continue;
      }
      if (!this.resourceItems.some(candidate => candidate.getId() === resource.id)) {
        this.resourceItems.push(new FakeResourceItem(resource.id, resource.x, resource.y, this.world));
      }
      resourceLeftovers.delete(resource.id);
    }
    for (const id of resourceLeftovers) {
      const item = this.resourceItems.find(candidate => candidate.getId() === id)!;
      this.resourceItems = this.resourceItems.filter(candidate => candidate !== item);
      item.dispose();
      this.resourceRemovedListeners.forEach(listener => listener(item));
    }
  }

  onCameraMoved(): void {
    this.sync();
    const viewField = this.world.viewField();
    this.viewFieldListeners.forEach(listener => listener.onViewFieldChanged(viewField));
  }

  // --- What the tip code calls ------------------------------------------------------------------

  getBabylonBaseItemById(id: number): any {
    return this.baseItems.find(item => item.getId() === id) ?? null;
  }

  getBabylonBaseItemByDiplomacyItemType(diplomacy: Diplomacy, itemTypeId: number): any {
    return this.baseItems.find(item => item.diplomacy === diplomacy && item.itemType.getId() === itemTypeId) ?? null;
  }

  getBabylonBaseItemsByDiplomacy(diplomacy: Diplomacy): any[] {
    return this.baseItems.filter(item => item.diplomacy === diplomacy);
  }

  getBabylonResourceItemImpls(): any[] {
    return [...this.resourceItems];
  }

  getCurrentViewField(): ViewField {
    return this.world.viewField();
  }

  isPromptReadable(x: number, y: number): boolean {
    return this.world.promptReadable(x, y);
  }

  addViewFieldListener(listener: ViewFieldListener): void {
    if (!this.viewFieldListeners.includes(listener)) {
      this.viewFieldListeners.push(listener);
    }
  }

  removeViewFieldListener(listener: ViewFieldListener): void {
    const index = this.viewFieldListeners.indexOf(listener);
    if (index >= 0) {
      this.viewFieldListeners.splice(index, 1);
    }
  }

  addBaseItemCreatedListener(listener: (item: any) => void): void {
    this.baseItemCreatedListeners.push(listener);
  }

  addResourceRemovedListener(listener: (item: any) => void): void {
    this.resourceRemovedListeners.push(listener);
  }

  removeResourceRemovedListener(listener: (item: any) => void): void {
    const index = this.resourceRemovedListeners.indexOf(listener);
    if (index >= 0) {
      this.resourceRemovedListeners.splice(index, 1);
    }
  }

  showOutOfViewMarker(markerConfig: any, angle: number, target?: { x: number, y: number }): void {
    this.outOfViewAngle = markerConfig ? angle : null;
    this.outOfViewTarget = markerConfig && target ? target : null;
  }

  showPlaceMarker(placeConfig: any, _markerConfig: any): void {
    this.placeMarkerShown = !!placeConfig;
  }

  setBaseItemPlacerCallback(callback: ((event: BaseItemPlacerPresenterEvent) => void) | null): void {
    this.placerCallback = callback;
  }

  showCommandTargetMarker(_item: any, _kind: string): void {
  }

  showGroundCommandMarker(_x: number, _z: number, _radius: number, _kind: string, _y?: number): void {
  }

  // --- The placer, driven by the player -----------------------------------------------------------

  private placerTypeId: number | null = null;
  private placerBuilderIds: number[] = [];
  /** The container whose Unload button opened the placer; null for a build placer. */
  private unloadContainerId: number | null = null;

  activatePlacer(typeId: number, builderIds: number[]): void {
    this.placerTypeId = typeId;
    this.placerBuilderIds = builderIds;
    this.unloadContainerId = null;
    this.baseItemPlacerActive = true;
    this.placerCallback?.(BaseItemPlacerPresenterEvent.ACTIVATED);
  }

  /** JsItemCockpitBridge.requestUnload: the placer, with the unit type as the thing placed. */
  activateUnloadPlacer(containerId: number): void {
    this.placerTypeId = null;
    this.placerBuilderIds = [];
    this.unloadContainerId = containerId;
    this.baseItemPlacerActive = true;
    this.placerCallback?.(BaseItemPlacerPresenterEvent.ACTIVATED);
  }

  place(x: number, y: number): void {
    if (!this.baseItemPlacerActive) {
      throw new Error('No placer to place with');
    }
    this.placerCallback?.(BaseItemPlacerPresenterEvent.PLACED);
    if (this.unloadContainerId !== null) {
      this.world.unload(this.unloadContainerId, x, y);
    } else {
      this.world.command(this.placerBuilderIds, {kind: 'build', typeId: this.placerTypeId!, x, y});
    }
    this.deactivatePlacer();
  }

  cancelPlacer(): void {
    this.deactivatePlacer();
  }

  private deactivatePlacer(): void {
    this.baseItemPlacerActive = false;
    this.placerTypeId = null;
    this.unloadContainerId = null;
    this.placerCallback?.(BaseItemPlacerPresenterEvent.DEACTIVATED);
  }

  // --- For the observation --------------------------------------------------------------------------

  liveBaseItems(): FakeBaseItem[] {
    return [...this.baseItems];
  }

  liveResourceItems(): FakeResourceItem[] {
    return [...this.resourceItems];
  }
}

/**
 * The item cockpit as the tips see it. Rebuilt from the rendered selection on every selection change,
 * the way ItemCockpitService does it (W10) - a unit that is selected but off screen at that moment
 * is not in it.
 */
export class FakeItemCockpit {
  /** The build button the hint stands on, as the player sees it. */
  hintTypeId: number | null = null;
  /** The hint stands on the Unload button. */
  unloadHint = false;
  /** The hint stands on the sell button. */
  sellHint = false;
  /** The sell button took its first tap and waits for the second, as ItemCockpitComponent.sellArmed. */
  sellArmed = false;
  private cockpitTypeId: number | null = null;
  private cockpitItemIds: number[] = [];
  private buildClickCallback: ((model: { itemTypeId: number }) => void) | null = null;

  constructor(private readonly world: World) {
  }

  rebuild(selectedRenderedItems: { getId(): number, getBaseItemType(): any }[]): void {
    // A new selection is a new context: the armed sell button disarms (ItemCockpitComponent.ngDoCheck).
    this.sellArmed = false;
    const types = new Set(selectedRenderedItems.map(item => item.getBaseItemType().getId()));
    if (types.size === 1) {
      this.cockpitTypeId = [...types][0];
      this.cockpitItemIds = selectedRenderedItems.map(item => item.getId());
    } else {
      this.cockpitTypeId = null;
      this.cockpitItemIds = [];
    }
  }

  showBuildupTip(itemTypeId: number | null): boolean {
    if (itemTypeId == null) {
      this.hintTypeId = null;
      return true;
    }
    const blockReason = this.getBuildupTipBlockReason(itemTypeId);
    if (blockReason !== null) {
      // As in ItemCockpitComponent: the two "not there yet" reasons leave a hint standing.
      if (blockReason !== TipStallReason.COCKPIT_NOT_READY && blockReason !== TipStallReason.BUTTON_NOT_RENDERED) {
        this.hintTypeId = null;
      }
      return false;
    }
    this.hintTypeId = itemTypeId;
    return true;
  }

  getBuildupTipBlockReason(itemTypeId: number): string | null {
    if (this.cockpitTypeId === null) {
      return TipStallReason.COCKPIT_NOT_READY;
    }
    if (!this.buttons().includes(itemTypeId)) {
      return TipStallReason.NOT_BUILDABLE;
    }
    if (this.world.limitReached(itemTypeId, 1)) {
      return TipStallReason.ITEM_LIMIT;
    }
    if (this.world.razarion < itemTypeSpec(itemTypeId).price) {
      return TipStallReason.NO_MONEY;
    }
    return null;
  }

  setBuildClickCallback(callback: ((model: { itemTypeId: number }) => void) | null): void {
    this.buildClickCallback = callback;
  }

  /** As ItemCockpitComponent.showUnloadTip. */
  showUnloadTip(show: boolean): boolean {
    if (!show) {
      this.unloadHint = false;
      return true;
    }
    const blockReason = this.getUnloadTipBlockReason();
    this.unloadHint = blockReason === null;
    return this.unloadHint;
  }

  /** The Unload button is there while one container is selected, and enabled while it carries something. */
  getUnloadTipBlockReason(): string | null {
    const container = this.container();
    if (!container) {
      return TipStallReason.COCKPIT_NOT_READY;
    }
    if (container.cargo.length === 0) {
      return TipStallReason.BUTTON_DISABLED;
    }
    return null;
  }

  /** As ItemCockpitComponent.showSellTip. */
  showSellTip(show: boolean): boolean {
    this.sellHint = show && this.getSellTipBlockReason() === null;
    return this.sellHint || !show;
  }

  /** The sell button is there while one type of own item is selected. */
  getSellTipBlockReason(): string | null {
    return this.cockpitTypeId === null ? TipStallReason.COCKPIT_NOT_READY : null;
  }

  /** The player taps the sell button: the first tap arms it, the second sells the selection. */
  clickSell(): void {
    if (this.cockpitTypeId === null) {
      throw new Error('There is no sell button');
    }
    if (!this.sellArmed) {
      this.sellArmed = true;
      return;
    }
    this.sellArmed = false;
    this.world.sell([...this.cockpitItemIds]);
  }

  /** The player presses Unload. */
  clickUnload(renderer: FakeRenderer): void {
    const container = this.container();
    if (!container || container.cargo.length === 0) {
      throw new Error('There is no enabled Unload button');
    }
    renderer.activateUnloadPlacer(container.id);
  }

  private container(): Unit | null {
    if (this.cockpitTypeId === null || this.cockpitItemIds.length !== 1 || !itemTypeSpec(this.cockpitTypeId).container) {
      return null;
    }
    return this.world.units.get(this.cockpitItemIds[0]) ?? null;
  }

  buttons(): number[] {
    if (this.cockpitTypeId === null) {
      return [];
    }
    const spec = itemTypeSpec(this.cockpitTypeId);
    return spec.builds ?? spec.fabricates ?? [];
  }

  /** The player clicks a build button. Only an enabled button that is there can be clicked. */
  click(itemTypeId: number, renderer: FakeRenderer): void {
    if (!this.buttons().includes(itemTypeId)) {
      throw new Error(`The cockpit has no button for ${itemTypeId}`);
    }
    if (this.getBuildupTipBlockReason(itemTypeId) !== null) {
      throw new Error(`The button for ${itemTypeId} is disabled`);
    }
    const spec = itemTypeSpec(this.cockpitTypeId!);
    if (spec.builds) {
      renderer.activatePlacer(itemTypeId, this.cockpitItemIds);
    } else {
      this.world.fabricate(this.cockpitItemIds, itemTypeId);
    }
    this.buildClickCallback?.({itemTypeId});
  }
}
