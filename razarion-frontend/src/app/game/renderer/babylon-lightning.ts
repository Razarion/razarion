import {Color3, Constants, Matrix, Mesh, MeshBuilder, PointLight, Quaternion, RawTexture, StandardMaterial, Texture, TmpVectors, Vector3, VertexData} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";

export class BabylonLightning {
  private static readonly DEFAULT_LIFETIME_MS = 700;
  private static readonly PEAK_MS = 70;
  private static readonly WIDTH = 10.0;
  private static readonly SHEET_URL = "renderer/textures/lightning-sprite.png";
  private static readonly FRAME_MS = 60;
  // 2x2 sprite sheet: cell (uOffset, vOffset) values, scale is 0.5 in both axes.
  private static readonly FRAMES: Array<[number, number]> = [
    [0.0, 0.5], [0.5, 0.5], [0.0, 0.0], [0.5, 0.0],
  ];
  // Endpoint flash spheres at start (muzzle) and end (impact)
  private static readonly FLASH_RAMP_MS = 50;
  private static readonly FLASH_FADE_MS = 250;
  private static readonly FLASH_BASE_DIAMETER = 1.5;
  private static readonly FLASH_PEAK_DIAMETER = 4.0;
  // Dynamic point light at bolt midpoint — illuminates terrain & nearby units
  private static readonly LIGHT_RAMP_MS = 50;
  private static readonly LIGHT_FADE_MS = 300;
  private static readonly LIGHT_PEAK_INTENSITY = 3.0;
  private static readonly LIGHT_RANGE = 18;

  private static textureCache: Texture | null = null;
  private static flashTextureCache: RawTexture | null = null;
  // Shared scene-wide PointLight reused across all bolts. Creating/disposing a PointLight per
  // shot marks every material in the scene as light-dirty, forcing Babylon to recompile shaders
  // on the next frame — under heavy combat that recompile churn dominates the frame budget.
  private static sharedLight: PointLight | null = null;
  private static activeBoltCount = 0;

  /**
   * Pre-creates the shared PointLight and force-runs the entire bolt pipeline (mesh, materials,
   * textures, sprite-sheet animation, billboard quaternion) by firing a real but hidden warmup
   * bolt deep underground at Y=-1000. Call once at scene init.
   *
   * forceCompilation alone proved insufficient: the first user-visible Tesla shot still rendered
   * blank for ~1-2 frames while Babylon finished setting up the GL pipeline / uploading textures.
   * Actually running fire() once makes the cache hit reliably for all subsequent shots.
   */
  static preWarm(scene: Scene): void {
    const hiddenStart = new Vector3(0, -1000, 0);
    const hiddenEnd = new Vector3(0, -1000, 1);
    BabylonLightning.fire(scene, hiddenStart, hiddenEnd, 100);
  }

  private static createBoltMaterial(scene: Scene): StandardMaterial {
    const tex = BabylonLightning.getTexture(scene);
    const mat = new StandardMaterial("LightningMat", scene);
    mat.emissiveColor = new Color3(0.4, 0.7, 1.0);
    mat.diffuseColor = new Color3(0, 0, 0);
    mat.disableLighting = true;
    mat.backFaceCulling = false;
    mat.emissiveTexture = tex;
    mat.opacityTexture = tex;
    return mat;
  }

  private static createFlashMaterial(scene: Scene): StandardMaterial {
    const tex = BabylonLightning.getFlashTexture(scene);
    const mat = new StandardMaterial("LightningFlashMat", scene);
    mat.emissiveColor = new Color3(1.8, 2.5, 3.5);
    mat.diffuseColor = Color3.Black();
    mat.disableLighting = true;
    mat.backFaceCulling = false;
    mat.emissiveTexture = tex;
    mat.opacityTexture = tex;
    mat.alphaMode = Constants.ALPHA_ADD;
    mat.alpha = 0;
    return mat;
  }

