import {
  Color3,
  Matrix,
  Mesh,
  MeshBuilder,
  Nullable,
  Observer,
  PointerEventTypes,
  PointerInfo,
  Scene,
  StandardMaterial,
  Tools,
  Vector3
} from "@babylonjs/core";
import {BaseItemPlacer, BaseItemPlacerPresenter, Diplomacy} from "src/app/gwtangular/GwtAngularFacade";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {BabylonModelService} from "./babylon-model.service";
import {BabylonAudioService} from "./babylon-audio.service";
import {AdvancedDynamicTexture} from "@babylonjs/gui";
import {RenderObject} from './render-object';
import {PressMouseVisualization} from './press-mouse-visualization';

export enum BaseItemPlacerPresenterEvent {
  ACTIVATED,
  PLACED,
  DEACTIVATED
}

export class BaseItemPlacerPresenterImpl implements BaseItemPlacerPresenter {
  /**
   * How far from the ghost a finger may land and still grab it, in canvas pixels. The real grab area
   * is the placer disc, but it is clamped into this band: a disc smaller than the lower bound cannot
   * be hit with a thumb, and one larger than the upper bound would swallow the whole screen and
   * leave nowhere to start a camera pan.
   */
  private static readonly MIN_GRAB_RADIUS_PX = 55;
  private static readonly MAX_GRAB_RADIUS_PX = 160;
  /** Same tap tolerance the camera control and the terrain click use. */
  private static readonly TAP_THRESHOLD_PX = 5;
  /**
   * Hint bubble: how far above the building it hangs, and where it goes when there is no room.
   * <p>
   * These are offsets to its centre, so they carry its height: the bubble stacks its text over the
   * deploy button and is 132px tall, and each was moved by half the 56px it grew by. Keeping the
   * old numbers would have brought its lower edge down onto the building it is describing.
   */
  private static readonly HINT_ABOVE_OFFSET_PX = -128;
  private static readonly HINT_BELOW_OFFSET_PX = 138;
  /** Below this the bubble would hang off the top of the screen, so it moves under the building. */
  private static readonly HINT_ABOVE_MIN_Y_PX = 200;

  private disc: Mesh | null = null;
  private rallyDisc: Mesh | null = null;
  private rallyOffsetX = 0;
  private rallyOffsetZ = 0;
  // One ghost model per unit to be placed, with its offset relative to the cursor.
  // DecimalPosition.x maps to world-X, DecimalPosition.y maps to world-Z.
  private renderObjects: RenderObject[] = [];
  private relativeOffsets: { x: number; z: number }[] = [];
  private uiTexture: AdvancedDynamicTexture | null = null;
  private pressMouseVisualization: PressMouseVisualization | null = null;
  private readonly material;
  private readonly rallyMaterial;
  private pointerObservable: Nullable<Observer<PointerInfo>> = null;
  private hintObserver: Nullable<Observer<Scene>> = null;
  private baseItemPlacerCallback: ((event: BaseItemPlacerPresenterEvent) => void) | null = null;
  private keydownHandler: ((event: KeyboardEvent) => void) | null = null;
  private activationGeneration = 0;
  /** Where the ghost currently stands, the position the deploy button builds on. */
  private currentPosition: Vector3 | null = null;
  private discRadius = 0;
  private touchDragPointerId: number | null = null;
  private touchDownScreen: { x: number, y: number } | null = null;
  /** Ghost centre minus the finger, in canvas pixels, held for the length of a drag. */
  private touchDragOffset: { x: number, y: number } = {x: 0, y: 0};

  constructor(private rendererService: BabylonRenderServiceAccessImpl,
              private babylonModelService: BabylonModelService,
              private babylonAudioService: BabylonAudioService) {
    this.material = new StandardMaterial("Base Item Placer", this.rendererService.getScene());
    this.material.emissiveColor = Color3.Red()
    this.rallyMaterial = new StandardMaterial("Base Item Rally", this.rendererService.getScene());
    this.rallyMaterial.emissiveColor = Color3.Red();
  }

