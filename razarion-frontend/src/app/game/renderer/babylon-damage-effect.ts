import {
  Color4,
  DynamicTexture,
  Mesh,
  MeshBuilder,
  ParticleSystem,
  TransformNode,
  Vector3,
  VertexBuffer,
} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";

export class BabylonDamageEffect {
  private static smokeTextureCache: DynamicTexture | null = null;
  private static fireTextureCache: DynamicTexture | null = null;

  private smokeSystem: ParticleSystem | null = null;
  private fireSystem: ParticleSystem | null = null;
  private emitterMesh: Mesh | null = null;
  private originalPositions: Map<Mesh, Float32Array> = new Map();
  private deformSeeded = false;
  private deformOffsets: Map<Mesh, Float32Array> = new Map();
  private lastDeformDamage = 0;

  constructor(
    private scene: Scene,
    private container: TransformNode,
    private radius: number
  ) {}

  private getEmitterMesh(): Mesh {
    if (!this.emitterMesh) {
      this.emitterMesh = MeshBuilder.CreateBox("damageEmitter", {size: 0.01}, this.scene);
      this.emitterMesh.isVisible = false;
      this.emitterMesh.isPickable = false;
      this.emitterMesh.parent = this.container;
    }
    return this.emitterMesh;
  }

  update(health: number): void {
    const damage = 1.0 - health;

    // --- Smoke (health < 1.0) ---
    if (health < 1.0) {
      if (!this.smokeSystem) {
        this.smokeSystem = this.createSmokeSystem();
      }
      const t = damage; // 0..1
      this.smokeSystem.emitRate = 15 + 120 * t;
      this.smokeSystem.minSize = this.radius * 0.3;
      this.smokeSystem.maxSize = this.radius * (0.6 + 0.8 * t);
      // Darken smoke color as damage increases
      const grey = 0.7 - 0.4 * t;
      this.smokeSystem.color1 = new Color4(grey + 0.1, grey + 0.05, grey, 0.7 + 0.3 * t);
      this.smokeSystem.color2 = new Color4(grey - 0.1, grey - 0.1, grey - 0.1, 0.5 + 0.3 * t);
      this.smokeSystem.colorDead = new Color4(grey - 0.2, grey - 0.2, grey - 0.2, 0);
    } else if (this.smokeSystem) {
      this.smokeSystem.stop();
      this.smokeSystem.dispose();
      this.smokeSystem = null;
    }

    // --- Fire (health < 0.5) ---
    if (health < 0.5) {
      if (!this.fireSystem) {
        this.fireSystem = this.createFireSystem();
      }
      const fireT = (0.5 - health) / 0.5; // 0..1
      this.fireSystem.emitRate = 20 + 100 * fireT;
      this.fireSystem.minSize = this.radius * 0.2;
      this.fireSystem.maxSize = this.radius * (0.5 + 0.7 * fireT);
    } else if (this.fireSystem) {
      this.fireSystem.stop();
      this.fireSystem.dispose();
      this.fireSystem = null;
    }

    // --- Vertex deformation ---
    if (health < 1.0) {
      this.applyDeformation(damage);
    } else if (this.originalPositions.size > 0) {
      this.restoreVertices();
    }
  }

  dispose(): void {
    if (this.smokeSystem) {
      this.smokeSystem.stop();
      this.smokeSystem.dispose();
      this.smokeSystem = null;
    }
    if (this.fireSystem) {
      this.fireSystem.stop();
      this.fireSystem.dispose();
      this.fireSystem = null;
    }
    this.restoreVertices();
    if (this.emitterMesh) {
      this.emitterMesh.dispose();
      this.emitterMesh = null;
    }
  }

