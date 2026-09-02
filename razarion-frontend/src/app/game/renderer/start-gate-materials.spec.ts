import {materialsForFirstFrame} from './start-gate-materials';
import {UiConfigCollection} from '../../generated/razarion-share';

/**
 * Which materials a player waits for before the game starts.
 *
 * Getting this wrong is expensive in both directions and neither shows up in a build: too wide and
 * the first frame is 5.5 MB late again, too narrow and the ground renders in the red "missing
 * material" placeholder for everyone.
 */
describe('Start gate materials', () => {
  function collection(materialIds: number[],
                      glbs: { [name: string]: number }[]): UiConfigCollection {
    return {
      babylonMaterials: materialIds.map(id => ({id})),
      gltfs: glbs.map((materialGltfNames, index) => ({id: 100 + index, materialGltfNames}))
    } as unknown as UiConfigCollection;
  }

  it('keeps what nothing else can wait for and drops what a glb names', () => {
    // The shape of the real content on 2026-09-02: asphalt and water are painted by the terrain
    // code, the vehicle and building materials belong to models that wait for them themselves.
    const required = materialsForFirstFrame(collection([1, 2, 3, 8, 11, 12], [
      {'vehicle_body': 2, 'vehicle_glass': 2},
      {'building_wall': 3}
    ]));

    expect(required).toEqual([1, 8, 11, 12]);
  });

  it('drops a material the moment any single glb names it', () => {
    // One reference is enough: that model waits, so the gate need not.
    expect(materialsForFirstFrame(collection([1, 2], [{'a': 2}, {'b': 2}, {'c': 2}])))
      .toEqual([1]);
  });

  it('keeps everything when no glb names anything', () => {
    expect(materialsForFirstFrame(collection([1, 2, 3], []))).toEqual([1, 2, 3]);
  });

  it('survives content that has no models, no materials, or neither', () => {
    // A planet under construction, and the mock server, both produce these.
    expect(materialsForFirstFrame(collection([], []))).toEqual([]);
    expect(materialsForFirstFrame({} as UiConfigCollection)).toEqual([]);
    expect(materialsForFirstFrame({babylonMaterials: [{id: 5}]} as unknown as UiConfigCollection))
      .toEqual([5]);
  });

  it('ignores a glb with no material names at all', () => {
    // materialGltfNames is absent on a model that paints itself entirely from its own glb.
    expect(materialsForFirstFrame(collection([1, 2], [{} as any, undefined as any])))
      .toEqual([1, 2]);
  });
});
