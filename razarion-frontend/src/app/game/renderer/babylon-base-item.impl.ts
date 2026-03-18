import {
  AbstractMesh,
  Animation,
  Color3,
  Color4,
  GlowLayer,
  Material,
  Mesh,
  MeshBuilder,
  MeshExploder,
  ParticleSystem,
  ParticleSystemSet,
  RawTexture,
  StandardMaterial,
  UtilityLayerRenderer,
  Vector3,
  VertexData
} from "@babylonjs/core";
import {
  BabylonBaseItem,
  BaseItemType,
  DecimalPosition,
  Diplomacy,
  MarkerConfig,
  Vertex,
} from "../../gwtangular/GwtAngularFacade";
import {GwtHelper} from "../../gwtangular/GwtHelper";
import {BabylonItemImpl} from "./babylon-item.impl";
import {BabylonModelService} from "./babylon-model.service";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {ActionService} from "../action.service";
import {UiConfigCollectionService} from "../ui-config-collection.service";
import {SelectionService as TsSelectionService} from "../selection.service";
import {AdvancedDynamicTexture, TextBlock} from '@babylonjs/gui';
import {Slider} from '@babylonjs/gui/2D/controls/sliders/slider';
import {GridMaterial} from "@babylonjs/materials/grid/gridMaterial";
import {Nullable} from '@babylonjs/core/types';

export class BabylonBaseItemImpl extends BabylonItemImpl implements BabylonBaseItem {
  // See GWT java PlanetService
  static readonly TICK_TIME_MILLI_SECONDS: number = 100;
  private baseId: number;
  private buildingBeam: Mesh | null = null;
  private buildingBeamMaterial: StandardMaterial | null = null;
  private buildingBeamTarget: Vector3 | null = null;
  private buildingBeamStartTime: number = 0;
  private buildingBeamRenderCallback: (() => void) | null = null;
  private harvestingBeam: Mesh | null = null;
  private harvestingBeamMaterial: StandardMaterial | null = null;
  private harvestingBeamTarget: Vector3 | null = null;
  private harvestingBeamStartTime: number = 0;
  private harvestingBeamRenderCallback: (() => void) | null = null;
  private progressSlider: Slider | null = null;
  private healthSlider: Slider | null = null;
  private nameBlock: TextBlock | null = null;
  private buildup: number | null = null;
  private progress: number = 0;
  private idle = false;
  private readonly utilLayer: UtilityLayerRenderer;
  private position3D: Vector3 | null = null;
  private oldPosition3D: Vector3 | null = null;
  private lastPositionUpdateTime: number | null = null;
  private rotation3D: Vector3 | null = null;
  private oldRotation3D: Vector3 | null = null;
  private lastRotationUpdateTime: number | null = null;
  private idleCallback: ((idle: boolean) => void) | null = null;
  private buildupCallback: ((buildup: number) => void) | null = null;
  private readonly uiTexture: AdvancedDynamicTexture;
  private buildupMeshes: Mesh[] | null = null;
  private buildupOriginalMaterials: Map<Mesh, Material | null> = new Map();
  private buildupMaterial: GridMaterial | null = null;
  private buildupInitialized = false;
  private buildupWorldMinY = 0;
  private buildupWorldMaxY = 0;
  private buildupScanLine: Mesh | null = null;
  private buildupScanLineMaterial: StandardMaterial | null = null;
  private buildupGridDisc: Mesh | null = null;
  private buildupGridDisc2: Mesh | null = null;
  private buildupDiscRenderCallback: (() => void) | null = null;
  private buildupParticleSystem: ParticleSystem | null = null;
  private buildupGlowLayer: GlowLayer | null = null;
  private demolitionMeshExploder: MeshExploder | null = null;
  private demolitionCenterMesh: Mesh | null = null;
  private demolitionMeshes: { mesh: Mesh; demolished: boolean }[] | null = null;
  private isExploding = false;

  constructor(id: number,
              private baseItemType: BaseItemType,
              baseId: number,
              diplomacy: Diplomacy,
              userName: string,
              rendererService: BabylonRenderServiceAccessImpl,
              actionService: ActionService,
              tsSelectionService: TsSelectionService,
              babylonModelService: BabylonModelService,
              uiConfigCollectionService: UiConfigCollectionService,
              disposeCallback: ((permanent: boolean) => void) | null) {
    super(id,
      baseItemType,
      diplomacy,
      rendererService,
      babylonModelService,
      uiConfigCollectionService,
      actionService,
      tsSelectionService,
      rendererService.baseItemContainer,
      disposeCallback);

    this.baseId = baseId;
    this.updateItemCursor();

    this.utilLayer = new UtilityLayerRenderer(rendererService.getScene());
    this.uiTexture = AdvancedDynamicTexture.CreateFullscreenUI(`Base item ui ${baseItemType.getInternalName()}`);
    this.uiTexture.disablePicking = true; // Prevent mouse down on terrain cursor change

    if (baseItemType.getPhysicalAreaConfig().fulfilledMovable()) {
      rendererService.addInterpolationListener(this);
    }

    this.setupName(userName);
  }

