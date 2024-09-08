import { Index, InputService, TerrainTile } from "../../gwtangular/GwtAngularFacade";
import { Color3, Mesh, MeshBuilder, StandardMaterial, Texture, Vector3, VertexBuffer, VertexData } from "@babylonjs/core";
import { BabylonTerrainTileImpl } from 'src/app/game/renderer/babylon-terrain-tile.impl';
import { AbstractBrush } from "./brushes/abstract-brush";
import { BabylonRenderServiceAccessImpl } from "src/app/game/renderer/babylon-render-service-access-impl.service";
import { GwtInstance } from "src/app/gwtangular/GwtInstance";
import { TerrainType } from "src/app/generated/razarion-share";

export class EditorTerrainTile {
  private positions?: Vector3[];
  private babylonTerrainTileImpl: BabylonTerrainTileImpl | null = null;
  private decalMesh: Mesh | null = null;
  private decalMeshWorker: Mesh | null = null;

  constructor(private renderService: BabylonRenderServiceAccessImpl, private inputeService: InputService, private index: Index) {
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
    let index = 0;
    this.positions!.forEach(position => {
      const xNode = index % (BabylonTerrainTileImpl.NODE_X_COUNT + 1);
      const yNode = Math.floor(index / (BabylonTerrainTileImpl.NODE_Y_COUNT + 1))
      if (xNode < BabylonTerrainTileImpl.NODE_X_COUNT && yNode < BabylonTerrainTileImpl.NODE_Y_COUNT) {
        callback(BabylonTerrainTileImpl.heightToUnit16(position.y));
      }
      index++;
    });
  }

  hasPositions(): boolean {
    return !!this.positions;
  }

  public showTerrainType() {
    this.hideTerrainType();
    this.decalMesh = this.createTerrainTypeDecal();
  }

  public hideTerrainType() {
    if (this.decalMesh) {
      this.decalMesh.dispose();
    }
    this.decalMesh = null;
  }

  public showTerrainTypeWorker() {
    if (this.decalMeshWorker) {
      this.decalMeshWorker.setEnabled(true);
      return;
    }
    this.decalMeshWorker = this.createTerrainTypeDecalWorker()
  }

  public hideTerrainTypeWorker() {
    if (this.decalMeshWorker) {
      this.decalMeshWorker.setEnabled(false);
    }
  }

  private createTerrainTypeDecal(): Mesh {
    let xOffset = BabylonTerrainTileImpl.NODE_X_COUNT / 2 + this.index.getX() * BabylonTerrainTileImpl.NODE_X_COUNT;
    let yOffset = BabylonTerrainTileImpl.NODE_Y_COUNT / 2 + this.index.getY() * BabylonTerrainTileImpl.NODE_Y_COUNT;
    var decalSize = new Vector3(BabylonTerrainTileImpl.NODE_X_COUNT, BabylonTerrainTileImpl.NODE_Y_COUNT, 100);
    var decal = MeshBuilder.CreateDecal("Terrain type", this.babylonTerrainTileImpl!.getGroundMesh(), {
      position: new Vector3(xOffset, 0.5, yOffset), normal: new Vector3(0, 1, 0), size: decalSize
    });
    var decalMaterial = new StandardMaterial("decalMat", this.renderService.getScene());
    decalMaterial.diffuseTexture = this.createDynamicTexture();
    decalMaterial.diffuseTexture.hasAlpha = true;
    decalMaterial.specularColor = new Color3(0, 0, 0)
    decal.material = decalMaterial;
    decal.isPickable = false;
    return decal
  }

  private createDynamicTexture(): Texture {
    const factor = 10;
    const border = 0.3;
    const effectiveBorder = factor * border;

    const canvas = document.createElement('canvas');
    canvas.width = BabylonTerrainTileImpl.NODE_X_COUNT * factor;
    canvas.height = BabylonTerrainTileImpl.NODE_Y_COUNT * factor;
    this.drawMiniMap(
      canvas.getContext('2d')!,
      factor,
      effectiveBorder,
      "rgba(0, 0, 255, 0.5)",
      "rgba(0, 255, 0, 0.5)",
      "rgba(255, 0, 0, 0.5)");
    const dynamicTexture = new Texture(canvas.toDataURL(), this.renderService.getScene());
    return dynamicTexture;
  }

