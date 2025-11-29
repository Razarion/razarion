import {Injectable} from "@angular/core";
import {
  BabylonBaseItem,
  BabylonBoxItem,
  BabylonRenderServiceAccess,
  BabylonResourceItem,
  BabylonTerrainTile,
  BaseItemPlacerPresenter,
  BaseItemType,
  BoxItemType,
  DecimalPosition,
  Diplomacy,
  MarkerConfig,
  PlaceConfig,
  ResourceItemType,
  TerrainTile,
} from "src/app/gwtangular/GwtAngularFacade";
import {BabylonTerrainTileImpl} from "./babylon-terrain-tile.impl";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {BabylonModelService} from "./babylon-model.service";
import {BabylonWaterRenderService} from "./babylon-water-render.service";
import {
  AbstractMesh,
  Color3,
  Constants,
  CubeTexture,
  DirectionalLight,
  Engine,
  FreeCamera,
  InputBlock,
  Matrix,
  Mesh,
  MeshBuilder,
  Node,
  NodeMaterial,
  Nullable,
  ParticleSystemSet,
  PolygonMeshBuilder,
  Quaternion,
  Ray,
  Scene,
  ShadowGenerator,
  Tools,
  TransformNode,
  Vector2,
  Vector3,
  VertexBuffer
} from "@babylonjs/core";
import {SimpleMaterial} from "@babylonjs/materials";
import {GwtHelper} from "../../gwtangular/GwtHelper";
import {PickingInfo} from "@babylonjs/core/Collisions/pickingInfo";
import {BabylonBaseItemImpl} from "./babylon-base-item.impl";
import {BabylonResourceItemImpl} from "./babylon-resource-item.impl";
import {SelectionFrame} from "./selection-frame";
import {BabylonBoxItemImpl} from "./babylon-box-item.impl";
import {LocationVisualization} from "src/app/editor/common/place-config/location-visualization";
import {ActionService} from "../action.service";
import {BaseItemPlacerPresenterEvent, BaseItemPlacerPresenterImpl} from "./base-item-placer-presenter.impl";
import {UiConfigCollectionService} from "../ui-config-collection.service";
import {TerrainObjectPosition} from "../../generated/razarion-share";
import earcut from 'earcut';
import {ViewField, ViewFieldListener} from './view-field';
import {PlaceConfigComponent} from '../../editor/common/place-config/place-config.component';

export interface RazarionMetadata {
  type: RazarionMetadataType;
  id: number | undefined;
  configId: number | undefined;
  editorHintTerrainObjectPosition: TerrainObjectPosition | undefined;
}

export enum RazarionMetadataType {
  GROUND,
  TERRAIN_OBJECT,
  BOT_BOX
}

@Injectable({
  providedIn: 'root'
})
export class BabylonRenderServiceAccessImpl implements BabylonRenderServiceAccess {
  private readonly SPAWN_PARTICLE_HEIGHT = 15;

  private scene!: Scene;
  private engine!: Engine;
  public shadowGenerator!: ShadowGenerator;
  private directionalLight!: DirectionalLight
  private camera!: FreeCamera;
  private keyPressed: Map<string, number> = new Map();
  private canvas!: HTMLCanvasElement;
  public readonly itemMarkerMaterialCache: Map<Diplomacy, NodeMaterial> = new Map<Diplomacy, NodeMaterial>();
  public baseItemContainer!: TransformNode;
  public resourceItemContainer!: TransformNode;
  public boxItemContainer!: TransformNode;
  public projectileMaterial!: SimpleMaterial;
  private selectionFrame!: SelectionFrame;
  private viewFieldListeners: ViewFieldListener[] = [];
  private viewField?: ViewField;
  private outOfViewPlane?: Mesh;
  private placeMarkerMesh?: Mesh;
  baseItemPlacerActive = false;
  private editorTerrainTileContainer: BabylonTerrainTileImpl[] = [];
  private editorTerrainTileCreationCallback: ((babylonTerrainTile: BabylonTerrainTileImpl) => undefined) | undefined;
  private interpolationListeners: BabylonBaseItemImpl[] = [];
  private babylonBaseItems: BabylonBaseItemImpl[] = [];
  private babylonResourceItems: BabylonResourceItemImpl[] = [];
  private baseItemPlacerPresenterImpl!: BaseItemPlacerPresenterImpl;
  private pendingSetViewFieldCenter: Vector2 | null = null;
  public static readonly SCROLL_SPEED = 0.2;
  public static readonly SCROLL_SPEED_CAMERA_HEIGHT_FACTOR = 0.03;

