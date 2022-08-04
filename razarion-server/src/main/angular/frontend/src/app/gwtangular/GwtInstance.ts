// @ts-nocheck

import {DecimalPosition, TerrainObjectPosition, Vertex} from "./GwtAngularFacade";
import {environment} from "../../environments/environment";

export class GwtInstance {
  static newDecimalPosition(x: number, y: number): DecimalPosition {
    if (environment.gwtMock) {
      return new class implements DecimalPosition {
        getX(): number {
          return x;
        }

        getY(): number {
          return y;
        }
      }
    } else {
      return com.btxtech.shared.datatypes.DecimalPosition.create(x, y);
    }
  }

  static newVertex(x: number, y: number, z: number): Vertex {
    if (environment.gwtMock) {
      return new class implements Vertex {
        getX(): number {
          return x;
        }

        getY(): number {
          return y;
        }

        getZ(): number {
          return z;
        }
      }
    } else {
      return com.btxtech.shared.datatypes.Vertex.create(x, y, z);
    }
  }

  static newTerrainObjectPosition(): TerrainObjectPosition {
    if (environment.gwtMock) {
      return new class implements TerrainObjectPosition {
        setTerrainObjectConfigId(terrainObjectConfigId: number): void {

        }

        setPosition(position: DecimalPosition): void {

        }

        setScale(scale: Vertex): void {

        }

        setRotation(rotation: Vertex): void {

        }

        setOffset(offset: Vertex): void {

        }

      }
    } else {
      return new com.btxtech.shared.dto.TerrainObjectPosition();
    }
  }

}
