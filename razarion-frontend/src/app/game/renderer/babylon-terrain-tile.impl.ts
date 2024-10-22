import {
  BabylonTerrainTile,
  Index,
  TerrainObjectConfig,
  TerrainObjectModel,
  TerrainTile
} from "src/app/gwtangular/GwtAngularFacade";
import { GwtAngularService } from "src/app/gwtangular/GwtAngularService";
import { BabylonModelService } from "./babylon-model.service";
import { ThreeJsWaterRenderService } from "./three-js-water-render.service";
import { ActionManager, Color3, ExecuteCodeAction, Mesh, MeshBuilder, Node, PBRMaterial, Ray, StandardMaterial, Texture, TransformNode, Vector3, VertexData } from "@babylonjs/core";
import { BabylonRenderServiceAccessImpl, RazarionMetadataType } from "./babylon-render-service-access-impl.service";
import { Nullable } from "@babylonjs/core/types";
import { ActionService, SelectionInfo } from "../action.service";
import { GwtHelper } from "src/app/gwtangular/GwtHelper";
import { GwtInstance } from "src/app/gwtangular/GwtInstance";

export class BabylonTerrainTileImpl implements BabylonTerrainTile {
  // See: GWT Java Code TerrainUtil
  static readonly NODE_X_COUNT = 160;
  static readonly NODE_Y_COUNT = 160;
  static readonly NODE_SIZE = 1;
  static readonly HEIGHT_PRECISION = 0.1;
  static readonly HEIGHT_MIN = -200;
  static readonly WATER_LEVEL = 0;
  static readonly HEIGHT_DEFAULT = 0.5;
  private readonly container: TransformNode;
  private cursorTypeHandler: (selectionInfo: SelectionInfo) => void;
  private groundMesh: Mesh;

  constructor(public readonly terrainTile: TerrainTile,
    private gwtAngularService: GwtAngularService,
    private rendererService: BabylonRenderServiceAccessImpl,
    actionService: ActionService,
    private babylonModelService: BabylonModelService,
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
    actionService.addCursorHandler(this.cursorTypeHandler);

    this.groundMesh = new Mesh("Ground", rendererService.getScene());
    let vertexData = this.createVertexData(terrainTile.getGroundHeightMap());
    vertexData.applyToMesh(this.groundMesh, true);
    this.container.getChildren().push(this.groundMesh);
    this.groundMesh.receiveShadows = true;
    this.groundMesh.parent = this.container;
    this.groundMesh.actionManager = actionManager;

    let groundConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(GwtHelper.gwtIssueNumber(terrainTile.getGroundConfigId()));
    let groundNodeMaterial = babylonModelService.getNodeMaterial(groundConfig.getTopThreeJsMaterial());
    this.groundMesh!.material = groundNodeMaterial;

    BabylonRenderServiceAccessImpl.setRazarionMetadataSimple(this.groundMesh, RazarionMetadataType.GROUND, undefined, terrainTile.getGroundConfigId());

    let waterConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getWaterConfig(GwtHelper.gwtIssueNumber(terrainTile.getWaterConfigId()));
    this.threeJsWaterRenderService.setup(terrainTile.getIndex(), waterConfig, this.container);

    if (terrainTile.getTerrainTileObjectLists()) {
      terrainTile.getTerrainTileObjectLists().forEach(terrainTileObjectList => {
        try {
          let terrainObjectConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(terrainTileObjectList.terrainObjectConfigId);
          if (!terrainObjectConfig.getThreeJsModelPackConfigId()) {
            throw new Error(`TerrainObjectConfig has no threeJsModelPackConfigId: ${terrainObjectConfig.toString()}`);
          }
          terrainTileObjectList.terrainObjectModels.forEach(terrainObjectModel => {
            try {
              //-----
              let ray = new Ray(new Vector3(terrainObjectModel.position.getX(), -100, terrainObjectModel.position.getY()), new Vector3(0, 1, 0), 1000);
              let pickingInfo = this.groundMesh.intersects(ray);
              if (pickingInfo.hit) {
                terrainObjectModel.position = GwtInstance.newVertex(terrainObjectModel.position.getX(),
                  terrainObjectModel.position.getY(),
                  terrainObjectModel.position.getZ() + pickingInfo.pickedPoint!.y,
                );
              } else {
                console.warn(`TerrainObject ${terrainObjectModel.terrainObjectId} not on ground`);
              }
              //-----
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

    if (terrainTile.getBabylonDecals()) {
      terrainTile.getBabylonDecals().forEach(babylonDecal => {
        const material = this.babylonModelService.getBabylonMaterial(babylonDecal.babylonMaterialId);

        let pickingInfo = this.rendererService.setupTerrainPickPointFromPosition(GwtInstance.newDecimalPosition(babylonDecal.xPos, babylonDecal.yPos));
        if (pickingInfo && pickingInfo.hit) {
          const decal = MeshBuilder.CreateDecal("Bot ground", this.groundMesh, {
            position: pickingInfo!.pickedPoint!,
            size: new Vector3(babylonDecal.ySize, babylonDecal.xSize, 10),
            normal: new Vector3(0,1,0)
          });
          decal.material = material;
          decal.material.zOffset = -2
          decal.setParent(this.groundMesh);
          decal.receiveShadows = true;
          decal.actionManager = actionManager;
          BabylonRenderServiceAccessImpl.setRazarionMetadataSimple(decal, RazarionMetadataType.GROUND, undefined, undefined);
        } else {
          console.warn(`Can not create BabylonDecals ${babylonDecal}`)
        }

      });
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

}