  constructor(private gwtAngularService: GwtAngularService,
              private babylonModelService: BabylonModelService,
              private uiConfigCollectionService: UiConfigCollectionService,
              private threeJsWaterRenderService: BabylonWaterRenderService,
              private actionService: ActionService) {
    this.babylonModelService.renderer = this;
  }

  public static color4Diplomacy(diplomacy: Diplomacy): Color3 {
    diplomacy = GwtHelper.gwtIssueStringEnum(diplomacy, Diplomacy);
    switch (diplomacy) {
      case Diplomacy.OWN:
        return Color3.Green()
      case Diplomacy.ENEMY:
        return Color3.Red()
      case Diplomacy.FRIEND:
        return Color3.Yellow()
      case Diplomacy.RESOURCE:
        return Color3.Blue()
      case Diplomacy.BOX:
        return Color3.Purple()
    }
    return Color3.Gray()
  }

  addViewFieldListener(viewFieldListener: ViewFieldListener): void {
    this.viewFieldListeners.push(viewFieldListener);
  }

  removeViewFieldListener(viewFieldListener: ViewFieldListener): void {
    this.viewFieldListeners = this.viewFieldListeners.filter(listener => listener !== viewFieldListener);
  }

  getCurrentViewField(): ViewField {
    if (!this.viewField) {
      this.viewField = this.setupViewField();
    }
    return this.viewField;
  }

  runRenderer(): void {
    this.internalSetup();
  }

  internalSetup() {
    // ----- Keyboard -----
    const self = this;
    window.addEventListener("keydown", e => {
      if (!self.keyPressed.has(e.key)) {
        self.keyPressed.set(e.key, Date.now());
      }
    }, true);
    window.addEventListener("keyup", e => {
      self.keyPressed.delete(e.key);
    }, true);
    window.addEventListener('wheel', e => {
      let delta = e.deltaY;
      delta = delta * 0.08;
      const cameraRotation = Quaternion.FromEulerAngles(self.camera.rotation.x, self.camera.rotation.y, self.camera.rotation.z);
      let deltaVector = Vector3.Zero();
      new Vector3(0, 0, -delta).rotateByQuaternionToRef(cameraRotation, deltaVector);
      if (self.camera.position.y + deltaVector.y > 5 && self.camera.position.y + deltaVector.y < 200) {
        this.camera.position.x += deltaVector.x;
        this.camera.position.y += deltaVector.y;
        this.camera.position.z += deltaVector.z;
        this.ensureCameraViewOnMap();
        this.onViewFieldChanged();
      }
    }, true);

    // -----  Camera -----
    //this.camera = new Camera("Main Cam", new Vector3(0, -10, 20), this.scene);
    this.camera = new FreeCamera("Camera", new Vector3(0, 30, -35), this.scene);
    this.camera.maxZ = 800;
    this.camera.setTarget(new Vector3(0, 0, 0));

    // ----- Light -----
    const lightDirection = new Vector3(-3, -10, 3);
    this.directionalLight = new DirectionalLight("DirectionalLight", lightDirection, this.scene);
    this.directionalLight.intensity = 1;
    this.directionalLight.shadowMinZ = -55;
    this.directionalLight.shadowMaxZ = 100;
    this.directionalLight.diffuse = new Color3(1, 1, 1);
    this.directionalLight.specular = new Color3(1, 1, 1);

    this.shadowGenerator = new ShadowGenerator(4096, this.directionalLight);
    this.shadowGenerator.useExponentialShadowMap = true;
    this.shadowGenerator.darkness = 0.6;

    // ----- Resize listener -----
    new ResizeObserver(entries => {
      for (let entry of entries) {
        if (this.canvas == entry.target) {
          this.engine.resize();
          this.ensureCameraViewOnMap();
          this.onViewFieldChanged();
        }
      }
    }).observe(this.canvas);

    this.selectionFrame = new SelectionFrame(this.scene, this, this.gwtAngularService);

    // ----- Render loop -----
    this.scene.onBeforeRenderObservable.add(() => {
      const date = Date.now();
      this.interpolateItemPositions(date);
      this.scrollCamera();
      if (this.pendingSetViewFieldCenter) {
        this.setViewFieldCenter(this.pendingSetViewFieldCenter.x, this.pendingSetViewFieldCenter.y)
      }
    });

    this.engine.runRenderLoop(() => {
      try {
        if (!this.scene.activeCamera) {
          return;
        }
        this.scene.render();
      } catch (e) {
        console.error("Render Engine crashed")
        console.log(e);
        throw e;
      }
    });
  }

