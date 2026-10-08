/**
 * The ground shader's sand/land decision on the CPU, for placing sprites where the shader draws the
 * matching ground. Sprites decided sand by height alone (< 0.4 m) while the shader draws sand by
 * height AND a splatter texture, so grass tufts and flowers stood on sand.
 *
 * Mirrors ground-material.ts "Beach detection": beachValue = (splatter.r - 0.4) * 0.6 + y * 1.2,
 * beachStep = smoothstep(0.23, 0.30, beachValue); 0 = sand, 1 = land.
 */
export class GroundZone {
  private static readonly URL = "renderer/textures/ground-splatter.webp";
  private static readonly UV_SCALE = 0.0125;   // "uv beach splatter"
  private static pixels: Uint8ClampedArray | null = null;
  private static width = 0;
  private static height = 0;
  private static loading: Promise<void> | null = null;

  /** Resolves on failure too: then the height-only rule is used, as before. */
  static load(): Promise<void> {
    if (!GroundZone.loading) {
      GroundZone.loading = new Promise<void>(resolve => {
        const image = new Image();
        image.onload = () => {
          try {
            const canvas = document.createElement("canvas");
            canvas.width = image.width;
            canvas.height = image.height;
            const context = canvas.getContext("2d")!;
            context.drawImage(image, 0, 0);
            GroundZone.pixels = context.getImageData(0, 0, image.width, image.height).data;
            GroundZone.width = image.width;
            GroundZone.height = image.height;
          } catch (error) {
            console.warn("[Razarion] ground splatter not readable", error);
          }
          resolve();
        };
        image.onerror = () => resolve();
        image.src = GroundZone.URL;
      });
    }
    return GroundZone.loading;
  }

  /** 0 = sand, 1 = land (grass or earth), as the shader blends it; null if the texture is missing. */
  static landStep(worldX: number, worldZ: number, height: number): number | null {
    if (!GroundZone.pixels) {
      return null;
    }
    const u = worldX * GroundZone.UV_SCALE, v = worldZ * GroundZone.UV_SCALE;
    // Wrapping texture; a URL texture is uploaded with invertY, so v = 0 is the image's bottom row
    const px = Math.floor((u - Math.floor(u)) * GroundZone.width);
    const py = GroundZone.height - 1 - Math.floor((v - Math.floor(v)) * GroundZone.height);
    const splatter = GroundZone.pixels[(py * GroundZone.width + px) * 4] / 255;
    const beachValue = (splatter - 0.4) * 0.6 + height * 1.2;
    const t = Math.max(0, Math.min(1, (beachValue - 0.23) / (0.30 - 0.23)));
    return t * t * (3 - 2 * t);
  }
}
