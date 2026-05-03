import {Color3, Color4, DynamicTexture, MeshBuilder, ParticleSystem, StandardMaterial, Vector3} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";
import {BabylonSpriteSheetFireball} from "./babylon-sprite-sheet-fireball";

export class BabylonImpact {
  private static lightBurstTextureCache: DynamicTexture | null = null;
  private static smokeTextureCache: DynamicTexture | null = null;
  private static trailSmokeTextureCache: DynamicTexture | null = null;
  private static debrisTextureCache: DynamicTexture | null = null;

  static detonate(scene: Scene, position: Vector3): void {
    BabylonImpact.spawnFlashOnly(scene, position);
    BabylonImpact.spawnFireballOnly(scene, position);
    BabylonImpact.spawnSmokeOnly(scene, position);
    BabylonImpact.spawnShrapnelTrailsOnly(scene, position);
    BabylonImpact.spawnDebris(scene, position);
    BabylonImpact.spawnShockwave(scene, position);
  }

  /**
   * Force-runs the entire impact pipeline (six particle systems + shockwave mesh/material) at
   * a hidden Y=-1000 position so the first user-visible impact doesn't render blank while
   * shaders compile and textures upload. Call once at scene init.
   */
  static preWarm(scene: Scene): void {
    BabylonImpact.detonate(scene, new Vector3(0, -1000, 0));
  }

  // TEMP helper: only the shrapnel trails, for isolated tuning
  private static spawnShrapnelTrailsOnly(scene: Scene, position: Vector3): void {
    const trails = new ParticleSystem("shrapnelTrails", 100, scene);
    trails.particleTexture = BabylonImpact.getTrailSmokeTexture(scene);
    trails.emitter = position.clone();
    trails.blendMode = ParticleSystem.BLENDMODE_STANDARD;

    trails.minEmitBox = new Vector3(-0.05, -0.05, -0.05);
    trails.maxEmitBox = new Vector3(0.05, 0.05, 0.05);

    // Same directions as shrapnel but slower — fills in the path behind.
    trails.direction1 = new Vector3(-1.8, 0.8, -1.8);
    trails.direction2 = new Vector3(1.8, 3.5, 1.8);

    trails.minLifeTime = 0.5;
    trails.maxLifeTime = 1.1;
    trails.emitRate = 450;
    trails.targetStopDuration = 0.5;

    trails.minEmitPower = 2.0;
    trails.maxEmitPower = 6.0;

    // Start nearly white + fully opaque so the trail reads strongly even against bright scenes.
    trails.addColorGradient(0, new Color4(0.95, 0.93, 0.90, 1.0));
    trails.addColorGradient(0.25, new Color4(0.80, 0.78, 0.75, 0.95));
    trails.addColorGradient(0.60, new Color4(0.55, 0.52, 0.48, 0.65));
    trails.addColorGradient(0.90, new Color4(0.30, 0.28, 0.25, 0.25));
    trails.addColorGradient(1.00, new Color4(0.15, 0.13, 0.12, 0));

    // NOTE: addSizeGradient values are ABSOLUTE sizes and override minSize/maxSize entirely.
    trails.addSizeGradient(0, 1.0);
    trails.addSizeGradient(0.5, 2.2);
    trails.addSizeGradient(1.0, 3.2);

    trails.gravity = new Vector3(0, -1.5, 0);
    trails.disposeOnStop = true;
    trails.particleTexture!.dispose = () => {};
    trails.start();
  }

