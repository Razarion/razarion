import {Injectable} from "@angular/core";
import {
  BabylonBaseItem,
  BaseItemPlacer,
  BaseItemPlacerPresenter,
  Diplomacy,
  MeshContainer,
  ShapeTransform,
  TerrainTile,
  ThreeJsRendererServiceAccess,
  ThreeJsTerrainTile,
  Vertex
} from "src/app/gwtangular/GwtAngularFacade";
import {ThreeJsTerrainTileImpl} from "./three-js-terrain-tile.impl";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {BabylonModelService} from "./babylon-model.service";
import {ThreeJsWaterRenderService} from "./three-js-water-render.service";
import {
  Animation,
  CascadedShadowGenerator,
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
  Quaternion,
  Scene,
  Tools,
  TransformNode,
  Vector2,
  Vector3
} from "@babylonjs/core";
import {SimpleMaterial} from "@babylonjs/materials";
import {GwtHelper} from "../../gwtangular/GwtHelper";

export class ThreeJsRendererServiceMouseEvent {
  object3D: any = null;
  pointOnObject3D: Vector3 | null = null;
  razarionTerrainObject3D: any = null;
  razarionTerrainObjectId: number | null = null;
  razarionTerrainObjectConfigId: number | null = null;

}

export interface ThreeJsRendererServiceMouseEventListener {
  onThreeJsRendererServiceMouseEvent(threeJsRendererServiceMouseEvent: ThreeJsRendererServiceMouseEvent): void;
}

@Injectable()
export class ThreeJsRendererServiceImpl implements ThreeJsRendererServiceAccess {
  private scene!: Scene;
  private engine!: Engine;
  private camera!: FreeCamera;
  private shadowGenerator!: CascadedShadowGenerator;
  private keyPressed: Map<string, number> = new Map();
  private canvas!: HTMLCanvasElement;
  private directionalLight!: DirectionalLight
  private mouseListeners: ThreeJsRendererServiceMouseEventListener[] = [];
  private meshContainers!: MeshContainer[];
  private diplomacyMaterialCache: Map<number, Map<Diplomacy, NodeMaterial>> = new Map<number, Map<Diplomacy, NodeMaterial>>();
  private itemMarkerMaterialCache: Map<Diplomacy, SimpleMaterial> = new Map<Diplomacy, SimpleMaterial>();

  constructor(private gwtAngularService: GwtAngularService, private threeJsModelService: BabylonModelService, private threeJsWaterRenderService: ThreeJsWaterRenderService) {
  }