  static fire(scene: Scene, start: Vector3, end: Vector3, durationMs?: number): void {
    const dist = Vector3.Distance(start, end);
    if (dist < 0.01) {
      return;
    }
    const lifetimeMs = durationMs ?? BabylonLightning.DEFAULT_LIFETIME_MS;

    const tex = BabylonLightning.getTexture(scene);
    const mat = BabylonLightning.createBoltMaterial(scene);

    const mesh = new Mesh("Lightning", scene);
    mesh.isPickable = false;
    mesh.material = mat;

    const half = BabylonLightning.WIDTH / 2;
    const vertexData = new VertexData();
    vertexData.positions = [-half, 0, 0, half, 0, 0, half, dist, 0, -half, dist, 0];
    vertexData.indices = [0, 1, 2, 0, 2, 3];
    // V-flipped: bright impact burst (bottom of each sheet cell) lands at the bolt
    // end (target); fading tendrils (top of cell) at the start (Tesla origin).
    vertexData.uvs = [0, 1, 1, 1, 1, 0, 0, 0];
    vertexData.normals = [0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1];
    vertexData.applyToMesh(mesh, true);

    mesh.position = start.clone();
    mesh.rotationQuaternion = new Quaternion();

    const boltDir = end.subtract(start).scale(1 / dist);
    const midpoint = Vector3.Center(start, end);

    const flashStart = BabylonLightning.createFlashSphere(scene, start);
    const flashEnd = BabylonLightning.createFlashSphere(scene, end);

    const light = BabylonLightning.getOrCreateSharedLight(scene);
    BabylonLightning.activeBoltCount++;
    const rotMatrix = new Matrix();
    const updateBillboardOrientation = () => {
      const cam = scene.activeCamera;
      if (!cam) {
        return;
      }
      const toCam = TmpVectors.Vector3[0];
      cam.position.subtractToRef(midpoint, toCam);
      const proj = Vector3.Dot(toCam, boltDir);
      const scaledBoltDir = TmpVectors.Vector3[1];
      boltDir.scaleToRef(proj, scaledBoltDir);
      const localZ = TmpVectors.Vector3[2];
      toCam.subtractToRef(scaledBoltDir, localZ);
      const zLen = localZ.length();
      if (zLen < 0.001) {
        return;
      }
      localZ.scaleInPlace(1 / zLen);
      const localX = TmpVectors.Vector3[3];
      Vector3.CrossToRef(boltDir, localZ, localX);
      Matrix.FromXYZAxesToRef(localX, boltDir, localZ, rotMatrix);
      Quaternion.FromRotationMatrixToRef(rotMatrix, mesh.rotationQuaternion!);
    };
    updateBillboardOrientation();

    const startTime = Date.now();
    let callback: (() => void) | null = null;
    callback = () => {
      const elapsed = Date.now() - startTime;
      if (elapsed >= lifetimeMs) {
        if (callback) {
          scene.unregisterBeforeRender(callback);
        }
        mesh.dispose();
        mat.dispose();
        flashStart.mesh.dispose();
        flashStart.mat.dispose();
        flashEnd.mesh.dispose();
        flashEnd.mat.dispose();
        BabylonLightning.activeBoltCount--;
        if (BabylonLightning.activeBoltCount === 0) {
          light.intensity = 0;
        }
        return;
      }
      const frameIdx = ((elapsed / BabylonLightning.FRAME_MS) | 0) % BabylonLightning.FRAMES.length;
      const [u, v] = BabylonLightning.FRAMES[frameIdx];
      tex.uOffset = u;
      tex.vOffset = v;
      updateBillboardOrientation();
      let alpha: number;
      if (elapsed < BabylonLightning.PEAK_MS) {
        alpha = elapsed / BabylonLightning.PEAK_MS;
      } else {
        const fadeT = (elapsed - BabylonLightning.PEAK_MS) / (lifetimeMs - BabylonLightning.PEAK_MS);
        alpha = 1 - fadeT;
      }
      mat.alpha = alpha;

      const flashState = BabylonLightning.computeFlashState(elapsed);
      flashStart.mesh.scaling.setAll(flashState.scale);
      flashEnd.mesh.scaling.setAll(flashState.scale);
      flashStart.mat.alpha = flashState.alpha;
      flashEnd.mat.alpha = flashState.alpha;

      // Last-write-wins on the shared light. With activeBoltCount tracking, we don't reset
      // intensity to 0 until all bolts are gone — overlapping shots flicker between intensity
      // values per frame, which is imperceptible at typical frame rates.
      light.position.copyFrom(midpoint);
      light.intensity = BabylonLightning.computeLightIntensity(elapsed);
    };
    scene.registerBeforeRender(callback);
  }