  // TEMP helper: only the smoke, for isolated tuning
  private static spawnSmokeOnly(scene: Scene, position: Vector3): void {
    const smoke = new ParticleSystem("impactSmoke", 60, scene);
    smoke.particleTexture = BabylonImpact.getSmokeTexture(scene);
    smoke.emitter = position.clone();
    smoke.blendMode = ParticleSystem.BLENDMODE_STANDARD;

    smoke.minEmitBox = new Vector3(-0.2, -0.05, -0.2);
    smoke.maxEmitBox = new Vector3(0.2, 0.1, 0.2);

    // Strongly upward drift with slight lateral spread so it billows as it rises.
    smoke.direction1 = new Vector3(-0.3, 1.8, -0.3);
    smoke.direction2 = new Vector3(0.3, 3.5, 0.3);

    smoke.minLifeTime = 1.8;
    smoke.maxLifeTime = 3.5;
    smoke.emitRate = 100;
    smoke.targetStopDuration = 0.35;

    smoke.minEmitPower = 0.8;
    smoke.maxEmitPower = 1.8;

    // Random rotation so the wispy texture doesn't read as repeating sprites.
    smoke.minInitialRotation = 0;
    smoke.maxInitialRotation = Math.PI * 2;
    smoke.minAngularSpeed = -0.4;
    smoke.maxAngularSpeed = 0.4;

    smoke.addColorGradient(0, new Color4(0.55, 0.5, 0.45, 0.7));
    smoke.addColorGradient(0.3, new Color4(0.45, 0.42, 0.38, 0.55));
    smoke.addColorGradient(0.7, new Color4(0.35, 0.32, 0.3, 0.3));
    smoke.addColorGradient(1.0, new Color4(0.25, 0.22, 0.2, 0));

    // NOTE: addSizeGradient values are ABSOLUTE sizes and override minSize/maxSize entirely.
    // Smoke billows outward and upward as it rises.
    smoke.addSizeGradient(0, 1.2);
    smoke.addSizeGradient(0.4, 3.0);
    smoke.addSizeGradient(1.0, 5.0);

    // Positive Y = buoyancy: smoke accelerates upward as it heats/thins.
    smoke.gravity = new Vector3(0, 1.2, 0);
    smoke.disposeOnStop = true;
    smoke.particleTexture!.dispose = () => {};
    smoke.start();
  }

  // TEMP helper: only the flash, for isolated tuning
  // Flash = pure white light pop. Distinct from fireball by using a clean round light texture,
  // pure-white colors, huge starting size that shrinks fast (light pulse, not fire).
  private static spawnFlashOnly(scene: Scene, position: Vector3): void {
    const flash = new ParticleSystem("impactFlash", 8, scene);
    flash.particleTexture = BabylonImpact.getLightBurstTexture(scene);
    flash.emitter = position.clone();
    flash.blendMode = ParticleSystem.BLENDMODE_ADD;

    flash.minEmitBox = new Vector3(0, 0, 0);
    flash.maxEmitBox = new Vector3(0, 0, 0);

    flash.direction1 = new Vector3(0, 0, 0);
    flash.direction2 = new Vector3(0, 0, 0);

    flash.minLifeTime = 0.12;
    flash.maxLifeTime = 0.18;
    flash.emitRate = 0;
    flash.manualEmitCount = 6;
    flash.targetStopDuration = 0.01;

    flash.minEmitPower = 0;
    flash.maxEmitPower = 0;

    // Pure white light core → slight warm tint → fade. No orange/red fire colors.
    flash.addColorGradient(0.00, new Color4(1.0, 1.0, 1.0, 1));
    flash.addColorGradient(0.40, new Color4(1.0, 0.98, 0.90, 0.85));
    flash.addColorGradient(1.00, new Color4(1.0, 0.95, 0.80, 0));

    // Huge at birth, shrinks fast — reads as a single bright pop, not a growing fireball.
    flash.addSizeGradient(0.0, 12.0);
    flash.addSizeGradient(0.3, 8.0);
    flash.addSizeGradient(1.0, 3.0);

    flash.gravity = new Vector3(0, 0, 0);
    flash.disposeOnStop = true;
    flash.particleTexture!.dispose = () => {};
    flash.start();
  }

  // Sprite-sheet fireball at the impact point — same 64-frame WebP that BabylonExplosion
  // uses for buildings, but smaller (size ~1.5–3 vs 6–10), fewer particles (4 vs 10),
  // shorter lifetime, and only mild upward drift (it's a projectile hit, not a building
  // detonation).
  private static spawnFireballOnly(scene: Scene, position: Vector3): void {
    BabylonSpriteSheetFireball.spawn(scene, position, {
      capacity: 4,
      sphereRadius: 0.2,
      minSize: 4.5,
      maxSize: 9.0,
      minLifeTime: 0.5,
      maxLifeTime: 0.8,
      direction1: new Vector3(-0.3, 8, -0.3),
      direction2: new Vector3(0.3, 14, 0.3),
      minEmitPower: 1.0,
      maxEmitPower: 1.0,
      gravity: new Vector3(0, 4.0, 0),
      namePrefix: "impactFireball",
    });
  }