  createTerrainTile(terrainTile: TerrainTile): BabylonTerrainTile {
    try {
      let babylonTerrainTileImpl = new BabylonTerrainTileImpl(terrainTile,
        this.gwtAngularService,
        this,
        this.actionService,
        this.babylonModelService,
        this.threeJsWaterRenderService);
      if (this.editorTerrainTileCreationCallback) {
        this.editorTerrainTileCreationCallback(babylonTerrainTileImpl);
      }
      this.editorTerrainTileContainer.push(babylonTerrainTileImpl);
      return babylonTerrainTileImpl;
    } catch (e) {
      console.error(`Error createTerrainTile() with index ${terrainTile.getIndex()}`)
      console.error(e);
      throw e;
    }
  }


  createBabylonBaseItem(id: number, baseItemType: BaseItemType, baseId: number, diplomacy: Diplomacy, userName: string): BabylonBaseItem {
    try {
      let item = new BabylonBaseItemImpl(id,
        baseItemType,
        baseId,
        GwtHelper.gwtIssueStringEnum(diplomacy, Diplomacy),
        userName,
        this,
        this.actionService,
        this.gwtAngularService.gwtAngularFacade.selectionService,
        this.babylonModelService,
        this.uiConfigCollectionService,
        () => this.babylonBaseItems.filter(i => i !== item));
      this.babylonBaseItems.push(item)
      return item;
    } catch (error) {
      console.error(error);
      return BabylonBaseItemImpl.createDummy(id);
    }
  }

  setViewFieldCenter(x: number, y: number): void {
    let currentViewFieldCenter = this.setupCenterGroundPosition();
    if (!this.isValidVector3(currentViewFieldCenter)) {
      this.pendingSetViewFieldCenter = new Vector2(x, y);
      return;
    }
    this.pendingSetViewFieldCenter = null;
    let newFieldCenter = new Vector2(x, y);
    let delta = newFieldCenter.subtract(new Vector2(currentViewFieldCenter.x, currentViewFieldCenter.z));
    this.camera.position.x += delta.x;
    this.camera.position.z += delta.y;
    this.ensureCameraViewOnMap();
    this.onViewFieldChanged();
  }

  hasPendingSetViewFieldCenter(): boolean {
    return !!this.pendingSetViewFieldCenter;
  }

  private isValidVector3(v: Vector3) {
    return (
      typeof v === "object" &&
      isFinite(v.x) &&
      isFinite(v.y) &&
      isFinite(v.z)
    );
  }

  setup(canvas: HTMLCanvasElement) {
    this.canvas = canvas;
    this.engine = new Engine(this.canvas)
    this.scene = new Scene(this.engine);
    this.scene.environmentTexture = CubeTexture.CreateFromImages([
      "renderer/env/px.jpg", // +X
      "renderer/env/py.jpg", // +Y
      "renderer/env/pz.jpg", // +Z
      "renderer/env/nx.jpg", // -X
      "renderer/env/ny.jpg", // -Y
      "renderer/env/nz.jpg", // -Z
    ], this.scene);
    this.scene.createDefaultEnvironment({
      createSkybox: false,
      createGround: false,

    });
    this.scene.environmentIntensity = 1.0;
    this.babylonModelService.setScene(this.scene);
    this.baseItemContainer = new TransformNode("Base items");
    this.resourceItemContainer = new TransformNode("Resource items");
    this.boxItemContainer = new TransformNode("Box items");
    this.projectileMaterial = new SimpleMaterial("Projectile", this.scene);
    this.projectileMaterial.diffuseColor = new Color3(0, 0, 0);
  }

