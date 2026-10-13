/**
 * Shoreline detection via Marching Squares on the height grid.
 * Finds the 0m contour (water/land boundary), chains segments into polylines,
 * and resamples at even spacing to produce wave spawn points.
 */

export interface ShorelineSegment {
  x1: number; y1: number;
  x2: number; y2: number;
}

/**
 * Extract the 0m height contour from a grid using Marching Squares.
 * @param heights Flat array of height values, row-major [y * xCount + x]
 * @param xCount Number of columns in the grid
 * @param yCount Number of rows in the grid
 * @returns Array of line segments representing the shoreline
 */
export function detectShoreline(heights: ArrayLike<number>, xCount: number, yCount: number): ShorelineSegment[] {
  const segments: ShorelineSegment[] = [];

  function h(x: number, y: number): number {
    return heights[y * xCount + x];
  }

  function lerp(x0: number, y0: number, h0: number, x1: number, y1: number, h1: number): [number, number] {
    if (Math.abs(h1 - h0) < 1e-6) {
      return [(x0 + x1) / 2, (y0 + y1) / 2];
    }
    const t = -h0 / (h1 - h0);
    return [x0 + t * (x1 - x0), y0 + t * (y1 - y0)];
  }

  for (let y = 0; y < yCount - 1; y++) {
    for (let x = 0; x < xCount - 1; x++) {
      // Corner heights: BL, BR, TR, TL
      const bl = h(x, y);
      const br = h(x + 1, y);
      const tr = h(x + 1, y + 1);
      const tl = h(x, y + 1);

      // Classification: 1 = above water (>= 0), 0 = below water (< 0)
      const caseIndex =
        (bl >= 0 ? 1 : 0) |
        (br >= 0 ? 2 : 0) |
        (tr >= 0 ? 4 : 0) |
        (tl >= 0 ? 8 : 0);

      if (caseIndex === 0 || caseIndex === 15) continue;

      // Edge midpoints with linear interpolation
      const bottom = lerp(x, y, bl, x + 1, y, br);       // bottom edge
      const right = lerp(x + 1, y, br, x + 1, y + 1, tr); // right edge
      const top = lerp(x + 1, y + 1, tr, x, y + 1, tl);   // top edge
      const left = lerp(x, y + 1, tl, x, y, bl);           // left edge

      function addSeg(a: [number, number], b: [number, number]) {
        segments.push({ x1: a[0], y1: a[1], x2: b[0], y2: b[1] });
      }

      switch (caseIndex) {
        case 1: addSeg(bottom, left); break;
        case 2: addSeg(right, bottom); break;
        case 3: addSeg(right, left); break;
        case 4: addSeg(top, right); break;
        case 5: // Ambiguous: check center
          if ((bl + br + tr + tl) / 4 >= 0) {
            addSeg(top, left); addSeg(bottom, right);
          } else {
            addSeg(top, right); addSeg(bottom, left);
          }
          break;
        case 6: addSeg(top, bottom); break;
        case 7: addSeg(top, left); break;
        case 8: addSeg(left, top); break;
        case 9: addSeg(bottom, top); break;
        case 10: // Ambiguous
          if ((bl + br + tr + tl) / 4 >= 0) {
            addSeg(bottom, right); addSeg(left, top);
          } else {
            addSeg(bottom, left); addSeg(right, top);
          }
          break;
        case 11: addSeg(right, top); break;
        case 12: addSeg(left, right); break;
        case 13: addSeg(bottom, right); break;
        case 14: addSeg(left, bottom); break;
      }
    }
  }

  return segments;
}


/**
 * Compute shortest distance from each grid vertex to the nearest shoreline,
 * plus the arc-length position along the shoreline polyline.
 * Returns a flat array [signedDist, arcLength, ...] suitable for UV2 (stride 2).
 * - UV2.x = signed distance: positive on land, negative underwater
 * - UV2.y = arc-length along the nearest shoreline polyline (continuous U coordinate)
 */
