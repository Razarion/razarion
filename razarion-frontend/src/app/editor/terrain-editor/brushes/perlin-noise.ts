/**
 * Classic 2D Perlin noise implementation.
 * Based on the improved Perlin noise algorithm by Ken Perlin.
 */
export class PerlinNoise {
  private readonly perm: Uint8Array;

  constructor(seed: number = 0) {
    this.perm = new Uint8Array(512);
    const p = new Uint8Array(256);
    for (let i = 0; i < 256; i++) p[i] = i;

    // Fisher-Yates shuffle with seed
    let s = seed;
    for (let i = 255; i > 0; i--) {
      s = (s * 16807 + 0) % 2147483647;
      const j = s % (i + 1);
      const tmp = p[i];
      p[i] = p[j];
      p[j] = tmp;
    }

    for (let i = 0; i < 512; i++) {
      this.perm[i] = p[i & 255];
    }
  }

  private static fade(t: number): number {
    return t * t * t * (t * (t * 6 - 15) + 10);
  }

  private static grad(hash: number, x: number, y: number): number {
    const h = hash & 3;
    const u = h < 2 ? x : y;
    const v = h < 2 ? y : x;
    return ((h & 1) === 0 ? u : -u) + ((h & 2) === 0 ? v : -v);
  }

  noise2D(x: number, y: number): number {
    const X = Math.floor(x) & 255;
    const Y = Math.floor(y) & 255;
    const xf = x - Math.floor(x);
    const yf = y - Math.floor(y);
    const u = PerlinNoise.fade(xf);
    const v = PerlinNoise.fade(yf);

    const aa = this.perm[this.perm[X] + Y];
    const ab = this.perm[this.perm[X] + Y + 1];
    const ba = this.perm[this.perm[X + 1] + Y];
    const bb = this.perm[this.perm[X + 1] + Y + 1];

    const x1 = this.lerp(PerlinNoise.grad(aa, xf, yf), PerlinNoise.grad(ba, xf - 1, yf), u);
    const x2 = this.lerp(PerlinNoise.grad(ab, xf, yf - 1), PerlinNoise.grad(bb, xf - 1, yf - 1), u);

    return this.lerp(x1, x2, v);
  }

  private lerp(a: number, b: number, t: number): number {
    return a + t * (b - a);
  }

  /**
   * Fractal Brownian Motion: layered Perlin noise with multiple octaves.
   * Returns value roughly in [-1, 1].
   */
  fbm(x: number, y: number, octaves: number): number {
    let value = 0;
    let amplitude = 1;
    let totalAmplitude = 0;
    let frequency = 1;

    for (let i = 0; i < octaves; i++) {
      value += this.noise2D(x * frequency, y * frequency) * amplitude;
      totalAmplitude += amplitude;
      amplitude *= 0.5;
      frequency *= 2;
    }

    return value / totalAmplitude;
  }
}
