import {Index, InputService, TerrainType, TerrainUiService} from "../../gwtangular/GwtAngularFacade";
import {Color3, Mesh, MeshBuilder, MultiMaterial, StandardMaterial, SubMesh, Texture, Vector3, VertexBuffer, VertexData} from "@babylonjs/core";
import {BabylonTerrainTileImpl} from 'src/app/game/renderer/babylon-terrain-tile.impl';
import {AbstractBrush, BrushContext} from "./brushes/abstract-brush";
import {BabylonRenderServiceAccessImpl} from "src/app/game/renderer/babylon-render-service-access-impl.service";
import {GwtInstance} from "src/app/gwtangular/GwtInstance";
import {GwtHelper} from "../../gwtangular/GwtHelper";

export class EditorTerrainTile {
  private static readonly TEXTURE_UPDATE_THROTTLE_MS = 150;
  private positions?: Vector3[];
  private babylonTerrainTileImpl: BabylonTerrainTileImpl | null = null;
  private decalMesh: Mesh | null = null;
  private decalMeshWorker: Mesh | null = null;
  private materialIndexDecalMesh: Mesh | null = null;
  private pendingTextureUpdate: number[] | null = null;
  private textureUpdateScheduled: boolean = false;

  constructor(private renderService: BabylonRenderServiceAccessImpl,
              private inputService: InputService,
              private terrainUiService: TerrainUiService,
              private index: Index) {
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
      this.babylonTerrainTileImpl.getGroundMesh().refreshBoundingInfo();
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

  isInside(position: Vector3, brushRadius: number): boolean {
    if (brushRadius <= 0) {
      return true;
    }
    const tileMinX = this.index.getX() * BabylonTerrainTileImpl.NODE_X_COUNT * BabylonTerrainTileImpl.NODE_SIZE;
    const tileMinZ = this.index.getY() * BabylonTerrainTileImpl.NODE_Y_COUNT * BabylonTerrainTileImpl.NODE_SIZE;
    const tileMaxX = tileMinX + BabylonTerrainTileImpl.NODE_X_COUNT * BabylonTerrainTileImpl.NODE_SIZE;
    const tileMaxZ = tileMinZ + BabylonTerrainTileImpl.NODE_Y_COUNT * BabylonTerrainTileImpl.NODE_SIZE;

    // Check if the brush circle/square overlaps this tile's AABB
    const closestX = Math.max(tileMinX, Math.min(position.x, tileMaxX));
    const closestZ = Math.max(tileMinZ, Math.min(position.z, tileMaxZ));
    const dx = position.x - closestX;
    const dz = position.z - closestZ;
    return (dx * dx + dz * dz) <= (brushRadius * brushRadius);
  }

  prepareContext(brushContext: BrushContext, mousePosition: Vector3) {
    if (!this.positions) {
      return;
    }

    for (let i = 0; i < this.positions.length; i++) {
      let oldPosition = this.positions[i];
      if (!oldPosition) {
        continue;
      }
      if (brushContext.isInRadius(mousePosition, oldPosition)) {
        brushContext.addSpatialHeight(oldPosition.x, oldPosition.z, oldPosition.y);
      }
    }
  }

  modelTerrain(brush: AbstractBrush, mousePosition: Vector3) {
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
      let newHeight = brush.calculateHeight(mousePosition, oldPosition);
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
      this.babylonTerrainTileImpl!.getGroundMesh().refreshBoundingInfo();
      this.scheduleTextureUpdate(changedPosition);
    }
  }

  private scheduleTextureUpdate(positions: number[]): void {
    this.pendingTextureUpdate = positions;

    if (!this.textureUpdateScheduled) {
      this.textureUpdateScheduled = true;
      setTimeout(() => {
        if (this.pendingTextureUpdate && this.babylonTerrainTileImpl) {
          this.babylonTerrainTileImpl.updateGroundTypeTexture(this.pendingTextureUpdate);
        }
        this.textureUpdateScheduled = false;
        this.pendingTextureUpdate = null;
      }, EditorTerrainTile.TEXTURE_UPDATE_THROTTLE_MS);
    }
  }