export function computeShoreDistance(
  segments: ShorelineSegment[],
  heights: ArrayLike<number>,
  xCount: number,
  yCount: number,
  maxDist: number = 15
): number[] {
  // Build polylines with cumulative arc-length for continuous U
  const polylines = chainSegmentsToPolylines(segments);

  const uv2: number[] = new Array(xCount * yCount * 2);

  // Every vertex used to be measured against every shore segment of the tile: 161 x 161 vertices
  // times the whole coastline, 250 ms per coastal tile on a desktop and up to 620 (2026-10-11),
  // seconds on a phone - on the main thread, while the tile appears. A segment farther away than
  // maxDist can never win, so the segments go into a coarse grid and a vertex only looks at the
  // cells within maxDist. Ties keep going to the segment the full scan would have met first (the
  // lower sequence number), so the result is the same, value for value.
  const flat = flattenSegments(polylines);
  const grid = SegmentGrid.build(flat, xCount, yCount, maxDist);
  const seen = new Int32Array(flat.count).fill(-1);

  for (let y = 0; y < yCount; y++) {
    for (let x = 0; x < xCount; x++) {
      const vertex = y * xCount + x;
      const idx = vertex * 2;
      let minDist = maxDist;
      let arcLen = 0;
      let bestSeq = -1;

      // Find closest point on any polyline segment near enough to matter
      const cells = grid.cellsNear(x, y, maxDist);
      for (let ci = 0; ci < cells.length; ci++) {
        const cell = cells[ci];
        for (let k = 0; k < cell.length; k++) {
          const seq = cell[k];
          if (seen[seq] === vertex) {
            continue;
          }
          seen[seq] = vertex;
          const o = seq * 4;
          const x1 = flat.coords[o], y1 = flat.coords[o + 1], x2 = flat.coords[o + 2], y2 = flat.coords[o + 3];
          // Cheap reject: the bounding box alone is already farther than the best so far
          const bx = x < Math.min(x1, x2) ? Math.min(x1, x2) - x : x > Math.max(x1, x2) ? x - Math.max(x1, x2) : 0;
          const by = y < Math.min(y1, y2) ? Math.min(y1, y2) - y : y > Math.max(y1, y2) ? y - Math.max(y1, y2) : 0;
          if (bx > minDist || by > minDist) {
            continue;
          }
          // Closest point on the segment and its parameter t (0..1), inline: no object per segment
          const dx = x2 - x1;
          const dy = y2 - y1;
          const lenSq = dx * dx + dy * dy;
          let dist: number;
          let t: number;
          if (lenSq === 0) {
            dist = Math.sqrt((x - x1) ** 2 + (y - y1) ** 2);
            t = 0;
          } else {
            t = Math.max(0, Math.min(1, ((x - x1) * dx + (y - y1) * dy) / lenSq));
            const cx = x1 + t * dx;
            const cy = y1 + t * dy;
            dist = Math.sqrt((x - cx) ** 2 + (y - cy) ** 2);
          }
          if (dist < minDist || (bestSeq >= 0 && dist === minDist && seq < bestSeq)) {
            minDist = dist;
            bestSeq = seq;
            // Arc-length = cumulative distance to segment start + t * segment length
            arcLen = flat.cumStart[seq] + t * flat.length[seq];
          }
        }
      }

      const h = heights[y * xCount + x];
      const signedDist = h >= 0 ? minDist : -minDist;

      uv2[idx] = signedDist;
      uv2[idx + 1] = arcLen;
    }
  }

  // Smooth U values to remove discontinuities at polyline seams and sharp bends
  smoothU(uv2, xCount, yCount, 5);

  return uv2;
}

/**
 * Smooth UV2.y (along-shore U) values across the grid.
 * Skips vertices where neighbors have large jumps (polyline seams).
 * Only smooths vertices near the shore.
 */
