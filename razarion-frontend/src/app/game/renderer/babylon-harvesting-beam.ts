import {
  Color3,
  Mesh,
  MeshBuilder,
  RawTexture,
  StandardMaterial,
  Vector3,
  VertexData
} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";

export class BabylonHarvestingBeam {
  private static readonly CRYSTAL_COUNT = 34;
  private static readonly SPIRAL_RADIUS = 0.35;
  private static readonly SPIRAL_TURNS = 3.0;
  private static readonly CRYSTAL_TRAVEL_SECONDS = 3.0;
  private static readonly CRYSTAL_SIZE_MIN = 0.5;
  private static readonly CRYSTAL_SIZE_MAX = 1.6;
  // First chunk of each crystal's life cycle is spent jittering at the resource
  // before it gets picked up by the helix. This is what makes them look like they
  // were just chipped off the rock rather than spawning in mid-air.
  private static readonly BIRTH_FRAC = 0.25;
  private static readonly SOURCE_SPHERE_RADIUS = 0.5;

  private crystalSizes: number[] = [];
  private crystalSourceOffsets: Vector3[] = [];

  private beam: Mesh | null = null;
  private beamMaterial: StandardMaterial | null = null;
  private beamTexture: RawTexture | null = null;
  private crystalMesh: Mesh | null = null;
  private crystalMaterial: StandardMaterial | null = null;
  private crystalTexture: RawTexture | null = null;
  private crystalMatrices: Float32Array = new Float32Array(BabylonHarvestingBeam.CRYSTAL_COUNT * 16);
  private beamTarget: Vector3 | null = null;
  private beamStartTime: number = 0;
  private beamRenderCallback: (() => void) | null = null;

  constructor(
    private scene: Scene,
    private getBeamOrigin: () => Vector3 | null,
    private getContainerPosition: () => Vector3
  ) {
  }

  isActive(): boolean {
    return this.beamRenderCallback !== null;
  }

  start(target: Vector3): void {
    if (this.beamRenderCallback) {
      return;
    }

    try {
      this.beamTarget = target;
      this.beamStartTime = Date.now();

      const sizeRange = BabylonHarvestingBeam.CRYSTAL_SIZE_MAX - BabylonHarvestingBeam.CRYSTAL_SIZE_MIN;
      this.crystalSizes = [];
      this.crystalSourceOffsets = [];
      for (let i = 0; i < BabylonHarvestingBeam.CRYSTAL_COUNT; i++) {
        this.crystalSizes.push(BabylonHarvestingBeam.CRYSTAL_SIZE_MIN + Math.random() * sizeRange);

        // Uniform random point inside a sphere around the resource (cube root keeps
        // density even — without it, points cluster at the surface).
        const phi = Math.random() * Math.PI * 2;
        const cosTheta = 2 * Math.random() - 1;
        const sinTheta = Math.sqrt(Math.max(0, 1 - cosTheta * cosTheta));
        const r = Math.cbrt(Math.random()) * BabylonHarvestingBeam.SOURCE_SPHERE_RADIUS;
        this.crystalSourceOffsets.push(new Vector3(
          r * sinTheta * Math.cos(phi),
          r * sinTheta * Math.sin(phi),
          r * cosTheta
        ));
      }

      this.beamRenderCallback = () => this.update();
      this.scene.registerBeforeRender(this.beamRenderCallback);
    } catch (e) {
      console.error(e);
    }
  }

  dispose(): void {
    if (this.beamRenderCallback) {
      this.scene.unregisterBeforeRender(this.beamRenderCallback);
      this.beamRenderCallback = null;
    }
    if (this.beam) {
      this.beam.dispose();
      this.beam = null;
    }
    if (this.beamMaterial) {
      this.beamMaterial.dispose();
      this.beamMaterial = null;
    }
    if (this.beamTexture) {
      this.beamTexture.dispose();
      this.beamTexture = null;
    }
    if (this.crystalMesh) {
      this.crystalMesh.dispose();
      this.crystalMesh = null;
    }
    if (this.crystalMaterial) {
      this.crystalMaterial.dispose();
      this.crystalMaterial = null;
    }
    if (this.crystalTexture) {
      this.crystalTexture.dispose();
      this.crystalTexture = null;
    }
    this.beamTarget = null;
  }

