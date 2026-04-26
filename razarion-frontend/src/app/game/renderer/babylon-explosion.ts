import {
  Color3,
  Color4,
  MeshBuilder,
  ParticleSystem,
  SphereParticleEmitter,
  StandardMaterial,
  Texture,
  Vector3,
} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";

// Building explosion effect — extracted from the data-driven "Explosion" particle system
// (id 5, NodeParticleSystemSet, blocks 5301–5361) into a procedural class with all
// visual components:
//
//   1) Fireball  — 8x8 sprite-sheet animated (64 frames, fire→smoke sequence)
//                  driven by the WebP sheet in public/renderer/textures/explosion-texture.webp
//   2) Debris    — tumbling earth chunks thrown outward, strong gravity
//                  (public/renderer/textures/explosion-debris.png)
//   3) Shockwave — 3D expanding sphere (same component as BabylonImpact.spawnShockwave,
//                  scaled up for building-sized detonation)
//
// Tuning values mirror the source NodeParticleSystemSet inputs (emit power / lifetime / size / etc.).
export class BabylonExplosion {
  private static readonly SHEET_URL = "renderer/textures/explosion-texture.webp";
  private static readonly DEBRIS_URL = "renderer/textures/explosion-debris.png";
  private static readonly SHEET_COLS = 8;
  private static readonly SHEET_ROWS = 8;
  private static readonly SHEET_FRAMES = 64;
  private static readonly SHEET_CELL = 128;

  // Textures loaded once and reused. Both textures are ~500KB combined — caching matters.
  private static fireballTextureCache: Texture | null = null;
  private static debrisTextureCache: Texture | null = null;

  static detonate(scene: Scene, position: Vector3): void {
    BabylonExplosion.spawnFireball(scene, position);
    BabylonExplosion.spawnDebris(scene, position);
    BabylonExplosion.spawnShockwave(scene, position);
  }

  // Sprite-sheet-animated fireball puffs that rise upward from the impact point.
  // Each particle plays the full 64-frame animation once during its lifetime.
  private static spawnFireball(scene: Scene, position: Vector3): void {
    const capacity = 10;
    const fire = new ParticleSystem("explosionFireball", capacity, scene, null, true);
    fire.particleTexture = BabylonExplosion.getFireballTexture(scene);
    fire.emitter = position.clone();
    fire.blendMode = ParticleSystem.BLENDMODE_STANDARD;

    // Small sphere emitter — slight lateral spread so puffs don't stack on a perfect line.
    const emitter = new SphereParticleEmitter(0.6);
    fire.particleEmitterType = emitter;

    // We compute cellIndex ourselves (in the updateFunction wrapper below), so disable
    // Babylon's automatic progression: changeSpeed=0 keeps updateCellIndex from advancing
    // the frame on its own. startSpriteCellID is irrelevant once we override cellIndex
    // every frame, but we still configure cell dimensions so the shader can sample.
    fire.startSpriteCellID = 0;
    fire.endSpriteCellID = BabylonExplosion.SHEET_FRAMES - 1;
    fire.spriteCellWidth = BabylonExplosion.SHEET_CELL;
    fire.spriteCellHeight = BabylonExplosion.SHEET_CELL;
    fire.spriteCellChangeSpeed = 0;
    fire.spriteCellLoop = false;
    fire.spriteRandomStartCell = false;

    fire.emitRate = 0;
    fire.manualEmitCount = capacity;
    fire.targetStopDuration = 0.05;

    // 1 second lifetime — ~16 ms per frame (roughly one frame per render tick at 60fps).
    fire.minLifeTime = 1.0;
    fire.maxLifeTime = 1.4;

    // Slight size variation so the puffs read as distinct.
    fire.minSize = 6.0;
    fire.maxSize = 10.0;

    // Emit upward with a narrower cone — puffs shoot higher with less lateral drift.
    fire.direction1 = new Vector3(-1, 20, -1);
    fire.direction2 = new Vector3(1, 30, 1);
    fire.minEmitPower = 1.0;
    fire.maxEmitPower = 1.0;

    // Slight tumble so the billboarded puffs don't feel static.
    fire.minAngularSpeed = -Math.PI * 0.3;
    fire.maxAngularSpeed = Math.PI * 0.3;
    fire.minInitialRotation = 0;
    fire.maxInitialRotation = Math.PI * 2;

    // Strong upward buoyancy — keeps accelerating the puffs vertically as they age.
    fire.gravity = new Vector3(0, 6.0, 0);

    fire.color1 = new Color4(1, 1, 1, 1);
    fire.color2 = new Color4(1, 1, 1, 1);
    fire.colorDead = new Color4(1, 1, 1, 1);

    // Fully deterministic cell progression — we ignore whatever Babylon's own
    // updateCellIndex computed and compute the frame from age/lifeTime ourselves.
    // This avoids edge cases (cellIndex=64 overshoot at ratio=1, float fractional cellIndex,
    // double invocation, etc.) and gives us a clean row-flipped index every frame.
    const total = BabylonExplosion.SHEET_FRAMES;
    const defaultUpdate = fire.updateFunction;
    fire.updateFunction = function (particles) {
      defaultUpdate.call(this, particles);
      for (const p of particles) {
        // ratio clamped just below 1 so the last cell is fully reached without overshoot.
        const ratio = Math.max(0, Math.min(p.age / p.lifeTime, 0.99999));
        const naturalCell = (ratio * total) | 0;
        p.cellIndex = BabylonExplosion.remapCellIndex(naturalCell);
      }
    };

    fire.disposeOnStop = true;
    fire.start();
  }

