import {computeTileGround, TileGroundInput, TileGroundOutput} from "./tile-ground-compute";
import {GroundPath, GroundPaths} from "./ground-paths";
import {BotGroundFootprint, BotGroundMarkingsPixels, computeBotGroundMarkings} from "./bot-ground-markings-compute";

export type TerrainHelperRequest = {
  id: number;
  /** Sent with the first request after the paths changed; the worker keeps them. */
  paths?: GroundPath[];
} & ({ kind: 'ground', input: TileGroundInput } | { kind: 'markings', boxes: BotGroundFootprint[] });

export interface TerrainHelperResponse {
  id: number;
  output?: TileGroundOutput | BotGroundMarkingsPixels;
  error?: string;
}

interface Pending {
  inline: () => unknown;
  resolve: (output: any) => void;
  reject: (error: unknown) => void;
}

/**
 * Runs the terrain arithmetic that needs neither Babylon nor the engine bridge in a worker: a tile's
 * ground (tile-ground-compute.ts) and the markings of a bot ground (bot-ground-markings-compute.ts).
 * On the main thread they took 25-45 ms per tile on a desktop after the shore and path grids, and
 * 54-71 ms more per bot ground (2026-10-11), four to eight times that on a phone - while the tile
 * appeared.
 * <p>
 * Never fails for want of a worker: without one (no Worker, a script that does not load, an error
 * inside) the same function runs inline, as it did before. Inputs are copied rather than transferred
 * so they are still there for that. The bot ground logo needs a canvas, in the worker an
 * OffscreenCanvas: without one the markings stay inline, where there is a DOM canvas.
 */
export class TerrainHelper {
  // undefined: not tried yet, null: no worker - compute inline
  private static worker: Worker | null | undefined;
  private static nextId = 1;
  private static pending = new Map<number, Pending>();
  private static pathsSent: GroundPath[] | null = null;

  static computeGround(input: TileGroundInput): Promise<TileGroundOutput> {
    return TerrainHelper.run({kind: 'ground', input}, () => computeTileGround(input, GroundPaths.getPaths()));
  }

  static computeBotGroundMarkings(boxes: BotGroundFootprint[]): Promise<BotGroundMarkingsPixels> {
    const inline = () => computeBotGroundMarkings(boxes);
    if (typeof OffscreenCanvas === 'undefined') {
      return TerrainHelper.runInline(inline);
    }
    return TerrainHelper.run({kind: 'markings', boxes}, inline);
  }

  private static run<T>(request: { kind: 'ground', input: TileGroundInput } | { kind: 'markings', boxes: BotGroundFootprint[] },
                        inline: () => T): Promise<T> {
    const worker = TerrainHelper.getWorker();
    if (!worker) {
      return TerrainHelper.runInline(inline);
    }
    const message: TerrainHelperRequest = {id: TerrainHelper.nextId++, ...request};
    const paths = GroundPaths.getPaths();
    if (paths !== TerrainHelper.pathsSent) {
      message.paths = paths;
      TerrainHelper.pathsSent = paths;
    }
    return new Promise<T>((resolve, reject) => {
      TerrainHelper.pending.set(message.id, {inline, resolve, reject});
      worker.postMessage(message);
    });
  }

  private static runInline<T>(inline: () => T): Promise<T> {
    try {
      return Promise.resolve(inline());
    } catch (error) {
      return Promise.reject(error);
    }
  }

  private static getWorker(): Worker | null {
    if (TerrainHelper.worker !== undefined) {
      return TerrainHelper.worker;
    }
    TerrainHelper.worker = null;
    if (typeof Worker === 'undefined') {
      return null;
    }
    try {
      const worker = new Worker(new URL('./terrain-helper.worker', import.meta.url), {type: 'module', name: 'TerrainHelper'});
      worker.onmessage = (event: MessageEvent<TerrainHelperResponse>) => TerrainHelper.onResponse(event.data);
      worker.onerror = event => {
        console.warn("[Razarion] terrain helper worker failed, computing tiles inline", event.message);
        TerrainHelper.giveUpWorker();
      };
      TerrainHelper.worker = worker;
    } catch (error) {
      console.warn("[Razarion] no terrain helper worker, computing tiles inline", error);
    }
    return TerrainHelper.worker;
  }

  private static onResponse(response: TerrainHelperResponse): void {
    const entry = TerrainHelper.pending.get(response.id);
    if (!entry) {
      return;
    }
    TerrainHelper.pending.delete(response.id);
    if (response.output) {
      entry.resolve(response.output);
    } else {
      console.warn("[Razarion] terrain helper worker:", response.error);
      TerrainHelper.runInline(entry.inline).then(entry.resolve, entry.reject);
    }
  }

  /** The worker is gone: everything it still owes is computed here, everything after it too. */
  private static giveUpWorker(): void {
    TerrainHelper.worker?.terminate();
    TerrainHelper.worker = null;
    const owed = [...TerrainHelper.pending.values()];
    TerrainHelper.pending.clear();
    owed.forEach(entry => TerrainHelper.runInline(entry.inline).then(entry.resolve, entry.reject));
  }
}
