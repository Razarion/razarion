import { Injectable } from "@angular/core";
import {
  BabylonBaseItem,
  BabylonRenderServiceAccess,
  BabylonResourceItem,
  BaseItemPlacer,
  BaseItemPlacerPresenter,
  BaseItemType,
  Diplomacy,
  MeshContainer,
  ResourceItemType,
  ShapeTransform,
  TerrainObjectPosition,
  TerrainSlopePosition,
  TerrainTile,
  BabylonTerrainTile,
  DecimalPosition,
  BabylonBoxItem,
  BoxItemType,
  MarkerConfig,
  PlaceConfig,
} from "src/app/gwtangular/GwtAngularFacade";
import { BabylonTerrainTileImpl } from "./babylon-terrain-tile.impl";
import { GwtAngularService } from "src/app/gwtangular/GwtAngularService";
import { BabylonModelService } from "./babylon-model.service";
import { ThreeJsWaterRenderService } from "./three-js-water-render.service";
import {
  AbstractMesh,
  Color3,
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
  PointerEventTypes,
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
import { SimpleMaterial } from "@babylonjs/materials";
import { GwtHelper } from "../../gwtangular/GwtHelper";
import { PickingInfo } from "@babylonjs/core/Collisions/pickingInfo";
import { BabylonBaseItemImpl } from "./babylon-base-item.impl";
import { BabylonResourceItemImpl } from "./babylon-resource-item.impl";
import { SelectionFrame } from "./selection-frame";
import { GwtInstance } from "src/app/gwtangular/GwtInstance";
import { BabylonBoxItemImpl } from "./babylon-box-item.impl";
import { Geometry } from "src/app/common/geometry";
import { PlaceConfigComponent } from "src/app/editor/common/place-config/place-config.component";
import { LocationVisualization } from "src/app/editor/common/place-config/location-visualization";
import { ActionService } from "../action.service";

export interface RazarionMetadata {
  type: RazarionMetadataType;
  id: number | undefined;
  configId: number | undefined;
  editorHintTerrainObjectPosition: TerrainObjectPosition | undefined;
  editorHintSlopePolygon: Vector2[] | undefined;
  editorHintSlopePosition: TerrainSlopePosition | undefined;
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
  public meshContainers!: MeshContainer[];
  private diplomacyMaterialCache: Map<number, Map<Diplomacy, NodeMaterial>> = new Map<number, Map<Diplomacy, NodeMaterial>>();
  public readonly itemMarkerMaterialCache: Map<Diplomacy, SimpleMaterial> = new Map<Diplomacy, SimpleMaterial>();
  public baseItemContainer!: TransformNode;
  public resourceItemContainer!: TransformNode;
  public boxItemContainer!: TransformNode;
  public projectileMaterial!: SimpleMaterial;
  private selectionFrame!: SelectionFrame;
  private viewFieldListeners: ViewFieldListener[] = [];
  private viewField?: ViewField;
  private outOfViewPlane?: Mesh;
  private placeMarkerMesh?: Mesh;

  constructor(private gwtAngularService: GwtAngularService,
    private babylonModelService: BabylonModelService,
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

  runRenderer(meshContainers: MeshContainer[]): void {
    this.meshContainers = meshContainers;
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
    this.directionalLight = new DirectionalLight("DirectionalLight", new Vector3(0, -50, 0), this.scene);
    this.directionalLight.intensity = 2;
    this.directionalLight.autoUpdateExtends = false;
    this.shadowGenerator = new ShadowGenerator(1024, this.directionalLight);
    this.shadowGenerator.bias = 0.0001;
    this.shadowGenerator.normalBias = 0;

    // ----- Resize listener -----
    new ResizeObserver(entries => {
      for (let entry of entries) {
        if (this.canvas == entry.target) {
          this.engine.resize();
          this.onViewFieldChanged();
        }
      }
    }).observe(this.canvas);

    this.selectionFrame = new SelectionFrame(this.scene, this.gwtAngularService.gwtAngularFacade.selectionHandler, this);
    this.setupPointerInteraction();

    // ----- Render loop -----
    let renderTime = Date.now();
    this.engine.runRenderLoop(() => {
      try {
        this.scrollCamera((Date.now() - renderTime) / 1000);
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
      return new BabylonTerrainTileImpl(terrainTile,
        this.gwtAngularService,
        this,
        this.actionService,
        this.babylonModelService,
        this.threeJsWaterRenderService);
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
        this.babylonModelService);
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
    this.scene.environmentTexture = CubeTexture.CreateFromPrefilteredData("https://playground.babylonjs.com/textures/countrySpecularHDR.dds", this.scene);
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
        this.outOfViewPlane = MeshBuilder.CreatePlane("Out of view plane", { size: markerConfig.outOfViewSize }, this.scene);
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
    let polygonTriangulation = new PolygonMeshBuilder("Place marker", polygonData, this.scene, Geometry.EAR_CUT);
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
    const diskMesh = MeshBuilder.CreateDisc("Place marker", { radius: radius }, this.scene);
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

      this.directionalLight.orthoLeft = this.viewField.getBottomLeft().getX();
      this.directionalLight.orthoBottom = this.viewField.getBottomLeft().getY();
      this.directionalLight.orthoTop = this.viewField.getTopRight().getY();
      this.directionalLight.orthoRight = this.viewField.getBottomRight().getX();

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
      this.scene.debugLayer.show({ enableClose: true, embedMode: true });
    });
  }

  createBabylonResourceItem(id: number, resourceItemType: ResourceItemType): BabylonResourceItem {
    try {
      return new BabylonResourceItemImpl(id,
        resourceItemType,
        this,
        this.actionService,
        this.babylonModelService);
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
        this.babylonModelService);
    } catch (error) {
      console.error(error);
      return BabylonBoxItemImpl.createDummy(id);
    }
  }

  public showMeshContainer(meshContainers: MeshContainer[], id: number, diplomacy: Diplomacy): TransformNode {
    let foundMeshContainer = null;
    for (let meshContainer of meshContainers) {
      if (meshContainer.getId() === id) {
        foundMeshContainer = meshContainer;
        break;
      }
    }
    if (!foundMeshContainer) {
      throw new Error(`No MeshContainer for '${id}'`);
    }
    let baseItemContainer = new TransformNode(`'`);
    this.scene.addTransformNode(baseItemContainer);
    this.recursivelyFillMeshes(foundMeshContainer!, baseItemContainer, diplomacy);
    return baseItemContainer;
  }

  private recursivelyFillMeshes(meshContainer: MeshContainer, parent: Node, diplomacy: Diplomacy) {
    if (meshContainer.getMesh() && meshContainer.getMesh()!.getThreeJsModelId()) {
      this.createMesh(meshContainer.getMesh()!.getThreeJsModelId()!,
        meshContainer.getMesh()!.getElement3DId(),
        parent,
        diplomacy,
        meshContainer.getMesh()!.toShapeTransformsArray());
    }
    if (meshContainer.toChildrenArray()) {
      meshContainer!.toChildrenArray()?.forEach(childMeshContainer => {
        this.recursivelyFillMeshes(childMeshContainer, parent, diplomacy);
      })
    }
  }

  private findChildNode(node: Node, name: string): Node | null {
    if (node.name === name) {
      return node;
    }
    for (let childNode of node.getChildren()) {
      let childMesh = this.findChildNode(childNode, name);
      if (childMesh) {
        return childMesh;
      }
    }
    return null;
  }

  private setupPointerInteraction() {
    this.scene.onPointerObservable.add((pointerInfo) => {
      if (!this.gwtAngularService.gwtAngularFacade.inputService) {
        return;
      }
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERDOWN: {
          this.selectionFrame.onPointerDown(this.scene.pointerX, this.scene.pointerY);
          let pickingInfo = this.setupMeshPickPoint();
          if (pickingInfo.hit) {
            // TODO this.gwtAngularService.gwtAngularFacade.inputService.onMouseDown(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z, pickingInfo.pickedPoint!.y);
          }
          break;
        }
        case PointerEventTypes.POINTERUP: {
          this.selectionFrame.onPointerUp(this.scene.pointerX, this.scene.pointerY);
          let pickingInfo = this.setupMeshPickPoint();
          if (pickingInfo.hit) {
            // TODO this.gwtAngularService.gwtAngularFacade.inputService.onMouseUp(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z, pickingInfo.pickedPoint!.y);
          }
          break;
        }
        case PointerEventTypes.POINTERMOVE: {
          this.selectionFrame.onPointerMove(this.scene.pointerX, this.scene.pointerY);
          let pickingInfo = this.setupMeshPickPoint();
          if (pickingInfo.hit) {
            // TODO this.gwtAngularService.gwtAngularFacade.inputService.onMouseMove(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z, pickingInfo.pickedPoint!.y, (pointerInfo.event.buttons & 1) === 1);
          }
          break;
        }
      }
    });
  }

  createBaseItemPlacerPresenter(): BaseItemPlacerPresenter {
    const threeJsRendererServiceImpl = this;

    let disc: Mesh | null = null;
    const material = new SimpleMaterial("Base Item Placer", threeJsRendererServiceImpl.scene);
    material.diffuseColor = Color3.Red()
    return new class implements BaseItemPlacerPresenter {
      activate(baseItemPlacer: BaseItemPlacer): void {
        disc = MeshBuilder.CreateDisc("Base Item Placer", { radius: baseItemPlacer.getEnemyFreeRadius() }, threeJsRendererServiceImpl.scene);
        disc.visibility = 0.5;
        disc.material = material;
        disc.rotation.x = Tools.ToRadians(90);
        disc.isPickable = false;
        this.updatePosition(disc, baseItemPlacer);
        disc.position.y = 0.1;
        material.diffuseColor = baseItemPlacer.isPositionValid() ? Color3.Green() : Color3.Red();
        disc.onBeforeRenderObservable.add(() => {
          if (disc) {
            this.updatePosition(disc, baseItemPlacer);
            material.diffuseColor = baseItemPlacer.isPositionValid() ? Color3.Green() : Color3.Red();
          }
        })
      }

      private updatePosition(disc: Mesh, baseItemPlacer: BaseItemPlacer) {
        if (baseItemPlacer.getPosition()) {
          disc.position.x = baseItemPlacer.getPosition().getX();
          disc.position.y = baseItemPlacer.getPosition().getZ() + 0.01;
          disc.position.z = baseItemPlacer.getPosition().getY();
        } else {
          let centerPosition = threeJsRendererServiceImpl.setupCenterGroundPosition();
          disc.position.x = centerPosition.x;
          disc.position.z = centerPosition.z;
        }
      }

      deactivate(): void {
        threeJsRendererServiceImpl.scene.removeMesh(disc!);
        disc?.dispose();
        disc = null;
      }
    };
  }

  private createMesh(threeJsModelId: number, element3DId: string, parent: Node, diplomacy: Diplomacy, shapeTransforms: ShapeTransform[] | null) {
    let assetContainer = this.babylonModelService.getAssetContainer(threeJsModelId);
    let threeJsModelConfig = this.babylonModelService.getThreeJsModelConfig(threeJsModelId);

    let childMesh = null;
    for (let childNod of assetContainer.getNodes()) {
      childMesh = this.findChildNode(childNod, element3DId);
      if (childMesh) {
        break;
      }
    }
    if (childMesh) {
      let childParent: Node = parent;
      let mesh = (<Mesh>childMesh!).clone(`${element3DId} '${threeJsModelId}'`);
      if (shapeTransforms) {
        for (let shapeTransform of shapeTransforms) {
          const transform: TransformNode = new TransformNode(`${element3DId} '${threeJsModelId}'`);
          transform.ignoreNonUniformScaling = true;
          transform.position.x = shapeTransform.getTranslateX();
          transform.position.y = shapeTransform.getTranslateY();
          transform.position.z = shapeTransform.getTranslateZ();
          transform.rotationQuaternion = new Quaternion(shapeTransform.getRotateX(),
            shapeTransform.getRotateY(),
            shapeTransform.getRotateZ(),
            shapeTransform.getRotateW());
          // Strange unity behavior
          transform.scaling.x = (mesh.position.x < 0 ? -1 : 1) * shapeTransform.getScaleX();
          transform.scaling.y = shapeTransform.getScaleY();
          transform.scaling.z = shapeTransform.getScaleZ();
          transform.parent = childParent;
          childParent = transform;
        }
      }
      if (threeJsModelConfig.getNodeMaterialId()) {
        let diplomacyCache = this.diplomacyMaterialCache.get(threeJsModelConfig.getNodeMaterialId()!);
        if (!diplomacyCache) {
          diplomacyCache = new Map<Diplomacy, NodeMaterial>();
          this.diplomacyMaterialCache.set(threeJsModelConfig.getNodeMaterialId()!, diplomacyCache)
        }
        let cachedMaterial = diplomacyCache.get(diplomacy);
        if (!cachedMaterial) {
          cachedMaterial = this.babylonModelService.getNodeMaterial(threeJsModelConfig.getNodeMaterialId()!).clone(`${threeJsModelConfig.getNodeMaterialId()} '${diplomacy}'`);
          const diplomacyColorNode = (<NodeMaterial>cachedMaterial).getBlockByPredicate(block => {
            return "DiplomacyColor" === block.name;
          });
          if (diplomacyColorNode) {
            (<InputBlock>diplomacyColorNode).value = BabylonRenderServiceAccessImpl.color4Diplomacy(diplomacy);
          }
          diplomacyCache.set(diplomacy, cachedMaterial);
        }
        mesh.material = cachedMaterial;
        mesh.hasVertexAlpha = false;
      }
      mesh.parent = childParent;
      // console.log(`${element3DId} '${threeJsModelId}' ${mesh.position}`)

      mesh.position.x = 0;
      mesh.position.y = 0;
      mesh.position.z = 0;
      // this.shadowGenerator.addShadowCaster(mesh, true);
      // mesh.rotationQuaternion = null;
      // mesh.rotation.x = 0;
      // mesh.rotation.y = 0;
      // mesh.rotation.z = 0;
      // mesh.scaling.x = 1;
      // mesh.scaling.y = 1;
      // mesh.scaling.z = 1;
    } else {
      console.warn(`Can not find element3DId '${element3DId}' in threeJsModelId '${threeJsModelId}'.`)
    }
  }

  public static setRazarionMetadata(node: Node, razarionMetadata: RazarionMetadata) {
    if (!node.metadata) {
      node.metadata = {};
    }
    node.metadata.razarionMetadata = razarionMetadata;
  }

  public static setRazarionMetadataSimple(node: Node, razarionMetadataType: RazarionMetadataType, id?: number, configId?: number) {
    BabylonRenderServiceAccessImpl.setRazarionMetadata(node, new class implements RazarionMetadata {
      type = razarionMetadataType;
      id = GwtHelper.gwtIssueNumber(id);
      configId = GwtHelper.gwtIssueNumber(configId);
      editorHintTerrainObjectPosition = undefined;
      editorHintSlopePolygon = undefined;
      editorHintSlopePosition = undefined;
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

}

