import {initPerm, scatterNoise, SEED} from "./procedural-textures";

export interface GroundPath {
  name: string;
  width: number;
  points: [number, number][];
}

/**
 * Trodden-earth paths drawn into the ground (the green channel of the GroundUtility texture) - look
 * only, pathing knows nothing of them. The network is made by
 * razarion-ai-content/scripts/noob-island-paths.mjs and shipped as a small JSON file, fetched once at
 * the start; until it has arrived (or if it is missing) tiles are built without paths.
 */
export class GroundPaths {
  private static readonly URL = "renderer/paths/planet-117.json";
  private static paths: GroundPath[] = [];
  private static loading: Promise<void> | null = null;

  /** Fetched once; resolves on failure too, a tile must never wait for ever on decoration. */
  static load(): Promise<void> {
    if (!GroundPaths.loading) {
      GroundPaths.loading = fetch(GroundPaths.URL)
        .then(response => response.ok ? response.json() : {paths: []})
        .then(json => {
          GroundPaths.paths = json.paths ?? [];
        })
        .catch(error => console.warn("[Razarion] ground paths not loaded", error));
    }
    return GroundPaths.loading;
  }

  /** The paths loaded so far - empty until load() has resolved. */
  static getPaths(): GroundPath[] {
    return GroundPaths.paths;
  }

  /** createPathTileMask over the loaded paths. */
  static createTileMask(x0: number, y0: number, size: number): Float32Array | null {
    return createPathTileMask(GroundPaths.paths, x0, y0, size);
  }
}

// Soft edge (m) outside the path's half width, where earth fades into grass
const EDGE = 0.9;
// Side (m) of the grid cells the segments are filed under in createPathTileMask
const GRID_CELL = 8;

/**
 * Path strength 0..1 per metre cell of one tile, row-major (size x size), cell (x, y) covering
 * [x0 + x, x0 + x + 1] x [y0 + y, y0 + y + 1]. Null when no path comes near the tile. Pure - it also
 * runs in the terrain helper worker (tile-ground-compute.ts).
 */
export function createPathTileMask(paths: GroundPath[], x0: number, y0: number, size: number): Float32Array | null {
  const margin = 8;
  const segments: number[] = [];   // ax, ay, bx, by, halfWidth
  for (const path of paths) {
    const p = path.points;
    for (let i = 1; i < p.length; i++) {
      const [ax, ay] = p[i - 1], [bx, by] = p[i];
      if (Math.max(ax, bx) < x0 - margin || Math.min(ax, bx) > x0 + size + margin
        || Math.max(ay, by) < y0 - margin || Math.min(ay, by) > y0 + size + margin) {
        continue;
      }
      segments.push(ax, ay, bx, by, path.width / 2);
    }
  }
  if (segments.length === 0) {
    return null;
  }
  // Every metre cell used to be measured against every path segment near the tile - 506 ms for
  // the first tile on the start island, on the main thread (2026-10-11). Only a segment whose
  // edge is within EDGE + 1 of a cell can change the result, so each segment is filed under the
  // grid cells its box widened by that reach touches, and a metre cell looks at its grid cell
  // alone. The minimum does not depend on the order, so the mask is the same, value for value.
  const reachPadding = EDGE + 1;
  const columns = Math.ceil(size / GRID_CELL);
  const grid: number[][] = new Array(columns * columns);
  for (let s = 0; s < segments.length; s += 5) {
    const reach = segments[s + 4] + reachPadding;
    const c0 = gridIndex(Math.min(segments[s], segments[s + 2]) - reach - x0, columns);
    const c1 = gridIndex(Math.max(segments[s], segments[s + 2]) + reach - x0, columns);
    const r0 = gridIndex(Math.min(segments[s + 1], segments[s + 3]) - reach - y0, columns);
    const r1 = gridIndex(Math.max(segments[s + 1], segments[s + 3]) + reach - y0, columns);
    for (let r = r0; r <= r1; r++) {
      for (let c = c0; c <= c1; c++) {
        (grid[r * columns + c] ??= []).push(s);
      }
    }
  }
  initPerm(SEED);
  const mask = new Float32Array(size * size);
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      const cell = grid[Math.floor(y / GRID_CELL) * columns + Math.floor(x / GRID_CELL)];
      if (!cell) {
        continue;
      }
      const px = x0 + x + 0.5, py = y0 + y + 0.5;
      let inside = Infinity;
      for (const s of cell) {
        const ax = segments[s], ay = segments[s + 1], dx = segments[s + 2] - ax, dy = segments[s + 3] - ay;
        const l = dx * dx + dy * dy;
        const t = l ? Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / l)) : 0;
        inside = Math.min(inside, Math.hypot(px - ax - t * dx, py - ay - t * dy) - segments[s + 4]);
      }
      if (inside > EDGE + 1) {
        continue;
      }
      // A ragged edge: grass reaches into the path in some places, earth spills out in others
      const ragged = inside + scatterNoise(px / 2.5, py / 2.5) * 2.0;
      const t = Math.max(0, Math.min(1, (EDGE - ragged) / (2 * EDGE)));
      mask[y * size + x] = t * t * (3 - 2 * t);
    }
  }
  return mask;
}

/** The grid column (or row) of a tile-relative coordinate, clamped into the tile. */
function gridIndex(offset: number, columns: number): number {
  return Math.max(0, Math.min(columns - 1, Math.floor(offset / GRID_CELL)));
}
