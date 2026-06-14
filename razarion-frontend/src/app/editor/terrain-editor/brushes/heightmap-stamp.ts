import {PerlinNoise} from './perlin-noise';

/**
 * A procedurally generated mountain heightmap "stamp" (World-Creator style). Instead of shipping DEM
 * image assets, each preset is a recipe built from ridged-multifractal noise — which produces real
 * fractal mountain relief (ridges, valleys, irregular outline) rather than a smooth parametric cone.
 *
 * The grid is normalized so the summit is 1.0 and a radial edge mask fades it to 0 at the boundary,
 * so a stamp always blends seamlessly into the surrounding terrain (no square seam / cliff). Sample
 * it in normalized [0,1] UV space; multiply the result by the desired peak height in meters.
 */
export class HeightmapStamp {
  static readonly PRESETS: { label: string, value: string }[] = [
    {label: 'Peak (single summit)', value: 'peak'},
    {label: 'Ridge (elongated)', value: 'ridge'},
    {label: 'Massif (cluster)', value: 'massif'},
    {label: 'Hills (gentle)', value: 'hills'}
  ];

  private readonly res: number;
  private readonly grid: Float32Array;

  constructor(preset: string, seed: number, resolution: number = 96) {
    this.res = resolution;
    this.grid = HeightmapStamp.generate(preset, seed, resolution);
  }

  /** Bilinear sample at normalized u,v in [0,1]; returns 0 outside. */
  sample(u: number, v: number): number {
    if (u < 0 || u > 1 || v < 0 || v > 1) {
      return 0;
    }
    const n = this.res;
    const fx = u * (n - 1);
    const fy = v * (n - 1);
    const x0 = Math.floor(fx);
    const y0 = Math.floor(fy);
    const x1 = Math.min(n - 1, x0 + 1);
    const y1 = Math.min(n - 1, y0 + 1);
    const tx = fx - x0;
    const ty = fy - y0;
    const h00 = this.grid[y0 * n + x0];
    const h10 = this.grid[y0 * n + x1];
    const h01 = this.grid[y1 * n + x0];
    const h11 = this.grid[y1 * n + x1];
    const a = h00 + (h10 - h00) * tx;
    const b = h01 + (h11 - h01) * tx;
    return a + (b - a) * ty;
  }

  private static generate(preset: string, seed: number, res: number): Float32Array {
    const noise = new PerlinNoise(seed);
    const grid = new Float32Array(res * res);
    let max = 0;

    for (let y = 0; y < res; y++) {
      for (let x = 0; x < res; x++) {
        const u = x / (res - 1);
        const v = y / (res - 1);
        const nx = u - 0.5; // [-0.5, 0.5]
        const ny = v - 0.5;
        const rr = Math.min(1, Math.sqrt(nx * nx + ny * ny) * 2); // 0 center .. 1 inscribed edge

        let value: number;
        switch (preset) {
          case 'ridge': {
            // Elongated spine along x, ridged detail, ends tapered.
            const spine = Math.max(0, 1 - Math.abs(ny) / 0.5);
            const ends = Math.max(0, 1 - Math.abs(nx) / 0.5);
            const detail = HeightmapStamp.ridged(noise, nx * 5, ny * 6, 4);
            value = Math.pow(spine, 1.5) * Math.pow(ends, 0.5) * (0.55 + 0.6 * detail);
            break;
          }
          case 'massif': {
            // Broad cluster of summits.
            const bowl = Math.pow(Math.max(0, 1 - rr), 0.6);
            const detail = HeightmapStamp.ridged(noise, nx * 2.4, ny * 2.4, 5);
            value = bowl * (0.35 + 0.85 * detail);
            break;
          }
          case 'hills': {
            // Gentle rounded relief, low contrast.
            const bowl = Math.pow(Math.max(0, 1 - rr), 0.8);
            const f = 0.5 + 0.5 * noise.fbm(nx * 2.6 + 11, ny * 2.6 + 11, 3);
            value = bowl * (0.4 + 0.7 * f);
            break;
          }
          case 'peak':
          default: {
            // One dominant central summit with rough ridged flanks.
            const cone = Math.pow(Math.max(0, 1 - rr), 1.2);
            const detail = HeightmapStamp.ridged(noise, nx * 3.2, ny * 3.2, 4);
            value = cone * (0.65 + 0.6 * detail);
            break;
          }
        }

        // Radial edge mask: full inside, smooth cosine fade to 0 at the boundary -> no seam.
        const m = HeightmapStamp.edgeMask(rr);
        value = Math.max(0, value) * m;
        grid[y * res + x] = value;
        if (value > max) {
          max = value;
        }
      }
    }

    // Normalize so the summit is exactly 1.0.
    if (max > 0) {
      const inv = 1 / max;
      for (let i = 0; i < grid.length; i++) {
        grid[i] *= inv;
      }
    }
    return grid;
  }

  /** Ridged multifractal in [0,1]: layered (1 - |noise|)^2, ridge crests near 1. */
  private static ridged(noise: PerlinNoise, x: number, y: number, octaves: number): number {
    let amp = 1;
    let freq = 1;
    let sum = 0;
    let norm = 0;
    for (let i = 0; i < octaves; i++) {
      let n = 1 - Math.abs(noise.noise2D(x * freq + i * 7.3, y * freq + i * 3.1));
      n = n * n;
      sum += n * amp;
      norm += amp;
      amp *= 0.5;
      freq *= 2;
    }
    return norm > 0 ? sum / norm : 0;
  }

  /** 1 for rr <= 0.55, smooth cosine fade to 0 at rr >= 1.0. */
  private static edgeMask(rr: number): number {
    if (rr <= 0.55) {
      return 1;
    }
    if (rr >= 1) {
      return 0;
    }
    const t = (rr - 0.55) / 0.45;
    return 0.5 * (1 + Math.cos(Math.PI * t));
  }
}