  drawMiniMap(context: CanvasRenderingContext2D, factor: number, effectiveBorder: number, waterColor: string, landColor: string, blockedColor: string) {
    let xCount = (BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_X_DISTANCE) + 1;
    let yCount = (BabylonTerrainTileImpl.NODE_Y_COUNT / BabylonTerrainTileImpl.NODE_Y_DISTANCE) + 1; 
    for (let y = 0; y < yCount - 1; y++) {
      for (let x = 0; x < xCount - 1; x++) {
        const blHeight = this.positions![x + y * xCount].y;
        const brHeight = this.positions![x + 1 + y * xCount].y;
        const tlHeight = this.positions![x + (y + 1) * xCount].y;
        const trHeight = this.positions![x + 1 + (y + 1) * xCount].y;

        switch (EditorTerrainTile.setupTerrainType(blHeight, brHeight, trHeight, tlHeight)) {
          case TerrainType.WATER:
            context.fillStyle = waterColor;
            break;
          case TerrainType.LAND:
            context.fillStyle = landColor;
            break;
          case TerrainType.BLOCKED:
            context.fillStyle = blockedColor;
            break;
          default:
            context.fillStyle = "rgba(1, 1, 1, 1)";
        }
        context.fillRect(
          (x + 1) * factor - effectiveBorder * 2,
          (yCount - y + 1) * factor - effectiveBorder * 2,
          factor - effectiveBorder * 2,
          factor - effectiveBorder * 2);
      }
    }
  }

  public static setupTerrainType(blHeight: number, brHeight: number, trHeight: number, tlHeight: number): TerrainType {
    const avgHeight = (blHeight + brHeight + trHeight + tlHeight) / 4.0;

    if (avgHeight < BabylonTerrainTileImpl.WATER_LEVEL) {
      return TerrainType.WATER;
    } else {
      const maxHeight = Math.max(blHeight, brHeight, trHeight, tlHeight);
      const minHeight = Math.min(blHeight, brHeight, trHeight, tlHeight);
      // if (Math.abs(maxHeight - minHeight) < 0.6999998) {
      if (Math.abs(maxHeight - minHeight) < 0.7) {
        return TerrainType.LAND;
      } else {
        return TerrainType.BLOCKED;
      }
    }

  }

  private createTerrainTypeDecalWorker(): Mesh {
    let xOffset = BabylonTerrainTileImpl.NODE_X_COUNT / 2 + this.index.getX() * BabylonTerrainTileImpl.NODE_X_COUNT;
    let yOffset = BabylonTerrainTileImpl.NODE_Y_COUNT / 2 + this.index.getY() * BabylonTerrainTileImpl.NODE_Y_COUNT;
    var decalSize = new Vector3(BabylonTerrainTileImpl.NODE_X_COUNT, BabylonTerrainTileImpl.NODE_Y_COUNT, 100);
    var decal = MeshBuilder.CreateDecal("Terrain type", this.babylonTerrainTileImpl!.getGroundMesh(), {
      position: new Vector3(xOffset, 0.5, yOffset), normal: new Vector3(0, 1, 0), size: decalSize
    });
    var decalMaterial = new StandardMaterial("decalMat", this.renderService.getScene());
    this.createDynamicTextureGameEngine(decalMaterial)
    decalMaterial.specularColor = new Color3(0, 0, 0)
    decal.material = decalMaterial;
    decal.isPickable = false;
    return decal
  }

  private createDynamicTextureGameEngine(decalMaterial: StandardMaterial) {
    const factor = 10;
    const border = 0.3;
    const effectiveBorder = factor * border;
    const canvas = document.createElement('canvas');
    canvas.width = BabylonTerrainTileImpl.NODE_X_COUNT * factor;
    canvas.height = BabylonTerrainTileImpl.NODE_Y_COUNT * factor;
    const context = canvas.getContext('2d')!;

    let xCount = BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_X_DISTANCE;
    let yCount = BabylonTerrainTileImpl.NODE_Y_COUNT / BabylonTerrainTileImpl.NODE_Y_DISTANCE;

    const xNodeOffest = this.index.getX() * xCount;
    const yNodeOffest = this.index.getY() * yCount;

    let remaining = xCount * yCount;

    for (let y = 0; y < yCount; y++) {
      for (let x = 0; x < xCount; x++) {
        this.inputeService.getTerrainTypeOnTerrain(GwtInstance.newIndex(x + xNodeOffest, y + yNodeOffest))
          .then(terrainType => {
            const terrainTypeString = terrainType.d // Ugly gwt enum hack

            if (TerrainType.WATER == terrainTypeString) {
              context.fillStyle = "rgba(0, 0, 255, 0.5)";
            } else if (TerrainType.LAND == terrainTypeString) {
              context.fillStyle = "rgba(0, 255, 0, 0.5)";
            } else {
              context.fillStyle = "rgba(255, 0, 0, 0.5)";
            }
            context.fillRect(
              (y + 1) * factor - effectiveBorder * 2,
              (x + 1) * factor - effectiveBorder * 2,
              factor - effectiveBorder * 2,
              factor - effectiveBorder * 2);

            remaining--;
            if (remaining === 0) {
              const dynamicTexture = new Texture(canvas.toDataURL(), this.renderService.getScene());
              decalMaterial.diffuseTexture = dynamicTexture;
              decalMaterial.diffuseTexture.hasAlpha = true;
            }

          })
          .catch(error => console.warn(error));
      }
    }
  }

}
