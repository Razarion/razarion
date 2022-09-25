import {Injectable} from "@angular/core";
import {
  AmbientLight,
  Camera,
  Clock,
  DirectionalLight,
  Group,
  Light,
  MathUtils,
  Object3D,
  PCFSoftShadowMap,
  PerspectiveCamera,
  Raycaster,
  Vector2,
  Vector3,
  WebGLRenderTarget
} from "three";
import {WebGLRenderer} from "three/src/renderers/WebGLRenderer";
import {Scene} from "three/src/scenes/Scene";
import {TerrainTile, ThreeJsRendererServiceAccess, ThreeJsTerrainTile} from "src/app/gwtangular/GwtAngularFacade";
import {ThreeJsTerrainTileImpl} from "./three-js-terrain-tile.impl";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {ThreeJsModelService} from "./three-js-model.service";
import {ShadowMapViewer} from 'three/examples/jsm/utils/ShadowMapViewer';
import {ThreeJsWaterRenderService} from "./three-js-water-render.service";
import {Intersection} from "three/src/core/Raycaster";
import { nodeFrame as nodeFrame } from 'three/examples/jsm/renderers/webgl/nodes/WebGLNodes';
export class ThreeJsRendererServiceMouseEvent {
  object3D: Object3D | null = null;
  pointOnObject3D: Vector3 | null = null;
  razarionTerrainObject3D: Object3D | null = null;
  razarionTerrainObjectId: number | null = null;
  razarionTerrainObjectConfigId: number | null = null;

}

export interface ThreeJsRendererServiceMouseEventListener {
  onThreeJsRendererServiceMouseEvent(threeJsRendererServiceMouseEvent: ThreeJsRendererServiceMouseEvent): void;
}

@Injectable()
export class ThreeJsRendererServiceImpl implements ThreeJsRendererServiceAccess {
  scene = new Scene();
  slopeScene = new Scene();
  slopeInnerGroundScene = new Scene();
  camera: PerspectiveCamera = new PerspectiveCamera;
  private keyPressed: Map<string, number> = new Map();
  private canvasDiv!: HTMLDivElement;
  private renderer!: WebGLRenderer
  private directionalLight!: DirectionalLight
  private slopeRenderTarget!: WebGLRenderTarget;
  private slopeInnerGroundRenderTarget!: WebGLRenderTarget;
  private mouseListeners: ThreeJsRendererServiceMouseEventListener[] = [];

  constructor(private gwtAngularService: GwtAngularService, private threeJsModelService: ThreeJsModelService, private threeJsWaterRenderService: ThreeJsWaterRenderService) {
    this.scene.name = "Main Scene"
    this.slopeScene.name = "Splatting Slope"
    this.slopeInnerGroundScene.name = "Splatting Slope Ground"
  }

  private static createHUD(light: Light): ShadowMapViewer {
    let lightShadowMapViewer = new ShadowMapViewer(light);
    lightShadowMapViewer.position.x = 10;
    lightShadowMapViewer.position.y = 500;
    lightShadowMapViewer.size.width = 256;
    lightShadowMapViewer.size.height = 256;
    lightShadowMapViewer.update();
    return lightShadowMapViewer;
  }

