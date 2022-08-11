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
        id: number;
        offset: Vertex;
        position: DecimalPosition;
        rotation: Vertex;
        scale: Vertex;
        terrainObjectConfigId: number;

        getId(): number {
          return this.id;
        }

        getOffset(): Vertex {
          return this.offset;
        }

        getPosition(): DecimalPosition {
          return this.position;
        }

        getRotation(): Vertex {
          return this.rotation;
        }

        getScale(): Vertex {
          return this.scale;
        }

        getTerrainObjectConfigId(): number {
          return this.terrainObjectConfigId;
        }

        setTerrainObjectConfigId(terrainObjectConfigId: number): void {
          this.terrainObjectConfigId = terrainObjectConfigId;
        }

        setPosition(position: DecimalPosition): void {
          this.position = position;
        }

        setScale(scale: Vertex): void {
          this.scale = scale;
        }

        setRotation(rotation: Vertex): void {
          this.rotation = rotation;
        }

        setOffset(offset: Vertex): void {
          this.offset = offset;
        }

      }
    } else {
      return new com.btxtech.shared.dto.TerrainObjectPosition();
    }
  }

}
