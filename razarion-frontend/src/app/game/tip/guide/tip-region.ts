import {PlaceConfig} from '../../../gwtangular/GwtAngularFacade';
import {ViewField} from '../../renderer/view-field';
import {Point} from './tip-decision';

/**
 * The build region of a SYNC_ITEM_POSITION quest (386), as the build tip needs it: whether it is in
 * view, and a point inside it to send the arrow to. Taken over from SendBuildCommandTipTask, where
 * the reasoning for both is written down.
 */
export class TipRegion {
  private readonly corners: Point[] | null;
  private readonly circle: { center: Point, radius: number } | null;

  constructor(placeConfig: PlaceConfig) {
    const position = placeConfig.getPosition();
    if (position) {
      this.circle = {center: {x: position.getX(), y: position.getY()}, radius: placeConfig.toRadiusAngular() || 1};
      this.corners = null;
    } else {
      this.corners = placeConfig.getPolygon2D()?.toCornersAngular().map(corner => ({x: corner.getX(), y: corner.getY()})) ?? null;
      this.circle = null;
    }
  }

  /** The region touches the part of the screen where a placement can be seen. */
  inView(viewField: ViewField): boolean {
    const rect = viewField.calculateInnerAabbRectangle();
    if (this.circle) {
      const closestX = Math.max(rect.x, Math.min(this.circle.center.x, rect.x + rect.width));
      const closestY = Math.max(rect.y, Math.min(this.circle.center.y, rect.y + rect.height));
      const dx = closestX - this.circle.center.x;
      const dy = closestY - this.circle.center.y;
      return dx * dx + dy * dy < this.circle.radius * this.circle.radius;
    }
    if (!this.corners) {
      return false;
    }
    const inRect = (point: Point) => point.x >= rect.x && point.x <= rect.x + rect.width
      && point.y >= rect.y && point.y <= rect.y + rect.height;
    if (this.corners.some(inRect)) {
      return true;
    }
    return [
      {x: rect.x, y: rect.y}, {x: rect.x + rect.width, y: rect.y},
      {x: rect.x + rect.width, y: rect.y + rect.height}, {x: rect.x, y: rect.y + rect.height}
    ].some(point => insidePolygon(point, this.corners!));
  }

  /** How far the point is from the region, 0 inside it. */
  distanceTo(point: Point): number {
    if (this.circle) {
      return Math.max(0, Math.sqrt(squaredDistance(point, this.circle.center)) - this.circle.radius);
    }
    if (!this.corners || this.corners.length < 3) {
      return Infinity;
    }
    if (insidePolygon(point, this.corners)) {
      return 0;
    }
    let best = Infinity;
    for (let i = 0, j = this.corners.length - 1; i < this.corners.length; j = i++) {
      best = Math.min(best, distanceToSegment(point, this.corners[j], this.corners[i]));
    }
    return best;
  }

  /**
   * A point inside the region, nearest to the builder: any point of it will do for the quest, and the
   * arrow is a direction to walk in. Not the centre of the bounding box - for quest 386's diagonal
   * coastal band that is 75.6 units outside the region.
   */
  pointFor(from: Point | null): Point | null {
    if (this.circle) {
      return this.circle.center;
    }
    return this.corners ? pointInPolygon(this.corners, from) : null;
  }
}

/**
 * A point inside the polygon, nearest to `from`, or in its widest part without one. Also where the
 * quest line takes the camera for a region (QuestMarkerService.jumpPoint).
 */
export function pointInPolygon(corners: Point[], from: Point | null): Point | null {
  if (corners.length < 3) {
    return null;
  }
  const candidates = interiorCandidates(corners);
  if (candidates.length === 0) {
    return null;
  }
  if (!from) {
    return candidates.reduce((a, b) => b.width > a.width ? b : a);
  }
  return candidates.reduce((a, b) =>
    squaredDistance(b, from) < squaredDistance(a, from) ? b : a);
}

/**
 * Midpoints of the region's horizontal cross sections, one set per sampling line between two
 * consecutive corner heights - inside the region by construction, however bent the shape is.
 */
function interiorCandidates(corners: Point[]): (Point & { width: number })[] {
  const heights = [...new Set(corners.map(corner => corner.y))].sort((one, other) => one - other);
  const candidates: (Point & { width: number })[] = [];
  for (let i = 0; i + 1 < heights.length; i++) {
    const y = (heights[i] + heights[i + 1]) / 2;
    const crossings: number[] = [];
    for (let current = 0, previous = corners.length - 1; current < corners.length; previous = current++) {
      const a = corners[current];
      const b = corners[previous];
      if ((a.y > y) !== (b.y > y)) {
        crossings.push((b.x - a.x) * (y - a.y) / (b.y - a.y) + a.x);
      }
    }
    crossings.sort((one, other) => one - other);
    for (let k = 0; k + 1 < crossings.length; k += 2) {
      candidates.push({x: (crossings[k] + crossings[k + 1]) / 2, y, width: crossings[k + 1] - crossings[k]});
    }
  }
  return candidates;
}

function insidePolygon(point: Point, corners: Point[]): boolean {
  let inside = false;
  for (let i = 0, j = corners.length - 1; i < corners.length; j = i++) {
    const a = corners[i];
    const b = corners[j];
    if ((a.y > point.y) !== (b.y > point.y) && point.x < (b.x - a.x) * (point.y - a.y) / (b.y - a.y) + a.x) {
      inside = !inside;
    }
  }
  return inside;
}

function distanceToSegment(point: Point, a: Point, b: Point): number {
  const dx = b.x - a.x;
  const dy = b.y - a.y;
  const lengthSquared = dx * dx + dy * dy;
  const t = lengthSquared > 0
    ? Math.max(0, Math.min(1, ((point.x - a.x) * dx + (point.y - a.y) * dy) / lengthSquared))
    : 0;
  return Math.sqrt(squaredDistance(point, {x: a.x + t * dx, y: a.y + t * dy}));
}

function squaredDistance(one: Point, other: Point): number {
  const dx = one.x - other.x;
  const dy = one.y - other.y;
  return dx * dx + dy * dy;
}