  scrollCamera() {
    let hasChanged = false;
    const speed = BabylonRenderServiceAccessImpl.SCROLL_SPEED + this.camera.position.y * BabylonRenderServiceAccessImpl.SCROLL_SPEED_CAMERA_HEIGHT_FACTOR;
    this.camera.position.y

    let newX = null;
    if (this.checkKeyDown("a", "A", "ArrowLeft")) {
      newX = this.camera.position.x - speed;
      hasChanged = true;
    }
    if (this.checkKeyDown("d", "D", "ArrowRight")) {
      newX = this.camera.position.x + speed;
      hasChanged = true;
    }

    let newZ = null;
    if (this.checkKeyDown("s", "S", "ArrowDown")) {
      newZ = this.camera.position.z - speed;
      hasChanged = true;
    }
    if (this.checkKeyDown("w", "W", "ArrowUp")) {
      newZ = this.camera.position.z + speed;
      hasChanged = true;
    }

    if (newZ == null && newX == null) {
      return;
    }

    if (newX || newX === 0) {
      this.camera.position.x = newX;
    }

    if (newZ || newZ === 0) {
      this.camera.position.z = newZ;
    }

    if (hasChanged) {
      this.ensureCameraViewOnMap();
      this.onViewFieldChanged();
    }
  }

  private checkKeyDown(key1: string, key2: string, key3: string): boolean {
    return !!this.keyPressed.get(key1) || !!this.keyPressed.get(key2) || !!this.keyPressed.get(key3);
  }

  private ensureCameraViewOnMap() {
    let height = LocationVisualization.getHeightFromTerrain(0, 0, this)
    // make sure the transformation matrix we get when calling 'getTransformationMatrix()' is calculated with an up to date view matrix
    // getViewMatrix() forces recalculation of the camera view matrix
    this.camera.getViewMatrix();
    let invertCameraViewProj = Matrix.Invert(this.camera.getTransformationMatrix());
    let bottomLeft = this.setupTerrainLevelPosition(-1, -1, invertCameraViewProj, height);
    if (bottomLeft.x < 0) {
      this.camera.position.x -= bottomLeft.x;
    }
    if (bottomLeft.z < 0) {
      this.camera.position.z -= bottomLeft.z;
    }
  }

  public setupCenterGroundPosition(): Vector3 {
    return this.setupZeroLevelPosition(0, 0, Matrix.Invert(this.camera.getTransformationMatrix()));
  }

  addTerrainTileToScene(terrainTile: BabylonTerrainTileImpl): void {
    this.scene.addTransformNode(terrainTile.container);
    terrainTile.addShadowCasters((mesh: AbstractMesh) => this.shadowGenerator.addShadowCaster(mesh, true));
  }

  removeTerrainTileFromScene(terrainTile: BabylonTerrainTileImpl): void {
    this.scene.removeTransformNode(terrainTile.container);
    terrainTile.removeShadowCasters((mesh: AbstractMesh) => this.shadowGenerator.removeShadowCaster(mesh, true));
  }

  getScene(): Scene {
    return this.scene;
  }

  showOutOfViewMarker(markerConfig: MarkerConfig | null, angle: number): void {
    if (markerConfig) {
      if (!markerConfig.outOfViewNodesMaterialId) {
        console.warn("No outOfViewNodesMaterialId set");
        return;
      }
      if (!this.outOfViewPlane) {
        this.outOfViewPlane = MeshBuilder.CreatePlane("Out of view plane", {size: markerConfig.outOfViewSize}, this.scene);
        this.outOfViewPlane.parent = this.camera;
        this.outOfViewPlane.position.z = markerConfig.outOfViewDistanceFromCamera;
        this.outOfViewPlane.rotation.x = this.camera.rotation.x;
        this.outOfViewPlane.isPickable = false;
        let nodeMaterial = <NodeMaterial>this.babylonModelService.getBabylonMaterial(markerConfig.outOfViewNodesMaterialId!);
        // nodeMaterial = nodeMaterial.clone(`Out of view plane outOfViewNodesMaterialId: ${markerConfig.outOfViewNodesMaterialId}`);
        nodeMaterial.ignoreAlpha = false; // Can not be saved in the NodeEditor
        this.outOfViewPlane.material = nodeMaterial;
        let angleBlock = <InputBlock>(<NodeMaterial>this.outOfViewPlane.material).getBlockByName("angle");
        if (!angleBlock) {
          console.warn(`No angle block found in outOfViewNodesMaterialId: ${markerConfig.outOfViewNodesMaterialId}`);
        } else {
          angleBlock.value = angle;
        }
      } else {
        let angleBlock = <InputBlock>(<NodeMaterial>this.outOfViewPlane.material).getBlockByName("angle");
        if (angleBlock) {
          angleBlock.value = angle;
        }
      }
    } else {
      if (this.outOfViewPlane) {
        this.outOfViewPlane.dispose();
        this.outOfViewPlane = undefined;
      }
    }
  }

