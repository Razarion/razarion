import {
  Color3,
  Color4,
  GlowLayer,
  Material,
  Mesh,
  MeshBuilder,
  ParticleSystem,
  RawTexture,
  StandardMaterial,
  Vector3
} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";
import {GridMaterial} from "@babylonjs/materials/grid/gridMaterial";

export interface BuildingBeamOriginProvider {
  getBeamOrigin(): Vector3 | null;
  getContainerPosition(): Vector3;
}

export class BabylonBuildupEffect {
  private meshes: Mesh[] | null = null;
  private originalMaterials: Map<Mesh, Material | null> = new Map();
  private material: GridMaterial | null = null;
  private initialized = false;
  private worldMinY = 0;
  private worldMaxY = 0;
  scanLine: Mesh | null = null;
  private scanLineMaterial: StandardMaterial | null = null;
  private gridDisc: Mesh | null = null;
  private gridDisc2: Mesh | null = null;
  private discRenderCallback: (() => void) | null = null;
  private particleSystem: ParticleSystem | null = null;
  private glowLayer: GlowLayer | null = null;
  // Building beam fields
  private buildingBeam: Mesh | null = null;
  private buildingBeamMaterial: StandardMaterial | null = null;
  private buildingBeamTarget: Vector3 | null = null;
  private buildingBeamStartTime: number = 0;
  private buildingBeamRenderCallback: (() => void) | null = null;
  private buildingBeamOriginProvider: BuildingBeamOriginProvider | null = null;
  private findScanLinePosition: ((target: Vector3) => Vector3 | null) | null = null;

  private static hologramTextureCache: RawTexture | null = null;

  constructor(
    private scene: Scene,
    private container: Mesh,
    private diplomacyColor: Color3,
    private radius: number
  ) {
  }

  isInitialized(): boolean {
    return this.initialized;
  }

  hasScanLine(): boolean {
    return this.scanLine !== null;
  }

  init(): boolean {
    const meshes = (this.container.getChildMeshes() as Mesh[]).filter(m => m.isVisible);
    if (meshes.length === 0) {
      return false;
    }

    let minY = Infinity, maxY = -Infinity;
    meshes.forEach(mesh => {
      mesh.computeWorldMatrix(true);
      const bi = mesh.getBoundingInfo();
      minY = Math.min(minY, bi.boundingBox.minimumWorld.y);
      maxY = Math.max(maxY, bi.boundingBox.maximumWorld.y);
    });

    if (maxY <= minY) {
      return false;
    }

    this.initialized = true;
    this.worldMinY = minY;
    this.worldMaxY = maxY;
    this.meshes = meshes;
    meshes.forEach(m => {
      this.originalMaterials.set(m, m.material);
      m.isVisible = false;
    });

    const buildupMat = new GridMaterial("BuildupMat", this.scene);
    buildupMat.mainColor = this.diplomacyColor.scale(0.05);
    buildupMat.lineColor = this.diplomacyColor;
    buildupMat.gridRatio = 0.15;
    buildupMat.majorUnitFrequency = 5;
    buildupMat.minorUnitVisibility = 0.6;
    buildupMat.opacity = 1.0;
    buildupMat.backFaceCulling = false;
    this.material = buildupMat;
    return true;
  }

