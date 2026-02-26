import {
  Color3,
  Mesh,
  MeshBuilder,
  Nullable,
  Observer,
  PointerEventTypes,
  PointerInfo,
  StandardMaterial,
  Tools,
  Vector3
} from "@babylonjs/core";
import {BaseItemPlacer, BaseItemPlacerPresenter, Diplomacy} from "src/app/gwtangular/GwtAngularFacade";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {BabylonModelService} from "./babylon-model.service";
import {BabylonAudioService} from "./babylon-audio.service";
import {AdvancedDynamicTexture} from "@babylonjs/gui";
import {RenderObject} from './render-object';
import {PressMouseVisualization} from './press-mouse-visualization';

export enum BaseItemPlacerPresenterEvent {
  ACTIVATED,
  PLACED,
  DEACTIVATED
}

export class BaseItemPlacerPresenterImpl implements BaseItemPlacerPresenter {
  private disc: Mesh | null = null;
  private renderObject: RenderObject | null = null;
  private uiTexture: AdvancedDynamicTexture | null = null;
  private pressMouseVisualization: PressMouseVisualization | null = null;
  private readonly material;
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;
  private baseItemPlacerCallback: ((event: BaseItemPlacerPresenterEvent) => void) | null = null;
  private keydownHandler: ((event: KeyboardEvent) => void) | null = null;

  constructor(private rendererService: BabylonRenderServiceAccessImpl,
              private babylonModelService: BabylonModelService,
              private babylonAudioService: BabylonAudioService) {
    this.material = new StandardMaterial("Base Item Placer", this.rendererService.getScene());
    this.material.emissiveColor = Color3.Red()
  }

  activate(baseItemPlacer: BaseItemPlacer): void {
    this.disc = MeshBuilder.CreateDisc("Base Item Placer", {radius: baseItemPlacer.getEnemyFreeRadius()}, this.rendererService.getScene());
    this.disc.visibility = 0.5;
    this.disc.material = this.material;
    this.disc.rotation.x = Tools.ToRadians(90);
    this.disc.isPickable = false;
    this.disc.position.y = 0.1;

    const positionValid = baseItemPlacer.isPositionValid();
    this.material.emissiveColor = positionValid ? Color3.Green() : Color3.Red();

    this.renderObject = this.babylonModelService.cloneModel3D(baseItemPlacer.getModel3DId()!, null, Diplomacy.OWN_PLACER);
    this.renderObject.setRotationY(Tools.ToRadians(90));

    this.uiTexture = AdvancedDynamicTexture.CreateFullscreenUI("Base item placer");
    this.uiTexture.disablePicking = true; // Prevent mouse down on terrain cursor change
    this.pressMouseVisualization = new PressMouseVisualization(positionValid, this.rendererService);
    this.uiTexture.addControl(this.pressMouseVisualization.getContainer());
    this.pressMouseVisualization.getContainer().linkWithMesh(this.disc!);
    this.pressMouseVisualization.getContainer().linkOffsetY = -100;

    let pickedPoint = this.setupPickedPoint();
    if (pickedPoint) {
      this.setPosition(baseItemPlacer, pickedPoint);
    } else {
      this.setupPickedPointDelayed(baseItemPlacer);
    }

    this.rendererService.baseItemPlacerActive = true;

    this.pointerObservable = this.rendererService.getScene().onPointerObservable.add((pointerInfo) => {
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERUP: {
          let pickingInfo = this.rendererService.setupTerrainPickPoint();
          if (pickingInfo.hit) {
            this.setPosition(baseItemPlacer, pickingInfo.pickedPoint!);
            if (this.baseItemPlacerCallback) {
              this.baseItemPlacerCallback(BaseItemPlacerPresenterEvent.PLACED);
            }

            if (baseItemPlacer.isPlayBuildSound()) {
              this.babylonAudioService.speakCommand('Building');
            }
            baseItemPlacer.onPlace(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z);
          }
          break;
        }
        case PointerEventTypes.POINTERMOVE: {
          let pickingInfo = this.rendererService.setupTerrainPickPoint();
          if (pickingInfo.hit) {
            this.setPosition(baseItemPlacer, pickingInfo.pickedPoint!);
          }
          break;
        }
      }
    });

    this.keydownHandler = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && baseItemPlacer.isCanBeCanceled()) {
        baseItemPlacer.cancel();
      }
    };
    window.addEventListener('keydown', this.keydownHandler);

    if (this.baseItemPlacerCallback) {
      this.baseItemPlacerCallback(BaseItemPlacerPresenterEvent.ACTIVATED);
    }
  }

  private setupPickedPoint(): Vector3 | null {
    if (this.rendererService.hasPendingSetViewFieldCenter()) {
      return null;
    }
    let centerPickingInfo = this.rendererService.setupPickInfoFromNDC(0, 0);
    if (centerPickingInfo.hit && centerPickingInfo.pickedPoint) {
      return centerPickingInfo.pickedPoint;
    } else {
      return null;
    }
  }

  private setupPickedPointDelayed(baseItemPlacer: BaseItemPlacer) {
    setTimeout(() => {
      let pickedPoint = this.setupPickedPoint();
      if (pickedPoint) {
        this.setPosition(baseItemPlacer, pickedPoint);
      } else {
        this.setupPickedPointDelayed(baseItemPlacer);
      }
    }, 1000);
  }

  deactivate(): void {
    if (this.keydownHandler) {
      window.removeEventListener('keydown', this.keydownHandler);
      this.keydownHandler = null;
    }
    this.rendererService.getScene().onPointerObservable.remove(this.pointerObservable);
    this.rendererService.getScene().removeMesh(this.disc!);
    this.disc?.dispose();
    this.disc = null;
    this.renderObject?.dispose();
    this.renderObject = null;
    this.rendererService.baseItemPlacerActive = false;
    this.uiTexture?.dispose();
    this.uiTexture = null;
    this.pressMouseVisualization = null;
    if (this.baseItemPlacerCallback) {
      this.baseItemPlacerCallback(BaseItemPlacerPresenterEvent.DEACTIVATED);
    }
  }

  setBaseItemPlacerCallback(callback: ((event: BaseItemPlacerPresenterEvent) => void) | null) {
    this.baseItemPlacerCallback = callback;
  }

  private setPosition(baseItemPlacer: BaseItemPlacer, pickedPoint: Vector3) {
    if (!this.disc) return;
    baseItemPlacer.onMove(pickedPoint.x, pickedPoint.z);
    this.disc.position = pickedPoint
    this.disc.position.y += 0.1;
    const positionValid = baseItemPlacer.isPositionValid();
    this.material.emissiveColor = positionValid ? Color3.Green() : Color3.Red();
    this.pressMouseVisualization?.setPositionValid(positionValid);
    this.renderObject?.setPosition(pickedPoint);
    this.renderObject?.increaseHeight(0.01);
  }
}
