import {
  Color3,
  Constants,
  Matrix,
  Mesh,
  MeshBuilder,
  PointLight,
  Quaternion,
  RawTexture,
  StandardMaterial,
  Texture,
  TmpVectors,
  Vector3,
  VertexData
} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";

/**
 * Sustained energy-beam weapon effect (Badger). A continuous, camera-facing beam quad from a
 * muzzle origin to a target, with a bright core + outer glow scrolling energy texture, a pulsing
 * impact flare at the target, and a dynamic point light at the hit point.
 *
 * Development scaffold — built to be tweaked live in the GwtMock (see GameMockService). Unlike
 * {@link BabylonLightning} (a short one-shot flickering bolt) this beam is held open while firing:
 * call {@link start} with a target, then {@link dispose} to stop. The origin is re-read every
 * frame via the `getBeamOrigin` callback so the beam tracks the muzzle as the unit/turret moves.
 *
 * All tunable look parameters live in the static block below.
 */
export class BabylonEnergyBeam {
  // --- Beam geometry / look ---
  private static readonly CORE_WIDTH = 0.45;       // bright inner core quad width (world units)
  private static readonly GLOW_WIDTH = 1.6;        // soft outer glow quad width
  private static readonly SCROLL_SPEED = 2.6;      // texture v-scroll (energy flowing toward target)
  private static readonly CORE_COLOR = new Color3(0.6, 0.95, 1.0);  // emissive tint, core
  private static readonly GLOW_COLOR = new Color3(0.2, 0.6, 1.0);   // emissive tint, glow
  // --- Impact flare at the target ---
  private static readonly FLARE_BASE_DIAMETER = 1.4;
  private static readonly FLARE_PULSE = 0.35;      // +/- pulse fraction around the base diameter
  private static readonly FLARE_PULSE_HZ = 9;
  // --- Muzzle flash at the beam origin ---
  // Must be clearly WIDER than GLOW_WIDTH, otherwise the flash is swallowed by the beam's own
  // (brighter, additive) glow halo at the origin and reads as invisible.
  private static readonly MUZZLE_BASE_DIAMETER = 3.0;
  private static readonly MUZZLE_FLICKER = 0.22;   // steady-state flicker amplitude
  private static readonly MUZZLE_FLICKER_HZ = 14;
  private static readonly MUZZLE_SPIKE_MS = 130;   // initial bright kick when the beam opens
  private static readonly MUZZLE_SPIKE_SCALE = 2.0;
  // Push the flash out of the barrel toward the target so it isn't buried in the model geometry.
  private static readonly MUZZLE_FORWARD_OFFSET = 0.5;
  // --- Dynamic light at the impact point ---
  private static readonly LIGHT_INTENSITY = 2.4;
  private static readonly LIGHT_RANGE = 16;

  // Shared, lazily-built textures/light (reused across every beam instance so firing a beam never
  // triggers a scene-wide shader recompile — same rationale as BabylonLightning.sharedLight).
  private static beamTextureCache: RawTexture | null = null;
  private static flareTextureCache: RawTexture | null = null;
  private static sharedLight: PointLight | null = null;
  private static activeBeamCount = 0;

  private coreMesh: Mesh | null = null;
  private coreMat: StandardMaterial | null = null;
  private glowMesh: Mesh | null = null;
  private glowMat: StandardMaterial | null = null;
  private flareMesh: Mesh | null = null;
  private flareMat: StandardMaterial | null = null;
  private muzzleMesh: Mesh | null = null;
  private muzzleMat: StandardMaterial | null = null;
  private target: Vector3 | null = null;
  private startTime = 0;
  private renderCallback: (() => void) | null = null;
  private countedActive = false;

  /**
   * @param scene          the Babylon scene
   * @param getBeamOrigin  re-read each frame — return the live muzzle world position, or null to
   *                       fall back to {@link getFallbackPosition} (e.g. RenderObject.getBeamOrigin)
   * @param getFallbackPosition  used when getBeamOrigin returns null (e.g. the unit's center)
   */
  constructor(
    private scene: Scene,
    private getBeamOrigin: () => Vector3 | null,
    private getFallbackPosition: () => Vector3
  ) {
  }

