import {
  BabylonDecal,
  BabylonTerrainTile,
  BotGround,
  DecimalPosition,
  Diplomacy,
  TerrainObjectConfig,
  TerrainObjectModel,
  TerrainTile,
  TerrainTileObjectList,
  TerrainType
} from "src/app/gwtangular/GwtAngularFacade";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {BabylonModelService} from "./babylon-model.service";
import {BabylonWaterRenderService} from "./babylon-water-render.service";
import {
  Color3,
  Mesh,
  MultiMaterial,
  Node,
  NodeMaterial,
  Ray,
  Scalar,
  Sprite,
  SpriteManager,
  StandardMaterial,
  SubMesh,
  Texture,
  TextureBlock,
  TransformNode,
  Vector3,
  VertexBuffer,
  VertexData
} from "@babylonjs/core";
import {buildGroundMaterial} from "./ground-material";
import {detectShoreline, computeShoreDistance} from "./shoreline-detection";
import {initPerm, SEED, splatterValue} from "./procedural-textures";
import {BabylonRenderServiceAccessImpl, RazarionMetadataType} from "./babylon-render-service-access-impl.service";
import {Nullable} from "@babylonjs/core/types";
import type {AbstractMesh} from '@babylonjs/core/Meshes/abstractMesh';
import {GwtHelper} from "src/app/gwtangular/GwtHelper";
import {GroundUtil} from './ground-util';
import {GwtInstance} from '../../gwtangular/GwtInstance';

enum MaterialIndex {
  GROUND = 0,
  ASPHALT = 1,
}

enum TerrainZone {
  UPPER = 0,
  UNDER = 1,
  BEACH = 2,
  UNDERWATER = 3,
}

export class BabylonTerrainTileImpl implements BabylonTerrainTile {
  // See: GWT Java Code TerrainUtil
  static readonly NODE_X_COUNT = 160;
  static readonly NODE_Y_COUNT = 160;
  static readonly NODE_SIZE = 1;
  static readonly TILE_NODE_SIZE = this.NODE_X_COUNT * this.NODE_Y_COUNT;
  static readonly HEIGHT_PRECISION = 0.01;
  static readonly HEIGHT_MIN = -200;
  static readonly WATER_LEVEL = 0;
  static readonly BEACH_HEIGHT = 0.3;
  static readonly WALL_HEIGHT_DIFF = 0.5;
  static readonly HEIGHT_DEFAULT = 0.5;
  static readonly BOT_BOX_LENGTH = 8;
  static readonly BOT_BOX_Z_ROTATION = 22;
  public readonly container: TransformNode;
  public readonly shadowCasterObjects: TransformNode[] = []
  private readonly groundMesh: Mesh;
  private waterMesh: Mesh | null = null;
  private groundMaterial: NodeMaterial | null = null;
  private shadowCaster?: ((mesh: AbstractMesh) => void) | null = null;
  private groundHeights: number[] = [];
  private tileXOffset: number = 0;
  private tileYOffset: number = 0;

  public static setupTerrainType(bLHeight: number, bRHeight: number, tRHeight: number, tLHeight: number): TerrainType {
    if (bLHeight <= 0.0 && bRHeight <= 0.0 && tRHeight <= 0.0 && tLHeight <= 0.0) {
      return TerrainType.WATER;
    }
    const maxHeight = Math.max(bLHeight, bRHeight, tRHeight, tLHeight);
    const minHeight = Math.min(bLHeight, bRHeight, tRHeight, tLHeight);
    if (Math.abs(maxHeight - minHeight) < BabylonTerrainTileImpl.WALL_HEIGHT_DIFF) {
      return TerrainType.LAND;
    } else {
      return TerrainType.BLOCKED;
    }
  }

  /** Schedule work for next idle period or after a minimal delay */
  private static scheduleIdle(callback: () => void): void {
    if (typeof requestIdleCallback === 'function') {
      requestIdleCallback(() => callback(), { timeout: 100 });
    } else {
      setTimeout(callback, 4);
    }
  }

