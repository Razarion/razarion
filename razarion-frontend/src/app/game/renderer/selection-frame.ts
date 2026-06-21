import {PointerEventTypes, Scene, Vector2} from "@babylonjs/core";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {ActionService} from "../action.service";
import {Observer} from '@babylonjs/core/Misc/observable';
import type {PointerInfo} from '@babylonjs/core/Events/pointerEvents';

export class SelectionFrame {
  // Minimum drag in screen pixels before it counts as a marquee (smaller = treated as a click).
  private static readonly MIN_PIXEL_DISTANCE = 5;
  private mousePos0: Vector2 | undefined;
  private observer: Observer<PointerInfo> | null = null;
  private overlay: HTMLDivElement | null = null;

  constructor(private scene: Scene,
              private renderService: BabylonRenderServiceAccessImpl,
              private actionService: ActionService) {
    this.observer = this.scene.onPointerObservable.add((pointerInfo) => {
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

  private onPointerDown(x: number, y: number) {
    this.mousePos0 = new Vector2(x, y);
  }

  private onPointerMove(x: number, y: number) {
    if (!this.mousePos0) {
      return;
    }
    this.updateOverlay(x, y);
  }

  private onPointerUp() {
    this.hideOverlay();
    const start = this.mousePos0;
    this.mousePos0 = undefined;

    if (!start) {
      return;
    }
    const endX = this.scene.pointerX;
    const endY = this.scene.pointerY;

    // Below the threshold it's a click, not a marquee — let the per-item pick handlers deal with it.
    if (Math.abs(start.x - endX) < SelectionFrame.MIN_PIXEL_DISTANCE &&
      Math.abs(start.y - endY) < SelectionFrame.MIN_PIXEL_DISTANCE) {
      return;
    }

    // Screen-pixel rectangle — same space as the green overlay the user drew.
    this.actionService.selectScreenRectangle(
      Math.min(start.x, endX),
      Math.min(start.y, endY),
      Math.max(start.x, endX),
      Math.max(start.y, endY),
    );
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