function smoothU(uv2: number[], xCount: number, yCount: number, iterations: number): void {
  const maxShoreDist = 12;
  for (let iter = 0; iter < iterations; iter++) {
    // Copy current U values
    const uCopy = new Float32Array(xCount * yCount);
    for (let i = 0; i < xCount * yCount; i++) {
      uCopy[i] = uv2[i * 2 + 1];
    }

    for (let y = 1; y < yCount - 1; y++) {
      for (let x = 1; x < xCount - 1; x++) {
        const idx = y * xCount + x;
        if (Math.abs(uv2[idx * 2]) > maxShoreDist) continue;

        // Gather 3x3 neighborhood U values
        const center = uCopy[idx];
        let sum = 0;
        let count = 0;
        let maxDiff = 0;
        for (let dy = -1; dy <= 1; dy++) {
          for (let dx = -1; dx <= 1; dx++) {
            const ni = (y + dy) * xCount + (x + dx);
            const val = uCopy[ni];
            const diff = Math.abs(val - center);
            if (diff > maxDiff) maxDiff = diff;
            sum += val;
            count++;
          }
        }

        // Skip if large jump detected (seam of closed polyline)
        if (maxDiff > 5) continue;

        uv2[idx * 2 + 1] = sum / count;
      }
    }
  }
}

interface Polyline {
  points: Array<[number, number]>;
  cumDist: number[];
}

/**
 * Every segment of every polyline in the order the full scan visited them, which is the sequence
 * number ties are decided by: x1, y1, x2, y2 per segment, its arc-length start and its length.
 */
interface FlatSegments {
  count: number;
  coords: Float64Array;
  cumStart: Float64Array;
  length: Float64Array;
}

function flattenSegments(polylines: Polyline[]): FlatSegments {
  let count = 0;
  for (const poly of polylines) {
    count += Math.max(0, poly.points.length - 1);
  }
  const coords = new Float64Array(count * 4);
  const cumStart = new Float64Array(count);
  const length = new Float64Array(count);
  let seq = 0;
  for (const poly of polylines) {
    for (let i = 0; i < poly.points.length - 1; i++) {
      coords[seq * 4] = poly.points[i][0];
      coords[seq * 4 + 1] = poly.points[i][1];
      coords[seq * 4 + 2] = poly.points[i + 1][0];
      coords[seq * 4 + 3] = poly.points[i + 1][1];
      cumStart[seq] = poly.cumDist[i];
      length[seq] = poly.cumDist[i + 1] - poly.cumDist[i];
      seq++;
    }
  }
  return {count, coords, cumStart, length};
}

/** Segments bucketed by the grid cells their bounding box touches. */
class SegmentGrid {
  private constructor(private readonly cellSize: number,
                      private readonly columns: number,
                      private readonly rows: number,
                      private readonly cells: number[][]) {
  }

  static build(flat: FlatSegments, xCount: number, yCount: number, maxDist: number): SegmentGrid {
    // About maxDist/2: a query then covers a handful of cells, and a cell holds few segments.
    const cellSize = Math.max(1, Math.ceil(maxDist / 2));
    const columns = Math.ceil(xCount / cellSize) + 1;
    const rows = Math.ceil(yCount / cellSize) + 1;
    const cells: number[][] = new Array(columns * rows);
    for (let seq = 0; seq < flat.count; seq++) {
      const o = seq * 4;
      const c0 = clampCell(Math.floor(Math.min(flat.coords[o], flat.coords[o + 2]) / cellSize), columns);
      const c1 = clampCell(Math.floor(Math.max(flat.coords[o], flat.coords[o + 2]) / cellSize), columns);
      const r0 = clampCell(Math.floor(Math.min(flat.coords[o + 1], flat.coords[o + 3]) / cellSize), rows);
      const r1 = clampCell(Math.floor(Math.max(flat.coords[o + 1], flat.coords[o + 3]) / cellSize), rows);
      for (let r = r0; r <= r1; r++) {
        for (let c = c0; c <= c1; c++) {
          (cells[r * columns + c] ??= []).push(seq);
        }
      }
    }
    return new SegmentGrid(cellSize, columns, rows, cells);
  }