  public static createDummy(id: number): BabylonBaseItem {
    return new class implements BabylonBaseItem {
      getBaseItemType(): BaseItemType {
        return <any>{}
      }

      dispose(): void {
      }

      removeFromView(): void {
      }

      setAngle(angle: number): void {
      }

      getAngle(): number {
        return 0;
      }

      getPosition(): Vertex | null {
        return null;
      }

      setHealth(health: number): void {
      }

      setPosition(position: Vertex): void {
      }

      getId(): number {
        return id;
      }

      setup(): void {
      }

      isEnemy(): boolean {
        return false;
      }

      select(active: boolean): void {
      }

      hover(active: boolean): void {
      }

      mark(markerConfig: MarkerConfig | null): void {
      }

      setBuildingPosition(xOrPosition: any, y?: number): void {

      }

      setHarvestingPosition(harvestingPosition: DecimalPosition | null): void {
      }

      setBuildup(buildup: number): void {
      }

      setConstructing(progress: number): void {
      }

      setIdle(idle: boolean): void {

      }

      onProjectileFired(tagetSyncBaseItemId: number, targetPosition: DecimalPosition): void {
      }

      onExplode(): void {
      }

      getBaseId(): number {
        return 0;
      }

      updateUserName(userName: string): void {
      }

      setTurretAngle(turretAngle: number): void {
      }
    }
  }

  override dispose() {
    if (this.isExploding) {
      return;
    }
    this.cleanupBaseItem();
    super.dispose();
  }

  override removeFromView() {
    if (this.isExploding) {
      return;
    }
    this.cleanupBaseItem();
    super.removeFromView();
  }

  private cleanupBaseItem() {
    if (this.baseItemType.getPhysicalAreaConfig().fulfilledMovable()) {
      this.rendererService.removeInterpolationListener(this);
    }
    this.disposeBuildingBeam();
    this.cleanupBuildupEffect();
    this.disposeHarvestingBeam();
    if (this.healthSlider) {
      this.uiTexture.removeControl(this.healthSlider)
      this.healthSlider = null;
    }
    if (this.progressSlider) {
      this.uiTexture.removeControl(this.progressSlider)
      this.progressSlider = null;
    }
    if (this.nameBlock) {
      this.uiTexture.removeControl(this.nameBlock)
      this.nameBlock = null;
    }
    if (this.demolitionCenterMesh) {
      this.demolitionCenterMesh.dispose();
      this.demolitionCenterMesh = null;
    }
    this.uiTexture.dispose();
  }

  getBaseItemType(): BaseItemType {
    return this.baseItemType;
  }

  setHealth(health: number): void {
    if (this.isSelectOrHove() && !this.healthSlider) {
      this.healthSlider = new Slider();
      this.healthSlider.minimum = 0;
      this.healthSlider.maximum = 1;
      this.healthSlider.value = health;
      this.healthSlider.height = "20px";
      this.healthSlider.width = "150px";
      this.healthSlider.color = "green";
      this.healthSlider.background = "red";
      this.healthSlider.displayThumb = false;
      this.healthSlider.isReadOnly = false;
      this.uiTexture.addControl(this.healthSlider)
      this.healthSlider.linkWithMesh(this.getContainer());
      this.healthSlider.linkOffsetY = -52;
    } else if (!this.isSelectOrHove() && this.healthSlider) {
      this.uiTexture.removeControl(this.healthSlider)
      this.healthSlider = null;
    } else if (this.isSelectOrHove() && this.healthSlider) {
      if (this.healthSlider) {
        this.healthSlider.value = health;
      }
    }

    if (!this.baseItemType.getPhysicalAreaConfig().fulfilledMovable()) {
      if (health < 1.0 && this.demolitionMeshExploder == null) {
        this.demolitionCenterMesh = MeshBuilder.CreateBox("Demolition ground");
        this.demolitionCenterMesh.position.x = this.getPosition()!.getX();
        this.demolitionCenterMesh.position.y = this.getPosition()!.getZ();
        this.demolitionCenterMesh.position.z = this.getPosition()!.getY();
        this.demolitionCenterMesh.isVisible = false;
        this.demolitionCenterMesh.setParent(this.getContainer());

        let demolitionMeshes = this.getContainer().getChildMeshes() as Mesh[];
        demolitionMeshes.sort((a, b) => {
          return a.position.y - b.position.y;
        });
        this.demolitionMeshes = [];
        demolitionMeshes.forEach(mesh => {
          this.demolitionMeshes!.push({mesh: mesh, demolished: false})
        })
        this.demolitionMeshExploder = new MeshExploder(demolitionMeshes, this.demolitionCenterMesh);
      } else if (health >= 1.0 && this.demolitionMeshExploder != null) {
        this.demolitionMeshExploder = null;
        this.demolitionMeshes = null;
        this.demolitionCenterMesh?.dispose()
        this.demolitionCenterMesh = null;
      }

      if (health < 1.0 && this.demolitionMeshExploder != null) {
        this.demolitionMeshExploder.explode(-(1.0 - health));

        const stepSize = 1.0 / this.demolitionMeshes!.length;
        for (let i = 0; i < this.demolitionMeshes!.length; i++) {
          if (health < i * stepSize && !this.demolitionMeshes![i].demolished) {
            this.demolitionMeshes![i].mesh.rotationQuaternion = null;
            this.demolitionMeshes![i].mesh.rotation.x -= 0.9 * Math.random();
            this.demolitionMeshes![i].mesh.rotation.y -= 0.9 * Math.random();
            this.demolitionMeshes![i].mesh.rotation.z -= 0.9 * Math.random();
            this.demolitionMeshes![i].demolished = true;
          }
        }
      }
    }
  }

