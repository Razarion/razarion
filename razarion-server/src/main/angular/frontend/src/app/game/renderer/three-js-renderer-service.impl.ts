import {Injectable} from "@angular/core";
import {TerrainTile, ThreeJsRendererServiceAccess, ThreeJsTerrainTile} from "src/app/gwtangular/GwtAngularFacade";
import {ThreeJsTerrainTileImpl} from "./three-js-terrain-tile.impl";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {BabylonModelService} from "./babylon-model.service";
import {ThreeJsWaterRenderService} from "./three-js-water-render.service";
import * as BABYLON from 'babylonjs';
import {Scene, Vector3} from 'babylonjs';
import Quaternion = BABYLON.Quaternion;

export class ThreeJsRendererServiceMouseEvent {
  object3D: any = null;
  pointOnObject3D: BABYLON.Vector3 | null = null;
  razarionTerrainObject3D: any = null;
  razarionTerrainObjectId: number | null = null;
  razarionTerrainObjectConfigId: number | null = null;

}

export interface ThreeJsRendererServiceMouseEventListener {
  onThreeJsRendererServiceMouseEvent(threeJsRendererServiceMouseEvent: ThreeJsRendererServiceMouseEvent): void;
}

@Injectable()
export class ThreeJsRendererServiceImpl implements ThreeJsRendererServiceAccess {
  private scene!: BABYLON.Scene;
  private engine!: BABYLON.Engine;
  private camera!: BABYLON.FreeCamera;
  private keyPressed: Map<string, number> = new Map();
  private canvas!: HTMLCanvasElement;
  private directionalLight!: BABYLON.DirectionalLight
  private mouseListeners: ThreeJsRendererServiceMouseEventListener[] = [];
  private gizmoManager!: BABYLON.GizmoManager;

  constructor(private gwtAngularService: GwtAngularService, private threeJsModelService: BabylonModelService, private threeJsWaterRenderService: ThreeJsWaterRenderService) {
  }

  internalSetup(canvas: HTMLCanvasElement) {
    this.engine = new BABYLON.Engine(canvas)
    this.scene = new BABYLON.Scene(this.engine);
    this.scene.debugLayer.show({enableClose: true, embedMode: true});

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
    this.camera = new BABYLON.FreeCamera("Camera", new BABYLON.Vector3(0, 10, -10), this.scene);
    this.camera.setTarget(new BABYLON.Vector3(0, 0, 0));

    // ----- Light -----
    this.directionalLight = new BABYLON.DirectionalLight("DirectionalLight", new BABYLON.Vector3(0.12, -0.98, 0.15), this.scene);
    // --- gizmo
    const lightGizmo = new BABYLON.LightGizmo();
    lightGizmo.light = this.directionalLight;

    this.gizmoManager = new BABYLON.GizmoManager(this.scene);
    this.gizmoManager.positionGizmoEnabled = true;
    this.gizmoManager.rotationGizmoEnabled = true;
    this.gizmoManager.usePointerToAttachGizmos = false;
    this.gizmoManager.attachToMesh(lightGizmo.attachedMesh);
    // --- gizmo ends

    // ----- Resize listener -----
    const resizeObserver = new ResizeObserver(entries => {
      for (let entry of entries) {
        if (this.canvas == entry.target) {
          this.engine.resize();
        }
      }
    });
    resizeObserver.observe(this.canvas);


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

  createTerrainTile(terrainTile: TerrainTile, defaultGroundConfigId: number): ThreeJsTerrainTile {
    try {
      return new ThreeJsTerrainTileImpl(terrainTile,
        defaultGroundConfigId,
        this.scene,
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
    // let currentViewFieldCenter = this.setupCenterGroundPosition();
    // let newFiledCenter = new Vector2(x, y);
    // let delta = newFiledCenter.sub(new Vector2(currentViewFieldCenter.x, currentViewFieldCenter.y));
    // this.camera.position.x += delta.x;
    // this.camera.position.y += delta.y;
    this.onViewFieldChanged();
  }

  onResize() {
    // TODO this.renderer.setSize(this.canvas.clientWidth - 5, this.canvas.clientHeight); // TODO -> -5 prevent strange loop
    // TODO this.camera.aspect = (this.canvas.clientWidth - 5) / this.canvas.clientHeight;
    // TODO this.camera.updateProjectionMatrix();
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

  intersectObjects(mousePosition: BABYLON.Vector2): any {
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

  // public setupCenterGroundPosition(): BABYLON.Vector3 {
  //   return this.setupZeroLevelPosition(0, 0);
  // }

  public addToSceneEditor(scene: Scene) {
    // let group = new Group();
    // group.add(scene);
    // group.name = "Imported";
    // let groundPos = this.setupCenterGroundPosition();
    // group.position.set(groundPos.x, groundPos.y, groundPos.z);
    // TODO this.scene.add(group);
  }

  addScene(scene: BABYLON.Scene) {

  }

  removeScene(scene: BABYLON.Scene) {

  }

  getEngine(): BABYLON.Engine {
    return this.engine;
  }

  getScene(): BABYLON.Scene {
    return this.scene;
  }

  private onViewFieldChanged() {
    if (this.gwtAngularService.gwtAngularFacade.inputService === undefined) {
      return;
    }
    let invertCameraViewProj = BABYLON.Matrix.Invert(this.camera.getTransformationMatrix());

    const bottomLeft = this.setupZeroLevelPosition(-1, -1, invertCameraViewProj);
    const bottomRight = this.setupZeroLevelPosition(1, -1, invertCameraViewProj);
    const topRight = this.setupZeroLevelPosition(1, 1, invertCameraViewProj);
    const topLeft = this.setupZeroLevelPosition(-1, 1, invertCameraViewProj);

    this.gwtAngularService.gwtAngularFacade.inputService.onViewFieldChanged(
      bottomLeft.x, bottomLeft.z,
      bottomRight.x, bottomRight.z,
      topRight.x, topRight.z,
      topLeft.x, topLeft.z
    );
  }

  private setupZeroLevelPosition(ndcX: number, ndcY: number, invertCameraViewProj: BABYLON.Matrix): BABYLON.Vector3 {
    let ndcToWorld = BABYLON.Vector3.TransformCoordinates(new BABYLON.Vector3(ndcX, ndcY, 0), invertCameraViewProj);
    const direction = ndcToWorld.subtract(this.camera.position).normalize();
    const distanceToNullLevel = -this.camera.position.y / direction.y;
    return this.camera.position.add(direction.multiplyByFloats(distanceToNullLevel, distanceToNullLevel, distanceToNullLevel));
  }

  private onMousedownEvent(this: ThreeJsRendererServiceImpl, event: any): void {
    let newMouseEvent = new ThreeJsRendererServiceMouseEvent();
    let intersection = this.intersectObjects(new BABYLON.Vector2(event.clientX, event.clientY));
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
}