  showPlaceMarker(placeConfig: PlaceConfig | null, markerConfig: MarkerConfig | null): void {
    if (!placeConfig || !markerConfig) {
      if (this.placeMarkerMesh) {
        this.placeMarkerMesh.dispose();
        this.placeMarkerMesh = undefined;
      }
    } else {
      if (this.placeMarkerMesh) {
        return;
      }
      if (placeConfig.getPolygon2D()) {
        this.placeMarkerMesh = this.createPlacePolygonMarker(placeConfig);
      } else if (placeConfig.getPosition()) {
        this.placeMarkerMesh = this.createPlaceDiscMarker(placeConfig);
      } else {
        console.warn("Place marker has invalid place config");
        return;
      }
      let nodeMaterial = <NodeMaterial>this.babylonModelService.getBabylonMaterial(markerConfig.placeNodesMaterialId!);
      nodeMaterial.ignoreAlpha = false; // Can not be saved in the NodeEditor
      // nodeMaterial.material.depthFunction = Constants.ALWAYS;
      this.placeMarkerMesh.material = nodeMaterial;
      this.placeMarkerMesh.material.disableDepthWrite = true;
      this.placeMarkerMesh.material.depthFunction = Constants.ALWAYS;
    }
  }

  private createPlacePolygonMarker(placeConfig: PlaceConfig): Mesh {
    const positions2d = placeConfig.getPolygon2D()?.toCornersAngular()!;
    const polygon = PlaceConfigComponent.toVertex2ArrayAngular(positions2d);
    let polygonTriangulation = new PolygonMeshBuilder("Place marker", polygon, this.scene, earcut);
    const polygonMesh = polygonTriangulation.build();

    let booleanWater = false;
    let highest: null | number = null;
    positions2d.forEach(position2d => {
      const heightFromTerrain = LocationVisualization.getHeightFromTerrain(position2d.getX(), position2d.getY(), this);
      if (heightFromTerrain < BabylonTerrainTileImpl.WATER_LEVEL) {
        booleanWater = true;
      }
      if (highest === null || highest < heightFromTerrain) {
        highest = heightFromTerrain;
      }
    });
    if (booleanWater) {
      polygonMesh.position.y = BabylonTerrainTileImpl.WATER_LEVEL;
    } else {
      polygonMesh.position.y = 0.1 + (highest === null ? 0 : highest);
    }

    polygonMesh.isPickable = false;
    const boundingBox = polygonMesh.getBoundingInfo().boundingBox;
    const width = boundingBox.maximum.x - boundingBox.minimum.x;
    const height = boundingBox.maximum.z - boundingBox.minimum.z;
    const meshUv = polygonMesh.getVerticesData(VertexBuffer.UVKind);
    if (meshUv) {
      for (let i = 0; i < meshUv.length; i += 2) {
        meshUv[i] *= width;
        meshUv[i + 1] *= height;
      }
      polygonMesh.setVerticesData(VertexBuffer.UVKind, meshUv);
    }
    return polygonMesh;
  }

