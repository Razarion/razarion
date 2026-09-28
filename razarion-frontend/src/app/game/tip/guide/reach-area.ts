import {DecimalPosition, PlaceConfig} from '../../../gwtangular/GwtAngularFacade';
import type {Point} from './tip-decision';

/** A ship's reach: where its unload placer takes a unit (JsItemCockpitBridge.unloadReach). */
export interface Reach {
  x: number;
  y: number;
  radius: number;
}

const CIRCLE_SEGMENTS = 48;

/**
 * The part of a quest region the ship can actually unload into, as a PlaceConfig the place marker
 * draws. Marking the whole region told the player "anywhere here" when nearly all of it was out of
 * reach - at the Phase 2 coast the region is the whole land behind the beach, the reach a circle of
 * 20 around the ship (quest 392, 2026-09-27). Null region = the reach alone.
 * <p>
 * Falls back to the region itself when the two do not overlap, which the tip never asks for: it
 * sends the ship closer first.
 */
export function regionInReach(region: PlaceConfig | null, reach: Reach): PlaceConfig | null {
  const circle = circlePolygon(reach.x, reach.y, reach.radius);
  if (!region) {
    return polygonPlaceConfig(circle);
  }
  const corners = regionCorners(region);
  if (!corners) {
    return region;
  }
  const clipped = clipToConvex(corners, circle);
  return clipped.length >= 3 ? polygonPlaceConfig(clipped) : region;
}

function regionCorners(region: PlaceConfig): Point[] | null {
  const position = region.getPosition();
  if (position) {
    return circlePolygon(position.getX(), position.getY(), region.toRadiusAngular() || 1);
  }
  const corners = region.getPolygon2D()?.toCornersAngular().map(corner => ({x: corner.getX(), y: corner.getY()}));
  return corners && corners.length >= 3 ? corners : null;
}

/** Counter-clockwise, so the inside of every edge is on its left. */
function circlePolygon(x: number, y: number, radius: number): Point[] {
  const points: Point[] = [];
  for (let i = 0; i < CIRCLE_SEGMENTS; i++) {
    const angle = 2 * Math.PI * i / CIRCLE_SEGMENTS;
    points.push({x: x + radius * Math.cos(angle), y: y + radius * Math.sin(angle)});
  }
  return points;
}

/**
 * Sutherland-Hodgman: any polygon, convex or not, clipped by a convex counter-clockwise one. A
 * concave region can come out as one polygon with a zero-width seam where it should be two pieces;
 * for a marker that is invisible.
 */
export function clipToConvex(subject: Point[], clip: Point[]): Point[] {
  let output = subject;
  for (let i = 0; i < clip.length && output.length > 0; i++) {
    const a = clip[i];
    const b = clip[(i + 1) % clip.length];
    const inside = (p: Point) => (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x) >= 0;
    const input = output;
    output = [];
    for (let j = 0; j < input.length; j++) {
      const current = input[j];
      const previous = input[(j + input.length - 1) % input.length];
      if (inside(current)) {
        if (!inside(previous)) {
          output.push(intersection(previous, current, a, b));
        }
        output.push(current);
      } else if (inside(previous)) {
        output.push(intersection(previous, current, a, b));
      }
    }
  }
  return output;
}

function intersection(p: Point, q: Point, a: Point, b: Point): Point {
  const d = (p.x - q.x) * (a.y - b.y) - (p.y - q.y) * (a.x - b.x);
  if (d === 0) {
    return q;
  }
  const t = ((p.x - a.x) * (a.y - b.y) - (p.y - a.y) * (a.x - b.x)) / d;
  return {x: p.x + t * (q.x - p.x), y: p.y + t * (q.y - p.y)};
}

/** Only what the place marker reads: the corners' getX/getY. */
function polygonPlaceConfig(points: Point[]): PlaceConfig {
  const corners = points.map(point => ({getX: () => point.x, getY: () => point.y}) as unknown as DecimalPosition);
  return {
    getPolygon2D: () => ({toCornersAngular: () => corners}),
    getPosition: () => null,
    toRadiusAngular: () => 0
  };
}