  setWireframe(wireframe: boolean) {
    if (this.babylonTerrainTileImpl) {
      const material = this.babylonTerrainTileImpl.getGroundMesh().material;
      if (material instanceof MultiMaterial) {
        material.subMaterials.forEach(subMat => {
          if (subMat) subMat.wireframe = wireframe;
        });
      } else if (material) {
        material.wireframe = wireframe;
      }
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

  public showMaterialIndex() {
    this.hideMaterialIndex();
    this.materialIndexDecalMesh = this.createMaterialIndexDecal();
  }

  public hideMaterialIndex() {
    if (this.materialIndexDecalMesh) {
      this.materialIndexDecalMesh.dispose();
    }
    this.materialIndexDecalMesh = null;
  }

  private createMaterialIndexDecal(): Mesh {
    let xOffset = BabylonTerrainTileImpl.NODE_X_COUNT / 2 + this.index.getX() * BabylonTerrainTileImpl.NODE_X_COUNT;
    let yOffset = BabylonTerrainTileImpl.NODE_Y_COUNT / 2 + this.index.getY() * BabylonTerrainTileImpl.NODE_Y_COUNT;
    var decalSize = new Vector3(BabylonTerrainTileImpl.NODE_X_COUNT, BabylonTerrainTileImpl.NODE_Y_COUNT, 100);
    var decal = MeshBuilder.CreateDecal("Material index", this.babylonTerrainTileImpl!.getGroundMesh(), {
      position: new Vector3(xOffset, 0.5, yOffset),
      normal: new Vector3(0, 1, 0),
      size: decalSize,
    });
    var decalMaterial = new StandardMaterial("materialIndexDecalMat", this.renderService.getScene());
    decalMaterial.disableLighting = true;
    const texture = this.createMaterialIndexTexture();
    texture.hasAlpha = true;
    decalMaterial.emissiveTexture = texture;
    decalMaterial.opacityTexture = texture;
    decal.material = decalMaterial;
    decal.isPickable = false;
    decal.setParent(this.babylonTerrainTileImpl!.getGroundMesh());
    return decal;
  }

  private createMaterialIndexTexture(): Texture {
    const factor = 10;
    const border = 0.3;
    const effectiveBorder = factor * border;

    const canvas = document.createElement('canvas');
    canvas.width = BabylonTerrainTileImpl.NODE_X_COUNT * factor;
    canvas.height = BabylonTerrainTileImpl.NODE_Y_COUNT * factor;

    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    var context = canvas.getContext('2d')!;
    context.translate(centerX, centerY);
    context.rotate(Math.PI / 2);
    context.translate(-centerX, -centerY);

    const MATERIAL_COLORS: { [key: number]: string } = {
      0: "rgba(0, 200, 0, 0.5)",    // GROUND - Green
      1: "rgba(0, 0, 255, 0.5)",    // UNDER_WATER - Blue
      2: "rgba(255, 165, 0, 0.5)",  // BOT - Orange
      3: "rgba(180, 0, 255, 0.5)",  // BOT_WALL - Purple
    };

    const xCount = (BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    const quadsPerRow = xCount - 1;

    const groundMesh = this.babylonTerrainTileImpl!.getGroundMesh();
    const subMeshes: SubMesh[] = groundMesh.subMeshes;

    // Build a per-quad color map from subMesh data
    const quadColors: string[] = new Array(quadsPerRow * quadsPerRow).fill("rgba(128, 128, 128, 0.5)");

    for (const subMesh of subMeshes) {
      const color = MATERIAL_COLORS[subMesh.materialIndex] || "rgba(128, 128, 128, 0.5)";
      const startQuad = subMesh.indexStart / 6;
      const quadCount = subMesh.indexCount / 6;
      for (let i = 0; i < quadCount; i++) {
        quadColors[startQuad + i] = color;
      }
    }

    // Draw quads
    for (let qi = 0; qi < quadColors.length; qi++) {
      const x = qi % quadsPerRow;
      const y = Math.floor(qi / quadsPerRow);
      context.fillStyle = quadColors[qi];
      context.fillRect(
        (x + 1) * factor - effectiveBorder * 2,
        (quadsPerRow - y) * factor - effectiveBorder * 2,
        factor - effectiveBorder * 2,
        factor - effectiveBorder * 2);
    }

    return new Texture(canvas.toDataURL(), this.renderService.getScene());
  }

  private createTerrainTypeDecal(): Mesh {
    let xOffset = BabylonTerrainTileImpl.NODE_X_COUNT / 2 + this.index.getX() * BabylonTerrainTileImpl.NODE_X_COUNT;
    let yOffset = BabylonTerrainTileImpl.NODE_Y_COUNT / 2 + this.index.getY() * BabylonTerrainTileImpl.NODE_Y_COUNT;
    var decalSize = new Vector3(BabylonTerrainTileImpl.NODE_X_COUNT, BabylonTerrainTileImpl.NODE_Y_COUNT, 100);
    var decal = MeshBuilder.CreateDecal("Terrain type", this.babylonTerrainTileImpl!.getGroundMesh(), {
      position: new Vector3(xOffset, 0.5, yOffset),
      normal: new Vector3(0, 1, 0),
      size: decalSize,
    });
    var decalMaterial = new StandardMaterial("decalMat", this.renderService.getScene());
    decalMaterial.disableLighting = true;
    const texture = this.createDynamicTexture();
    texture.hasAlpha = true;
    decalMaterial.emissiveTexture = texture;
    decalMaterial.opacityTexture = texture;
    decal.material = decalMaterial;
    decal.isPickable = false;
    decal.setParent(this.babylonTerrainTileImpl!.getGroundMesh());
    return decal
  }

  private createDynamicTexture(): Texture {
    const factor = 10;
    const border = 0.3;
    const effectiveBorder = factor * border;

    const canvas = document.createElement('canvas');
    canvas.width = BabylonTerrainTileImpl.NODE_X_COUNT * factor;
    canvas.height = BabylonTerrainTileImpl.NODE_Y_COUNT * factor;

    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    var  context = canvas.getContext('2d')!
    context.translate(centerX, centerY);
    context.rotate(Math.PI / 2);
    context.translate(-centerX, -centerY);

    this.drawMiniMap(
      context,
      true,
      factor,
      effectiveBorder,
      "rgba(0, 0, 255, 0.5)",
      "rgba(0, 255, 0, 0.5)",
      "rgba(255, 0, 0, 0.5)");
    return new Texture(canvas.toDataURL(), this.renderService.getScene());
  }

  drawMiniMap(context: CanvasRenderingContext2D, flipY: boolean, factor: number, effectiveBorder: number, waterColor: string, landColor: string, blockedColor: string) {
    let xCount = (BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    let yCount = (BabylonTerrainTileImpl.NODE_Y_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    let xNodeTile = (this.index.getX() * BabylonTerrainTileImpl.NODE_X_COUNT) * BabylonTerrainTileImpl.NODE_SIZE;
    let yNodeTile = (this.index.getY() * BabylonTerrainTileImpl.NODE_Y_COUNT) * BabylonTerrainTileImpl.NODE_SIZE;
    for (let x = 0; x < xCount; x++) {
      for (let y = 0; y < yCount; y++) {
        let terrainType = GwtHelper.gwtIssueStringEnum(
          this.terrainUiService.getTerrainType(x + xNodeTile, y + yNodeTile),
          TerrainType
        );
        switch (terrainType) {
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
          (flipY ? (yCount - y - 1) : (y + 1)) * factor - effectiveBorder * 2,
          factor - effectiveBorder * 2,
          factor - effectiveBorder * 2);
      }
    }
  }

  drawMiniMapRealistic(context: CanvasRenderingContext2D, flipY: boolean, factor: number, effectiveBorder: number) {
    let xCount = (BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    let yCount = (BabylonTerrainTileImpl.NODE_Y_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    for (let x = 0; x < xCount; x++) {
      for (let y = 0; y < yCount; y++) {
        const height = this.positions![x + y * xCount].y;
        context.fillStyle = EditorTerrainTile.heightToColor(height);
        context.fillRect(
          (x + 1) * factor - effectiveBorder * 2,
          (flipY ? (yCount - y - 1) : (y + 1)) * factor - effectiveBorder * 2,
          factor - effectiveBorder * 2,
          factor - effectiveBorder * 2);
      }
    }
  }

  static heightToColor(height: number): string {
    const rgb = EditorTerrainTile.heightToRgb(height);
    return `rgb(${rgb[0]}, ${rgb[1]}, ${rgb[2]})`;
  }

  static heightToRgb(height: number): [number, number, number] {
    if (height < BabylonTerrainTileImpl.WATER_LEVEL) {
      // Water: deeper = darker blue
      const depth = Math.min(-height, 10);
      const t = depth / 10; // 0 = surface, 1 = deep
      return [
        Math.round(30 * (1 - t)),
        Math.round(100 + 80 * (1 - t)),
        Math.round(140 + 115 * (1 - t))
      ];
    } else if (height < BabylonTerrainTileImpl.BEACH_HEIGHT) {
      // Beach/sand
      return [210, 190, 140];
    } else if (height < 3) {
      // Low land: green
      const t = (height - 0.3) / 2.7;
      return [
        Math.round(50 + 50 * t),
        Math.round(160 - 30 * t),
        Math.round(40 + 20 * t)
      ];
    } else if (height < 8) {
      // Hills: green to dark olive
      const t = (height - 3) / 5;
      return [
        Math.round(90 + 30 * t),
        Math.round(130 - 20 * t),
        Math.round(55 - 10 * t)
      ];
    } else if (height < 18) {
      // Mountains: olive to earthy gray-brown
      const t = (height - 8) / 10;
      return [
        Math.round(120 - 10 * t),
        Math.round(110 - 5 * t),
        Math.round(70 + 20 * t)
      ];
    } else if (height < 35) {
      // High mountains: gray-brown to rocky gray
      const t = (height - 18) / 17;
      return [
        Math.round(120 + 10 * t),
        Math.round(110 + 15 * t),
        Math.round(95 + 15 * t)
      ];
    } else {
      // Peaks: rocky gray to light gray
      const t = Math.min((height - 35) / 15, 1);
      return [
        Math.round(130 + 30 * t),
        Math.round(125 + 30 * t),
        Math.round(110 + 30 * t)
      ];
    }
  }

  getHeightAtNode(localX: number, localY: number): number {
    if (!this.positions) {
      return BabylonTerrainTileImpl.HEIGHT_DEFAULT;
    }
    const stride = BabylonTerrainTileImpl.NODE_X_COUNT + 1;
    const index = localX + localY * stride;
    if (index < this.positions.length) {
      return this.positions[index].y;
    }
    return BabylonTerrainTileImpl.HEIGHT_DEFAULT;
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
    decalMaterial.disableLighting = true;
    this.createDynamicTextureGameEngine(decalMaterial);
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

    let xCount = BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_SIZE;
    let yCount = BabylonTerrainTileImpl.NODE_Y_COUNT / BabylonTerrainTileImpl.NODE_SIZE;

    const xNodeOffest = this.index.getX() * xCount;
    const yNodeOffest = this.index.getY() * yCount;

    let remaining = xCount * yCount;

    for (let y = 0; y < yCount; y++) {
      for (let x = 0; x < xCount; x++) {
        this.inputService.getTerrainTypeOnTerrain(GwtInstance.newIndex(x + xNodeOffest, y + yNodeOffest))
          .then(terrainType => {
            const terrainTypeString = terrainType.toString();

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
              dynamicTexture.hasAlpha = true;
              decalMaterial.emissiveTexture = dynamicTexture;
              decalMaterial.opacityTexture = dynamicTexture;
            }

          })
          .catch(error => console.warn(error));
      }
    }
  }

}