  private createPlaceDiscMarker(placeConfig: PlaceConfig): Mesh {
    let radius = placeConfig.toRadiusAngular() || 1;
    let heightFromTerrain = LocationVisualization.getHeightFromTerrain(placeConfig.getPosition()?.getX()!, placeConfig.getPosition()?.getY()!, this);
    if (heightFromTerrain < BabylonTerrainTileImpl.WATER_LEVEL) {
      heightFromTerrain = BabylonTerrainTileImpl.WATER_LEVEL;
    }
    const diskMesh = MeshBuilder.CreateDisc("Place marker", {radius: radius}, this.scene);
    diskMesh.position.x = placeConfig.getPosition()?.getX()!;
    diskMesh.position.y = 0.01 + heightFromTerrain;
    diskMesh.position.z = placeConfig.getPosition()?.getY()!;
    diskMesh.rotation.x = Tools.ToRadians(90);
    diskMesh.isPickable = false;
    const discUv = diskMesh.getVerticesData(VertexBuffer.UVKind);
    if (discUv) {
      for (let i = 0; i < discUv.length; i++) {
        discUv[i] *= 2 * radius;
      }
      diskMesh.setVerticesData(VertexBuffer.UVKind, discUv);
    }
    return diskMesh;
  }

  public onViewFieldChanged() {
    if (this.gwtAngularService.gwtAngularFacade.inputService === undefined) {
      return;
    }
    try {

      this.viewField = this.setupViewField()
      this.gwtAngularService.gwtAngularFacade.inputService.onViewFieldChanged(
        this.viewField.getBottomLeft().getX(), this.viewField.getBottomLeft().getY(),
        this.viewField.getBottomRight().getX(), this.viewField.getBottomRight().getY(),
        this.viewField.getTopRight().getX(), this.viewField.getTopRight().getY(),
        this.viewField.getTopLeft().getX(), this.viewField.getTopLeft().getY(),
      );

      this.viewFieldListeners.forEach(viewFieldListener => {
        viewFieldListener.onViewFieldChanged(this.viewField!);
      });
    } catch (error) {
      console.error(error);
    }
  }

  private setupZeroLevelPosition(
    ndcX: number,
    ndcY: number,
    invertCameraViewProj:
    Matrix): Vector3 {
    let worldNearPosition = Vector3.TransformCoordinates(new Vector3(ndcX, ndcY, -1), invertCameraViewProj);
    let direction = worldNearPosition.subtract(this.camera.position).normalize();
    const distanceToNullLevel = -this.camera.position.y / direction.y;
    return this.camera.position.add(direction.multiplyByFloats(distanceToNullLevel, distanceToNullLevel, distanceToNullLevel));
  }

  private setupTerrainLevelPosition(
    ndcX: number,
    ndcY: number,
    invertCameraViewProj: Matrix,
    terrainHeight: number
  ): Vector3 {
    const worldNearPosition = Vector3.TransformCoordinates(new Vector3(ndcX, ndcY, -1), invertCameraViewProj);
    const direction = worldNearPosition.subtract(this.camera.position).normalize();
    const distanceToTerrainLevel = (terrainHeight - this.camera.position.y) / direction.y;
    return this.camera.position.add(
      direction.multiplyByFloats(distanceToTerrainLevel, distanceToTerrainLevel, distanceToTerrainLevel)
    );
  }

  private setupTerrainPosition(ndcX: number, ndcY: number, invertCameraViewProj: Matrix): Vector3 | undefined {
    let worldNearPosition = Vector3.TransformCoordinates(new Vector3(ndcX, ndcY, -1), invertCameraViewProj);
    let direction = worldNearPosition.subtract(this.camera.position).normalize();

    let terrainPosition = LocationVisualization.getTerrainPositionFromRay(
      new Ray(
        this.camera.position,
        direction,
        1000),
      this);

    if (terrainPosition) {
      return terrainPosition;
    } else {
      return undefined;
    }
  }