  private update(): void {
    const startPos = this.getBeamOrigin() ?? this.getContainerPosition();

    if (!this.beamTarget) {
      return;
    }

    const endPos = this.beamTarget.clone();

    if (Vector3.Distance(startPos, endPos) < 0.01) {
      return;
    }

    if (!this.beamMaterial) {
      const beamTex = BabylonHarvestingBeam.createRedBeamTexture(this.scene);
      this.beamTexture = beamTex;
      const beamMat = new StandardMaterial("HarvestingBeamMat", this.scene);
      beamMat.emissiveColor = new Color3(3, 0.3, 0.3);
      beamMat.diffuseColor = new Color3(0, 0, 0);
      beamMat.disableLighting = true;
      beamMat.backFaceCulling = false;
      beamMat.useEmissiveAsIllumination = true;
      beamMat.emissiveTexture = beamTex;
      beamMat.opacityTexture = beamTex;
      this.beamMaterial = beamMat;
    }

    const dist = Vector3.Distance(startPos, endPos);
    const dir = endPos.subtract(startPos).normalize();
    const angle = Math.atan2(dir.x, dir.z);
    const hDist = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
    const pitch = Math.atan2(dir.y, hDist);

    const narrowWidth = 0.15;
    const wideWidth = 0.30;

    if (!this.beam) {
      this.beam = new Mesh("HarvestingBeam", this.scene);
      this.beam.isPickable = false;
      this.beam.material = this.beamMaterial;
    }

    const vertexData = new VertexData();
    vertexData.positions = [
      -narrowWidth / 2, 0, 0,
      narrowWidth / 2, 0, 0,
      wideWidth / 2, dist, 0,
      -wideWidth / 2, dist, 0,
    ];
    vertexData.indices = [0, 1, 2, 0, 2, 3];
    vertexData.uvs = [0, 0, 1, 0, 1, 1, 0, 1];
    vertexData.normals = [0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1];
    vertexData.applyToMesh(this.beam, true);

    this.beam.position = startPos.clone();
    this.beam.rotationQuaternion = null;
    this.beam.rotation.set(Math.PI / 2 - pitch, angle, 0);

    const elapsed = (Date.now() - this.beamStartTime) / 1000;
    (this.beamMaterial.emissiveTexture as any).vOffset = elapsed * 1.2;

    this.updateCrystals(startPos, endPos, dir, elapsed);
  }