  private applyDeformation(damage: number): void {
    const meshes = this.container.getChildMeshes(false) as Mesh[];

    // Seed random per-vertex offsets once
    if (!this.deformSeeded) {
      for (const mesh of meshes) {
        const positions = mesh.getVerticesData(VertexBuffer.PositionKind);
        const normals = mesh.getVerticesData(VertexBuffer.NormalKind);
        if (!positions || !normals) continue;

        // Clone geometry so shared model buffers are not affected
        mesh.makeGeometryUnique();
        mesh.markVerticesDataAsUpdatable(VertexBuffer.PositionKind, true);

        // Store original positions
        this.originalPositions.set(mesh, new Float32Array(positions));

        // Generate per-vertex random inward offsets along normals
        const offsets = new Float32Array(positions.length);
        const vertexCount = positions.length / 3;
        for (let i = 0; i < vertexCount; i++) {
          // Random strength per vertex: some dent deeply, some barely
          const strength = Math.random() * Math.random(); // bias toward smaller dents
          const idx = i * 3;
          // Push inward along negative normal
          offsets[idx] = -normals[idx] * strength;
          offsets[idx + 1] = -normals[idx + 1] * strength;
          offsets[idx + 2] = -normals[idx + 2] * strength;
        }
        this.deformOffsets.set(mesh, offsets);
      }
      this.deformSeeded = true;
    }

    // Scale deformation by damage — max displacement is radius * 0.15
    const maxDisplacement = this.radius * 0.15 * damage;

    for (const mesh of meshes) {
      const original = this.originalPositions.get(mesh);
      const offsets = this.deformOffsets.get(mesh);
      if (!original || !offsets) continue;

      const deformed = new Float32Array(original.length);
      for (let i = 0; i < original.length; i++) {
        deformed[i] = original[i] + offsets[i] * maxDisplacement;
      }
      mesh.updateVerticesData(VertexBuffer.PositionKind, deformed);
    }
    this.lastDeformDamage = damage;
  }

  private restoreVertices(): void {
    for (const [mesh, original] of this.originalPositions) {
      try {
        mesh.updateVerticesData(VertexBuffer.PositionKind, original);
      } catch (_) {
        // mesh may already be disposed
      }
    }
    this.originalPositions.clear();
    this.deformOffsets.clear();
    this.deformSeeded = false;
    this.lastDeformDamage = 0;
  }

  private createSmokeSystem(): ParticleSystem {
    const smoke = new ParticleSystem("damageSmoke", 400, this.scene);
    smoke.particleTexture = BabylonDamageEffect.getSmokeTexture(this.scene);
    smoke.emitter = this.getEmitterMesh();
    smoke.blendMode = ParticleSystem.BLENDMODE_STANDARD;

    const r = this.radius * 0.6;
    smoke.minEmitBox = new Vector3(-r, 0, -r);
    smoke.maxEmitBox = new Vector3(r, this.radius * 0.4, r);

    smoke.direction1 = new Vector3(-0.4, 0.8, -0.4);
    smoke.direction2 = new Vector3(0.4, 2.5, 0.4);

    smoke.minLifeTime = 1.5;
    smoke.maxLifeTime = 3.5;
    smoke.emitRate = 15;

    smoke.minEmitPower = 0.3;
    smoke.maxEmitPower = 1.0;

    smoke.addSizeGradient(0, 0.4);
    smoke.addSizeGradient(0.3, 0.8);
    smoke.addSizeGradient(0.7, 1.0);
    smoke.addSizeGradient(1.0, 1.2);

    smoke.gravity = new Vector3(0, -0.2, 0);
    smoke.particleTexture!.dispose = () => {};
    smoke.start();
    return smoke;
  }

  private createFireSystem(): ParticleSystem {
    const fire = new ParticleSystem("damageFire", 500, this.scene);
    fire.particleTexture = BabylonDamageEffect.getFireTexture(this.scene);
    fire.emitter = this.getEmitterMesh();
    fire.blendMode = ParticleSystem.BLENDMODE_STANDARD;

    const r = this.radius * 0.4;
    fire.minEmitBox = new Vector3(-r, -0.1, -r);
    fire.maxEmitBox = new Vector3(r, this.radius * 0.15, r);

    // Flames go mostly upward with slight flicker sideways
    fire.direction1 = new Vector3(-0.15, 1.0, -0.15);
    fire.direction2 = new Vector3(0.15, 3.0, 0.15);

    fire.minLifeTime = 0.5;
    fire.maxLifeTime = 1.4;
    fire.emitRate = 20;

    fire.minEmitPower = 0.8;
    fire.maxEmitPower = 2.0;

    // Core: bright white-yellow → orange → brief red → dark smoke → fade out
    fire.addColorGradient(0, new Color4(1, 1, 0.85, 1));
    fire.addColorGradient(0.08, new Color4(1, 0.95, 0.5, 1));
    fire.addColorGradient(0.25, new Color4(1, 0.7, 0.15, 1));
    fire.addColorGradient(0.35, new Color4(1, 0.4, 0.02, 0.9));
    fire.addColorGradient(0.42, new Color4(0.7, 0.12, 0.0, 0.8));
    fire.addColorGradient(0.55, new Color4(0.2, 0.15, 0.12, 0.6));
    fire.addColorGradient(0.75, new Color4(0.1, 0.08, 0.06, 0.35));
    fire.addColorGradient(1.0, new Color4(0.05, 0.04, 0.03, 0));

    // Flames start small, swell, then expand into smoke puff
    fire.addSizeGradient(0, 0.15);
    fire.addSizeGradient(0.15, 0.6);
    fire.addSizeGradient(0.35, 1.0);
    fire.addSizeGradient(0.55, 0.7);
    fire.addSizeGradient(0.75, 1.0);
    fire.addSizeGradient(1.0, 1.4);

    // Slight negative gravity pulls flames upward faster
    fire.gravity = new Vector3(0, 1.0, 0);

    // Flicker via angular speed
    fire.minAngularSpeed = -1.5;
    fire.maxAngularSpeed = 1.5;

    fire.particleTexture!.dispose = () => {};
    fire.start();
    return fire;
  }

