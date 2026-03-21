import {
  AbstractMesh,
  Color3,
  Color4,
  DynamicTexture,
  Mesh,
  MeshBuilder,
  PBRMaterial,
  ParticleSystem,
  Ray,
  StandardMaterial,
  Vector3,
} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";
import {BabylonRenderServiceAccessImpl, RazarionMetadataType} from "./babylon-render-service-access-impl.service";

export class BabylonWreckage {
  private static ashTextureCache: DynamicTexture | null = null;
  private static scorchTextureCache: DynamicTexture | null = null;
  private static readonly WRECKAGE_LIFETIME = 15000; // ms before fade-out starts
  private static readonly FADE_DURATION = 5000; // ms for fade-out

  static spawn(scene: Scene, position: Vector3, radius: number, survivingMeshes?: Mesh[]): void {
    // Find ground height via raycast (hits Ground and BotGround)
    const groundPos = position.clone();
    const ray = new Ray(new Vector3(position.x, 100, position.z), new Vector3(0, -1, 0), 200);
    const pick = scene.pickWithRay(ray, (mesh: AbstractMesh) => {
      const meta = BabylonRenderServiceAccessImpl.getRazarionMetadata(mesh);
      if (!meta) return false;
      return meta.type === RazarionMetadataType.GROUND || meta.type === RazarionMetadataType.BOT_GROUND;
    });
    if (pick?.pickedPoint) {
      groundPos.y = pick.pickedPoint.y;
    }

    // === 1) Scorch mark on ground (decal on terrain, disc fallback on BotGround) ===
    let scorchMesh: Mesh | null = null;
    let scorchMat: StandardMaterial | null = null;
    const scorchSize = radius * 3.0 + 1.5;

    if (pick?.pickedMesh && pick.pickedPoint) {
      const meta = BabylonRenderServiceAccessImpl.getRazarionMetadata(pick.pickedMesh);
      const isBotGround = meta?.type === RazarionMetadataType.BOT_GROUND;

      // Decal works on terrain, not on BotGround glTF meshes
      if (!isBotGround) {
        try {
          const normal = pick.getNormal(true) ?? new Vector3(0, 1, 0);
          const decal = MeshBuilder.CreateDecal("wreckageScorch", pick.pickedMesh as Mesh, {
            position: pick.pickedPoint.clone(),
            normal: normal,
            size: new Vector3(scorchSize, scorchSize, scorchSize),
            angle: Math.random() * Math.PI * 2
          });
          if (decal && decal.getTotalVertices() > 0) {
            scorchMesh = decal;
          } else {
            decal?.dispose();
          }
        } catch (_) {
          // Decal failed — fall through to disc
        }
      }
    }

    // Fallback: flat disc on ground
    if (!scorchMesh) {
      scorchMesh = MeshBuilder.CreateDisc("wreckageScorchDisc", {
        radius: scorchSize / 2,
        tessellation: 24
      }, scene);
      scorchMesh.position = groundPos.clone();
      scorchMesh.position.y += 0.06;
      scorchMesh.rotation.x = Math.PI / 2;
    }

    scorchMesh.isPickable = false;
    scorchMat = new StandardMaterial("wreckageScorchMat", scene);
    scorchMat.diffuseTexture = BabylonWreckage.getScorchTexture(scene);
    scorchMat.diffuseTexture.hasAlpha = true;
    scorchMat.useAlphaFromDiffuseTexture = true;
    scorchMat.specularColor = Color3.Black();
    scorchMat.emissiveColor = new Color3(0.02, 0.01, 0.0);
    scorchMat.zOffset = -2;
    scorchMesh.material = scorchMat;

    // === 2) Reposition surviving building meshes as debris ===
    const debrisMeshes: Mesh[] = [];

    if (survivingMeshes && survivingMeshes.length > 0) {
      for (const debris of survivingMeshes) {
        const scale = 0.3 + Math.random() * 0.4;
        debris.scaling.setAll(scale);

        const angle = Math.random() * Math.PI * 2;
        const dist = Math.random() * radius * 0.8;
        debris.position = groundPos.clone();
        debris.position.x += Math.cos(angle) * dist;
        debris.position.z += Math.sin(angle) * dist;
        debris.position.y += 0.1;

        debris.rotationQuaternion = null;
        debris.rotation.x = (Math.random() - 0.5) * 1.2;
        debris.rotation.y = Math.random() * Math.PI * 2;
        debris.rotation.z = (Math.random() - 0.5) * 1.2;

        debris.isPickable = false;

        // Darken material to look fire-damaged
        const darken = 0.05 + Math.random() * 0.1;
        const darkMat = new PBRMaterial(`wreckDark_${debris.name}`, scene);
        darkMat.albedoColor = new Color3(darken, darken * 0.95, darken * 0.9);
        darkMat.metallic = 0.0;
        darkMat.roughness = 0.9;
        debris.material = darkMat;

        debrisMeshes.push(debris);
      }
    }

    // === 3) Lingering smoke ===
    const smoke = new ParticleSystem("wreckageSmoke", 300, scene);
    smoke.particleTexture = BabylonWreckage.getAshTexture(scene);
    smoke.emitter = groundPos.clone();
    smoke.blendMode = ParticleSystem.BLENDMODE_STANDARD;

    const r = radius * 0.7;
    smoke.minEmitBox = new Vector3(-r, 0, -r);
    smoke.maxEmitBox = new Vector3(r, 0.3, r);

    smoke.direction1 = new Vector3(-0.4, 0.5, -0.4);
    smoke.direction2 = new Vector3(0.4, 2.5, 0.4);

    smoke.minLifeTime = 2.5;
    smoke.maxLifeTime = 5.0;
    smoke.emitRate = 25;

    smoke.minSize = radius * 0.5;
    smoke.maxSize = radius * 1.2;
    smoke.minEmitPower = 0.2;
    smoke.maxEmitPower = 0.8;

    smoke.color1 = new Color4(0.3, 0.27, 0.24, 0.6);
    smoke.color2 = new Color4(0.18, 0.16, 0.14, 0.4);
    smoke.colorDead = new Color4(0.08, 0.07, 0.06, 0);

    smoke.addSizeGradient(0, 0.4);
    smoke.addSizeGradient(0.5, 0.9);
    smoke.addSizeGradient(1.0, 1.2);

    smoke.gravity = new Vector3(0, -0.1, 0);
    smoke.particleTexture!.dispose = () => {};
    smoke.start();

    // === 4) Timed cleanup with fade ===
    const startTime = Date.now();

    const fadeCallback = () => {
      const elapsed = Date.now() - startTime;

      if (elapsed > BabylonWreckage.WRECKAGE_LIFETIME) {
        const fadeElapsed = elapsed - BabylonWreckage.WRECKAGE_LIFETIME;
        const fadeT = Math.min(fadeElapsed / BabylonWreckage.FADE_DURATION, 1.0);

        if (scorchMat) {
          scorchMat.alpha = 1 - fadeT;
        }
        for (const d of debrisMeshes) {
          if (d.material) {
            d.material.alpha = 1 - fadeT;
          }
        }

        // Reduce smoke as we fade
        smoke.emitRate = 8 * (1 - fadeT);

        if (fadeT >= 1.0) {
          // Full cleanup
          scene.unregisterBeforeRender(fadeCallback);
          if (scorchMesh) {
            scorchMesh.dispose();
          }
          if (scorchMat) {
            scorchMat.dispose();
          }
          for (const d of debrisMeshes) {
            d.material?.dispose();
            d.dispose();
          }
          smoke.stop();
          smoke.dispose();
        }
      }
    };

    scene.registerBeforeRender(fadeCallback);
  }