  private updateCrystals(startPos: Vector3, endPos: Vector3, dir: Vector3, elapsed: number): void {
    if (!this.crystalMaterial) {
      const tex = BabylonHarvestingBeam.createCrystalTexture(this.scene);
      this.crystalTexture = tex;
      const mat = new StandardMaterial("HarvestCrystalMat", this.scene);
      mat.emissiveTexture = tex;
      mat.opacityTexture = tex;
      mat.emissiveColor = new Color3(0.2, 1.0, 1.2);
      mat.diffuseColor = new Color3(0, 0, 0);
      mat.disableLighting = true;
      mat.backFaceCulling = false;
      mat.useEmissiveAsIllumination = true;
      this.crystalMaterial = mat;
    }

    if (!this.crystalMesh) {
      this.crystalMesh = MeshBuilder.CreatePlane("HarvestCrystal", {size: 0.22}, this.scene);
      this.crystalMesh.material = this.crystalMaterial;
      this.crystalMesh.isPickable = false;
      this.crystalMesh.thinInstanceEnablePicking = false;
    }

    // Two perpendicular axes spanning the plane normal to the beam direction.
    // If the beam is nearly vertical, fall back to X axis to avoid a degenerate cross.
    let perp1: Vector3;
    if (Math.abs(dir.y) < 0.99) {
      perp1 = Vector3.Cross(dir, Vector3.Up()).normalize();
    } else {
      perp1 = Vector3.Cross(dir, new Vector3(1, 0, 0)).normalize();
    }
    const perp2 = Vector3.Cross(dir, perp1).normalize();

    const count = BabylonHarvestingBeam.CRYSTAL_COUNT;
    const radius = BabylonHarvestingBeam.SPIRAL_RADIUS;
    const turns = BabylonHarvestingBeam.SPIRAL_TURNS;
    const cycle = BabylonHarvestingBeam.CRYSTAL_TRAVEL_SECONDS;

    const birthFrac = BabylonHarvestingBeam.BIRTH_FRAC;

    // Camera position drives per-instance billboard orientation. activeCamera is
    // expected during render, but fall back to identity if it's missing for any reason.
    const cam = this.scene.activeCamera;
    const camX = cam ? cam.position.x : 0;
    const camY = cam ? cam.position.y : 0;
    const camZ = cam ? cam.position.z : 0;

    const m = this.crystalMatrices;

    for (let i = 0; i < count; i++) {
      const phaseOffset = i / count;
      const t = ((elapsed / cycle) + phaseOffset) % 1;

      const sourceOffset = this.crystalSourceOffsets[i];

      let posX: number, posY: number, posZ: number;

      if (t < birthFrac) {
        // Birth phase: crystal sits in its random spawn pocket around the rock
        // and bobs lightly, like a freshly broken-off chunk.
        const bob = 0.04;
        const jx = Math.sin(elapsed * 6 + i * 1.7) * bob;
        const jy = Math.cos(elapsed * 5 + i * 2.1) * bob;
        const jz = Math.sin(elapsed * 4 + i * 0.9) * bob;
        posX = endPos.x + sourceOffset.x + jx;
        posY = endPos.y + sourceOffset.y + jy;
        posZ = endPos.z + sourceOffset.z + jz;
      } else {
        const helixT = (t - birthFrac) / (1 - birthFrac);
        const theta = turns * Math.PI * 2 * helixT + phaseOffset * Math.PI * 2;

        const along = Vector3.Lerp(endPos, startPos, helixT);
        const offX = Math.cos(theta) * radius;
        const offY = Math.sin(theta) * radius;
        const helixX = along.x + perp1.x * offX + perp2.x * offY;
        const helixY = along.y + perp1.y * offX + perp2.y * offY;
        const helixZ = along.z + perp1.z * offX + perp2.z * offY;

        const settle = Math.min(1, helixT * 4);
        const srcX = endPos.x + sourceOffset.x;
        const srcY = endPos.y + sourceOffset.y;
        const srcZ = endPos.z + sourceOffset.z;
        posX = srcX + (helixX - srcX) * settle;
        posY = srcY + (helixY - srcY) * settle;
        posZ = srcZ + (helixZ - srcZ) * settle;
      }

      const fadeIn = Math.min(1, t * 20);
      const fadeOut = Math.min(1, (1 - t) * 8);
      const fade = Math.min(fadeIn, fadeOut);
      const scale = (0.5 + 0.5 * fade) * this.crystalSizes[i];

      // Billboard basis: build (right, up, forward) where forward points to camera.
      // forward = normalize(cam - pos)
      let fX = camX - posX;
      let fY = camY - posY;
      let fZ = camZ - posZ;
      const fLen = Math.sqrt(fX * fX + fY * fY + fZ * fZ) || 1;
      fX /= fLen; fY /= fLen; fZ /= fLen;

      // right = worldUp × forward, fallback if forward ~ worldUp
      let rX: number, rY: number, rZ: number;
      if (Math.abs(fY) > 0.999) {
        rX = 1; rY = 0; rZ = 0;
      } else {
        // (0,1,0) × (fX,fY,fZ) = (fZ, 0, -fX)
        rX = fZ; rY = 0; rZ = -fX;
        const rLen = Math.sqrt(rX * rX + rZ * rZ) || 1;
        rX /= rLen; rZ /= rLen;
      }

      // up = forward × right
      const uX = fY * rZ - fZ * rY;
      const uY = fZ * rX - fX * rZ;
      const uZ = fX * rY - fY * rX;

      // Per-instance twist around the camera axis — gives each crystal a slowly
      // tumbling in-plane rotation so they don't all read as flat cards.
      const twist = elapsed * 1.2 + i * 0.8;
      const ct = Math.cos(twist);
      const st = Math.sin(twist);

      // Final basis after twisting (right, up) within their plane:
      const fRx = (rX * ct + uX * st) * scale;
      const fRy = (rY * ct + uY * st) * scale;
      const fRz = (rZ * ct + uZ * st) * scale;
      const fUx = (uX * ct - rX * st) * scale;
      const fUy = (uY * ct - rY * st) * scale;
      const fUz = (uZ * ct - rZ * st) * scale;
      const fFx = fX * scale;
      const fFy = fY * scale;
      const fFz = fZ * scale;

      // Babylon stores matrices column-major: cols are X-axis, Y-axis, Z-axis, translation.
      const off = i * 16;
      m[off + 0] = fRx;  m[off + 1] = fRy;  m[off + 2] = fRz;  m[off + 3] = 0;
      m[off + 4] = fUx;  m[off + 5] = fUy;  m[off + 6] = fUz;  m[off + 7] = 0;
      m[off + 8] = fFx;  m[off + 9] = fFy;  m[off + 10] = fFz; m[off + 11] = 0;
      m[off + 12] = posX; m[off + 13] = posY; m[off + 14] = posZ; m[off + 15] = 1;
    }

    this.crystalMesh.thinInstanceSetBuffer("matrix", this.crystalMatrices, 16, false);
  }

