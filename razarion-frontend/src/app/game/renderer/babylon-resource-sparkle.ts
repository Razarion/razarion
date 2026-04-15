import {Color4, ParticleSystem, RawTexture, Vector3} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";

export class BabylonResourceSparkle {
  private particleSystem: ParticleSystem | null = null;
  readonly emitter: Vector3 = new Vector3(0, 0, 0);

  constructor(scene: Scene, radius: number) {
    const ps = new ParticleSystem("ResourceSparkle", 200, scene);

    // Create tiny sparkle dust texture via RawTexture (synchronous)
    const size = 32;
    const data = new Uint8Array(size * size * 4);
    const center = size / 2;
    for (let y = 0; y < size; y++) {
      for (let x = 0; x < size; x++) {
        const dx = (x - center) / center;
        const dy = (y - center) / center;
        const dist = Math.sqrt(dx * dx + dy * dy);
        // Sharp bright core with rapid falloff — tiny glinting speck
        const core = Math.exp(-dist * dist * 12.0);
        // Faint cross flare for sparkle glint
        const flareX = Math.exp(-dy * dy * 20.0) * Math.exp(-dx * dx * 2.0);
        const flareY = Math.exp(-dx * dx * 20.0) * Math.exp(-dy * dy * 2.0);
        const flare = Math.max(flareX, flareY) * 0.4;
        const brightness = Math.min(1, core + flare);
        const alpha = brightness * (dist < 1.0 ? 1 : 0);
        const idx = (y * size + x) * 4;
        data[idx] = Math.round(220 + 35 * core);
        data[idx + 1] = Math.round(230 + 25 * core);
        data[idx + 2] = 255;
        data[idx + 3] = Math.round(alpha * 255);
      }
    }
    ps.particleTexture = RawTexture.CreateRGBATexture(data, size, size, scene, false, false);

    ps.emitter = this.emitter;
    ps.minEmitBox = new Vector3(-radius * 0.8, 0.2, -radius * 0.8);
    ps.maxEmitBox = new Vector3(radius * 0.8, radius * 1.2, radius * 0.8);

    ps.direction1 = new Vector3(-1.0, 0.2, -1.0);
    ps.direction2 = new Vector3(1.0, 0.8, 1.0);
    ps.gravity = new Vector3(0, -0.1, 0);

    ps.color1 = new Color4(0.85, 0.92, 1.0, 1.0);
    ps.color2 = new Color4(0.7, 0.85, 1.0, 1.0);
    ps.colorDead = new Color4(0.6, 0.75, 1.0, 0.0);

    ps.minSize = 0.08;
    ps.maxSize = 0.3;
    ps.minLifeTime = 1.5;
    ps.maxLifeTime = 3.5;

    ps.emitRate = 30;
    ps.blendMode = ParticleSystem.BLENDMODE_ADD;

    ps.minEmitPower = 0.3;
    ps.maxEmitPower = 0.8;

    ps.renderingGroupId = 1;
    ps.start();
    this.particleSystem = ps;
  }

  dispose(): void {
    if (this.particleSystem) {
      this.particleSystem.stop();
      this.particleSystem.dispose();
      this.particleSystem = null;
    }
  }
}
