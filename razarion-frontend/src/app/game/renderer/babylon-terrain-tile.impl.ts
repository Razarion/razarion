import {
  BabylonDecal,
  BabylonTerrainTile,
  BotGround,
  Diplomacy,
  TerrainObjectConfig,
  TerrainObjectModel,
  TerrainTile,
  TerrainTileObjectList
} from "src/app/gwtangular/GwtAngularFacade";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {BabylonModelService} from "./babylon-model.service";
import {BabylonWaterRenderService} from "./babylon-water-render.service";
import {
  ActionManager,
  ExecuteCodeAction,
  Mesh,
  MultiMaterial,
  Node,
  NodeMaterial,
  Ray,
  SubMesh,
  TransformNode,
  Vector3,
  VertexData
} from "@babylonjs/core";
import {BabylonRenderServiceAccessImpl, RazarionMetadataType} from "./babylon-render-service-access-impl.service";
import {Nullable} from "@babylonjs/core/types";
import {ActionService, SelectionInfo} from "../action.service";
import {GwtHelper} from "src/app/gwtangular/GwtHelper";
import type {AbstractMesh} from '@babylonjs/core/Meshes/abstractMesh';

enum MaterialIndex {
  GROUND = 0,
  UNDER_WATER = 1,
  BOT = 2,
  BOT_WALL = 3,
}

export class BabylonTerrainTileImpl implements BabylonTerrainTile {
  // See: GWT Java Code TerrainUtil
  static readonly NODE_X_COUNT = 160;
  static readonly NODE_Y_COUNT = 160;
  static readonly NODE_SIZE = 1;
  static readonly TILE_NODE_SIZE = this.NODE_X_COUNT * this.NODE_Y_COUNT;
  static readonly HEIGHT_PRECISION = 0.1;
  static readonly HEIGHT_MIN = -200;
  static readonly WATER_LEVEL = 0;
  static readonly WALL_HEIGHT_DIFF = 0.5;
  static readonly HEIGHT_DEFAULT = 0.5;
  static readonly BOT_BOX_LENGTH = 8;
  static readonly BOT_BOX_Z_ROTATION = 22;
  public readonly container: TransformNode;
  public readonly shadowCasterObjects: TransformNode[] = []
  private readonly groundMesh: Mesh;
  private shadowCaster?: ((mesh: AbstractMesh) => void) | null = null;
  private readonly cursorTypeHandlerTerrain: (selectionInfo: SelectionInfo) => void;
  private readonly actionManagerTerrain: ActionManager;
  private readonly cursorTypeHandlerTerrainObject: (selectionInfo: SelectionInfo) => void;
  private readonly actionManagerTerrainObject: ActionManager;

