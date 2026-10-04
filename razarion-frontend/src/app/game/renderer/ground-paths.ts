import {initPerm, scatterNoise, SEED} from "./procedural-textures";

interface GroundPath {
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
  // Soft edge (m) outside the path's half width, where earth fades into grass
  private static readonly EDGE = 0.9;
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

  /**
   * Path strength 0..1 per metre cell of one tile, row-major (size x size), cell (x, y) covering
   * [x0 + x, x0 + x + 1] x [y0 + y, y0 + y + 1]. Null when no path comes near the tile.
   */
  static createTileMask(x0: number, y0: number, size: number): Float32Array | null {
    const margin = 8;
    const segments: number[] = [];   // ax, ay, bx, by, halfWidth
    for (const path of GroundPaths.paths) {
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
    initPerm(SEED);
    const mask = new Float32Array(size * size);
    for (let y = 0; y < size; y++) {
      for (let x = 0; x < size; x++) {
        const px = x0 + x + 0.5, py = y0 + y + 0.5;
        let inside = Infinity;
        for (let s = 0; s < segments.length; s += 5) {
          const ax = segments[s], ay = segments[s + 1], dx = segments[s + 2] - ax, dy = segments[s + 3] - ay;
          const l = dx * dx + dy * dy;
          const t = l ? Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / l)) : 0;
          inside = Math.min(inside, Math.hypot(px - ax - t * dx, py - ay - t * dy) - segments[s + 4]);
        }
        if (inside > GroundPaths.EDGE + 1) {
          continue;
        }
        // A ragged edge: grass reaches into the path in some places, earth spills out in others
        const ragged = inside + scatterNoise(px / 2.5, py / 2.5) * 2.0;
        const t = Math.max(0, Math.min(1, (GroundPaths.EDGE - ragged) / (2 * GroundPaths.EDGE)));
        mask[y * size + x] = t * t * (3 - 2 * t);
      }
    }
    return mask;
  }
}