  private static getOrCreateSharedLight(scene: Scene): PointLight {
    const cached = BabylonLightning.sharedLight;
    if (cached && !cached.isDisposed() && cached.getScene() === scene) {
      return cached;
    }
    const light = new PointLight("LightningSharedLight", new Vector3(0, 0, 0), scene);
    light.diffuse = new Color3(0.5, 0.8, 1.0);
    light.specular = new Color3(0.5, 0.8, 1.0);
    light.range = BabylonLightning.LIGHT_RANGE;
    light.intensity = 0;
    BabylonLightning.sharedLight = light;
    return light;
  }

  private static createFlashSphere(scene: Scene, position: Vector3): { mesh: Mesh, mat: StandardMaterial } {
    const mesh = MeshBuilder.CreatePlane("LightningFlash", {size: 1}, scene);
    mesh.position = position.clone();
    mesh.isPickable = false;
    mesh.billboardMode = Mesh.BILLBOARDMODE_ALL;
    const mat = BabylonLightning.createFlashMaterial(scene);
    mesh.material = mat;
    return {mesh, mat};
  }

  private static getFlashTexture(scene: Scene): RawTexture {
    if (BabylonLightning.flashTextureCache) {
      return BabylonLightning.flashTextureCache;
    }
    const size = 256;
    const canvas = document.createElement("canvas");
    canvas.width = size;
    canvas.height = size;
    const ctx = canvas.getContext("2d")!;
    const cx = size / 2, cy = size / 2;
    const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, size / 2);
    grad.addColorStop(0.00, "rgba(255, 255, 255, 1.0)");
    grad.addColorStop(0.12, "rgba(220, 240, 255, 0.95)");
    grad.addColorStop(0.30, "rgba(150, 200, 255, 0.55)");
    grad.addColorStop(0.55, "rgba(80, 150, 255, 0.18)");
    grad.addColorStop(1.00, "rgba(0, 0, 0, 0)");
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, size, size);
    const imgData = ctx.getImageData(0, 0, size, size);
    const tex = RawTexture.CreateRGBATexture(
      new Uint8Array(imgData.data), size, size, scene, true, false
    );
    tex.hasAlpha = true;
    tex.wrapU = Texture.CLAMP_ADDRESSMODE;
    tex.wrapV = Texture.CLAMP_ADDRESSMODE;
    tex.dispose = () => {};
    BabylonLightning.flashTextureCache = tex;
    return tex;
  }

  private static computeFlashState(elapsed: number): { scale: number, alpha: number } {
    const ramp = BabylonLightning.FLASH_RAMP_MS;
    const fade = BabylonLightning.FLASH_FADE_MS;
    const base = BabylonLightning.FLASH_BASE_DIAMETER;
    const peak = BabylonLightning.FLASH_PEAK_DIAMETER;
    if (elapsed < ramp) {
      const t = elapsed / ramp;
      return {scale: base + (peak - base) * t, alpha: t};
    }
    if (elapsed < ramp + fade) {
      const t = (elapsed - ramp) / fade;
      return {scale: peak * (1 + 0.15 * t), alpha: 1 - t};
    }
    return {scale: peak * 1.15, alpha: 0};
  }

  private static computeLightIntensity(elapsed: number): number {
    const ramp = BabylonLightning.LIGHT_RAMP_MS;
    const fade = BabylonLightning.LIGHT_FADE_MS;
    const peak = BabylonLightning.LIGHT_PEAK_INTENSITY;
    if (elapsed < ramp) {
      return peak * (elapsed / ramp);
    }
    if (elapsed < ramp + fade) {
      const t = (elapsed - ramp) / fade;
      return peak * (1 - t);
    }
    return 0;
  }

  private static getTexture(scene: Scene): Texture {
    if (BabylonLightning.textureCache) {
      return BabylonLightning.textureCache;
    }
    const tex = new Texture(BabylonLightning.SHEET_URL, scene, true, false, Texture.TRILINEAR_SAMPLINGMODE);
    tex.hasAlpha = true;
    tex.uScale = 0.5;
    tex.vScale = 0.5;
    tex.wrapU = Texture.CLAMP_ADDRESSMODE;
    tex.wrapV = Texture.CLAMP_ADDRESSMODE;
    // Material.dispose() on bolt end would otherwise dispose the shared texture.
    tex.dispose = () => {};
    BabylonLightning.textureCache = tex;
    return tex;
  }
}
