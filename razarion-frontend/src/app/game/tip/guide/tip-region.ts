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
 * <p>
 * Along a cross section the point slides towards `from`, but keeps {@link EDGE_MARGIN} from its
 * ends. For a long band along a coast - the harbour zone of the Noob Island band, 1245 m wide - the
 * middle of the cross section is hundreds of metres away from the builder, while straight across
 * from it is where it should build.
 * <p>
 * Across the cross sections it does the same: one more is taken at the height of `from`, kept
 * {@link CROSS_MARGIN} inside the region. The cross sections between the corner heights alone put
 * a rectangle's point on its middle line - for "Phase 2 region 1", 850 m deep, the build tip sent
 * the builder 425 m inland from where it landed, far from the bots it fights next.
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
  const top = Math.min(...corners.map(corner => corner.y)) + CROSS_MARGIN;
  const bottom = Math.max(...corners.map(corner => corner.y)) - CROSS_MARGIN;
  if (top <= bottom) {
    candidates.push(...crossSection(corners, Math.max(top, Math.min(bottom, from.y))));
  }
  const points = candidates.map(candidate => {
    const reach = Math.max(0, candidate.width / 2 - EDGE_MARGIN);
    return {x: Math.max(candidate.x - reach, Math.min(candidate.x + reach, from.x)), y: candidate.y};
  });
  // Cross sections run horizontally, so along a wavy coastal strip some of them only graze it. A point
  // on such a one sits at the shore, where a dockyard does not fit - prefer points well inside.
  const inside = points.filter(point => distanceToBoundary(point, corners) >= INNER_MARGIN);
  return (inside.length > 0 ? inside : points)
    .reduce((a, b) => squaredDistance(b, from) < squaredDistance(a, from) ? b : a);
}

/** How far {@link pointInPolygon} stays from the ends of a cross section. */
const EDGE_MARGIN = 20;
/** How far inside the region {@link pointInPolygon} prefers its point to be. */
const INNER_MARGIN = 5;
/** How far from the top and bottom of the region the cross section at the builder's height stays. */
const CROSS_MARGIN = 30;

function distanceToBoundary(point: Point, corners: Point[]): number {
  let best = Infinity;
  for (let i = 0, j = corners.length - 1; i < corners.length; j = i++) {
    best = Math.min(best, distanceToSegment(point, corners[j], corners[i]));
  }
  return best;
}

/**
 * Midpoints of the region's horizontal cross sections, one set per sampling line between two
 * consecutive corner heights - inside the region by construction, however bent the shape is.
 */
function interiorCandidates(corners: Point[]): (Point & { width: number })[] {
  const heights = [...new Set(corners.map(corner => corner.y))].sort((one, other) => one - other);
  const candidates: (Point & { width: number })[] = [];
  for (let i = 0; i + 1 < heights.length; i++) {
    candidates.push(...crossSection(corners, (heights[i] + heights[i + 1]) / 2));
  }
  return candidates;
}

/** Midpoints of the parts of the horizontal line at `y` that lie inside the region. */
function crossSection(corners: Point[], y: number): (Point & { width: number })[] {
  const crossings: number[] = [];
  for (let current = 0, previous = corners.length - 1; current < corners.length; previous = current++) {
    const a = corners[current];
    const b = corners[previous];
    if ((a.y > y) !== (b.y > y)) {
      crossings.push((b.x - a.x) * (y - a.y) / (b.y - a.y) + a.x);
    }
  }
  crossings.sort((one, other) => one - other);
  const sections: (Point & { width: number })[] = [];
  for (let k = 0; k + 1 < crossings.length; k += 2) {
    sections.push({x: (crossings[k] + crossings[k + 1]) / 2, y, width: crossings[k + 1] - crossings[k]});
  }
  return sections;
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
