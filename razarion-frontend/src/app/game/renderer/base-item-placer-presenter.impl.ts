import {
  markPlacerClosed,
  notifyPlacement,
  setOpenPlacerCancel,
  setPlacerCloseReason,
  takePlacerCloseReason
} from './placer-release';
import {PointerEventTypes, PointerInfo} from "@babylonjs/core/Events/pointerEvents";
import {StandardMaterial} from "@babylonjs/core/Materials/standardMaterial";
import {Color3} from "@babylonjs/core/Maths/math.color";
import {Matrix, Vector3} from "@babylonjs/core/Maths/math.vector";
import {Mesh} from "@babylonjs/core/Meshes/mesh";
import {MeshBuilder} from "@babylonjs/core/Meshes/meshBuilder";
import {Observer} from "@babylonjs/core/Misc/observable";
import {Tools} from "@babylonjs/core/Misc/tools";
import {Scene} from "@babylonjs/core/scene";
import {Nullable} from "@babylonjs/core/types";
import {BaseItemPlacer, BaseItemPlacerPresenter, Diplomacy} from "src/app/gwtangular/GwtAngularFacade";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {BabylonModelService} from "./babylon-model.service";
import {BabylonAudioService} from "./babylon-audio.service";
import {AdvancedDynamicTexture} from "@babylonjs/gui/2D/advancedDynamicTexture";
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
  /**
   * How far the ghost may be nudged when the placer opens on a spot the game will not accept,
   * as a fraction of the smaller screen dimension. Far enough to clear a building, near enough
   * that the player sees it settle rather than finding it gone.
   */
  private static readonly NUDGE_MAX_SCREEN_FRACTION = 0.35;
  /**
   * Rings searched outward and probes per ring. Every probe is one onMove plus one validity
   * check - the same work a single mouse move costs - so thirty-two of them is a few frames
   * worth of budget, spent once, at the moment the placer opens.
   */
  private static readonly NUDGE_RINGS = 4;
  private static readonly NUDGE_PROBES_PER_RING = 8;

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
  /**
   * How long the placer may stand on a camera-computed position before that is reported. Three
   * seconds is well past the point where a tile under the camera should have been built, and short
   * enough to still be inside the session: the players this exists to find leave after twenty.
   */
  private static readonly NO_TERRAIN_REPORT_MS = 3000;

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
  /** Set while the placer's model is still on the wire; pulled when the placer closes first. */
  private cancelModelRequest: (() => void) | null = null;
  /** Set as soon as the player drags or taps the ghost themselves. */
  private movedByPlayer = false;
  /** Whether this activation has already reported that it opened without terrain. */
  private noTerrainReported = false;
  /**
   * Whether this activation is a builder's or factory's placer rather than the start base's. The
   * two report under different kinds: the tracker keeps the first record of a kind per session, and
   * the start placer always comes first, so a shared kind never showed a single building placer.
   * See BUILD_PLACER_SHOWN in first-interaction-tracker.service.ts.
   */
  private buildPlacer = false;
  /**
   * `type=<itemTypeId>` on every building placer kind. The tracker keys on kind and detail, so with
   * it each building is on record once per session instead of only the first one - the factory of
   * quest 358. Without it, what happened in the dockyard placer of quest 386 was invisible: 19 of 28
   * players who failed there pressed the button, watched the camera fly to the coast and left.
   */
  private buildTypeDetail = '';
  /** Set when this activation built something, so closing it is not reported as giving up. */
  private placedThisActivation = false;
  /** When this building placer opened, for how long it stayed open before it was abandoned. */
  private openedAt = 0;
  /**
   * An abandon waiting to be reported. The engine closes the open placer before it opens the next
   * one, in the same call: a second press on the build button reads as abandoned and is not. Held
   * until the end of the task, and labelled `replaced` if a placer opened in between.
   */
  private pendingAbandon: { detail: string, reason: string | null } | null = null;

  constructor(private rendererService: BabylonRenderServiceAccessImpl,
              private babylonModelService: BabylonModelService,
              private babylonAudioService: BabylonAudioService) {
    this.material = new StandardMaterial("Base Item Placer", this.rendererService.getScene());
    this.material.emissiveColor = Color3.Red()
    this.rallyMaterial = new StandardMaterial("Base Item Rally", this.rendererService.getScene());
    this.rallyMaterial.emissiveColor = Color3.Red();
  }

  activate(baseItemPlacer: BaseItemPlacer): void {
    // Reported before anything is drawn: what this answers is whether the player was ever asked to
    // place a base, and that has to be true even if the drawing below fails.
    // Only the start placer cannot be cancelled, so that is what tells the two apart.
    this.buildPlacer = baseItemPlacer.isCanBeCanceled();
    this.buildTypeDetail = this.buildPlacer ? 'type=' + baseItemPlacer.getBaseItemTypeId() : '';
    this.placedThisActivation = false;
    this.openedAt = Date.now();
    this.flushAbandon('replaced');
    setOpenPlacerCancel(this.buildPlacer ? () => baseItemPlacer.cancel() : null);
    if (this.buildPlacer) {
      this.rendererService.reportFirstInteraction('BUILD_PLACER_SHOWN', this.buildTypeDetail);
    } else {
      this.rendererService.reportFirstInteraction('PLACER_SHOWN');
    }
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
    const model3DId = baseItemPlacer.getModel3DId()!;
    if (this.babylonModelService.isModel3DReady(model3DId)) {
      this.createGhosts(model3DId);
    } else {
      // The glb may still be downloading when the first quest opens the placer - which is the one
      // moment in the whole funnel that must not fail. The disc, the hint and the deploy button
      // are all up and usable; only the translucent building is missing, and it drops in the
      // moment the model lands, at whatever position the player has dragged the placer to by then.
      this.cancelModelRequest = this.babylonModelService.requestModel3D(model3DId, ready => {
        this.cancelModelRequest = null;
        if (!ready || this.activationGeneration !== currentGeneration) {
          return;
        }
        this.createGhosts(model3DId);
        if (this.currentPosition) {
          this.setPosition(baseItemPlacer, this.currentPosition.clone());
        }
      });
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

    /*
     * The placer has to be somewhere from the first frame, and the terrain ray pick cannot promise
     * that: it needs the tile under the screen centre to exist, and since the game deliberately
     * starts before the tiles are built there is a window in which it does not.
     *
     * What used to happen in that window is that nothing was positioned at all. The disc is created
     * with only its y set, so it kept x=0, z=0 - the corner of the map - and the hint bubble is
     * linked to the disc and went with it. The placer was fully active off screen, holding the pan
     * claim, retrying once a second in silence. On PROD that cost every single base placed from the
     * Meta in-app browser.
     *
     * So the pick is now an improvement on a position, not a precondition for having one. The
     * camera-computed point is a few centimetres of height away from the picked one at worst, and
     * it is on screen, which is the whole difference.
     */
    this.movedByPlayer = false;
    this.noTerrainReported = false;
    // The unload placer knows where the ground in the ship's reach takes a unit - often a strip too
    // thin to find by trying (quest 392). Its spot beats the screen centre.
    const openPosition = baseItemPlacer.getOpenPosition?.();
    const pickedPoint = openPosition
      ? new Vector3(openPosition.getX(), this.rendererService.getTerrainHeightAt(openPosition.getX(), openPosition.getY()) ?? 0, openPosition.getY())
      : this.setupPickedPoint();
    if (pickedPoint) {
      this.openAt(baseItemPlacer, pickedPoint);
      if (openPosition && this.buildPlacer && this.currentPosition) {
        // The spot was searched around the picture's middle, not at it - after the camera flight of
        // quest 386 the nearest water lay up to 25 m above, so the ghost opened at the top edge and
        // its hint bubble, which sits above it, slid under the quest line; the cancel button went
        // under the minimap. Only DEPLOY was left to read. Bring the spot to the middle instead.
        this.rendererService.flyViewFieldCenter(this.currentPosition.x, this.currentPosition.z);
      }
    } else {
      const estimated = this.rendererService.setupCenterTerrainPosition();
      if (estimated) {
        this.openAt(baseItemPlacer, estimated);
      }
      this.setupPickedPointDelayed(baseItemPlacer, currentGeneration, Date.now());
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
        setPlacerCloseReason('esc');
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
    this.movedByPlayer = true;
    this.setPosition(baseItemPlacer, new Vector3(point.x, point.y, point.z));
  }

  /** Move the ghost under the pointer. Returns whether the pointer was over terrain at all. */
  private moveToPointer(baseItemPlacer: BaseItemPlacer): boolean {
    const pickingInfo = this.rendererService.setupTerrainPickPoint();
    if (!pickingInfo.hit) {
      return false;
    }
    this.movedByPlayer = true;
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
    /*
     * Re-check at exactly the position about to be built on, before anything is committed.
     *
     * Green is a snapshot of the last onMove, which came from the drag: a slightly different
     * position, and an older picture of the world. onPlace() checks again on its way through, and
     * if that check refuses, BaseItemPlacerService.onPlace() builds nothing and says nothing -
     * while this method has already fired PLACED, closed the deploy bubble and reported the
     * placement as confirmed. Reported from a phone in the Meta in-app browser on 2026-09-16: the
     * factory placer was green, the dialog vanished, nothing was built, and the build arrow came
     * back on the builder a while later.
     *
     * Forcing the check here closes the window rather than papering over it. This call, the test
     * below and onPlace() all run in one synchronous pass, and worker updates arrive as their own
     * tasks - so nothing can change the answer in between, and the silent branch downstream
     * becomes unreachable instead of merely unlikely.
     */
    baseItemPlacer.onMove(position.x, position.z);
    // Ignore clicks on an invalid spot (red preview): occupied by an item or a resource, wrong
    // terrain, enemy too near or outside the allowed area. Without this the placement was sent
    // anyway - the master silently dropped builder builds and let the start builder spawn on
    // top of a resource.
    if (!baseItemPlacer.isPositionValid()) {
      /*
       * With the reason, because without it the record cannot be acted on. The six conditions
       * want different repairs - the opening search can clear "blocked by another item", and is
       * powerless against "outside the allowed area" - and which one dominates decides what to
       * build next. Measured over seven days before the search existed, 44% of the sessions that
       * clicked were rejected at least once; the reasons behind that number had to be read out
       * of Cloud Logging, where onInvalidPlaceAttempt logs them, and that record carries no
       * gameSessionUuid. So it could never be crossed with the device, the outcome, or whether
       * the player gave up.
       *
       * Keyed with the kind, so a session reports each distinct reason once - the same shape
       * ENGINE_ERROR already uses, and MAX_PER_KIND caps it at five.
       */
      const reason = baseItemPlacer.getErrorText() || 'unknown';
      if (this.buildPlacer) {
        this.rendererService.reportFirstInteraction('BUILD_PLACER_REJECTED', this.buildTypeDetail + ' reason=' + reason);
      } else {
        this.rendererService.reportFirstInteraction('PLACER_REJECTED', reason);
      }
      baseItemPlacer.onInvalidPlaceAttempt();
      return;
    }
    if (this.baseItemPlacerCallback) {
      this.baseItemPlacerCallback(BaseItemPlacerPresenterEvent.PLACED);
    }
    // Before notifyPlacement: that puts the builder down, and a builder leaving the selection closes
    // an open building placer - which must not be this one, on its way to building.
    this.placedThisActivation = true;
    setOpenPlacerCancel(null);
    // After PLACED - the tip remembers the order from the selection - and before onPlace(), which
    // closes the placer and with it spends the callback.
    notifyPlacement(true);
    if (baseItemPlacer.isPlayBuildSound()) {
      this.babylonAudioService.speakCommand('Building');
    }
    // glb=0: the builder this base spawns will finish spawning with no model to draw. See MODELS_READY.
    this.rendererService.reportFirstInteraction(this.buildPlacer ? 'BUILD_PLACER_CONFIRMED' : 'PLACER_CONFIRMED',
      this.buildPlacer ? this.buildTypeDetail : 'glb=' + (this.babylonModelService.areModelsLoaded() ? 1 : 0));
    // The ghost goes away with the placer, and the construction site only appears once the engine
    // has created it - the rings bridge that gap on the green disc the player just confirmed.
    this.rendererService.showGroundCommandMarker(position.x, position.z, this.discRadius, 'build', position.y);
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
    // A finger has no Escape key: a building placer gets a button to close it. The start base has
    // to be placed, so its placer gets none.
    this.pressMouseVisualization.setTouchMode(() => this.deploy(baseItemPlacer),
      this.buildPlacer ? () => {
        setPlacerCloseReason('x');
        baseItemPlacer.cancel();
      } : null);
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

  /**
   * Waits for the terrain to appear under the screen centre and moves the ghost onto it.
   * <p>
   * The placer is already on screen by the time this runs - see {@link activate} - so this is a
   * correction and no longer the only thing that can put it anywhere. It ends on the first
   * successful pick, and reports once the wait has gone on long enough to be worth knowing about.
   */
  private setupPickedPointDelayed(baseItemPlacer: BaseItemPlacer, generation: number, since: number) {
    setTimeout(() => {
      if (this.activationGeneration !== generation) {
        return;
      }
      const pickedPoint = this.setupPickedPoint();
      if (pickedPoint) {
        // A player who has already dragged the ghost somewhere meant to put it there. Snapping it
        // back to the screen centre would undo a deliberate move, so the correction only applies
        // while the placer still stands where the game put it.
        if (!this.movedByPlayer) {
          this.openAt(baseItemPlacer, pickedPoint);
        }
        return;
      }
      if (!this.noTerrainReported && Date.now() - since >= BaseItemPlacerPresenterImpl.NO_TERRAIN_REPORT_MS) {
        // Once. The wait below has no upper bound, and the tracker discarding a repeat is not a
        // reason to hand it one every second for the rest of the session.
        this.noTerrainReported = true;
        this.rendererService.reportFirstInteraction('PLACER_NO_TERRAIN');
      }
      this.setupPickedPointDelayed(baseItemPlacer, generation, since);
    }, 1000);
  }

  private cleanupPreviousPlacer(): void {
    this.rendererService.touchCameraControl?.setPanClaim(null);
    if (this.cancelModelRequest) {
      this.cancelModelRequest();
      this.cancelModelRequest = null;
    }
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
    markPlacerClosed();
    notifyPlacement(false); // closed without a placement - after one, the callback is already spent
    const reason = takePlacerCloseReason();
    if (this.buildPlacer && !this.placedThisActivation) {
      const open = Math.round((Date.now() - this.openedAt) / 100) / 10;
      this.pendingAbandon = {detail: `${this.buildTypeDetail} open=${open}`, reason};
      setTimeout(() => this.flushAbandon(null), 0);
    }
    this.buildPlacer = false;
    setOpenPlacerCancel(null);
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

  /**
   * Reports the abandon held by deactivate(), if it is still waiting. A named reason (the ✕, Escape,
   * the builder leaving the selection) wins; otherwise `fallback`, which is `replaced` when another
   * placer opened right behind it and `other` when nothing did - a scene ending, for one.
   */
  private flushAbandon(fallback: string | null): void {
    const pending = this.pendingAbandon;
    if (!pending) {
      return;
    }
    this.pendingAbandon = null;
    this.rendererService.reportFirstInteraction('BUILD_PLACER_ABANDONED',
      `${pending.detail} by=${pending.reason ?? fallback ?? 'other'}`);
  }

  setBaseItemPlacerCallback(callback: ((event: BaseItemPlacerPresenterEvent) => void) | null) {
    this.baseItemPlacerCallback = callback;
  }

  /** One translucent building per relative offset, all sharing the placer's model. */
  private createGhosts(model3DId: number): void {
    for (let i = 0; i < this.relativeOffsets.length; i++) {
      const renderObject = this.babylonModelService.cloneModel3D(model3DId, null, Diplomacy.OWN_PLACER);
      renderObject.setRotationY(Tools.ToRadians(90));
      this.renderObjects.push(renderObject);
    }
  }

  /**
   * Put the ghost down where the placer opened, and if the game will not accept that spot, on
   * the nearest one it will.
   * <p>
   * The placer used to open wherever the camera happened to point and merely colour the ghost
   * red. Measured over seven days on PROD: of 520 sessions that clicked at all, 231 - 44% - had
   * their first click rejected, and 23% of those never placed a base at all. For the start
   * base the reasons were 48% "blocked by another item" and 34% "cannot build on a razarion
   * field", so the player was not aiming badly; the placer was opening on top of things.
   * <p>
   * Only on opening, and only while the player has not taken over: once somebody drags the
   * ghost themselves, where it sits is their decision and moving it would be rude.
   */
  private openAt(baseItemPlacer: BaseItemPlacer, pickedPoint: Vector3): void {
    this.setPosition(baseItemPlacer, pickedPoint);
    if (this.movedByPlayer || baseItemPlacer.isPositionValid()) {
      return;
    }
    const better = this.findValidPosition(baseItemPlacer, pickedPoint);
    // setPosition either way: the probing left the placer on the last spot it tried, and its
    // error text with it. Without this the bubble would name a reason for a position the ghost
    // is not standing on.
    this.setPosition(baseItemPlacer, better ?? pickedPoint);
  }

  /**
   * The nearest acceptable spot, searched outward in rings. Null when the neighbourhood is full,
   * which is an answer too - the ghost then stays put and stays red, as before.
   * <p>
   * Probes carry no terrain height: {@link BaseItemPlacer#onMove} only takes x and z, and the
   * height is cosmetic. One ray pick is spent on the winner, none on the losers.
   */
  private findValidPosition(baseItemPlacer: BaseItemPlacer, start: Vector3): Vector3 | null {
    const maxRadius = this.nudgeRadiusLimit(start);
    if (maxRadius === null || !(maxRadius > 0)) {
      return null;
    }
    for (let ring = 1; ring <= BaseItemPlacerPresenterImpl.NUDGE_RINGS; ring++) {
      const radius = maxRadius * ring / BaseItemPlacerPresenterImpl.NUDGE_RINGS;
      const probes = BaseItemPlacerPresenterImpl.NUDGE_PROBES_PER_RING;
      // Every other ring is rotated half a step so the probes do not all sit on the same spokes,
      // which would miss a gap lying between them at every radius.
      const phase = (ring % 2) * Math.PI / probes;
      for (let i = 0; i < probes; i++) {
        const angle = phase + 2 * Math.PI * i / probes;
        const x = start.x + radius * Math.cos(angle);
        const z = start.z + radius * Math.sin(angle);
        baseItemPlacer.onMove(x, z);
        if (baseItemPlacer.isPositionValid()) {
          return new Vector3(x, this.rendererService.getTerrainHeightAt(x, z) ?? start.y, z);
        }
      }
    }
    return null;
  }

  /**
   * How far the nudge may reach, in world units, so that it is the same distance on screen at
   * every zoom level. Bounding it in world units instead would be a nudge on a zoomed-out view
   * and a teleport on a zoomed-in one.
   */
  private nudgeRadiusLimit(start: Vector3): number | null {
    const scene = this.rendererService.getScene();
    const camera = scene.activeCamera;
    if (!camera) {
      return null;
    }
    const engine = scene.getEngine();
    const viewport = camera.viewport.toGlobal(engine.getRenderWidth(), engine.getRenderHeight());
    const transform = scene.getTransformMatrix();
    const centre = Vector3.Project(start, Matrix.Identity(), transform, viewport);
    const edge = Vector3.Project(new Vector3(start.x + 1, start.y, start.z),
      Matrix.Identity(), transform, viewport);
    const pixelsPerWorldUnit = Math.hypot(edge.x - centre.x, edge.y - centre.y);
    if (!isFinite(pixelsPerWorldUnit) || pixelsPerWorldUnit <= 0) {
      return null;
    }
    return Math.min(viewport.width, viewport.height) *
      BaseItemPlacerPresenterImpl.NUDGE_MAX_SCREEN_FRACTION / pixelsPerWorldUnit;
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
