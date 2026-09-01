import {Vector3} from '@babylonjs/core';
import {BaseItemPlacerPresenterImpl} from './base-item-placer-presenter.impl';
import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';

/**
 * Where the base placer stands before the terrain has caught up.
 *
 * The placer used to get a position only from a terrain ray pick, and that pick needs the tile under
 * the screen centre to exist. The game starts before the tiles are built, so on a phone there is a
 * window of seconds in which it does not - and in that window nothing positioned the placer at all.
 * The disc keeps x=0, z=0, the hint bubble is linked to the disc, and the whole thing sits in the
 * corner of the map while the retry ticks away once a second in silence.
 *
 * That is what happened in the Meta in-app browser on 2026-08-30: three sessions reported
 * PLACER_SHOWN, sat there for 17 to 50 seconds and left without a single POINTER_DOWN. The deploy
 * dialog was never on screen to be tapped.
 *
 * Reaching into the prototype rather than constructing: a real placer builds meshes, materials and a
 * fullscreen GUI texture, and none of that is what these questions are about.
 */
describe('Base item placer position', () => {
  describe('the camera-computed ground point', () => {
    function serviceAt(centre: Vector3, terrainHeight: number | null) {
      const service: any = Object.create(BabylonRenderServiceAccessImpl.prototype);
      service.setupCenterGroundPosition = () => centre;
      service.getTerrainHeightAt = () => terrainHeight;
      return service;
    }

    it('stands on the terrain height, not on zero level', () => {
      const point = serviceAt(new Vector3(120, 0, -40), 7.5).setupCenterTerrainPosition();

      expect(point.x).toBe(120);
      expect(point.y).toBe(7.5);
      expect(point.z).toBe(-40);
    });

    it('falls back to zero level where the height map says nothing', () => {
      expect(serviceAt(new Vector3(1, 0, 2), null).setupCenterTerrainPosition().y).toBe(0);
    });

    it('gives up only when the camera yields no ground point at all', () => {
      expect(serviceAt(new Vector3(NaN, NaN, NaN), 0).setupCenterTerrainPosition()).toBeNull();
    });
  });

  describe('waiting for the terrain', () => {
    let placer: any;
    let positions: Vector3[];
    let reported: string[];
    let pick: () => Vector3 | null;

    beforeEach(() => {
      jasmine.clock().install();
      // Without this Date.now() would ignore tick(), and the three-second test would prove nothing.
      jasmine.clock().mockDate();
      positions = [];
      reported = [];
      pick = () => null;
      placer = Object.create(BaseItemPlacerPresenterImpl.prototype);
      placer.activationGeneration = 1;
      placer.movedByPlayer = false;
      placer.noTerrainReported = false;
      placer.setupPickedPoint = () => pick();
      placer.setPosition = (_: unknown, point: Vector3) => positions.push(point);
      placer.rendererService = {reportFirstInteraction: (kind: string) => reported.push(kind)};
    });

    afterEach(() => jasmine.clock().uninstall());

    function waitForTerrain() {
      placer.setupPickedPointDelayed({}, 1, Date.now());
    }

    it('moves the ghost onto the terrain as soon as it exists', () => {
      waitForTerrain();
      jasmine.clock().tick(1000);
      expect(positions.length).toBe(0);

      pick = () => new Vector3(10, 2, 20);
      jasmine.clock().tick(1000);

      expect(positions.length).toBe(1);
      expect(positions[0].x).toBe(10);
    });

    it('stops retrying once it has succeeded', () => {
      pick = () => new Vector3(10, 2, 20);
      waitForTerrain();
      jasmine.clock().tick(10000);

      expect(positions.length).toBe(1);
    });

    it('leaves a ghost the player has dragged where they put it', () => {
      waitForTerrain();
      jasmine.clock().tick(1000);
      placer.movedByPlayer = true;
      pick = () => new Vector3(10, 2, 20);

      jasmine.clock().tick(1000);

      // The correction is worth nothing next to a position the player chose on purpose.
      expect(positions.length).toBe(0);
    });

    it('reports the wait once it has lasted three seconds', () => {
      waitForTerrain();

      jasmine.clock().tick(2000);
      expect(reported).toEqual([]);

      jasmine.clock().tick(2000);
      expect(reported).toEqual(['PLACER_NO_TERRAIN']);

      // The wait has no upper bound; the report must not turn into a per-second drip.
      jasmine.clock().tick(60000);
      expect(reported).toEqual(['PLACER_NO_TERRAIN']);
    });

    it('says nothing when the terrain arrives in time', () => {
      pick = () => new Vector3(10, 2, 20);
      waitForTerrain();
      jasmine.clock().tick(10000);

      expect(reported).toEqual([]);
    });

    it('drops the retry when the placer has been reopened', () => {
      waitForTerrain();
      placer.activationGeneration = 2;
      pick = () => new Vector3(10, 2, 20);

      jasmine.clock().tick(10000);

      expect(positions.length).toBe(0);
      expect(reported).toEqual([]);
    });
  });
});
