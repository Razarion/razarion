import {Control, Rectangle, StackPanel, TextBlock} from '@babylonjs/gui';
import {Image} from '@babylonjs/gui/2D/controls/image';
import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';
import {Animation} from '@babylonjs/core/Animations/animation';

export class PressMouseVisualization {
  static readonly POSITION_VALID_TEXT = "Click left mouse button to deploy";
  static readonly POSITION_IN_VALID_TEXT = "Move mouse to find free position";
  public readonly container: Rectangle;
  public readonly label: TextBlock;
  private readonly mouse: Image;
  private readonly mouseLeftButton: Image;

  constructor(positionValid: boolean,
              protected readonly rendererService: BabylonRenderServiceAccessImpl) {
    this.container = new Rectangle();
    this.container.width = "350px";
    this.container.height = "60px";
    this.container.cornerRadius = 20;
    this.container.color = "orange";
    this.container.thickness = 4;
    this.container.background = "green";

    let mouseContainer = new Rectangle();
    mouseContainer.width = "40px";
    mouseContainer.height = "40px";
    mouseContainer.cornerRadius = 0;
    mouseContainer.color = "Orange";
    mouseContainer.thickness = 0;

    this.mouse = new Image();
    this.mouse.source = "babylon-gui/mouse.svg";
    this.mouse.width = "40px";
    this.mouse.height = "40px";
    this.mouse.stretch = Image.STRETCH_UNIFORM;
    this.mouse.horizontalAlignment = Control.HORIZONTAL_ALIGNMENT_LEFT
    mouseContainer.addControl(this.mouse);

    this.mouseLeftButton = new Image();
    this.mouseLeftButton.source = "babylon-gui/mouse-left-button.svg";
    this.mouseLeftButton.width = "40px";
    this.mouseLeftButton.height = "40px";
    this.mouseLeftButton.stretch = Image.STRETCH_UNIFORM;
    this.mouseLeftButton.horizontalAlignment = Control.HORIZONTAL_ALIGNMENT_LEFT
    mouseContainer.addControl(this.mouseLeftButton);

    let stackPanel = new StackPanel();
    stackPanel.isVertical = false;
    stackPanel.addControl(mouseContainer);

    this.label = new TextBlock();
    this.label.color = "white";
    this.label.width = "300px";
    this.label.height = "40px";
    this.label.textHorizontalAlignment = Control.HORIZONTAL_ALIGNMENT_LEFT;
    this.label.paddingLeft = "10px";
    stackPanel.addControl(this.label);

    this.container.addControl(stackPanel);

    this.setPositionValid(positionValid);
  }

  setPositionValid(positionValid: boolean) {
    if (positionValid) {
      this.container.background = "green";
      this.label.text = PressMouseVisualization.POSITION_VALID_TEXT;
      this.setupMouseButtonAnimation();
      this.mouse.animations = [];
      this.mouse.left = 0;
      this.rendererService.getScene().stopAnimation(this.mouse);
    } else {
      this.container.background = "red";
      this.label.text = PressMouseVisualization.POSITION_IN_VALID_TEXT;
      this.setupMouseMoveAnimation();
      this.mouseLeftButton.animations = [];
      this.mouseLeftButton.alpha = 0;
      this.rendererService.getScene().stopAnimation(this.mouseLeftButton);
    }
  }

  getContainer() {
    return this.container;
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
