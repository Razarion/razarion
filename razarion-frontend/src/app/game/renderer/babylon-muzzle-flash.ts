import {Color4, DynamicTexture, ParticleSystem, Vector3} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";

export class BabylonMuzzleFlash {
  private static flashTextureCache: DynamicTexture | null = null;
  private static fireballTextureCache: DynamicTexture | null = null;
  private static smokeTextureCache: DynamicTexture | null = null;

  static fire(scene: Scene, muzzlePosition: Vector3, forward: Vector3): void {
    // === 1) FLASH — bright, instant, at muzzle ===
    const flash = new ParticleSystem("muzzleFlash", 20, scene);
    flash.particleTexture = BabylonMuzzleFlash.getFlashTexture(scene);
    flash.emitter = muzzlePosition.clone();
    flash.blendMode = ParticleSystem.BLENDMODE_ADD;

    flash.minEmitBox = new Vector3(-0.02, -0.02, -0.02);
    flash.maxEmitBox = new Vector3(0.02, 0.02, 0.02);

    flash.direction1 = forward.scale(0.5).add(new Vector3(-0.3, -0.3, -0.3));
    flash.direction2 = forward.scale(1.0).add(new Vector3(0.3, 0.3, 0.3));

    flash.minLifeTime = 0.05;
    flash.maxLifeTime = 0.15;
    flash.emitRate = 500;
    flash.targetStopDuration = 0.08;

    flash.minSize = 1.2;
    flash.maxSize = 2.5;
    flash.minEmitPower = 0.5;
    flash.maxEmitPower = 1.0;

    flash.addColorGradient(0, new Color4(1, 1, 0.9, 1));
    flash.addColorGradient(0.5, new Color4(1, 0.9, 0.5, 1));
    flash.addColorGradient(1.0, new Color4(1, 0.7, 0.2, 0));

    flash.disposeOnStop = true;
    flash.particleTexture!.dispose = () => {};
    flash.start();

    // === 2) FIREBALL — moves away from barrel ===
    const fireball = new ParticleSystem("muzzleFireball", 60, scene);
    fireball.particleTexture = BabylonMuzzleFlash.getFireballTexture(scene);
    fireball.emitter = muzzlePosition.clone();
    fireball.blendMode = ParticleSystem.BLENDMODE_ADD;

    fireball.minEmitBox = new Vector3(-0.03, -0.03, -0.03);
    fireball.maxEmitBox = new Vector3(0.03, 0.03, 0.03);

    fireball.direction1 = forward.scale(1.5).add(new Vector3(-0.1, -0.1, -0.1));
    fireball.direction2 = forward.scale(3.0).add(new Vector3(0.1, 0.1, 0.1));

    fireball.minLifeTime = 0.1;
    fireball.maxLifeTime = 0.3;
    fireball.emitRate = 400;
    fireball.targetStopDuration = 0.12;

    fireball.minSize = 0.3;
    fireball.maxSize = 0.8;
    fireball.minEmitPower = 2.0;
    fireball.maxEmitPower = 5.0;

    fireball.addColorGradient(0, new Color4(1, 1, 0.6, 1));
    fireball.addColorGradient(0.3, new Color4(1, 0.7, 0.1, 1));
    fireball.addColorGradient(0.7, new Color4(1, 0.3, 0.0, 0.8));
    fireball.addColorGradient(1.0, new Color4(0.5, 0.1, 0.0, 0));

    fireball.addSizeGradient(0, 0.3);
    fireball.addSizeGradient(0.5, 0.7);
    fireball.addSizeGradient(1.0, 0.1);

    fireball.gravity = new Vector3(0, -0.5, 0);
    fireball.disposeOnStop = true;
    fireball.particleTexture!.dispose = () => {};
    fireball.start();

    // === 3) SMOKE — slow, fading, drifts from muzzle ===
    const smoke = new ParticleSystem("muzzleSmoke", 40, scene);
    smoke.particleTexture = BabylonMuzzleFlash.getSmokeTexture(scene);
    smoke.emitter = muzzlePosition.clone();
    smoke.blendMode = ParticleSystem.BLENDMODE_STANDARD;

    smoke.minEmitBox = new Vector3(-0.05, -0.05, -0.05);
    smoke.maxEmitBox = new Vector3(0.05, 0.05, 0.05);

    smoke.direction1 = forward.scale(0.3).add(new Vector3(-0.15, 0.1, -0.15));
    smoke.direction2 = forward.scale(0.8).add(new Vector3(0.15, 0.4, 0.15));

    smoke.minLifeTime = 0.8;
    smoke.maxLifeTime = 2.0;
    smoke.emitRate = 150;
    smoke.targetStopDuration = 0.25;

    smoke.minSize = 0.2;
    smoke.maxSize = 0.6;
    smoke.minEmitPower = 0.3;
    smoke.maxEmitPower = 1.0;

    smoke.addColorGradient(0, new Color4(0.6, 0.6, 0.55, 0.4));
    smoke.addColorGradient(0.3, new Color4(0.5, 0.5, 0.48, 0.3));
    smoke.addColorGradient(0.7, new Color4(0.4, 0.4, 0.38, 0.15));
    smoke.addColorGradient(1.0, new Color4(0.3, 0.3, 0.3, 0));

    smoke.addSizeGradient(0, 0.2);
    smoke.addSizeGradient(0.5, 0.5);
    smoke.addSizeGradient(1.0, 0.8);

    smoke.gravity = new Vector3(0, 0.2, 0);
    smoke.disposeOnStop = true;
    smoke.particleTexture!.dispose = () => {};
    smoke.start();
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

  private static getFireballTexture(scene: Scene): DynamicTexture {
    if (BabylonMuzzleFlash.fireballTextureCache) return BabylonMuzzleFlash.fireballTextureCache;
    const tex = new DynamicTexture("muzzleFireballTex", 256, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext();
    const size = 256;
    const cx = size / 2;
    const cy = size / 2;
    const blobs = [
      {x: cx, y: cy, r: 100},
      {x: cx - 25, y: cy - 15, r: 65},
      {x: cx + 30, y: cy + 20, r: 60},
      {x: cx + 15, y: cy - 30, r: 50},
      {x: cx - 20, y: cy + 25, r: 55},
      {x: cx - 10, y: cy + 5, r: 75},
    ];
    for (const blob of blobs) {
      const grad = ctx.createRadialGradient(blob.x, blob.y, 0, blob.x, blob.y, blob.r);
      grad.addColorStop(0, "rgba(255, 255, 200, 0.7)");
      grad.addColorStop(0.25, "rgba(255, 200, 80, 0.6)");
      grad.addColorStop(0.5, "rgba(255, 120, 20, 0.4)");
      grad.addColorStop(0.75, "rgba(200, 50, 0, 0.15)");
      grad.addColorStop(1.0, "rgba(100, 20, 0, 0)");
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, size, size);
    }
    tex.update();
    BabylonMuzzleFlash.fireballTextureCache = tex;
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
