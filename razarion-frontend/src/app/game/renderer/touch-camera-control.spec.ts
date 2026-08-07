import {TouchCameraControl} from './touch-camera-control';
import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';

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
  let zooms: number[];
  let terrainDistance: number;

  beforeEach(() => {
    pans = [];
    zooms = [];
    terrainDistance = 30;

    canvas = document.createElement('canvas');
    // The control reads the laid-out size; a detached canvas reports zeros without this.
    canvas.getBoundingClientRect = () => ({
      left: 0, top: 0, right: WIDTH, bottom: HEIGHT, width: WIDTH, height: HEIGHT, x: 0, y: 0,
      toJSON: () => ({})
    } as DOMRect);

    const renderService = {
      panByNdc: (fromX: number, fromY: number, toX: number, toY: number) => pans.push({fromX, fromY, toX, toY}),
      getCameraTerrainDistance: () => terrainDistance,
      setCameraTerrainDistance: (distance: number) => {
        terrainDistance = distance;
        zooms.push(distance);
      }
    } as unknown as BabylonRenderServiceAccessImpl;

    control = new TouchCameraControl(canvas, renderService);
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
    expect(zooms[0]).toBeCloseTo(15, 5);
    expect(control.isGesturing()).toBeTrue();
  });

  it('zooms out when two fingers close', () => {
    fire('pointerdown', 200, 300, 1);
    fire('pointerdown', 400, 300, 2);   // gap 200
    fire('pointermove', 300, 300, 2);   // gap 100

    expect(zooms[0]).toBeCloseTo(60, 5);
  });

  it('does not pan while two fingers are pinching', () => {
    fire('pointerdown', 300, 300, 1);
    fire('pointerdown', 400, 300, 2);
    fire('pointermove', 500, 300, 2);

    expect(pans.length).toBe(0);
  });

  it('does not jump when one finger of a pinch lifts', () => {
    fire('pointerdown', 300, 300, 1);
    fire('pointerdown', 400, 300, 2);
    fire('pointermove', 600, 300, 2);
    fire('pointerup', 600, 300, 2);
    // The remaining finger keeps moving; it must pan from where it is, not from where it started.
    fire('pointermove', 320, 300, 1);

    expect(pans.length).toBe(1);
    expect(pans[0].fromX).toBeCloseTo(-0.25, 5);  // x=300, where the finger actually was
    expect(pans[0].toX).toBeCloseTo(-0.2, 5);     // x=320
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