  getBuildup(): number {
    return this.buildup ?? 1.0;
  }

  setBuildup(buildup: number): void {
    const previousBuildup = this.buildup;
    this.buildup = buildup;
    if (this.buildupCallback) {
      this.buildupCallback(buildup);
    }
    this.getRenderObject().setEffectsActive(buildup >= 1.0);

    if (buildup >= 1.0) {
      if (this.buildupMeshes != null) {
        this.buildupMeshes.forEach(m => m.isVisible = true);
        this.buildupMeshes = null;
        this.cleanupBuildupEffect();
        this.updateItemCursor();
      }
      return;
    }

    // Buildup not progressing: builder stopped, remove ring but keep mesh state
    if (previousBuildup !== null && buildup === previousBuildup) {
      if (this.buildupInitialized) {
        this.cleanupBuildupRing();
      }
      return;
    }

    if (!this.buildupInitialized) {
      this.initBuildupEffect();
    }

    if (this.buildupInitialized && !this.buildupScanLine) {
      this.createBuildupRing();
    }

    if (this.buildupInitialized) {
      this.updateBuildupVisibility(buildup);
    }
  }

  private initBuildupEffect(): void {
    const meshes = (this.getContainer().getChildMeshes() as Mesh[]).filter(m => m.isVisible);
    if (meshes.length === 0) {
      return;
    }

    let minY = Infinity, maxY = -Infinity;
    meshes.forEach(mesh => {
      mesh.computeWorldMatrix(true);
      const bi = mesh.getBoundingInfo();
      minY = Math.min(minY, bi.boundingBox.minimumWorld.y);
      maxY = Math.max(maxY, bi.boundingBox.maximumWorld.y);
    });

    if (maxY <= minY) {
      return;
    }

    this.buildupInitialized = true;
    this.buildupWorldMinY = minY;
    this.buildupWorldMaxY = maxY;
    this.buildupMeshes = meshes;
    meshes.forEach(m => {
      this.buildupOriginalMaterials.set(m, m.material);
      m.isVisible = false;
    });

    const buildupMat = new GridMaterial("BuildupMat", this.rendererService.getScene());
    const diplomacyColor = BabylonRenderServiceAccessImpl.color4Diplomacy(this.diplomacy);
    buildupMat.mainColor = diplomacyColor.scale(0.05);
    buildupMat.lineColor = diplomacyColor;
    buildupMat.gridRatio = 0.15;
    buildupMat.majorUnitFrequency = 5;
    buildupMat.minorUnitVisibility = 0.6;
    buildupMat.opacity = 1.0;
    buildupMat.backFaceCulling = false;
    this.buildupMaterial = buildupMat;
  }

  private createBuildupRing(): void {
    const radius = this.baseItemType.getPhysicalAreaConfig().getRadius();
    this.buildupScanLine = MeshBuilder.CreateTorus("BuildupScanLine", {
      diameter: radius * 3,
      thickness: 0.05,
      tessellation: 32
    }, this.rendererService.getScene());
    this.buildupScanLine.parent = this.getContainer();
    this.buildupScanLine.isPickable = false;
    this.buildupScanLine.isVisible = false;

    // Glow effect for buildup meshes
    const gl = new GlowLayer("BuildupGlow", this.rendererService.getScene(), {
      blurKernelSize: 128,
      mainTextureFixedSize: 256
    });
    gl.intensity = 5.0;
    gl.addIncludedOnlyMesh(this.buildupScanLine as Mesh);
    this.buildupGlowLayer = gl;

    const holoTex = BabylonBaseItemImpl.createHologramTexture(this.rendererService.getScene());

    const createDisc = (name: string): Mesh => {
      const disc = MeshBuilder.CreateDisc(name, {
        radius: radius * 2.5,
        tessellation: 32
      }, this.rendererService.getScene());
      disc.parent = this.buildupScanLine;
      disc.rotation.x = Math.PI / 2;
      disc.isPickable = false;

      const mat = new StandardMaterial(name + "Mat", this.rendererService.getScene());
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

    this.buildupGridDisc = createDisc("BuildupGrid1");
    this.buildupGridDisc2 = createDisc("BuildupGrid2");
    this.buildupGridDisc2.position.y = 0.01;

    const cycleDuration = 3000;
    const startTime = Date.now();

    this.buildupDiscRenderCallback = () => {
      const elapsed = Date.now() - startTime;
      const t1 = (elapsed % cycleDuration) / cycleDuration;
      const t2 = ((elapsed + cycleDuration / 2) % cycleDuration) / cycleDuration;

      const scale1 = t1;
      const scale2 = t2;
      const alpha1 = Math.sin(t1 * Math.PI);
      const alpha2 = Math.sin(t2 * Math.PI);

      if (this.buildupGridDisc) {
        this.buildupGridDisc.scaling.set(scale1, scale1, scale1);
        (this.buildupGridDisc.material as StandardMaterial).alpha = alpha1;
      }
      if (this.buildupGridDisc2) {
        this.buildupGridDisc2.scaling.set(scale2, scale2, scale2);
        (this.buildupGridDisc2.material as StandardMaterial).alpha = alpha2;
      }
    };
    this.rendererService.getScene().registerBeforeRender(this.buildupDiscRenderCallback);

    // Spark particle system around the ring with animated sprite sheet
    const scene = this.rendererService.getScene();
    const ps = new ParticleSystem("BuildupSparks", 200, scene);

    // Generate 4x4 sprite sheet with spark/lightning frames
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
      const points: {x: number, y: number}[] = [{x: x1, y: y1}];
      for (let i = 1; i < segments; i++) {
        const t = i / segments;
        const offset = (Math.random() - 0.5) * 2 * jitter;
        points.push({x: x1 + dx * t + nx * offset, y: y1 + dy * t + ny * offset});
      }
      points.push({x: x2, y: y2});

      // Outer glow
      ctx.strokeStyle = `rgba(0,180,255,${alpha * 0.4})`;
      ctx.lineWidth = lineWidth * 3;
      ctx.lineCap = "round";
      ctx.lineJoin = "round";
      ctx.beginPath();
      points.forEach((p, i) => i === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y));
      ctx.stroke();

      // Core bolt
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

        // Main bolt
        drawLightningBolt(sCtx,
          Math.cos(angle) * half * intensity, Math.sin(angle) * half * intensity,
          -Math.cos(angle) * half * intensity, -Math.sin(angle) * half * intensity,
          5, half * 0.3, 1.5 * intensity + 0.5, intensity
        );

        // Branch bolt
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
      new Uint8Array(imgData.data), sheetCanvas.width, sheetCanvas.height, scene, false, false
    );
    ps.particleTexture.hasAlpha = true;

