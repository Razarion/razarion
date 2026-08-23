import {PointerEventTypes, Scene, Vector2} from "@babylonjs/core";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {ActionService} from "../action.service";
import {TouchSelectionModeService} from "./touch-selection-mode.service";
import {Observer} from '@babylonjs/core/Misc/observable';
import type {PointerInfo} from '@babylonjs/core/Events/pointerEvents';

export class SelectionFrame {
  // Minimum drag in screen pixels before it counts as a marquee (smaller = treated as a click).
  private static readonly MIN_PIXEL_DISTANCE = 5;
  private mousePos0: Vector2 | undefined;
  private observer: Observer<PointerInfo> | null = null;
  private overlay: HTMLDivElement | null = null;
  /** The finger that is drawing the box, if one is. */
  private marqueePointerId: number | null = null;

  constructor(private scene: Scene,
              private renderService: BabylonRenderServiceAccessImpl,
              private actionService: ActionService,
              private touchSelectionMode: TouchSelectionModeService) {
    this.observer = this.scene.onPointerObservable.add((pointerInfo) => {
      // A marquee is a mouse gesture. On a touch screen the same drag moves the camera, and there
      // is no second button to tell the two apart - so by default the finger pans and a tap selects
      // the one item under it. Drawing a box as well would select whatever the pan swept across.
      // The box therefore has a mode of its own; while it is armed the camera lets this finger
      // through (see TouchCameraControl) and the drag draws instead of panning.
      if (SelectionFrame.isTouch(pointerInfo)) {
        this.onTouch(pointerInfo);
        return;
      }
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERDOWN: {
          if (renderService.baseItemPlacerActive) {
            return;
          }
          this.onPointerDown(this.scene.pointerX, this.scene.pointerY);
          break;
        }
        case PointerEventTypes.POINTERUP: {
          if (renderService.baseItemPlacerActive) {
            return;
          }
          this.onPointerUp();
          break;
        }
        case PointerEventTypes.POINTERMOVE: {
          this.onPointerMove(this.scene.pointerX, this.scene.pointerY);
          break;
        }
      }
    });
  }

  /**
   * Whether this pointer is a finger. Babylon forwards the original browser event, so the pointer
   * type survives; a pen counts as a mouse here because it can hover and drag precisely.
   */
  private static isTouch(pointerInfo: PointerInfo): boolean {
    return (pointerInfo.event as PointerEvent)?.pointerType === 'touch';
  }

  /**
   * The box on a touch screen: one armed finger draws it, and releasing it both selects and spends
   * the mode.
   * <p>
   * A second finger cancels the box rather than fighting it - two fingers are a pinch, and the
   * player who put them down is zooming, not selecting. The mode survives that, so the box is still
   * one drag away afterwards.
   */
  private onTouch(pointerInfo: PointerInfo): void {
    const event = pointerInfo.event as PointerEvent;
    // The placer owns the screen while it is up; the mode is disarmed when it opens, and this keeps
    // a box from being drawn across it should that ever be armed again underneath.
    const armed = this.touchSelectionMode.armed() && !this.renderService.baseItemPlacerActive;
    if (!armed) {
      this.cancelMarquee();
      return;
    }
    switch (pointerInfo.type) {
      case PointerEventTypes.POINTERDOWN: {
        // isPrimary rather than a count of the fingers we have seen: a touch that is cancelled
        // instead of released does not always arrive here, and a finger this class still believes
        // is down would block every box after it. The browser knows better - a pointer is primary
        // exactly when no other one is active, so a second finger says so about itself.
        if (!event.isPrimary) {
          this.cancelMarquee();
          return;
        }
        this.marqueePointerId = event.pointerId;
        this.onPointerDown(this.scene.pointerX, this.scene.pointerY);
        break;
      }
      case PointerEventTypes.POINTERMOVE: {
        if (this.marqueePointerId !== event.pointerId) {
          return;
        }
        this.onPointerMove(this.scene.pointerX, this.scene.pointerY);
        break;
      }
      case PointerEventTypes.POINTERUP: {
        if (this.marqueePointerId !== event.pointerId) {
          return;
        }
        this.marqueePointerId = null;
        // Only a box that was actually drawn spends the mode. A finger that went down and came
        // straight back up selected nothing, and disarming on it would cost the player the mode
        // for a touch that did nothing at all.
        if (this.onPointerUp()) {
          this.touchSelectionMode.disarm();
        }
        break;
      }
    }
  }

  /** Drops the box being drawn. The mode itself survives - only a finished box spends it. */
  private cancelMarquee(): void {
    this.marqueePointerId = null;
    this.mousePos0 = undefined;
    this.hideOverlay();
  }

  private onPointerDown(x: number, y: number) {
    this.mousePos0 = new Vector2(x, y);
  }

  private onPointerMove(x: number, y: number) {
    if (!this.mousePos0) {
      return;
    }
    this.updateOverlay(x, y);
  }

  /** Whether a marquee was drawn and its selection sent - false when the press was just a click. */
  private onPointerUp(): boolean {
    this.hideOverlay();
    const start = this.mousePos0;
    this.mousePos0 = undefined;

    if (!start) {
      return false;
    }
    const endX = this.scene.pointerX;
    const endY = this.scene.pointerY;

    // Below the threshold it's a click, not a marquee — let the per-item pick handlers deal with it.
    if (Math.abs(start.x - endX) < SelectionFrame.MIN_PIXEL_DISTANCE &&
      Math.abs(start.y - endY) < SelectionFrame.MIN_PIXEL_DISTANCE) {
      return false;
    }

    // Screen-pixel rectangle — same space as the green overlay the user drew.
    this.actionService.selectScreenRectangle(
      Math.min(start.x, endX),
      Math.min(start.y, endY),
      Math.max(start.x, endX),
      Math.max(start.y, endY),
    );
    return true;
  }

  private updateOverlay(x: number, y: number) {
    if (!this.mousePos0) {
      return;
    }
    const canvas = this.scene.getEngine().getRenderingCanvas();
    if (!canvas) {
      return;
    }
    const overlay = this.ensureOverlay();
    const rect = canvas.getBoundingClientRect();
    const left = rect.left + Math.min(this.mousePos0.x, x);
    const top = rect.top + Math.min(this.mousePos0.y, y);
    const width = Math.abs(x - this.mousePos0.x);
    const height = Math.abs(y - this.mousePos0.y);
    overlay.style.display = "block";
    overlay.style.left = `${left}px`;
    overlay.style.top = `${top}px`;
    overlay.style.width = `${width}px`;
    overlay.style.height = `${height}px`;
  }

  private hideOverlay() {
    if (this.overlay) {
      this.overlay.style.display = "none";
    }
  }

  private ensureOverlay(): HTMLDivElement {
    if (this.overlay) {
      return this.overlay;
    }
    const div = document.createElement("div");
    div.className = "razarion-selection-frame";
    div.style.position = "fixed";
    div.style.pointerEvents = "none";
    // box-sizing so the border doesn't push the rectangle outward as the user drags.
    div.style.boxSizing = "border-box";
    div.style.border = "2px solid rgb(0, 255, 0)";
    div.style.backgroundColor = "rgba(0, 255, 0, 0.1)";
    div.style.boxShadow = "0 0 6px rgba(0, 255, 0, 0.4)";
    div.style.display = "none";
    div.style.zIndex = "100";
    document.body.appendChild(div);
    this.overlay = div;
    return this.overlay;
  }

  disable() {
    this.cancelMarquee();
    if (this.observer) {
      this.observer.remove();
      this.observer = null;
    }
    if (this.overlay) {
      this.overlay.remove();
      this.overlay = null;
    }
  }
}
