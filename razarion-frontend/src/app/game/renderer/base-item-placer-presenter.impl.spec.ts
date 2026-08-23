import {FreeCamera, NullEngine, PickingInfo, PointerEventTypes, PointerInfo, Scene, Vector3} from '@babylonjs/core';
import {BaseItemPlacerPresenterImpl} from './base-item-placer-presenter.impl';
import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';
import {BabylonModelService} from './babylon-model.service';
import {BabylonAudioService} from './babylon-audio.service';
import {BaseItemPlacer} from '../../gwtangular/GwtAngularFacade';
import {AdvancedDynamicTexture} from '@babylonjs/gui';

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
    const modelService = {cloneModel3D: () => renderObject} as unknown as BabylonModelService;
    const audioService = {speakCommand: () => {
    }} as unknown as BabylonAudioService;

    placer = {
      getModel3DId: () => 1,
      getRelativeItemPositions: () => [],
      getSpawnAudioId: () => null,
      isPositionValid: () => positionValid,
      getErrorText: () => '',
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
});
