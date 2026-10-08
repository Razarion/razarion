import {initPerm, scatterNoise, SEED} from "./procedural-textures";

/**
 * Ground under plants: greener and darker, so a palm or a bush stands in its own patch of growth
 * instead of on bare sand. Goes into the blue channel of the GroundUtility texture.
 *
 * Plants come from the tile's terrain objects as [x, z, radius, isPlant] quadruples. Each plant
 * reaches about 1.2 m plus 2.5 times its radius (a palm ~4 m, a fern ~2.5 m); overlapping reaches
 * merge, so a grove gets one patch. Only plants of this tile count - a plant just across the tile
 * edge does not green this side of it.
 */
export function createVegetationMask(x0: number, y0: number, size: number, anchors: number[]): Float32Array | null {
  let mask: Float32Array | null = null;
  initPerm(SEED);
  for (let a = 0; a < anchors.length; a += 4) {
    if (!anchors[a + 3]) {
      continue;
    }
    const ax = anchors[a] - x0, ay = anchors[a + 1] - y0;
    const reach = 1.2 + anchors[a + 2] * 2.5;
    const minX = Math.max(0, Math.floor(ax - reach - 1)), maxX = Math.min(size - 1, Math.ceil(ax + reach + 1));
    const minY = Math.max(0, Math.floor(ay - reach - 1)), maxY = Math.min(size - 1, Math.ceil(ay + reach + 1));
    for (let y = minY; y <= maxY; y++) {
      for (let x = minX; x <= maxX; x++) {
        const d = Math.hypot(x + 0.5 - ax, y + 0.5 - ay);
        // Ragged edge, so patches do not read as circles
        const ragged = d + scatterNoise((x0 + x) / 2, (y0 + y) / 2) * 1.6;
        const t = Math.max(0, Math.min(1, (reach - ragged) / (reach * 0.6)));
        const strength = t * t * (3 - 2 * t);
        if (strength > 0) {
          mask ??= new Float32Array(size * size);
          const i = y * size + x;
          if (strength > mask[i]) {
            mask[i] = strength;
          }
        }
      }
    }
  }
  return mask;
}
