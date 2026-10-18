import {Injectable, NgZone} from '@angular/core';
import {ActionService, OrderNote} from './action.service';
import {BabylonRenderServiceAccessImpl} from './renderer/babylon-render-service-access-impl.service';
import {GwtAngularService} from '../gwtangular/GwtAngularService';
import {TipItemState} from '../gwtangular/GwtAngularFacade';
import {TipService} from './tip/tip.service';
import {SelectionService} from './selection.service';
import {FirstInteractionTrackerService} from './tracking/first-interaction-tracker.service';
import {GwtInstance} from '../gwtangular/GwtInstance';

interface Point {
  x: number;
  y: number;
}

interface Follow {
  order: OrderNote;
  startedAt: number;
  /** Where the order goes, tracked while it is an item the engine reports (a base item). */
  target: Point | null;
  /** Whether the target is an item that can disappear (attacked, loaded into, finished). */
  targetTracked: boolean;
  targetGoneAt: number | null;
  idleSince: number | null;
  point: Point | null;
  stop: (() => void) | null;
}

/**
 * The camera goes along with an order while the quest is guided by a tip: the units on their way,
 * then the units and what they were sent to together.
 * <p>
 * A phone shows a narrow strip of the world, and a player who has never scrolled may not know it can
 * be. Quest 365 sends a Viper at an Extractor; the "go there" chip took the camera to the Extractor,
 * and the player saw the Extractor and shots coming in from the side (reported 2026-10-14). Many
 * phone games never scroll at all.
 * <p>
 * Only while there is a tip - the guided start - and the player always wins: moving the camera ends
 * the follow for this order. It also ends when the units have arrived and stand, when the target is
 * gone, when no unit of the order is left, and with the next order.
 */
@Injectable({
  providedIn: 'root'
})
export class OrderFollowService {
  /** How often the units are looked up; the camera eases toward the point every frame in between. */
  private static readonly POLL_MILLIS = 200;
  /** Units closer to their target than this are shown together with it, at the midpoint. */
  private static readonly TOGETHER_DISTANCE = 35;
  /** Units stand at the start until the engine has taken the order up. */
  private static readonly START_GRACE_MILLIS = 2000;
  private static readonly ARRIVED_IDLE_MILLIS = 1500;
  /** The kill, the load or the finished building stays on screen for a moment. */
  private static readonly TARGET_GONE_LINGER_MILLIS = 1500;
  /** A harvester never stands: it is there once it has reached the field. */
  private static readonly HARVEST_ARRIVED_DISTANCE = 12;
  private static readonly MAX_MILLIS = 180000;
  private follow: Follow | null = null;
  private timer: ReturnType<typeof setInterval> | null = null;

  constructor(private readonly actionService: ActionService,
              private readonly renderService: BabylonRenderServiceAccessImpl,
              private readonly gwtAngularService: GwtAngularService,
              private readonly tipService: TipService,
              private readonly selectionService: SelectionService,
              private readonly tracker: FirstInteractionTrackerService,
              private readonly zone: NgZone) {
    this.actionService.addOrderListener(order => this.onOrder(order));
    this.renderService.setArrowJumpFraming((x, y) => this.frameArrowJump(x, y));
  }

  private onOrder(order: OrderNote): void {
    this.end(null);
    if (!this.tipService.isActive()) {
      return;
    }
    const items = this.items(order);
    if (!items) {
      return;
    }
    const units = this.centroid(items, order.unitIds);
    const tracked = order.targetId !== null && order.kind !== 'harvest' && order.kind !== 'pickBox';
    const target = (tracked ? this.position(items, order.targetId) : null) ?? order.target ?? null;
    if (!units) {
      return;
    }
    // On screen already, both: nothing to follow.
    if (this.inView(units) && (!target || this.inView(target))) {
      return;
    }
    const follow: Follow = {
      order,
      startedAt: performance.now(),
      target,
      targetTracked: tracked,
      targetGoneAt: null,
      idleSince: null,
      point: this.framePoint(units, target),
      stop: null
    };
    this.follow = follow;
    this.tracker.report('ORDER_FOLLOW', 'kind=' + order.kind);
    follow.stop = this.renderService.followViewFieldCenter(() => follow.point, () => {
      follow.stop = null;
      const seconds = Math.round((performance.now() - follow.startedAt) / 1000);
      this.tracker.report('ORDER_FOLLOW_TAKEOVER', `kind=${order.kind} after=${seconds < 3 ? '0-2' : seconds < 10 ? '3-9' : '10+'}`);
      this.end(follow);
    });
    // The look-ups run outside Angular like the follow itself: they change no template.
    this.timer = this.zone.runOutsideAngular(() => setInterval(() => this.poll(follow), OrderFollowService.POLL_MILLIS));
  }