  activate(baseItemPlacer: BaseItemPlacer): void {
    this.cleanupPreviousPlacer();
    this.activationGeneration++;
    const currentGeneration = this.activationGeneration;
    this.discRadius = baseItemPlacer.getEnemyFreeRadius();
    this.disc = MeshBuilder.CreateDisc("Base Item Placer", {radius: this.discRadius}, this.rendererService.getScene());
    this.disc.visibility = 0.5;
    this.disc.material = this.material;
    this.disc.rotation.x = Tools.ToRadians(90);
    this.disc.isPickable = false;
    this.disc.position.y = 0.1;

    if (baseItemPlacer.hasRallyPoint()) {
      // DecimalPosition.y maps to world-Z (game uses XZ ground plane).
      this.rallyOffsetX = baseItemPlacer.getRallyOffsetX();
      this.rallyOffsetZ = baseItemPlacer.getRallyOffsetY();
      this.rallyDisc = MeshBuilder.CreateDisc("Base Item Rally", {radius: baseItemPlacer.getRallyRadius()}, this.rendererService.getScene());
      this.rallyDisc.visibility = 0.5;
      this.rallyDisc.material = this.rallyMaterial;
      this.rallyDisc.rotation.x = Tools.ToRadians(90);
      this.rallyDisc.isPickable = false;
      this.rallyDisc.position.y = 0.1;
    }

    const positionValid = baseItemPlacer.isPositionValid();
    this.material.emissiveColor = positionValid ? Color3.Green() : Color3.Red();
    this.rallyMaterial.emissiveColor = positionValid ? Color3.Green() : Color3.Red();

    this.relativeOffsets = [];
    const relativePositions = baseItemPlacer.getRelativeItemPositions() || [];
    if (relativePositions.length > 0) {
      for (const relativePosition of relativePositions) {
        this.relativeOffsets.push({x: relativePosition.getX(), z: relativePosition.getY()});
      }
    } else {
      this.relativeOffsets.push({x: 0, z: 0});
    }
    for (let i = 0; i < this.relativeOffsets.length; i++) {
      const renderObject = this.babylonModelService.cloneModel3D(baseItemPlacer.getModel3DId()!, null, Diplomacy.OWN_PLACER);
      renderObject.setRotationY(Tools.ToRadians(90));
      this.renderObjects.push(renderObject);
    }

    this.uiTexture = AdvancedDynamicTexture.CreateFullscreenUI("Base item placer");
    this.uiTexture.disablePicking = true; // Prevent mouse down on terrain cursor change
    this.pressMouseVisualization = new PressMouseVisualization(positionValid, this.rendererService);
    this.uiTexture.addControl(this.pressMouseVisualization.getContainer());
    this.pressMouseVisualization.getContainer().linkWithMesh(this.disc!);
    this.pressMouseVisualization.getContainer().linkOffsetY = BaseItemPlacerPresenterImpl.HINT_ABOVE_OFFSET_PX;
    if (window.matchMedia?.('(pointer: coarse)').matches) {
      // A phone gets the touch controls straight away rather than after its first tap - the hint is
      // read before anything is touched, and it may not say "click the left mouse button" there.
      this.enableTouchMode(baseItemPlacer);
    }

    let pickedPoint = this.setupPickedPoint();
    if (pickedPoint) {
      this.setPosition(baseItemPlacer, pickedPoint);
    } else {
      this.setupPickedPointDelayed(baseItemPlacer, currentGeneration);
    }

    this.rendererService.baseItemPlacerActive = true;

    // Placing is not selecting: an armed selection box would sit under the placer and take the
    // finger that is supposed to drag the building.
    this.rendererService.touchSelectionMode.disarm();

    this.rendererService.touchCameraControl?.setPanClaim((x, y) => this.isGrip(x, y));

    this.pointerObservable = this.rendererService.getScene().onPointerObservable.add((pointerInfo) => {
      const event = pointerInfo.event as PointerEvent;
      const touch = event?.pointerType === 'touch';
      if (touch) {
        this.enableTouchMode(baseItemPlacer);
      }
      switch (pointerInfo.type) {
        case PointerEventTypes.POINTERDOWN: {
          // A finger going down is the start of a gesture that may be a pan, a drag of the building
          // or a tap - none of which is a build. On touch nothing is ever built from a pointer event;
          // that is what the deploy button is for. The mouse keeps placing on the press as before.
          if (touch) {
            this.touchDownScreen = {x: this.rendererService.getScene().pointerX, y: this.rendererService.getScene().pointerY};
            // A second finger means a pinch, and a pinch is not a drag of the building. Clearing
            // first also drops a drag whose release was never reported - a cancelled gesture would
            // otherwise leave the building stuck to the next finger with the same id.
            const secondFinger = this.touchDragPointerId !== null && this.touchDragPointerId !== event.pointerId;
            this.touchDragPointerId = null;
            if (!secondFinger && this.isGrip(this.touchDownScreen.x, this.touchDownScreen.y)
              && this.startDragOffset()) {
              this.touchDragPointerId = event.pointerId;
            }
            break;
          }
          this.place(baseItemPlacer);
          break;
        }
        case PointerEventTypes.POINTERUP: {
          if (!touch) {
            break;
          }
          const wasDragging = this.touchDragPointerId === event.pointerId;
          this.touchDragPointerId = null;
          const downScreen = this.touchDownScreen;
          this.touchDownScreen = null;
          if (wasDragging) {
            // The building was carried to where it now stands; lifting the finger is not a build.
            break;
          }
          if (!downScreen || this.rendererService.touchCameraControl?.isGesturing()) {
            break;
          }
          const scene = this.rendererService.getScene();
          if (Math.hypot(scene.pointerX - downScreen.x, scene.pointerY - downScreen.y)
            > BaseItemPlacerPresenterImpl.TAP_THRESHOLD_PX) {
            break;
          }
          // A tap moves the building to the spot instead of building there. Building blind - on a
          // spot the player has never seen judged green or red - is what tapping used to do.
          this.moveToPointer(baseItemPlacer);
          break;
        }
        case PointerEventTypes.POINTERMOVE: {
          if (touch) {
            // Only the finger that grabbed the building moves it. Any other one is panning the
            // camera, and there the ghost stays put in the world while the ground slides beneath it.
            if (this.touchDragPointerId === event.pointerId) {
              this.dragToPointer(baseItemPlacer);
            }
            break;
          }
          this.moveToPointer(baseItemPlacer);
          break;
        }
      }
    });

    this.keydownHandler = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && baseItemPlacer.isCanBeCanceled()) {
        baseItemPlacer.cancel();
      }
    };
    window.addEventListener('keydown', this.keydownHandler);

    if (this.baseItemPlacerCallback) {
      this.baseItemPlacerCallback(BaseItemPlacerPresenterEvent.ACTIVATED);
    }
  }

  /** Put the ghost where the pointer is and build there if the spot allows it. The mouse route. */
  private place(baseItemPlacer: BaseItemPlacer): void {
    if (!this.moveToPointer(baseItemPlacer)) {
      return;
    }
    this.deploy(baseItemPlacer);
  }

  /**
   * Remember where the building was grabbed, as the distance in screen pixels from the finger to
   * the building. Dragging keeps that distance, so the building does not jump its centre under the
   * finger the moment it is touched, and a thumb on the edge of the disc does not cover the very
   * thing being judged green or red.
   * <p>
   * Screen pixels, not world units. The two are not the same thing under a tilted camera: a world
   * offset held constant covers fewer and fewer pixels as the building is dragged away from the
   * viewer, so whatever the finger was holding slides out from under it. On the building itself
   * that is a pixel or two; the hint bubble is grabbed 128px above the building, and there the
   * drift was plain to see.
   */
  private startDragOffset(): boolean {
    this.touchDragOffset = {x: 0, y: 0};
    const ghost = this.projectGhost();
    if (!ghost) {
      return false;
    }
    const scene = this.rendererService.getScene();
    this.touchDragOffset = {x: ghost.x - scene.pointerX, y: ghost.y - scene.pointerY};
    return true;
  }

  /**
   * Carry the ghost with the finger, holding the grip taken in {@link startDragOffset}: the ground
   * is picked where the building is meant to be, not where the finger is, so the building lands on
   * real ground with the height that belongs to it.
   */
  private dragToPointer(baseItemPlacer: BaseItemPlacer): void {
    const scene = this.rendererService.getScene();
    const pickingInfo = this.rendererService.setupTerrainPickPoint(
      scene.pointerX + this.touchDragOffset.x,
      scene.pointerY + this.touchDragOffset.y);
    if (!pickingInfo.hit) {
      // The building would be off the map or above the horizon. It stays where it is rather than
      // being dropped somewhere else.
      return;
    }
    const point = pickingInfo.pickedPoint!;
    this.setPosition(baseItemPlacer, new Vector3(point.x, point.y, point.z));
  }

  /** Move the ghost under the pointer. Returns whether the pointer was over terrain at all. */
  private moveToPointer(baseItemPlacer: BaseItemPlacer): boolean {
    const pickingInfo = this.rendererService.setupTerrainPickPoint();
    if (!pickingInfo.hit) {
      return false;
    }
    this.setPosition(baseItemPlacer, pickingInfo.pickedPoint!);
    return true;
  }

  /**
   * Build at the position the ghost currently occupies. Reached from the mouse press and from the
   * deploy button on touch, so both apply the same validity rule.
   */
  private deploy(baseItemPlacer: BaseItemPlacer): void {
    const position = this.currentPosition;
    if (!position) {
      return;
    }
    // Ignore clicks on an invalid spot (red preview): occupied by an item or a resource, wrong
    // terrain, enemy too near or outside the allowed area. Without this the placement was sent
    // anyway - the master silently dropped builder builds and let the start builder spawn on
    // top of a resource.
    if (!baseItemPlacer.isPositionValid()) {
      baseItemPlacer.onInvalidPlaceAttempt();
      return;
    }
    if (this.baseItemPlacerCallback) {
      this.baseItemPlacerCallback(BaseItemPlacerPresenterEvent.PLACED);
    }
    if (baseItemPlacer.isPlayBuildSound()) {
      this.babylonAudioService.speakCommand('Building');
    }
    baseItemPlacer.onPlace(position.x, position.z);
  }

  /**
   * Switch the hint bubble over to the touch controls. Done on the first touch event rather than
   * from a device query alone, so a tablet that reports a mouse still gets the button as soon as a
   * finger is used.
   */
  private enableTouchMode(baseItemPlacer: BaseItemPlacer): void {
    if (!this.pressMouseVisualization || this.pressMouseVisualization.isTouchMode()) {
      return;
    }
    this.pressMouseVisualization.setTouchMode(() => this.deploy(baseItemPlacer));
    if (this.uiTexture) {
      // The button can only be tapped once the texture picks at all; picking is off for the mouse
      // because it fights with the terrain cursor.
      this.uiTexture.disablePicking = false;
    }
    // The building also travels across the screen when the camera moves under it, and then nothing
    // calls setPosition - so the bubble is kept on screen from the render loop as well.
    this.hintObserver = this.rendererService.getScene().onBeforeRenderObservable.add(() => this.updateHintSide());
    this.updateHintSide();
  }

  /**
   * Whether a finger landing at this canvas point grabs the building. Measured against the placer
   * disc as it appears on screen, so the grab area is what the player sees - clamped into a band
   * that is neither too small for a thumb nor big enough to eat the whole screen.
   */
  private isOnGhost(canvasX: number, canvasY: number): boolean {
    const ghost = this.projectGhost();
    if (!ghost) {
      return false;
    }
    return Math.hypot(canvasX - ghost.x, canvasY - ghost.y) <= ghost.grabRadius;
  }

  /**
   * Everything a finger can take hold of the building by: the building itself and the hint bubble
   * riding above it. The bubble is the larger target of the two and sits where a thumb reaching
   * into the screen arrives first, so leaving it out made the placer feel unmovable.
   */
  private isGrip(canvasX: number, canvasY: number): boolean {
    return this.isOnGhost(canvasX, canvasY) || this.isOnHint(canvasX, canvasY);
  }

  /**
   * The hint bubble measures itself in the pixels of the fullscreen GUI texture, which is the size
   * of the render buffer; the pointer is counted in canvas pixels. That is the same factor
   * {@link projectGhost} applies, in the other direction.
   */
  private isOnHint(canvasX: number, canvasY: number): boolean {
    if (!this.pressMouseVisualization) {
      return false;
    }
    const scale = this.rendererService.getScene().getEngine().getHardwareScalingLevel();
    return this.pressMouseVisualization.containsGrip(canvasX / scale, canvasY / scale);
  }

  /** Where the ghost sits on the canvas and how big a target it makes, in canvas pixels. */
  private projectGhost(): { x: number, y: number, grabRadius: number } | null {
    const position = this.currentPosition;
    const scene = this.rendererService.getScene();
    const camera = scene.activeCamera;
    if (!position || !camera) {
      return null;
    }
    const engine = scene.getEngine();
    const viewport = camera.viewport.toGlobal(engine.getRenderWidth(), engine.getRenderHeight());
    const transform = scene.getTransformMatrix();
    const center = Vector3.Project(position, Matrix.Identity(), transform, viewport);
    const edge = Vector3.Project(new Vector3(position.x + this.discRadius, position.y, position.z),
      Matrix.Identity(), transform, viewport);
    // Project() answers in render pixels; the pointer is counted in canvas pixels.
    const scale = engine.getHardwareScalingLevel();
    const radius = Math.hypot(edge.x - center.x, edge.y - center.y) * scale;
    return {
      x: center.x * scale,
      y: center.y * scale,
      grabRadius: Math.min(Math.max(radius, BaseItemPlacerPresenterImpl.MIN_GRAB_RADIUS_PX),
        BaseItemPlacerPresenterImpl.MAX_GRAB_RADIUS_PX)
    };
  }

  /**
   * Keep the hint bubble on screen. It hangs above the building, which is fine until the building
   * is dragged to the top edge - and on touch the bubble carries the deploy button, so letting it
   * leave the screen would leave the player unable to build at all.
   */
  private updateHintSide(): void {
    if (!this.pressMouseVisualization?.isTouchMode()) {
      return;
    }
    const ghost = this.projectGhost();
    if (!ghost) {
      return;
    }
    const container = this.pressMouseVisualization.getContainer();
    container.linkOffsetY = ghost.y < BaseItemPlacerPresenterImpl.HINT_ABOVE_MIN_Y_PX
      ? BaseItemPlacerPresenterImpl.HINT_BELOW_OFFSET_PX
      : BaseItemPlacerPresenterImpl.HINT_ABOVE_OFFSET_PX;
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

  private setupPickedPointDelayed(baseItemPlacer: BaseItemPlacer, generation: number) {
    setTimeout(() => {
      if (this.activationGeneration !== generation) return;
      let pickedPoint = this.setupPickedPoint();
      if (pickedPoint) {
        this.setPosition(baseItemPlacer, pickedPoint);
      } else {
        this.setupPickedPointDelayed(baseItemPlacer, generation);
      }
    }, 1000);
  }

  private cleanupPreviousPlacer(): void {
    this.rendererService.touchCameraControl?.setPanClaim(null);
    this.currentPosition = null;
    this.touchDragPointerId = null;
    this.touchDownScreen = null;
    if (this.keydownHandler) {
      window.removeEventListener('keydown', this.keydownHandler);
      this.keydownHandler = null;
    }
    if (this.pointerObservable) {
      this.rendererService.getScene().onPointerObservable.remove(this.pointerObservable);
      this.pointerObservable = null;
    }
    if (this.hintObserver) {
      this.rendererService.getScene().onBeforeRenderObservable.remove(this.hintObserver);
      this.hintObserver = null;
    }
    if (this.disc) {
      this.rendererService.getScene().removeMesh(this.disc);
      this.disc.dispose();
      this.disc = null;
    }
    if (this.rallyDisc) {
      this.rendererService.getScene().removeMesh(this.rallyDisc);
      this.rallyDisc.dispose();
      this.rallyDisc = null;
    }
    for (const renderObject of this.renderObjects) {
      renderObject.dispose();
    }
    this.renderObjects = [];
    this.relativeOffsets = [];
    if (this.uiTexture) {
      this.uiTexture.dispose();
      this.uiTexture = null;
    }
    this.pressMouseVisualization = null;
  }

  deactivate(): void {
    this.cleanupPreviousPlacer();
    // Defer clearing so ActionManager handlers (terrain/water click) that fire
    // in the same event loop tick still see the placer as active.
    // Use generation check to avoid undoing a subsequent activate().
    const gen = this.activationGeneration;
    setTimeout(() => {
      if (this.activationGeneration === gen) {
        this.rendererService.baseItemPlacerActive = false;
      }
    }, 0);
    if (this.baseItemPlacerCallback) {
      this.baseItemPlacerCallback(BaseItemPlacerPresenterEvent.DEACTIVATED);
    }
  }

  setBaseItemPlacerCallback(callback: ((event: BaseItemPlacerPresenterEvent) => void) | null) {
    this.baseItemPlacerCallback = callback;
  }

  private setPosition(baseItemPlacer: BaseItemPlacer, pickedPoint: Vector3) {
    if (!this.disc) return;
    baseItemPlacer.onMove(pickedPoint.x, pickedPoint.z);
    this.currentPosition = pickedPoint.clone();
    this.disc.position = pickedPoint
    this.disc.position.y += 0.1;
    if (this.rallyDisc) {
      this.rallyDisc.position = new Vector3(pickedPoint.x + this.rallyOffsetX, this.disc.position.y, pickedPoint.z + this.rallyOffsetZ);
    }
    const positionValid = baseItemPlacer.isPositionValid();
    this.material.emissiveColor = positionValid ? Color3.Green() : Color3.Red();
    this.rallyMaterial.emissiveColor = positionValid ? Color3.Green() : Color3.Red();
    this.pressMouseVisualization?.setPositionValid(positionValid, baseItemPlacer.getErrorText());
    this.updateHintSide();
    for (let i = 0; i < this.renderObjects.length; i++) {
      const offset = this.relativeOffsets[i];
      this.renderObjects[i].setPosition(new Vector3(pickedPoint.x + offset.x, pickedPoint.y, pickedPoint.z + offset.z));
      this.renderObjects[i].increaseHeight(0.01);
    }
  }
}