  // --- Cached texture generation ---

  private static getSmokeTexture(scene: Scene): DynamicTexture {
    if (BabylonDamageEffect.smokeTextureCache) return BabylonDamageEffect.smokeTextureCache;
    const tex = new DynamicTexture("damageSmoketex", 128, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext();
    const size = 128;
    const cx = size / 2;
    const cy = size / 2;

    const blobs = [
      {x: cx, y: cy, r: 55},
      {x: cx - 15, y: cy - 10, r: 40},
      {x: cx + 18, y: cy + 12, r: 35},
      {x: cx + 8, y: cy - 18, r: 32},
      {x: cx - 12, y: cy + 15, r: 38},
    ];
    for (const blob of blobs) {
      const grad = ctx.createRadialGradient(blob.x, blob.y, 0, blob.x, blob.y, blob.r);
      grad.addColorStop(0, "rgba(160, 155, 150, 0.7)");
      grad.addColorStop(0.3, "rgba(120, 115, 110, 0.5)");
      grad.addColorStop(0.6, "rgba(80, 75, 70, 0.25)");
      grad.addColorStop(1.0, "rgba(40, 38, 36, 0)");
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, size, size);
    }

    tex.update();
    BabylonDamageEffect.smokeTextureCache = tex;
    return tex;
  }

  private static getFireTexture(scene: Scene): DynamicTexture {
    if (BabylonDamageEffect.fireTextureCache) return BabylonDamageEffect.fireTextureCache;
    const tex = new DynamicTexture("damageFireTex", 128, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext();
    const size = 128;
    const cx = size / 2;
    const cy = size * 0.65; // base lower, flame rises upward

    ctx.clearRect(0, 0, size, size);

    // Flame shape: irregular teardrop pointing up, built from overlapping ellipses
    const flames = [
      {x: cx, y: cy, rx: 38, ry: 55, a: 0.9},
      {x: cx - 8, y: cy - 10, rx: 25, ry: 45, a: 0.7},
      {x: cx + 10, y: cy - 5, rx: 22, ry: 40, a: 0.6},
      {x: cx - 3, y: cy - 20, rx: 18, ry: 35, a: 0.5},
      {x: cx + 5, y: cy - 25, rx: 12, ry: 25, a: 0.4},
    ];

    for (const f of flames) {
      ctx.save();
      ctx.translate(f.x, f.y);
      ctx.scale(1, f.ry / f.rx);
      const grad = ctx.createRadialGradient(0, 0, 0, 0, 0, f.rx);
      grad.addColorStop(0, `rgba(255, 255, 220, ${f.a})`);
      grad.addColorStop(0.2, `rgba(255, 230, 120, ${f.a * 0.85})`);
      grad.addColorStop(0.45, `rgba(255, 170, 40, ${f.a * 0.6})`);
      grad.addColorStop(0.7, `rgba(230, 90, 5, ${f.a * 0.3})`);
      grad.addColorStop(1.0, "rgba(180, 40, 0, 0)");
      ctx.fillStyle = grad;
      ctx.beginPath();
      ctx.arc(0, 0, f.rx, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();
    }

    // Hot bright core at base
    const core = ctx.createRadialGradient(cx, cy + 5, 0, cx, cy + 5, 15);
    core.addColorStop(0, "rgba(255, 255, 255, 0.95)");
    core.addColorStop(0.4, "rgba(255, 255, 200, 0.6)");
    core.addColorStop(1.0, "rgba(255, 220, 100, 0)");
    ctx.fillStyle = core;
    ctx.fillRect(0, 0, size, size);

    tex.update();
    BabylonDamageEffect.fireTextureCache = tex;
    return tex;
  }
}
