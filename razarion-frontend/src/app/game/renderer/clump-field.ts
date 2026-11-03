import {Matrix} from '@babylonjs/core/Maths/math.vector';
import {BabylonTerrainTileImpl} from './babylon-terrain-tile.impl';

/** Edge (m) of a chunk: the unit the clumps are worked out and cached in. 160 / 16: one tile each. */
const CHUNK = 16;
/** Edge (m) of the cells the spots are seeded by */
const CELL = 2;
/** Floats per clump in a chunk's list: matrix, colour, the draw that the camera fade compares against */
const STRIDE = 21;
/** A chunk's clumps are sorted by their fade draw into this many buckets (assemble, visible) */
const BUCKETS = 10;
/** Phones and tablets get half the clumps: their GPU and CPU are a few times slower, the screen small */
const DEVICE_DENSITY = typeof matchMedia !== 'undefined' && matchMedia('(pointer: coarse)').matches ? 0.5 : 1;

/** Hands a clump that stands to its variant's list. */
export type AddClump = (variant: number, matrix: Matrix, r: number, g: number, b: number) => void;
/** Decides for a spot (on its tile, with its own random sequence) whether and which clump stands there. */
export type PlaceClump = (tile: any, x: number, z: number, rnd: () => number, add: AddClump) => void;

/**
 * The clumps around the camera per variant (ClumpField.assemble): matrices (16 floats each), colours (4),
 * fade draws (1), in order of the draws; ends[v][b] = how many of them lie in the first b buckets.
 */
export interface Clumps {
  matrices: Float32Array[];
  colors: Float32Array[];
  fades: Float32Array[];
  ends: Int32Array[];
}

/** One chunk per variant: its clumps sorted by the fade draw, and where each bucket of draws starts. */
interface Chunk {
  lists: Float32Array[];
  starts: Int32Array[];
}

/**
 * Where the clumps of one kind stand (grass-clumps.ts, earth-clumps.ts), worked out per 16 m chunk
 * and kept: what is expensive - the tile's zone, the noise, the matrices - runs once per chunk, a
 * few at a time per frame (prepare), and showing them around the camera (assemble) only copies the
 * chunks' lists together. Every spot has a random sequence of its own, so a chunk comes out the same
 * whenever it is worked out, and the clumps stay put as the camera moves.
 */
export class ClumpField {
  private readonly chunks = new Map<number, Chunk>();
  private worked = 0;

  constructor(private readonly variants: number, private readonly salt: number,
              private readonly density: () => number, private readonly place: PlaceClump) {
  }

  /** Counts up with every chunk worked out: when it changed, there is more to show. */
  version(): number {
    return this.worked;
  }

  /** Forget all chunks - after a setting has changed. */
  reset(): void {
    this.chunks.clear();
  }

  /**
   * Works out the missing chunks within radius around (x, z), nearest first, until budgetMs is spent,
   * and drops those beyond keepRadius. True once all are there. Chunks whose tile is not ready
   * (not in tiles) wait.
   */
  prepare(tiles: Map<number, any>, x: number, z: number, radius: number, keepRadius: number, budgetMs: number): boolean {
    const start = performance.now();
    for (const key of [...this.chunks.keys()]) {
      const [cx, cz] = ClumpField.coords(key);
      if (ClumpField.distance(cx, cz, x, z) > keepRadius || !tiles.has(ClumpField.tileKey(cx, cz))) {
        this.chunks.delete(key);
      }
    }
    const missing: { cx: number, cz: number, d: number }[] = [];
    this.forChunks(x, z, radius, (cx, cz) => {
      if (!this.chunks.has(ClumpField.key(cx, cz)) && tiles.has(ClumpField.tileKey(cx, cz))) {
        missing.push({cx, cz, d: ClumpField.distance(cx, cz, x, z)});
      }
    });
    missing.sort((a, b) => a.d - b.d);
    for (const {cx, cz} of missing) {
      if (performance.now() - start > budgetMs) {
        return false;
      }
      this.chunks.set(ClumpField.key(cx, cz), this.work(tiles.get(ClumpField.tileKey(cx, cz)), cx, cz));
      this.worked++;
    }
    return true;
  }

  /**
   * The clumps within radius around (x, z), all of them: the camera fade thins them in the shader
   * (ClumpFadePlugin), and visible() says how many a draw call needs for it.
   */
  assemble(x: number, z: number, radius: number): Clumps {
    // Written straight into typed arrays as long as all the chunks' clumps, then cut to the radius
    const chunks: Chunk[] = [];
    this.forChunks(x, z, radius, (cx, cz) => {
      const chunk = this.chunks.get(ClumpField.key(cx, cz));
      if (chunk) {
        chunks.push(chunk);
      }
    });
    const matrices: Float32Array[] = [], colors: Float32Array[] = [], fades: Float32Array[] = [], ends: Int32Array[] = [], r2 = radius * radius;
    for (let v = 0; v < this.variants; v++) {
      let capacity = 0;
      for (const chunk of chunks) {
        capacity += chunk.lists[v].length / STRIDE;
      }
      const m = new Float32Array(capacity * 16), c = new Float32Array(capacity * 4), f = new Float32Array(capacity), end = new Int32Array(BUCKETS + 1);
      let n = 0;
      // Bucket by bucket over all chunks: the lowest draws first, so a prefix is what the fade keeps
      for (let b = 0; b < BUCKETS; b++) {
        for (const chunk of chunks) {
          const list = chunk.lists[v], to = chunk.starts[v][b + 1] * STRIDE;
          for (let i = chunk.starts[v][b] * STRIDE; i < to; i += STRIDE) {
            const px = list[i + 12], pz = list[i + 14];
            if ((px - x) * (px - x) + (pz - z) * (pz - z) > r2) {
              continue;
            }
            m.set(list.subarray(i, i + 16), n * 16);
            c.set(list.subarray(i + 16, i + 20), n * 4);
            f[n] = list[i + 20];
            n++;
          }
        }
        end[b + 1] = n;
      }
      matrices.push(m.subarray(0, n * 16));
      colors.push(c.subarray(0, n * 4));
      fades.push(f.subarray(0, n));
      ends.push(end);
    }
    return {matrices, colors, fades, ends};
  }