  // === 8) SHOCKWAVE SPHERE — 3D pressure bubble expanding from the impact point ===
  private static spawnShockwave(scene: Scene, position: Vector3): void {
    const sphere = MeshBuilder.CreateSphere("impactShockwave", {diameter: 0.8, segments: 24}, scene);
    sphere.position = position.clone();
    sphere.isPickable = false;

    const mat = new StandardMaterial("impactShockwaveMat", scene);
    mat.emissiveColor = new Color3(1, 0.85, 0.55);
    mat.diffuseColor = new Color3(0, 0, 0);
    mat.specularColor = new Color3(0, 0, 0);
    mat.disableLighting = true;
    // backFaceCulling kept ON so we see a clean outer shell, not a double-shaded interior blob.
    mat.backFaceCulling = true;
    mat.alpha = 0.85;
    mat.wireframe = false;
    sphere.material = mat;
    sphere.renderingGroupId = 1;

    const durationMs = 420;
    const startScale = 1.0;
    const endScale = 12.0;
    const t0 = performance.now();
    const onBeforeRender = () => {
      const t = Math.min((performance.now() - t0) / durationMs, 1);
      const scale = startScale + (endScale - startScale) * t;
      sphere.scaling.setAll(scale);
      // Alpha fades faster than the sphere grows — shell thins out as it expands.
      mat.alpha = 0.85 * (1 - t) * (1 - t);
      if (t >= 1) {
        scene.onBeforeRenderObservable.removeCallback(onBeforeRender);
        sphere.dispose();
        mat.dispose();
      }
    };
    scene.onBeforeRenderObservable.add(onBeforeRender);
  }

  // === 7) DEBRIS CHUNKS — opaque flying parts that tumble and fall ===
  private static spawnDebris(scene: Scene, position: Vector3): void {
    const debris = new ParticleSystem("impactDebris", 24, scene);
    debris.particleTexture = BabylonImpact.getDebrisTexture(scene);
    debris.emitter = position.clone();
    debris.blendMode = ParticleSystem.BLENDMODE_STANDARD;
    // Render after the additive flash/fire layers so the opaque chunks aren't washed out.
    debris.renderingGroupId = 1;

    debris.minEmitBox = new Vector3(-0.08, -0.08, -0.08);
    debris.maxEmitBox = new Vector3(0.08, 0.08, 0.08);

    debris.direction1 = new Vector3(-3.0, 1.5, -3.0);
    debris.direction2 = new Vector3(3.0, 6.0, 3.0);

    debris.minLifeTime = 0.9;
    debris.maxLifeTime = 1.8;
    debris.emitRate = 0;
    debris.manualEmitCount = 20;
    debris.targetStopDuration = 0.01;

    debris.minSize = 0.25;
    debris.maxSize = 0.55;
    debris.minEmitPower = 3.0;
    debris.maxEmitPower = 7.0;

    debris.minAngularSpeed = -12;
    debris.maxAngularSpeed = 12;

    debris.addColorGradient(0.00, new Color4(0.85, 0.85, 0.85, 1.0));
    debris.addColorGradient(0.60, new Color4(0.70, 0.70, 0.70, 1.0));
    debris.addColorGradient(0.90, new Color4(0.55, 0.55, 0.55, 0.8));
    debris.addColorGradient(1.00, new Color4(0.40, 0.40, 0.40, 0.0));

    debris.gravity = new Vector3(0, -25, 0);
    debris.disposeOnStop = true;
    debris.particleTexture!.dispose = () => {};
    // Slight delay so debris emerges AFTER the initial bright flash/sparks burst.
    setTimeout(() => debris.start(), 90);
  }

  // --- Texture generation (cached) ---

  // Clean round light burst — pure bright core, smooth falloff.
  private static getLightBurstTexture(scene: Scene): DynamicTexture {
    if (BabylonImpact.lightBurstTextureCache) return BabylonImpact.lightBurstTextureCache;
    const tex = new DynamicTexture("impactLightBurstTex", 128, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext();
    const size = 128;
    const cx = size / 2;
    const cy = size / 2;

    ctx.clearRect(0, 0, size, size);
    const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, size * 0.5);
    grad.addColorStop(0.00, "rgba(255, 255, 255, 1.0)");
    grad.addColorStop(0.15, "rgba(255, 255, 250, 0.85)");
    grad.addColorStop(0.40, "rgba(255, 250, 230, 0.45)");
    grad.addColorStop(0.75, "rgba(255, 240, 200, 0.12)");
    grad.addColorStop(1.00, "rgba(255, 230, 180, 0)");
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, size, size);

