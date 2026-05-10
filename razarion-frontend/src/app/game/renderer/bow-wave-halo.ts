import {
  DynamicTexture,
  Effect,
  Material,
  Mesh,
  MeshBuilder,
  Scene,
  ShaderMaterial,
  Texture,
} from "@babylonjs/core";

// Bow-wave foam halo: soft foam patch under a water object (ship hull or
// building footprint). Animated via UV-displacement in a custom fragment
// shader; the mesh itself just tracks position/yaw so the halo aligns with
// the object. Material and procedural mask are shared across all instances.

const HALO_ALPHA = 0.9;
const Y_ABOVE_WATER = 0.04;

export class BowWaveHalo {
  private static sharedMaterial: Material | null = null;
  private static sharedMask: DynamicTexture | null = null;

  private mesh: Mesh;

  constructor(scene: Scene, lengthM: number, widthM: number) {
    // CreateGround: width = local X (perpendicular to forward), height = local
    // Z (along forward, since rotation.y=yaw aligns local +Z with object facing).
    this.mesh = MeshBuilder.CreateGround("BowWaveHalo", {
      width: widthM,
      height: lengthM,
      subdivisions: 1,
    }, scene);
    this.mesh.material = BowWaveHalo.getMaterial(scene);
    this.mesh.isPickable = false;
  }

  setPose(x: number, z: number, yaw: number): void {
    this.mesh.position.set(x, Y_ABOVE_WATER, z);
    this.mesh.rotation.y = yaw;
  }

  dispose(): void {
    this.mesh.dispose();
  }

  private static getMaterial(scene: Scene): Material {
    if (!BowWaveHalo.sharedMaterial) {
      // Register inline GLSL once. Idempotent — re-assignment is a no-op.
      Effect.ShadersStore["bowWaveHaloVertexShader"] = `
        precision highp float;
        attribute vec3 position;
        attribute vec2 uv;
        uniform mat4 worldViewProjection;
        varying vec2 vUV;
        void main() {
          gl_Position = worldViewProjection * vec4(position, 1.0);
          vUV = uv;
        }
      `;
      // Two scrolling noise samples produce a 2D displacement vector. The
      // foam alpha is sampled at the displaced UV so the foam pattern
      // appears to flow/swirl. The radial mask is sampled at the ORIGINAL
      // UV so the halo's outer rim stays cleanly circular.
      Effect.ShadersStore["bowWaveHaloFragmentShader"] = `
        precision highp float;
        varying vec2 vUV;
        uniform float time;
        uniform float matAlpha;
        uniform sampler2D foamSampler;
        uniform sampler2D noiseSampler;
        uniform sampler2D maskSampler;
        void main() {
          // Fast small-scale noise — drives the strong foam-pattern flow.
          vec2 nUV1 = vUV * 1.5 + vec2( time * 0.040,  time * 0.030);
          vec2 nUV2 = vUV * 1.0 + vec2(-time * 0.025,  time * 0.045);
          vec2 noise = vec2(
            texture2D(noiseSampler, nUV1).r - 0.5,
            texture2D(noiseSampler, nUV2).r - 0.5
          );
          // Slow large-scale noise — global warp so the mask edge breathes
          // too, instead of looking like a static frame around an animated
          // layer.
          vec2 sUV1 = vUV * 0.4 + vec2( time * 0.010, time * 0.007);
          vec2 sUV2 = vUV * 0.4 + vec2(-time * 0.0055, time * 0.0085);
          vec2 slow = vec2(
            texture2D(noiseSampler, sUV1).r - 0.5,
            texture2D(noiseSampler, sUV2).r - 0.5
          );
          vec2 displaced = vUV + noise * 0.35 + slow * 0.12;
          vec2 maskUV   = vUV +                 slow * 0.12;
          float foamA = texture2D(foamSampler, displaced).a;
          float maskA = texture2D(maskSampler, maskUV).a;
          gl_FragColor = vec4(1.0, 1.0, 1.0, foamA * maskA * matAlpha);
        }
      `;

      const mat = new ShaderMaterial("BowWaveHaloMat", scene, {
        vertex: "bowWaveHalo",
        fragment: "bowWaveHalo",
      }, {
        attributes: ["position", "uv"],
        uniforms: ["worldViewProjection", "time", "matAlpha"],
        samplers: ["foamSampler", "noiseSampler", "maskSampler"],
        needAlphaBlending: true,
      });

      const foamTex = new Texture("renderer/textures/foam-cells.png", scene);
      foamTex.hasAlpha = true;
      foamTex.dispose = () => {};

      const noiseTex = new Texture("renderer/textures/foam-noise.png", scene);
      noiseTex.dispose = () => {};

      mat.setTexture("foamSampler", foamTex);
      mat.setTexture("noiseSampler", noiseTex);
      mat.setTexture("maskSampler", BowWaveHalo.getMask(scene));
      mat.setFloat("matAlpha", HALO_ALPHA);

      let accumulatedTime = 0;
      scene.onBeforeRenderObservable.add(() => {
        accumulatedTime += scene.getEngine().getDeltaTime() / 1000;
        mat.setFloat("time", accumulatedTime);
      });

      mat.backFaceCulling = false;
      mat.alphaMode = 2; // ALPHA_COMBINE
      mat.disableDepthWrite = true;

      BowWaveHalo.sharedMaterial = mat;
    }
    return BowWaveHalo.sharedMaterial;
  }

  // Procedural radial alpha mask: white centre fading to transparent rim.
  // Sampled at the original (non-displaced) UV so the halo's outer shape
  // stays cleanly circular even as the foam pattern flows inside it.
  private static getMask(scene: Scene): DynamicTexture {
    if (!BowWaveHalo.sharedMask) {
      const size = 128;
      const tex = new DynamicTexture("BowWaveHaloMask", {width: size, height: size}, scene, false);
      tex.hasAlpha = true;
      const ctx = tex.getContext() as unknown as CanvasRenderingContext2D;
      ctx.clearRect(0, 0, size, size);
      const cx = size / 2;
      const cy = size / 2;
      const grad = ctx.createRadialGradient(cx, cy, size * 0.05, cx, cy, size * 0.5);
      grad.addColorStop(0.0, "rgba(255, 255, 255, 1.0)");
      grad.addColorStop(0.55, "rgba(255, 255, 255, 0.85)");
      grad.addColorStop(1.0, "rgba(255, 255, 255, 0)");
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, size, size);
      tex.update();
      tex.dispose = () => {};
      BowWaveHalo.sharedMask = tex;
    }
    return BowWaveHalo.sharedMask;
  }
}
