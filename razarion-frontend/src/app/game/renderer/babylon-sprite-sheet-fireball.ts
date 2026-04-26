import {Color4, ParticleSystem, SphereParticleEmitter, Texture, Vector3} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";

// Reusable sprite-sheet-animated fireball system. The 64-frame sheet at
// public/renderer/textures/explosion-texture.webp is laid out bottom-left → top-right
// (first frame = ignition spark at row 7, last = dissipated smoke at row 0). Babylon's
// natural cellIndex is row-major top-first, so we run our own updateFunction that
// computes the frame deterministically from age/lifeTime and remaps rows.
//
// Used both by BabylonExplosion (large, upward-rising puffs for building destruction)
// and BabylonImpact (smaller, shorter-lived fireball for projectile hits).
export interface SpriteSheetFireballOptions {
  capacity: number;
  sphereRadius?: number;
  minSize?: number;
  maxSize?: number;
  minLifeTime?: number;
  maxLifeTime?: number;
  direction1?: Vector3;
  direction2?: Vector3;
  minEmitPower?: number;
  maxEmitPower?: number;
  gravity?: Vector3;
  minAngularSpeed?: number;
  maxAngularSpeed?: number;
  namePrefix?: string;
}

export class BabylonSpriteSheetFireball {
  private static readonly SHEET_URL = "renderer/textures/explosion-texture.webp";
  private static readonly SHEET_COLS = 8;
  private static readonly SHEET_ROWS = 8;
  private static readonly SHEET_FRAMES = 64;
  private static readonly SHEET_CELL = 128;

  private static textureCache: Texture | null = null;

  static spawn(scene: Scene, position: Vector3, options: SpriteSheetFireballOptions): ParticleSystem {
    const namePrefix = options.namePrefix ?? "spriteFireball";
    const fire = new ParticleSystem(namePrefix, options.capacity, scene, null, true);
    fire.particleTexture = BabylonSpriteSheetFireball.getTexture(scene);
    fire.emitter = position.clone();
    fire.blendMode = ParticleSystem.BLENDMODE_STANDARD;

    const emitter = new SphereParticleEmitter(options.sphereRadius ?? 0);
    fire.particleEmitterType = emitter;

    // We drive cellIndex manually in updateFunction below; disable Babylon's own cell
    // progression. The shader still needs cell dimensions to sample the right frame.
    fire.startSpriteCellID = 0;
    fire.endSpriteCellID = BabylonSpriteSheetFireball.SHEET_FRAMES - 1;
    fire.spriteCellWidth = BabylonSpriteSheetFireball.SHEET_CELL;
    fire.spriteCellHeight = BabylonSpriteSheetFireball.SHEET_CELL;
    fire.spriteCellChangeSpeed = 0;
    fire.spriteCellLoop = false;
    fire.spriteRandomStartCell = false;

    fire.emitRate = 0;
    fire.manualEmitCount = options.capacity;
    fire.targetStopDuration = 0.05;

    fire.minLifeTime = options.minLifeTime ?? 0.8;
    fire.maxLifeTime = options.maxLifeTime ?? 1.2;

    fire.minSize = options.minSize ?? 4;
    fire.maxSize = options.maxSize ?? 6;

    fire.direction1 = options.direction1 ?? Vector3.Zero();
    fire.direction2 = options.direction2 ?? Vector3.Zero();
    fire.minEmitPower = options.minEmitPower ?? 0;
    fire.maxEmitPower = options.maxEmitPower ?? 0;

    fire.minAngularSpeed = options.minAngularSpeed ?? -Math.PI * 0.3;
    fire.maxAngularSpeed = options.maxAngularSpeed ?? Math.PI * 0.3;
    fire.minInitialRotation = 0;
    fire.maxInitialRotation = Math.PI * 2;

    fire.gravity = options.gravity ?? Vector3.Zero();

    // Solid white tint — sprite sheet already carries the fire/smoke colors.
    fire.color1 = new Color4(1, 1, 1, 1);
    fire.color2 = new Color4(1, 1, 1, 1);
    fire.colorDead = new Color4(1, 1, 1, 1);

    // Deterministic cell progression — sidesteps Babylon's cellIndex=64 overshoot at
    // ratio=1, fractional cellIndex float issues, and potential double invocation.
    const total = BabylonSpriteSheetFireball.SHEET_FRAMES;
    const defaultUpdate = fire.updateFunction;
    fire.updateFunction = function (particles) {
      defaultUpdate.call(this, particles);
      for (const p of particles) {
        const ratio = Math.max(0, Math.min(p.age / p.lifeTime, 0.99999));
        const naturalCell = (ratio * total) | 0;
        p.cellIndex = BabylonSpriteSheetFireball.remapCellIndex(naturalCell);
      }
    };

    fire.disposeOnStop = true;
    fire.start();
    return fire;
  }

  // Babylon's natural row-major cellIndex (top-left = 0, bottom-right = 63) → physical
  // animation order stored in the sheet (bottom-left first, rightward, then the row
  // above left→right, …, top-right last). Column order preserved.
  private static remapCellIndex(natural: number): number {
    const row = (natural / BabylonSpriteSheetFireball.SHEET_COLS) | 0;
    const col = natural % BabylonSpriteSheetFireball.SHEET_COLS;
    const flippedRow = BabylonSpriteSheetFireball.SHEET_ROWS - 1 - row;
    return flippedRow * BabylonSpriteSheetFireball.SHEET_COLS + col;
  }

  private static getTexture(scene: Scene): Texture {
    if (BabylonSpriteSheetFireball.textureCache) return BabylonSpriteSheetFireball.textureCache;
    const tex = new Texture(BabylonSpriteSheetFireball.SHEET_URL, scene, true, false, Texture.TRILINEAR_SAMPLINGMODE);
    tex.hasAlpha = true;
    // ParticleSystem.dispose() defaults to disposeTexture=true with disposeOnStop=true —
    // would kill this cache after the first fireball finishes. Keep the cached texture
    // alive across all subsequent spawns.
    tex.dispose = () => {};
    BabylonSpriteSheetFireball.textureCache = tex;
    return tex;
  }
}
