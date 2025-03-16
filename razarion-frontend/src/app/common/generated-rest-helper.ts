import {DecimalPosition, TerrainObjectPosition, Vertex} from "../generated/razarion-share";

export class GeneratedRestHelper {
  static newDecimalPosition(x: number, y: number): DecimalPosition {
    return new class implements DecimalPosition {
      x = x;
      y = y;
    };
  }

  static newVertex(x: number, y: number, z: number): Vertex {
    return new class implements Vertex {
      x = x;
      y = y;
      z = z;
    }
  }

  static newTerrainObjectPosition(): TerrainObjectPosition {
    return new class implements TerrainObjectPosition {
      id=-987654321
      terrainObjectConfigId=-123456789
      position=GeneratedRestHelper.newDecimalPosition(0,0);
      scale=null;
      rotation= null;
      offset= null;

    }
  }
}
