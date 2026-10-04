import {BabylonTerrainTileImpl} from './babylon-terrain-tile.impl';
import {TerrainType} from '../../gwtangular/GwtAngularFacade';

export class GroundUtil {
  private heightMap: number[][] = [];

  addHeightAt(height: number, x: number, y: number) {
    if (!this.heightMap[x]) {
      this.heightMap[x] = [];
    }
    this.heightMap[x][y] = height;
  }

  /**
   * One texel per metre cell. Red: blocked (the shader shows rock). Green: path strength from
   * GroundPaths (trodden earth), or 0 where there is no path.
   */
  createGroundTypeTexture(pathMask: Float32Array | null): HTMLCanvasElement {
    const width = BabylonTerrainTileImpl.NODE_X_COUNT;
    const height = BabylonTerrainTileImpl.NODE_Y_COUNT;
    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;
    const context = canvas.getContext('2d')!;
    const image = context.createImageData(width, height);

    for (let y = 0; y < height; y++) {
      for (let x = 0; x < width; x++) {
        const terrainType = BabylonTerrainTileImpl.setupTerrainType(this.heightMap[x][y],
          this.heightMap[x + 1][y],
          this.heightMap[x + 1][y + 1],
          this.heightMap[x][y + 1])
        const i = (y * width + x) * 4;
        image.data[i] = terrainType == TerrainType.BLOCKED ? 255 : 0;
        image.data[i + 1] = pathMask ? Math.round(pathMask[y * width + x] * 255) : 0;
        image.data[i + 2] = 0;
        image.data[i + 3] = 255;
      }
    }
    context.putImageData(image, 0, 0);
    return canvas;
  }
}
