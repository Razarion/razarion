import {Constants, InternalTexture} from "@babylonjs/core";

/**
 * How much GPU memory the loaded textures occupy, estimated.
 *
 * Why this exists. 24 hours of PROD telemetry split the touch sessions by {@code deviceMemory}:
 *
 *   RAM 3-4 GB   n=41   6.0 fps   frameP50 132 ms   tickGap 132 ms
 *   RAM > 4 GB   n=57  13.7 fps   frameP50  67 ms   tickGap 108 ms
 *
 * with the same scene on both sides (1712 against 1813 meshes). And the difference is not that the
 * smaller devices are evenly slower — it is that they stop: renderMax/renderP50 3.5 against 2.9,
 * four frames over 250 ms per ten seconds against one, tick gaps to 3.2 s against 1.2 s. That is
 * the handwriting of garbage collection and texture eviction, not of a weaker chip.
 *
 * Nothing in the telemetry could confirm or refute that, which is what this is for.
 *
 * <p><b>It is an estimate and has to be read as one.</b> The bytes a driver actually reserves
 * depend on the internal format it picks, on padding and on whether it keeps a staging copy. What
 * is computed here is the lower bound implied by dimensions, channel count and mip chain — good
 * enough to answer "is this 30 MB or 300 MB", which is the question, and not good enough to argue
 * about ten per cent.
 *
 * <p>Read from {@code getLoadedTexturesCache()} rather than from {@code scene.textures}: the latter
 * counts Texture objects, and several of those can share one GPU allocation. The cache is the
 * allocations.
 */
export class TextureMemory {

  /**
   * Bytes per texel for the format/type pairs this game actually produces.
   *
   * <p>Everything the renderer loads arrives as WebP or PNG and is uploaded as 8-bit RGBA, so the
   * common answer is 4. The float cases are here because a render target (the shadow map is one)
   * can be half- or full-float, and getting those wrong would misplace the largest single item on
   * the list by a factor of four.
   */
  static bytesPerTexel(format: number, type: number): number {
    let channels: number;
    switch (format) {
      case Constants.TEXTUREFORMAT_ALPHA:
      case Constants.TEXTUREFORMAT_LUMINANCE:
      case Constants.TEXTUREFORMAT_R:
        channels = 1;
        break;
      case Constants.TEXTUREFORMAT_LUMINANCE_ALPHA:
      case Constants.TEXTUREFORMAT_RG:
        channels = 2;
        break;
      case Constants.TEXTUREFORMAT_RGB:
        channels = 3;
        break;
      default:
        // RGBA and anything unrecognised. Four is the honest default: it is what every colour
        // texture in this game is, and guessing lower would flatter the number.
        channels = 4;
    }
    let bytes: number;
    switch (type) {
      case Constants.TEXTURETYPE_FLOAT:
        bytes = 4;
        break;
      case Constants.TEXTURETYPE_HALF_FLOAT:
        bytes = 2;
        break;
      default:
        bytes = 1;
    }
    return channels * bytes;
  }

  /**
   * Bytes for one allocation, mip chain and cube faces included.
   *
   * <p>The mip chain adds a third: 1 + 1/4 + 1/16 + ... converges to 4/3. Taken as the closed form
   * rather than summed level by level — the difference is under a per cent and this runs over every
   * texture in the scene.
   */
  static bytesOf(texture: InternalTexture): number {
    const width = texture.width || 0;
    const height = texture.height || 0;
    const depth = texture.depth && texture.depth > 1 ? texture.depth : 1;
    if (width <= 0 || height <= 0) {
      return 0;
    }
    let bytes = width * height * depth * TextureMemory.bytesPerTexel(texture.format, texture.type);
    if (texture.isCube) {
      bytes *= 6;
    }
    if (texture.generateMipMaps) {
      bytes = bytes * 4 / 3;
    }
    return bytes;
  }

  /** Total over a whole texture cache, in bytes. Returns -1 rather than throwing. */
  static totalBytes(textures: InternalTexture[] | null | undefined): number {
    if (!textures) {
      return -1;
    }
    try {
      let total = 0;
      for (let i = 0; i < textures.length; i++) {
        total += TextureMemory.bytesOf(textures[i]);
      }
      return total;
    } catch (e) {
      return -1;
    }
  }

  /**
   * The JS heap, where the browser exposes it: {@code [used, limit]} in bytes, or {@code [-1, -1]}.
   *
   * <p>{@code performance.memory} is Chromium-only and non-standard, which for this audience is
   * almost everybody: 90 of 101 touch sessions run without cross-origin isolation, i.e. inside the
   * Instagram in-app browser, and that is Chromium. iOS returns nothing and will read as -1 — the
   * coverage is itself worth knowing.
   *
   * <p>{@code jsHeapSizeLimit} is the interesting half. A device thrashing its heap is one where
   * used sits near the limit, and the limit on a memory-constrained WebView is not the desktop's.
   */
  static jsHeap(): [number, number] {
    try {
      const memory = (performance as any).memory;
      if (!memory || typeof memory.usedJSHeapSize !== "number") {
        return [-1, -1];
      }
      return [memory.usedJSHeapSize, memory.jsHeapSizeLimit ?? -1];
    } catch (e) {
      return [-1, -1];
    }
  }

  /** Bytes to megabytes, one decimal, for the log line. -1 stays -1. */
  static toMb(bytes: number): number {
    return bytes < 0 ? -1 : Math.round(bytes / 104857.6) / 10;
  }
}
