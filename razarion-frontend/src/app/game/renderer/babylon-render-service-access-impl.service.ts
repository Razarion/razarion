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
import {ThreeJsWaterRenderService} from "./three-js-water-render.service";
import {
  AbstractMesh,
  Color3,
  DirectionalLight,
  Engine,
  FreeCamera,
  HemisphericLight,
  InputBlock,
  Matrix,
  Mesh,
  MeshBuilder,
  Node,
  NodeMaterial,
  Nullable,
  ParticleSystem,
  PolygonMeshBuilder,
  Quaternion,
  Ray,
  Scene,
  ShadowGenerator,
  Texture,
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
import {GwtInstance} from "src/app/gwtangular/GwtInstance";
import {BabylonBoxItemImpl} from "./babylon-box-item.impl";
import {PlaceConfigComponent} from "src/app/editor/common/place-config/place-config.component";
import {LocationVisualization} from "src/app/editor/common/place-config/location-visualization";
import {ActionService} from "../action.service";
import {BaseItemPlacerPresenterImpl} from "./base-item-placer-presenter.impl";
import {getImageUrl} from "src/app/common";
import {UiConfigCollectionService} from "../ui-config-collection.service";
import {TerrainObjectPosition} from "../../generated/razarion-share";

export interface RazarionMetadata {
  type: RazarionMetadataType;
  id: number | undefined;
  configId: number | undefined;
  editorHintTerrainObjectPosition: TerrainObjectPosition | undefined;
}

export enum RazarionMetadataType {
  GROUND,
  TERRAIN_OBJECT,
  SLOPE
}

export class ViewField {
  private readonly bottomLeft: DecimalPosition;
  private readonly bottomRight: DecimalPosition;
  private readonly topRight: DecimalPosition;
  private readonly topLeft: DecimalPosition;
  private center?: DecimalPosition;

  constructor(bottomLeft: Vector3, bottomRight: Vector3, topRight: Vector3, topLeft: Vector3) {
    this.bottomLeft = GwtInstance.newDecimalPosition(bottomLeft.x, bottomLeft.z);
    this.bottomRight = GwtInstance.newDecimalPosition(bottomRight.x, bottomRight.z);
    this.topRight = GwtInstance.newDecimalPosition(topRight.x, topRight.z);
    this.topLeft = GwtInstance.newDecimalPosition(topLeft.x, topLeft.z);
  }

  getBottomLeft(): DecimalPosition {
    return this.bottomLeft;
  }

  getBottomRight(): DecimalPosition {
    return this.bottomRight;
  }

  getTopRight(): DecimalPosition {
    return this.topRight;
  }

  getTopLeft(): DecimalPosition {
    return this.topLeft;
  }

  getCenter(): DecimalPosition {
    if (!this.center) {
      this.center = GwtInstance.newDecimalPosition(
        (this.bottomLeft.getX() + this.bottomRight.getX() + this.topRight.getX() + this.topLeft.getX()) / 4,
        (this.bottomLeft.getY() + this.bottomRight.getY() + this.topRight.getY() + this.topLeft.getY()) / 4
      );
    }
    return this.center;
  }
}

export interface ViewFieldListener {
  onViewFieldChanged(viewField: ViewField): void;
}

@Injectable()
export class BabylonRenderServiceAccessImpl implements BabylonRenderServiceAccess {
  private scene!: Scene;
  private engine!: Engine;
  private camera!: FreeCamera;
  public shadowGenerator!: ShadowGenerator;
  private keyPressed: Map<string, number> = new Map();
  private canvas!: HTMLCanvasElement;
  private directionalLight!: DirectionalLight
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

  constructor(private gwtAngularService: GwtAngularService,
              private babylonModelService: BabylonModelService,
              private uiConfigCollectionService: UiConfigCollectionService,
              private threeJsWaterRenderService: ThreeJsWaterRenderService,
              private actionService: ActionService) {
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
      delta = delta / 240;
      delta = -delta;
      if (delta <= 0) {
        delta += self.camera.position.y * 0.2;
      } else {
        delta -= self.camera.position.y * 0.2;
      }
      const cameraRotation = Quaternion.FromEulerAngles(self.camera.rotation.x, self.camera.rotation.y, self.camera.rotation.z);
      let deltaVector = Vector3.Zero();
      new Vector3(0, 0, -delta).rotateByQuaternionToRef(cameraRotation, deltaVector);
      if (self.camera.position.y + deltaVector.y > 1 && self.camera.position.y + deltaVector.y < 200) {
        this.camera.position.x += deltaVector.x;
        this.camera.position.y += deltaVector.y;
        this.camera.position.z += deltaVector.z;
        this.onViewFieldChanged();
      }
    }, true);

    // -----  Camera -----
    //this.camera = new Camera("Main Cam", new Vector3(0, -10, 20), this.scene);
    this.camera = new FreeCamera("Camera", new Vector3(0, 30, -35), this.scene);
    this.camera.maxZ = 500;
    this.camera.setTarget(new Vector3(0, 0, 0));

    // ----- Light -----
    const lightDirection = new Vector3(-1, -2, 1);
    this.directionalLight = new DirectionalLight("DirectionalLight", lightDirection, this.scene);
    this.directionalLight.intensity = 1;
    this.directionalLight.autoCalcShadowZBounds = true;

    const hemisphericLight = new HemisphericLight("HemiLight", lightDirection, this.scene);
    hemisphericLight.diffuse = new Color3(0.945, 0.969, 0.894);
    hemisphericLight.groundColor = new Color3(0.325, 0.278, 0.055);
    hemisphericLight.intensity = 0.1;

    this.shadowGenerator = new ShadowGenerator(2048, this.directionalLight);
    this.shadowGenerator.bias = 0.006;
    this.shadowGenerator.normalBias = 0;
    this.shadowGenerator.darkness = 0.2;
    this.shadowGenerator.filter = ShadowGenerator.FILTER_PCF;
    this.shadowGenerator.filteringQuality = ShadowGenerator.QUALITY_LOW;

    // ----- Resize listener -----
    new ResizeObserver(entries => {
      for (let entry of entries) {
        if (this.canvas == entry.target) {
          this.engine.resize();
          this.onViewFieldChanged();
        }
      }
    }).observe(this.canvas);

    this.selectionFrame = new SelectionFrame(this.scene, this, this.gwtAngularService);

    // ----- Render loop -----
    let renderTime = Date.now();
    this.engine.runRenderLoop(() => {
      try {
        const date = Date.now();
        this.scrollCamera((date - renderTime) / 1000);
        this.interpolateItemPositions(date);
        this.scene.render();
        renderTime = Date.now();
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

  createBabylonBaseItem(id: number, baseItemType: BaseItemType, diplomacy: Diplomacy): BabylonBaseItem {
    try {
      return new BabylonBaseItemImpl(id,
        baseItemType,
        GwtHelper.gwtIssueStringEnum(diplomacy, Diplomacy),
        this,
        this.actionService,
        this.babylonModelService,
        this.uiConfigCollectionService);
    } catch (error) {
      console.error(error);
      return BabylonBaseItemImpl.createDummy(id);
    }
  }

  setViewFieldCenter(x: number, y: number): void {
    let currentViewFieldCenter = this.setupCenterGroundPosition();
    let newFieldCenter = new Vector2(x, y);
    let delta = newFieldCenter.subtract(new Vector2(currentViewFieldCenter.x, currentViewFieldCenter.z));
    this.camera.position.x += delta.x;
    this.camera.position.z += delta.y;
    this.onViewFieldChanged();
  }

  setup(canvas: HTMLCanvasElement) {
    this.canvas = canvas;
    this.engine = new Engine(this.canvas)
    this.scene = new Scene(this.engine);
    this.scene.ambientColor = new Color3(0.3, 0.3, 0.3);

    this.babylonModelService.setScene(this.scene);
    this.baseItemContainer = new TransformNode("Base items");
    this.resourceItemContainer = new TransformNode("Resource items");
    this.boxItemContainer = new TransformNode("Box items");
    this.projectileMaterial = new SimpleMaterial("Projectile", this.scene);
    this.projectileMaterial.diffuseColor = new Color3(0, 0, 0);
  }

  scrollCamera(delta: number) {
    let hasChanged = false;
    for (let [key, start] of this.keyPressed) {
      const duration = new Date().getTime() - start;

      let distance = Math.sqrt(duration + 200) * 0.01 + 0.05;

      distance = distance * delta * 0.03;

      distance = distance + this.camera.position.y * 0.03;

      switch (key) {
        case 'ArrowUp': {
          hasChanged = true;
          this.camera.position.z += distance;
          break;
        }
        case 'ArrowDown': {
          hasChanged = true;
          this.camera.position.z -= distance;
          break;
        }
        case 'ArrowRight': {
          hasChanged = true;
          this.camera.position.x += distance;
          break;
        }
        case 'ArrowLeft': {
          hasChanged = true;
          this.camera.position.x -= distance;
          break;
        }
        default:
      }
    }
    if (hasChanged) {
      this.onViewFieldChanged();
    }
  }

  public setupCenterGroundPosition(): Vector3 {
    return this.setupZeroLevelPosition(0, 0, Matrix.Invert(this.camera.getTransformationMatrix()));
  }

  addTerrainTileToScene(transformNode: TransformNode): void {
    this.scene.addTransformNode(transformNode);
    this.addShadowCaster(transformNode);
  }

  addShadowCaster(transformNode: TransformNode): void {
    transformNode.getChildMeshes().forEach(childMesh => {
      this.shadowGenerator.addShadowCaster(childMesh, true);
    });
  }

  removeTerrainTileFromScene(transformNode: TransformNode): void {
    this.scene.removeTransformNode(transformNode);
    transformNode.getChildMeshes().forEach(childMesh => {
      this.shadowGenerator.removeShadowCaster(childMesh, true);
    });
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
        let nodeMaterial = this.babylonModelService.getNodeMaterial(markerConfig.outOfViewNodesMaterialId!);
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
      let nodeMaterial = this.babylonModelService.getNodeMaterial(markerConfig.placeNodesMaterialId!);
      nodeMaterial.ignoreAlpha = false; // Can not be saved in the NodeEditor
      // nodeMaterial.material.depthFunction = Constants.ALWAYS;
      this.placeMarkerMesh.material = nodeMaterial;
    }
  }

  private createPlacePolygonMarker(placeConfig: PlaceConfig): Mesh {
    let polygonData = PlaceConfigComponent.toVertex2ArrayAngular(placeConfig.getPolygon2D()?.toCornersAngular()!)
    let polygonTriangulation = new PolygonMeshBuilder("Place marker", polygonData, this.scene);
    const polygonMesh = polygonTriangulation.build();
    polygonMesh.position.y = 0.1 + LocationVisualization.getHeightFromTerrain(polygonData[0].x, polygonData[0].y, this);
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
    const diskMesh = MeshBuilder.CreateDisc("Place marker", {radius: radius}, this.scene);
    diskMesh.position.x = placeConfig.getPosition()?.getX()!;
    diskMesh.position.y = 0.1 + LocationVisualization.getHeightFromTerrain(placeConfig.getPosition()?.getX()!, placeConfig.getPosition()?.getY()!, this);
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

  private setupZeroLevelPosition(ndcX: number, ndcY: number, invertCameraViewProj: Matrix): Vector3 {
    let worldNearPosition = Vector3.TransformCoordinates(new Vector3(ndcX, ndcY, -1), invertCameraViewProj);
    let direction = worldNearPosition.subtract(this.camera.position).normalize();
    const distanceToNullLevel = -this.camera.position.y / direction.y;
    return this.camera.position.add(direction.multiplyByFloats(distanceToNullLevel, distanceToNullLevel, distanceToNullLevel));
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
    let ray = new Ray(new Vector3(position.getX(), -100, position.getY()), new Vector3(0, 1, 0), 1000);

    return this.scene.pickWithRay(ray,
      (mesh: AbstractMesh) => {
        let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(mesh);
        if (!razarionMetadata) {
          return false;
        }
        return razarionMetadata.type == RazarionMetadataType.GROUND || razarionMetadata.type == RazarionMetadataType.SLOPE;
      }
    );
  }

  public setupTerrainPickPoint(): PickingInfo {
    return this.scene.pick(this.scene.pointerX, this.scene.pointerY, (mesh: AbstractMesh) => {
      let razarionMetadata = BabylonRenderServiceAccessImpl.getRazarionMetadata(mesh);
      if (!razarionMetadata) {
        return false;
      }
      return razarionMetadata.type == RazarionMetadataType.GROUND || razarionMetadata.type == RazarionMetadataType.SLOPE;
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
      import("@babylonjs/node-editor")
    ]).then((_values) => {
      this.scene.debugLayer.show({enableClose: true, embedMode: true});
    });
  }

  createBabylonResourceItem(id: number, resourceItemType: ResourceItemType): BabylonResourceItem {
    try {
      return new BabylonResourceItemImpl(id,
        resourceItemType,
        this,
        this.actionService,
        this.babylonModelService,
        this.uiConfigCollectionService);
    } catch (error) {
      console.error(error);
      return BabylonResourceItemImpl.createDummy(id);
    }
  }

  createBabylonBoxItem(id: number, boxItemType: BoxItemType): BabylonBoxItem {
    try {
      return new BabylonBoxItemImpl(id,
        boxItemType,
        this,
        this.actionService,
        this.babylonModelService,
        this.uiConfigCollectionService);
    } catch (error) {
      console.error(error);
      return BabylonBoxItemImpl.createDummy(id);
    }
  }

  createBaseItemPlacerPresenter(): BaseItemPlacerPresenter {
    return new BaseItemPlacerPresenterImpl(this);
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

  public createParticleSystem(particleSystemEntityId: number | null, imageId: number | null, emitterPosition: AbstractMesh | Vector3, destination: Vector3 | null, stretchToDestination: boolean): ParticleSystem {
    if (!particleSystemEntityId && particleSystemEntityId !== 0) {
      throw new Error("babylonModelId not set");
    }
    const particleJson = this.babylonModelService.getParticleSystemJson(particleSystemEntityId);

    const particleSystem = ParticleSystem.Parse(particleJson, this.scene, "");
    particleSystem.emitter = emitterPosition;

    let correctedImageId = GwtHelper.gwtIssueNumber(imageId);
    if (correctedImageId || correctedImageId === 0) {
      particleSystem.particleTexture = new Texture(getImageUrl(correctedImageId));
    }
    if (destination) {
      const beam = destination.subtract(<Vector3>emitterPosition);
      const delta = 2;
      const direction1 = beam.subtractFromFloats(delta, delta, delta).normalize();
      const direction2 = beam.subtractFromFloats(-delta, -delta, -delta).normalize();
      particleSystem.createPointEmitter(direction1, direction2);
      if (stretchToDestination) {
        const distance = beam.length();
        particleSystem.minLifeTime = distance;
        particleSystem.maxLifeTime = distance;
      }
    }
    return particleSystem;
  }

  getAllBabylonTerrainTile(): BabylonTerrainTileImpl[] {
    return this.editorTerrainTileContainer;
  }

}

