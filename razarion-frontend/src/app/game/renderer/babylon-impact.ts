import {Color4, DynamicTexture, ParticleSystem, Vector3} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";

export class BabylonImpact {
  private static sparkTextureCache: DynamicTexture | null = null;
  private static flashTextureCache: DynamicTexture | null = null;
  private static smokeTextureCache: DynamicTexture | null = null;
  private static trailSmokeTextureCache: DynamicTexture | null = null;

  static detonate(scene: Scene, position: Vector3): void {
    // === 1) FLASH — bright instant burst at impact point ===
    const flash = new ParticleSystem("impactFlash", 25, scene);
    flash.particleTexture = BabylonImpact.getFlashTexture(scene);
    flash.emitter = position.clone();
    flash.blendMode = ParticleSystem.BLENDMODE_ADD;

    flash.minEmitBox = new Vector3(-0.03, -0.03, -0.03);
    flash.maxEmitBox = new Vector3(0.03, 0.03, 0.03);

    flash.direction1 = new Vector3(-0.8, -0.8, -0.8);
    flash.direction2 = new Vector3(0.8, 0.8, 0.8);

    flash.minLifeTime = 0.04;
    flash.maxLifeTime = 0.12;
    flash.emitRate = 800;
    flash.targetStopDuration = 0.05;

    flash.minSize = 2.0;
    flash.maxSize = 4.0;
    flash.minEmitPower = 0.4;
    flash.maxEmitPower = 1.0;

    flash.addColorGradient(0, new Color4(1, 1, 0.9, 1));
    flash.addColorGradient(0.4, new Color4(1, 0.85, 0.4, 1));
    flash.addColorGradient(1.0, new Color4(1, 0.6, 0.1, 0));

    flash.gravity = new Vector3(0, -2.0, 0);
    flash.disposeOnStop = true;
    flash.particleTexture!.dispose = () => {};
    flash.start();

    // === 2) SPARKS — hot fragments flying outward and falling ===
    const sparks = new ParticleSystem("impactSparks", 80, scene);
    sparks.particleTexture = BabylonImpact.getSparkTexture(scene);
    sparks.emitter = position.clone();
    sparks.blendMode = ParticleSystem.BLENDMODE_ADD;

    sparks.minEmitBox = new Vector3(-0.08, -0.08, -0.08);
    sparks.maxEmitBox = new Vector3(0.08, 0.08, 0.08);

    sparks.direction1 = new Vector3(-1.8, 0.4, -1.8);
    sparks.direction2 = new Vector3(1.8, 3.5, 1.8);

    sparks.minLifeTime = 0.2;
    sparks.maxLifeTime = 0.7;
    sparks.emitRate = 0;
    sparks.manualEmitCount = 80;
    sparks.targetStopDuration = 0.01;

    sparks.minSize = 0.1;
    sparks.maxSize = 0.4;
    sparks.minEmitPower = 2.0;
    sparks.maxEmitPower = 5.5;

    sparks.addColorGradient(0, new Color4(1, 1, 0.7, 1));
    sparks.addColorGradient(0.3, new Color4(1, 0.6, 0.1, 1));
    sparks.addColorGradient(0.7, new Color4(0.8, 0.2, 0.0, 0.5));
    sparks.addColorGradient(1.0, new Color4(0.4, 0.1, 0.0, 0));

    sparks.gravity = new Vector3(0, -8.0, 0);
    sparks.minAngularSpeed = -Math.PI * 2;
    sparks.maxAngularSpeed = Math.PI * 2;
    sparks.disposeOnStop = true;
    sparks.particleTexture!.dispose = () => {};
    sparks.start();

    // === 3) FIREBALL — orange fire puff ===
    const fire = new ParticleSystem("impactFire", 40, scene);
    fire.particleTexture = BabylonImpact.getFlashTexture(scene);
    fire.emitter = position.clone();
    fire.blendMode = ParticleSystem.BLENDMODE_ADD;

    fire.minEmitBox = new Vector3(-0.08, -0.08, -0.08);
    fire.maxEmitBox = new Vector3(0.08, 0.08, 0.08);

    fire.direction1 = new Vector3(-1.5, -0.3, -1.5);
    fire.direction2 = new Vector3(1.5, 2, 1.5);

    fire.minLifeTime = 0.1;
    fire.maxLifeTime = 0.35;
    fire.emitRate = 400;
    fire.targetStopDuration = 0.12;

    fire.minSize = 0.8;
    fire.maxSize = 2.0;
    fire.minEmitPower = 1.0;
    fire.maxEmitPower = 3.0;

    fire.addColorGradient(0, new Color4(1, 0.9, 0.5, 1));
    fire.addColorGradient(0.3, new Color4(1, 0.5, 0.05, 0.9));
    fire.addColorGradient(0.7, new Color4(0.7, 0.15, 0.0, 0.4));
    fire.addColorGradient(1.0, new Color4(0.3, 0.05, 0.0, 0));

    fire.addSizeGradient(0, 0.3);
    fire.addSizeGradient(0.4, 1.0);
    fire.addSizeGradient(1.0, 0.2);

    fire.gravity = new Vector3(0, -1.5, 0);
    fire.disposeOnStop = true;
    fire.particleTexture!.dispose = () => {};
    fire.start();

    // === 4) SMOKE — drifting dark puff ===
    const smoke = new ParticleSystem("impactSmoke", 35, scene);
    smoke.particleTexture = BabylonImpact.getSmokeTexture(scene);
    smoke.emitter = position.clone();
    smoke.blendMode = ParticleSystem.BLENDMODE_STANDARD;

    smoke.minEmitBox = new Vector3(-0.15, -0.08, -0.15);
    smoke.maxEmitBox = new Vector3(0.15, 0.08, 0.15);

    smoke.direction1 = new Vector3(-0.5, 0.2, -0.5);
    smoke.direction2 = new Vector3(0.5, 1.5, 0.5);

    smoke.minLifeTime = 0.6;
    smoke.maxLifeTime = 1.8;
    smoke.emitRate = 80;
    smoke.targetStopDuration = 0.25;

    smoke.minSize = 0.5;
    smoke.maxSize = 1.2;
    smoke.minEmitPower = 0.3;
    smoke.maxEmitPower = 0.8;

    smoke.addColorGradient(0, new Color4(0.5, 0.45, 0.38, 0.5));
    smoke.addColorGradient(0.3, new Color4(0.4, 0.35, 0.3, 0.35));
    smoke.addColorGradient(0.7, new Color4(0.3, 0.27, 0.24, 0.15));
    smoke.addColorGradient(1.0, new Color4(0.2, 0.18, 0.16, 0));

    smoke.addSizeGradient(0, 0.3);
    smoke.addSizeGradient(0.4, 0.7);
    smoke.addSizeGradient(1.0, 1.0);

    smoke.gravity = new Vector3(0, -0.5, 0);
    smoke.disposeOnStop = true;
    smoke.particleTexture!.dispose = () => {};
    smoke.start();

    // === 5) SHRAPNEL — glowing debris chunks ===
    const shrapnel = new ParticleSystem("impactShrapnel", 12, scene);
    shrapnel.particleTexture = BabylonImpact.getSparkTexture(scene);
    shrapnel.emitter = position.clone();
    shrapnel.blendMode = ParticleSystem.BLENDMODE_ADD;

    shrapnel.minEmitBox = new Vector3(-0.05, -0.05, -0.05);
    shrapnel.maxEmitBox = new Vector3(0.05, 0.05, 0.05);

    shrapnel.direction1 = new Vector3(-2.0, 1.0, -2.0);
    shrapnel.direction2 = new Vector3(2.0, 4.0, 2.0);

    shrapnel.minLifeTime = 0.5;
    shrapnel.maxLifeTime = 1.2;
    shrapnel.emitRate = 0;
    shrapnel.manualEmitCount = 8;
    shrapnel.targetStopDuration = 0.01;

    shrapnel.minSize = 0.15;
    shrapnel.maxSize = 0.35;
    shrapnel.minEmitPower = 2.5;
    shrapnel.maxEmitPower = 5.0;

    shrapnel.addColorGradient(0, new Color4(1, 0.9, 0.5, 1));
    shrapnel.addColorGradient(0.4, new Color4(1, 0.5, 0.1, 0.9));
    shrapnel.addColorGradient(0.8, new Color4(0.5, 0.15, 0.0, 0.5));
    shrapnel.addColorGradient(1.0, new Color4(0.2, 0.05, 0.0, 0));

    shrapnel.gravity = new Vector3(0, -6.0, 0);
    shrapnel.minAngularSpeed = -Math.PI;
    shrapnel.maxAngularSpeed = Math.PI;
    shrapnel.disposeOnStop = true;
    shrapnel.particleTexture!.dispose = () => {};
    shrapnel.start();

    // === 6) SHRAPNEL SMOKE TRAILS — grey puffs following debris paths ===
    const trails = new ParticleSystem("shrapnelTrails", 60, scene);
    trails.particleTexture = BabylonImpact.getTrailSmokeTexture(scene);
    trails.emitter = position.clone();
    trails.blendMode = ParticleSystem.BLENDMODE_STANDARD;

    trails.minEmitBox = new Vector3(-0.05, -0.05, -0.05);
    trails.maxEmitBox = new Vector3(0.05, 0.05, 0.05);

    // Same directions as shrapnel but slower — fills in the path behind
    trails.direction1 = new Vector3(-1.8, 0.8, -1.8);
    trails.direction2 = new Vector3(1.8, 3.5, 1.8);

    trails.minLifeTime = 0.4;
    trails.maxLifeTime = 1.0;
    trails.emitRate = 200;
    trails.targetStopDuration = 0.3;

    trails.minSize = 0.15;
    trails.maxSize = 0.45;
    trails.minEmitPower = 1.0;
    trails.maxEmitPower = 3.0;

    trails.addColorGradient(0, new Color4(0.55, 0.55, 0.55, 0.5));
    trails.addColorGradient(0.3, new Color4(0.4, 0.4, 0.4, 0.35));
    trails.addColorGradient(0.7, new Color4(0.2, 0.2, 0.2, 0.15));
    trails.addColorGradient(1.0, new Color4(0.05, 0.05, 0.05, 0));

    trails.addSizeGradient(0, 0.2);
    trails.addSizeGradient(0.5, 0.6);
    trails.addSizeGradient(1.0, 0.9);

    trails.gravity = new Vector3(0, -3.0, 0);
    trails.disposeOnStop = true;
    trails.particleTexture!.dispose = () => {};
    trails.start();
  }