  internalSetup(canvas: HTMLCanvasElement) {
    this.engine = new Engine(canvas)
    this.scene = new Scene(this.engine);
    this.scene.ambientColor = new Color3(0.3, 0.3, 0.3);
    this.scene.environmentTexture = CubeTexture.CreateFromPrefilteredData("https://playground.babylonjs.com/textures/countrySpecularHDR.dds", this.scene);
    this.scene.environmentIntensity = 0.04;
    this.threeJsModelService.setScene(this.scene);

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
    this.camera = new FreeCamera("Camera", new Vector3(0, 20, -10), this.scene);
    this.camera.maxZ = 500;
    this.camera.setTarget(new Vector3(0, 0, 0));

    // ----- Light -----
    this.directionalLight = new DirectionalLight("DirectionalLight", new Vector3(0.5, -1, 0).normalize(), this.scene);
    this.directionalLight.intensity = 5;

    this.shadowGenerator = new CascadedShadowGenerator(4096, this.directionalLight);
    this.shadowGenerator.bias = 0.005;
    // this.shadowGenerator.debug = true;

    // ----- Resize listener -----
    new ResizeObserver(entries => {
      for (let entry of entries) {
        if (this.canvas == entry.target) {
          this.engine.resize();
          this.onViewFieldChanged();
        }
      }
    }).observe(this.canvas);

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

  initMeshContainers(meshContainers: MeshContainer[]): void {
    this.meshContainers = meshContainers;
  }

  createTerrainTile(terrainTile: TerrainTile, defaultGroundConfigId: number): ThreeJsTerrainTile {
    try {
      return new ThreeJsTerrainTileImpl(terrainTile,
        defaultGroundConfigId,
        this.gwtAngularService,
        this,
        this.threeJsModelService,
        this.threeJsWaterRenderService);
    } catch (e) {
      console.error(`Error createTerrainTile() with index ${terrainTile.getIndex()}`)
      console.error(e);
      throw e;
    }
  }

  createSyncBaseItem(id: number, threeJsModelPackConfigId: number | null, meshContainerId: number | null, internalName: string, diplomacy: Diplomacy, radius: number): BabylonBaseItem {
    try {
      const correctedDiplomacy = GwtHelper.gwtIssueStringEnum(diplomacy, Diplomacy);
      const threeJsRendererServiceImpl = this;
      return new class implements BabylonBaseItem {
        private mesh: Mesh;
        private markerDisc: Mesh | null = null;
        private selectActive: boolean = false;
        private hoverActive: boolean = false;
        private position: Vertex | null = null;
        private angle: number = 0;
        private health: number = 0;

        constructor() {
          if(threeJsModelPackConfigId) {
            this.mesh = <Mesh>threeJsRendererServiceImpl.threeJsModelService.cloneMesh(threeJsModelPackConfigId, null);
            this.mesh.name = `${internalName} '${id}')`;
            try {
              // If TransformNode is returned, it has no getBoundingInfo() methode
              this.mesh.getBoundingInfo();
              threeJsRendererServiceImpl.shadowGenerator.addShadowCaster(this.mesh, true);
            } catch(error) {
              this.mesh.getChildMeshes().forEach(childMesh => {
                threeJsRendererServiceImpl.shadowGenerator.addShadowCaster(childMesh, true);
              });
            }
          } else if (meshContainerId) {
            this.mesh = threeJsRendererServiceImpl.showMeshContainer(threeJsRendererServiceImpl.meshContainers,
              GwtHelper.gwtIssueNumber(meshContainerId),
              correctedDiplomacy);
            this.mesh.name = `${internalName} '${id}')`;
            threeJsRendererServiceImpl.shadowGenerator.addShadowCaster(this.mesh, true);
          } else {
            this.mesh = MeshBuilder.CreateSphere(`No threeJsModelPackConfigId or meshContainerId for ${internalName}`, {diameter: radius * 2});
            console.warn(`No meshContainerId for ${internalName}`)
            this.mesh.name = `! ${internalName} '${id}')`;
          }
        }

        getId(): number {
          return id;
        }

        getAngle(): number {
          return this.angle;
        }

        getHealth(): number {
          return this.angle;
        }

        getPosition(): Vertex | null {
          return this.position;
        }

        setAngle(angle: number): void {
          this.angle = angle;
        }

        setHealth(health: number): void {
          this.health = health;
        }

        setPosition(position: Vertex): void {
          this.position = position;
        }

        dispose(): void {
          threeJsRendererServiceImpl.scene.removeMesh(this.mesh);
          this.mesh.dispose();
        }

        updatePosition(): void {
          if (this.position) {
            this.mesh.position.x = this.position.getX();
            this.mesh.position.y = this.position.getZ();
            this.mesh.position.z = this.position.getY();
          }
        }

        updateAngle(): void {
          this.mesh.rotation.y = Tools.ToRadians(90) - this.angle;
        }

        updateHealth(): void {
        }

        select(active: boolean): void {
          this.selectActive = active;
          this.updateMarkedDisk();
        }

        hover(active: boolean): void {
          this.hoverActive = active;
          this.updateMarkedDisk();
        }

        private updateMarkedDisk(): void {
          if (this.selectActive || this.hoverActive) {
            if (!this.markerDisc) {
              this.markerDisc = MeshBuilder.CreateDisc("Base Item Marker", {radius: radius + 0.1});
              let material = threeJsRendererServiceImpl.itemMarkerMaterialCache.get(correctedDiplomacy);
              if(!material) {
                material = new SimpleMaterial(`Base Item Marker ${correctedDiplomacy}`, threeJsRendererServiceImpl.scene);
                material.diffuseColor = ThreeJsRendererServiceImpl.color4Diplomacy(correctedDiplomacy);
                threeJsRendererServiceImpl.itemMarkerMaterialCache.set(correctedDiplomacy, material);
              }
              this.markerDisc.material = material;
              this.markerDisc.position.y = 0.01;
              this.markerDisc.rotation.x = Tools.ToRadians(90);
              this.markerDisc.parent = this.mesh;
            }
          } else {
            if (this.markerDisc) {
              this.markerDisc.dispose();
              this.markerDisc = null;
            }
          }

          if (this.selectActive) {
            (<SimpleMaterial>this.markerDisc!.material).alpha = 0.6
          } else if (this.hoverActive) {
            (<SimpleMaterial>this.markerDisc!.material).alpha = 0.3
          }
        }
      }
    } catch (error) {
      console.error(error);
      return new class implements BabylonBaseItem {
        getAngle(): number {
          return 0;
        }

        getHealth(): number {
          return 0;
        }

        getPosition(): Vertex | null {
          return null;
        }

        setAngle(angle: number): void {
        }

        setHealth(health: number): void {
        }

        setPosition(position: Vertex): void {
        }

        getId(): number {
          return id;
        }

        setup(): void {
        }

        dispose(): void {
        }

        updateAngle(): void {
        }

        updateHealth(): void {
        }

        updatePosition(): void {
        }

        select(active: boolean): void {
        }

        hover(active: boolean): void {
        }
      }
    }
  }

  createProjectile(start: Vertex, destination: Vertex, duration: number): void {
    const box = MeshBuilder.CreateSphere("Projectile", {diameter: 0.1, segments: 1}, this.scene);

    const frameRate = 1;
    const xSlide = new Animation("Projectile",
      "position",
      frameRate,
      Animation.ANIMATIONTYPE_VECTOR3,
      Animation.ANIMATIONLOOPMODE_CONSTANT);

    const keyFrames = [];

    keyFrames.push({
      frame: 0,
      value: new Vector3(start.getX(), start.getZ(), start.getY())
    });

    keyFrames.push({
      frame: 1,
      value: new Vector3(destination.getX(), destination.getZ(), destination.getY())
    });

    xSlide.setKeys(keyFrames);

    box.animations.push(xSlide);

    let animatable = this.scene.beginAnimation(box, 0, 1, false, 1.0 / duration);
    animatable.onAnimationEnd = () => {
      this.scene.removeMesh(box);
      box.dispose();
    };


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
    try {
      this.internalSetup(canvas);
    } catch (err) {
      console.error(err);
    }
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

  addMouseDownHandler(mouseListener: ThreeJsRendererServiceMouseEventListener): void {
    this.mouseListeners.push(mouseListener)
  }

  removeMouseDownHandler(mouseListener: ThreeJsRendererServiceMouseEventListener): void {
    this.mouseListeners = this.mouseListeners.filter(obj => obj !== mouseListener);
  }

  intersectObjects(mousePosition: Vector2): any {
    // const raycaster = new Raycaster();
    // const ndcPointer = new Vector2();
    // ndcPointer.x = (mousePosition.x / this.renderer.domElement.width) * 2 - 1;
    // ndcPointer.y = -(mousePosition.y / this.renderer.domElement.height) * 2 + 1;
    //
    // raycaster.setFromCamera(ndcPointer, this.camera);
    // let intersections: Intersection[] = [];
    // raycaster.intersectObjects(this.scene.children, true, intersections);
    // if (intersections.length == 0) {
    //   return null;
    // }
    // TODO return intersections[0];
    return null;
  }

  public setupCenterGroundPosition(): Vector3 {
    return this.setupZeroLevelPosition(0, 0, Matrix.Invert(this.camera.getTransformationMatrix()));
  }

  public addToSceneEditor(scene: Scene) {
    // let group = new Group();
    // group.add(scene);
    // group.name = "Imported";
    // let groundPos = this.setupCenterGroundPosition();
    // group.position.set(groundPos.x, groundPos.y, groundPos.z);
    // TODO this.scene.add(group);
  }

  addToScene(mesh: Mesh): void {
    this.scene.addMesh(mesh);
    this.shadowGenerator.addShadowCaster(mesh, true);
  }

  removeFromScene(mesh: Mesh): void {
    this.scene.removeMesh(mesh);
    this.shadowGenerator.removeShadowCaster(mesh, true);
  }

  getEngine(): Engine {
    return this.engine;
  }

  getScene(): Scene {
    return this.scene;
  }

  private onViewFieldChanged() {
    if (this.gwtAngularService.gwtAngularFacade.inputService === undefined) {
      return;
    }
    try {
      // make sure the transformation matrix we get when calling 'getTransformationMatrix()' is calculated with an up to date view matrix
      //getViewMatrix forces recalculation of the camera view matrix
      this.camera.getViewMatrix();

      let invertCameraViewProj = Matrix.Invert(this.camera.getTransformationMatrix());

      const bottomLeft = this.setupZeroLevelPosition(-1, -1, invertCameraViewProj);
      const bottomRight = this.setupZeroLevelPosition(1, -1, invertCameraViewProj);
      const topRight = this.setupZeroLevelPosition(1, 1, invertCameraViewProj);
      const topLeft = this.setupZeroLevelPosition(-1, 1, invertCameraViewProj);

      // console.info(`ViewField BL ${bottomLeft.x}:${bottomLeft.z}:${bottomLeft.y} BR ${bottomRight.x}:${bottomRight.z}:${bottomRight.y} TR ${topRight.x}:${topRight.z}:${topRight.y} TL ${topLeft.x}:${topLeft.z}:${topLeft.y}`)

      this.gwtAngularService.gwtAngularFacade.inputService.onViewFieldChanged(
        bottomLeft.x, bottomLeft.z,
        bottomRight.x, bottomRight.z,
        topRight.x, topRight.z,
        topLeft.x, topLeft.z
      );
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

  public setupMeshPickPoint(pointerX: number, pointerY: number): Vector3 | undefined {
    const pickResult = this.scene.pick(pointerX, pointerY);

    if (!pickResult.hit) {
      return undefined;
    }
    return pickResult.pickedPoint!;
  }

  private onMousedownEvent(this: ThreeJsRendererServiceImpl, event: any): void {
    let newMouseEvent = new ThreeJsRendererServiceMouseEvent();
    let intersection = this.intersectObjects(new Vector2(event.clientX, event.clientY));
    if (intersection != null) {
      newMouseEvent.object3D = intersection.object;
      newMouseEvent.pointOnObject3D = intersection.point;
      this.recursivelySearchTerrainObject(newMouseEvent.object3D, newMouseEvent);
    }
    this.mouseListeners.forEach(mouseListener => mouseListener.onThreeJsRendererServiceMouseEvent(newMouseEvent));
  }

  private recursivelySearchTerrainObject(object3D: any, newMouseEvent: ThreeJsRendererServiceMouseEvent) {
    if ((<any>object3D).razarionTerrainObjectId) {
      newMouseEvent.razarionTerrainObjectId = (<any>object3D).razarionTerrainObjectId;
    }
    if ((<any>object3D).razarionTerrainObjectConfigId) {
      newMouseEvent.razarionTerrainObjectConfigId = (<any>object3D).razarionTerrainObjectConfigId;
    }
    if (newMouseEvent.razarionTerrainObjectId || newMouseEvent.razarionTerrainObjectConfigId) {
      newMouseEvent.razarionTerrainObject3D = object3D;
      return;
    }
    if (object3D.parent != null) {
      this.recursivelySearchTerrainObject(object3D.parent, newMouseEvent);
    }
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

  private showMeshContainer(meshContainers: MeshContainer[], id: number, diplomacy: Diplomacy): Mesh {
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
    let baseItemContainer = new Mesh(`BaseItems '${id}' AssetConfig '${foundMeshContainer.getInternalName()}'`);
    this.scene.addMesh(baseItemContainer);
    this.recursivelyFillMeshes(foundMeshContainer!, baseItemContainer, diplomacy);
    return baseItemContainer;
  }


  private createMesh(threeJsModelId: number, element3DId: string, parent: Node, diplomacy: Diplomacy, shapeTransforms: ShapeTransform[] | null) {
    let assetContainer = this.threeJsModelService.getAssetContainer(threeJsModelId);
    let threeJsModelConfig = this.threeJsModelService.getThreeJsModelConfig(threeJsModelId);

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
          cachedMaterial = this.threeJsModelService.getNodeMaterial(threeJsModelConfig.getNodeMaterialId()!).clone(`${threeJsModelConfig.getNodeMaterialId()} '${diplomacy}'`);
          const diplomacyColorNode = (<NodeMaterial>cachedMaterial).getBlockByPredicate(block => {
            return "DiplomacyColor" === block.name;
          });
          if (diplomacyColorNode) {
            (<InputBlock>diplomacyColorNode).value = ThreeJsRendererServiceImpl.color4Diplomacy(diplomacy);
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

  private recursivelyFillMeshes(meshContainer: MeshContainer, parent: Node, diplomacy: Diplomacy) {
    if (meshContainer.getMesh() && meshContainer.getMesh()!.getThreeJsModelId()) {
      this.createMesh(meshContainer.getMesh()!.getThreeJsModelId()!,
        meshContainer.getMesh()!.getElement3DId(),
        parent,
        diplomacy,
        meshContainer.getMesh()!.getShapeTransformsArray());
    }
    if (meshContainer.getChildrenArray()) {
      meshContainer!.getChildrenArray()?.forEach(childMeshContainer => {
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
          let pickPoint = this.setupMeshPickPoint(this.scene.pointerX, this.scene.pointerY);
          if(!pickPoint) {
            return;
          }
          this.gwtAngularService.gwtAngularFacade.inputService.onMouseDown(pickPoint.x, pickPoint.z);
          break;
        }
        case PointerEventTypes.POINTERUP: {
          let pickPoint = this.setupMeshPickPoint(this.scene.pointerX, this.scene.pointerY);
          if(!pickPoint) {
            return;
          }
          this.gwtAngularService.gwtAngularFacade.inputService.onMouseUp(pickPoint.x, pickPoint.z);
          break;
        }
        case PointerEventTypes.POINTERMOVE: {
          let pickPoint = this.setupMeshPickPoint(this.scene.pointerX, this.scene.pointerY);
          if (!pickPoint) {
            return;
          }
          this.gwtAngularService.gwtAngularFacade.inputService.onMouseMove(pickPoint.x, pickPoint.z, (pointerInfo.event.buttons & 1) === 1);
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
        disc = MeshBuilder.CreateDisc("Base Item Placer", {radius: baseItemPlacer.getEnemyFreeRadius()}, threeJsRendererServiceImpl.scene);
        disc.visibility = 0.5;
        disc.material = material;
        disc.rotation.x = Tools.ToRadians(90);
        this.updatePosition(disc, baseItemPlacer);
        disc.position.y = 0.1;
        material.diffuseColor = baseItemPlacer.isPositionValid() ? Color3.Green() : Color3.Red();
        disc.onBeforeRenderObservable.add(eventData => {
          if (disc) {
            this.updatePosition(disc, baseItemPlacer);
            material.diffuseColor = baseItemPlacer.isPositionValid() ? Color3.Green() : Color3.Red();
          }
        })
      }

      private updatePosition(disc: Mesh, baseItemPlacer: BaseItemPlacer) {
        if (baseItemPlacer.getPosition()) {
          disc.position.x = baseItemPlacer.getPosition().getX();
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

  public static color4Diplomacy(diplomacy: Diplomacy): Color3 {
    switch (diplomacy) {
      case Diplomacy.OWN:
        return Color3.Green()
      case Diplomacy.ENEMY:
        return Color3.Red()
      case Diplomacy.FRIEND:
        return Color3.Yellow()
    }
    return Color3.Gray()
  }
}

