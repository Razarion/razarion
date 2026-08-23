import {Button, Control, Rectangle, StackPanel, TextBlock} from '@babylonjs/gui';
import {Image} from '@babylonjs/gui/2D/controls/image';
import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';
import {Animation} from '@babylonjs/core/Animations/animation';

export class PressMouseVisualization {
  static readonly POSITION_VALID_TEXT = "Click left mouse button to deploy";
  static readonly POSITION_IN_VALID_TEXT = "Move mouse to find free position";
  /** A finger has no button to press and no cursor to follow, so the hint names the two gestures. */
  static readonly TOUCH_POSITION_VALID_TEXT = "Drag the building or tap a spot";
  static readonly TOUCH_POSITION_IN_VALID_TEXT = "Drag the building to a free spot";
  public readonly container: Rectangle;
  public readonly label: TextBlock;
  private readonly mouse: Image;
  private readonly mouseLeftButton: Image;
  private readonly mouseContainer: Rectangle;
  private readonly stackPanel: StackPanel;
  private deployButton: Button | null = null;
  private deployCallback: (() => void) | null = null;
  private touchMode = false;
  private positionValid = true;

  constructor(positionValid: boolean,
              protected readonly rendererService: BabylonRenderServiceAccessImpl) {
    this.container = new Rectangle();
    this.container.width = "350px";
    this.container.height = "60px";
    this.container.cornerRadius = 20;
    this.container.color = "orange";
    this.container.thickness = 4;
    this.container.background = "green";

    const mouseContainer = new Rectangle();
    this.mouseContainer = mouseContainer;
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

    const stackPanel = new StackPanel();
    this.stackPanel = stackPanel;
    stackPanel.isVertical = false;
    stackPanel.addControl(mouseContainer);

    this.label = new TextBlock();
    this.label.color = "white";
    this.label.width = "300px";
    this.label.height = "40px";
    this.label.textHorizontalAlignment = Control.HORIZONTAL_ALIGNMENT_LEFT;
    this.label.paddingLeft = "10px";
    // The concrete placer reasons are longer than the two generic hints and would be cut off.
    this.label.textWrapping = true;
    stackPanel.addControl(this.label);

    this.container.addControl(stackPanel);

    this.setPositionValid(positionValid);
  }

  /**
   * @param errorText concrete reason from the placer, shown instead of the generic hint when the
   *                  position is invalid. Falls back to the generic text when empty.
   */
  setPositionValid(positionValid: boolean, errorText?: string) {
    this.positionValid = positionValid;
    if (this.touchMode) {
      this.container.background = positionValid ? "green" : "red";
      this.label.text = positionValid
        ? PressMouseVisualization.TOUCH_POSITION_VALID_TEXT
        : (errorText ? errorText : PressMouseVisualization.TOUCH_POSITION_IN_VALID_TEXT);
      this.updateDeployButton();
      return;
    }
    if (positionValid) {
      this.container.background = "green";
      this.label.text = PressMouseVisualization.POSITION_VALID_TEXT;
      this.setupMouseButtonAnimation();
      this.mouse.animations = [];
      this.mouse.left = 0;
      this.rendererService.getScene().stopAnimation(this.mouse);
    } else {
      this.container.background = "red";
      this.label.text = errorText ? errorText : PressMouseVisualization.POSITION_IN_VALID_TEXT;
      this.setupMouseMoveAnimation();
      this.mouseLeftButton.animations = [];
      this.mouseLeftButton.alpha = 0;
      this.rendererService.getScene().stopAnimation(this.mouseLeftButton);
    }
  }

  /**
   * Swaps the mouse hint for a deploy button. A finger has no left button to press, and with the
   * tap moving the building instead of building it there has to be something to press at the end.
   */
  setTouchMode(deployCallback: () => void) {
    this.deployCallback = deployCallback;
    if (this.touchMode) {
      return;
    }
    this.touchMode = true;

    const scene = this.rendererService.getScene();
    scene.stopAnimation(this.mouse);
    scene.stopAnimation(this.mouseLeftButton);
    this.mouse.animations = [];
    this.mouseLeftButton.animations = [];
    this.mouseContainer.isVisible = false;
    this.mouseContainer.width = "0px";
    // Height too, now that the panel stacks downwards: an invisible child is skipped by the layout,
    // but nothing else guarantees it stays that way.
    this.mouseContainer.height = "0px";

    // Stacked rather than side by side: what the bubble says applies to the button under it, and
    // read that way round the eye arrives at the button having just been told what it does. It also
    // takes 60px off the width, which on a phone held upright is a seventh of the screen.
    this.stackPanel.isVertical = true;
    this.container.width = "300px";
    this.container.height = "132px";
    this.label.width = "280px";
    this.label.height = "56px";
    this.label.fontSize = 18;
    // Centred over the button rather than ranged left, which only made sense beside the mouse icon.
    this.label.textHorizontalAlignment = Control.HORIZONTAL_ALIGNMENT_CENTER;
    this.label.paddingLeft = "0px";
    this.stackPanel.spacing = 8;

    const deployButton = Button.CreateSimpleButton("Base Item Placer Deploy", "DEPLOY");
    deployButton.width = "200px";
    deployButton.height = "56px";
    deployButton.cornerRadius = 12;
    deployButton.thickness = 3;
    deployButton.color = "white";
    deployButton.fontSize = 22;
    deployButton.fontWeight = "bold";
    // Kept tappable while the spot is red: the placer answers a press on a red spot with its own
    // explanation, and a dead button would leave the player guessing why nothing happens.
    deployButton.onPointerClickObservable.add(() => this.deployCallback?.());
    this.deployButton = deployButton;
    this.stackPanel.addControl(deployButton);

    this.updateDeployButton();
    // Re-run the current verdict through the touch branch so the hint text matches the new controls.
    this.setPositionValid(this.positionValid);
  }

  isTouchMode(): boolean {
    return this.touchMode;
  }

  /**
   * Whether this point lies on the hint bubble itself, and so counts as a grip on the building it
   * belongs to. Coordinates are the ones the fullscreen GUI measures itself in.
   * <p>
   * The bubble floats a hundred pixels above the building and is wider than a thumb is long, so on
   * a phone it is the part of the placer a finger lands on first. Without this it swallowed the
   * drag and the player was left pressing the instructions that told them to drag.
   * <p>
   * The deploy button is cut out of the grip: it is the one thing in here that is pressed, not
   * dragged.
   */
  containsGrip(x: number, y: number): boolean {
    if (!this.touchMode || !this.container.isVisible) {
      return false;
    }
    if (this.deployButton?.contains(x, y)) {
      return false;
    }
    return this.container.contains(x, y);
  }

  private updateDeployButton() {
    if (!this.deployButton) {
      return;
    }
    this.deployButton.background = this.positionValid ? "#2e7d32" : "#555555";
    this.deployButton.alpha = this.positionValid ? 1 : 0.6;
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