  // === Debris: brown earth chunks thrown outward, fall under gravity ===
  private static spawnDebris(scene: Scene, position: Vector3): void {
    const capacity = 60;
    const debris = new ParticleSystem("explosionDebris", capacity, scene);
    debris.particleTexture = BabylonExplosion.getDebrisTexture(scene);
    debris.emitter = position.clone();
    debris.blendMode = ParticleSystem.BLENDMODE_STANDARD;
    debris.renderingGroupId = 1;

    const emitter = new SphereParticleEmitter(0.3);
    debris.particleEmitterType = emitter;

    debris.emitRate = 0;
    debris.manualEmitCount = capacity;
    debris.targetStopDuration = 0.05;

    debris.minLifeTime = 1.0;
    debris.maxLifeTime = 3.0;

    debris.minSize = 0.3;
    debris.maxSize = 1.2;

    debris.minEmitPower = 15.0;
    debris.maxEmitPower = 40.0;

    // Source colors: [0.4, 0.35, 0.25, 1] → [0.2, 0.15, 0.1, 0]
    debris.color1 = new Color4(0.4, 0.35, 0.25, 1);
    debris.color2 = new Color4(0.4, 0.35, 0.25, 1);
    debris.colorDead = new Color4(0.2, 0.15, 0.1, 0);

    debris.minAngularSpeed = -Math.PI * 2;
    debris.maxAngularSpeed = Math.PI * 2;
    debris.minInitialRotation = 0;
    debris.maxInitialRotation = Math.PI * 2;

    // Strong gravity — debris arcs and slams down fast.
    debris.gravity = new Vector3(0, -15, 0);

    debris.disposeOnStop = true;
    debris.start();
  }

  // === Shockwave: expanding 3D sphere (same style as BabylonImpact, larger for a building) ===
  private static spawnShockwave(scene: Scene, position: Vector3): void {
    const sphere = MeshBuilder.CreateSphere(
      "explosionShockwave",
      {diameter: 1.0, segments: 24},
      scene,
    );
    sphere.position = position.clone();
    sphere.isPickable = false;

    const mat = new StandardMaterial("explosionShockwaveMat", scene);
    mat.emissiveColor = new Color3(1.0, 0.75, 0.35);
    mat.diffuseColor = new Color3(0, 0, 0);
    mat.specularColor = new Color3(0, 0, 0);
    mat.disableLighting = true;
    mat.backFaceCulling = true;
    mat.alpha = 0.9;
    sphere.material = mat;
    sphere.renderingGroupId = 1;

    const durationMs = 600;
    const startScale = 1.0;
    const endScale = 20.0;
    const t0 = performance.now();
    const onBeforeRender = () => {
      const t = Math.min((performance.now() - t0) / durationMs, 1);
      const scale = startScale + (endScale - startScale) * t;
      sphere.scaling.setAll(scale);
      mat.alpha = 0.9 * (1 - t) * (1 - t);
      if (t >= 1) {
        scene.onBeforeRenderObservable.removeCallback(onBeforeRender);
        sphere.dispose();
        mat.dispose();
      }
    };
    scene.onBeforeRenderObservable.add(onBeforeRender);
  }

  // --- Texture loading (cached) ---

  private static getFireballTexture(scene: Scene): Texture {
    if (BabylonExplosion.fireballTextureCache) return BabylonExplosion.fireballTextureCache;
    const tex = new Texture(BabylonExplosion.SHEET_URL, scene, true, false, Texture.TRILINEAR_SAMPLINGMODE);
    tex.hasAlpha = true;
    // ParticleSystem.dispose() defaults to disposeTexture=true. With disposeOnStop=true
    // that kills the cached texture after the first system's particles die — the second
    // explosion then tries to render with a disposed texture and disappears. Override the
    // texture's dispose() to a no-op so it survives across respawns.
    tex.dispose = () => {};
    BabylonExplosion.fireballTextureCache = tex;
    return tex;
  }

  // Remap Babylon's natural row-major cellIndex (top-left = 0, bottom-right = 63) to the
  // physical animation order stored in explosion-texture.webp: bottom-left is the first
  // frame, then rightward along the bottom row, then the row above (left→right), …,
  // top-right is the last frame. Flipping the row preserves column order.
  private static remapCellIndex(natural: number): number {
    const row = (natural / BabylonExplosion.SHEET_COLS) | 0;
    const col = natural % BabylonExplosion.SHEET_COLS;
    const flippedRow = BabylonExplosion.SHEET_ROWS - 1 - row;
    return flippedRow * BabylonExplosion.SHEET_COLS + col;
  }

  private static getDebrisTexture(scene: Scene): Texture {
    if (BabylonExplosion.debrisTextureCache) return BabylonExplosion.debrisTextureCache;
    const tex = new Texture(BabylonExplosion.DEBRIS_URL, scene, true, false, Texture.TRILINEAR_SAMPLINGMODE);
    tex.hasAlpha = true;
    tex.dispose = () => {};
    BabylonExplosion.debrisTextureCache = tex;
    return tex;
  }
}