  // --- Texture generation (cached) ---

  private static getFlashTexture(scene: Scene): DynamicTexture {
    if (BabylonImpact.flashTextureCache) return BabylonImpact.flashTextureCache;
    const tex = new DynamicTexture("impactFlashTex", 128, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext();
    const size = 128;
    const cx = size / 2;
    const cy = size / 2;

    const steps = 32;
    const radii: number[] = [];
    for (let i = 0; i < steps; i++) radii.push(25 + Math.random() * 30);
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
    const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, 60);
    grad.addColorStop(0, "rgba(255, 255, 230, 0.95)");
    grad.addColorStop(0.2, "rgba(255, 240, 160, 0.7)");
    grad.addColorStop(0.45, "rgba(255, 200, 80, 0.35)");
    grad.addColorStop(0.7, "rgba(255, 150, 30, 0.1)");
    grad.addColorStop(1.0, "rgba(200, 100, 10, 0)");
    ctx.fillStyle = grad;
    ctx.fill();

    tex.update();
    BabylonImpact.flashTextureCache = tex;
    return tex;
  }

  private static getSparkTexture(scene: Scene): DynamicTexture {
    if (BabylonImpact.sparkTextureCache) return BabylonImpact.sparkTextureCache;
    const tex = new DynamicTexture("impactSparkTex", 32, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext();
    const size = 32;
    const cx = size / 2;
    const cy = size / 2;

    ctx.clearRect(0, 0, size, size);
    const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, size * 0.4);
    grad.addColorStop(0, "rgba(255, 255, 230, 1)");
    grad.addColorStop(0.3, "rgba(255, 210, 100, 0.8)");
    grad.addColorStop(0.6, "rgba(255, 140, 20, 0.4)");
    grad.addColorStop(1.0, "rgba(200, 80, 0, 0)");
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, size, size);