    tex.update();
    BabylonImpact.lightBurstTextureCache = tex;
    return tex;
  }

  private static getSmokeTexture(scene: Scene): DynamicTexture {
    if (BabylonImpact.smokeTextureCache) return BabylonImpact.smokeTextureCache;
    const size = 256;
    const tex = new DynamicTexture("impactSmokeTex", size, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext() as unknown as CanvasRenderingContext2D;
    const cx = size / 2;
    const cy = size / 2;

    ctx.clearRect(0, 0, size, size);

    // Layer 1 — main volumetric body: 14 overlapping blobs of varying size clustered near centre.
    const mainBlobs = 14;
    for (let i = 0; i < mainBlobs; i++) {
      const angle = Math.random() * Math.PI * 2;
      const dist = Math.random() * size * 0.28;
      const bx = cx + Math.cos(angle) * dist;
      const by = cy + Math.sin(angle) * dist;
      const r = size * (0.12 + Math.random() * 0.20);
      const greyBase = 130 + Math.floor(Math.random() * 60); // grey tones, slight variance
      const grad = ctx.createRadialGradient(bx, by, 0, bx, by, r);
      grad.addColorStop(0.00, `rgba(${greyBase}, ${greyBase - 5}, ${greyBase - 10}, 0.55)`);
      grad.addColorStop(0.35, `rgba(${greyBase - 20}, ${greyBase - 25}, ${greyBase - 30}, 0.30)`);
      grad.addColorStop(0.70, `rgba(${greyBase - 50}, ${greyBase - 55}, ${greyBase - 60}, 0.10)`);
      grad.addColorStop(1.00, "rgba(40, 38, 36, 0)");
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, size, size);
    }

    // Layer 2 — smaller wisps at the edges to make the silhouette ragged, not a clean circle.
    const wisps = 18;
    for (let i = 0; i < wisps; i++) {
      const angle = Math.random() * Math.PI * 2;
      const dist = size * (0.22 + Math.random() * 0.20); // biased outward
      const wx = cx + Math.cos(angle) * dist;
      const wy = cy + Math.sin(angle) * dist;
      const r = size * (0.05 + Math.random() * 0.10);
      const greyBase = 110 + Math.floor(Math.random() * 50);
      const grad = ctx.createRadialGradient(wx, wy, 0, wx, wy, r);
      grad.addColorStop(0.00, `rgba(${greyBase}, ${greyBase - 5}, ${greyBase - 10}, 0.35)`);
      grad.addColorStop(0.50, `rgba(${greyBase - 25}, ${greyBase - 30}, ${greyBase - 35}, 0.15)`);
      grad.addColorStop(1.00, "rgba(30, 28, 26, 0)");
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, size, size);
    }

    // Layer 3 — bright highlight spots for volumetric lift (simulates light scattering through thin smoke).
    for (let i = 0; i < 5; i++) {
      const hx = cx + (Math.random() - 0.5) * size * 0.35;
      const hy = cy + (Math.random() - 0.5) * size * 0.35;
      const r = size * (0.08 + Math.random() * 0.08);
      const grad = ctx.createRadialGradient(hx, hy, 0, hx, hy, r);
      grad.addColorStop(0.00, "rgba(210, 205, 200, 0.20)");
      grad.addColorStop(1.00, "rgba(200, 195, 190, 0)");
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, size, size);
    }

    // Layer 4 — overall soft radial mask to fade the whole texture near the edges (no hard square).
    const mask = ctx.createRadialGradient(cx, cy, size * 0.25, cx, cy, size * 0.5);
    mask.addColorStop(0, "rgba(0, 0, 0, 0)");
    mask.addColorStop(1, "rgba(0, 0, 0, 1)");
    ctx.globalCompositeOperation = "destination-in";
    ctx.fillStyle = "rgba(0, 0, 0, 1)";
    ctx.fillRect(0, 0, size, size);
    ctx.fillStyle = mask;
    ctx.fillRect(0, 0, size, size);
    ctx.globalCompositeOperation = "source-over";

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

  private static getDebrisTexture(scene: Scene): DynamicTexture {
    if (BabylonImpact.debrisTextureCache) return BabylonImpact.debrisTextureCache;
    const size = 64;
    const tex = new DynamicTexture("impactDebrisTex", size, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext() as unknown as CanvasRenderingContext2D;

    ctx.clearRect(0, 0, size, size);

    // --- 1) Sharp jagged silhouette — irregular 14-gon with strong radius variation ---
    const cx = size / 2;
    const cy = size / 2;
    const steps = 14;
    const vertices: { x: number; y: number }[] = [];
    for (let i = 0; i < steps; i++) {
      const angle = (i / steps) * Math.PI * 2 + (Math.random() - 0.5) * 0.25;
      const radius = size * 0.30 * (0.55 + Math.random() * 0.55);
      vertices.push({x: cx + Math.cos(angle) * radius, y: cy + Math.sin(angle) * radius});
    }
    ctx.beginPath();
    vertices.forEach((v, i) => i === 0 ? ctx.moveTo(v.x, v.y) : ctx.lineTo(v.x, v.y));
    ctx.closePath();

    ctx.fillStyle = "rgba(200, 200, 200, 1.0)";
    ctx.fill();

    ctx.save();
    ctx.clip();

    // --- 2) Mottled grey patches for rock-like tonal variation ---
    const patchGreys = [185, 195, 205, 215, 225, 175, 210];
    for (let i = 0; i < 9; i++) {
      const px = Math.random() * size;
      const py = Math.random() * size;
      const pr = size * (0.15 + Math.random() * 0.25);
      const grey = patchGreys[Math.floor(Math.random() * patchGreys.length)];
      const patch = ctx.createRadialGradient(px, py, 0, px, py, pr);
      patch.addColorStop(0.0, `rgba(${grey}, ${grey}, ${grey}, 0.55)`);
      patch.addColorStop(1.0, `rgba(${grey}, ${grey}, ${grey}, 0)`);
      ctx.fillStyle = patch;
      ctx.fillRect(0, 0, size, size);
    }

    // --- 3) Directional diagonal shading: bright top-left, shadow bottom-right ---
    const shade = ctx.createLinearGradient(cx - size * 0.4, cy - size * 0.4, cx + size * 0.35, cy + size * 0.35);
    shade.addColorStop(0.0, "rgba(255, 255, 255, 0.35)");
    shade.addColorStop(0.5, "rgba(200, 200, 200, 0.0)");
    shade.addColorStop(1.0, "rgba(90, 90, 90, 0.55)");
    ctx.fillStyle = shade;
    ctx.fillRect(0, 0, size, size);

    // --- 3) Speckle noise — 120 small dots at varied lightness for stone grain ---
    for (let i = 0; i < 120; i++) {
      const x = Math.random() * size;
      const y = Math.random() * size;
      const brightness = Math.random() < 0.5 ? 60 + Math.random() * 40 : 180 + Math.random() * 50;
      ctx.fillStyle = `rgba(${brightness | 0}, ${brightness | 0}, ${brightness | 0}, ${0.25 + Math.random() * 0.35})`;
      ctx.fillRect(x, y, 1, 1);
    }

    // --- 4) Jagged crack lines across the chunk ---
    ctx.strokeStyle = "rgba(60, 60, 60, 0.7)";
    ctx.lineWidth = 1.2;
    for (let c = 0; c < 2; c++) {
      ctx.beginPath();
      const x0 = 8 + Math.random() * (size - 16);
      const y0 = 8 + Math.random() * (size - 16);
      ctx.moveTo(x0, y0);
      let x = x0, y = y0;
      const segments = 3 + Math.floor(Math.random() * 2);
      for (let s = 0; s < segments; s++) {
        x += (Math.random() - 0.5) * size * 0.45;
        y += (Math.random() - 0.5) * size * 0.45;
        ctx.lineTo(x, y);
      }
      ctx.stroke();
    }

    // --- 5) Bright rim highlight along the outline to read as a faceted edge ---
    ctx.strokeStyle = "rgba(255, 255, 255, 0.55)";
    ctx.lineWidth = 1.0;
    ctx.beginPath();
    vertices.forEach((v, i) => i === 0 ? ctx.moveTo(v.x, v.y) : ctx.lineTo(v.x, v.y));
    ctx.closePath();
    ctx.stroke();

    ctx.restore();

    tex.update();
    BabylonImpact.debrisTextureCache = tex;
    return tex;
  }
}
