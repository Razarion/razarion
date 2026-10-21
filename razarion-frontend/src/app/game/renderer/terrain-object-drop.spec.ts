import {NullEngine} from '@babylonjs/core/Engines/nullEngine';
import {Scene} from '@babylonjs/core/scene';
import {TransformNode} from '@babylonjs/core/Meshes/transformNode';
import {BabylonTerrainTileImpl} from './babylon-terrain-tile.impl';

/**
 * A tile kept hidden in the terrain cache disposes its rocks and plants after a while and plants them
 * again when shown. Parking alone left 6 000-17 000 instances in long PROD sessions (2026-10-21).
 *
 * Built on Object.create like terrain-build-queue.spec: a real tile needs the worker, the terrain
 * helper and the models. The nodes and the container are real, so dispose() is Babylon's own.
 */
describe('Terrain object drop', () => {
  const impl = BabylonTerrainTileImpl as any;
  const DELAY = impl.TERRAIN_OBJECT_DROP_DELAY;
  let scene: Scene;
  let editor: boolean;

  function placements(count: number): any[] {
    return Array.from({length: count}, (_, i) => ({config: {getModel3DId: () => 1}, model: {terrainObjectId: i}}));
  }

  function newTile(): any {
    const tile = Object.create(BabylonTerrainTileImpl.prototype);
    Object.assign(tile, {
      container: new TransformNode('container', scene),
      groundMesh: new TransformNode('ground', scene),
      active: true,
      disposed: false,
      spriteManagers: [],
      pendingModelRequests: new Set(),
      terrainObjectPlacements: [],
      terrainObjectNodes: [],
      terrainObjectGeneration: 0,
      terrainObjectsDropped: false,
      terrainObjectDropTimer: null,
      babylonModelService: {isModel3DReady: () => true},
      rendererService: {
        getScene: () => scene,
        directionalLight: {includedOnlyMeshes: []},
        needsGroundMeshAtOnce: () => editor,
        addTerrainTileToScene: () => undefined,
        removeTerrainTileFromScene: () => undefined
      },
      gwtAngularService: {gwtAngularFacade: {terrainTypeService: {getTerrainObjectConfig: () => ({getModel3DId: () => 1})}}}
    });
    return tile;
  }

  function plant(tile: any, count: number): void {
    tile.terrainObjectPlacements = placements(count);
    tile.createTerrainObjectsBatched(tile.terrainObjectPlacements, 0, tile.terrainObjectGeneration);
  }

  function liveNodes(tile: any): TransformNode[] {
    return tile.container.getChildren().filter((node: TransformNode) => !node.isDisposed());
  }

  beforeEach(() => {
    jasmine.clock().install();
    editor = false;
    scene = new Scene(new NullEngine());
    spyOn(impl, 'createTerrainObject').and.callFake((_model: any, _config: any, _service: any, parent: TransformNode) => {
      const node = new TransformNode('TerrainObject', scene);
      node.setParent(parent);
      return node;
    });
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    scene.getEngine().dispose();
  });

  it('disposes the objects of a tile hidden for the delay and plants them again when shown', () => {
    const tile = newTile();
    plant(tile, 30);
    expect(liveNodes(tile).length).toBe(30);

    tile.removeFromScene();
    jasmine.clock().tick(DELAY - 1);
    expect(liveNodes(tile).length).withContext('not yet').toBe(30);
    jasmine.clock().tick(1);
    expect(liveNodes(tile).length).toBe(0);

    tile.addToScene();
    expect(liveNodes(tile).length).toBe(30);
  });

  it('keeps the objects of a tile shown again before the delay', () => {
    const tile = newTile();
    plant(tile, 10);
    tile.removeFromScene();
    jasmine.clock().tick(DELAY / 2);
    tile.addToScene();
    jasmine.clock().tick(DELAY);

    expect(liveNodes(tile).length).toBe(10);
    expect(impl.createTerrainObject).toHaveBeenCalledTimes(10);
  });

  it('disposes a large tile over several frames and stops a planting still under way', () => {
    const tile = newTile();
    plant(tile, 120);            // 50 now, the rest in later batches
    expect(liveNodes(tile).length).toBe(50);
    tile.removeFromScene();
    jasmine.clock().tick(DELAY); // dropped while 70 were still to come

    expect(liveNodes(tile).filter(node => node.isEnabled(false)).length).withContext('all switched off at once').toBe(0);
    jasmine.clock().tick(1000);
    expect(liveNodes(tile).length).withContext('the stale batches planted nothing').toBe(0);

    tile.addToScene();
    jasmine.clock().tick(1000);
    expect(liveNodes(tile).length).toBe(120);
  });

  it('plants once a tile that was dropped before its objects arrived', () => {
    const tile = newTile();
    tile.removeFromScene();
    jasmine.clock().tick(DELAY);
    tile.setupTerrainTileObjects([{terrainObjectConfigId: 1, terrainObjectModels: [{position: {}}, {position: {}}]}]);
    expect(liveNodes(tile).length).withContext('hidden: nothing planted').toBe(0);

    tile.addToScene();
    expect(liveNodes(tile).length).toBe(2);
  });

  it('never drops in the editor, which moves the planted nodes itself', () => {
    editor = true;
    const tile = newTile();
    plant(tile, 5);
    tile.removeFromScene();
    jasmine.clock().tick(DELAY * 2);

    expect(liveNodes(tile).length).toBe(5);
  });
});
