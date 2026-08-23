import {ParkedMeshFilter} from './parked-mesh-filter';

/**
 * The filter never touches Babylon types beyond reading a flag and pushing into the buffers it
 * hands back, so the tests drive it with the smallest stand-ins that carry the same shape. That
 * keeps them honest about the one thing worth asserting: what ends up in front of Babylon's two
 * per-frame loops, and that a bypassed filter is indistinguishable from not having one.
 */
describe('ParkedMeshFilter', () => {
  function mesh(name: string): any {
    return {name};
  }

  /** Minimal stand-in for the parts of Scene and the shadow map the filter installs itself on. */
  function sceneStub(meshes: any[]): any {
    return {
      meshes,
      getActiveMeshCandidates: () => ({data: meshes, length: meshes.length})
    };
  }

  function shadowMapStub(): any {
    return {getCustomRenderList: null};
  }

  it('hands Babylon only the meshes that are not parked', () => {
    const visible = mesh('ground');
    const hidden = mesh('palm#inst');
    ParkedMeshFilter.park(hidden, true);
    const scene = sceneStub([visible, hidden]);
    new ParkedMeshFilter().install(scene, null);

    const candidates = scene.getActiveMeshCandidates();

    expect(candidates.length).toBe(1);
    expect(candidates.data[0]).toBe(visible);
  });

  it('drops the parked casters from the shadow render list', () => {
    const visible = mesh('trunk#inst');
    const hidden = mesh('leaves#inst');
    ParkedMeshFilter.parkAll([hidden], true);
    const shadowMap = shadowMapStub();
    new ParkedMeshFilter().install(sceneStub([]), shadowMap);

    const list = shadowMap.getCustomRenderList(0, [visible, hidden], 2);

    expect(list).toEqual([visible]);
  });

  it('honours the render list length Babylon passes rather than the array length', () => {
    // The render list array can be longer than the part Babylon considers valid — the contract
    // says to use renderListLength, and reading past it would resurrect stale meshes.
    const first = mesh('a');
    const stale = mesh('b');
    const shadowMap = shadowMapStub();
    new ParkedMeshFilter().install(sceneStub([]), shadowMap);

    expect(shadowMap.getCustomRenderList(0, [first, stale], 1)).toEqual([first]);
  });

  it('is a pure bypass when switched off, in both loops', () => {
    const visible = mesh('ground');
    const hidden = mesh('rock#inst');
    ParkedMeshFilter.park(hidden, true);
    const scene = sceneStub([visible, hidden]);
    const shadowMap = shadowMapStub();
    const filter = new ParkedMeshFilter();
    filter.install(scene, shadowMap);

    filter.setEnabled(false);

    // Scene: the untouched original list, parked mesh included.
    expect(scene.getActiveMeshCandidates().length).toBe(2);
    // Shadow: null tells Babylon to use the render list it already has.
    expect(shadowMap.getCustomRenderList(0, [visible, hidden], 2)).toBeNull();
  });

  it('unparks again, so a tile scrolled back into view is drawn', () => {
    const tile = mesh('palm#inst');
    ParkedMeshFilter.parkAll([tile], true);
    ParkedMeshFilter.parkAll([tile], false);
    const scene = sceneStub([tile]);
    new ParkedMeshFilter().install(scene, null);

    expect(scene.getActiveMeshCandidates().length).toBe(1);
    expect(ParkedMeshFilter.isParked(tile)).toBeFalse();
  });

  it('counts what it withheld, and admits to nothing while bypassed', () => {
    const meshes = [mesh('a'), mesh('b'), mesh('c')];
    ParkedMeshFilter.parkAll([meshes[1], meshes[2]], true);
    const scene = sceneStub(meshes);
    const filter = new ParkedMeshFilter();
    filter.install(scene, null);

    scene.getActiveMeshCandidates();
    expect(filter.getParkedCount()).toBe(2);

    filter.setEnabled(false);
    expect(filter.getParkedCount()).toBe(-1);
  });

  it('reuses its buffers instead of allocating per frame', () => {
    // 30 000 meshes at 60 fps is why: a fresh array per frame would be pure GC pressure.
    const meshes = [mesh('a'), mesh('b')];
    const scene = sceneStub(meshes);
    new ParkedMeshFilter().install(scene, null);

    const first = scene.getActiveMeshCandidates();
    const second = scene.getActiveMeshCandidates();

    expect(second).toBe(first);
    expect(second.length).toBe(2);
  });
});