  isActive(): boolean {
    return this.renderCallback !== null;
  }

  /** Aim the (already firing) beam at a new target. No-op if not started. */
  setTarget(target: Vector3): void {
    this.target = target.clone();
  }

  /** Open the beam onto `target`. Idempotent while already firing. */
  start(target: Vector3): void {
    if (this.renderCallback) {
      this.setTarget(target);
      return;
    }
    this.target = target.clone();
    this.startTime = Date.now();
    this.renderCallback = () => this.update();
    this.scene.registerBeforeRender(this.renderCallback);
  }

  dispose(): void {
    if (this.renderCallback) {
      this.scene.unregisterBeforeRender(this.renderCallback);
      this.renderCallback = null;
    }
    this.coreMesh?.dispose();
    this.coreMat?.dispose();
    this.glowMesh?.dispose();
    this.glowMat?.dispose();
    this.flareMesh?.dispose();
    this.flareMat?.dispose();
    this.muzzleMesh?.dispose();
    this.muzzleMat?.dispose();
    this.coreMesh = this.glowMesh = this.flareMesh = this.muzzleMesh = null;
    this.coreMat = this.glowMat = this.flareMat = this.muzzleMat = null;
    if (this.countedActive) {
      this.countedActive = false;
      BabylonEnergyBeam.activeBeamCount = Math.max(0, BabylonEnergyBeam.activeBeamCount - 1);
      if (BabylonEnergyBeam.activeBeamCount === 0 && BabylonEnergyBeam.sharedLight) {
        BabylonEnergyBeam.sharedLight.intensity = 0;
      }
    }
    this.target = null;
  }

  private update(): void {
    if (!this.target) {
      return;
    }
    const start = this.getBeamOrigin() ?? this.getFallbackPosition();
    const end = this.target;
    const dist = Vector3.Distance(start, end);
    if (dist < 0.01) {
      return;
    }
    const boltDir = end.subtract(start).scale(1 / dist);

    this.ensureMeshes();

    const elapsed = (Date.now() - this.startTime) / 1000;

    // Both quads share the same camera-facing billboard orientation along the beam axis.
    const orientation = this.computeBillboardQuaternion(start, end, dist);
    this.layoutBeamQuad(this.coreMesh!, BabylonEnergyBeam.CORE_WIDTH, dist, start, orientation);
    this.layoutBeamQuad(this.glowMesh!, BabylonEnergyBeam.GLOW_WIDTH, dist, start, orientation);

    // Scroll the energy texture toward the target; a slight breathing on the core alpha sells "live".
    (this.coreMat!.emissiveTexture as Texture).vOffset = -elapsed * BabylonEnergyBeam.SCROLL_SPEED;
    (this.glowMat!.emissiveTexture as Texture).vOffset = -elapsed * BabylonEnergyBeam.SCROLL_SPEED * 0.6;
    this.coreMat!.alpha = 0.9 + 0.1 * Math.sin(elapsed * 30);

    // Muzzle flash at the beam origin: a bright spike when the beam opens, decaying into a
    // steady high-frequency flicker. Tracks the muzzle since `start` is re-read each frame.
    const elapsedMs = elapsed * 1000;
    const spike = elapsedMs < BabylonEnergyBeam.MUZZLE_SPIKE_MS
      ? (BabylonEnergyBeam.MUZZLE_SPIKE_SCALE - 1) * (1 - elapsedMs / BabylonEnergyBeam.MUZZLE_SPIKE_MS)
      : 0;
    const flicker = 1 + BabylonEnergyBeam.MUZZLE_FLICKER * Math.sin(elapsed * BabylonEnergyBeam.MUZZLE_FLICKER_HZ * Math.PI * 2);
    this.muzzleMesh!.position.copyFrom(start);
    this.muzzleMesh!.position.addInPlace(boltDir.scale(BabylonEnergyBeam.MUZZLE_FORWARD_OFFSET));
    this.muzzleMesh!.scaling.setAll(BabylonEnergyBeam.MUZZLE_BASE_DIAMETER * (flicker + spike));
    this.muzzleMat!.alpha = 0.75 + 0.25 * Math.min(1, spike + (flicker - 1) / BabylonEnergyBeam.MUZZLE_FLICKER);

    // Impact flare + light at the target.
    const pulse = 1 + BabylonEnergyBeam.FLARE_PULSE * Math.sin(elapsed * BabylonEnergyBeam.FLARE_PULSE_HZ * Math.PI * 2);
    this.flareMesh!.position.copyFrom(end);
    this.flareMesh!.scaling.setAll(BabylonEnergyBeam.FLARE_BASE_DIAMETER * pulse);

    const light = BabylonEnergyBeam.getOrCreateSharedLight(this.scene);
    light.position.copyFrom(end);
    light.intensity = BabylonEnergyBeam.LIGHT_INTENSITY * (0.85 + 0.15 * pulse);
  }