  createRing(): void {
    this.scanLine = MeshBuilder.CreateTorus("BuildupScanLine", {
      diameter: this.radius * 3,
      thickness: 0.05,
      tessellation: 32
    }, this.scene);
    this.scanLine.parent = this.container;
    this.scanLine.isPickable = false;
    this.scanLine.isVisible = false;

    const gl = new GlowLayer("BuildupGlow", this.scene, {
      blurKernelSize: 128,
      mainTextureFixedSize: 256
    });
    gl.intensity = 5.0;
    gl.addIncludedOnlyMesh(this.scanLine as Mesh);
    this.glowLayer = gl;

    const holoTex = BabylonBuildupEffect.createHologramTexture(this.scene);

    const createDisc = (name: string): Mesh => {
      const disc = MeshBuilder.CreateDisc(name, {
        radius: this.radius * 2.5,
        tessellation: 32
      }, this.scene);
      disc.parent = this.scanLine;
      disc.rotation.x = Math.PI / 2;
      disc.isPickable = false;

      const mat = new StandardMaterial(name + "Mat", this.scene);
      mat.emissiveColor = new Color3(0, 3, 3);
      mat.diffuseColor = new Color3(0, 0, 0);
      mat.disableLighting = true;
      mat.alpha = 1.0;
      mat.backFaceCulling = false;
      mat.useEmissiveAsIllumination = true;
      mat.emissiveTexture = holoTex;
      mat.opacityTexture = holoTex;
      disc.material = mat;
      gl.addIncludedOnlyMesh(disc);
      return disc;
    };

    this.gridDisc = createDisc("BuildupGrid1");
    this.gridDisc2 = createDisc("BuildupGrid2");
    this.gridDisc2.position.y = 0.01;

    const cycleDuration = 3000;
    const startTime = Date.now();

    this.discRenderCallback = () => {
      const elapsed = Date.now() - startTime;
      const t1 = (elapsed % cycleDuration) / cycleDuration;
      const t2 = ((elapsed + cycleDuration / 2) % cycleDuration) / cycleDuration;

      const scale1 = t1;
      const scale2 = t2;
      const alpha1 = Math.sin(t1 * Math.PI);
      const alpha2 = Math.sin(t2 * Math.PI);

      if (this.gridDisc) {
        this.gridDisc.scaling.set(scale1, scale1, scale1);
        (this.gridDisc.material as StandardMaterial).alpha = alpha1;
      }
      if (this.gridDisc2) {
        this.gridDisc2.scaling.set(scale2, scale2, scale2);
        (this.gridDisc2.material as StandardMaterial).alpha = alpha2;
      }
    };
    this.scene.registerBeforeRender(this.discRenderCallback);

    // Spark particle system around the ring with animated sprite sheet
    const ps = new ParticleSystem("BuildupSparks", 200, this.scene);

    const cellSize = 64;
    const cols = 4, rows = 4;
    const sheetCanvas = document.createElement("canvas");
    sheetCanvas.width = cellSize * cols;
    sheetCanvas.height = cellSize * rows;
    const sCtx = sheetCanvas.getContext("2d")!;

    const drawLightningBolt = (ctx: CanvasRenderingContext2D, x1: number, y1: number, x2: number, y2: number, segments: number, jitter: number, lineWidth: number, alpha: number) => {
      const dx = x2 - x1, dy = y2 - y1;
      const perpX = -dy, perpY = dx;
      const len = Math.sqrt(perpX * perpX + perpY * perpY);
      const nx = len > 0 ? perpX / len : 0, ny = len > 0 ? perpY / len : 0;
      const points: { x: number, y: number }[] = [{x: x1, y: y1}];
      for (let i = 1; i < segments; i++) {
        const t = i / segments;
        const offset = (Math.random() - 0.5) * 2 * jitter;
        points.push({x: x1 + dx * t + nx * offset, y: y1 + dy * t + ny * offset});
      }
      points.push({x: x2, y: y2});

      ctx.strokeStyle = `rgba(0,180,255,${alpha * 0.4})`;
      ctx.lineWidth = lineWidth * 3;
      ctx.lineCap = "round";
      ctx.lineJoin = "round";
      ctx.beginPath();
      points.forEach((p, i) => i === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y));
      ctx.stroke();

      ctx.strokeStyle = `rgba(255,255,255,${alpha})`;
      ctx.lineWidth = lineWidth;
      ctx.beginPath();
      points.forEach((p, i) => i === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y));
      ctx.stroke();
    };

    for (let row = 0; row < rows; row++) {
      for (let col = 0; col < cols; col++) {
        const frame = row * cols + col;
        const cx = col * cellSize + cellSize / 2;
        const cy = row * cellSize + cellSize / 2;
        const intensity = 1.0 - frame / (cols * rows);

        sCtx.save();
        sCtx.translate(cx, cy);

        const half = cellSize * 0.4;
        const angle = Math.random() * Math.PI * 2;

        drawLightningBolt(sCtx,
          Math.cos(angle) * half * intensity, Math.sin(angle) * half * intensity,
          -Math.cos(angle) * half * intensity, -Math.sin(angle) * half * intensity,
          5, half * 0.3, 1.5 * intensity + 0.5, intensity
        );

        if (intensity > 0.3) {
          const branchAngle = angle + (Math.random() - 0.5) * Math.PI;
          drawLightningBolt(sCtx,
            0, 0,
            Math.cos(branchAngle) * half * 0.6 * intensity, Math.sin(branchAngle) * half * 0.6 * intensity,
            3, half * 0.2, 1.0 * intensity, intensity * 0.7
          );
        }

        sCtx.restore();
      }
    }

    const imgData = sCtx.getImageData(0, 0, sheetCanvas.width, sheetCanvas.height);
    ps.particleTexture = RawTexture.CreateRGBATexture(
      new Uint8Array(imgData.data), sheetCanvas.width, sheetCanvas.height, this.scene, false, false
    );
    ps.particleTexture.hasAlpha = true;

    ps.isAnimationSheetEnabled = true;
    ps.spriteCellWidth = cellSize;
    ps.spriteCellHeight = cellSize;
    ps.startSpriteCellID = 0;
    ps.endSpriteCellID = cols * rows - 1;
    ps.spriteCellChangeSpeed = 4;
    ps.spriteRandomStartCell = true;

    ps.emitter = this.scanLine;
    ps.minEmitBox = new Vector3(-this.radius, 0, -this.radius);
    ps.maxEmitBox = new Vector3(this.radius, 0, this.radius);

    ps.color1 = new Color4(0, 0.8, 1, 1);
    ps.color2 = new Color4(0.5, 0.9, 1, 1);
    ps.colorDead = new Color4(0, 0.1, 0.3, 0);

    ps.addColorGradient(0, new Color4(1, 1, 1, 1));
    ps.addColorGradient(0.1, new Color4(0, 0.8, 1, 1));
    ps.addColorGradient(1, new Color4(0, 0.1, 0.3, 0));

    ps.addSizeGradient(0, 0.6);
    ps.addSizeGradient(0.1, 1.0);
    ps.addSizeGradient(1, 0.1);

    ps.minLifeTime = 0.1;
    ps.maxLifeTime = 0.4;
    ps.emitRate = 300;
    ps.direction1 = new Vector3(-0.3, 0.3, -0.3);
    ps.direction2 = new Vector3(0.3, 1.0, 0.3);
    ps.minEmitPower = 0.1;
    ps.maxEmitPower = 0.4;
    ps.gravity = new Vector3(0, -0.5, 0);
    ps.blendMode = ParticleSystem.BLENDMODE_ONEONE;
    ps.minAngularSpeed = -Math.PI;
    ps.maxAngularSpeed = Math.PI;

    ps.start();
    this.particleSystem = ps;
  }

  updateVisibility(buildup: number): void {
    const range = this.worldMaxY - this.worldMinY;
    // Use container Y (ground level) as base, not worldMinY which can be below ground
    const groundY = this.container.position.y;
    const minStartY = Math.max(this.worldMinY, groundY) + 0.2;
    const remainingRange = this.worldMaxY - minStartY;
    const worldClipY = minStartY + remainingRange * buildup;

    if (this.scanLine) {
      this.scanLine.position.y = worldClipY - this.container.position.y;
    }

    if (this.meshes) {
      this.meshes.forEach(mesh => {
        const bb = mesh.getBoundingInfo().boundingBox;
        const meshMaxY = bb.maximumWorld.y;

        if (meshMaxY <= worldClipY) {
          mesh.isVisible = true;
          mesh.material = this.originalMaterials.get(mesh) ?? mesh.material;
        } else {
          mesh.isVisible = true;
          mesh.material = this.material;
        }
      });
    }
  }

  complete(): void {
    if (this.meshes != null) {
      this.meshes.forEach(m => m.isVisible = true);
      this.meshes = null;
      this.cleanupRing();

      this.originalMaterials.forEach((originalMat, mesh) => {
        mesh.material = originalMat;
      });
      this.originalMaterials.clear();

      if (this.material) {
        this.material.dispose();
        this.material = null;
      }

      this.initialized = false;
    }
  }

  cleanupRing(): void {
    if (this.discRenderCallback) {
      this.scene.unregisterBeforeRender(this.discRenderCallback);
      this.discRenderCallback = null;
    }
    if (this.glowLayer) {
      this.glowLayer.dispose();
      this.glowLayer = null;
    }
    if (this.particleSystem) {
      this.particleSystem.dispose();
      this.particleSystem = null;
    }
    if (this.gridDisc) {
      this.gridDisc.material?.dispose();
      this.gridDisc.dispose();
      this.gridDisc = null;
    }
    if (this.gridDisc2) {
      this.gridDisc2.material?.dispose();
      this.gridDisc2.dispose();
      this.gridDisc2 = null;
    }
    if (this.scanLine) {
      this.scanLine.dispose();
      this.scanLine = null;
    }
    if (this.scanLineMaterial) {
      this.scanLineMaterial.dispose();
      this.scanLineMaterial = null;
    }
  }

  cleanup(): void {
    this.cleanupRing();
    this.disposeBuildingBeam();

    this.originalMaterials.forEach((originalMat, mesh) => {
      mesh.material = originalMat;
    });
    this.originalMaterials.clear();

    if (this.material) {
      this.material.dispose();
      this.material = null;
    }

    this.initialized = false;
  }

  // --- Building beam (from builder unit to this building) ---

  isBuildingBeamActive(): boolean {
    return this.buildingBeamRenderCallback !== null;
  }

  startBuildingBeam(target: Vector3, originProvider: BuildingBeamOriginProvider, findScanLine: (target: Vector3) => Vector3 | null): void {
    if (this.buildingBeamRenderCallback) {
      return;
    }

    try {
      this.buildingBeamTarget = target;
      this.buildingBeamStartTime = Date.now();
      this.buildingBeamOriginProvider = originProvider;
      this.findScanLinePosition = findScanLine;

      this.buildingBeamRenderCallback = () => this.updateBuildingBeam();
      this.scene.registerBeforeRender(this.buildingBeamRenderCallback);
    } catch (e) {
      console.error(e);
    }
  }

  disposeBuildingBeam(): void {
    if (this.buildingBeamRenderCallback) {
      this.scene.unregisterBeforeRender(this.buildingBeamRenderCallback);
      this.buildingBeamRenderCallback = null;
    }
    if (this.buildingBeam) {
      this.buildingBeam.dispose();
      this.buildingBeam = null;
    }
    if (this.buildingBeamMaterial) {
      this.buildingBeamMaterial.dispose();
      this.buildingBeamMaterial = null;
    }
    this.buildingBeamTarget = null;
    this.buildingBeamOriginProvider = null;
    this.findScanLinePosition = null;
  }

  private updateBuildingBeam(): void {
    if (!this.buildingBeamTarget || !this.buildingBeamOriginProvider) {
      return;
    }

    const startPos = this.buildingBeamOriginProvider.getBeamOrigin() ?? this.buildingBeamOriginProvider.getContainerPosition();
    const endPos = this.findScanLinePosition?.(this.buildingBeamTarget) ?? this.buildingBeamTarget.clone();

    if (Vector3.Distance(startPos, endPos) < 0.01) {
      return;
    }

    if (!this.buildingBeamMaterial) {
      const beamTex = BabylonBuildupEffect.createBeamTexture(this.scene);
      const beamMat = new StandardMaterial("BuildingBeamMat", this.scene);
      beamMat.emissiveColor = new Color3(0, 3, 3);
      beamMat.diffuseColor = new Color3(0, 0, 0);
      beamMat.disableLighting = true;
      beamMat.backFaceCulling = false;
      beamMat.useEmissiveAsIllumination = true;
      beamMat.emissiveTexture = beamTex;
      beamMat.opacityTexture = beamTex;
      this.buildingBeamMaterial = beamMat;
    }

    const dist = Vector3.Distance(startPos, endPos);
    const midPos = Vector3.Lerp(startPos, endPos, 0.5);
    const dir = endPos.subtract(startPos).normalize();
    const angle = Math.atan2(dir.x, dir.z);

    if (!this.buildingBeam) {
      this.buildingBeam = MeshBuilder.CreatePlane("BuildingBeam", {width: 1, height: 1}, this.scene);
      this.buildingBeam.isPickable = false;
      this.buildingBeam.material = this.buildingBeamMaterial;
    }

    this.buildingBeam.position = new Vector3(midPos.x, startPos.y, midPos.z);
    this.buildingBeam.rotationQuaternion = null;
    this.buildingBeam.rotation.set(Math.PI / 2, angle, 0);
    this.buildingBeam.scaling.set(0.4, dist, 1);

    // Scroll texture along beam (energy flowing to building)
    const elapsed = (Date.now() - this.buildingBeamStartTime) / 1000;
    (this.buildingBeamMaterial.emissiveTexture as any).vOffset = -elapsed * 1.5;
  }

  // --- Shared beam texture (used by building beam and harvesting beam) ---

  static createBeamTexture(scene: Scene): RawTexture {
    const w = 128, h = 512;
    const canvas = document.createElement("canvas");
    canvas.width = w;
    canvas.height = h;
    const ctx = canvas.getContext("2d")!;

    ctx.clearRect(0, 0, w, h);

    // Jagged core beam: wobbly center path with glow
    for (let pass = 0; pass < 3; pass++) {
      const lineWidth = pass === 0 ? 12 : pass === 1 ? 6 : 2;
      const alpha = pass === 0 ? 0.15 : pass === 1 ? 0.4 : 0.9;
      ctx.strokeStyle = `rgba(0,200,255,${alpha})`;
      if (pass === 2) ctx.strokeStyle = `rgba(180,240,255,${alpha})`;
      ctx.lineWidth = lineWidth;
      ctx.shadowColor = "rgba(0,200,255,0.5)";
      ctx.shadowBlur = pass === 0 ? 15 : 5;
      ctx.beginPath();
      let cx = w / 2;
      ctx.moveTo(cx, 0);
      for (let y = 0; y < h; y += 5) {
        cx = w * 0.3 + Math.random() * w * 0.4;
        ctx.lineTo(cx, y);
      }
      ctx.stroke();
      ctx.shadowBlur = 0;
    }

    // Energy pulses traveling along the beam (bright bands)
    for (let i = 0; i < 8; i++) {
      const y = (i / 8) * h;
      const bandH = 6 + Math.random() * 12;
      const pulseGrad = ctx.createLinearGradient(0, 0, w, 0);
      pulseGrad.addColorStop(0, "rgba(0,0,0,0)");
      pulseGrad.addColorStop(0.3, "rgba(0,200,255,0.3)");
      pulseGrad.addColorStop(0.5, "rgba(200,250,255,0.8)");
      pulseGrad.addColorStop(0.7, "rgba(0,200,255,0.3)");
      pulseGrad.addColorStop(1, "rgba(0,0,0,0)");
      ctx.fillStyle = pulseGrad;
      ctx.fillRect(0, y, w, bandH);
    }

    // Thin bright lines across
    for (let i = 0; i < 20; i++) {
      const y = Math.random() * h;
      const lineGrad = ctx.createLinearGradient(0, 0, w, 0);
      lineGrad.addColorStop(0, "rgba(0,0,0,0)");
      lineGrad.addColorStop(0.35, "rgba(0,200,255,0.2)");
      lineGrad.addColorStop(0.5, "rgba(200,255,255,0.6)");
      lineGrad.addColorStop(0.65, "rgba(0,200,255,0.2)");
      lineGrad.addColorStop(1, "rgba(0,0,0,0)");
      ctx.fillStyle = lineGrad;
      ctx.fillRect(0, y, w, 1.5);
    }

    // Jagged lightning lines along the beam
    for (let i = 0; i < 6; i++) {
      const startX = w * 0.3 + Math.random() * w * 0.4;
      let cx = startX;
      const alpha = 0.5 + Math.random() * 0.5;
      ctx.strokeStyle = `rgba(180,250,255,${alpha})`;
      ctx.lineWidth = 1 + Math.random() * 1.5;
      ctx.shadowColor = "rgba(0,200,255,0.8)";
      ctx.shadowBlur = 4;
      ctx.beginPath();
      ctx.moveTo(cx, 0);
      for (let y = 0; y < h; y += 4 + Math.random() * 6) {
        cx = startX + (Math.random() - 0.5) * w * 0.3;
        ctx.lineTo(cx, y);
      }
      ctx.stroke();
      ctx.shadowBlur = 0;

      // Branch bolts
      if (Math.random() > 0.4) {
        ctx.strokeStyle = `rgba(100,220,255,${alpha * 0.5})`;
        ctx.lineWidth = 0.5 + Math.random();
        ctx.beginPath();
        const branchY = Math.random() * h;
        const branchX = startX + (Math.random() - 0.5) * w * 0.2;
        ctx.moveTo(branchX, branchY);
        for (let j = 0; j < 4; j++) {
          ctx.lineTo(
            branchX + (Math.random() - 0.5) * w * 0.25,
            branchY + (j + 1) * (8 + Math.random() * 10)
          );
        }
        ctx.stroke();
      }
    }

    // Sparkle dots along the center
    for (let i = 0; i < 50; i++) {
      const x = w * 0.3 + Math.random() * w * 0.4;
      const y = Math.random() * h;
      const size = 1 + Math.random() * 2;
      const alpha = 0.5 + Math.random() * 0.5;
      ctx.fillStyle = `rgba(200,250,255,${alpha})`;
      ctx.shadowColor = "rgba(0,200,255,0.8)";
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

  // --- Hologram texture ---

  private static createHologramTexture(scene: Scene): RawTexture {
    if (BabylonBuildupEffect.hologramTextureCache) {
      return BabylonBuildupEffect.hologramTextureCache;
    }

    const size = 512;
    const canvas = document.createElement("canvas");
    canvas.width = size;
    canvas.height = size;
    const ctx = canvas.getContext("2d")!;
    const cx = size / 2, cy = size / 2;

    ctx.clearRect(0, 0, size, size);

    // Concentric rings
    for (let r = 20; r < size / 2; r += 16) {
      const alpha = 0.5 + 0.4 * Math.sin(r * 0.08);
      ctx.strokeStyle = `rgba(50,240,255,${alpha})`;
      ctx.lineWidth = 2.5 + Math.random() * 1.0;
      ctx.beginPath();
      ctx.arc(cx, cy, r, 0, Math.PI * 2);
      ctx.stroke();
    }

    // Radial lines
    for (let i = 0; i < 24; i++) {
      const angle = (i / 24) * Math.PI * 2;
      const innerR = 30 + Math.random() * 40;
      const outerR = size * 0.42 + Math.random() * 20;
      const alpha = 0.4 + Math.random() * 0.4;
      ctx.strokeStyle = `rgba(50,240,255,${alpha})`;
      ctx.lineWidth = 1.5 + Math.random() * 1.0;
      ctx.beginPath();
      ctx.moveTo(cx + Math.cos(angle) * innerR, cy + Math.sin(angle) * innerR);
      ctx.lineTo(cx + Math.cos(angle) * outerR, cy + Math.sin(angle) * outerR);
      ctx.stroke();
    }

    // Arc segments
    for (let i = 0; i < 12; i++) {
      const r = 40 + Math.random() * (size * 0.35);
      const startAngle = Math.random() * Math.PI * 2;
      const arcLen = 0.2 + Math.random() * 0.8;
      const alpha = 0.6 + Math.random() * 0.4;
      ctx.strokeStyle = `rgba(120,245,255,${alpha})`;
      ctx.lineWidth = 3.0 + Math.random() * 3.0;
      ctx.beginPath();
      ctx.arc(cx, cy, r, startAngle, startAngle + arcLen);
      ctx.stroke();
    }

    // Bright highlights
    for (let i = 0; i < 6; i++) {
      const r = 50 + Math.random() * (size * 0.3);
      const startAngle = Math.random() * Math.PI * 2;
      const arcLen = 0.1 + Math.random() * 0.3;
      ctx.strokeStyle = `rgba(200,240,255,${0.5 + Math.random() * 0.5})`;
      ctx.lineWidth = 2 + Math.random() * 2;
      ctx.shadowColor = "rgba(0,200,255,0.8)";
      ctx.shadowBlur = 8;
      ctx.beginPath();
      ctx.arc(cx, cy, r, startAngle, startAngle + arcLen);
      ctx.stroke();
      ctx.shadowBlur = 0;
    }

    // Dots
    for (let i = 0; i < 30; i++) {
      const angle = Math.random() * Math.PI * 2;
      const dist = 30 + Math.random() * (size * 0.38);
      const x = cx + Math.cos(angle) * dist;
      const y = cy + Math.sin(angle) * dist;
      ctx.fillStyle = `rgba(200,245,255,${0.5 + Math.random() * 0.5})`;
      ctx.shadowColor = "rgba(0,200,255,0.6)";
      ctx.shadowBlur = 4;
      ctx.beginPath();
      ctx.arc(x, y, 1.5 + Math.random() * 2.5, 0, Math.PI * 2);
      ctx.fill();
      ctx.shadowBlur = 0;
    }

    // Center cross-hair
    ctx.strokeStyle = "rgba(150,230,255,0.6)";
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.moveTo(cx - 15, cy);
    ctx.lineTo(cx + 15, cy);
    ctx.moveTo(cx, cy - 15);
    ctx.lineTo(cx, cy + 15);
    ctx.stroke();

    // Center glow
    const centerGlow = ctx.createRadialGradient(cx, cy, 0, cx, cy, 12);
    centerGlow.addColorStop(0, "rgba(255,255,255,0.9)");
    centerGlow.addColorStop(0.3, "rgba(0,200,255,0.5)");
    centerGlow.addColorStop(1, "rgba(0,0,0,0)");
    ctx.fillStyle = centerGlow;
    ctx.fillRect(cx - 12, cy - 12, 24, 24);

    // Circular edge fade
    const edgeFade = ctx.createRadialGradient(cx, cy, size * 0.3, cx, cy, size * 0.5);
    edgeFade.addColorStop(0, "rgba(0,0,0,0)");
    edgeFade.addColorStop(1, "rgba(0,0,0,1)");
    ctx.globalCompositeOperation = "destination-out";
    ctx.fillStyle = edgeFade;
    ctx.fillRect(0, 0, size, size);
    ctx.globalCompositeOperation = "source-over";

    const imgData = ctx.getImageData(0, 0, size, size);
    const tex = RawTexture.CreateRGBATexture(
      new Uint8Array(imgData.data), size, size, scene, true, false
    );
    tex.hasAlpha = true;

    BabylonBuildupEffect.hologramTextureCache = tex;
    return tex;
  }
}