  internalSetup(canvasHolder: HTMLDivElement) {
    let clock = new Clock();

    this.camera = new PerspectiveCamera(75, canvasHolder.offsetWidth / canvasHolder.offsetHeight, 0.1, 1000);
    this.camera.name = "Camera";

    this.renderer = new WebGLRenderer({antialias: true});
    this.renderer.setSize(canvasHolder.offsetWidth, canvasHolder.offsetHeight);
    canvasHolder.appendChild(this.renderer.domElement);
    this.slopeInnerGroundRenderTarget = new WebGLRenderTarget(canvasHolder.offsetWidth, canvasHolder.offsetHeight);
    this.slopeRenderTarget = new WebGLRenderTarget(canvasHolder.offsetWidth, canvasHolder.offsetHeight);

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
        delta += self.camera.position.z * 0.1;
      } else {
        delta -= self.camera.position.z * 0.1;
      }
      if (self.camera.position.z + delta > 1 && self.camera.position.z + delta < 200) {
        self.camera.translateZ(delta);
        this.onViewFieldChanged();
      }
    }, true);

    // Mouse
    this.renderer.domElement.addEventListener("mousedown", e => this.onMousedownEvent(e));

    // -----  Camera -----
    this.camera.position.x = 0;
    this.camera.position.y = -10;
    this.camera.position.z = 20;
    this.camera.rotation.x = MathUtils.degToRad(45);

    // ----- Light -----
    let ambientLight = new AmbientLight(0xFFFFFF, 0.70);
    ambientLight.name = "Ambient Light"
    this.scene.add(ambientLight);
    this.slopeScene.add(ambientLight.clone());
    this.slopeInnerGroundScene.add(ambientLight.clone());

    this.directionalLight = new DirectionalLight(0xFFFFFF, 0.80);
    this.directionalLight.name = "Directional Light"
    this.directionalLight.position.set(50, -50, 100);
    this.directionalLight.target.position.set(0, 0, -100);

    // ----- Shadow -----
    this.renderer.shadowMap.enabled = true;
    this.renderer.shadowMap.type = PCFSoftShadowMap; // default THREE.PCFShadowMap
    this.directionalLight.castShadow = true;
    this.directionalLight.shadow.mapSize.width = 2048; // default
    this.directionalLight.shadow.mapSize.height = 2048; // default
    this.directionalLight.shadow.camera.near = 0.5; // default
    this.directionalLight.shadow.camera.far = 500; // default
    this.directionalLight.shadow.camera.left = -50;
    this.directionalLight.shadow.camera.bottom = -50;
    this.directionalLight.shadow.camera.top = 150;
    this.directionalLight.shadow.camera.right = 150;
    let lightShadowMapViewer = ThreeJsRendererServiceImpl.createHUD(this.directionalLight);

    this.scene.add(this.directionalLight);
    this.slopeScene.add(this.directionalLight.clone());
    this.slopeInnerGroundScene.add(this.directionalLight.clone());

    // ----- Render loop -----
    function animate() {
      const delta = clock.getDelta();

      self.threeJsWaterRenderService.update();

      requestAnimationFrame(animate);
      self.scrollCamera(delta, self.camera);

      self.renderer.setRenderTarget(self.slopeRenderTarget);
      self.renderer.clear();
      self.renderer.render(self.slopeScene, self.camera);

      self.renderer.setRenderTarget(self.slopeInnerGroundRenderTarget);
      self.renderer.clear();
      self.renderer.render(self.slopeInnerGroundScene, self.camera);

      self.renderer.setRenderTarget(null);
      self.renderer.clear();
      nodeFrame.update();
      self.renderer.render(self.scene, self.camera);

      lightShadowMapViewer.render(self.renderer);
    }

    animate();
  }

  createTerrainTile(terrainTile: TerrainTile, defaultGroundConfigId: number): ThreeJsTerrainTile {
    try {
      return new ThreeJsTerrainTileImpl(terrainTile,
        defaultGroundConfigId,
        this.scene,
        this.slopeScene,
        this.slopeInnerGroundScene,
        this.slopeRenderTarget,
        this.slopeInnerGroundRenderTarget,
        this.gwtAngularService,
        this.threeJsModelService,
        this.threeJsWaterRenderService);
    } catch (e) {
      console.error(`Error createTerrainTile() with index ${terrainTile.getIndex()}`)
      console.error(e);
      throw e;
    }
  }

  setViewFieldCenter(x: number, y: number): void {
    let currentViewFieldCenter = this.setupCenterGroundPosition();
    let newFiledCenter = new Vector2(x, y);
    let delta = newFiledCenter.sub(new Vector2(currentViewFieldCenter.x, currentViewFieldCenter.y));
    this.camera.position.x += delta.x;
    this.camera.position.y += delta.y;
    this.onViewFieldChanged();
  }

  onResize() {
    this.renderer.setSize(this.canvasDiv.clientWidth - 5, this.canvasDiv.clientHeight); // TODO -> -5 prevent strange loop
    this.slopeInnerGroundRenderTarget.setSize(this.canvasDiv.clientWidth - 5, this.canvasDiv.clientHeight); // TODO -> -5 prevent strange loop
    this.slopeRenderTarget.setSize(this.canvasDiv.clientWidth - 5, this.canvasDiv.clientHeight); // TODO -> -5 prevent strange loop
    this.camera.aspect = (this.canvasDiv.clientWidth - 5) / this.canvasDiv.clientHeight;
    this.camera.updateProjectionMatrix();
    this.onViewFieldChanged();
  }

  setup(canvasHolder: HTMLDivElement) {
    this.canvasDiv = canvasHolder;
    try {
      this.internalSetup(canvasHolder);
    } catch (err) {
      console.error(err);
    }
  }

  scrollCamera(delta: number, camera: Camera) {
    let hasChanged = false;
    for (let [key, start] of this.keyPressed) {
      const duration = new Date().getTime() - start;

      let distance = Math.sqrt(duration + 200) * 0.01 + 0.05;

      distance = distance * delta / 0.016;

      distance = distance + camera.position.z * 0.02;

      switch (key) {
        case 'ArrowUp': {
          hasChanged = true;
          camera.position.y += distance;
          break;
        }
        case 'ArrowDown': {
          hasChanged = true;
          camera.position.y -= distance;
          break;
        }
        case 'ArrowRight': {
          hasChanged = true;
          camera.position.x += distance;
          break;
        }
        case 'ArrowLeft': {
          hasChanged = true;
          camera.position.x -= distance;
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

  intersectObjects(mousePosition: Vector2): Intersection | null {
    const raycaster = new Raycaster();
    const ndcPointer = new Vector2();
    ndcPointer.x = (mousePosition.x / this.renderer.domElement.width) * 2 - 1;
    ndcPointer.y = -(mousePosition.y / this.renderer.domElement.height) * 2 + 1;

    raycaster.setFromCamera(ndcPointer, this.camera);
    let intersections: Intersection[] = [];
    raycaster.intersectObjects(this.scene.children, true, intersections);
    if (intersections.length == 0) {
      return null;
    }
    return intersections[0];
  }

  public setupCenterGroundPosition(): Vector3 {
    return this.setupGroundPosition(0, 0);
  }

  public addToSceneEditor(scene: Scene) {
    let group = new Group();
    group.add(scene);
    group.name = "Imported";
    let groundPos = this.setupCenterGroundPosition();
    group.position.set(groundPos.x, groundPos.y, groundPos.z);
    this.scene.add(group);
  }

  private onViewFieldChanged() {
    if (this.gwtAngularService.gwtAngularFacade.inputService === undefined) {
      return;
    }
    let bottomLeft = this.setupGroundPosition(-1, -1);
    let bottomRight = this.setupGroundPosition(1, -1);
    let topRight = this.setupGroundPosition(1, 1);
    let topLeft = this.setupGroundPosition(-1, 1);

    this.directionalLight.shadow.camera.left = topLeft.x;
    this.directionalLight.shadow.camera.bottom = bottomLeft.y;
    this.directionalLight.shadow.camera.top = topLeft.y;
    this.directionalLight.shadow.camera.right = topRight.x;
    this.directionalLight.shadow.camera.updateMatrix();
    this.directionalLight.shadow.camera.updateProjectionMatrix();

    this.gwtAngularService.gwtAngularFacade.inputService.onViewFieldChanged(
      bottomLeft.x, bottomLeft.y,
      bottomRight.x, bottomRight.y,
      topRight.x, topRight.y,
      topLeft.x, topLeft.y
    );
  }

  private setupGroundPosition(ndcX: number, ndcY: number): Vector3 {
    let raycaster = new Raycaster();
    raycaster.setFromCamera({x: ndcX, y: ndcY}, this.camera);
    let factor = this.camera.position.z / -raycaster.ray.direction.z;
    let pointOnGround = raycaster.ray.direction.clone().setLength(factor);
    pointOnGround.add(this.camera.position);
    return new Vector3(pointOnGround.x, pointOnGround.y, pointOnGround.z);
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

  private recursivelySearchTerrainObject(object3D: Object3D, newMouseEvent: ThreeJsRendererServiceMouseEvent) {
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
}

