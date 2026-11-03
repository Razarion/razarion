import {NullEngine} from '@babylonjs/core/Engines/nullEngine';
import {Scene} from '@babylonjs/core/scene';
import {rockTextures} from './rock-material';

/**
 * A terrain tile evicted from the cache disposes its ground material together with its textures.
 * Any texture object that material shares with another one dies with it - on 2026-11-03 that was
 * the rock maps, and from then on no ground material was ready and the ground was no longer drawn.
 */
describe('Rock textures', () => {
  let scene: Scene;

  beforeEach(() => {
    scene = new Scene(new NullEngine());
  });

  afterEach(() => {
    scene.dispose();
  });

  it('gives every material texture objects of its own', () => {
    const first = rockTextures(scene);
    const second = rockTextures(scene);

    expect(second.diffuse).not.toBe(first.diffuse);
    expect(second.normal).not.toBe(first.normal);
  });

  it('keeps another material\'s rock maps alive when one material disposes its textures', () => {
    const evicted = rockTextures(scene);
    const shown = rockTextures(scene);

    evicted.diffuse.dispose();
    evicted.normal.dispose();

    expect(shown.diffuse.getInternalTexture()).not.toBeNull();
    expect(shown.normal.getInternalTexture()).not.toBeNull();
  });
});
