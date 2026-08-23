import {TouchCameraControl} from './touch-camera-control';
import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';
import {TouchSelectionModeService} from './touch-selection-mode.service';

/**
 * The gesture machine decides three things that are easy to get wrong and impossible to notice on a
 * desktop: that a tap stays a tap (or the item placer builds on every touch), that a pan is still
 * remembered as a pan when the finger lifts (or the placer builds where the pan ended), and that a
 * mouse is left alone (or the desktop marquee dies).
 */
describe('TouchCameraControl', () => {
  const WIDTH = 800;
  const HEIGHT = 600;

  let canvas: HTMLCanvasElement;
  let control: TouchCameraControl;
  let pans: { fromX: number, fromY: number, toX: number, toY: number }[];
  let zooms: { distance: number, ndcX: number, ndcY: number }[];
  let firstInteractions: string[];
  let terrainDistance: number;
  let armed: boolean;
  let placerActive: boolean;

  beforeEach(() => {
    pans = [];
    zooms = [];
    firstInteractions = [];
    terrainDistance = 30;
    armed = false;
    placerActive = false;

    canvas = document.createElement('canvas');
    // The control reads the laid-out size; a detached canvas reports zeros without this.
    canvas.getBoundingClientRect = () => ({
      left: 0, top: 0, right: WIDTH, bottom: HEIGHT, width: WIDTH, height: HEIGHT, x: 0, y: 0,
      toJSON: () => ({})
    } as DOMRect);

    const renderService = {
      panByNdc: (fromX: number, fromY: number, toX: number, toY: number) => pans.push({fromX, fromY, toX, toY}),
      getCameraTerrainDistance: () => terrainDistance,
      setCameraTerrainDistanceAt: (distance: number, ndcX: number, ndcY: number) => {
        terrainDistance = distance;
        zooms.push({distance, ndcX, ndcY});
      },
      reportFirstInteraction: (kind: string) => firstInteractions.push(kind),
      get baseItemPlacerActive() {
        return placerActive;
      }
    } as unknown as BabylonRenderServiceAccessImpl;

    const touchSelectionMode = {
      armed: () => armed
    } as unknown as TouchSelectionModeService;

    control = new TouchCameraControl(canvas, renderService, touchSelectionMode);
  });

  afterEach(() => control.disable());

  function fire(type: string, x: number, y: number, pointerId = 1, pointerType = 'touch') {
    canvas.dispatchEvent(new PointerEvent(type, {pointerId, pointerType, clientX: x, clientY: y, bubbles: true}));
  }

  it('leaves a tap alone', () => {
    fire('pointerdown', 400, 300);
    // Two pixels of wobble while the finger lifts - still a tap.
    fire('pointermove', 402, 301);
    fire('pointerup', 402, 301);

    expect(pans.length).toBe(0);
    expect(control.isGesturing()).toBeFalse();
  });

  it('pans once the finger passes the threshold', () => {
    fire('pointerdown', 400, 300);
    fire('pointermove', 403, 300);
    expect(pans.length).toBe(0);

    fire('pointermove', 440, 300);
    expect(pans.length).toBe(1);
    expect(control.isGesturing()).toBeTrue();
  });

  it('reports normalized device coordinates, x right and y up', () => {
    fire('pointerdown', 400, 300);
    fire('pointermove', 600, 150);

    expect(pans.length).toBe(1);
    // Centre of the canvas is the origin; the move ends a quarter right and a quarter up.
    expect(pans[0].fromX).toBeCloseTo(0, 5);
    expect(pans[0].fromY).toBeCloseTo(0, 5);
    expect(pans[0].toX).toBeCloseTo(0.5, 5);
    expect(pans[0].toY).toBeCloseTo(0.5, 5);
  });

  it('still reports the gesture when the finger has lifted', () => {
    fire('pointerdown', 400, 300);
    fire('pointermove', 500, 300);
    fire('pointerup', 500, 300);

    // The placer asks after the release, and Babylon may ask before this class has seen the release
    // at all - either way the answer has to stay "that was a pan".
    expect(control.isGesturing()).toBeTrue();
  });

  it('forgets the previous gesture when the next finger goes down', () => {
    fire('pointerdown', 400, 300);
    fire('pointermove', 500, 300);
    fire('pointerup', 500, 300);
    expect(control.isGesturing()).toBeTrue();

    fire('pointerdown', 400, 300);
    expect(control.isGesturing()).toBeFalse();
  });

  it('zooms in when two fingers move apart', () => {
    fire('pointerdown', 300, 300, 1);
    fire('pointerdown', 400, 300, 2);   // gap 100
    fire('pointermove', 500, 300, 2);   // gap 200

    expect(zooms.length).toBe(1);
    // Twice the gap halves the distance to the ground: closer in.
    expect(zooms[0].distance).toBeCloseTo(15, 5);
    expect(control.isGesturing()).toBeTrue();
  });

  it('zooms out when two fingers close', () => {
    fire('pointerdown', 200, 300, 1);
    fire('pointerdown', 400, 300, 2);   // gap 200
    fire('pointermove', 300, 300, 2);   // gap 100

    expect(zooms[0].distance).toBeCloseTo(60, 5);
  });

  it('zooms about the point between the fingers, not about the screen centre', () => {
    // Both fingers in the left half; whatever is between them must stay put, and that is not (0,0).
    fire('pointerdown', 100, 150, 1);
    fire('pointerdown', 200, 150, 2);
    fire('pointermove', 300, 150, 2);   // centre now x=200, y=150

    expect(zooms.length).toBe(1);
    expect(zooms[0].ndcX).toBeCloseTo(-0.5, 5);
    expect(zooms[0].ndcY).toBeCloseTo(0.5, 5);
  });

  it('pans by the point between the fingers while pinching', () => {
    fire('pointerdown', 300, 300, 1);
    fire('pointerdown', 400, 300, 2);   // centre x=350
    fire('pointermove', 500, 300, 2);   // centre x=400

    // Two fingers that spread also travel; the ground they hold has to travel with them.
    expect(pans.length).toBe(1);
    expect(pans[0].fromX).toBeCloseTo(-0.125, 5);  // x=350
    expect(pans[0].toX).toBeCloseTo(0, 5);         // x=400
  });

  it('does not pan while two fingers spread symmetrically', () => {
    fire('pointerdown', 300, 300, 1);
    fire('pointerdown', 500, 300, 2);   // centre x=400
    fire('pointermove', 200, 300, 1);   // centre x=350
    fire('pointermove', 600, 300, 2);   // centre x=400 again

    // The centre wandered and came back; a pure spread about a fixed point ends where it started.
    expect(pans.length).toBe(2);
    expect(pans[1].toX).toBeCloseTo(pans[0].fromX, 5);
  });

  it('does not jump when one finger of a pinch lifts', () => {
    fire('pointerdown', 300, 300, 1);
    fire('pointerdown', 400, 300, 2);
    fire('pointermove', 600, 300, 2);
    fire('pointerup', 600, 300, 2);
    // The remaining finger keeps moving; it must pan from where it is, not from where it started.
    fire('pointermove', 320, 300, 1);

    const last = pans[pans.length - 1];
    expect(last.fromX).toBeCloseTo(-0.25, 5);  // x=300, where the finger actually was
    expect(last.toX).toBeCloseTo(-0.2, 5);     // x=320
  });

  it('leaves the camera alone while a claimed finger drags', () => {
    // The item placer claims the area around the building it is placing.
    control.setPanClaim((x, y) => Math.hypot(x - 400, y - 300) < 60);

    fire('pointerdown', 410, 300);
    fire('pointermove', 600, 300);
    fire('pointerup', 600, 300);

    expect(pans.length).toBe(0);
    // Nothing was gestured either - the placer decides itself what that drag meant.
    expect(control.isGesturing()).toBeFalse();
  });

  it('still pans when the finger goes down outside the claim', () => {
    control.setPanClaim((x, y) => Math.hypot(x - 400, y - 300) < 60);

    fire('pointerdown', 100, 300);
    fire('pointermove', 300, 300);

    expect(pans.length).toBe(1);
  });

  it('judges the claim where the finger went down, not where it is now', () => {
    control.setPanClaim((x, y) => Math.hypot(x - 400, y - 300) < 60);

    // Starts outside the claim and is dragged straight through it: still a pan all the way.
    fire('pointerdown', 100, 300);
    fire('pointermove', 400, 300);
    fire('pointermove', 700, 300);

    expect(pans.length).toBe(2);
  });

  it('frees the finger again when the claim is dropped', () => {
    control.setPanClaim(() => true);
    fire('pointerdown', 400, 300);
    fire('pointermove', 600, 300);
    expect(pans.length).toBe(0);

    control.setPanClaim(null);
    fire('pointermove', 700, 300);

    expect(pans.length).toBe(1);
  });

  it('leaves the camera alone while the selection box is armed', () => {
    armed = true;

    fire('pointerdown', 400, 300);
    fire('pointermove', 600, 300);

    // The drag is drawing a box (SelectionFrame); moving the ground under it would drag the units
    // out of the box being drawn.
    expect(pans.length).toBe(0);
  });

  it('counts an armed touch as a gesture from the moment it goes down', () => {
    armed = true;
    fire('pointerdown', 400, 300);

    // Everything that turns a touch into a command asks this on the release. A box that was armed
    // and then barely moved is still not the tap that sends the units marching.
    expect(control.isGesturing()).toBeTrue();
  });

  it('pans again once the box has been drawn and the mode is spent', () => {
    armed = true;
    fire('pointerdown', 400, 300);
    fire('pointermove', 600, 300);
    fire('pointerup', 600, 300);
    armed = false;   // SelectionFrame disarms on the release

    fire('pointerdown', 400, 300);
    fire('pointermove', 600, 300);

    expect(pans.length).toBe(1);
  });

  it('still pinches while the selection box is armed', () => {
    armed = true;
    fire('pointerdown', 300, 300, 1);
    fire('pointerdown', 400, 300, 2);
    fire('pointermove', 500, 300, 2);

    // Two fingers are never a box; the camera must stay reachable while the mode is on.
    expect(zooms.length).toBe(1);
  });

  it('pans while the placer is up, armed or not', () => {
    armed = true;
    placerActive = true;

    fire('pointerdown', 100, 300);
    fire('pointermove', 300, 300);

    // No box is drawn under the placer, so suppressing the pan would only stop the camera dead.
    expect(pans.length).toBe(1);
  });

  it('ignores the mouse so the desktop marquee keeps working', () => {
    fire('pointerdown', 400, 300, 1, 'mouse');
    fire('pointermove', 600, 300, 1, 'mouse');
    fire('pointerup', 600, 300, 1, 'mouse');

    expect(pans.length).toBe(0);
    expect(control.isGesturing()).toBeFalse();
  });

  it('drops its listeners when disabled', () => {
    control.disable();
    fire('pointerdown', 400, 300);
    fire('pointermove', 600, 300);

    expect(pans.length).toBe(0);
  });
});