  private static createRedBeamTexture(scene: Scene): RawTexture {
    const w = 64, h = 256;
    const canvas = document.createElement("canvas");
    canvas.width = w;
    canvas.height = h;
    const ctx = canvas.getContext("2d")!;

    ctx.clearRect(0, 0, w, h);

    // Soft red halo across the beam width
    const halo = ctx.createLinearGradient(0, 0, w, 0);
    halo.addColorStop(0, "rgba(0,0,0,0)");
    halo.addColorStop(0.4, "rgba(255,40,40,0.2)");
    halo.addColorStop(0.5, "rgba(255,80,80,0.5)");
    halo.addColorStop(0.6, "rgba(255,40,40,0.2)");
    halo.addColorStop(1, "rgba(0,0,0,0)");
    ctx.fillStyle = halo;
    ctx.fillRect(0, 0, w, h);

    // Hot bright core
    const core = ctx.createLinearGradient(0, 0, w, 0);
    core.addColorStop(0, "rgba(0,0,0,0)");
    core.addColorStop(0.45, "rgba(255,160,160,0.85)");
    core.addColorStop(0.5, "rgba(255,255,240,1)");
    core.addColorStop(0.55, "rgba(255,160,160,0.85)");
    core.addColorStop(1, "rgba(0,0,0,0)");
    ctx.fillStyle = core;
    ctx.fillRect(0, 0, w, h);

    // Energy pulses traveling along the beam
    for (let i = 0; i < 6; i++) {
      const y = (i / 6) * h;
      const bandH = 6 + Math.random() * 14;
      const pulse = ctx.createLinearGradient(0, 0, w, 0);
      pulse.addColorStop(0, "rgba(0,0,0,0)");
      pulse.addColorStop(0.35, "rgba(255,90,90,0.4)");
      pulse.addColorStop(0.5, "rgba(255,255,220,0.9)");
      pulse.addColorStop(0.65, "rgba(255,90,90,0.4)");
      pulse.addColorStop(1, "rgba(0,0,0,0)");
      ctx.fillStyle = pulse;
      ctx.fillRect(0, y, w, bandH);
    }

    // Center sparkle highlights
    for (let i = 0; i < 30; i++) {
      const x = w * 0.4 + Math.random() * w * 0.2;
      const y = Math.random() * h;
      const size = 0.5 + Math.random() * 1.4;
      const alpha = 0.6 + Math.random() * 0.4;
      ctx.fillStyle = `rgba(255,220,220,${alpha})`;
      ctx.shadowColor = "rgba(255,80,80,0.9)";
      ctx.shadowBlur = 4;
      ctx.beginPath();
      ctx.arc(x, y, size, 0, Math.PI * 2);
      ctx.fill();
      ctx.shadowBlur = 0;
    }

    const imgData = ctx.getImageData(0, 0, w, h);
    const tex = RawTexture.CreateRGBATexture(
      new Uint8Array(imgData.data), w, h, scene, true, false
    );
    tex.hasAlpha = true;
    tex.wrapU = RawTexture.WRAP_ADDRESSMODE;
    tex.wrapV = RawTexture.WRAP_ADDRESSMODE;

    return tex;
  }

