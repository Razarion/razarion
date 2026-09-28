import {Button} from '@babylonjs/gui/2D/controls/button';
import {Control} from '@babylonjs/gui/2D/controls/control';
import {Rectangle} from '@babylonjs/gui/2D/controls/rectangle';
import {StackPanel} from '@babylonjs/gui/2D/controls/stackPanel';
import {TextBlock} from '@babylonjs/gui/2D/controls/textBlock';
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
  /** Whether fitToText was told this is a touch device - see refitWidth. */
  private touchFit = false;
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
   * Gives the bubble the width its text actually needs, in pixels.
   *
   * The quest tips used to hand a width in per prompt, with a table of exceptions for the longer
   * texts, and it still went wrong: on a phone the label is a large share of the width, so
   * "Click to attack" wrapped onto a second line the 40 px label had no room for and the player
   * read "Click to" with the rest cut off.
   *
   * Measured rather than left to `adaptWidthToChildren`, which Babylon implements by setting the
   * container's width to "100%" - and a child with a width in percent is skipped when the
   * StackPanel around it adds up its children, so the panel keeps the full size of the picture in
   * that axis. Its contents are then centred in whatever of it is not clipped away at the edge
   * rather than on the item, which is how the arrow ended up above the unit it was pointing at.
   *
   * @param touch hides the mouse icon. A finger has no button to press, and the icon is 60 px of
   *              a picture that has none to spare.
   * @return the width of the bubble, in the GUI's pixels.
   */
  fitToText(touch: boolean): number {
    this.touchFit = touch;
    const text = this.label.text;
    const textWidth = Math.ceil(PressMouseVisualization.measureText(text));
    this.label.textWrapping = false;
    // Babylon measures the text itself once it lays out, in whatever font the texture ends up
    // using. The width below is only a starting guess so the first frame is not wildly wrong -
    // refitWidth() replaces it with the real thing. Measuring here with "18px Arial" was close
    // but not equal: on Android the family falls back and the text came out a fifth wider, which
    // is one word cut off the end.
    this.label.resizeToFit = true;
    this.label.paddingRight = "10px";
    this.label.width = `${textWidth + 2 * PressMouseVisualization.LABEL_PADDING + PressMouseVisualization.TEXT_SLACK}px`;
    this.label.height = "40px";
    if (touch) {
      this.mouseContainer.isVisible = false;
      this.mouseContainer.width = "0px";
      this.mouseContainer.height = "0px";
      this.rendererService.getScene().stopAnimation(this.mouse);
      this.rendererService.getScene().stopAnimation(this.mouseLeftButton);
      this.mouse.animations = [];
      this.mouseLeftButton.animations = [];
    }
    // Plus the bubble's own border on both sides: a Rectangle measures its children inside its
    // thickness, so a label exactly as wide as the bubble loses its last letters - which is what
    // cut "Tap to select" to "Tap to sel" on the phone.
    const width = textWidth + 2 * PressMouseVisualization.LABEL_PADDING + PressMouseVisualization.TEXT_SLACK
      + 2 * PressMouseVisualization.BUBBLE_BORDER + (touch ? 0 : PressMouseVisualization.MOUSE_ICON_WIDTH);
    this.container.width = `${width}px`;
    return width;
  }

  /**
   * Corrects the bubble to the width Babylon measured for the text, once it has laid it out.
   * Returns the new width when something changed, null when it is already right.
   */
  refitWidth(): number | null {
    const measured = (this.label as any)._lines?.[0]?.width;
    if (typeof measured !== 'number' || measured <= 0) {
      return null;
    }
    // _lines is in render pixels; everything set here is in the GUI's own, which the ideal size
    // scales down by the same factor.
    const host = this.label.host;
    const scale = host && host.idealHeight ? host.getSize().height / host.idealHeight : 1;
    const wanted = Math.ceil(measured / scale) + 2 * PressMouseVisualization.LABEL_PADDING
      + PressMouseVisualization.TEXT_SLACK;
    if (Math.abs(wanted - this.label.widthInPixels / scale) < 2) {
      return null;
    }
    this.label.width = `${wanted}px`;
    const width = wanted + 2 * PressMouseVisualization.BUBBLE_BORDER
      + (this.touchFit ? 0 : PressMouseVisualization.MOUSE_ICON_WIDTH);
    this.container.width = `${width}px`;
    return width;
  }

  /** Width of the mouse icon beside the text, which a finger has no use for. */
  static readonly MOUSE_ICON_WIDTH = 60;
  /** paddingLeft/Right on the label. */
  private static readonly LABEL_PADDING = 10;
  /** The orange frame of the bubble - see fitToText. */
  private static readonly BUBBLE_BORDER = 4;
  /** Room for the last glyph: measureText and the GUI's own layout round differently. */
  private static readonly TEXT_SLACK = 8;
  private static measureCanvas: CanvasRenderingContext2D | null = null;

  /**
   * How wide the text comes out in the GUI's own pixels - the label carries no font of its own, so
   * it renders in the fullscreen texture's default, and the ideal-size scaling then applies to the
   * result exactly as it does to every other pixel value here.
   */
  private static measureText(text: string): number {
    if (!PressMouseVisualization.measureCanvas) {
      PressMouseVisualization.measureCanvas = document.createElement('canvas').getContext('2d');
    }
    const context = PressMouseVisualization.measureCanvas;
    if (!context) {
      return text.length * 10; // No 2d context in this environment; the estimate is close enough.
    }
    context.font = '18px Arial';
    return context.measureText(text).width;
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
