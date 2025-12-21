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

  createGroundTypeTexture(): HTMLCanvasElement {
    const factor = 1;
    const canvas = document.createElement('canvas');
    canvas.width = BabylonTerrainTileImpl.NODE_X_COUNT * factor;
    canvas.height = BabylonTerrainTileImpl.NODE_Y_COUNT * factor;
    const context = canvas.getContext('2d')!;

    context.fillStyle = "rgba(255, 0, 0, 0.5)";

    for (let y = 0; y < BabylonTerrainTileImpl.NODE_Y_COUNT; y++) {
      for (let x = 0; x < BabylonTerrainTileImpl.NODE_X_COUNT; x++) {

        const terrainType = BabylonTerrainTileImpl.setupTerrainType(this.heightMap[x][y],
          this.heightMap[x + 1][y],
          this.heightMap[x + 1][y + 1],
          this.heightMap[x][y + 1])

        if (terrainType == TerrainType.BLOCKED) {
          context.fillStyle = "rgba(255, 0, 0)";
        } else {
          context.fillStyle = "rgba(0, 0, 0)";
        }

        context.fillRect(
          x * factor,
          y * factor,
          factor,
          factor);
      }
    }
    return canvas;
  }
}
