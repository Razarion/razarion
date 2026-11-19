import {Button} from '@babylonjs/gui/2D/controls/button';
import {Control} from '@babylonjs/gui/2D/controls/control';
import {Rectangle} from '@babylonjs/gui/2D/controls/rectangle';
import {StackPanel} from '@babylonjs/gui/2D/controls/stackPanel';
import {TextBlock} from '@babylonjs/gui/2D/controls/textBlock';
import {Image} from '@babylonjs/gui/2D/controls/image';
import {Ellipse} from '@babylonjs/gui/2D/controls/ellipse';
import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';
import {Animation} from '@babylonjs/core/Animations/animation';

export class PressMouseVisualization {
  static readonly POSITION_VALID_TEXT = "Click left mouse button to deploy";
  static readonly POSITION_IN_VALID_TEXT = "Move mouse to find free position";
  /** A finger has no button to press and no cursor to follow, so the hint names the two gestures. */
  static readonly TOUCH_POSITION_VALID_TEXT = "Drag or tap to move it";
  static readonly TOUCH_POSITION_IN_VALID_TEXT = "Drag it to a free spot";
  /** Width of the touch panel, in the GUI's pixels - the placer keeps it on screen with this. */
  static readonly TOUCH_WIDTH = 300;
  /** The placer panel: the HUD's dark, an amber accent for what is pressed, a red frame for a bad spot. */
  private static readonly PANEL_BACKGROUND = "rgba(14, 24, 30, 0.92)";
  private static readonly PANEL_TEXT = "#e8f0f2";
  private static readonly ACCENT = "#f5a623";
  private static readonly ACCENT_TEXT = "#1d1300";
  private static readonly INVALID = "#e5534b";
  private static readonly INVALID_TEXT = "#ffab91";
  private static readonly BUTTON_OFF = "#3a4449";
  private static readonly QUIET_TEXT = "#8d989e";
  /** The gesture hands are shown until this many buildings were built through the button. */
  private static readonly HANDS_UNTIL_BUILDS = 2;
  /** Buildings built through the touch button this session - see updateDeployButton. */
  private static buildDeploys = 0;
  public readonly container: Rectangle;
  public readonly label: TextBlock;
  private readonly mouse: Image;
  private readonly mouseLeftButton: Image;
  private readonly mouseContainer: Rectangle;
  private readonly stackPanel: StackPanel;
  private deployButton: Button | null = null;
  private deployCallback: (() => void) | null = null;
  private cancelButton: Button | null = null;
  private cancelCallback: (() => void) | null = null;
  private handBox: Rectangle | null = null;
  private tapHand: Image | null = null;
  private tapRing: Ellipse | null = null;
  private dragHint: Rectangle | null = null;
  private dragHand: Image | null = null;
  private userDragging = false;
  private placerStyle = false;
  private deployText = "DEPLOY";
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
    if (this.placerStyle) {
      // The verdict is in the frame and the text, not the panel: a panel flooding red and green
      // under white text was half the reason the bubble looked like an error box.
      this.container.background = PressMouseVisualization.PANEL_BACKGROUND;
      this.container.color = positionValid ? PressMouseVisualization.ACCENT : PressMouseVisualization.INVALID;
      this.label.color = positionValid ? PressMouseVisualization.PANEL_TEXT : PressMouseVisualization.INVALID_TEXT;
    }
    if (this.touchMode) {
      if (!this.placerStyle) {
        this.container.background = positionValid ? "green" : "red";
      }
      this.label.text = positionValid
        ? PressMouseVisualization.TOUCH_POSITION_VALID_TEXT
        : (errorText ? errorText : PressMouseVisualization.TOUCH_POSITION_IN_VALID_TEXT);
      this.updateDeployButton();
      return;
    }
    if (positionValid) {
      if (!this.placerStyle) {
        this.container.background = "green";
      }
      this.label.text = PressMouseVisualization.POSITION_VALID_TEXT;
      this.setupMouseButtonAnimation();
      this.mouse.animations = [];
      this.mouse.left = 0;
      this.rendererService.getScene().stopAnimation(this.mouse);
    } else {
      if (!this.placerStyle) {
        this.container.background = "red";
      }
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
  setTouchMode(deployCallback: () => void, cancelCallback: (() => void) | null = null) {
    this.deployCallback = deployCallback;
    this.cancelCallback = cancelCallback;
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
    this.container.width = `${PressMouseVisualization.TOUCH_WIDTH}px`;
    this.container.height = "132px";
    // Narrower than the panel: the ✕ sits in its top right corner.
    this.label.width = "220px";
    this.label.height = "48px";
    this.label.fontSize = 17;
    // Centred over the button rather than ranged left, which only made sense beside the mouse icon.
    this.label.textHorizontalAlignment = Control.HORIZONTAL_ALIGNMENT_CENTER;
    this.label.paddingLeft = "0px";
    this.stackPanel.spacing = 6;

    /*
     * 2026-11-19, quest 358 on a phone: the touch players who opened the factory placer and never
     * built had pressed ✕ four times as often as those who built (45 % against 11 %), and almost
     * none had pressed DEPLOY at all. DEPLOY was a dull green on the green bubble and read as
     * switched off; ✕ beside it was the one bright thing there. Now the button is the bright thing,
     * a hand taps it, and ✕ is a quiet corner mark away from it.
     */
    const deployButton = Button.CreateSimpleButton("Base Item Placer Deploy", this.deployText);
    // 48 + 190 + 48 = 286: inside the 300px panel and its 3px frame on both sides.
    deployButton.width = "190px";
    deployButton.height = "56px";
    deployButton.cornerRadius = 12;
    deployButton.thickness = 0;
    deployButton.fontSize = 22;
    deployButton.fontWeight = "bold";
    deployButton.shadowColor = "rgba(0, 0, 0, 0.5)";
    deployButton.shadowBlur = 8;
    deployButton.shadowOffsetY = 3;
    // Kept tappable while the spot is red: the placer answers a press on a red spot with its own
    // explanation, and a dead button would leave the player guessing why nothing happens.
    deployButton.onPointerClickObservable.add(() => {
      if (this.positionValid && this.cancelCallback) {
        PressMouseVisualization.buildDeploys++;
      }
      this.deployCallback?.();
    });
    this.deployButton = deployButton;

    // A hand tapping the button from the right, with a ring where the finger lands: it shows the
    // gesture rather than only the place, and needs no reading. In a box of its own because the row
    // positions its children through `left`, which the tap animates. Not clipped: the fingertip
    // reaches onto the button.
    const handBox = new Rectangle("Base Item Placer Tap Hint");
    handBox.width = "48px";
    handBox.height = "56px";
    handBox.thickness = 0;
    handBox.clipChildren = false;
    handBox.clipContent = false;
    handBox.isPointerBlocker = false;
    handBox.isHitTestVisible = false;
    const tapRing = new Ellipse("Base Item Placer Tap Ring");
    tapRing.width = "36px";
    tapRing.height = "36px";
    tapRing.thickness = 3;
    tapRing.color = "white";
    tapRing.alpha = 0;
    tapRing.isHitTestVisible = false;
    // Where the fingertip is at the bottom of the tap - see startButtonAnimations.
    tapRing.left = "-30px";
    tapRing.top = "2px";
    handBox.addControl(tapRing);
    const tapHand = PressMouseVisualization.handImage("Base Item Placer Tap Hand", "babylon-gui/hand-tap.svg", 50);
    handBox.addControl(tapHand);
    this.handBox = handBox;
    this.tapHand = tapHand;
    this.tapRing = tapRing;

    const row = new StackPanel();
    row.isVertical = false;
    row.height = "56px";
    row.spacing = 0;
    // The hand's box on the right is balanced by an empty one on the left, so the button stays
    // centred under the text whether the hand is shown or not.
    const balance = new Rectangle();
    balance.width = "48px";
    balance.height = "56px";
    balance.thickness = 0;
    balance.isPointerBlocker = false;
    row.addControl(balance);
    row.addControl(deployButton);
    row.addControl(handBox);
    // The fingertip leaves the hand's box for the button.
    row.clipChildren = false;
    row.clipContent = false;
    this.stackPanel.clipChildren = false;
    this.stackPanel.clipContent = false;
    // Up to the panel too: the hand reaches past its frame.
    this.container.clipChildren = false;
    this.container.clipContent = false;
    this.stackPanel.addControl(row);

    if (this.cancelCallback) {
      // A building placer on a phone: without a key to press, this is the only way to close it
      // without building (phone test, 2026-10-05). In the corner, away from the button it was
      // being taken for.
      const cancelButton = Button.CreateSimpleButton("Base Item Placer Cancel", "✕");
      cancelButton.width = "40px";
      cancelButton.height = "40px";
      cancelButton.thickness = 0;
      cancelButton.color = PressMouseVisualization.QUIET_TEXT;
      cancelButton.fontSize = 20;
      cancelButton.horizontalAlignment = Control.HORIZONTAL_ALIGNMENT_RIGHT;
      cancelButton.verticalAlignment = Control.VERTICAL_ALIGNMENT_TOP;
      cancelButton.top = "2px";
      cancelButton.left = "-2px";
      cancelButton.onPointerClickObservable.add(() => this.cancelCallback?.());
      this.cancelButton = cancelButton;
      this.container.addControl(cancelButton);
    }

    this.startButtonAnimations();
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
    if (this.deployButton?.contains(x, y) || this.cancelButton?.contains(x, y)) {
      return false;
    }
    return this.container.contains(x, y);
  }

  private updateDeployButton() {
    if (!this.deployButton) {
      return;
    }
    this.deployButton.background = this.positionValid ? PressMouseVisualization.ACCENT : PressMouseVisualization.BUTTON_OFF;
    this.deployButton.color = this.positionValid ? PressMouseVisualization.ACCENT_TEXT : PressMouseVisualization.QUIET_TEXT;
    // Only for the first buildings of a session: a player who has built twice knows the gestures,
    // and a hand on every placer after that is noise. Then the hand that fits the spot: on a free
    // one the next thing to do is tap the button, on a taken one it is to move the building.
    const early = PressMouseVisualization.buildDeploys < PressMouseVisualization.HANDS_UNTIL_BUILDS;
    if (this.handBox) {
      this.handBox.alpha = this.positionValid && early ? 1 : 0;
    }
    if (this.dragHint) {
      // Not while a finger is on the building: it is doing what the hand shows, and the hand would
      // sit right under it.
      this.dragHint.isVisible = !this.positionValid && early && !this.userDragging;
    }
  }

  /**
   * The placer's own look: a dark panel like the HUD around it, with the colour kept for the one
   * thing to press. The quest tips share this class and keep their green bubble.
   *
   * @param deployText what the button says - the start base is deployed, a building is built.
   */
  usePlacerStyle(deployText: string): void {
    this.placerStyle = true;
    this.deployText = deployText;
    this.container.thickness = 3;
    this.container.cornerRadius = 16;
    this.container.shadowColor = "rgba(0, 0, 0, 0.55)";
    this.container.shadowBlur = 16;
    this.container.shadowOffsetY = 4;
    this.label.fontWeight = "bold";
    // The one thing to press, in the colour of the touch button.
    this.mouseLeftButton.source = "babylon-gui/mouse-left-button-accent.svg";
    this.setPositionValid(this.positionValid);
  }

  /** The button breathes and the hand taps it - both only in what they move, never in colour. */
  private startButtonAnimations(): void {
    const scene = this.rendererService.getScene();
    if (this.deployButton) {
      const pulse = (property: string) => {
        const animation = new Animation("deploy pulse " + property, property, 30, Animation.ANIMATIONTYPE_FLOAT,
          Animation.ANIMATIONLOOPMODE_CYCLE);
        animation.setKeys([{frame: 0, value: 1}, {frame: 18, value: 1.06}, {frame: 36, value: 1}]);
        return animation;
      };
      this.deployButton.animations = [pulse("scaleX"), pulse("scaleY")];
      scene.beginAnimation(this.deployButton, 0, 36, true);
    }
    if (this.tapHand && this.tapRing) {
      // Hover, come down onto the button, hold, lift, rest - 1.6 s a tap. Pressed is a little
      // smaller, as a finger looks from above when it pushes.
      this.tapHand.animations = [
        PressMouseVisualization.keyed("tap left", "left", [[0, -16], [10, -26], [16, -26], [26, -16], [48, -16]]),
        PressMouseVisualization.keyed("tap top", "top", [[0, 14], [10, 6], [16, 6], [26, 14], [48, 14]]),
        PressMouseVisualization.keyed("tap scaleX", "scaleX", [[0, 1], [10, 0.9], [16, 0.9], [26, 1], [48, 1]]),
        PressMouseVisualization.keyed("tap scaleY", "scaleY", [[0, 1], [10, 0.9], [16, 0.9], [26, 1], [48, 1]]),
      ];
      scene.beginAnimation(this.tapHand, 0, 48, true);
      this.tapRing.animations = [
        PressMouseVisualization.keyed("ring alpha", "alpha", [[0, 0], [10, 0], [11, 0.9], [30, 0], [48, 0]]),
        PressMouseVisualization.keyed("ring scaleX", "scaleX", [[0, 0.3], [10, 0.3], [30, 1.4], [48, 1.4]]),
        PressMouseVisualization.keyed("ring scaleY", "scaleY", [[0, 0.3], [10, 0.3], [30, 1.4], [48, 1.4]]),
      ];
      scene.beginAnimation(this.tapRing, 0, 48, true);
    }
  }

  /**
   * A hand that presses on the building and carries it a little way, for a spot where it cannot be
   * built. Linked to the building by the placer, not part of the panel: the gesture is made on the
   * building, and a hand in the panel would show dragging the panel.
   * <p>
   * 2026-11-19, quest 358: of the touch players who opened the factory placer and never built,
   * 36 % had touched the world while it was open - panned, pinched, tapped a taken spot - against
   * 7 % of those who built. Nothing told them the building itself can be picked up.
   */
  createDragHint(): Rectangle {
    const hint = new Rectangle("Base Item Placer Drag Hint");
    hint.width = "200px";
    hint.height = "100px";
    hint.thickness = 0;
    hint.clipChildren = false;
    hint.clipContent = false;
    hint.isPointerBlocker = false;
    hint.isHitTestVisible = false;
    // Bigger than the tapping hand and with a shadow: it is drawn on a building, not on a plain panel.
    const hand = PressMouseVisualization.handImage("Base Item Placer Drag Hand", "babylon-gui/hand-drag.svg", 60);
    hand.shadowColor = "rgba(0, 0, 0, 0.6)";
    hand.shadowBlur = 10;
    hand.shadowOffsetY = 4;
    hint.addControl(hand);
    // Appear pressed onto the building, carry it to the right, let go and fade, rest - 1.8 s.
    // top 24 puts the fingertip, at the top of the picture, on the point the hint is linked to.
    hand.animations = [
      PressMouseVisualization.keyed("drag left", "left", [[0, -30], [8, -30], [34, 45], [42, 45], [54, -30]]),
      PressMouseVisualization.keyed("drag top", "top", [[0, 24], [54, 24]]),
      PressMouseVisualization.keyed("drag alpha", "alpha", [[0, 0], [6, 1], [36, 1], [44, 0], [54, 0]]),
      PressMouseVisualization.keyed("drag scaleX", "scaleX", [[0, 1.1], [8, 0.9], [34, 0.9], [40, 1.05], [54, 1.1]]),
      PressMouseVisualization.keyed("drag scaleY", "scaleY", [[0, 1.1], [8, 0.9], [34, 0.9], [40, 1.05], [54, 1.1]]),
    ];
    this.rendererService.getScene().beginAnimation(hand, 0, 54, true);
    this.dragHint = hint;
    this.dragHand = hand;
    this.updateDeployButton();
    return hint;
  }

  /** A finger is carrying the building - see updateDeployButton. */
  setUserDragging(dragging: boolean): void {
    if (this.userDragging === dragging) {
      return;
    }
    this.userDragging = dragging;
    this.updateDeployButton();
  }

  private static handImage(name: string, source: string, size = 44): Image {
    const hand = new Image(name, source);
    hand.width = `${size}px`;
    hand.height = `${size}px`;
    hand.stretch = Image.STRETCH_UNIFORM;
    hand.isHitTestVisible = false;
    return hand;
  }

  private static keyed(name: string, property: string, keys: [number, number][]): Animation {
    const animation = new Animation(name, property, 30, Animation.ANIMATIONTYPE_FLOAT, Animation.ANIMATIONLOOPMODE_CYCLE);
    animation.setKeys(keys.map(([frame, value]) => ({frame, value})));
    return animation;
  }

  /** Stops what runs from the render loop; the texture disposing the controls does not. */
  dispose(): void {
    const scene = this.rendererService.getScene();
    for (const control of [this.deployButton, this.tapHand, this.tapRing, this.dragHand, this.mouse, this.mouseLeftButton]) {
      if (control) {
        scene.stopAnimation(control);
        control.animations = [];
      }
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
