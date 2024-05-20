import {
  SlopeConfig,
  SlopeGeometry,
  TerrainObjectConfig,
  TerrainObjectModel,
  TerrainTile,
  BabylonTerrainTile
} from "src/app/gwtangular/GwtAngularFacade";
import { GwtAngularService } from "src/app/gwtangular/GwtAngularService";
import { BabylonModelService } from "./babylon-model.service";
import { ThreeJsWaterRenderService } from "./three-js-water-render.service";
import { ActionManager, ExecuteCodeAction, GroundMesh, Mesh, MeshBuilder, Node, NodeMaterial, TransformNode, Vector3, VertexData } from "@babylonjs/core";
import { RazarionMetadataType, BabylonRenderServiceAccessImpl } from "./babylon-render-service-access-impl.service";
import { BabylonJsUtils } from "./babylon-js.utils";
import { Nullable } from "@babylonjs/core/types";
import { ActionService, SelectionInfo } from "../action.service";

export class BabylonTerrainTileImpl implements BabylonTerrainTile {
  static readonly NODE_X_COUNT = 160;
  static readonly NODE_Y_COUNT = 160;
  static readonly NODE_X_DISTANCE = 1;
  static readonly NODE_Y_DISTANCE = 1;
  static readonly HEIGH_MAP_SIZE = BabylonTerrainTileImpl.NODE_X_COUNT * BabylonTerrainTileImpl.NODE_Y_COUNT;
  static readonly HEIGH_PRECISION = 0.1;
  static readonly HEIGH_MIN = -200;
  static readonly HEIGH_DEFAULT = 0.5;
  private readonly container: TransformNode;
  private cursorTypeHandler: (selectionInfo: SelectionInfo) => void;
  private groundMesh: Mesh;

