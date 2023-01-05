import {Injectable} from "@angular/core";
import {TerrainTile, ThreeJsRendererServiceAccess, ThreeJsTerrainTile} from "src/app/gwtangular/GwtAngularFacade";
import {ThreeJsTerrainTileImpl} from "./three-js-terrain-tile.impl";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {BabylonModelService} from "./babylon-model.service";
import {ThreeJsWaterRenderService} from "./three-js-water-render.service";
import {
  CascadedShadowGenerator,
  DirectionalLight,
  Engine,
  FreeCamera,
  Matrix,
  Mesh,
  Quaternion,
  Scene,
  Vector2,
  Vector3
} from "@babylonjs/core";

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

  constructor(private gwtAngularService: GwtAngularService, private threeJsModelService: BabylonModelService, private threeJsWaterRenderService: ThreeJsWaterRenderService) {
  }

  internalSetup(canvas: HTMLCanvasElement) {
    this.engine = new Engine(canvas)
    this.scene = new Scene(this.engine);

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
    this.camera = new FreeCamera("Camera", new Vector3(0, 10, -10), this.scene);
    this.camera.maxZ = 500;
    this.camera.setTarget(new Vector3(0, 0, 0));

    // ----- Light -----
    this.directionalLight = new DirectionalLight("DirectionalLight", new Vector3(0, -2, 0), this.scene);
    this.directionalLight.position = new Vector3(1, 100, 0);

    this.shadowGenerator = new CascadedShadowGenerator(4096, this.directionalLight);
    // this.shadowGenerator.debug = true;

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

  setViewFieldCenter(x: number, y: number): void {
    let currentViewFieldCenter = this.setupCenterGroundPosition();
    let newFiledCenter = new Vector2(x, y);
    let delta = newFiledCenter.subtract(new Vector2(currentViewFieldCenter.x, currentViewFieldCenter.z));
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
    let invertCameraViewProj = Matrix.Invert(this.camera.getTransformationMatrix());

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

  private setupZeroLevelPosition(ndcX: number, ndcY: number, invertCameraViewProj: Matrix): Vector3 {
    let ndcToWorld = Vector3.TransformCoordinates(new Vector3(ndcX, 0, ndcY), invertCameraViewProj);
    const direction = ndcToWorld.subtract(this.camera.position).normalize();
    const distanceToNullLevel = -this.camera.position.y / direction.y;
    return this.camera.position.add(direction.multiplyByFloats(distanceToNullLevel, distanceToNullLevel, distanceToNullLevel));
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
}