  /**
   * Crystal shard texture: an asymmetric polygonal outline filled with a bright
   * radial gradient, with a few facet ridges and a sparkle highlight. Drawn once
   * per beam instance — the per-instance twist + scale makes them look distinct
   * even though the source bitmap is shared.
   */
  private static createCrystalTexture(scene: Scene): RawTexture {
    const size = 256;
    const canvas = document.createElement("canvas");
    canvas.width = size;
    canvas.height = size;
    const ctx = canvas.getContext("2d")!;
    ctx.clearRect(0, 0, size, size);

    const cx = size / 2;
    const cy = size / 2;

    // Asymmetric crystal-shard outline (slightly irregular hexagon, tall axis).
    const outline: [number, number][] = [
      [cx - 4, cy - 95],   // top apex
      [cx - 48, cy - 45],  // upper left shoulder
      [cx - 56, cy + 25],  // mid left
      [cx - 22, cy + 92],  // lower left
      [cx + 18, cy + 96],  // bottom apex
      [cx + 54, cy + 30],  // mid right
      [cx + 42, cy - 50],  // upper right shoulder
    ];

    const traceOutline = () => {
      ctx.beginPath();
      outline.forEach(([x, y], i) => i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y));
      ctx.closePath();
    };

    // Outer halo glow — saturated turquoise to match the resource stone colour.
    ctx.shadowColor = "rgba(60, 200, 220, 0.95)";
    ctx.shadowBlur = 22;
    ctx.fillStyle = "rgba(40, 170, 200, 0.6)";
    traceOutline();
    ctx.fill();
    ctx.shadowBlur = 0;

    // Main body: bright turquoise core (NO pure white — we don't want emissive
    // boost to saturate it back to white) out to a deep teal rim.
    const body = ctx.createRadialGradient(cx - 10, cy - 22, 4, cx, cy + 8, 105);
    body.addColorStop(0, "rgba(150, 230, 240, 1)");
    body.addColorStop(0.18, "rgba(110, 210, 225, 0.95)");
    body.addColorStop(0.5, "rgba(40, 170, 205, 0.9)");
    body.addColorStop(0.85, "rgba(15, 100, 150, 0.78)");
    body.addColorStop(1, "rgba(5, 50, 90, 0.55)");
    ctx.fillStyle = body;
    traceOutline();
    ctx.fill();

    // Facet ridges: bright lines from the inner core to each vertex split the
    // shard into triangular facets and read as crystal edges.
    ctx.strokeStyle = "rgba(150, 230, 240, 0.55)";
    ctx.lineWidth = 1.4;
    ctx.lineCap = "round";
    outline.forEach(([x, y]) => {
      ctx.beginPath();
      ctx.moveTo(cx - 6, cy - 12);
      ctx.lineTo(x, y);
      ctx.stroke();
    });

    // Bright outline highlight on the sun-facing left edges. Bright turquoise
    // rather than white so it stays in the colour family.
    ctx.strokeStyle = "rgba(160, 230, 240, 0.9)";
    ctx.lineWidth = 2.5;
    ctx.beginPath();
    ctx.moveTo(outline[0][0], outline[0][1]);
    ctx.lineTo(outline[1][0], outline[1][1]);
    ctx.lineTo(outline[2][0], outline[2][1]);
    ctx.stroke();

    // Subtle darker outline on the shadowed right edges to give 3D feel.
    ctx.strokeStyle = "rgba(10, 50, 80, 0.65)";
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.moveTo(outline[4][0], outline[4][1]);
    ctx.lineTo(outline[5][0], outline[5][1]);
    ctx.lineTo(outline[6][0], outline[6][1]);
    ctx.stroke();

    // Sparkle highlight near the upper apex.
    const spark = ctx.createRadialGradient(cx - 12, cy - 55, 0, cx - 12, cy - 55, 18);
    spark.addColorStop(0, "rgba(170, 235, 245, 1)");
    spark.addColorStop(0.4, "rgba(110, 210, 230, 0.7)");
    spark.addColorStop(1, "rgba(40, 170, 200, 0)");
    ctx.fillStyle = spark;
    ctx.fillRect(cx - 40, cy - 80, 60, 50);

    const imgData = ctx.getImageData(0, 0, size, size);
    const tex = RawTexture.CreateRGBATexture(
      new Uint8Array(imgData.data), size, size, scene, true, false
    );
    tex.hasAlpha = true;
    return tex;
  }
}
