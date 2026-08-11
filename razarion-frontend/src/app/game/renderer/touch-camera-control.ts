import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";

/**
 * Camera control for touch devices: one finger drags the ground, two fingers pinch to zoom.
 * <p>
 * Until this existed the camera answered only to WASD/arrow keys and the mouse wheel, neither of
 * which a phone has - so a mobile player saw the rectangle they spawned in and nothing else for the
 * whole session. Measured over the first days of August that was 19% base placement against 86% on
 * desktop, and not one mobile session ever passed three quests.
 * <p>
 * Panning is "grab the ground" rather than pixels-per-frame: the world point under the finger stays
 * under the finger. A fixed pixel speed drifts away from the finger as soon as the camera height
 * changes with the terrain, and the camera height here follows the ground on every frame.
 */
export class TouchCameraControl {
  /**
   * A finger wanders a few pixels while it lifts. Below this the interaction is still a tap, and
   * taps belong to selection and commands, not to the camera. Deliberately the same threshold the
   * terrain click handler uses, so a drag that pans is exactly a drag that cancels the click -
   * anything else leaves a band of movement where neither happens.
   */
  private static readonly DRAG_THRESHOLD = 5;
  /** Below this finger gap a pinch ratio explodes; two fingers that close are treated as held. */
  private static readonly MIN_PINCH_DISTANCE = 20;

  private readonly pointers = new Map<number, { startX: number, startY: number, x: number, y: number }>();
  private panning = false;
  private pinching = false;
  private gesturing = false;
  private pinchStartGap = 0;
  private pinchStartTerrainDistance = 0;
  private panClaim: ((x: number, y: number) => boolean) | null = null;
  private claimedPointerId: number | null = null;

  private readonly onPointerDown = (event: PointerEvent) => this.pointerDown(event);
  private readonly onPointerMove = (event: PointerEvent) => this.pointerMove(event);
  private readonly onPointerEnd = (event: PointerEvent) => this.pointerEnd(event);

  constructor(private canvas: HTMLCanvasElement,
              private renderService: BabylonRenderServiceAccessImpl) {
    // Listeners sit on the canvas rather than on the window: the cockpit panels are DOM siblings
    // laid over the canvas, and dragging on a panel must scroll the panel, not the world.
    this.canvas.addEventListener('pointerdown', this.onPointerDown);
    this.canvas.addEventListener('pointermove', this.onPointerMove);
    this.canvas.addEventListener('pointerup', this.onPointerEnd);
    this.canvas.addEventListener('pointercancel', this.onPointerEnd);
  }

  /**
   * Whether the touch interaction in progress - or the one that just ended - moved the camera.
   * Read by the item placer, because on a phone the drag that moves the view and the tap that
   * builds arrive as the same pointer.
   * <p>
   * It stays true until the next finger goes down rather than being cleared when the last one
   * lifts. Babylon has its own listeners on this canvas and there is no guarantee whose pointerup
   * runs first; clearing on release would let the placer see a finished pan as a tap whenever the
   * order came out the other way, and build wherever the pan happened to end.
   */
  isGesturing(): boolean {
    return this.gesturing;
  }

  /**
   * Lets a feature keep a finger for itself: the claim is asked once, where the first finger of an
   * interaction goes down, and while it answers yes that finger never pans.
   * <p>
   * The item placer uses it to let the player drag the building. Without it the ghost and the camera
   * would both follow the same finger, and the building could never be moved relative to the ground
   * it is being placed on.
   */
  setPanClaim(panClaim: ((x: number, y: number) => boolean) | null) {
    this.panClaim = panClaim;
    if (!panClaim) {
      this.claimedPointerId = null;
    }
  }

  private pointerDown(event: PointerEvent) {
    if (event.pointerType !== 'touch') {
      return;
    }
    if (this.pointers.size === 0) {
      // First finger of a new interaction - only here is the previous verdict stale.
      this.gesturing = false;
    }
    const position = this.canvasPosition(event);
    if (this.pointers.size === 0 && this.panClaim?.(position.x, position.y)) {
      this.claimedPointerId = event.pointerId;
    }
    this.pointers.set(event.pointerId, {startX: position.x, startY: position.y, x: position.x, y: position.y});
    if (this.pointers.size === 2) {
      this.startPinch();
    }
  }

