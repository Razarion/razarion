import {clipToConvex, regionInReach} from './reach-area';
import {DecimalPosition, PlaceConfig} from '../../../gwtangular/GwtAngularFacade';
import {Point} from './tip-decision';

function polygonRegion(points: Point[]): PlaceConfig {
  const corners = points.map(p => ({getX: () => p.x, getY: () => p.y}) as unknown as DecimalPosition);
  return {getPolygon2D: () => ({toCornersAngular: () => corners}), getPosition: () => null, toRadiusAngular: () => 0};
}

function area(points: Point[]): number {
  let sum = 0;
  for (let i = 0; i < points.length; i++) {
    const a = points[i];
    const b = points[(i + 1) % points.length];
    sum += a.x * b.y - b.x * a.y;
  }
  return Math.abs(sum) / 2;
}

function cornersOf(placeConfig: PlaceConfig | null): Point[] {
  return placeConfig!.getPolygon2D()!.toCornersAngular().map(c => ({x: c.getX(), y: c.getY()}));
}

describe('regionInReach', () => {
  // Region 2063 (quest 392, unload onto the Phase 2 land) and the ship where the local repro
  // left it, 2026-09-27: reach 20.
  const region2063 = polygonRegion([
    {x: 149, y: 654}, {x: 241, y: 582}, {x: 304, y: 569}, {x: 357, y: 539}, {x: 411, y: 506},
    {x: 427, y: 465}, {x: 465, y: 439}, {x: 516, y: 399}, {x: 546, y: 350}, {x: 576, y: 313},
    {x: 725, y: 154}, {x: 866, y: 330}, {x: 703, y: 674}, {x: 246, y: 919}]);
  const reach = {x: 486.9, y: 463.9, radius: 20};

  it('marks only what is in reach', () => {
    const corners = cornersOf(regionInReach(region2063, reach));
    expect(corners.length).toBeGreaterThanOrEqual(3);
    for (const corner of corners) {
      expect(Math.hypot(corner.x - reach.x, corner.y - reach.y)).toBeLessThanOrEqual(reach.radius + 1e-6);
    }
  });

  it('is the whole reach for a ship inside the region', () => {
    // Region 2063 is drawn coarsely: its coast edge runs through the water, some 40 units off the
    // real shore, and the ship lies inside it. What the marker takes away is the land out of reach.
    const corners = cornersOf(regionInReach(region2063, reach));
    expect(area(corners)).toBeCloseTo(Math.PI * reach.radius * reach.radius, -1);
  });

  it('cuts the reach where it leaves the region', () => {
    // Centred on the region's corner 465/439: well over half the circle is outside.
    const corners = cornersOf(regionInReach(region2063, {x: 465, y: 439, radius: 20}));
    expect(area(corners)).toBeLessThan(0.6 * Math.PI * 400);
    expect(area(corners)).toBeGreaterThan(0.2 * Math.PI * 400);
  });

  it('is the reach itself without a region', () => {
    expect(cornersOf(regionInReach(null, reach)).length).toBe(48);
  });

  it('falls back to the region when nothing of it is in reach', () => {
    expect(regionInReach(region2063, {x: 0, y: 0, radius: 5})).toBe(region2063);
  });
});

describe('clipToConvex', () => {
  it('cuts a square to a smaller one', () => {
    const square = [{x: 0, y: 0}, {x: 10, y: 0}, {x: 10, y: 10}, {x: 0, y: 10}];
    const clip = [{x: 5, y: 5}, {x: 15, y: 5}, {x: 15, y: 15}, {x: 5, y: 15}];
    const result = clipToConvex(square, clip);
    const xs = result.map(p => p.x);
    const ys = result.map(p => p.y);
    expect(Math.min(...xs)).toBeCloseTo(5);
    expect(Math.max(...xs)).toBeCloseTo(10);
    expect(Math.min(...ys)).toBeCloseTo(5);
    expect(Math.max(...ys)).toBeCloseTo(10);
  });
});
