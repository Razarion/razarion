import { Index } from "../../gwtangular/GwtAngularFacade";
import { Vector3, VertexBuffer, VertexData } from "@babylonjs/core";
import { BabylonTerrainTileImpl } from 'src/app/game/renderer/babylon-terrain-tile.impl';
import { AbstractBrush } from "./brushes/abstract-brush";

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

  onPointerDown(brush: AbstractBrush, mousePosition: Vector3) {
    this.modelTerrain(brush, mousePosition);
  }


  onPointerMove(brush: AbstractBrush, mousePosition: Vector3, buttonDown: boolean) {
    if (buttonDown) {
      this.modelTerrain(brush, mousePosition);
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

  private modelTerrain(brush: AbstractBrush, mousePosition: Vector3) {
    if (!this.positions) {
      return;
    }
    var count = 0;
    var heightSum = 0;
    for (let i = 0; i < this.positions.length; i++) {
      let oldPosition = this.positions[i];
      if (!oldPosition) {
        continue;
      }
      if (brush.isInRadius(mousePosition, oldPosition)) {
        count++;
        heightSum += oldPosition.y;
      }
    }
    var avgHeight = count === 0 ? undefined : heightSum / count;

    let changedPosition = [];
    let changed = false;
    for (let i = 0; i < this.positions.length; i++) {
      let oldPosition = this.positions[i];
      if (!oldPosition) {
        continue;
      }
      let newHeight = brush.calculateHeight(mousePosition, oldPosition, avgHeight);
      if (newHeight || newHeight === 0) {
        this.positions[i].y = newHeight;
        changed = true;
      }

      changedPosition.push(oldPosition.x);
      changedPosition.push(BabylonTerrainTileImpl.uint16ToHeight(BabylonTerrainTileImpl.heightToUnit16(this.positions[i].y)));
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
