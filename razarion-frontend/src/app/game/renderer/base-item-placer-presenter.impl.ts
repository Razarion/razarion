import {
  Color3,
  Mesh,
  MeshBuilder,
  Nullable,
  Observer,
  PointerEventTypes,
  PointerInfo,
  Tools,
  TransformNode,
  Vector3
} from "@babylonjs/core";
import {SimpleMaterial} from "@babylonjs/materials";
import {BaseItemPlacer, BaseItemPlacerPresenter, Diplomacy} from "src/app/gwtangular/GwtAngularFacade";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {BabylonModelService} from "./babylon-model.service";
import {AdvancedDynamicTexture, Control, Rectangle, StackPanel, TextBlock} from "@babylonjs/gui";
import {Image} from "@babylonjs/gui/2D/controls/image";
import {Animation} from "@babylonjs/core/Animations/animation";
import {RenderObject} from './render-object';

export enum BaseItemPlacerPresenterEvent {
  ACTIVATED,
  PLACED,
  DEACTIVATED
}

export class BaseItemPlacerPresenterImpl implements BaseItemPlacerPresenter {
  private disc: Mesh | null = null;
  private renderObject: RenderObject | null = null;
  private tip: Tip | null = null;
  private readonly material;
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;
  private baseItemPlacerCallback: ((event: BaseItemPlacerPresenterEvent) => void) | null = null;

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

    const positionValid = baseItemPlacer.isPositionValid();
    this.material.diffuseColor = positionValid ? Color3.Green() : Color3.Red();

    this.renderObject = this.babylonModelService.cloneModel3D(baseItemPlacer.getModel3DId()!, null, Diplomacy.OWN_PLACER);
    this.renderObject.setRotationY(Tools.ToRadians(90));

    this.tip = new Tip(positionValid, this.rendererService, this.disc!)

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
    this.rendererService.getScene().onPointerObservable.remove(this.pointerObservable);
    this.rendererService.getScene().removeMesh(this.disc!);
    this.disc?.dispose();
    this.disc = null;
    this.renderObject?.dispose();
    this.renderObject = null;
    this.rendererService.baseItemPlacerActive = false;
    this.tip?.dispose();
    this.tip = null;
    if (this.baseItemPlacerCallback) {
      this.baseItemPlacerCallback(BaseItemPlacerPresenterEvent.DEACTIVATED);
    }
  }

  setBaseItemPlacerCallback(callback: ((event: BaseItemPlacerPresenterEvent) => void) | null) {
    this.baseItemPlacerCallback = callback;
  }

  private setPosition(baseItemPlacer: BaseItemPlacer, pickedPoint: Vector3) {
    baseItemPlacer.onMove(pickedPoint.x, pickedPoint.z);
    this.disc!.position = pickedPoint
    this.disc!.position.y += +0.1;
    const positionValid = baseItemPlacer.isPositionValid();
    this.material.diffuseColor = positionValid ? Color3.Green() : Color3.Red();
    this.tip!.setPositionValid(positionValid);
    this.renderObject!.setPosition(pickedPoint);
    this.renderObject!.increaseHeight(0.01);
  }
}

class Tip {
  static readonly POSITION_VALID_TEXT = "Click left mouse button to deploy";
  static readonly POSITION_IN_VALID_TEXT = "Move mouse to find free position";
  private readonly uiTexture: AdvancedDynamicTexture;
  private readonly rect1: Rectangle;
  private readonly label: TextBlock;
  private readonly mouse: Image;
  private readonly mouseLeftButton: Image;

  constructor(positionValid: boolean,
              private rendererService: BabylonRenderServiceAccessImpl,
              transformNode: TransformNode) {
    this.uiTexture = AdvancedDynamicTexture.CreateFullscreenUI("Base item placer");
    this.rect1 = new Rectangle();
    this.rect1.width = "350px";
    this.rect1.height = "60px";
    this.rect1.cornerRadius = 20;
    this.rect1.color = "Orange";
    this.rect1.thickness = 4;
    this.rect1.background = "green";
    this.uiTexture.addControl(this.rect1);

    let image = new Rectangle();
    image.width = "40px";
    image.height = "40px";
    image.cornerRadius = 0;
    image.color = "Orange";
    image.thickness = 0;

    this.mouse = new Image();
    this.mouse.source = "babylon-gui/mouse.svg";
    this.mouse.width = "40px";
    this.mouse.height = "40px";
    this.mouse.stretch = Image.STRETCH_UNIFORM;
    this.mouse.horizontalAlignment = Control.HORIZONTAL_ALIGNMENT_LEFT
    image.addControl(this.mouse);

    this.mouseLeftButton = new Image();
    this.mouseLeftButton.source = "babylon-gui/mouse-left-button.svg";
    this.mouseLeftButton.width = "40px";
    this.mouseLeftButton.height = "40px";
    this.mouseLeftButton.stretch = Image.STRETCH_UNIFORM;
    this.mouseLeftButton.horizontalAlignment = Control.HORIZONTAL_ALIGNMENT_LEFT
    image.addControl(this.mouseLeftButton);

    let stackPanel = new StackPanel();
    stackPanel.isVertical = false;
    stackPanel.addControl(image);

    this.label = new TextBlock();
    this.label.color = "white";
    this.label.width = "300px";
    this.label.height = "40px";
    this.label.textHorizontalAlignment = Control.HORIZONTAL_ALIGNMENT_LEFT;
    this.label.paddingLeft = "10px";
    stackPanel.addControl(this.label);

    this.rect1.addControl(stackPanel);

    this.setPositionValid(positionValid);

    this.rect1.linkWithMesh(transformNode);
    this.rect1.linkOffsetY = -100;
  }

  setPositionValid(positionValid: boolean) {
    if (positionValid) {
      this.rect1.background = "green";
      this.label.text = Tip.POSITION_VALID_TEXT;
      this.setupMouseButtonAnimation();
      this.mouse.animations = [];
      this.mouse.left = 0;
      this.rendererService.getScene().stopAnimation(this.mouse);
    } else {
      this.rect1.background = "red";
      this.label.text = Tip.POSITION_IN_VALID_TEXT;
      this.setupMouseMoveAnimation();
      this.mouseLeftButton.animations = [];
      this.mouseLeftButton.alpha = 0;
      this.rendererService.getScene().stopAnimation(this.mouseLeftButton);
    }
  }

  dispose() {
    this.uiTexture.dispose();
  }

  private setupMouseButtonAnimation() {
    let blinkAnimation = new Animation(
      "blink",
      "alpha",
      30,
      Animation.ANIMATIONTYPE_FLOAT,
      Animation.ANIMATIONLOOPMODE_CYCLE
    );

    let keys = [];
    keys.push({frame: 0, value: 1});
    keys.push({frame: 15, value: 0});
    keys.push({frame: 30, value: 1});
    blinkAnimation.setKeys(keys);
    this.mouseLeftButton.animations = [blinkAnimation];

    this.rendererService.getScene().beginAnimation(this.mouseLeftButton, 0, 30, true);
  }


  private setupMouseMoveAnimation() {
    let moveAnimation = new Animation(
      "move",
      "left",
      30,
      Animation.ANIMATIONTYPE_FLOAT,
      Animation.ANIMATIONLOOPMODE_CYCLE
    );

    let keys = [];
    keys.push({frame: 0, value: 5});
    keys.push({frame: 15, value: -5});
    keys.push({frame: 30, value: 5});
    moveAnimation.setKeys(keys);
    this.mouse.animations = [moveAnimation];

    this.rendererService.getScene().beginAnimation(this.mouse, 0, 30, true);
  }
}