  private setupViewField(): ViewField {
    // make sure the transformation matrix we get when calling 'getTransformationMatrix()' is calculated with an up to date view matrix
    // getViewMatrix() forces recalculation of the camera view matrix
    this.camera.getViewMatrix();

    let invertCameraViewProj = Matrix.Invert(this.camera.getTransformationMatrix());

    let bottomLeft = this.setupTerrainPosition(-1, -1, invertCameraViewProj);
    let bottomRight = this.setupTerrainPosition(1, -1, invertCameraViewProj);
    let topRight = this.setupTerrainPosition(1, 1, invertCameraViewProj);
    let topLeft = this.setupTerrainPosition(-1, 1, invertCameraViewProj);

    // console.info(`ViewField BL ${bottomLeft.x}:${bottomLeft.z}:${bottomLeft.y} BR ${bottomRight.x}:${bottomRight.z}:${bottomRight.y} TR ${topRight.x}:${topRight.z}:${topRight.y} TL ${topLeft.x}:${topLeft.z}:${topLeft.y}`)

    if (!bottomLeft || !bottomRight || !topRight || !topLeft) {
      bottomLeft = this.setupZeroLevelPosition(-1, -1, invertCameraViewProj);
      bottomRight = this.setupZeroLevelPosition(1, -1, invertCameraViewProj);
      topRight = this.setupZeroLevelPosition(1, 1, invertCameraViewProj);
      topLeft = this.setupZeroLevelPosition(-1, 1, invertCameraViewProj);
    }

    if (isNaN(bottomLeft.x) || isNaN(bottomLeft.x)
      || isNaN(bottomRight.x) || isNaN(bottomRight.z)
      || isNaN(topRight.x) || isNaN(topRight.z)
      || isNaN(topLeft.x) || isNaN(topLeft.z)) {
      console.warn("setupViewField() has NaN");
      return new ViewField(
        new Vector3(0, 0, 0),
        new Vector3(1, 0, 0),
        new Vector3(1, 0, 1),
        new Vector3(0, 0, 1));
    }
    this.gwtAngularService.gwtAngularFacade.inputService.onViewFieldChanged(
      bottomLeft.x, bottomLeft.z,
      bottomRight.x, bottomRight.z,
      topRight.x, topRight.z,
      topLeft.x, topLeft.z
    );

    return new ViewField(bottomLeft, bottomRight, topRight, topLeft);
  }

  public setupMeshPickPoint(): PickingInfo {
    return this.scene.pick(this.scene.pointerX, this.scene.pointerY);
  }

  public setupTerrainPickPointFromPosition(position: DecimalPosition): Nullable<PickingInfo> {
    let ray = new Ray(new Vector3(position.getX(), 100, position.getY()), new Vector3(0, -1, 0), 1000);

    return this.scene.pickWithRay(ray,
      (mesh: AbstractMesh) => {
        let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(mesh);
        if (!razarionMetadata) {
          return false;
        }
        return razarionMetadata.type == RazarionMetadataType.GROUND || razarionMetadata.type == RazarionMetadataType.BOT_BOX;
      }
    );
  }

  public setupTerrainPickPoint(): PickingInfo {
    return this.scene.pick(this.scene.pointerX, this.scene.pointerY, (mesh: AbstractMesh) => {
      let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(mesh);
      if (!razarionMetadata) {
        return false;
      }
      return razarionMetadata.type == RazarionMetadataType.GROUND || razarionMetadata.type == RazarionMetadataType.BOT_BOX;
    });
  }

  public setupPickInfoFromNDC(ndcX: number, ndcY: number): PickingInfo {
    const x = (ndcX + 1) / 2 * this.engine.getRenderWidth();
    const y = (1 - ndcY) / 2 * this.engine.getRenderHeight();

    return this.scene.pick(x, y);
  }

  showInspector() {
    void Promise.all([
      import("@babylonjs/core/Debug/debugLayer"),
      import("@babylonjs/inspector"),
      import("@babylonjs/node-editor"),
      import("@babylonjs/node-particle-editor")
    ]).then((_values) => {
      this.scene.debugLayer.show({enableClose: true, embedMode: true});
    });
  }

  createBabylonResourceItem(id: number, resourceItemType: ResourceItemType): BabylonResourceItem {
    try {
      const item = new BabylonResourceItemImpl(id,
        resourceItemType,
        this,
        this.actionService,
        this.gwtAngularService.gwtAngularFacade.selectionService,
        this.babylonModelService,
        this.uiConfigCollectionService,
        () => this.babylonResourceItems.filter(i => i !== item));
      this.babylonResourceItems.push(item);
      return item;
    } catch (error) {
      console.error(error);
      return BabylonResourceItemImpl.createDummy(id);
    }
  }

  startSpawn(particleSystemId: number, x: number, y: number, z: number): void {
    this.createParticleSystem(particleSystemId, null).then(particleSystemSet => {
      particleSystemSet.start(<any>new Vector3(x, z + this.SPAWN_PARTICLE_HEIGHT, y));
    });
  }