  private static getAshTexture(scene: Scene): DynamicTexture {
    if (BabylonWreckage.ashTextureCache) return BabylonWreckage.ashTextureCache;
    const tex = new DynamicTexture("wreckageAshTex", 128, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext();
    const size = 128;
    const cx = size / 2;
    const cy = size / 2;

    const blobs = [
      {x: cx, y: cy, r: 55},
      {x: cx - 15, y: cy - 10, r: 40},
      {x: cx + 18, y: cy + 12, r: 35},
      {x: cx + 8, y: cy - 18, r: 30},
      {x: cx - 12, y: cy + 15, r: 35},
    ];
    for (const blob of blobs) {
      const grad = ctx.createRadialGradient(blob.x, blob.y, 0, blob.x, blob.y, blob.r);
      grad.addColorStop(0, "rgba(80, 75, 70, 0.6)");
      grad.addColorStop(0.4, "rgba(60, 55, 50, 0.35)");
      grad.addColorStop(0.7, "rgba(40, 38, 35, 0.15)");
      grad.addColorStop(1.0, "rgba(20, 18, 16, 0)");
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, size, size);
    }

    tex.update();
    BabylonWreckage.ashTextureCache = tex;
    return tex;
  }

  private static getScorchTexture(scene: Scene): DynamicTexture {
    if (BabylonWreckage.scorchTextureCache) return BabylonWreckage.scorchTextureCache;
    const tex = new DynamicTexture("wreckageScorchTex", 256, scene, false);
    tex.hasAlpha = true;
    const ctx = tex.getContext();
    const size = 256;
    const cx = size / 2;
    const cy = size / 2;

    ctx.clearRect(0, 0, size, size);

    // Irregular scorch shape from overlapping dark blobs
    const blobs = [
      {x: cx, y: cy, r: 100},
      {x: cx - 30, y: cy - 20, r: 70},
      {x: cx + 35, y: cy + 15, r: 65},
      {x: cx + 10, y: cy - 35, r: 55},
      {x: cx - 25, y: cy + 30, r: 60},
      {x: cx + 40, y: cy - 10, r: 50},
      {x: cx - 15, y: cy - 5, r: 80},
    ];
    for (const blob of blobs) {
      const grad = ctx.createRadialGradient(blob.x, blob.y, 0, blob.x, blob.y, blob.r);
      grad.addColorStop(0, "rgba(15, 12, 10, 0.85)");
      grad.addColorStop(0.3, "rgba(25, 20, 15, 0.7)");
      grad.addColorStop(0.6, "rgba(35, 28, 20, 0.4)");
      grad.addColorStop(0.85, "rgba(40, 30, 20, 0.1)");
      grad.addColorStop(1.0, "rgba(40, 30, 20, 0)");
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, size, size);
    }

    tex.update();
    BabylonWreckage.scorchTextureCache = tex;
    return tex;
  }
}