  private readonly near: number[][] = [];

  /**
   * The non-empty cells within `radius` of the point; a segment may be in more than one of them.
   * The returned array is reused by the next call.
   */
  cellsNear(x: number, y: number, radius: number): number[][] {
    const near = this.near;
    near.length = 0;
    const c0 = clampCell(Math.floor((x - radius) / this.cellSize), this.columns);
    const c1 = clampCell(Math.floor((x + radius) / this.cellSize), this.columns);
    const r0 = clampCell(Math.floor((y - radius) / this.cellSize), this.rows);
    const r1 = clampCell(Math.floor((y + radius) / this.cellSize), this.rows);
    for (let r = r0; r <= r1; r++) {
      for (let c = c0; c <= c1; c++) {
        const cell = this.cells[r * this.columns + c];
        if (cell) {
          near.push(cell);
        }
      }
    }
    return near;
  }
}

function clampCell(index: number, count: number): number {
  return Math.max(0, Math.min(count - 1, index));
}

/** Chain segments into polylines with cumulative arc-length */
function chainSegmentsToPolylines(segments: ShorelineSegment[]): Polyline[] {
  if (segments.length === 0) return [];

  const eps = 0.01;
  const key = (x: number, y: number) => `${Math.round(x / eps)},${Math.round(y / eps)}`;

  const adj = new Map<string, number[]>();
  for (let i = 0; i < segments.length; i++) {
    const s = segments[i];
    const k1 = key(s.x1, s.y1);
    const k2 = key(s.x2, s.y2);
    if (!adj.has(k1)) adj.set(k1, []);
    if (!adj.has(k2)) adj.set(k2, []);
    adj.get(k1)!.push(i);
    adj.get(k2)!.push(i);
  }

  const visited = new Set<number>();
  const result: Polyline[] = [];

  for (let i = 0; i < segments.length; i++) {
    if (visited.has(i)) continue;

    const points: Array<[number, number]> = [];
    const s0 = segments[i];
    points.push([s0.x1, s0.y1]);
    points.push([s0.x2, s0.y2]);
    visited.add(i);

    // Extend from end
    let endKey = key(s0.x2, s0.y2);
    let extended = true;
    while (extended) {
      extended = false;
      for (const ni of adj.get(endKey) || []) {
        if (visited.has(ni)) continue;
        visited.add(ni);
        const ns = segments[ni];
        const k1 = key(ns.x1, ns.y1);
        if (k1 === endKey) {
          points.push([ns.x2, ns.y2]);
          endKey = key(ns.x2, ns.y2);
        } else {
          points.push([ns.x1, ns.y1]);
          endKey = key(ns.x1, ns.y1);
        }
        extended = true;
        break;
      }
    }

    // Extend from start
    let startKey = key(points[0][0], points[0][1]);
    extended = true;
    while (extended) {
      extended = false;
      for (const ni of adj.get(startKey) || []) {
        if (visited.has(ni)) continue;
        visited.add(ni);
        const ns = segments[ni];
        const k1 = key(ns.x1, ns.y1);
        if (k1 === startKey) {
          points.unshift([ns.x2, ns.y2]);
          startKey = key(ns.x2, ns.y2);
        } else {
          points.unshift([ns.x1, ns.y1]);
          startKey = key(ns.x1, ns.y1);
        }
        extended = true;
        break;
      }
    }

    if (points.length >= 2) {
      // Compute cumulative distances
      const cumDist: number[] = [0];
      for (let j = 1; j < points.length; j++) {
        const dx = points[j][0] - points[j - 1][0];
        const dy = points[j][1] - points[j - 1][1];
        cumDist.push(cumDist[j - 1] + Math.sqrt(dx * dx + dy * dy));
      }
      result.push({ points, cumDist });
    }
  }

  return result;
}
