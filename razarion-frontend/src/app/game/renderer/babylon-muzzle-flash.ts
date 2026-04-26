import {Color4, DynamicTexture, ParticleSystem, Vector3} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";

export class BabylonMuzzleFlash {
  private static flashTextureCache: DynamicTexture | null = null;
  private static smokeTextureCache: DynamicTexture | null = null;

  // Persistent instances kept alive so they stay visible and tunable in the Babylon Inspector
  private static flash: ParticleSystem | null = null;
  private static smoke: ParticleSystem | null = null;

  static fire(scene: Scene, muzzlePosition: Vector3, forward: Vector3): void {
    if (!BabylonMuzzleFlash.flash) {
      BabylonMuzzleFlash.flash = BabylonMuzzleFlash.buildFlash(scene);
      BabylonMuzzleFlash.smoke = BabylonMuzzleFlash.buildSmoke(scene);
    }

    const flash = BabylonMuzzleFlash.flash!;
    flash.emitter = muzzlePosition.clone();
    flash.direction1 = forward.scale(0.5).add(new Vector3(-0.3, -0.3, -0.3));
    flash.direction2 = forward.scale(1.0).add(new Vector3(0.3, 0.3, 0.3));
    // Single-frame burst so the flash is at full intensity immediately (no ramp-up)
    flash.manualEmitCount = 20;
    flash.start();

    const smoke = BabylonMuzzleFlash.smoke!;
    smoke.emitter = muzzlePosition.clone();
    smoke.direction1 = forward.scale(0.8).add(new Vector3(-0.15, 0.1, -0.15));
    smoke.direction2 = forward.scale(1.6).add(new Vector3(0.15, 0.4, 0.15));
    smoke.start();
  }

  private static buildFlash(scene: Scene): ParticleSystem {
    const ps = new ParticleSystem("muzzleFlash", 20, scene);
    ps.particleTexture = BabylonMuzzleFlash.getFlashTexture(scene);
    ps.blendMode = ParticleSystem.BLENDMODE_ADD;
    ps.minEmitBox = new Vector3(-0.02, -0.02, -0.02);
    ps.maxEmitBox = new Vector3(0.02, 0.02, 0.02);
    ps.minLifeTime = 0.05;
    ps.maxLifeTime = 0.15;
    ps.emitRate = 0;
    ps.minSize = 1.2;
    ps.maxSize = 2.5;
    ps.minEmitPower = 0.5;
    ps.maxEmitPower = 1.0;
    ps.addColorGradient(0, new Color4(1, 1, 0.9, 1));
    ps.addColorGradient(0.5, new Color4(1, 0.9, 0.5, 1));
    ps.addColorGradient(1.0, new Color4(1, 0.7, 0.2, 0));
    ps.particleTexture!.dispose = () => {
    };
    return ps;
  }

  private static buildSmoke(scene: Scene): ParticleSystem {
    const ps = new ParticleSystem("muzzleSmoke", 44, scene);
    ps.particleTexture = BabylonMuzzleFlash.getSmokeTexture(scene);
    ps.blendMode = ParticleSystem.BLENDMODE_STANDARD;
    ps.minEmitBox = new Vector3(-0.05, -0.05, -0.05);
    ps.maxEmitBox = new Vector3(0.05, 0.05, 0.05);
    ps.minLifeTime = 0.6;
    ps.maxLifeTime = 1.5;
    ps.emitRate = 165;
    ps.targetStopDuration = 0.25;
    ps.minSize = 0.5;
    ps.maxSize = 1.2;
    ps.minEmitPower = 1.0;
    ps.maxEmitPower = 2.5;
    ps.addColorGradient(0, new Color4(0.6, 0.6, 0.55, 0.4));
    ps.addColorGradient(0.3, new Color4(0.5, 0.5, 0.48, 0.3));
    ps.addColorGradient(0.7, new Color4(0.4, 0.4, 0.38, 0.15));
    ps.addColorGradient(1.0, new Color4(0.3, 0.3, 0.3, 0));
    ps.addSizeGradient(0, 0.5);
    ps.addSizeGradient(0.5, 1.2);
    ps.addSizeGradient(1.0, 1.8);
    ps.gravity = new Vector3(0, 0.2, 0);
    ps.particleTexture!.dispose = () => {
    };
    return ps;
  }