  private poll(follow: Follow): void {
    if (this.follow !== follow) {
      return;
    }
    const now = performance.now();
    const items = this.items(follow.order);
    const units = items ? this.centroid(items, follow.order.unitIds) : null;
    if (!items || !units || now - follow.startedAt > OrderFollowService.MAX_MILLIS) {
      // No unit of the order left - dead, or loaded into the transporter it was sent to.
      this.end(follow);
      return;
    }
    if (follow.targetTracked) {
      const target = this.position(items, follow.order.targetId);
      if (target) {
        follow.target = target;
      } else if (follow.targetGoneAt === null) {
        follow.targetGoneAt = now;
      }
    }
    if (follow.targetGoneAt !== null && now - follow.targetGoneAt > OrderFollowService.TARGET_GONE_LINGER_MILLIS) {
      this.end(follow);
      return;
    }
    if (this.arrived(follow, items, units, now)) {
      this.end(follow);
      return;
    }
    follow.point = this.framePoint(units, follow.target);
  }

  private arrived(follow: Follow, items: TipItemState[], units: Point, now: number): boolean {
    if (now - follow.startedAt < OrderFollowService.START_GRACE_MILLIS) {
      return false;
    }
    if (follow.order.kind === 'harvest') {
      return !!follow.target && distance(units, follow.target) < OrderFollowService.HARVEST_ARRIVED_DISTANCE;
    }
    const ids = new Set(follow.order.unitIds);
    const allIdle = items.filter(item => item.own && ids.has(item.id)).every(item => item.idle);
    if (!allIdle) {
      follow.idleSince = null;
      return false;
    }
    follow.idleSince ??= now;
    return now - follow.idleSince > OrderFollowService.ARRIVED_IDLE_MILLIS;
  }

  private end(follow: Follow | null): void {
    const current = this.follow;
    if (!current || (follow !== null && current !== follow)) {
      return;
    }
    this.follow = null;
    if (this.timer !== null) {
      clearInterval(this.timer);
      this.timer = null;
    }
    current.stop?.();
    current.stop = null;
  }

  /**
   * The "go there" chip beside a tip's arrow: the picture goes to the target, leaning toward the
   * selected units by at most JUMP_FRAME_MAX_SHIFT so near ones come along. The target stays clear.
   */
  private frameArrowJump(x: number, y: number): Point {
    const target = {x, y};
    const selected = this.selectionService.getSelectedOwnItemIds();
    if (selected.length === 0) {
      return target;
    }
    const items = this.safeItemStates(-1);
    return frameArrowJumpPoint(target, items ? this.centroid(items, selected) : null);
  }

  private framePoint(units: Point, target: Point | null): Point {
    if (target && distance(units, target) < OrderFollowService.TOGETHER_DISTANCE) {
      return midpoint(units, target);
    }
    return units;
  }

  private items(order: OrderNote): TipItemState[] | null {
    // Enemies are only in the snapshot by the type asked for.
    return this.safeItemStates(order.kind === 'attack' ? (order.targetTypeId ?? 0) : -1);
  }

  private safeItemStates(enemyItemTypeId: number): TipItemState[] | null {
    try {
      return this.gwtAngularService.gwtAngularFacade.baseItemUiService.getTipItemStates(enemyItemTypeId);
    } catch (e) {
      return null;
    }
  }

  private centroid(items: TipItemState[], ids: number[]): Point | null {
    const wanted = new Set(ids);
    const units = items.filter(item => item.own && wanted.has(item.id));
    if (units.length === 0) {
      return null;
    }
    return {
      x: units.reduce((sum, unit) => sum + unit.x, 0) / units.length,
      y: units.reduce((sum, unit) => sum + unit.y, 0) / units.length
    };
  }

  private position(items: TipItemState[], id: number | null): Point | null {
    const item = id !== null ? items.find(candidate => candidate.id === id) : undefined;
    return item ? {x: item.x, y: item.y} : null;
  }

  private inView(point: Point): boolean {
    const viewField = this.renderService.getCurrentViewField();
    return !!viewField && viewField.contains(GwtInstance.newDecimalPosition(point.x, point.y));
  }
}

/** The jump to an arrow's target takes the selected units into the picture up to this far away. */
const JUMP_FRAME_DISTANCE = 60;
/**
 * How far the jump may move the picture's centre off the target toward the selected units. The
 * midpoint it used to take was up to 30 m off: on a phone held upright the picture is under 40 m
 * wide, and the target - the builder a quest asks for - ended up under the button strip, where the
 * tip counts it as hidden and keeps the arrow up (phone, 2026-10-18).
 */
const JUMP_FRAME_MAX_SHIFT = 6;

/**
 * Where the "go there" chip puts the picture's centre: on the target, leaning toward the selected
 * units (their centroid, null for none) by at most JUMP_FRAME_MAX_SHIFT. Shared with the tip test
 * bed, so the bed jumps where the game does.
 */
export function frameArrowJumpPoint(target: Point, units: Point | null): Point {
  if (!units) {
    return target;
  }
  const towardUnits = distance(units, target);
  if (towardUnits <= 0 || towardUnits > JUMP_FRAME_DISTANCE) {
    return target;
  }
  const shift = Math.min(towardUnits / 2, JUMP_FRAME_MAX_SHIFT);
  return {
    x: target.x + (units.x - target.x) * shift / towardUnits,
    y: target.y + (units.y - target.y) * shift / towardUnits
  };
}

function distance(a: Point, b: Point): number {
  return Math.hypot(a.x - b.x, a.y - b.y);
}

function midpoint(a: Point, b: Point): Point {
  return {x: (a.x + b.x) / 2, y: (a.y + b.y) / 2};
}
