import {Index} from "../../gwtangular/GwtAngularFacade";
import {Vector3, VertexBuffer, VertexData} from "@babylonjs/core";
import {BabylonTerrainTileImpl} from 'src/app/game/renderer/babylon-terrain-tile.impl';
import {UpDownMode} from "./shape-terrain-editor.component";

export class EditorTerrainTile {
  private positions?: Vector3[];
  private babylonTerrainTileImpl: BabylonTerrainTileImpl | null = null;

  constructor(private index: Index) {
  }

  setBabylonTerrainTile(babylonTerrainTileImpl: BabylonTerrainTileImpl) {
    this.babylonTerrainTileImpl = babylonTerrainTileImpl;
    if (this.positions) {
      let changedPosition: number[] = [];
      this.positions.forEach(position => {
        changedPosition.push(position.x);
        changedPosition.push(position.y);
        changedPosition.push(position.z);
      });
      this.babylonTerrainTileImpl.getGroundMesh().setVerticesData(VertexBuffer.PositionKind, changedPosition);
      this.babylonTerrainTileImpl.getGroundMesh().createNormals(true);
    } else {
      this.positions = [];
      const vertexData: VertexData = VertexData.ExtractFromMesh(babylonTerrainTileImpl.getGroundMesh());
      for (let i = 0; i < vertexData.positions!.length; i += 3) {
        this.positions.push(new Vector3(
          vertexData.positions![i],
          vertexData.positions![i + 1],
          vertexData.positions![i + 2]));
      }
    }
  }

  isInside(position: Vector3): boolean {
    return true;
  }

  onPointerDown(position: Vector3, radius: number, falloff: number, height: number, random: number, upDownMode: UpDownMode) {
    this.modelTerrain(position, radius, falloff, height, random, upDownMode);
  }


  onPointerMove(position: Vector3, buttonDown: boolean, cursorSize: number, cursorFalloff: number, cursorHeight: number, random: number, upDownMode: UpDownMode) {
    if (buttonDown) {
      this.modelTerrain(position, cursorSize, cursorFalloff, cursorHeight, random, upDownMode);
    }
    // let minDistance = Number.MAX_VALUE;
    // for (let i = 0; i < this.heightMap.length; i++) {
    //   let vP = this.heightMap[i];
    //   if (!vP) continue;
    //   position.y = vP.y;
    //   let distance = Vector3.Distance(vP, position);
    //   if (distance < minDistance) {
    //     minDistance = distance;
    //   }
    // }
    // console.log(`Hit ${this.index.toString()} ${minDistance}`)
  }

  private modelTerrain(position: Vector3, radius: number, falloff: number, height: number, random: number, upDownMode: UpDownMode) {
    if (!this.positions) {
      return;
    }
    let changedPosition = [];
    let changed = false;
    for (let i = 0; i < this.positions.length; i++) {
      let oldPosition = this.positions[i];
      if (!oldPosition) {
        continue;
      }
      position.y = oldPosition.y;
      let distance = Vector3.Distance(oldPosition, position);
      if (distance < (radius + falloff)) {
        if (distance <= radius) {
          oldPosition.y = height;
        } else {
          var newValue = (height / falloff) * (falloff + radius - distance) + random * (Math.random() - 0.5) * 2.0;
          if (upDownMode === UpDownMode.DOWN) {
            if (oldPosition.y > newValue) {
              oldPosition.y = newValue;
            }
            if (oldPosition.y < height) {
              oldPosition.y = height;
            }
          } else if (upDownMode === UpDownMode.UP) {
            if (oldPosition.y < newValue) {
              oldPosition.y = newValue;
            }
            if (oldPosition.y > height) {
              oldPosition.y = height;
            }
          } else {
            oldPosition.y = newValue;
          }
        }
        // TODO if (vP.y < this._minY) {
        //   vP.y = this._minY;
        // } else if (vP.y > this._maxY) {
        //   vP.y = this._maxY;
        // }
        this.positions[i] = oldPosition;
        changed = true;
      }

      changedPosition.push(oldPosition.x);
      changedPosition.push(BabylonTerrainTileImpl.uint16ToHeight(BabylonTerrainTileImpl.heightToUnit16(oldPosition.y)));
      changedPosition.push(oldPosition.z);

    }

    if (changed) {
      this.babylonTerrainTileImpl!.getGroundMesh().setVerticesData(VertexBuffer.PositionKind, changedPosition);
      this.babylonTerrainTileImpl!.getGroundMesh().createNormals(true);
    }
  }

  setWireframe(wireframe: boolean) {
    if (this.babylonTerrainTileImpl) {
      this.babylonTerrainTileImpl.getGroundMesh().material!.wireframe = wireframe;
    }
  }

  fillHeights(callback: (height: number) => void) {
    this.positions!.forEach(position => {
      callback(BabylonTerrainTileImpl.heightToUnit16(position.y))
    });
  }

  hasPositions(): boolean {
    return !!this.positions;
  }
}