  private pointerMove(event: PointerEvent) {
    if (event.pointerType !== 'touch') {
      return;
    }
    const pointer = this.pointers.get(event.pointerId);
    if (!pointer) {
      return;
    }
    const position = this.canvasPosition(event);
    const previousX = pointer.x;
    const previousY = pointer.y;
    pointer.x = position.x;
    pointer.y = position.y;

    if (this.pointers.size >= 2) {
      this.updatePinch();
      return;
    }
    if (this.claimedPointerId === event.pointerId) {
      // Somebody else is dragging with this finger - see setPanClaim().
      return;
    }
    if (!this.panning) {
      const travelled = Math.hypot(position.x - pointer.startX, position.y - pointer.startY);
      if (travelled < TouchCameraControl.DRAG_THRESHOLD) {
        return;
      }
      this.panning = true;
      this.gesturing = true;
      // Reported here rather than from onViewFieldChanged: that callback also fires for camera
      // moves the game makes itself - the opening position, a quest focus, ensureCameraViewOnMap -
      // so every session would claim to have panned at t=0.
      this.renderService.reportFirstInteraction('CAMERA_PAN_TOUCH');
    }
    this.renderService.panByNdc(
      this.ndcX(previousX), this.ndcY(previousY),
      this.ndcX(position.x), this.ndcY(position.y));
  }

  private pointerEnd(event: PointerEvent) {
    if (event.pointerType !== 'touch') {
      return;
    }
    this.pointers.delete(event.pointerId);
    if (this.claimedPointerId === event.pointerId) {
      this.claimedPointerId = null;
    }
    if (this.pointers.size < 2) {
      this.pinching = false;
    }
    if (this.pointers.size === 0) {
      this.panning = false;
      // gesturing deliberately survives - see isGesturing().
    } else {
      // A finger lifted out of a pinch leaves the other one somewhere far from where it started;
      // treating that as the beginning of a pan would jump the camera by the whole gap.
      this.pointers.forEach(pointer => {
        pointer.startX = pointer.x;
        pointer.startY = pointer.y;
      });
    }
  }

  private startPinch() {
    const gap = this.pinchGap();
    if (gap < TouchCameraControl.MIN_PINCH_DISTANCE) {
      return;
    }
    this.pinching = true;
    this.gesturing = true;
    this.panning = false;
    this.pinchStartGap = gap;
    this.pinchStartTerrainDistance = this.renderService.getCameraTerrainDistance();
    this.renderService.reportFirstInteraction('CAMERA_PINCH');
  }

  private updatePinch() {
    if (!this.pinching) {
      // The fingers started too close together to give a ratio; take the first gap that is wide
      // enough as the reference rather than dropping the gesture.
      this.startPinch();
      return;
    }
    const gap = this.pinchGap();
    if (gap < TouchCameraControl.MIN_PINCH_DISTANCE) {
      return;
    }
    // Fingers moving apart (gap grows) pulls the camera closer, which is zooming in.
    this.renderService.setCameraTerrainDistance(this.pinchStartTerrainDistance * (this.pinchStartGap / gap));
  }

  private pinchGap(): number {
    const [first, second] = Array.from(this.pointers.values());
    if (!first || !second) {
      return 0;
    }
    return Math.hypot(first.x - second.x, first.y - second.y);
  }

  private canvasPosition(event: PointerEvent): { x: number, y: number } {
    const rect = this.canvas.getBoundingClientRect();
    return {x: event.clientX - rect.left, y: event.clientY - rect.top};
  }

  /** Canvas pixels to normalized device coordinates, the space the camera projection works in. */
  private ndcX(x: number): number {
    const width = this.canvas.getBoundingClientRect().width;
    return width ? (x / width) * 2 - 1 : 0;
  }

  private ndcY(y: number): number {
    const height = this.canvas.getBoundingClientRect().height;
    return height ? 1 - (y / height) * 2 : 0;
  }

  disable() {
    this.canvas.removeEventListener('pointerdown', this.onPointerDown);
    this.canvas.removeEventListener('pointermove', this.onPointerMove);
    this.canvas.removeEventListener('pointerup', this.onPointerEnd);
    this.canvas.removeEventListener('pointercancel', this.onPointerEnd);
    this.pointers.clear();
    this.panning = false;
    this.pinching = false;
    this.gesturing = false;
    this.claimedPointerId = null;
  }
}