    tex.update();
    BabylonImpact.sparkTextureCache = tex;
    return tex;
  }

  private static getSmokeTexture(scene: Scene): DynamicTexture {
    if (BabylonImpact.smokeTextureCache) return BabylonImpact.smokeTextureCache;
    const tex = new DynamicTexture("impactSmokeTex", 128, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext();
    const size = 128;
    const cx = size / 2;
    const cy = size / 2;

    const blobs = [
      {x: cx, y: cy, r: 50},
      {x: cx - 15, y: cy - 10, r: 35},
      {x: cx + 18, y: cy + 12, r: 30},
      {x: cx + 8, y: cy - 18, r: 28},
      {x: cx - 12, y: cy + 15, r: 32},
    ];
    for (const blob of blobs) {
      const grad = ctx.createRadialGradient(blob.x, blob.y, 0, blob.x, blob.y, blob.r);
      grad.addColorStop(0, "rgba(120, 110, 100, 0.5)");
      grad.addColorStop(0.4, "rgba(90, 85, 80, 0.3)");
      grad.addColorStop(0.7, "rgba(60, 58, 55, 0.12)");
      grad.addColorStop(1.0, "rgba(30, 28, 26, 0)");
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, size, size);
    }

    tex.update();
    BabylonImpact.smokeTextureCache = tex;
    return tex;
  }

  private static getTrailSmokeTexture(scene: Scene): DynamicTexture {
    if (BabylonImpact.trailSmokeTextureCache) return BabylonImpact.trailSmokeTextureCache;
    const tex = new DynamicTexture("impactTrailSmokeTex", 64, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext();
    const size = 64;
    const cx = size / 2;
    const cy = size / 2;

    ctx.clearRect(0, 0, size, size);
    const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, size * 0.45);
    grad.addColorStop(0, "rgba(160, 160, 160, 0.6)");
    grad.addColorStop(0.3, "rgba(120, 120, 120, 0.4)");
    grad.addColorStop(0.6, "rgba(80, 80, 80, 0.15)");
    grad.addColorStop(1.0, "rgba(40, 40, 40, 0)");
    ctx.fillStyle = grad;
    ctx.beginPath();
    ctx.arc(cx, cy, size * 0.45, 0, Math.PI * 2);
    ctx.fill();

    tex.update();
    BabylonImpact.trailSmokeTextureCache = tex;
    return tex;
  }
}