  private ensureMeshes(): void {
    if (this.coreMesh) {
      return;
    }
    const tex = BabylonEnergyBeam.getBeamTexture(this.scene);

    this.glowMat = BabylonEnergyBeam.makeBeamMaterial(this.scene, "EnergyBeamGlowMat", tex, BabylonEnergyBeam.GLOW_COLOR, 0.55);
    this.glowMesh = new Mesh("EnergyBeamGlow", this.scene);
    this.glowMesh.isPickable = false;
    this.glowMesh.material = this.glowMat;

    this.coreMat = BabylonEnergyBeam.makeBeamMaterial(this.scene, "EnergyBeamCoreMat", tex, BabylonEnergyBeam.CORE_COLOR, 1.0);
    this.coreMesh = new Mesh("EnergyBeamCore", this.scene);
    this.coreMesh.isPickable = false;
    this.coreMesh.material = this.coreMat;
    // Core draws over the glow.
    this.coreMesh.renderingGroupId = this.glowMesh.renderingGroupId;

    this.flareMat = BabylonEnergyBeam.makeFlareMaterial(this.scene);
    this.flareMesh = MeshBuilder.CreatePlane("EnergyBeamFlare", {size: 1}, this.scene);
    this.flareMesh.isPickable = false;
    this.flareMesh.billboardMode = Mesh.BILLBOARDMODE_ALL;
    this.flareMesh.material = this.flareMat;

    this.muzzleMat = BabylonEnergyBeam.makeFlareMaterial(this.scene);
    this.muzzleMat.name = "EnergyBeamMuzzleMat";
    // Always pass the depth test so the flash is never swallowed by the barrel/body geometry it
    // sits inside; the forward offset keeps it physically near the muzzle tip.
    this.muzzleMat.depthFunction = Constants.ALWAYS;
    this.muzzleMesh = MeshBuilder.CreatePlane("EnergyBeamMuzzle", {size: 1}, this.scene);
    this.muzzleMesh.isPickable = false;
    this.muzzleMesh.billboardMode = Mesh.BILLBOARDMODE_ALL;
    this.muzzleMesh.material = this.muzzleMat;

    if (!this.countedActive) {
      this.countedActive = true;
      BabylonEnergyBeam.activeBeamCount++;
    }
  }

  /** Builds a flat quad of `width` x `dist` lying along +Y, oriented by `orientation`, rooted at `start`. */
  private layoutBeamQuad(mesh: Mesh, width: number, dist: number, start: Vector3, orientation: Quaternion): void {
    const half = width / 2;
    const vertexData = new VertexData();
    vertexData.positions = [-half, 0, 0, half, 0, 0, half, dist, 0, -half, dist, 0];
    vertexData.indices = [0, 1, 2, 0, 2, 3];
    vertexData.uvs = [0, 0, 1, 0, 1, 1, 0, 1];
    vertexData.normals = [0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1];
    vertexData.applyToMesh(mesh, true);
    mesh.position.copyFrom(start);
    mesh.rotationQuaternion = orientation;
  }

