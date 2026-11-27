import {DeepCopier} from "@babylonjs/core/Misc/deepCopier";
import {NullEngine} from "@babylonjs/core/Engines/nullEngine";
import {Scene} from "@babylonjs/core/scene";
import {MeshBuilder} from "@babylonjs/core/Meshes/meshBuilder";
import {StandardMaterial} from "@babylonjs/core/Materials/standardMaterial";
import {Vector3} from "@babylonjs/core/Maths/math.vector";
import {fastDeepCopy, getAllPropertyNames} from "./fast-deep-copier";

describe('fastDeepCopy', () => {
  const originalDeepCopy = DeepCopier.DeepCopy;

  /** Babylon's own GetAllPropertyNames, the quadratic one, as the reference. */
  function referenceNames(obj: any): string[] {
    const props: string[] = [];
    do {
      for (const prop of Object.getOwnPropertyNames(obj)) {
        if (props.indexOf(prop) === -1) {
          props.push(prop);
        }
      }
    } while ((obj = Object.getPrototypeOf(obj)));
    return props;
  }

  function snapshot(obj: any, names: string[]): any[] {
    return names.map(name => {
      try {
        const value = obj[name];
        if (typeof value === 'function') return 'fn';
        if (value && typeof value === 'object') return value.constructor?.name ?? 'object';
        return value;
      } catch {
        return 'throws';
      }
    });
  }

  let engine: NullEngine;
  let scene: Scene;

  beforeEach(() => {
    DeepCopier.DeepCopy = originalDeepCopy;
    engine = new NullEngine();
    scene = new Scene(engine);
  });

  afterEach(() => {
    DeepCopier.DeepCopy = originalDeepCopy;
    scene.dispose();
    engine.dispose();
  });

  it('collects the same names in the same order as Babylon', () => {
    const mesh = MeshBuilder.CreateBox('box', {}, scene);
    expect(getAllPropertyNames(mesh)).toEqual(referenceNames(mesh));
    class Base { a = 1; get b() { return 2; } }
    class Derived extends Base { c = 3; override get b() { return 4; } }
    expect(getAllPropertyNames(new Derived())).toEqual(referenceNames(new Derived()));
  });

  it('copies a mesh exactly like Babylon', () => {
    const source = MeshBuilder.CreateBox('box', {}, scene);
    source.material = new StandardMaterial('mat', scene);
    source.position = new Vector3(1, 2, 3);
    source.receiveShadows = true;
    source.visibility = 0.5;
    source.metadata = {x: 1};
    const names = referenceNames(source);

    const expected = source.clone('a');
    DeepCopier.DeepCopy = fastDeepCopy as any;
    const actual = source.clone('b');

    const expectedNames = referenceNames(expected).filter(n => n !== 'name' && n !== 'id' && n !== 'uniqueId');
    expect(referenceNames(actual).filter(n => n !== 'name' && n !== 'id' && n !== 'uniqueId')).toEqual(expectedNames);
    const compared = names.filter(n => !n.startsWith('_') && !['name', 'id', 'uniqueId'].includes(n));
    expect(snapshot(actual, compared)).toEqual(snapshot(expected, compared));
    expect(actual.position.asArray()).toEqual([1, 2, 3]);
    expect(actual.material).toBe(source.material);
    expect(actual.visibility).toBe(0.5);
  });

  it('is faster than Babylon on a mesh', () => {
    const source = MeshBuilder.CreateBox('box', {}, scene);
    const time = (copy: (s: any, d: any) => void) => {
      const start = performance.now();
      for (let i = 0; i < 200; i++) {
        copy(source, {});
      }
      return performance.now() - start;
    };
    const babylon = time((s, d) => originalDeepCopy(s, d, ['name', 'material', 'skeleton', 'instances', 'parent', 'uniqueId', 'source', 'metadata', 'morphTargetManager', 'hasInstances', 'worldMatrixInstancedBuffer', 'previousWorldMatrixInstancedBuffer', 'hasLODLevels', 'geometry', 'isBlocked', 'areNormalsFrozen', 'facetNb', 'isFacetDataEnabled', 'lightSources', 'useBones', 'isAnInstance', 'collider', 'edgesRenderer', 'forward', 'up', 'right', 'absolutePosition', 'absoluteScaling', 'absoluteRotationQuaternion', 'isWorldMatrixFrozen', 'nonUniformScaling', 'behaviors', 'worldMatrixFromCache', 'hasThinInstances', 'cloneMeshMap', 'hasBoundingInfo', 'physicsBody', 'physicsImpostor'], ['_poseMatrix']));
    const fast = time((s, d) => fastDeepCopy(s, d, ['name', 'material', 'skeleton', 'instances', 'parent', 'uniqueId', 'source', 'metadata', 'morphTargetManager', 'hasInstances', 'worldMatrixInstancedBuffer', 'previousWorldMatrixInstancedBuffer', 'hasLODLevels', 'geometry', 'isBlocked', 'areNormalsFrozen', 'facetNb', 'isFacetDataEnabled', 'lightSources', 'useBones', 'isAnInstance', 'collider', 'edgesRenderer', 'forward', 'up', 'right', 'absolutePosition', 'absoluteScaling', 'absoluteRotationQuaternion', 'isWorldMatrixFrozen', 'nonUniformScaling', 'behaviors', 'worldMatrixFromCache', 'hasThinInstances', 'cloneMeshMap', 'hasBoundingInfo', 'physicsBody', 'physicsImpostor'], ['_poseMatrix']));
    console.log(`[fastDeepCopy] 200 mesh copies: babylon ${babylon.toFixed(1)} ms, fast ${fast.toFixed(1)} ms`);
    expect(fast).toBeLessThan(babylon);
  });
});