  private static getFlashTexture(scene: Scene): DynamicTexture {
    if (BabylonMuzzleFlash.flashTextureCache) return BabylonMuzzleFlash.flashTextureCache;
    const tex = new DynamicTexture("muzzleFlashTex", 256, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext();
    const size = 256;
    const cx = size / 2;
    const cy = size / 2;
    const steps = 40;
    const radii: number[] = [];
    for (let i = 0; i < steps; i++) radii.push(50 + Math.random() * 60);
    for (let pass = 0; pass < 3; pass++) {
      const smoothed = [...radii];
      for (let i = 0; i < steps; i++) {
        smoothed[i] = radii[(i - 1 + steps) % steps] * 0.25 + radii[i] * 0.5 + radii[(i + 1) % steps] * 0.25;
      }
      radii.splice(0, steps, ...smoothed);
    }
    ctx.clearRect(0, 0, size, size);
    ctx.beginPath();
    for (let i = 0; i < steps; i++) {
      const angle = (i / steps) * Math.PI * 2;
      const x = cx + Math.cos(angle) * radii[i];
      const y = cy + Math.sin(angle) * radii[i];
      i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y);
    }
    ctx.closePath();
    const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, 120);
    grad.addColorStop(0, "rgba(255, 255, 240, 0.9)");
    grad.addColorStop(0.15, "rgba(255, 255, 180, 0.6)");
    grad.addColorStop(0.35, "rgba(255, 220, 100, 0.3)");
    grad.addColorStop(0.6, "rgba(255, 180, 50, 0.1)");
    grad.addColorStop(1.0, "rgba(255, 140, 20, 0)");
    ctx.fillStyle = grad;
    ctx.fill();
    const glow = ctx.createRadialGradient(cx, cy, 0, cx, cy, 40);
    glow.addColorStop(0, "rgba(255, 255, 255, 1)");
    glow.addColorStop(0.5, "rgba(255, 255, 220, 0.7)");
    glow.addColorStop(1.0, "rgba(255, 255, 200, 0)");
    ctx.fillStyle = glow;
    ctx.fillRect(0, 0, size, size);
    tex.update();
    BabylonMuzzleFlash.flashTextureCache = tex;
    return tex;
  }

  private static getSmokeTexture(scene: Scene): DynamicTexture {
    if (BabylonMuzzleFlash.smokeTextureCache) return BabylonMuzzleFlash.smokeTextureCache;
    const tex = new DynamicTexture("muzzleSmokeTex", 256, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext();
    const size = 256;
    const cx = size / 2;
    const cy = size / 2;
    const blobs = [
      {x: cx, y: cy, r: 90},
      {x: cx - 30, y: cy - 20, r: 60},
      {x: cx + 35, y: cy + 15, r: 55},
      {x: cx + 10, y: cy - 35, r: 50},
      {x: cx - 25, y: cy + 30, r: 45},
      {x: cx + 30, y: cy - 25, r: 40},
      {x: cx - 15, y: cy - 10, r: 70},
    ];
    for (const blob of blobs) {
      const grad = ctx.createRadialGradient(blob.x, blob.y, 0, blob.x, blob.y, blob.r);
      grad.addColorStop(0, "rgba(200, 200, 195, 0.5)");
      grad.addColorStop(0.4, "rgba(180, 180, 175, 0.3)");
      grad.addColorStop(0.7, "rgba(150, 150, 148, 0.12)");
      grad.addColorStop(1.0, "rgba(120, 120, 120, 0)");
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, size, size);
    }
    tex.update();
    BabylonMuzzleFlash.smokeTextureCache = tex;
    return tex;
  }
}