  constructor(public readonly terrainTile: TerrainTile,
              private gwtAngularService: GwtAngularService,
              private rendererService: BabylonRenderServiceAccessImpl,
              actionService: ActionService,
              private babylonModelService: BabylonModelService,
              private threeJsWaterRenderService: BabylonWaterRenderService) {
    this.container = new TransformNode(`Terrain Tile ${terrainTile.getIndex().toString()}`);

    this.actionManagerTerrain = new ActionManager(rendererService.getScene());
    this.actionManagerTerrain.registerAction(
      new ExecuteCodeAction(
        ActionManager.OnPickTrigger,
        () => {
          let pickingInfo = rendererService.setupMeshPickPoint();
          if (pickingInfo.hit) {
            actionService.onTerrainClicked(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z);
          }
        }
      )
    );
    this.cursorTypeHandlerTerrain = (selectionInfo: SelectionInfo) => {
      if (selectionInfo.hasOwnMovable) {
        this.actionManagerTerrain.hoverCursor = "url(\"cursors/go.png\") 15 15, auto"
      } else {
        this.actionManagerTerrain.hoverCursor = "default"
      }
    }
    actionService.addCursorHandler(this.cursorTypeHandlerTerrain);

    this.actionManagerTerrainObject = new ActionManager(rendererService.getScene());
    this.actionManagerTerrainObject.registerAction(
      // If no action registered, go-no curser is not shown
      new ExecuteCodeAction(
        ActionManager.OnPickDownTrigger,
        () => {
        }
      )
    );
    this.cursorTypeHandlerTerrainObject = (selectionInfo: SelectionInfo) => {
      if (selectionInfo.hasOwnMovable) {
        this.actionManagerTerrainObject.hoverCursor = "url(\"cursors/go-no.png\") 15 15, auto"
      } else {
        this.actionManagerTerrainObject.hoverCursor = "default"
      }
    }
    actionService.addCursorHandler(this.cursorTypeHandlerTerrainObject);

    this.groundMesh = new Mesh("Ground", rendererService.getScene());
    let uv2GroundHeightMap: number[] = [];
    const materialSubmeshes: {
      materialIndex: MaterialIndex,
      indexStart: number,
      indexCount: number
    }[] = [];
    let vertexData = this.createVertexData(terrainTile.getGroundHeightMap(),
      uv2GroundHeightMap,
      materialSubmeshes,
      terrainTile.getBabylonDecals());
    vertexData.applyToMesh(this.groundMesh, true);
    this.container.getChildren().push(this.groundMesh);
    this.groundMesh.receiveShadows = true;
    this.groundMesh.parent = this.container;
    this.groundMesh.actionManager = this.actionManagerTerrain;

    let groundConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(GwtHelper.gwtIssueNumber(terrainTile.getGroundConfigId()));
    let groundMaterial = <NodeMaterial>babylonModelService.getBabylonMaterial(groundConfig.getGroundBabylonMaterialId());
    let underWaterMaterial = <NodeMaterial>babylonModelService.getBabylonMaterial(groundConfig.getUnderWaterBabylonMaterialId());
    let botMaterial = <NodeMaterial>babylonModelService.getBabylonMaterial(groundConfig.getBotBabylonMaterialId());
    let botWallMaterial = <NodeMaterial>babylonModelService.getBabylonMaterial(groundConfig.getBotWallBabylonMaterialId());

    const multiMaterial = new MultiMaterial(`Ground ${groundConfig.getInternalName()}`, rendererService.getScene());
    multiMaterial.subMaterials[MaterialIndex.GROUND] = groundMaterial;
    multiMaterial.subMaterials[MaterialIndex.UNDER_WATER] = underWaterMaterial;
    multiMaterial.subMaterials[MaterialIndex.BOT] = botMaterial;
    multiMaterial.subMaterials[MaterialIndex.BOT_WALL] = botWallMaterial;

    this.groundMesh!.material = multiMaterial;

    this.groundMesh.subMeshes = [];
    const totalVertices = this.groundMesh.getTotalVertices();
    materialSubmeshes.forEach(materialSubmesh =>
      new SubMesh(materialSubmesh.materialIndex, 0, totalVertices, materialSubmesh.indexStart, materialSubmesh.indexCount, this.groundMesh));

    BabylonRenderServiceAccessImpl.setRazarionMetadataSimple(this.groundMesh, RazarionMetadataType.GROUND, undefined, terrainTile.getGroundConfigId());

    this.threeJsWaterRenderService.setup(terrainTile.getIndex(), groundConfig, this.container, uv2GroundHeightMap, this.rendererService);

    if (terrainTile.getTerrainTileObjectLists()) {
      this.setupTerrainTileObjects(terrainTile.getTerrainTileObjectLists());
    }

    const selectionInfo = actionService.setupSelectionInfo();
    this.cursorTypeHandlerTerrain(selectionInfo);
    this.cursorTypeHandlerTerrainObject(selectionInfo);

    this.setupBotGrounds(terrainTile.getBotGrounds());
  }

  private setupTerrainTileObjects(terrainTileObjectLists: TerrainTileObjectList[]): void {
    terrainTileObjectLists.forEach(terrainTileObjectList => {
      try {
        let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(terrainTileObjectList.terrainObjectConfigId);
        if (!terrainObjectConfig.getModel3DId()) {
          throw new Error(`TerrainObjectConfig has no model3DId: ${terrainObjectConfig.toString()}`);
        }
        terrainTileObjectList.terrainObjectModels.forEach(terrainObjectModel => {
          this.createTerrainObject(terrainObjectConfig, terrainObjectModel, terrainObjectConfig.getRadius() <= 0)
        });
      } catch (error) {
        console.error(terrainTileObjectList);
        console.error(error);
      }
    });
  }

