import {Constants, InternalTexture} from '@babylonjs/core';
import {TextureMemory} from './texture-memory';

/** Enough of an InternalTexture for the arithmetic; the real one needs an engine. */
function texture(over: Partial<InternalTexture>): InternalTexture {
  return {
    width: 1024, height: 1024, depth: 1, isCube: false, generateMipMaps: false,
    format: Constants.TEXTUREFORMAT_RGBA, type: Constants.TEXTURETYPE_UNSIGNED_BYTE,
    ...over
  } as InternalTexture;
}

describe('TextureMemory', () => {

  describe('bytesPerTexel', () => {
    it('reads a colour texture as four bytes', () => {
      expect(TextureMemory.bytesPerTexel(Constants.TEXTUREFORMAT_RGBA, Constants.TEXTURETYPE_UNSIGNED_BYTE)).toBe(4);
    });

    it('does not pay for a channel that is not there', () => {
      expect(TextureMemory.bytesPerTexel(Constants.TEXTUREFORMAT_R, Constants.TEXTURETYPE_UNSIGNED_BYTE)).toBe(1);
      expect(TextureMemory.bytesPerTexel(Constants.TEXTUREFORMAT_RG, Constants.TEXTURETYPE_UNSIGNED_BYTE)).toBe(2);
      expect(TextureMemory.bytesPerTexel(Constants.TEXTUREFORMAT_RGB, Constants.TEXTURETYPE_UNSIGNED_BYTE)).toBe(3);
    });

    it('counts a float render target at four bytes a channel', () => {
      // The shadow map can be one of these. Reading it as 8-bit would understate the single
      // largest allocation in the scene by a factor of four.
      expect(TextureMemory.bytesPerTexel(Constants.TEXTUREFORMAT_RGBA, Constants.TEXTURETYPE_FLOAT)).toBe(16);
      expect(TextureMemory.bytesPerTexel(Constants.TEXTUREFORMAT_RGBA, Constants.TEXTURETYPE_HALF_FLOAT)).toBe(8);
    });

    it('assumes four bytes for a format it does not know', () => {
      // Guessing lower would flatter the number, and the number exists to be believed.
      expect(TextureMemory.bytesPerTexel(9999, 9999)).toBe(4);
    });
  });

  describe('bytesOf', () => {
    it('is width x height x bytes for a plain texture', () => {
      expect(TextureMemory.bytesOf(texture({width: 512, height: 256}))).toBe(512 * 256 * 4);
    });

    it('adds a third for the mip chain', () => {
      // 1 + 1/4 + 1/16 + ... = 4/3
      expect(TextureMemory.bytesOf(texture({width: 512, height: 512, generateMipMaps: true})))
        .toBe(512 * 512 * 4 * 4 / 3);
    });

    it('counts all six faces of a cube', () => {
      expect(TextureMemory.bytesOf(texture({width: 128, height: 128, isCube: true})))
        .toBe(128 * 128 * 4 * 6);
    });

    it('says nothing rather than something wrong about a texture with no size', () => {
      expect(TextureMemory.bytesOf(texture({width: 0, height: 0}))).toBe(0);
    });

    it('prices the 4096 shadow map the size arm is about', () => {
      // 4096 x 4096 x RGBA, no mips: 64 MB in one allocation. This is the number the whole
      // shadow-size experiment was reasoning about, so it is worth having it pinned.
      expect(TextureMemory.bytesOf(texture({width: 4096, height: 4096}))).toBe(67108864);
    });
  });

  describe('totalBytes', () => {
    it('sums the cache', () => {
      const cache = [texture({width: 256, height: 256}), texture({width: 512, height: 512})];
      expect(TextureMemory.totalBytes(cache)).toBe(256 * 256 * 4 + 512 * 512 * 4);
    });

    it('reports -1 rather than 0 when there is no cache', () => {
      // Zero would read as "no textures", which is a different and wrong statement.
      expect(TextureMemory.totalBytes(null)).toBe(-1);
    });
  });

  describe('toMb', () => {
    it('rounds to one decimal', () => {
      expect(TextureMemory.toMb(67108864)).toBe(64);
      expect(TextureMemory.toMb(1572864)).toBe(1.5);
    });

    it('keeps -1 as -1 instead of turning it into a tiny negative megabyte', () => {
      expect(TextureMemory.toMb(-1)).toBe(-1);
    });
  });

  describe('jsHeap', () => {
    it('answers with a pair either way', () => {
      // Chromium has performance.memory, iOS does not. Both must return a usable pair.
      const [used, limit] = TextureMemory.jsHeap();
      expect(typeof used).toBe('number');
      expect(typeof limit).toBe('number');
      expect(used === -1 || used > 0).toBeTrue();
    });
  });
});