  constructor(public readonly terrainTile: TerrainTile,
    private gwtAngularService: GwtAngularService,
    private rendererService: BabylonRenderServiceAccessImpl,
    actionService: ActionService,
    babylonModelService: BabylonModelService,
    private threeJsWaterRenderService: ThreeJsWaterRenderService) {
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
        actionManager.hoverCursor = "url(\"/assets/cursors/go.png\") 15 15, auto"
      } else {
        actionManager.hoverCursor = "default"
      }
    }
    actionService.addCursoHandler(this.cursorTypeHandler);

    this.groundMesh = new Mesh("Ground", rendererService.getScene());
    let vertexData = this.createVertexData(terrainTile.getGroundHeightMap());
    vertexData.applyToMesh(this.groundMesh, true);
    this.container.getChildren().push(this.groundMesh);
    this.groundMesh.receiveShadows = true;
    this.groundMesh.parent = this.container;
    this.groundMesh.actionManager = actionManager;

    NodeMaterial.ParseFromFileAsync(
      "Ground materail",
      "/assets/nodeMaterial_land.json",
      this.rendererService.getScene()
    ).then(nodeMaterial => {
      this.groundMesh!.material = nodeMaterial;
    }).catch(reason => {
      console.error(`Load NodeMaterial failed. Reason: ${reason}`);
    })

    BabylonRenderServiceAccessImpl.setRazarionMetadataSimple(this.groundMesh, RazarionMetadataType.GROUND, undefined, terrainTile.getGroundConfigId());

    // let waterConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getWaterConfig(1);
    // this.threeJsWaterRenderService.setup(terrainTile.getIndex(), waterConfig, this.container);

    if (terrainTile.getTerrainTileObjectLists() !== null) {
      terrainTile.getTerrainTileObjectLists().forEach(terrainTileObjectList => {
        try {
          let terrainObjectConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(terrainTileObjectList.terrainObjectConfigId);
          if (!terrainObjectConfig.getThreeJsModelPackConfigId()) {
            throw new Error(`TerrainObjectConfig has no threeJsModelPackConfigId: ${terrainObjectConfig.toString()}`);
          }
          terrainTileObjectList.terrainObjectModels.forEach(terrainObjectModel => {
            try {
              BabylonTerrainTileImpl.createTerrainObject(terrainObjectModel, terrainObjectConfig, babylonModelService, this.container);
            } catch (error) {
              console.error(error);
            }
          });
        } catch (error) {
          console.error(terrainTileObjectList);
          console.error(error);
        }
      });
    }

    this.cursorTypeHandler(actionService.setupSelectionInfo());
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
    let terrainObjectMesh: Mesh = <Mesh>babylonModelService.cloneMesh(terrainObjectConfig.getThreeJsModelPackConfigId(), terrainObjectModelTransform);
    terrainObjectMesh.name = `TerrainObject '${terrainObjectConfig.getInternalName()} (${terrainObjectConfig.getId()})'`;
    terrainObjectMesh.parent = terrainObjectModelTransform;

    return terrainObjectModelTransform;
  }

  addToScene(): void {
    this.rendererService.addTerrainTileToScene(this.container);
  }

  removeFromScene(): void {
    this.rendererService.removeTerrainTileFromScene(this.container);
    // TODO this.actionService.removeCursorHandler(this.cursorTypeHandler);
  }

  getGroundMesh(): Mesh {
    return this.groundMesh;
  }

  private createVertexData(groundHeightMap: Uint16Array): VertexData {
    const indices = [];
    const positions = [];
    const normals = [];
    let xCount = BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_X_DISTANCE;
    let yCount = BabylonTerrainTileImpl.NODE_Y_COUNT / BabylonTerrainTileImpl.NODE_Y_DISTANCE;
    let xOffset = this.terrainTile.getIndex().getX() * BabylonTerrainTileImpl.NODE_X_COUNT;
    let yOffset = this.terrainTile.getIndex().getY() * BabylonTerrainTileImpl.NODE_Y_COUNT;

    // Vertices
    for (let y = 0; y < yCount; y++) {
      for (let x = 0; x < xCount; x++) {
        const index = x + y * xCount;
        let height;
        if (!groundHeightMap || groundHeightMap[index] === undefined) {
          height = BabylonTerrainTileImpl.HEIGH_DEFAULT;
        } else {
          const uin16Height = groundHeightMap && groundHeightMap[index] || 0;
          height = BabylonTerrainTileImpl.uint16ToHeight(uin16Height);
        }


        positions.push(
          x * BabylonTerrainTileImpl.NODE_X_DISTANCE + xOffset,
          height,
          y * BabylonTerrainTileImpl.NODE_Y_DISTANCE + yOffset);
        normals.push(0, 0, 0);
      }
    }

    // Indices
    for (let y = 0; y < yCount - 1; y++) {
      for (let x = 0; x < xCount - 1; x++) {
        const bLIdx = x + y * xCount;
        const bRIdx = x + 1 + y * xCount;
        const tLIdx = x + (y + 1) * xCount;
        const tRIdx = x + 1 + (y + 1) * xCount;

        indices.push(bLIdx);
        indices.push(bRIdx);
        indices.push(tLIdx);

        indices.push(bRIdx);
        indices.push(tRIdx);
        indices.push(tLIdx);
      }
    }

    VertexData.ComputeNormals(positions, indices, normals);

    const vertexData = new VertexData();

    vertexData.indices = indices;
    vertexData.positions = positions;
    vertexData.normals = normals;

    return vertexData;
  }

  public static uint16ToHeight(uint16: number): number {
    return uint16 * BabylonTerrainTileImpl.HEIGH_PRECISION + BabylonTerrainTileImpl.HEIGH_MIN;
  }

  public static heightToUnit16(height: number): number {
    let value = (height - BabylonTerrainTileImpl.HEIGH_MIN) / BabylonTerrainTileImpl.HEIGH_PRECISION;
    return Math.round(value * 10) / 10
  }

}