  private createTerrainObject(terrainObjectConfig: TerrainObjectConfig, terrainObjectModel: TerrainObjectModel, zeroRadius: boolean): void {
    try {
      setTimeout(() => {
        let terrainObject = BabylonTerrainTileImpl.createTerrainObject(terrainObjectModel, terrainObjectConfig, this.babylonModelService, this.container);
        if (!zeroRadius && this.shadowCaster) {
          this.shadowCasterObjects.push(terrainObject);
          terrainObject.getChildMeshes().forEach(mesh => this.shadowCaster!(mesh))
        }

        if (zeroRadius) {
          terrainObject.getChildMeshes().forEach(childMesh => {
            childMesh.actionManager = this.actionManagerTerrain;
          });
          if (terrainObject.hasOwnProperty('actionManager')) {
            (<AbstractMesh>terrainObject).actionManager = this.actionManagerTerrain;
          }
        } else {
          terrainObject.getChildMeshes().forEach(childMesh => {
            childMesh.actionManager = this.actionManagerTerrainObject;
          });
          if (terrainObject.hasOwnProperty('actionManager')) {
            (<AbstractMesh>terrainObject).actionManager = this.actionManagerTerrainObject;
          }
        }
      }, 1);
    } catch (error) {
      console.error(error);
    }
  }

  private setupHeightForTerrainObject(terrainObjectModel: TerrainObjectModel): number {
    let ray = new Ray(new Vector3(terrainObjectModel.position.getX(), -100, terrainObjectModel.position.getY()), new Vector3(0, 1, 0), 1000);
    let pickingInfo = this.groundMesh.intersects(ray);
    if (pickingInfo.hit) {
      return pickingInfo.pickedPoint!.y;
    } else {
      console.warn(`TerrainObject ${terrainObjectModel.terrainObjectId} not on ground`);
      return 0;
    }
  }

  public static createTerrainObject(terrainObjectModel: TerrainObjectModel, terrainObjectConfig: TerrainObjectConfig, babylonModelService: BabylonModelService, parent: Nullable<Node>): TransformNode {
    const terrainObjectModelTransform = new TransformNode(`TerrainObject (${terrainObjectModel.terrainObjectId})`);
    BabylonRenderServiceAccessImpl.setRazarionMetadataSimple(terrainObjectModelTransform, RazarionMetadataType.TERRAIN_OBJECT, terrainObjectModel.terrainObjectId, terrainObjectConfig.getId());
    terrainObjectModelTransform.setParent(parent);
    parent?.getChildren().push(terrainObjectModelTransform);
    terrainObjectModelTransform.position.set(
      terrainObjectModel.position.getX(),
      terrainObjectModel.position.getZ(),
      terrainObjectModel.position.getY());
    if (terrainObjectModel.scale) {
      terrainObjectModelTransform.scaling.set(
        terrainObjectModel.scale.getX(),
        terrainObjectModel.scale.getZ(),
        terrainObjectModel.scale.getY());
    }
    if (terrainObjectModel.rotation) {
      terrainObjectModelTransform.rotationQuaternion = null;
      terrainObjectModelTransform.rotation.set(
        terrainObjectModel.rotation.getX(),
        terrainObjectModel.rotation.getZ(),
        terrainObjectModel.rotation.getY());
    }
    let renderObject = babylonModelService.cloneModel3D(terrainObjectConfig.getModel3DId(), terrainObjectModelTransform);
    renderObject.setName(`TerrainObject '${terrainObjectConfig.getInternalName()} (${terrainObjectConfig.getId()})'`);
    renderObject.setParent(terrainObjectModelTransform);

    return terrainObjectModelTransform;
  }

  addToScene(): void {
    this.rendererService.addTerrainTileToScene(this);
  }

  removeFromScene(): void {
    this.rendererService.removeTerrainTileFromScene(this);
    // TODO this.actionService.removeCursorHandler(this.cursorTypeHandlerTerrain);
  }

  getGroundMesh(): Mesh {
    return this.groundMesh;
  }