    // Sprite sheet animation
    ps.isAnimationSheetEnabled = true;
    ps.spriteCellWidth = cellSize;
    ps.spriteCellHeight = cellSize;
    ps.startSpriteCellID = 0;
    ps.endSpriteCellID = cols * rows - 1;
    ps.spriteCellChangeSpeed = 4;
    ps.spriteRandomStartCell = true;

    ps.emitter = this.buildupScanLine;
    ps.minEmitBox = new Vector3(-radius, 0, -radius);
    ps.maxEmitBox = new Vector3(radius, 0, radius);

    ps.color1 = new Color4(0, 0.8, 1, 1);
    ps.color2 = new Color4(0.5, 0.9, 1, 1);
    ps.colorDead = new Color4(0, 0.1, 0.3, 0);

    // Flash effect: bright start, quick fade
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
    this.buildupParticleSystem = ps;
  }

  private updateBuildupVisibility(buildup: number): void {
    const range = this.buildupWorldMaxY - this.buildupWorldMinY;
    const worldClipY = this.buildupWorldMinY + range * 0.5 + range * 0.5 * buildup;

    if (this.buildupScanLine) {
      this.buildupScanLine.position.y = worldClipY - this.getContainer().position.y;
    }

    if (this.buildupMeshes) {
      this.buildupMeshes.forEach(mesh => {
        const bb = mesh.getBoundingInfo().boundingBox;
        const meshMinY = bb.minimumWorld.y;
        const meshMaxY = bb.maximumWorld.y;

        if (meshMaxY <= worldClipY) {
          // Fully below ring: normal material, visible
          mesh.isVisible = true;
          mesh.material = this.buildupOriginalMaterials.get(mesh) ?? mesh.material;
        } else {
          // At or above ring: wireframe buildup material
          mesh.isVisible = true;
          mesh.material = this.buildupMaterial;
        }
      });
    }
  }

  private cleanupBuildupRing(): void {
    if (this.buildupDiscRenderCallback) {
      this.rendererService.getScene().unregisterBeforeRender(this.buildupDiscRenderCallback);
      this.buildupDiscRenderCallback = null;
    }
    if (this.buildupGlowLayer) {
      this.buildupGlowLayer.dispose();
      this.buildupGlowLayer = null;
    }
    if (this.buildupParticleSystem) {
      this.buildupParticleSystem.dispose();
      this.buildupParticleSystem = null;
    }
    if (this.buildupGridDisc) {
      this.buildupGridDisc.material?.dispose();
      this.buildupGridDisc.dispose();
      this.buildupGridDisc = null;
    }
    if (this.buildupGridDisc2) {
      this.buildupGridDisc2.material?.dispose();
      this.buildupGridDisc2.dispose();
      this.buildupGridDisc2 = null;
    }
    if (this.buildupScanLine) {
      this.buildupScanLine.dispose();
      this.buildupScanLine = null;
    }
    if (this.buildupScanLineMaterial) {
      this.buildupScanLineMaterial.dispose();
      this.buildupScanLineMaterial = null;
    }
  }

  private cleanupBuildupEffect(): void {
    this.cleanupBuildupRing();

    this.buildupOriginalMaterials.forEach((originalMat, mesh) => {
      mesh.material = originalMat;
    });
    this.buildupOriginalMaterials.clear();

    if (this.buildupMaterial) {
      this.buildupMaterial.dispose();
      this.buildupMaterial = null;
    }

    this.buildupInitialized = false;
  }

  setConstructing(progress: number): void {
    this.progress = progress;
    this.handleConstructing();
    if (this.progressSlider) {
      this.progressSlider.value = this.progress;
    }
  }

  setIdle(idle: boolean): void {
    if (this.idle != idle) {
      if (this.idleCallback) {
        this.idleCallback(idle);
      }
    }
    this.idle = idle;
  }

  getIdle(): boolean {
    return this.idle;
  }

  setIdleCallback(callback: ((idle: boolean) => void) | null) {
    this.idleCallback = callback;
  }

  setBuildupCallback(callback: ((buildup: number) => void) | null) {
    this.buildupCallback = callback;
  }

  handleConstructing(): void {
    if (this.isSelectOrHove() && this.progress > 0) {
      if (!this.progressSlider) {
        this.progressSlider = new Slider();
        this.progressSlider.minimum = 0;
        this.progressSlider.maximum = 1;
        this.progressSlider.value = this.progress;
        this.progressSlider.height = "20px";
        this.progressSlider.width = "150px";
        this.progressSlider.color = "dodgerblue";
        this.progressSlider.background = "black";
        this.progressSlider.displayThumb = false;
        this.progressSlider.isReadOnly = false;
        this.uiTexture.addControl(this.progressSlider)
        this.progressSlider.linkWithMesh(this.getContainer());
        this.progressSlider.linkOffsetY = -40;
      }
    } else {
      if (this.progressSlider) {
        this.uiTexture.removeControl(this.progressSlider)
        this.progressSlider = null;
      }
    }
  }

  onProjectileFired(targetSyncBaseItemId: number, targetPosition: DecimalPosition): void {
    const targetBaseItem = this.rendererService.getBabylonBaseItemById(targetSyncBaseItemId);
    if (!targetBaseItem) {
      console.warn("Target base item not found:", targetSyncBaseItemId);
      return;
    }

    const muzzleFlashAudioConfig = this.baseItemType.getWeaponType()?.getMuzzleFlashAudioConfig();
    if (muzzleFlashAudioConfig != null) {
      this.rendererService.babylonAudioService.playAudioAtPositionWithConfig(muzzleFlashAudioConfig, this.getContainer().position);
    }

    const renderObject = this.getRenderObject();
    if (renderObject.hasMuzzleFlash()) {
      renderObject.createMuzzleFlashParticleSystemSet()
        ?.then(particleSystemSet => particleSystemSet.start(renderObject.getMuzzleFlashMesh()));
    }
    let startPosition: Vector3 = renderObject.getBeamOrigin() ?? renderObject.getModel3D().getAbsolutePosition();

    const childMeshes: AbstractMesh[] = targetBaseItem.getContainer().getChildMeshes() as AbstractMesh[];

    let minDistance = Number.MAX_VALUE;
    let closestPoint: Vector3 | null = null;

    childMeshes
      .filter(mesh => mesh.isVisible)
      .forEach(mesh => {
        const positions = mesh.getVerticesData("position");
        if (!positions) {
          return;
        }

        for (let i = 0; i < positions.length; i += 3) {
          const vertexLocal = new Vector3(positions[i], positions[i + 1], positions[i + 2]);
          const vertexWorld = Vector3.TransformCoordinates(vertexLocal, mesh.getWorldMatrix());

          const dist = Vector3.Distance(vertexWorld, startPosition);
          if (dist < minDistance) {
            minDistance = dist;
            closestPoint = vertexWorld;
          }
        }
      });

    if (!closestPoint) {
      const pickingInfo = this.rendererService.setupTerrainPickPointFromPosition(targetPosition);
      closestPoint = pickingInfo?.pickedPoint ?? new Vector3(targetPosition.getX(), 0, targetPosition.getY());
    }

    this.createProjectile(startPosition, null, closestPoint!);
  }

  onExplode(): void {
    const explosionAudioId = this.baseItemType.getExplosionAudioItemConfigId();
    if (explosionAudioId != null) {
      this.rendererService.babylonAudioService.playAudioAtPosition(explosionAudioId, this.getContainer().position);
    }

    if (GwtHelper.gwtIssueNumber(this.baseItemType.getExplosionParticleId() == null)) {
      console.warn(`No ExplosionParticleId for base item type ${this.getId()}`)
      return;
    }

    try {
      let particleSystemConfig = this.babylonModelService.getParticleSystemEntity(this.baseItemType.getExplosionParticleId()!);
      this.rendererService.createParticleSystem(particleSystemConfig.id, particleSystemConfig.imageId)
        ?.then(particleSystemSet => {
          // TODO particleSystemSet.disposeOnStop = true;
          particleSystemSet.start(<any>this.getContainer().position.clone());
        });
    } catch (e) {
      console.warn(e);
    }

    this.isExploding = true;
    setTimeout(() => {
      try {
        let centerMesh = MeshBuilder.CreateBox("Explosion ground");
        centerMesh.position.x = this.getPosition()!.getX();
        centerMesh.position.y = this.getPosition()!.getZ();
        centerMesh.position.z = this.getPosition()!.getY();
        centerMesh.isVisible = false;
        centerMesh.setParent(this.getContainer());

        let toExplodeArray = this.getContainer().getChildMeshes() as Mesh[];

        let newExplosion = new MeshExploder(toExplodeArray, centerMesh);

        const dateStart = Date.now();
        const EXPLOSION_DURATION = 500;
        const MAX_DISTANCE = 10;   // bisher 30 im Code

        const renderCallback = () => {
          const elapsedTime = Date.now() - dateStart;

          if (elapsedTime < EXPLOSION_DURATION) {
            const t = elapsedTime / EXPLOSION_DURATION; // 0 → 1
            const easeOut = t * 0.7 + (1 - (1 - t) * (1 - t)) * 0.3;
            const distance = MAX_DISTANCE * easeOut;
            newExplosion.explode(distance);
          } else {
            this.isExploding = false;
            centerMesh.dispose();
            this.rendererService.getScene().unregisterBeforeRender(renderCallback);
            this.dispose();
          }
        };

        this.rendererService.getScene().registerBeforeRender(renderCallback);
      } catch (e) {
        console.warn(e);
      }
    }, 100)
  }

  setBuildingPosition(xOrPosition: any, y?: number): void {
    let bx: number | null = null;
    let by: number | null = null;

    if (typeof xOrPosition === 'number' && typeof y === 'number') {
      bx = xOrPosition;
      by = y;
    } else if (xOrPosition && typeof xOrPosition.getX === 'function') {
      bx = xOrPosition.getX();
      by = xOrPosition.getY();
    }

    const hasPosition = bx !== null && by !== null;

    if (hasPosition && this.buildingBeamRenderCallback) {
      return;
    }
    if (!hasPosition && !this.buildingBeam && !this.buildingBeamRenderCallback) {
      return;
    }

    if (!hasPosition) {
      this.disposeBuildingBeam();
      return;
    }

    try {
      this.buildingBeamTarget = new Vector3(
        bx!,
        this.getContainer().position.y,
        by!
      );
      this.buildingBeamStartTime = Date.now();

      this.buildingBeamRenderCallback = () => {
        this.updateBuildingBeam();
      };
      this.rendererService.getScene().registerBeforeRender(this.buildingBeamRenderCallback);
    } catch (e) {
      console.error(e);
    }
  }

  private updateBuildingBeam(): void {
    let startPos: Vector3 = this.getRenderObject().getBeamOrigin() ?? this.getContainer().position.clone();

    if (!this.buildingBeamTarget) {
      return;
    }

    // Find the target building's scan line position
    const targetItem = this.rendererService.findBabylonBaseItemAtPosition(this.buildingBeamTarget);
    let endPos: Vector3;
    if (targetItem && targetItem.buildupScanLine) {
      targetItem.buildupScanLine.computeWorldMatrix(true);
      endPos = targetItem.buildupScanLine.getAbsolutePosition().clone();
    } else {
      endPos = this.buildingBeamTarget.clone();
    }

    if (Vector3.Distance(startPos, endPos) < 0.01) {
      return;
    }

    if (!this.buildingBeamMaterial) {
      const beamTex = BabylonBaseItemImpl.createBeamTexture(this.rendererService.getScene());
      const beamMat = new StandardMaterial("BuildingBeamMat", this.rendererService.getScene());
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
      this.buildingBeam = MeshBuilder.CreatePlane("BuildingBeam", {
        width: 1,
        height: 1
      }, this.rendererService.getScene());
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

  setHarvestingPosition(xOrPosition: any, y?: number): void {
    let hx: number | null = null;
    let hy: number | null = null;

    if (typeof xOrPosition === 'number' && typeof y === 'number') {
      hx = xOrPosition;
      hy = y;
    } else if (xOrPosition && typeof xOrPosition.getX === 'function') {
      hx = xOrPosition.getX();
      hy = xOrPosition.getY();
    }

    const hasPosition = hx !== null && hy !== null;

    if (hasPosition && this.harvestingBeamRenderCallback) {
      return;
    }
    if (!hasPosition && !this.harvestingBeam && !this.harvestingBeamRenderCallback) {
      return;
    }

    if (!hasPosition) {
      this.disposeHarvestingBeam();
      return;
    }

    try {
      this.harvestingBeamTarget = new Vector3(
        hx!,
        this.getContainer().position.y,
        hy!
      );
      this.harvestingBeamStartTime = Date.now();

      this.harvestingBeamRenderCallback = () => {
        this.updateHarvestingBeam();
      };
      this.rendererService.getScene().registerBeforeRender(this.harvestingBeamRenderCallback);
    } catch (e) {
      console.error(e);
    }
  }

  private updateHarvestingBeam(): void {
    let startPos: Vector3 = this.getRenderObject().getBeamOrigin() ?? this.getContainer().position.clone();

    if (!this.harvestingBeamTarget) {
      return;
    }

    const endPos = this.harvestingBeamTarget.clone();

    if (Vector3.Distance(startPos, endPos) < 0.01) {
      return;
    }

    if (!this.harvestingBeamMaterial) {
      const beamTex = BabylonBaseItemImpl.createBeamTexture(this.rendererService.getScene());
      const beamMat = new StandardMaterial("HarvestingBeamMat", this.rendererService.getScene());
      beamMat.emissiveColor = new Color3(0, 3, 3);
      beamMat.diffuseColor = new Color3(0, 0, 0);
      beamMat.disableLighting = true;
      beamMat.backFaceCulling = false;
      beamMat.useEmissiveAsIllumination = true;
      beamMat.emissiveTexture = beamTex;
      beamMat.opacityTexture = beamTex;
      this.harvestingBeamMaterial = beamMat;
    }

    const dist = Vector3.Distance(startPos, endPos);
    const dir = endPos.subtract(startPos).normalize();
    const angle = Math.atan2(dir.x, dir.z);

    const narrowWidth = 0.4;
    const wideWidth = 1.2;

    if (!this.harvestingBeam) {
      this.harvestingBeam = new Mesh("HarvestingBeam", this.rendererService.getScene());
      this.harvestingBeam.isPickable = false;
      this.harvestingBeam.material = this.harvestingBeamMaterial;
    }

    // Build trapezoid: narrow at start (harvester), wide at end (resource)
    const vertexData = new VertexData();
    vertexData.positions = [
      -narrowWidth / 2, 0, 0,   //  bottom-left (harvester side)
       narrowWidth / 2, 0, 0,   //  bottom-right (harvester side)
       wideWidth / 2, dist, 0,  //  top-right (resource side)
      -wideWidth / 2, dist, 0,  //  top-left (resource side)
    ];
    vertexData.indices = [0, 1, 2, 0, 2, 3];
    vertexData.uvs = [0, 0, 1, 0, 1, 1, 0, 1];
    vertexData.normals = [0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1];
    vertexData.applyToMesh(this.harvestingBeam, true);

    this.harvestingBeam.position = startPos.clone();
    this.harvestingBeam.rotationQuaternion = null;
    this.harvestingBeam.rotation.set(Math.PI / 2, angle, 0);

    // Scroll texture along beam (energy flowing from resource to harvester)
    const elapsed = (Date.now() - this.harvestingBeamStartTime) / 1000;
    (this.harvestingBeamMaterial.emissiveTexture as any).vOffset = elapsed * 1.5;
  }

  getBaseId(): number {
    return this.baseId;
  }

  setTurretAngle(turretAngle: number): void {
    if (this.getRenderObject().getTurretMesh() != null) {
      this.getRenderObject().getTurretMesh()!.rotationQuaternion = null;
      this.getRenderObject().getTurretMesh()!.rotation.y = -turretAngle;
    }
  }

  updateUserName(userName: string): void {
    if (this.nameBlock) {
      this.nameBlock.text = userName;
    } else {
      this.setupName(userName);
    }
  }

  override onPosition3D(position3D: Vector3): boolean {
    let updateImmediately = !this.position3D || this.lastPositionUpdateTime === null;
    this.oldPosition3D = updateImmediately ? position3D.clone() : this.getContainer().position.clone();
    this.position3D = position3D.clone();
    this.lastPositionUpdateTime = Date.now();
    return updateImmediately;
  }

  override onRotation3D(rotation3D: Vector3): boolean {
    let updateImmediately = this.rotation3D === null || this.lastRotationUpdateTime === null;
    this.oldRotation3D = updateImmediately ? rotation3D.clone() : this.getContainer().rotation.clone();
    this.rotation3D = rotation3D.clone();
    this.lastRotationUpdateTime = Date.now();
    return updateImmediately;
  }

  interpolate(date: number): void {
    if (this.lastPositionUpdateTime !== null && this.position3D && this.oldPosition3D) {
      let t = (date - this.lastPositionUpdateTime) / BabylonBaseItemImpl.TICK_TIME_MILLI_SECONDS;
      if (t > 1) {
        t = 1;
      }
      this.getContainer().position = Vector3.Lerp(this.oldPosition3D, this.position3D, t);
    }

    if (this.lastRotationUpdateTime !== null && this.rotation3D !== null && this.oldRotation3D !== null) {
      let t = (date - this.lastRotationUpdateTime) / BabylonBaseItemImpl.TICK_TIME_MILLI_SECONDS;
      if (t > 1) {
        t = 1;
      }
      this.getContainer().rotation = new Vector3(
        BabylonBaseItemImpl.interpolateAngleRadians(this.oldRotation3D.x, this.rotation3D.x, t),
        BabylonBaseItemImpl.interpolateAngleRadians(this.oldRotation3D.y, this.rotation3D.y, t),
        BabylonBaseItemImpl.interpolateAngleRadians(this.oldRotation3D.z, this.rotation3D.z, t)
      );
    }

  }

  private disposeBuildingBeam() {
    if (this.buildingBeamRenderCallback) {
      this.rendererService.getScene().unregisterBeforeRender(this.buildingBeamRenderCallback);
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
  }

  private disposeHarvestingBeam() {
    if (this.harvestingBeamRenderCallback) {
      this.rendererService.getScene().unregisterBeforeRender(this.harvestingBeamRenderCallback);
      this.harvestingBeamRenderCallback = null;
    }
    if (this.harvestingBeam) {
      this.harvestingBeam.dispose();
      this.harvestingBeam = null;
    }
    if (this.harvestingBeamMaterial) {
      this.harvestingBeamMaterial.dispose();
      this.harvestingBeamMaterial = null;
    }
    this.harvestingBeamTarget = null;
  }


  private createProjectile(start: Vector3, targetImpactMesh: Nullable<AbstractMesh>, targetPosition: Nullable<Vector3>): void {
    if (!this.baseItemType.getWeaponType()!.getProjectileSpeed()) {
      return;
    }
    const projectileSpeed = GwtHelper.gwtIssueNumber(this.baseItemType.getWeaponType()!.getProjectileSpeed());
    if (projectileSpeed <= 0.0) {
      return;
    }

    const impactParticleSystemId = GwtHelper.gwtIssueNumber(this.baseItemType.getWeaponType()!.getImpactParticleSystemId());

    const meshProjectile = MeshBuilder.CreateSphere("Projectile", {
      diameter: 0.2,
      segments: 1
    }, this.rendererService.getScene());
    meshProjectile.material = this.rendererService.projectileMaterial;

    const trailParticleSystemEntityId = GwtHelper.gwtIssueNumber(this.baseItemType.getWeaponType()!.getTrailParticleSystemConfigId());
    if (trailParticleSystemEntityId || trailParticleSystemEntityId === 0) {
      let particleSystemEntity = this.babylonModelService.getParticleSystemEntity(trailParticleSystemEntityId);
      this.rendererService.createParticleSystem(particleSystemEntity.id, particleSystemEntity.imageId)?.then(particleSystemSet => {
        particleSystemSet.start(meshProjectile);
      });
    }

    const frameRate = 1;
    const xSlide = new Animation("Projectile",
      "position",
      frameRate,
      Animation.ANIMATIONTYPE_VECTOR3,
      Animation.ANIMATIONLOOPMODE_CONSTANT);

    const keyFrames = [];

    keyFrames.push({
      frame: 0,
      value: start
    });

    const destination = targetImpactMesh ? targetImpactMesh.getAbsolutePosition() : targetPosition!;
    keyFrames.push({
      frame: 1,
      value: destination
    });

    xSlide.setKeys(keyFrames);

    meshProjectile.animations.push(xSlide);


    let animatable = this.rendererService.getScene().beginAnimation(meshProjectile,
      0,
      1,
      false,
      projectileSpeed / start.subtract(destination).length());
    animatable.onAnimationEnd = () => {
      try {
        this.rendererService.getScene().removeMesh(meshProjectile);
        meshProjectile.dispose();
        if (impactParticleSystemId !== null && impactParticleSystemId !== undefined) {
          this.rendererService.createParticleSystem(impactParticleSystemId, null)
            ?.then(particleSystemSet => particleSystemSet.start(targetImpactMesh ? targetImpactMesh : meshProjectile));
        }
        const impactAudioConfig = this.baseItemType.getWeaponType()?.getImpactAudioConfig();
        if (impactAudioConfig != null) {
          this.rendererService.babylonAudioService.playAudioAtPositionWithConfig(impactAudioConfig, destination);
        }
      } catch (e) {
        console.warn(`BabylonBaseItemImpl animatable.onAnimationEnd failed ${e}`)
      }
    };
  }

  private setupName(userName: string) {
    if (!userName) {
      return;
    }
    this.nameBlock = new TextBlock();
    this.nameBlock.text = userName;
    this.nameBlock.color = BabylonRenderServiceAccessImpl.color4Diplomacy(this.diplomacy).toHexString();
    this.nameBlock.fontSize = 18;
    this.nameBlock.outlineWidth = 5;
    this.nameBlock.outlineColor = "black";
    this.uiTexture.addControl(this.nameBlock);
    this.nameBlock.linkWithMesh(this.getContainer());
    this.nameBlock.linkOffsetY = -62 - this.baseItemType.getPhysicalAreaConfig().getRadius() * 2;
  }

  static interpolateAngleRadians(startAngle: number, endAngle: number, t: number): number {
    const twoPi = 2 * Math.PI;

    startAngle = startAngle % twoPi;
    endAngle = endAngle % twoPi;

    if (startAngle < 0) {
      startAngle += twoPi
    }
    if (endAngle < 0) {
      endAngle += twoPi
    }

    let delta = endAngle - startAngle;

    if (delta > Math.PI) {
      delta -= twoPi;
    } else if (delta < -Math.PI) {
      delta += twoPi;
    }

    let interpolatedAngle = startAngle + delta * t;

    interpolatedAngle = interpolatedAngle % twoPi;
    if (interpolatedAngle < 0) {
      interpolatedAngle += twoPi;
    }

    return interpolatedAngle;
  }

  private static hologramTextureCache: RawTexture | null = null;

  private static createHologramTexture(scene: any): RawTexture {
    if (BabylonBaseItemImpl.hologramTextureCache) {
      return BabylonBaseItemImpl.hologramTextureCache;
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
    ctx.moveTo(cx - 15, cy); ctx.lineTo(cx + 15, cy);
    ctx.moveTo(cx, cy - 15); ctx.lineTo(cx, cy + 15);
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

    BabylonBaseItemImpl.hologramTextureCache = tex;
    return tex;
  }

  private static createBeamTexture(scene: any): RawTexture {
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

}
