import {computeShoreDistance, detectShoreline} from './shoreline-detection';

/**
 * computeShoreDistance used to measure every vertex against every shore segment of the tile - 250
 * ms per coastal tile on a desktop, up to 620, on the main thread (2026-10-11). It now only looks at
 * the segments within maxDist. On six synthetic coasts at full tile size the new result was compared
 * with the old one value for value, with no difference. These cases pin the meaning of the result.
 */
describe('computeShoreDistance', () => {
  const N = 161;

  function grid(height: (x: number, y: number) => number): number[] {
    const heights: number[] = [];
    for (let y = 0; y < N; y++) {
      for (let x = 0; x < N; x++) {
        heights.push(height(x, y));
      }
    }
    return heights;
  }

  function uv2For(heights: number[]): number[] {
    return computeShoreDistance(detectShoreline(heights, N, N), heights, N, N);
  }

  const dist = (uv2: number[], x: number, y: number) => uv2[(y * N + x) * 2];
  const arc = (uv2: number[], x: number, y: number) => uv2[(y * N + x) * 2 + 1];

  it('measures the distance to a straight coast, positive on land and negative under water', () => {
    // The 0 m contour runs at x = 70.5.
    const uv2 = uv2For(grid(x => x - 70.5));

    expect(dist(uv2, 71, 80)).toBeCloseTo(0.5, 6);
    expect(dist(uv2, 75, 80)).toBeCloseTo(4.5, 6);
    expect(dist(uv2, 70, 80)).toBeCloseTo(-0.5, 6);
    expect(dist(uv2, 60, 80)).toBeCloseTo(-10.5, 6);
    // Farther than maxDist (15) is reported as maxDist
    expect(dist(uv2, 100, 80)).toBe(15);
    expect(dist(uv2, 20, 80)).toBe(-15);
  });

  it('runs the arc length along the coast, one unit per unit of coast', () => {
    const uv2 = uv2For(grid(x => x - 70.5));

    for (let y = 20; y < 140; y++) {
      expect(Math.abs(arc(uv2, 71, y + 1) - arc(uv2, 71, y))).toBeCloseTo(1, 3);
    }
  });

  it('keeps an island positive inside and negative outside, never beyond maxDist', () => {
    // Coast on a circle of radius 40 around the tile centre
    const uv2 = uv2For(grid((x, y) => 20 - Math.hypot(x - 80, y - 80) / 2));

    expect(dist(uv2, 80, 80)).toBe(15);
    expect(dist(uv2, 80 + 35, 80)).toBeCloseTo(5, 1);
    expect(dist(uv2, 80 + 45, 80)).toBeCloseTo(-5, 1);
    expect(dist(uv2, 0, 0)).toBe(-15);
    for (let i = 0; i < uv2.length; i += 2) {
      expect(Math.abs(uv2[i])).toBeLessThanOrEqual(15);
    }
  });

  it('reports maxDist everywhere on a tile without a coast', () => {
    const land = uv2For(grid(() => 5));
    const water = uv2For(grid(() => -5));

    for (let i = 0; i < land.length; i += 2) {
      expect(land[i]).toBe(15);
      expect(water[i]).toBe(-15);
      expect(land[i + 1]).toBe(0);
    }
  });
});