  createBabylonBoxItem(id: number, boxItemType: BoxItemType): BabylonBoxItem {
    try {
      return new BabylonBoxItemImpl(id,
        boxItemType,
        this,
        this.actionService,
        this.gwtAngularService.gwtAngularFacade.selectionService,
        this.babylonModelService,
        this.uiConfigCollectionService,
        null);
    } catch (error) {
      console.error(error);
      return BabylonBoxItemImpl.createDummy(id);
    }
  }

  createBaseItemPlacerPresenter(): BaseItemPlacerPresenter {
    this.baseItemPlacerPresenterImpl = new BaseItemPlacerPresenterImpl(this, this.babylonModelService);
    return this.baseItemPlacerPresenterImpl;
  }

  setBaseItemPlacerCallback(callback: ((event: BaseItemPlacerPresenterEvent) => void) | null) {
    this.baseItemPlacerPresenterImpl.setBaseItemPlacerCallback(callback);
  }

  setEditorTerrainTileCreationCallback(callback: ((babylonTerrainTile: BabylonTerrainTileImpl) => undefined) | undefined) {
    this.editorTerrainTileCreationCallback = callback;
  }

  public static setRazarionMetadata(node: Node, razarionMetadata: RazarionMetadata) {
    if (!node.metadata) {
      node.metadata = {};
    }
    node.metadata.razarionMetadata = razarionMetadata;
  }

  public static setRazarionMetadataSimple(node: Node, razarionMetadataType: RazarionMetadataType, id: any, configId: any) {
    BabylonRenderServiceAccessImpl.setRazarionMetadata(node, new class implements RazarionMetadata {
      type = razarionMetadataType;
      id = GwtHelper.gwtIssueNumber(id);
      configId = GwtHelper.gwtIssueNumber(configId);
      editorHintTerrainObjectPosition = undefined;
    });
  }

  public static getRazarionMetadata(node: Node): RazarionMetadata | undefined {
    return node.metadata?.razarionMetadata;
  }

  public static findRazarionMetadataNode(node: Node): Node | null {
    if (node.metadata && node.metadata.razarionMetadata) {
      return node;
    }
    if (node.parent) {
      return BabylonRenderServiceAccessImpl.findRazarionMetadataNode(node.parent);
    } else {
      return null;
    }
  }

  addInterpolationListener(interpolationListener: BabylonBaseItemImpl) {
    this.interpolationListeners.push(interpolationListener);
  }

  removeInterpolationListener(interpolationListener: BabylonBaseItemImpl) {
    const index = this.interpolationListeners.indexOf(interpolationListener);
    if (index !== -1) {
      this.interpolationListeners.splice(index, 1);
    }
  }

  private interpolateItemPositions(date: number) {
    this.interpolationListeners.forEach(interpolationListener => interpolationListener.interpolate(date));
  }

  public createParticleSystem(particleSystemEntityId: number | null, imageId: number | null): Promise<ParticleSystemSet> {
    if (!particleSystemEntityId && particleSystemEntityId !== 0) {
      throw new Error("particleSystemEntityId not set");
    }
    const nodeParticleSystemSet = this.babylonModelService.getNodeParticleSystemSet(particleSystemEntityId);
    return nodeParticleSystemSet.buildAsync(this.scene);
  }

  getAllBabylonTerrainTile(): BabylonTerrainTileImpl[] {
    return this.editorTerrainTileContainer;
  }

  public getBabylonBaseItemById(syncBaseItemTypeId: number): BabylonBaseItemImpl | null {
    return this.babylonBaseItems.find(item => {
      return item.getId() === syncBaseItemTypeId;
    }) || null;
  }

  public getBabylonBaseItemByDiplomacyItemType(diplomacy: Diplomacy, itemTypeId: number): BabylonBaseItemImpl | null {
    return this.babylonBaseItems.find(item => {
      return item.diplomacy === diplomacy && item.itemType.getId() === itemTypeId;
    }) || null;
  }

  public getBabylonResourceItemImpls(): BabylonResourceItemImpl[] {
    return this.babylonResourceItems;
  }


  disableSelectionFrame() {
    this.selectionFrame.disable();
  }
}