  constructor(public readonly terrainTile: TerrainTile,
              private gwtAngularService: GwtAngularService,
              private rendererService: BabylonRenderServiceAccessImpl,
              private babylonModelService: BabylonModelService,
              private threeJsWaterRenderService: BabylonWaterRenderService) {
    this.container = new TransformNode(`Terrain Tile ${terrainTile.getIndex().toString()}`);
    this.groundMesh = new Mesh("Ground", rendererService.getScene());

    // Phase 1 (sync): Vertex data — needed immediately for visible ground mesh
    this.buildPhase1_VertexData();

    // Phase 2 (deferred): Shore detection, materials, water, objects, sprites
    BabylonTerrainTileImpl.scheduleIdle(() => this.buildPhase2_ShoreAndMaterial());
  }

  private buildPhase1_VertexData(): void {
    const terrainTile = this.terrainTile;
    this.uv2GroundHeightMap = [];
    this.materialSubmeshes = [];
    this.groundUtil = new GroundUtil();

    let vertexData = this.createVertexData(terrainTile.getGroundHeightMap(),
      this.uv2GroundHeightMap,
      this.materialSubmeshes,
      terrainTile.getBabylonDecals(),
      this.groundUtil);
    vertexData.applyToMesh(this.groundMesh, true);

    // Cache ground heights for sprite placement
    const groundHeightMap = terrainTile.getGroundHeightMap();
    const xCount = (BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    const yCount = (BabylonTerrainTileImpl.NODE_Y_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    this.tileXOffset = terrainTile.getIndex().getX() * BabylonTerrainTileImpl.NODE_X_COUNT;
    this.tileYOffset = terrainTile.getIndex().getY() * BabylonTerrainTileImpl.NODE_Y_COUNT;
    this.groundHeights = [];
    for (let i = 0; i < xCount * yCount; i++) {
      this.groundHeights.push(BabylonTerrainTileImpl.setupHeight(i, groundHeightMap));
    }

    this.container.getChildren().push(this.groundMesh);
    this.groundMesh.receiveShadows = true;
    this.groundMesh.parent = this.container;

    // Placeholder material while the real material loads
    const avgHeight = this.groundHeights.reduce((a, b) => a + b, 0) / this.groundHeights.length;
    const placeholder = new StandardMaterial("placeholder", this.rendererService.getScene());
    placeholder.disableLighting = true;
    if (avgHeight <= 0) {
      placeholder.emissiveColor = new Color3(0.12, 0.30, 0.55); // water blue
    } else {
      placeholder.emissiveColor = new Color3(0.30, 0.42, 0.18); // land green
    }
    this.groundMesh.material = placeholder;
  }

  private uv2GroundHeightMap: number[] = [];
  private materialSubmeshes: { materialIndex: MaterialIndex, indexStart: number, indexCount: number }[] = [];
  private groundUtil: GroundUtil | null = null;

  private buildPhase2_ShoreAndMaterial(): void {
    const xCount = (BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    const yCount = (BabylonTerrainTileImpl.NODE_Y_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;

    // Shore detection + UV2
    const shoreSegments = detectShoreline(this.groundHeights, xCount, yCount);
    const groundUv2 = computeShoreDistance(shoreSegments, this.groundHeights, xCount, yCount);
    this.groundMesh.setVerticesData(VertexBuffer.UV2Kind, groundUv2);

    BabylonTerrainTileImpl.scheduleIdle(() => this.buildPhase3_Material());
  }

  private buildPhase3_Material(): void {
    const terrainTile = this.terrainTile;
    let groundConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(GwtHelper.gwtIssueNumber(terrainTile.getGroundConfigId()));
    this.groundMaterial = buildGroundMaterial(this.rendererService.getScene());
    const groundUtilityBlock = <TextureBlock>this.groundMaterial.getBlockByName("GroundUtility");
    if (groundUtilityBlock && this.groundUtil) {
      groundUtilityBlock.texture = new Texture(this.groundUtil.createGroundTypeTexture().toDataURL(), this.rendererService.getScene());
      this.groundMaterial.build();
    }
    let asphaltMaterial = <NodeMaterial>this.babylonModelService.getBabylonMaterial(groundConfig.getAsphaltBabylonMaterialId());

    const multiMaterial = new MultiMaterial(`Ground ${groundConfig.getInternalName()}`, this.rendererService.getScene());
    multiMaterial.subMaterials[MaterialIndex.GROUND] = this.groundMaterial;
    multiMaterial.subMaterials[MaterialIndex.ASPHALT] = asphaltMaterial;

    // Dispose placeholder material and assign the real one
    if (this.groundMesh.material instanceof StandardMaterial) {
      this.groundMesh.material.dispose();
    }
    this.groundMesh!.material = multiMaterial;

    this.groundMesh.subMeshes = [];
    const totalVertices = this.groundMesh.getTotalVertices();
    this.materialSubmeshes.forEach(materialSubmesh =>
      new SubMesh(materialSubmesh.materialIndex, 0, totalVertices, materialSubmesh.indexStart, materialSubmesh.indexCount, this.groundMesh));

    BabylonRenderServiceAccessImpl.setRazarionMetadataSimple(this.groundMesh, RazarionMetadataType.GROUND, undefined, terrainTile.getGroundConfigId());

    // Free references no longer needed
    this.groundUtil = null;
    this.materialSubmeshes = [];

    BabylonTerrainTileImpl.scheduleIdle(() => this.buildPhase4_WaterAndObjects());
  }

  private buildPhase4_WaterAndObjects(): void {
    const terrainTile = this.terrainTile;
    let groundConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(GwtHelper.gwtIssueNumber(terrainTile.getGroundConfigId()));

    this.waterMesh = this.threeJsWaterRenderService.setup(terrainTile.getIndex(), groundConfig, this.container, this.uv2GroundHeightMap, this.rendererService);

    if (terrainTile.getTerrainTileObjectLists()) {
      this.setupTerrainTileObjects(terrainTile.getTerrainTileObjectLists());
    }

    this.setupBotGrounds(terrainTile.getBotGrounds());

    this.setupSprites();
  }

  private static readonly TERRAIN_OBJECTS_PER_BATCH = 100;
  private static readonly TERRAIN_OBJECT_BATCH_DELAY = 16;

  private setupTerrainTileObjects(terrainTileObjectLists: TerrainTileObjectList[]): void {
    // Collect all terrain objects to create, then batch-process them
    const pending: { config: TerrainObjectConfig, model: TerrainObjectModel, zeroRadius: boolean }[] = [];

    terrainTileObjectLists.forEach(terrainTileObjectList => {
      try {
        if (!terrainTileObjectList.terrainObjectModels || terrainTileObjectList.terrainObjectModels.length === 0) {
          return;
        }
        let terrainObjectConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(terrainTileObjectList.terrainObjectConfigId);
        if (!terrainObjectConfig.getModel3DId()) {
          console.error(`TerrainObjectConfig has no model3DId: ${terrainObjectConfig.toString()}`);
          return;
        }
        terrainTileObjectList.terrainObjectModels.forEach(terrainObjectModel => {
          if (!terrainObjectModel || !terrainObjectModel.position) {
            return;
          }
          pending.push({ config: terrainObjectConfig, model: terrainObjectModel, zeroRadius: terrainObjectConfig.getRadius() <= 0 });
        });
      } catch (error) {
        console.error(terrainTileObjectList);
        console.error(error);
      }
    });

    this.createTerrainObjectsBatched(pending, 0);
  }

  private createTerrainObjectsBatched(pending: { config: TerrainObjectConfig, model: TerrainObjectModel, zeroRadius: boolean }[], index: number): void {
    const end = Math.min(index + BabylonTerrainTileImpl.TERRAIN_OBJECTS_PER_BATCH, pending.length);
    for (let i = index; i < end; i++) {
      const { config, model, zeroRadius } = pending[i];
      try {
        let terrainObject = BabylonTerrainTileImpl.createTerrainObject(model, config, this.babylonModelService, this.container);
        if (!zeroRadius) {
          if (this.shadowCaster) {
            this.shadowCasterObjects.push(terrainObject);
            terrainObject.getChildMeshes().forEach(mesh => this.shadowCaster!(mesh))
          }
          const actionManager = this.rendererService.terrainObjectActionManager;
          terrainObject.getChildMeshes().forEach(childMesh => {
            childMesh.actionManager = actionManager;
          });
        }
      } catch (error) {
        console.error(error);
      }
    }
    if (end < pending.length) {
      setTimeout(() => this.createTerrainObjectsBatched(pending, end), BabylonTerrainTileImpl.TERRAIN_OBJECT_BATCH_DELAY);
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
    this.rendererService.directionalLight.includedOnlyMeshes.push(this.groundMesh);
    this.rendererService.addTerrainTileToScene(this);
  }

  removeFromScene(): void {
    const index = this.rendererService.directionalLight.includedOnlyMeshes.indexOf(this.groundMesh);
    if (index !== -1) {
      this.rendererService.directionalLight.includedOnlyMeshes.splice(index, 1);
    }

    this.rendererService.removeTerrainTileFromScene(this);
  }

  getGroundMesh(): Mesh {
    return this.groundMesh;
  }

  updateGroundTypeTexture(positions: number[]): void {
    if (!this.groundMaterial) {
      return;
    }

    const groundUtilityBlock = <TextureBlock>this.groundMaterial.getBlockByName("GroundUtility");
    if (!groundUtilityBlock) {
      return;
    }

    const groundUtil = new GroundUtil();
    const xCount = (BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    const yCount = (BabylonTerrainTileImpl.NODE_Y_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;

    for (let y = 0; y < yCount; y++) {
      for (let x = 0; x < xCount; x++) {
        const index = (x + y * xCount) * 3;
        const height = positions[index + 1]; // y-component is height
        groundUtil.addHeightAt(height, x, y);
      }
    }

    // Dispose old texture to prevent memory leak
    if (groundUtilityBlock.texture) {
      groundUtilityBlock.texture.dispose();
    }

    groundUtilityBlock.texture = new Texture(
      groundUtil.createGroundTypeTexture().toDataURL(),
      this.rendererService.getScene()
    );
    this.groundMaterial.build();

    // Update water UV2 for transparency
    if (this.waterMesh) {
      BabylonWaterRenderService.updateWaterUV2(this.waterMesh, positions);
    }
  }

  private createVertexData(groundHeightMap: Uint16Array, uv2GroundHeightMap: number[], materialSubmeshes: {
    materialIndex: MaterialIndex,
    indexStart: number,
    indexCount: number
  }[], babylonDecals: BabylonDecal[], groundUtil: GroundUtil): VertexData {
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
        const height = BabylonTerrainTileImpl.setupHeight(index, groundHeightMap);

        groundUtil.addHeightAt(height, x, y);

        positions.push(
          x * BabylonTerrainTileImpl.NODE_SIZE + xOffset,
          height,
          y * BabylonTerrainTileImpl.NODE_SIZE + yOffset);
        normals.push(0, 0, 0);
        const uvx = x / (xCount - 1);
        const uvy = 1.0 - y / (yCount - 1);
        uvs.push(uvx, uvy);

        const invertedY = xCount - y - 1;
        const index2 = x + invertedY * xCount;
        const invertedGroundHeight = BabylonTerrainTileImpl.setupHeight(index2, groundHeightMap);
        uv2GroundHeightMap.push(invertedGroundHeight, 0);
      }
    }

    // Compute shore direction (gradient of ground height) and store angle in UV2.y
    BabylonTerrainTileImpl.computeShoreDirections(uv2GroundHeightMap, xCount, yCount);

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
        const decal = this.findDecal(babylonDecals, terrainX, terrainY);

        let newMaterialIndex;
        const terrainType = BabylonTerrainTileImpl.setupTerrainType(bLHeight, bRHeight, tRHeight, tLHeight);
        if (decal && (terrainType == TerrainType.LAND || terrainType == TerrainType.BLOCKED)) {
          newMaterialIndex = MaterialIndex.ASPHALT;
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

  private findDecal(babylonDecals: BabylonDecal[], terrainX: number, terrainY: number) {
    return babylonDecals && babylonDecals.find(babylonDecal => {
      return this.insideDecal(terrainX, terrainY, babylonDecal);
    });
  }

  private insideDecal(x: number, y: number, babylonDecal: BabylonDecal) {
    return x >= babylonDecal.xPos
      && x < babylonDecal.xSize + babylonDecal.xPos
      && y >= babylonDecal.yPos
      && y < babylonDecal.ySize + babylonDecal.yPos;
  }

  private insideBotGround(x: number, y: number) {
    if (!this.terrainTile.getBotGrounds()) {
      return false;
    }

    let found = false;

    this.terrainTile.getBotGrounds().forEach(botGround => {
      if (botGround.positions) {
        botGround.positions.forEach(position => {
          // Skip invalid positions
          if (!position || typeof position.getX !== 'function' || typeof position.getY !== 'function') {
            return;
          }
          const posX = position.getX();
          const posY = position.getY();
          if (typeof posX !== 'number' || typeof posY !== 'number' || isNaN(posX) || isNaN(posY)) {
            return;
          }
          if (this.insideBotGroundPosition(position, x, y)) {
            found = true;
          }
        });
      }
    });

    if (found) {
      return true;
    }

    this.terrainTile.getBotGrounds().forEach(botGround => {
      if (botGround.botGroundSlopeBoxes) {
        botGround.botGroundSlopeBoxes.forEach(groundSlopeBox => {
          // Skip invalid slope boxes
          if (!groundSlopeBox || typeof groundSlopeBox.xPos !== 'number' || typeof groundSlopeBox.yPos !== 'number' ||
              isNaN(groundSlopeBox.xPos) || isNaN(groundSlopeBox.yPos)) {
            return;
          }
          if (this.insideBotGroundPosition(GwtInstance.newDecimalPosition(groundSlopeBox.xPos, groundSlopeBox.yPos), x, y)) {
            found = true;
          }
        });
      }
    });

    return found;
  }

  private insideBotGroundPosition(position: DecimalPosition, x: number, y: number) {
    return position.getX() - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 <= x
      && position.getY() - BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 <= y
      && position.getX() + BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 > x
      && position.getY() + BabylonTerrainTileImpl.BOT_BOX_LENGTH / 2 > y;
  }

  /**
   * Computes shore direction from ground height gradient and stores the angle in UV2.y.
   * The gradient of ground height points uphill (toward shore).
   * UV2 layout: [height0, angle0, height1, angle1, ...]
   */
  public static computeShoreDirections(uv2: number[], xCount: number, yCount: number): void {
    for (let y = 0; y < yCount; y++) {
      for (let x = 0; x < xCount; x++) {
        const idx = (y * xCount + x) * 2;
        const h = uv2[idx]; // ground height at this vertex

        // Central differences for gradient (clamped at edges)
        const xL = x > 0 ? uv2[((y * xCount) + (x - 1)) * 2] : h;
        const xR = x < xCount - 1 ? uv2[((y * xCount) + (x + 1)) * 2] : h;
        const yD = y > 0 ? uv2[(((y - 1) * xCount) + x) * 2] : h;
        const yU = y < yCount - 1 ? uv2[(((y + 1) * xCount) + x) * 2] : h;

        const dx = xR - xL;
        const dy = yU - yD;

        // atan2 gives angle of gradient (direction toward shore)
        // Default to 0 on flat areas (no gradient)
        const angle = (dx === 0 && dy === 0) ? 0 : Math.atan2(dy, dx);
        uv2[idx + 1] = angle;
      }
    }
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
      if (!botGround || !botGround.positions) {
        return;
      }
      let botGroundNorm = new Vector3(0, 1, 0).normalize();
      botGround.positions.forEach((position) => {
        // Skip invalid positions
        if (!position || typeof position.getX !== 'function' || typeof position.getY !== 'function') {
          return;
        }
        const posX = position.getX();
        const posY = position.getY();
        if (typeof posX !== 'number' || typeof posY !== 'number' || isNaN(posX) || isNaN(posY)) {
          return;
        }
        const renderObject = this.babylonModelService.cloneModel3D(botGround.model3DId, this.container, Diplomacy.OWN);
        renderObject.setPosition(new Vector3(posX, botGround.height, posY));
        renderObject.setMetadata({
          type: RazarionMetadataType.BOT_GROUND,
          configId: botGround.model3DId,
          id: undefined,
          editorHintTerrainObjectPosition: undefined,
          botGroundNorm: botGroundNorm
        });
      });
      if (botGround.botGroundSlopeBoxes) {
        botGround.botGroundSlopeBoxes.forEach(botGroundSlopeBox => {
          // Skip invalid slope boxes
          if (!botGroundSlopeBox || typeof botGroundSlopeBox.xPos !== 'number' || typeof botGroundSlopeBox.yPos !== 'number' ||
              isNaN(botGroundSlopeBox.xPos) || isNaN(botGroundSlopeBox.yPos)) {
            return;
          }
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
        })
      }
    });
  }

  private static readonly SPLATTER_UV_SCALE = 0.006;
  private static readonly SPRITE_CELL_SIZE = 64;
  private static readonly SPRITES_PER_TILE = 2500;
  private static readonly SPRITES_PER_BATCH = 1000;
  private static readonly SPRITE_BATCH_DELAY = 16;
  private static permInitialized = false;

  private static ensurePermInitialized(): void {
    if (!BabylonTerrainTileImpl.permInitialized) {
      initPerm(SEED);
      BabylonTerrainTileImpl.permInitialized = true;
    }
  }

  /**
   * Determine terrain zone at world position using height and splatter value.
   * Matches the shader logic in ground-material.ts.
   */
  private static getTerrainZone(worldX: number, worldZ: number, height: number): TerrainZone {
    if (height <= BabylonTerrainTileImpl.WATER_LEVEL - 0.5) {
      return TerrainZone.UNDERWATER;
    }
    if (height < BabylonTerrainTileImpl.BEACH_HEIGHT + 0.1) {
      return TerrainZone.BEACH;
    }
    BabylonTerrainTileImpl.ensurePermInitialized();
    const nx = (worldX * BabylonTerrainTileImpl.SPLATTER_UV_SCALE) % 1.0;
    const ny = (worldZ * BabylonTerrainTileImpl.SPLATTER_UV_SCALE) % 1.0;
    const sv = splatterValue(nx < 0 ? nx + 1 : nx, ny < 0 ? ny + 1 : ny);
    // sv: 0 = under (rock/dirt), 1 = upper (grass)
    if (sv > 0.6) {
      return TerrainZone.UPPER;
    } else if (sv < 0.4) {
      return TerrainZone.UNDER;
    }
    // Transition zone — randomly pick based on splatter value
    return Math.random() < sv ? TerrainZone.UPPER : TerrainZone.UNDER;
  }

  /** Fast height lookup from cached heightmap — no raycasting */
  private getHeightAt(worldX: number, worldZ: number): number {
    const localX = Math.floor(worldX - this.tileXOffset);
    const localY = Math.floor(worldZ - this.tileYOffset);
    const xCount = BabylonTerrainTileImpl.NODE_X_COUNT + 1;
    if (localX < 0 || localX >= xCount || localY < 0 || localY >= BabylonTerrainTileImpl.NODE_Y_COUNT + 1) {
      return BabylonTerrainTileImpl.HEIGHT_DEFAULT;
    }
    return this.groundHeights[localX + localY * xCount] ?? BabylonTerrainTileImpl.HEIGHT_DEFAULT;
  }

  private setupSprites() {
    const scene = this.rendererService.getScene();
    const spriteManagers: Record<TerrainZone, SpriteManager> = {
      [TerrainZone.UPPER]: new SpriteManager("upperSprites", "sprites_upper_4x4.png", 2500, BabylonTerrainTileImpl.SPRITE_CELL_SIZE, scene),
      [TerrainZone.UNDER]: new SpriteManager("underSprites", "sprites_under_4x4.png", 1500, BabylonTerrainTileImpl.SPRITE_CELL_SIZE, scene),
      [TerrainZone.BEACH]: new SpriteManager("beachSprites", "sprites_beach_4x4.png", 800, BabylonTerrainTileImpl.SPRITE_CELL_SIZE, scene),
      [TerrainZone.UNDERWATER]: new SpriteManager("underwaterSprites", "sprites_underwater_4x4.png", 800, BabylonTerrainTileImpl.SPRITE_CELL_SIZE, scene),
    };

    setTimeout(() => {
      this.placeSprites(BabylonTerrainTileImpl.SPRITES_PER_TILE, spriteManagers);
    }, BabylonTerrainTileImpl.SPRITE_BATCH_DELAY);
  }

  private placeSprites(count: number, spriteManagers: Record<TerrainZone, SpriteManager>) {
    for (let i = 0; i < BabylonTerrainTileImpl.SPRITES_PER_BATCH; i++) {
      if (count <= 0) {
        return;
      }
      count--;

      const x = Scalar.RandomRange(this.terrainTile.getIndex().getX() * BabylonTerrainTileImpl.NODE_X_COUNT, (this.terrainTile.getIndex().getX() + 1) * BabylonTerrainTileImpl.NODE_X_COUNT);
      const z = Scalar.RandomRange(this.terrainTile.getIndex().getY() * BabylonTerrainTileImpl.NODE_Y_COUNT, (this.terrainTile.getIndex().getY() + 1) * BabylonTerrainTileImpl.NODE_Y_COUNT);

      const decal = this.findDecal(this.terrainTile.getBabylonDecals(), x, z);
      if (decal) {
        continue;
      }

      if (this.insideBotGround(x, z)) {
        continue;
      }

      const height = this.getHeightAt(x, z);
      const zone = BabylonTerrainTileImpl.getTerrainZone(x, z, height);

      // No sprites below -5m
      if (height < -5) {
        continue;
      }

      // 50% less sprites for beach and underwater
      if ((zone === TerrainZone.BEACH || zone === TerrainZone.UNDERWATER) && Math.random() < 0.5) {
        continue;
      }

      const manager = spriteManagers[zone];
      const sprite = new Sprite("sprite", manager);
      sprite.cellIndex = Math.floor(Math.random() * 16);
      sprite.width = 2;
      sprite.height = 2;
      if (zone === TerrainZone.UNDERWATER) {
        sprite.color.a = 0.5;
      }
      sprite.position.x = x;
      sprite.position.y = height + 0.5;
      sprite.position.z = z;
    }
    if (count > 0) {
      setTimeout(() => {
        this.placeSprites(count, spriteManagers);
      }, BabylonTerrainTileImpl.SPRITE_BATCH_DELAY);
    }
  }
}
