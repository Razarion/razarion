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
  private rallyDisc: Mesh | null = null;
  private rallyOffsetX = 0;
  private rallyOffsetZ = 0;
  // One ghost model per unit to be placed, with its offset relative to the cursor.
  // DecimalPosition.x maps to world-X, DecimalPosition.y maps to world-Z.
  private renderObjects: RenderObject[] = [];
  private relativeOffsets: { x: number; z: number }[] = [];
  private uiTexture: AdvancedDynamicTexture | null = null;
  private pressMouseVisualization: PressMouseVisualization | null = null;
  private readonly material;
  private readonly rallyMaterial;
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;
  private baseItemPlacerCallback: ((event: BaseItemPlacerPresenterEvent) => void) | null = null;
  private keydownHandler: ((event: KeyboardEvent) => void) | null = null;
  private activationGeneration = 0;

  constructor(private rendererService: BabylonRenderServiceAccessImpl,
              private babylonModelService: BabylonModelService,
              private babylonAudioService: BabylonAudioService) {
    this.material = new StandardMaterial("Base Item Placer", this.rendererService.getScene());
    this.material.emissiveColor = Color3.Red()
    this.rallyMaterial = new StandardMaterial("Base Item Rally", this.rendererService.getScene());
    this.rallyMaterial.emissiveColor = Color3.Red();
  }

  activate(baseItemPlacer: BaseItemPlacer): void {
    this.cleanupPreviousPlacer();
    this.activationGeneration++;
    const currentGeneration = this.activationGeneration;
    this.disc = MeshBuilder.CreateDisc("Base Item Placer", {radius: baseItemPlacer.getEnemyFreeRadius()}, this.rendererService.getScene());
    this.disc.visibility = 0.5;
    this.disc.material = this.material;
    this.disc.rotation.x = Tools.ToRadians(90);
    this.disc.isPickable = false;
    this.disc.position.y = 0.1;

    if (baseItemPlacer.hasRallyPoint()) {
      // DecimalPosition.y maps to world-Z (game uses XZ ground plane).
      this.rallyOffsetX = baseItemPlacer.getRallyOffsetX();
      this.rallyOffsetZ = baseItemPlacer.getRallyOffsetY();
      this.rallyDisc = MeshBuilder.CreateDisc("Base Item Rally", {radius: baseItemPlacer.getRallyRadius()}, this.rendererService.getScene());
      this.rallyDisc.visibility = 0.5;
      this.rallyDisc.material = this.rallyMaterial;
      this.rallyDisc.rotation.x = Tools.ToRadians(90);
      this.rallyDisc.isPickable = false;
      this.rallyDisc.position.y = 0.1;
    }

    const positionValid = baseItemPlacer.isPositionValid();
    this.material.emissiveColor = positionValid ? Color3.Green() : Color3.Red();
    this.rallyMaterial.emissiveColor = positionValid ? Color3.Green() : Color3.Red();

    this.relativeOffsets = [];
    const relativePositions = baseItemPlacer.getRelativeItemPositions() || [];
    if (relativePositions.length > 0) {
      for (const relativePosition of relativePositions) {
        this.relativeOffsets.push({x: relativePosition.getX(), z: relativePosition.getY()});
      }
    } else {
      this.relativeOffsets.push({x: 0, z: 0});
    }
    for (let i = 0; i < this.relativeOffsets.length; i++) {
      const renderObject = this.babylonModelService.cloneModel3D(baseItemPlacer.getModel3DId()!, null, Diplomacy.OWN_PLACER);
      renderObject.setRotationY(Tools.ToRadians(90));
      this.renderObjects.push(renderObject);
    }

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
      this.setupPickedPointDelayed(baseItemPlacer, currentGeneration);
    }

    this.rendererService.baseItemPlacerActive = true;

    this.pointerObservable = this.rendererService.getScene().onPointerObservable.add((pointerInfo) => {
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERDOWN: {
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

  private setupPickedPointDelayed(baseItemPlacer: BaseItemPlacer, generation: number) {
    setTimeout(() => {
      if (this.activationGeneration !== generation) return;
      let pickedPoint = this.setupPickedPoint();
      if (pickedPoint) {
        this.setPosition(baseItemPlacer, pickedPoint);
      } else {
        this.setupPickedPointDelayed(baseItemPlacer, generation);
      }
    }, 1000);
  }

  private cleanupPreviousPlacer(): void {
    if (this.keydownHandler) {
      window.removeEventListener('keydown', this.keydownHandler);
      this.keydownHandler = null;
    }
    if (this.pointerObservable) {
      this.rendererService.getScene().onPointerObservable.remove(this.pointerObservable);
      this.pointerObservable = null;
    }
    if (this.disc) {
      this.rendererService.getScene().removeMesh(this.disc);
      this.disc.dispose();
      this.disc = null;
    }
    if (this.rallyDisc) {
      this.rendererService.getScene().removeMesh(this.rallyDisc);
      this.rallyDisc.dispose();
      this.rallyDisc = null;
    }
    for (const renderObject of this.renderObjects) {
      renderObject.dispose();
    }
    this.renderObjects = [];
    this.relativeOffsets = [];
    if (this.uiTexture) {
      this.uiTexture.dispose();
      this.uiTexture = null;
    }
    this.pressMouseVisualization = null;
  }

  deactivate(): void {
    this.cleanupPreviousPlacer();
    // Defer clearing so ActionManager handlers (terrain/water click) that fire
    // in the same event loop tick still see the placer as active.
    // Use generation check to avoid undoing a subsequent activate().
    const gen = this.activationGeneration;
    setTimeout(() => {
      if (this.activationGeneration === gen) {
        this.rendererService.baseItemPlacerActive = false;
      }
    }, 0);
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
    if (this.rallyDisc) {
      this.rallyDisc.position = new Vector3(pickedPoint.x + this.rallyOffsetX, this.disc.position.y, pickedPoint.z + this.rallyOffsetZ);
    }
    const positionValid = baseItemPlacer.isPositionValid();
    this.material.emissiveColor = positionValid ? Color3.Green() : Color3.Red();
    this.rallyMaterial.emissiveColor = positionValid ? Color3.Green() : Color3.Red();
    this.pressMouseVisualization?.setPositionValid(positionValid);
    for (let i = 0; i < this.renderObjects.length; i++) {
      const offset = this.relativeOffsets[i];
      this.renderObjects[i].setPosition(new Vector3(pickedPoint.x + offset.x, pickedPoint.y, pickedPoint.z + offset.z));
      this.renderObjects[i].increaseHeight(0.01);
    }
  }
}