  /** Orient the beam's local +Y along start→end while rotating its plane to face the camera. */
  private computeBillboardQuaternion(start: Vector3, end: Vector3, dist: number): Quaternion {
    const boltDir = end.subtract(start).scale(1 / dist);
    const midpoint = Vector3.Center(start, end);
    const cam = this.scene.activeCamera;
    const out = new Quaternion();
    if (!cam) {
      Quaternion.FromUnitVectorsToRef(Vector3.Up(), boltDir, out);
      return out;
    }
    const toCam = TmpVectors.Vector3[0];
    cam.position.subtractToRef(midpoint, toCam);
    const proj = Vector3.Dot(toCam, boltDir);
    const scaledDir = TmpVectors.Vector3[1];
    boltDir.scaleToRef(proj, scaledDir);
    const localZ = TmpVectors.Vector3[2];
    toCam.subtractToRef(scaledDir, localZ);
    const zLen = localZ.length();
    if (zLen < 0.001) {
      Quaternion.FromUnitVectorsToRef(Vector3.Up(), boltDir, out);
      return out;
    }
    localZ.scaleInPlace(1 / zLen);
    const localX = TmpVectors.Vector3[3];
    Vector3.CrossToRef(boltDir, localZ, localX);
    const rotMatrix = TmpVectors.Matrix[0];
    Matrix.FromXYZAxesToRef(localX, boltDir, localZ, rotMatrix);
    Quaternion.FromRotationMatrixToRef(rotMatrix, out);
    return out;
  }

  private static makeBeamMaterial(scene: Scene, name: string, tex: Texture, color: Color3, alpha: number): StandardMaterial {
    const mat = new StandardMaterial(name, scene);
    mat.emissiveColor = color;
    mat.diffuseColor = Color3.Black();
    mat.disableLighting = true;
    mat.backFaceCulling = false;
    mat.emissiveTexture = tex;
    mat.opacityTexture = tex;
    mat.alphaMode = Constants.ALPHA_ADD;
    mat.alpha = alpha;
    return mat;
  }

  private static makeFlareMaterial(scene: Scene): StandardMaterial {
    const tex = BabylonEnergyBeam.getFlareTexture(scene);
    const mat = new StandardMaterial("EnergyBeamFlareMat", scene);
    mat.emissiveColor = new Color3(1.6, 2.2, 3.2);
    mat.diffuseColor = Color3.Black();
    mat.disableLighting = true;
    mat.backFaceCulling = false;
    mat.emissiveTexture = tex;
    mat.opacityTexture = tex;
    mat.alphaMode = Constants.ALPHA_ADD;
    return mat;
  }

  private static getOrCreateSharedLight(scene: Scene): PointLight {
    const cached = BabylonEnergyBeam.sharedLight;
    if (cached && !cached.isDisposed() && cached.getScene() === scene) {
      return cached;
    }
    const light = new PointLight("EnergyBeamSharedLight", new Vector3(0, 0, 0), scene);
    light.diffuse = new Color3(0.4, 0.7, 1.0);
    light.specular = new Color3(0.4, 0.7, 1.0);
    light.range = BabylonEnergyBeam.LIGHT_RANGE;
    light.intensity = 0;
    BabylonEnergyBeam.sharedLight = light;
    return light;
  }