  /** How many of a variant's clumps (in assemble's order) can stand where the camera fade keeps at most `keep` (0 - 1). */
  static visible(ends: Int32Array, keep: number): number {
    return ends[Math.max(0, Math.min(BUCKETS, Math.ceil(keep * BUCKETS)))];
  }

  /** The tiles by their index, as prepare wants them. */
  static tilesByIndex(tiles: Iterable<BabylonTerrainTileImpl>): Map<number, any> {
    const size = BabylonTerrainTileImpl.NODE_X_COUNT, byIndex = new Map<number, any>();
    for (const tile of tiles as Iterable<any>) {
      if (tile.groundHeights?.length) {
        byIndex.set(tile.tileXOffset / size * 65536 + tile.tileYOffset / size, tile);
      }
    }
    return byIndex;
  }

  /** One chunk's clumps per variant, STRIDE floats each, sorted by the fade draw. */
  private work(tile: any, cx: number, cz: number): Chunk {
    const lists: number[][] = [];
    for (let v = 0; v < this.variants; v++) {
      lists.push([]);
    }
    const perCell = this.density() * DEVICE_DENSITY * CELL * CELL, cells = CHUNK / CELL;
    let fade = 0;
    const add: AddClump = (variant, matrix, r, g, b) => {
      const list = lists[variant];
      for (let k = 0; k < 16; k++) {
        list.push(matrix.m[k]);
      }
      list.push(r, g, b, 1, fade);
    };
    for (let j = 0; j < cells; j++) {
      for (let i = 0; i < cells; i++) {
        const gx = cx * cells + i, gz = cz * cells + j;
        const n = Math.floor(perCell + spotRandom(gx, gz, 0, this.salt)());
        for (let k = 1; k <= n; k++) {
          const rnd = spotRandom(gx, gz, k, this.salt), x = (gx + rnd()) * CELL, z = (gz + rnd()) * CELL;
          // The first draw is the camera fade's (ClumpFadePlugin), the rest the placer's
          fade = rnd();
          this.place(tile, x, z, rnd, add);
        }
      }
    }
    const sorted: Float32Array[] = [], starts: Int32Array[] = [];
    for (const list of lists) {
      const count = list.length / STRIDE, order: number[] = [];
      for (let i = 0; i < count; i++) {
        order.push(i);
      }
      order.sort((a, b) => list[a * STRIDE + 20] - list[b * STRIDE + 20]);
      const out = new Float32Array(list.length), start = new Int32Array(BUCKETS + 1);
      order.forEach((from, to) => {
        for (let k = 0; k < STRIDE; k++) {
          out[to * STRIDE + k] = list[from * STRIDE + k];
        }
        start[Math.min(BUCKETS - 1, Math.floor(list[from * STRIDE + 20] * BUCKETS)) + 1]++;
      });
      for (let b = 0; b < BUCKETS; b++) {
        start[b + 1] += start[b];
      }
      sorted.push(out);
      starts.push(start);
    }
    return {lists: sorted, starts};
  }

  private forChunks(x: number, z: number, radius: number, fn: (cx: number, cz: number) => void): void {
    for (let cz = Math.floor((z - radius) / CHUNK); cz <= Math.floor((z + radius) / CHUNK); cz++) {
      for (let cx = Math.floor((x - radius) / CHUNK); cx <= Math.floor((x + radius) / CHUNK); cx++) {
        if (ClumpField.distance(cx, cz, x, z) <= radius) {
          fn(cx, cz);
        }
      }
    }
  }

  /** From (x, z) to the nearest point of chunk (cx, cz) */
  private static distance(cx: number, cz: number, x: number, z: number): number {
    const dx = Math.max(cx * CHUNK - x, 0, x - (cx + 1) * CHUNK), dz = Math.max(cz * CHUNK - z, 0, z - (cz + 1) * CHUNK);
    return Math.hypot(dx, dz);
  }

  private static key(cx: number, cz: number): number {
    return (cx + 32768) * 65536 + (cz + 32768);
  }

  private static coords(key: number): [number, number] {
    return [Math.floor(key / 65536) - 32768, key % 65536 - 32768];
  }

  private static tileKey(cx: number, cz: number): number {
    const perTile = BabylonTerrainTileImpl.NODE_X_COUNT / CHUNK;
    return Math.floor(cx / perTile) * 65536 + Math.floor(cz / perTile);
  }
}

/** A random sequence (mulberry32) seeded from a spot: cell (gx, gz), clump i. */
function spotRandom(gx: number, gz: number, i: number, salt: number): () => number {
  let a = Math.imul(gx, 73856093) ^ Math.imul(gz, 19349663) ^ Math.imul(i + 1, 83492791) ^ Math.imul(salt, 2654435761);
  return () => {
    a = (a + 0x6D2B79F5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}
