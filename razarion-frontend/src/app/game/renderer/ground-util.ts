import {BabylonTerrainTileImpl} from './babylon-terrain-tile.impl';
import {RawTexture} from '@babylonjs/core/Materials/Textures/rawTexture';
import {Texture} from '@babylonjs/core/Materials/Textures/texture';
import type {Scene} from '@babylonjs/core/scene';
import {groundTypePixels} from './tile-ground-compute';

export class GroundUtil {
  private heightMap: number[][] = [];

  addHeightAt(height: number, x: number, y: number) {
    if (!this.heightMap[x]) {
      this.heightMap[x] = [];
    }
    this.heightMap[x][y] = height;
  }

  /**
   * The ground type texture of the heights added so far - see groundTypePixels (tile-ground-compute.ts)
   * for the channels. Used by the editor; a streamed tile gets its texels from the terrain helper.
   */
  createGroundTypeTexture(scene: Scene, pathMask: Float32Array | null, vegetationMask: Float32Array | null = null): Texture {
    return GroundUtil.textureFromPixels(scene, this.createGroundTypePixels(pathMask, vegetationMask));
  }

  /** The texels of createGroundTypeTexture, RGBA, row 0 first. */
  createGroundTypePixels(pathMask: Float32Array | null, vegetationMask: Float32Array | null = null): Uint8Array {
    const size = BabylonTerrainTileImpl.NODE_X_COUNT;
    const nodes = size + 1;
    const heights = new Float64Array(nodes * nodes);
    for (let y = 0; y < nodes; y++) {
      for (let x = 0; x < nodes; x++) {
        heights[y * nodes + x] = this.heightMap[x][y];
      }
    }
    return groundTypePixels(heights, size, pathMask, vegetationMask);
  }

  /**
   * Straight from the bytes. It used to be painted into a canvas, encoded to a PNG with toDataURL()
   * and decoded again by the Texture - 10-19 ms per tile on the main thread (2026-10-11) for a
   * round trip that changes nothing: alpha is always 255, so nothing is premultiplied on the way.
   * invertY as the image Texture had it, so row 0 still lands where it did.
   */
  static textureFromPixels(scene: Scene, pixels: Uint8Array): Texture {
    return RawTexture.CreateRGBATexture(pixels, BabylonTerrainTileImpl.NODE_X_COUNT, BabylonTerrainTileImpl.NODE_Y_COUNT,
      scene, true, true, Texture.TRILINEAR_SAMPLINGMODE);
  }
}
