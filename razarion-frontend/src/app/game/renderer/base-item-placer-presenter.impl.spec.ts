import {FreeCamera} from '@babylonjs/core/Cameras/freeCamera';
import {PickingInfo} from '@babylonjs/core/Collisions/pickingInfo';
import {NullEngine} from '@babylonjs/core/Engines/nullEngine';
import {PointerEventTypes, PointerInfo} from '@babylonjs/core/Events/pointerEvents';
import {Vector3} from '@babylonjs/core/Maths/math.vector';
import {Scene} from '@babylonjs/core/scene';
import {BaseItemPlacerPresenterImpl} from './base-item-placer-presenter.impl';
import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';
import {BabylonModelService} from './babylon-model.service';
import {BabylonAudioService} from './babylon-audio.service';
import {BaseItemPlacer, DecimalPosition} from '../../gwtangular/GwtAngularFacade';
import {AdvancedDynamicTexture} from '@babylonjs/gui/2D/advancedDynamicTexture';
import {cancelOpenPlacer} from './placer-release';

/**
 * The touch half of the item placer. A phone player has one finger for three different intentions -
 * look around, move the building, build it - and until this was split up the finger did the last one
 * whatever it meant: a tap anywhere spawned the base on a spot never seen judged green or red.
 */
describe('BaseItemPlacerPresenterImpl touch handling', () => {
  const WIDTH = 800;
  const HEIGHT = 600;
  const CENTRE_X = WIDTH / 2;
  const CENTRE_Y = HEIGHT / 2;
  /** Camera height; with the straight-down view it also sets the world-to-pixel scale. */
  const CAMERA_HEIGHT = 60;
  const DISC_RADIUS = 10;
  /**
   * Canvas pixels per world unit, the whole point of the camera set up below: it makes the ground
   * this test invents the same ground the camera would project, so a drag can be checked in world
   * units while the code works in pixels.
   */
  const PX_PER_UNIT = 5;

  let engine: NullEngine;
  let scene: Scene;
  let presenter: BaseItemPlacerPresenterImpl;
  let placer: BaseItemPlacer;
  let panClaim: ((x: number, y: number) => boolean) | null;
  let gesturing: boolean;
  let positionValid: boolean;
  /** Canvas line above which the tests put sky: a pick there finds no ground. */
  let horizonY: number;
  let moves: { x: number, z: number }[];
  let places: { x: number, z: number }[];
  let invalidAttempts: number;
  let reportedInteractions: string[];
  /** kind plus the detail where one was given, so a test can assert the reason travelled. */
  let reportedDetails: string[];
  let modelsLoaded: boolean;
  let errorText: string;
  /** Where the camera was sent to centre the placer. */
  let flights: { x: number, y: number }[];

  beforeEach(() => {
    engine = new NullEngine({renderWidth: WIDTH, renderHeight: HEIGHT, textureSize: 512, deterministicLockstep: false, lockstepMaxSteps: 1});
    scene = new Scene(engine);
    // Straight down onto the ground plane: world (0,0,0) lands exactly in the middle of the canvas,
    // so what the grab test does with the projection can be checked without redoing its arithmetic.
    const camera = new FreeCamera('test', new Vector3(0, CAMERA_HEIGHT, 0), scene);
    camera.rotation = new Vector3(Math.PI / 2, 0, 0);
    // A 90 degree field of view puts exactly CAMERA_HEIGHT world units between the middle of the
    // canvas and its edge, which at 600px tall is PX_PER_UNIT. The drag holds its grip in screen
    // pixels, so a mock ground that projected differently from the camera would make every
    // expectation below a fiction.
    camera.fov = Math.PI / 2;
    scene.activeCamera = camera;
    scene.updateTransformMatrix();

    panClaim = null;
    gesturing = false;
    positionValid = true;
    horizonY = Number.NEGATIVE_INFINITY;
    moves = [];
    places = [];
    invalidAttempts = 0;
    reportedInteractions = [];
    reportedDetails = [];
    modelsLoaded = true;
    errorText = '';
    flights = [];

    const rendererService = {
      getScene: () => scene,
      hasPendingSetViewFieldCenter: () => false,
      // Normalized device coordinates, the way the renderer converts them: the placer opens with
      // (0,0), the middle of the canvas, which is where the building starts out.
      setupPickInfoFromNDC: (ndcX: number, ndcY: number) =>
        hit(groundUnder((ndcX + 1) / 2 * WIDTH, (1 - ndcY) / 2 * HEIGHT)),
      // Answers for any canvas point, not just the one the pointer is on: the placer picks where
      // the building is meant to land, which is a screen offset away from the finger holding it.
      setupTerrainPickPoint: (canvasX: number = scene.pointerX, canvasY: number = scene.pointerY) =>
        skyAt(canvasX, canvasY) ? new PickingInfo() : hit(groundUnder(canvasX, canvasY)),
      setupTerrainPickPointFromPosition: () => null,
      // The ground this fixture invents is flat at y=0 - see groundUnder(). Only the nudge asks,
      // and only for the spot it settles on.
      getTerrainHeightAt: () => 0,
      // The placer reports when it appears, when a placement is refused and when one goes through -
      // see PLACER_SHOWN in first-interaction-tracker.service.ts. Collected rather than ignored,
      // so the tests below can say which of the three a given gesture produced.
      reportFirstInteraction: (kind: string, detail?: string) => {
        reportedInteractions.push(kind);
        reportedDetails.push(detail ? kind + '|' + detail : kind);
      },
      showGroundCommandMarker: () => {
      },
      flyViewFieldCenter: (x: number, y: number) => {
        flights.push({x, y});
        return Promise.resolve();
      },
      touchCameraControl: {
        isGesturing: () => gesturing,
        setPanClaim: (claim: ((x: number, y: number) => boolean) | null) => panClaim = claim
      },
      touchSelectionMode: {
        disarm: () => {
        }
      },
      baseItemPlacerActive: false
    } as unknown as BabylonRenderServiceAccessImpl;

    const renderObject = {
      setRotationY: () => {
      },
      setPosition: () => {
      },
      increaseHeight: () => {
      },
      dispose: () => {
      }
    };
    const modelService = {
      isModel3DReady: () => true,
      areModelsLoaded: () => modelsLoaded,
      cloneModel3D: () => renderObject
    } as unknown as BabylonModelService;
    const audioService = {speakCommand: () => {
    }} as unknown as BabylonAudioService;

    placer = {
      getModel3DId: () => 1,
      getBaseItemTypeId: () => 11,
      getRelativeItemPositions: () => [],
      getSpawnAudioId: () => null,
      isPositionValid: () => positionValid,
      getErrorText: () => errorText,
      isPlayBuildSound: () => false,
      getEnemyFreeRadius: () => DISC_RADIUS,
      onMove: (x: number, z: number) => moves.push({x, z}),
      onPlace: (x: number, z: number) => places.push({x, z}),
      onInvalidPlaceAttempt: () => invalidAttempts++,
      isCanBeCanceled: () => false,
      cancel: () => {
      },
      hasRallyPoint: () => false
    } as unknown as BaseItemPlacer;

    presenter = new BaseItemPlacerPresenterImpl(rendererService, modelService, audioService);
    presenter.activate(placer);
  });

  afterEach(() => {
    presenter.deactivate();
    scene.dispose();
    engine.dispose();
  });

  function hit(point: Vector3): PickingInfo {
    const pickingInfo = new PickingInfo();
    pickingInfo.hit = true;
    pickingInfo.pickedPoint = point.clone();
    return pickingInfo;
  }

  /**
   * The ground under a canvas point, the inverse of what the camera above does. The screen points
   * along +z, so further up the canvas is further along z.
   */
  function groundUnder(canvasX: number, canvasY: number): Vector3 {
    return new Vector3((canvasX - CENTRE_X) / PX_PER_UNIT, 0, (CENTRE_Y - canvasY) / PX_PER_UNIT);
  }

  function skyAt(_canvasX: number, canvasY: number): boolean {
    return canvasY < horizonY;
  }

  /** Puts the pointer on the canvas. What lies under it follows from the projection. */
  function pointAt(screenX: number, screenY: number) {
    scene.pointerX = screenX;
    scene.pointerY = screenY;
  }

  function fire(type: number, pointerId = 1, pointerType = 'touch') {
    const event = {pointerId, pointerType, type: type === PointerEventTypes.POINTERUP ? 'pointerup' : 'pointerdown'} as PointerEvent;
    scene.onPointerObservable.notifyObservers(new PointerInfo(type, event, null));
  }

  it('moves the building on a tap instead of building there', () => {
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);

    expect(places.length).toBe(0);
    expect(moves[moves.length - 1]).toEqual({x: 40, z: 0});
  });

  it('builds nothing while the camera is being panned', () => {
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    gesturing = true;
    fire(PointerEventTypes.POINTERUP);

    expect(places.length).toBe(0);
    // The pan ended over a different spot than it started; the building must not have followed.
    expect(moves.some(move => move.x === 40)).toBeFalse();
  });

  it('carries the building with the finger that grabbed it', () => {
    // Down on the ghost, which stands where the view opened - the middle of the canvas.
    pointAt(CENTRE_X, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    pointAt(CENTRE_X + 100, CENTRE_Y);
    fire(PointerEventTypes.POINTERMOVE);

    expect(moves[moves.length - 1]).toEqual({x: 20, z: 0});
    // Carrying is not building - that is what the deploy button is for.
    expect(places.length).toBe(0);
  });

  it('holds the grip taken on the edge of the building', () => {
    // Grabbed 5 units right of the centre, then dragged 20 units further right: the building ends
    // 20 units along, not with its centre under the finger.
    pointAt(CENTRE_X + 25, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    pointAt(CENTRE_X + 125, CENTRE_Y);
    fire(PointerEventTypes.POINTERMOVE);

    expect(moves[moves.length - 1]).toEqual({x: 20, z: 0});
  });

  it('leaves the building alone when the finger went down away from it', () => {
    pointAt(CENTRE_X + 300, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    const movesBefore = moves.length;
    pointAt(CENTRE_X + 350, CENTRE_Y);
    fire(PointerEventTypes.POINTERMOVE);

    // That finger belongs to the camera; the ghost stays in the world while the ground slides.
    expect(moves.length).toBe(movesBefore);
  });

  it('claims the finger on the building so the camera does not follow it too', () => {
    expect(panClaim).not.toBeNull();
    expect(panClaim!(CENTRE_X, CENTRE_Y)).toBeTrue();
    expect(panClaim!(CENTRE_X + 300, CENTRE_Y)).toBeFalse();
  });

  it('drops the claim when the placer goes away', () => {
    presenter.deactivate();

    expect(panClaim).toBeNull();
  });

  it('stops carrying the building when a second finger starts a pinch', () => {
    pointAt(CENTRE_X, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN, 1);
    fire(PointerEventTypes.POINTERDOWN, 2);
    const movesBefore = moves.length;
    pointAt(CENTRE_X + 100, CENTRE_Y);
    fire(PointerEventTypes.POINTERMOVE, 1);

    expect(moves.length).toBe(movesBefore);
  });

  function placerUiTexture(): AdvancedDynamicTexture {
    const uiTexture = scene.textures.find(texture => texture.name === 'Base item placer') as AdvancedDynamicTexture;
    expect(uiTexture).withContext('placer ui texture').toBeDefined();
    return uiTexture;
  }

  /** The button the touch player presses, reached the way the player reaches it. */
  function pressDeploy() {
    const deployButton = placerUiTexture().getControlByName('Base Item Placer Deploy');
    expect(deployButton).withContext('deploy button').not.toBeNull();
    deployButton!.onPointerClickObservable.notifyObservers({} as any);
  }

  it('builds where the ghost stands when the deploy button is pressed', () => {
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);

    pressDeploy();

    expect(places).toEqual([{x: 40, z: 0}]);
  });

  it('refuses the deploy button on a red spot and reports the attempt', () => {
    positionValid = false;
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);

    pressDeploy();

    expect(places.length).toBe(0);
    expect(invalidAttempts).toBe(1);
    expect(reportedInteractions).toContain('PLACER_REJECTED');
    expect(reportedInteractions).not.toContain('PLACER_CONFIRMED');
  });

  /**
   * The reason travels with the rejection. Six conditions can redden the ghost and they want
   * different repairs - the opening search clears "blocked by another item" and can do nothing
   * about "outside the allowed area" - so a bare count of rejections cannot say what to build.
   * It used to be logged and nowhere else, in a record that carries no game session.
   */
  it('reports why the spot was refused, not only that it was', () => {
    positionValid = false;
    errorText = 'Blocked by another item';
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);

    pressDeploy();

    expect(reportedDetails).toContain('PLACER_REJECTED|Blocked by another item');
  });

  it('says unknown rather than nothing when the placer has no reason to give', () => {
    // getErrorText is empty whenever the check itself failed - a WASM trap sets "Can not check
    // this position", but a placer that never ran a check at all returns the empty string. An
    // empty detail would be dropped and the row would look like the old one.
    positionValid = false;
    errorText = '';
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);

    pressDeploy();

    expect(reportedDetails).toContain('PLACER_REJECTED|unknown');
  });

  /**
   * Placing the starting base is the first thing the game asks of anybody, and until these three
   * events existed a player who was shown the placer and did nothing was indistinguishable from one
   * who never got one - 63 of 95 such sessions on PROD, all unexplained. Reaching and being refused
   * is a third case again, and each of the three calls for a different repair.
   */
  it('says that the game asked for a base, and what became of the asking', () => {
    expect(reportedInteractions).toContain('PLACER_SHOWN');
    expect(reportedInteractions).not.toContain('PLACER_CONFIRMED');
    expect(reportedInteractions).not.toContain('PLACER_REJECTED');

    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);
    pressDeploy();

    expect(places.length).toBe(1);
    expect(reportedInteractions).toContain('PLACER_CONFIRMED');
    expect(reportedDetails).toContain('PLACER_CONFIRMED|glb=1');
  });

  /**
   * The builder this base spawns is drawn from the one glb. A base set down before that glb has
   * landed spawns a builder with nothing to draw - which is what quest 358 needs to tell apart.
   */
  it('says when the base went down before the models had arrived', () => {
    modelsLoaded = false;

    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);
    pressDeploy();

    expect(reportedDetails).toContain('PLACER_CONFIRMED|glb=0');
  });

  /**
   * The start placer always comes first and the tracker keeps one record per kind, so a factory
   * placer reporting under the same kinds was never on record. A placer that can be cancelled is a
   * building's and reports under its own kinds.
   */
  it('reports a building placer under its own kinds', () => {
    presenter.deactivate();
    reportedInteractions.length = 0;
    presenter.activate({...placer, isCanBeCanceled: () => true} as BaseItemPlacer);
    expect(reportedInteractions).toEqual(['BUILD_PLACER_SHOWN']);

    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);
    pressDeploy();
    presenter.deactivate();

    expect(places.length).toBe(1);
    expect(reportedInteractions).toContain('BUILD_PLACER_CONFIRMED');
    expect(reportedDetails).toContain('BUILD_PLACER_SHOWN|type=11');
    expect(reportedDetails).toContain('BUILD_PLACER_CONFIRMED|type=11');
    expect(reportedInteractions).not.toContain('PLACER_CONFIRMED');
    expect(reportedInteractions).not.toContain('BUILD_PLACER_ABANDONED');
  });

  it('says when a building placer closes without building', () => {
    presenter.deactivate();
    expect(reportedInteractions).not.toContain('BUILD_PLACER_ABANDONED'); // the start placer is not abandoned
    presenter.activate({...placer, isCanBeCanceled: () => true} as BaseItemPlacer);
    presenter.deactivate();
    presenter.deactivate();

    expect(reportedInteractions.filter(kind => kind === 'BUILD_PLACER_ABANDONED').length).toBe(1);
    expect(reportedDetails).toContain('BUILD_PLACER_ABANDONED|type=11');
  });

  /**
   * Quest 386 on a phone: the searched spot lay at the top edge, the hint slid under the quest line
   * and the cancel button under the minimap. A building placer opening on a searched spot brings it
   * to the middle of the picture.
   */
  it('centres the camera on the spot a building placer opens on', () => {
    presenter.deactivate();
    const spot = {getX: () => 12, getY: () => -30} as DecimalPosition;
    presenter.activate({...placer, isCanBeCanceled: () => true, getOpenPosition: () => spot} as BaseItemPlacer);

    expect(flights).toEqual([{x: 12, y: -30}]);
  });

  it('leaves the camera alone for a building placer without a searched spot', () => {
    presenter.deactivate();
    presenter.activate({...placer, isCanBeCanceled: () => true} as BaseItemPlacer);

    expect(flights).toEqual([]);
  });

  /** Quest 386: a refused dockyard must say which building it was, or it is filed under the factory of 358. */
  it('names the building and the reason when a building placer refuses', () => {
    presenter.deactivate();
    presenter.activate({...placer, isCanBeCanceled: () => true} as BaseItemPlacer);
    positionValid = false;
    errorText = 'Build it on the water';
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);
    pressDeploy();

    expect(reportedDetails).toContain('BUILD_PLACER_REJECTED|type=11 reason=Build it on the water');
  });

  /** A phone has no Escape key: without this, a building placer opened by mistake could only be built with. */
  it('gives a building placer a button to close it on touch', () => {
    let cancels = 0;
    presenter.deactivate();
    presenter.activate({...placer, isCanBeCanceled: () => true, cancel: () => cancels++} as BaseItemPlacer);
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);

    const cancelButton = placerUiTexture().getControlByName('Base Item Placer Cancel');
    expect(cancelButton).withContext('cancel button').not.toBeNull();
    cancelButton!.onPointerClickObservable.notifyObservers({} as any);

    expect(cancels).toBe(1);
    expect(places.length).toBe(0);
  });

  it('gives the start placer no button to close it, and the mouse none at all', () => {
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);
    expect(placerUiTexture().getControlByName('Base Item Placer Cancel')).toBeNull();

    presenter.deactivate();
    presenter.activate({...placer, isCanBeCanceled: () => true} as BaseItemPlacer);
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERMOVE, 1, 'mouse');
    expect(placerUiTexture().getControlByName('Base Item Placer Cancel')).toBeNull();
  });

  it('closes a building placer through the open-placer hook, and not after it has built', () => {
    let cancels = 0;
    presenter.deactivate();
    presenter.activate({...placer, isCanBeCanceled: () => true, cancel: () => cancels++} as BaseItemPlacer);
    expect(cancelOpenPlacer()).toBeTrue();
    expect(cancels).toBe(1);

    presenter.activate({...placer, isCanBeCanceled: () => true, cancel: () => cancels++} as BaseItemPlacer);
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);
    pressDeploy();
    expect(cancelOpenPlacer()).toBeFalse();
    expect(cancels).toBe(1);
  });

  /**
   * Green is a snapshot of the last drag. The engine checks again inside onPlace(), and if that
   * check refuses, nothing is built and nothing is said - while the bubble is already closed and
   * the placement already reported as confirmed. Reported from a phone in the Meta in-app browser
   * on 2026-09-16.
   */
  it('re-checks the exact deploy position before committing anything', () => {
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);
    const movesBeforeDeploy = moves.length;

    pressDeploy();

    // The last thing checked is the spot being built on, not wherever the drag happened to end.
    expect(moves.length).toBe(movesBeforeDeploy + 1);
    expect(moves[moves.length - 1]).toEqual(places[places.length - 1]);
  });

  it('commits nothing when the fresh check refuses the deploy position', () => {
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);
    // The world moved between the drag and the tap: an enemy wandered in, a resource streamed.
    positionValid = false;

    pressDeploy();

    expect(places.length).toBe(0);
    expect(invalidAttempts).toBe(1);
    expect(reportedInteractions).toContain('PLACER_REJECTED');
    expect(reportedInteractions).not.toContain('PLACER_CONFIRMED');
  });

  it('hangs the hint below the building when it is dragged to the top edge', () => {
    // With this camera the screen points along +z, so a spot further along z sits higher up.
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);
    const hint = placerUiTexture().rootContainer.children[0];
    expect(hint.linkOffsetYInPixels).toBeLessThan(0);

    pointAt(CENTRE_X, 60);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);

    // Above the building there is no room left, and the deploy button rides in that bubble.
    expect(hint.linkOffsetYInPixels).toBeGreaterThan(0);
  });

  /**
   * Puts the placer into touch mode and lets the GUI measure itself - the hint bubble has no
   * position on screen until it has been drawn once.
   */
  function layOutHint() {
    pointAt(CENTRE_X, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN);
    fire(PointerEventTypes.POINTERUP);
    scene.render();
  }

  it('counts the hint bubble as a grip on the building', () => {
    layOutHint();

    // Both points are the same distance from the building and outside its own grab radius; the one
    // that grips is the one the bubble hangs over. The bubble is the biggest thing the placer puts
    // on a phone screen, so a thumb reaching in lands there first.
    expect(panClaim!(CENTRE_X - 130, CENTRE_Y - 160)).toBeTrue();
    expect(panClaim!(CENTRE_X - 130, CENTRE_Y + 160)).toBeFalse();
  });

  it('leaves the deploy button out of the grip', () => {
    layOutHint();

    // It sits at the bottom of the bubble and is the one thing in there that is pressed, not
    // dragged - a grip on it would turn every deploy into a drag of a few pixels.
    expect(panClaim!(CENTRE_X, CENTRE_Y - 96)).toBeFalse();
  });

  it('carries the building when the drag starts on the hint bubble', () => {
    layOutHint();

    // Down on the bubble's text, 130px left and 160px above the building it belongs to...
    pointAt(CENTRE_X - 130, CENTRE_Y - 160);
    fire(PointerEventTypes.POINTERDOWN);
    pointAt(CENTRE_X - 30, CENTRE_Y - 160);
    fire(PointerEventTypes.POINTERMOVE);

    // ...and 100px of finger move the building 100px, which is 20 units. The grip is held in screen
    // pixels: hold it in world units instead and the building - and with it the bubble under the
    // finger - drifts away as soon as the two are far apart on screen.
    expect(moves[moves.length - 1]).toEqual({x: 20, z: 0});
    expect(places.length).toBe(0);
  });

  it('carries the building even while the finger is over no ground at all', () => {
    layOutHint();
    // Sky above the middle of the canvas: the bubble hangs in it, the building stands below it.
    horizonY = CENTRE_Y;

    pointAt(CENTRE_X - 130, CENTRE_Y - 160);
    fire(PointerEventTypes.POINTERDOWN);
    pointAt(CENTRE_X - 30, CENTRE_Y - 160);
    fire(PointerEventTypes.POINTERMOVE);

    // What is picked is the ground where the building is going, not the ground under the finger -
    // so a bubble held over the horizon still carries the building.
    expect(moves[moves.length - 1]).toEqual({x: 20, z: 0});
  });

  it('leaves the building where it is when the drag would take it off the map', () => {
    layOutHint();
    pointAt(CENTRE_X - 130, CENTRE_Y - 160);
    fire(PointerEventTypes.POINTERDOWN);
    const movesBefore = moves.length;

    // Now there is no ground where the building would land.
    horizonY = HEIGHT;
    pointAt(CENTRE_X - 30, CENTRE_Y - 160);
    fire(PointerEventTypes.POINTERMOVE);

    expect(moves.length).toBe(movesBefore);
  });

  it('still builds on a mouse press, where nothing has to be confirmed', () => {
    pointAt(CENTRE_X + 200, CENTRE_Y);
    fire(PointerEventTypes.POINTERDOWN, 1, 'mouse');

    expect(places).toEqual([{x: 40, z: 0}]);
  });

  /**
   * Where the placer opens. It used to take the middle of the screen and colour the ghost red if
   * the game would not build there, which on PROD was 44% of first clicks.
   */
  describe('the spot it opens on', () => {
    /** Makes validity depend on where the ghost stands, which the flat mock cannot express. */
    function blockWithin(radius: number) {
      let at = {x: 0, z: 0};
      (placer as any).onMove = (x: number, z: number) => {
        at = {x, z};
        moves.push({x, z});
      };
      (placer as any).isPositionValid = () => Math.hypot(at.x, at.z) > radius;
    }

    it('moves the ghost off an occupied spot to one the game accepts', () => {
      blockWithin(15);
      moves.length = 0;
      presenter.activate(placer);

      const settled = moves[moves.length - 1];
      expect(Math.hypot(settled.x, settled.z)).toBeGreaterThan(15);
    });

    /**
     * The ghost has to end up standing where the last check was made. Probing leaves the placer
     * on whichever spot it tried last, and its error text with it - so a search that gave up
     * without putting the ghost back would leave the bubble naming a reason for a position the
     * player is not looking at.
     */
    it('puts the ghost back where it opened when nothing near is free', () => {
      blockWithin(Number.POSITIVE_INFINITY);
      moves.length = 0;
      presenter.activate(placer);

      expect(moves[moves.length - 1]).toEqual({x: 0, z: 0});
    });

    it('leaves a spot alone when the game already accepts it', () => {
      positionValid = true;
      moves.length = 0;
      presenter.activate(placer);

      // One move, not thirty-three: no ring is searched when there is nothing to search for.
      expect(moves).toEqual([{x: 0, z: 0}]);
    });
  });
});
