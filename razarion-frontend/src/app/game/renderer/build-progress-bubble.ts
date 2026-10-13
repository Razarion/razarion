import {AdvancedDynamicTexture} from "@babylonjs/gui/2D/advancedDynamicTexture";
import {Rectangle} from "@babylonjs/gui/2D/controls/rectangle";
import {TextBlock} from "@babylonjs/gui/2D/controls/textBlock";
import {TransformNode} from "@babylonjs/core/Meshes/transformNode";
import {Scene} from "@babylonjs/core/scene";
import {PROMPT_IDEAL_HEIGHT_PX} from './prompt-geometry';

/**
 * A small label over the builder on its way, then over the site going up: "On the way · 12 s",
 * "Building · 4 s". It tells, it does not ask - so it looks like neither the green prompts nor the
 * placer's bubble, has no arrow and no mouse, and takes no taps.
 * <p>
 * One texture for the whole game, built on first use and kept: the text changes every half second
 * and the anchor once or twice per build, and rebuilding a fullscreen texture for either would be
 * the costly part of an otherwise idle moment.
 */
export class BuildProgressBubble {
  private texture: AdvancedDynamicTexture | null = null;
  private container: Rectangle | null = null;
  private label: TextBlock | null = null;
  private anchor: TransformNode | null = null;

  constructor(private readonly scene: Scene) {
  }

  /** Shows the text over the node, or takes the label down for a null node. */
  show(anchor: TransformNode | null, text: string): void {
    if (!anchor || anchor.isDisposed()) {
      this.hide();
      return;
    }
    this.ensureControls();
    const container = this.container!;
    if (this.anchor !== anchor) {
      container.linkWithMesh(anchor);
      this.anchor = anchor;
    }
    if (this.label!.text !== text) {
      this.label!.text = text;
      // Wide enough for the longest text it shows; a bubble that changes its width with every
      // second counted down would wobble.
      container.width = `${Math.max(180, text.length * 11 + 32)}px`;
    }
    container.isVisible = true;
  }

  hide(): void {
    if (this.container) {
      this.container.isVisible = false;
      this.container.linkWithMesh(null);
    }
    this.anchor = null;
  }

  dispose(): void {
    this.texture?.dispose();
    this.texture = null;
    this.container = null;
    this.label = null;
    this.anchor = null;
  }

  private ensureControls(): void {
    if (this.texture) {
      return;
    }
    this.texture = AdvancedDynamicTexture.CreateFullscreenUI("Build progress", true, this.scene);
    // The same scale as the tip prompts, so the label keeps its share of the picture on a phone.
    this.texture.idealHeight = PROMPT_IDEAL_HEIGHT_PX;
    this.texture.disablePicking = true;
    const container = new Rectangle("Build progress bubble");
    container.height = "44px";
    container.cornerRadius = 14;
    container.thickness = 2;
    container.color = "rgba(255, 255, 255, 0.7)";
    container.background = "rgba(20, 30, 40, 0.75)";
    container.isPointerBlocker = false;
    container.linkOffsetY = -110;
    const label = new TextBlock("Build progress text");
    label.color = "white";
    label.fontSize = 20;
    container.addControl(label);
    this.texture.addControl(container);
    this.container = container;
    this.label = label;
  }
}
