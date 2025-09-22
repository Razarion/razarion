import {
  BabylonDecal,
  BabylonTerrainTile,
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
  static readonly HEIGHT_DEFAULT = 0.5;
  public readonly container: TransformNode;
  public readonly shadowCasterObjects: TransformNode[] = []
  private shadowCaster?: ((mesh: AbstractMesh) => void) | null = null;
  private readonly cursorTypeHandler: (selectionInfo: SelectionInfo) => void;
  private readonly groundMesh: Mesh;

  constructor(public readonly terrainTile: TerrainTile,
              private gwtAngularService: GwtAngularService,
              private rendererService: BabylonRenderServiceAccessImpl,
              actionService: ActionService,
              private babylonModelService: BabylonModelService,
              private threeJsWaterRenderService: BabylonWaterRenderService) {
    this.container = new TransformNode(`Terrain Tile ${terrainTile.getIndex().toString()}`);

    let actionManager = new ActionManager(rendererService.getScene());
    actionManager.registerAction(
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
    this.cursorTypeHandler = (selectionInfo: SelectionInfo) => {
      if (selectionInfo.hasOwnMovable) {
        actionManager.hoverCursor = "url(\"cursors/go.png\") 15 15, auto"
      } else {
        actionManager.hoverCursor = "default"
      }
    }
    actionService.addCursorHandler(this.cursorTypeHandler);

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
    this.groundMesh.actionManager = actionManager;

    let groundConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(GwtHelper.gwtIssueNumber(terrainTile.getGroundConfigId()));
    let groundMaterial = <NodeMaterial>babylonModelService.getBabylonMaterial(groundConfig.getGroundBabylonMaterialId());
    groundMaterial = groundMaterial!.clone(`Clone of ${groundMaterial.name} `);

    let underWaterMaterial = <NodeMaterial>babylonModelService.getBabylonMaterial(groundConfig.getUnderWaterBabylonMaterialId());
    underWaterMaterial = underWaterMaterial!.clone(`Clone of ${underWaterMaterial.name} `);

    let botMaterial = <NodeMaterial>babylonModelService.getBabylonMaterial(groundConfig.getBotBabylonMaterialId());
    botMaterial = botMaterial!.clone(`Clone of ${botMaterial.name} `);

    const multiMaterial = new MultiMaterial("multi", rendererService.getScene());
    multiMaterial.subMaterials[MaterialIndex.GROUND] = groundMaterial;
    multiMaterial.subMaterials[MaterialIndex.UNDER_WATER] = underWaterMaterial;
    multiMaterial.subMaterials[MaterialIndex.BOT] = botMaterial;

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

    this.cursorTypeHandler(actionService.setupSelectionInfo());
  }

  private setupTerrainTileObjects(terrainTileObjectLists: TerrainTileObjectList[]): void {
    terrainTileObjectLists.forEach(terrainTileObjectList => {
      try {
        let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(terrainTileObjectList.terrainObjectConfigId);
        if (!terrainObjectConfig.getModel3DId()) {
          throw new Error(`TerrainObjectConfig has no model3DId: ${terrainObjectConfig.toString()}`);
        }
        terrainTileObjectList.terrainObjectModels.forEach(terrainObjectModel => {
          this.createTerrainObject(terrainObjectConfig, terrainObjectModel, terrainObjectConfig.getRadius() > 0)
        });
      } catch (error) {
        console.error(terrainTileObjectList);
        console.error(error);
      }
    });
  }

  private createTerrainObject(terrainObjectConfig: TerrainObjectConfig, terrainObjectModel: TerrainObjectModel, castShadow: boolean): void {
    try {
      setTimeout(() => {
        let terrainObject = BabylonTerrainTileImpl.createTerrainObject(terrainObjectModel, terrainObjectConfig, this.babylonModelService, this.container);
        // if (!castShadow) {
        //   terrainObject.getChildMeshes(false).forEach(abstractMesh => abstractMesh.isPickable = false)
        // }
        if (castShadow && this.shadowCaster) {
          this.shadowCasterObjects.push(terrainObject);
          terrainObject.getChildMeshes().forEach(mesh => this.shadowCaster!(mesh))
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
    let terrainObjectMesh;
    terrainObjectMesh = babylonModelService.cloneModel3D(terrainObjectConfig.getModel3DId(), terrainObjectModelTransform)
    terrainObjectMesh.name = `TerrainObject '${terrainObjectConfig.getInternalName()} (${terrainObjectConfig.getId()})'`;
    terrainObjectMesh.parent = terrainObjectModelTransform;

    return terrainObjectModelTransform;
  }

  addToScene(): void {
    this.rendererService.addTerrainTileToScene(this);
  }

  removeFromScene(): void {
    this.rendererService.removeTerrainTileFromScene(this);
    // TODO this.actionService.removeCursorHandler(this.cursorTypeHandler);
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

    let xCount = (BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    let yCount = (BabylonTerrainTileImpl.NODE_Y_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    let xOffset = this.terrainTile.getIndex().getX() * BabylonTerrainTileImpl.NODE_X_COUNT;
    let yOffset = this.terrainTile.getIndex().getY() * BabylonTerrainTileImpl.NODE_Y_COUNT;

    // Vertices
    for (let y = 0; y < yCount; y++) {
      for (let x = 0; x < xCount; x++) {
        const index = x + y * xCount;
        positions.push(
          x * BabylonTerrainTileImpl.NODE_SIZE + xOffset,
          BabylonTerrainTileImpl.setupHeight(index, groundHeightMap),
          y * BabylonTerrainTileImpl.NODE_SIZE + yOffset);
        normals.push(0, 0, 0);
        uvs.push(x / xCount, 1.0 - y / yCount);


        const invertedY = xCount - y - 1;
        const index2 = x + invertedY * xCount;
        const groundHeight = BabylonTerrainTileImpl.setupHeight(index2, groundHeightMap);

        uv2GroundHeightMap.push(groundHeight, 0);
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
        const decal = babylonDecals && babylonDecals.find(babylonDecal => {
          return terrainX >= babylonDecal.xPos
            && terrainX < babylonDecal.xSize + babylonDecal.xPos
            && terrainY >= babylonDecal.yPos
            && terrainY < babylonDecal.ySize + babylonDecal.yPos;
        })

        let newMaterialIndex;
        if (decal) {
          newMaterialIndex = MaterialIndex.BOT;
        } else if (BabylonTerrainTileImpl.setupHeight(bLIdx, groundHeightMap) <= 0.0
          || BabylonTerrainTileImpl.setupHeight(bRIdx, groundHeightMap) <= 0.0
          || BabylonTerrainTileImpl.setupHeight(tLIdx, groundHeightMap) <= 0.0
          || BabylonTerrainTileImpl.setupHeight(tRIdx, groundHeightMap) <= 0.0) {
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
}
