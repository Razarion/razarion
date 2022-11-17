import {Injectable} from "@angular/core";
import {TerrainTile, ThreeJsRendererServiceAccess, ThreeJsTerrainTile} from "src/app/gwtangular/GwtAngularFacade";
import {ThreeJsTerrainTileImpl} from "./three-js-terrain-tile.impl";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {ThreeJsModelService} from "./three-js-model.service";
import {ThreeJsWaterRenderService} from "./three-js-water-render.service";
import * as BABYLON from 'babylonjs';
import {Scene} from 'babylonjs';
import {Vector3} from "babylonjs";
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
  // private camera!: BABYLON.Camera;
  private camera!: BABYLON.FreeCamera;
  private keyPressed: Map<string, number> = new Map();
  private canvas!: HTMLCanvasElement;
  private directionalLight!: BABYLON.DirectionalLight
  private mouseListeners: ThreeJsRendererServiceMouseEventListener[] = [];

  constructor(private gwtAngularService: GwtAngularService, private threeJsModelService: ThreeJsModelService, private threeJsWaterRenderService: ThreeJsWaterRenderService) {
  }

  internalSetup(canvas: HTMLCanvasElement) {
    this.engine = new BABYLON.Engine(canvas)
    this.scene = new BABYLON.Scene(this.engine);
    this.scene.useRightHandedSystem = true;
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
        delta += self.camera.position.z * 0.2;
      } else {
        delta -= self.camera.position.z * 0.2;
      }
      const cameraRotation = Quaternion.FromEulerAngles(self.camera.rotation.x, self.camera.rotation.y, self.camera.rotation.z);
      let deltaVector = Vector3.Zero();
      new Vector3(0, 0, -delta).rotateByQuaternionToRef(cameraRotation, deltaVector);
      if (self.camera.position.z + deltaVector.z > 1 && self.camera.position.z + deltaVector.z < 200) {
        this.camera.position.x += deltaVector.x;
        this.camera.position.y += deltaVector.y;
        this.camera.position.z += deltaVector.z;
        this.onViewFieldChanged();
      }
    }, true);

    // -----  Camera -----
    //this.camera = new BABYLON.Camera("Main Cam", new BABYLON.Vector3(0, -10, 20), this.scene);
    this.camera = new BABYLON.FreeCamera("camera1", new BABYLON.Vector3(10, -20, 20), this.scene);
    this.camera.setTarget(new BABYLON.Vector3(10, 20, 0));

    // ----- Light -----
    this.directionalLight = new BABYLON.DirectionalLight("DirectionalLight", new BABYLON.Vector3(0, 0, -1), this.scene);

    // ----- Helpers -----
    const axisX = BABYLON.Mesh.CreateLines("axisX",
      [new BABYLON.Vector3(0, 0, 0), new BABYLON.Vector3(10, 0, 0)],
      this.scene,
      false);
    axisX.color = new BABYLON.Color3(1, 0, 0);

    const axisY = BABYLON.Mesh.CreateLines("axisY",
      [new BABYLON.Vector3(0, 0, 0), new BABYLON.Vector3(0, 10, 0)],
      this.scene,
      false);
    axisY.color = new BABYLON.Color3(0, 1, 0);

    const axisZ = BABYLON.Mesh.CreateLines("axisZ",
      [new BABYLON.Vector3(0, 0, 0), new BABYLON.Vector3(0, 0, 10)],
      this.scene,
      false);
    axisZ.color = new BABYLON.Color3(0, 0, 1);

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

      distance = distance * delta / 0.016;

      distance = distance + this.camera.position.z * 0.02;

      switch (key) {
        case 'ArrowUp': {
          hasChanged = true;
          this.camera.position.y += distance;
          break;
        }
        case 'ArrowDown': {
          hasChanged = true;
          this.camera.position.y -= distance;
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

  public setupCenterGroundPosition(): BABYLON.Vector3 {
    return this.setupGroundPosition(0, 0);
  }

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

  private onViewFieldChanged() {
    if (this.gwtAngularService.gwtAngularFacade.inputService === undefined) {
      return;
    }
    // let bottomLeft = this.setupGroundPosition(-1, -1);
    // let bottomRight = this.setupGroundPosition(1, -1);
    // let topRight = this.setupGroundPosition(1, 1);
    // let topLeft = this.setupGroundPosition(-1, 1);

    // this.directionalLight.shadow.camera.left = topLeft.x;
    // this.directionalLight.shadow.camera.bottom = bottomLeft.y;
    // this.directionalLight.shadow.camera.top = topLeft.y;
    // this.directionalLight.shadow.camera.right = topRight.x;
    // this.directionalLight.shadow.camera.updateMatrix();
    // this.directionalLight.shadow.camera.updateProjectionMatrix();

    let bottomLeft = new BABYLON.Vector2(0, 0);
    let bottomRight = new BABYLON.Vector2(0, 400);
    let topRight = new BABYLON.Vector2(400, 400);
    let topLeft = new BABYLON.Vector2(0, 400);

    this.gwtAngularService.gwtAngularFacade.inputService.onViewFieldChanged(
      bottomLeft.x, bottomLeft.y,
      bottomRight.x, bottomRight.y,
      topRight.x, topRight.y,
      topLeft.x, topLeft.y
    );
  }

  private setupGroundPosition(ndcX: number, ndcY: number): BABYLON.Vector3 {
    // let raycaster = new Raycaster();
    // raycaster.setFromCamera({x: ndcX, y: ndcY}, this.camera);
    // let factor = this.camera.position.z / -raycaster.ray.direction.z;
    // let pointOnGround = raycaster.ray.direction.clone().setLength(factor);
    // pointOnGround.add(this.camera.position);
    // TODO return new Vector3(pointOnGround.x, pointOnGround.y, pointOnGround.z);
    throw new Error("...TODO...");
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

