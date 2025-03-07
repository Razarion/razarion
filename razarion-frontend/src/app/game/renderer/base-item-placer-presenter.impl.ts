import {
  Color3,
  Mesh,
  MeshBuilder,
  Nullable,
  Observer,
  PointerEventTypes,
  PointerInfo,
  Tools,
  TransformNode
} from "@babylonjs/core";
import {SimpleMaterial} from "@babylonjs/materials";
import {BaseItemPlacer, BaseItemPlacerPresenter, Diplomacy} from "src/app/gwtangular/GwtAngularFacade";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {BabylonModelService} from "./babylon-model.service";

export class BaseItemPlacerPresenterImpl implements BaseItemPlacerPresenter {
  private disc: Mesh | null = null;
  private model3D: TransformNode | null = null;
  private material;
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;

  constructor(private rendererService: BabylonRenderServiceAccessImpl,
              private babylonModelService: BabylonModelService) {
    this.material = new SimpleMaterial("Base Item Placer", this.rendererService.getScene());
    this.material.diffuseColor = Color3.Red()
  }

  activate(baseItemPlacer: BaseItemPlacer): void {
    this.disc = MeshBuilder.CreateDisc("Base Item Placer", {radius: baseItemPlacer.getEnemyFreeRadius()}, this.rendererService.getScene());
    this.disc.visibility = 0.5;
    this.disc.material = this.material;
    this.disc.rotation.x = Tools.ToRadians(90);
    this.disc.isPickable = false;
    this.disc.position.y = 0.1;
    this.material.diffuseColor = baseItemPlacer.isPositionValid() ? Color3.Green() : Color3.Red();

    this.model3D = this.babylonModelService.cloneModel3D(baseItemPlacer.getModel3DId()!, null, Diplomacy.OWN_PLACER);
    this.model3D.rotationQuaternion = null;
    this.model3D.rotation.y = Tools.ToRadians(90);

    this.rendererService.baseItemPlacerActive = true;
    this.pointerObservable = this.rendererService.getScene().onPointerObservable.add((pointerInfo) => {
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERUP: {
          let pickingInfo = this.rendererService.setupTerrainPickPoint();
          if (pickingInfo.hit) {
            if (pickingInfo.hit) {
              this.disc!.position = pickingInfo.pickedPoint!
              this.disc!.position.y += +0.01;
              this.material.diffuseColor = baseItemPlacer.isPositionValid() ? Color3.Green() : Color3.Red();
              this.model3D!.position = pickingInfo.pickedPoint!
              this.model3D!.position.y += +0.01;
              baseItemPlacer.onPlace(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z);
            }
          }
          break;
        }
        case PointerEventTypes.POINTERMOVE: {
          let pickingInfo = this.rendererService.setupTerrainPickPoint();
          if (pickingInfo.hit) {
            baseItemPlacer.onMove(pickingInfo.pickedPoint!.x, pickingInfo.pickedPoint!.z);
            this.disc!.position = pickingInfo.pickedPoint!
            this.disc!.position.y += +0.01;
            this.material.diffuseColor = baseItemPlacer.isPositionValid() ? Color3.Green() : Color3.Red();
            this.model3D!.position = pickingInfo.pickedPoint!
            this.model3D!.position.y += +0.01;
          }
          break;
        }
      }
    });
  }

  deactivate(): void {
    this.rendererService.getScene().onPointerObservable.remove(this.pointerObservable);
    this.rendererService.getScene().removeMesh(this.disc!);
    this.disc?.dispose();
    this.disc = null;
    this.model3D?.dispose();
    this.model3D = null;
    this.rendererService.baseItemPlacerActive = false;
  }
}