  private createVertexData(groundHeightMap: Uint16Array, uv2GroundHeightMap: number[], materialSubmeshes: {
    materialIndex: MaterialIndex,
    indexStart: number,
    indexCount: number
  }[], babylonDecals: BabylonDecal[]): VertexData {
    const indices = [];
    const positions = [];
    const normals = [];
    const uvs = [];
    const uvs2 = [];

    let xCount = (BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    let yCount = (BabylonTerrainTileImpl.NODE_Y_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    let xOffset = this.terrainTile.getIndex().getX() * BabylonTerrainTileImpl.NODE_X_COUNT;
    let yOffset = this.terrainTile.getIndex().getY() * BabylonTerrainTileImpl.NODE_Y_COUNT;

    // Vertices
    for (let y = 0; y < yCount; y++) {
      for (let x = 0; x < xCount; x++) {
        const index = x + y * xCount;
        const height = BabylonTerrainTileImpl.setupHeight(index, groundHeightMap);
        positions.push(
          x * BabylonTerrainTileImpl.NODE_SIZE + xOffset,
          height,
          y * BabylonTerrainTileImpl.NODE_SIZE + yOffset);
        normals.push(0, 0, 0);
        const uvx = x / xCount;
        const uvy = 1.0 - y / yCount;
        uvs.push(uvx, uvy);
        uvs2.push(uvx + uvy, ((height - 0.5) / 1.5) * 0.05);

        const invertedY = xCount - y - 1;
        const index2 = x + invertedY * xCount;
        const invertedGroundHeight = BabylonTerrainTileImpl.setupHeight(index2, groundHeightMap);
        uv2GroundHeightMap.push(invertedGroundHeight, 0);
      }
    }

    // Indices
    let currentStart = 0;
    let indexCount = 0;
    let materialIndex: MaterialIndex | null = null;

    let materialSubmesh: {
      materialIndex: MaterialIndex,
      indexStart: number,
      indexCount: number
    } | null = null;

    for (let y = 0; y < yCount - 1; y++) {
      for (let x = 0; x < xCount - 1; x++) {
        const bLIdx = x + y * xCount;
        const bRIdx = x + 1 + y * xCount;
        const tLIdx = x + (y + 1) * xCount;
        const tRIdx = x + 1 + (y + 1) * xCount;

        const terrainX = x * BabylonTerrainTileImpl.NODE_SIZE + xOffset;
        const terrainY = y * BabylonTerrainTileImpl.NODE_SIZE + yOffset;

        const bLHeight = BabylonTerrainTileImpl.setupHeight(bLIdx, groundHeightMap);
        const bRHeight = BabylonTerrainTileImpl.setupHeight(bRIdx, groundHeightMap);
        const tLHeight = BabylonTerrainTileImpl.setupHeight(tLIdx, groundHeightMap);
        const tRHeight = BabylonTerrainTileImpl.setupHeight(tRIdx, groundHeightMap);

        const decal = babylonDecals && babylonDecals.find(babylonDecal => {
          return terrainX >= babylonDecal.xPos
            && terrainX < babylonDecal.xSize + babylonDecal.xPos
            && terrainY >= babylonDecal.yPos
            && terrainY < babylonDecal.ySize + babylonDecal.yPos;
        })

        let newMaterialIndex;
        if (decal) {
          const maxHeight = Math.max(bLHeight, bRHeight, tRHeight, tLHeight);
          const minHeight = Math.min(bLHeight, bRHeight, tRHeight, tLHeight);
          if (Math.abs(maxHeight - minHeight) < BabylonTerrainTileImpl.WALL_HEIGHT_DIFF) {
            newMaterialIndex = MaterialIndex.BOT;
          } else {
            newMaterialIndex = MaterialIndex.BOT_WALL;
          }
        } else if (bLHeight <= 0.0
          && bRHeight <= 0.0
          && tLHeight <= 0.0
          && tRHeight <= 0.0) {
          newMaterialIndex = MaterialIndex.UNDER_WATER;
        } else {
          newMaterialIndex = MaterialIndex.GROUND;
        }
        if (materialIndex !== newMaterialIndex) {
          if (materialSubmesh != null) {
            materialSubmesh.indexCount = indexCount;
            materialSubmeshes.push(materialSubmesh)
          }
          materialSubmesh = {
            materialIndex: newMaterialIndex, indexStart: currentStart, indexCount: 0
          }
          indexCount = 0;
        }
        materialIndex = newMaterialIndex;

        indices.push(bLIdx);
        indices.push(bRIdx);
        indices.push(tLIdx);

        indices.push(bRIdx);
        indices.push(tRIdx);
        indices.push(tLIdx);

        currentStart += 6;
        indexCount += 6;
      }
    }
    if (materialSubmesh) {
      materialSubmesh.indexCount = indexCount;
      materialSubmeshes.push(materialSubmesh)
    }

    VertexData.ComputeNormals(positions, indices, normals);

    const vertexData = new VertexData();

    vertexData.indices = indices;
    vertexData.positions = positions;
    vertexData.normals = normals;
    vertexData.uvs = uvs;
    vertexData.uvs2 = uvs2;

    return vertexData;
  }

  public static setupHeight(index: number, groundHeightMap: Uint16Array): number {
    if (!groundHeightMap || groundHeightMap[index] === undefined) {
      return BabylonTerrainTileImpl.HEIGHT_DEFAULT;
    } else {
      const uin16Height = groundHeightMap && groundHeightMap[index] || 0;
      return BabylonTerrainTileImpl.uint16ToHeight(uin16Height);
    }
  }


  // See: GWT Java Code TerrainUtil.uint16ToHeight
  public static uint16ToHeight(uint16: number): number {
    return uint16 * BabylonTerrainTileImpl.HEIGHT_PRECISION + BabylonTerrainTileImpl.HEIGHT_MIN;
  }

  // See: GWT Java Code TerrainUtil.heightToUnit16
  public static heightToUnit16(height: number): number {
    let value = (height - BabylonTerrainTileImpl.HEIGHT_MIN) / BabylonTerrainTileImpl.HEIGHT_PRECISION;
    return Math.round(value * 10) / 10
  }

  addShadowCasters(shadowCaster: (mesh: AbstractMesh) => void) {
    this.shadowCasterObjects.forEach(node => {
      node.getChildMeshes().forEach(mesh => shadowCaster(mesh))
    });
    this.shadowCaster = shadowCaster;
  }

  removeShadowCasters(shadowCaster: (mesh: AbstractMesh) => void) {
    this.shadowCasterObjects.forEach(node => {
      node.getChildMeshes().forEach(mesh => shadowCaster(mesh))
    });
    this.shadowCaster = null;
  }

  private setupBotGrounds(botGrounds: BotGround[]) {
    if (!botGrounds) {
      return;
    }
    botGrounds.forEach((botGround: BotGround) => {
      let botGroundNorm = new Vector3(0, 1, 0).normalize();
      botGround.positions.forEach((position) => {
        const renderObject = this.babylonModelService.cloneModel3D(botGround.model3DId, this.container, Diplomacy.OWN);
        renderObject.setPosition(new Vector3(position.getX(), botGround.height, position.getY()));
        renderObject.setMetadata({
          type: RazarionMetadataType.BOT_GROUND,
          configId: botGround.model3DId,
          id: undefined,
          editorHintTerrainObjectPosition: undefined,
          botGroundNorm: botGroundNorm
        });
        renderObject.setActionManager(this.actionManagerTerrain);
      });
      if (botGround.botGroundSlopeBoxes) {
        botGround.botGroundSlopeBoxes.forEach(botGroundSlopeBox => {
          const x = -Math.sin(botGroundSlopeBox.zRot) * Math.cos(botGroundSlopeBox.yRot);
          const y = Math.cos(botGroundSlopeBox.zRot);
          const z = Math.sin(botGroundSlopeBox.zRot) * Math.sin(botGroundSlopeBox.yRot);
          let botGroundNorm = new Vector3(x, y, z).normalize();

          const renderObject = this.babylonModelService.cloneModel3D(botGround.model3DId, this.container, Diplomacy.OWN);
          renderObject.prefixName("Slope ");
          renderObject.setPosition(new Vector3(botGroundSlopeBox.xPos, botGroundSlopeBox.height, botGroundSlopeBox.yPos));

          renderObject.setRotationYZ(botGroundSlopeBox.yRot, botGroundSlopeBox.zRot);
          renderObject.setMetadata({
            type: RazarionMetadataType.BOT_GROUND,
            configId: botGround.model3DId,
            id: undefined,
            editorHintTerrainObjectPosition: undefined,
            botGroundNorm: botGroundNorm
          });
          renderObject.setActionManager(this.actionManagerTerrain);
        })
      }
    });
  }
}
