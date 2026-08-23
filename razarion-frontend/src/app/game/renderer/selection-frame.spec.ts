import {NullEngine, PointerEventTypes, Scene} from '@babylonjs/core';
import {SelectionFrame} from './selection-frame';
import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';
import {ActionService} from '../action.service';
import {TouchSelectionModeService} from './touch-selection-mode.service';

/**
 * The box on a phone. Everything here is about one question the desktop never has to ask: whether
 * this finger is drawing a box or moving the camera. Get it wrong in one direction and the map
 * stops scrolling; wrong in the other and every pan selects whatever it swept across.
 */
describe('SelectionFrame on touch', () => {
  let engine: NullEngine;
  let scene: Scene;
  let frame: SelectionFrame;
  let rectangles: { xStart: number, yStart: number, xEnd: number, yEnd: number }[];
  let armed: boolean;
  let placerActive: boolean;

  beforeEach(() => {
    rectangles = [];
    armed = false;
    placerActive = false;

    engine = new NullEngine();
    scene = new Scene(engine);

    const renderService = {
      get baseItemPlacerActive() {
        return placerActive;
      }
    } as unknown as BabylonRenderServiceAccessImpl;

    const actionService = {
      selectScreenRectangle: (xStart: number, yStart: number, xEnd: number, yEnd: number) =>
        rectangles.push({xStart, yStart, xEnd, yEnd})
    } as unknown as ActionService;

    const touchSelectionMode = {
      armed: () => armed,
      disarm: () => armed = false
    } as unknown as TouchSelectionModeService;

    frame = new SelectionFrame(scene, renderService, actionService, touchSelectionMode);
  });

  afterEach(() => {
    frame.disable();
    scene.dispose();
    engine.dispose();
  });

  /** isPrimary defaults to true: the browser sets it on every finger that is alone on the screen. */
  function fire(type: number, x: number, y: number,
                pointerId = 1, pointerType = 'touch', isPrimary = true) {
    scene.pointerX = x;
    scene.pointerY = y;
    scene.onPointerObservable.notifyObservers({
      type,
      event: {pointerId, pointerType, isPrimary} as PointerEvent
    } as any);
  }

  function drag(x0: number, y0: number, x1: number, y1: number, pointerId = 1, pointerType = 'touch') {
    fire(PointerEventTypes.POINTERDOWN, x0, y0, pointerId, pointerType);
    fire(PointerEventTypes.POINTERMOVE, x1, y1, pointerId, pointerType);
    fire(PointerEventTypes.POINTERUP, x1, y1, pointerId, pointerType);
  }

  it('draws no box while the mode is off - that drag belongs to the camera', () => {
    drag(100, 100, 300, 250);

    expect(rectangles.length).toBe(0);
  });

  it('selects the dragged rectangle while the mode is armed', () => {
    armed = true;
    drag(300, 250, 100, 100);

    expect(rectangles.length).toBe(1);
    // Normalized, whichever corner the finger started from.
    expect(rectangles[0]).toEqual({xStart: 100, yStart: 100, xEnd: 300, yEnd: 250});
  });

  it('spends the mode on the box it drew', () => {
    armed = true;
    drag(100, 100, 300, 250);

    // A mode the player cannot get stuck in: the camera answers the next finger again.
    expect(armed).toBeFalse();
  });

  it('keeps the mode when the finger only tapped', () => {
    armed = true;
    drag(100, 100, 102, 101);

    expect(rectangles.length).toBe(0);
    // Nothing was selected, so nothing was spent - otherwise a stray touch costs the player the
    // mode they just armed.
    expect(armed).toBeTrue();
  });

  it('abandons the box when a second finger joins, and keeps the mode', () => {
    armed = true;
    fire(PointerEventTypes.POINTERDOWN, 100, 100, 1);
    fire(PointerEventTypes.POINTERDOWN, 300, 100, 2, 'touch', false);   // pinch
    fire(PointerEventTypes.POINTERMOVE, 400, 300, 1);
    fire(PointerEventTypes.POINTERUP, 400, 300, 1);
    fire(PointerEventTypes.POINTERUP, 300, 100, 2, 'touch', false);

    expect(rectangles.length).toBe(0);
    expect(armed).toBeTrue();
  });

  it('starts a box again after a touch whose release was never delivered', () => {
    armed = true;
    fire(PointerEventTypes.POINTERDOWN, 100, 100, 1);
    fire(PointerEventTypes.POINTERMOVE, 200, 200, 1);
    // ... and the finger is cancelled rather than lifted: no POINTERUP ever arrives.

    drag(400, 400, 600, 550, 2);

    expect(rectangles.length).toBe(1);
    expect(rectangles[0]).toEqual({xStart: 400, yStart: 400, xEnd: 600, yEnd: 550});
  });

  it('draws no box over the item placer', () => {
    armed = true;
    placerActive = true;
    drag(100, 100, 300, 250);

    expect(rectangles.length).toBe(0);
  });

  it('leaves the mouse marquee alone', () => {
    drag(100, 100, 300, 250, 1, 'mouse');

    // The desktop never needs the mode: the left button is a second pointer of its own.
    expect(rectangles.length).toBe(1);
  });
});