  /**
   * Vertical energy-flow strip: a bright white core fading out to the sides (horizontal), with
   * travelling energy bands and sparkle along its length (vertical). Wraps vertically so the
   * v-scroll loops seamlessly.
   */
  private static getBeamTexture(scene: Scene): RawTexture {
    if (BabylonEnergyBeam.beamTextureCache) {
      return BabylonEnergyBeam.beamTextureCache;
    }
    const w = 64, h = 256;
    const canvas = document.createElement("canvas");
    canvas.width = w;
    canvas.height = h;
    const ctx = canvas.getContext("2d")!;
    ctx.clearRect(0, 0, w, h);

    // Horizontal core profile: hot white center → cyan → transparent edges.
    const core = ctx.createLinearGradient(0, 0, w, 0);
    core.addColorStop(0.00, "rgba(0,0,0,0)");
    core.addColorStop(0.35, "rgba(80,180,255,0.35)");
    core.addColorStop(0.47, "rgba(200,240,255,0.95)");
    core.addColorStop(0.50, "rgba(255,255,255,1)");
    core.addColorStop(0.53, "rgba(200,240,255,0.95)");
    core.addColorStop(0.65, "rgba(80,180,255,0.35)");
    core.addColorStop(1.00, "rgba(0,0,0,0)");
    ctx.fillStyle = core;
    ctx.fillRect(0, 0, w, h);

    // Travelling energy bands along the length.
    for (let i = 0; i < 7; i++) {
      const y = (i / 7) * h + Math.random() * 8;
      const bandH = 4 + Math.random() * 12;
      const band = ctx.createLinearGradient(0, 0, w, 0);
      band.addColorStop(0, "rgba(0,0,0,0)");
      band.addColorStop(0.4, "rgba(160,220,255,0.5)");
      band.addColorStop(0.5, "rgba(255,255,255,0.95)");
      band.addColorStop(0.6, "rgba(160,220,255,0.5)");
      band.addColorStop(1, "rgba(0,0,0,0)");
      ctx.fillStyle = band;
      ctx.fillRect(0, y, w, bandH);
    }

    // Sparkle highlights down the core.
    for (let i = 0; i < 28; i++) {
      const x = w * 0.42 + Math.random() * w * 0.16;
      const y = Math.random() * h;
      const size = 0.5 + Math.random() * 1.3;
      ctx.fillStyle = `rgba(230,245,255,${0.6 + Math.random() * 0.4})`;
      ctx.shadowColor = "rgba(120,200,255,0.9)";
      ctx.shadowBlur = 4;
      ctx.beginPath();
      ctx.arc(x, y, size, 0, Math.PI * 2);
      ctx.fill();
      ctx.shadowBlur = 0;
    }

    const imgData = ctx.getImageData(0, 0, w, h);
    const tex = RawTexture.CreateRGBATexture(new Uint8Array(imgData.data), w, h, scene, true, false);
    tex.hasAlpha = true;
    tex.wrapU = Texture.CLAMP_ADDRESSMODE;
    tex.wrapV = Texture.WRAP_ADDRESSMODE;
    tex.dispose = () => {}; // shared — survives per-beam material disposal
    BabylonEnergyBeam.beamTextureCache = tex;
    return tex;
  }

  /** Soft radial impact flare (white-hot center → blue → transparent). */
  private static getFlareTexture(scene: Scene): RawTexture {
    if (BabylonEnergyBeam.flareTextureCache) {
      return BabylonEnergyBeam.flareTextureCache;
    }
    const size = 256;
    const canvas = document.createElement("canvas");
    canvas.width = size;
    canvas.height = size;
    const ctx = canvas.getContext("2d")!;
    const cx = size / 2, cy = size / 2;
    const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, size / 2);
    grad.addColorStop(0.00, "rgba(255,255,255,1)");
    grad.addColorStop(0.15, "rgba(210,240,255,0.9)");
    grad.addColorStop(0.40, "rgba(120,200,255,0.45)");
    grad.addColorStop(0.70, "rgba(60,140,255,0.12)");
    grad.addColorStop(1.00, "rgba(0,0,0,0)");
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, size, size);
    const imgData = ctx.getImageData(0, 0, size, size);
    const tex = RawTexture.CreateRGBATexture(new Uint8Array(imgData.data), size, size, scene, true, false);
    tex.hasAlpha = true;
    tex.wrapU = Texture.CLAMP_ADDRESSMODE;
    tex.wrapV = Texture.CLAMP_ADDRESSMODE;
    tex.dispose = () => {};
    BabylonEnergyBeam.flareTextureCache = tex;
    return tex;
  }
}
