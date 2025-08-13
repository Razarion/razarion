import {Animation, MeshBuilder, ParticleSystem, UtilityLayerRenderer, Vector3} from "@babylonjs/core";
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
import {LocationVisualization} from "src/app/editor/common/place-config/location-visualization";
import {UiConfigCollectionService} from "../ui-config-collection.service";
import {AdvancedDynamicTexture, TextBlock} from '@babylonjs/gui';
import {Slider} from '@babylonjs/gui/2D/controls/sliders/slider';

export class BabylonBaseItemImpl extends BabylonItemImpl implements BabylonBaseItem {
  // See GWT java PlanetService
  static readonly TICK_TIME_MILLI_SECONDS: number = 100;
  private buildingParticleSystem: ParticleSystem | null = null;
  private harvestingParticleSystem: ParticleSystem | null = null;
  private progressSlider: Slider | null = null;
  private healthSlider: Slider | null = null;
  private nameBlock: TextBlock | null = null;
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
  private readonly advancedDynamicTexture: AdvancedDynamicTexture;

  constructor(id: number,
              private baseItemType: BaseItemType,
              diplomacy: Diplomacy,
              userName: string,
              rendererService: BabylonRenderServiceAccessImpl,
              actionService: ActionService,
              babylonModelService: BabylonModelService,
              uiConfigCollectionService: UiConfigCollectionService,
              disposeCallback: (() => void) | null) {
    super(id,
      baseItemType,
      diplomacy,
      rendererService,
      babylonModelService,
      uiConfigCollectionService,
      actionService,
      rendererService.baseItemContainer,
      disposeCallback);

    this.utilLayer = new UtilityLayerRenderer(rendererService.getScene());
    this.advancedDynamicTexture = AdvancedDynamicTexture.CreateFullscreenUI("UI");

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

      setBuildingPosition(buildingPosition: DecimalPosition): void {

      }

      setHarvestingPosition(harvestingPosition: DecimalPosition | null): void {
      }

      setBuildup(buildup: number): void {
      }

      setConstructing(progress: number): void {
      }

      setIdle(idle: boolean): void {

      }

      onProjectileFired(destination: DecimalPosition): void {
      }

      onExplode(): void {
      }

    }
  }

  override dispose() {
    if (this.baseItemType.getPhysicalAreaConfig().fulfilledMovable()) {
      this.rendererService.removeInterpolationListener(this);
    }
    this.disposeBuildingParticleSystem();
    this.disposeHarvestingParticleSystem();
    if (this.healthSlider) {
      this.advancedDynamicTexture.removeControl(this.healthSlider)
      this.healthSlider = null;
    }
    if (this.progressSlider) {
      this.advancedDynamicTexture.removeControl(this.progressSlider)
      this.progressSlider = null;
    }
    if (this.nameBlock) {
      this.advancedDynamicTexture.removeControl(this.nameBlock)
      this.nameBlock = null;
    }
    super.dispose();
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
      this.advancedDynamicTexture.addControl(this.healthSlider)
      this.healthSlider.linkWithMesh(this.getContainer());
      this.healthSlider.linkOffsetY = -52;
    } else if (!this.isSelectOrHove() && this.healthSlider) {
      this.advancedDynamicTexture.removeControl(this.healthSlider)
      this.healthSlider = null;
    } else if (this.isSelectOrHove() && this.healthSlider) {
      if (this.healthSlider) {
        this.healthSlider.value = health;
      }
    }
  }

  setBuildup(buildup: number): void {
    this.getContainer().scaling.y = buildup;
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
        this.advancedDynamicTexture.addControl(this.progressSlider)
        this.progressSlider.linkWithMesh(this.getContainer());
        this.progressSlider.linkOffsetY = -40;
        let nodeMaterial = this.babylonModelService.getBabylonMaterial(this.uiConfigCollectionService.getProgressBarNodeMaterialId());
      }
    } else {
      if (this.progressSlider) {
        this.advancedDynamicTexture.removeControl(this.progressSlider)
        this.progressSlider = null;
      }
    }
  }

  onProjectileFired(destination: DecimalPosition): void {
    if (!this.baseItemType.getWeaponType()!.getMuzzleFlashParticleSystemConfigId()) {
      console.warn(`No MuzzleFlashParticleSystemConfigId for ${this.baseItemType.getInternalName()} '${this.baseItemType.getId()}'`);
      return;
    }

    let correctDestination;
    let pickingInfo = this.rendererService.setupTerrainPickPointFromPosition(destination);
    if (pickingInfo && pickingInfo.hit) {
      correctDestination = pickingInfo.pickedPoint!;
    } else {
      correctDestination = new Vector3(destination.getX(), 0, destination.getY());
    }
    let projectileStartPosition = this.position3D!;
    if (this.baseItemType.getWeaponType()!.getMuzzleFlashParticleSystemConfigId()) {
      let particleSystemEntity = this.babylonModelService.getParticleSystemEntity(this.baseItemType.getWeaponType()!.getMuzzleFlashParticleSystemConfigId()!);
      if (particleSystemEntity.emitterNodeId) {
        const emitterMesh = this.findChildMesh(particleSystemEntity.emitterNodeId!);
        emitterMesh.computeWorldMatrix(true);
        projectileStartPosition = emitterMesh.absolutePosition;
      }
      const particleSystem = this.rendererService.createParticleSystem(particleSystemEntity.id, particleSystemEntity.imageId, projectileStartPosition, correctDestination, false);
      particleSystem.disposeOnStop = true;
      particleSystem.start();
    }
    this.createProjectile(projectileStartPosition, correctDestination);
  }

  onExplode(): void {
    if (GwtHelper.gwtIssueNumber(this.baseItemType.getExplosionParticleId() == null)) {
      console.warn(`No ExplosionParticleId for base item type ${this.getId()}`)
      return;
    }

    let particleSystemConfig = this.babylonModelService.getParticleSystemEntity(this.baseItemType.getExplosionParticleId()!);

    let positionOffset = particleSystemConfig.positionOffset || {x: 0, y: 0, z: 0};

    let emittingPosition = new Vector3(this.getContainer().position.x + positionOffset.x,
      this.getContainer().position.y + positionOffset.z,
      this.getContainer().position.z + positionOffset.y);

    const particleSystem = this.rendererService.createParticleSystem(particleSystemConfig.id,
      particleSystemConfig.imageId,
      emittingPosition,
      null,
      false);
    particleSystem.disposeOnStop = true;
    particleSystem.start();
  }

  setBuildingPosition(razarionBuildingPosition: DecimalPosition): void {
    if (razarionBuildingPosition && this.buildingParticleSystem) {
      return;
    }
    if (!razarionBuildingPosition && !this.buildingParticleSystem) {
      return;
    }

    let particleSystemConfigId = this.baseItemType.getBuilderType()?.getParticleSystemConfigId()
    if (!particleSystemConfigId) {
      return;
    }

    if (!razarionBuildingPosition && this.buildingParticleSystem) {
      this.disposeBuildingParticleSystem();
      return;
    }

    if (razarionBuildingPosition && !this.buildingParticleSystem) {
      try {
        if (this.baseItemType.getBuilderType()?.getParticleSystemConfigId()) {
          let particleSystemEntity = this.babylonModelService.getParticleSystemEntity(this.baseItemType.getBuilderType()?.getParticleSystemConfigId()!);
          const height = LocationVisualization.getHeightFromTerrain(razarionBuildingPosition.getX(), razarionBuildingPosition.getY(), this.rendererService);
          const buildingPosition = new Vector3(razarionBuildingPosition.getX(), height, razarionBuildingPosition.getY());
          let emitterPosition = this.position3D!
          if (particleSystemEntity.emitterNodeId) {
            const emitterMesh = this.findChildMesh(particleSystemEntity.emitterNodeId)
            emitterMesh.computeWorldMatrix(true);
            emitterPosition = emitterMesh.absolutePosition;
          }
          this.buildingParticleSystem = this.rendererService.createParticleSystem(particleSystemEntity.id, particleSystemEntity.imageId, emitterPosition, buildingPosition, true);
          this.buildingParticleSystem.start();
        }
      } catch (e) {
        console.error(e);
      }
    }
  }

  setHarvestingPosition(razarionHarvestingPosition: DecimalPosition | null): void {
    if (razarionHarvestingPosition && this.harvestingParticleSystem) {
      return;
    }
    if (!razarionHarvestingPosition && !this.harvestingParticleSystem) {
      return;
    }

    let particleSystemConfigId = this.baseItemType.getHarvesterType()?.getParticleSystemConfigId()
    if (!particleSystemConfigId) {
      return;
    }

    if (!razarionHarvestingPosition && this.harvestingParticleSystem) {
      this.disposeHarvestingParticleSystem();
      return;
    }

    if (razarionHarvestingPosition && !this.harvestingParticleSystem) {
      try {
        if (this.baseItemType.getHarvesterType()?.getParticleSystemConfigId()) {
          let particleSystemEntity = this.babylonModelService.getParticleSystemEntity(this.baseItemType.getHarvesterType()?.getParticleSystemConfigId()!);
          const harvestingPosition = new Vector3(razarionHarvestingPosition.getX(), this.getContainer().position.y, razarionHarvestingPosition.getY());
          let emitterPosition = this.position3D!
          if (particleSystemEntity.emitterNodeId) {
            const emitterMesh = this.findChildMesh(particleSystemEntity.emitterNodeId)
            emitterMesh.computeWorldMatrix(true);
            emitterPosition = emitterMesh.absolutePosition;
          }
          this.harvestingParticleSystem = this.rendererService.createParticleSystem(particleSystemEntity.id, particleSystemEntity.imageId, emitterPosition, harvestingPosition, true);
          this.harvestingParticleSystem.start();
        }
      } catch (e) {
        console.error(e);
      }
    }
  }

  override onPosition3D(position3D: Vector3): boolean {
    let updateImmediately = !this.position3D || !this.lastPositionUpdateTime;
    this.oldPosition3D = this.position3D;
    this.position3D = position3D.clone();
    this.lastPositionUpdateTime = Date.now();
    return updateImmediately;
  }

  override onRotation3D(rotation3D: Vector3): boolean {
    let updateImmediately = !this.rotation3D || !this.lastRotationUpdateTime;
    this.oldRotation3D = this.rotation3D;
    this.rotation3D = rotation3D.clone();
    this.lastRotationUpdateTime = Date.now();
    return updateImmediately;
  }

  interpolate(date: number): void {
    if (this.lastPositionUpdateTime && this.position3D && this.oldPosition3D) {
      let t = (date - this.lastPositionUpdateTime) / BabylonBaseItemImpl.TICK_TIME_MILLI_SECONDS;
      if (t > 1) {
        t = 1;
      }
      this.getContainer().position = Vector3.Lerp(this.oldPosition3D, this.position3D, t);
    }

    if (this.lastRotationUpdateTime && this.rotation3D && this.oldRotation3D) {
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

  private disposeBuildingParticleSystem() {
    if (this.buildingParticleSystem) {
      this.buildingParticleSystem!.dispose();
      this.buildingParticleSystem = null;
    }
  }

  private disposeHarvestingParticleSystem() {
    if (this.harvestingParticleSystem) {
      this.harvestingParticleSystem!.dispose();
      this.harvestingParticleSystem = null;
    }
  }

  private createProjectile(start: Vector3, destination: Vector3): void {
    if (!this.baseItemType.getWeaponType()!.getProjectileSpeed()) {
      return;
    }
    const projectileSpeed = GwtHelper.gwtIssueNumber(this.baseItemType.getWeaponType()!.getProjectileSpeed());
    if (projectileSpeed <= 0.0) {
      return;
    }

    const mesh = MeshBuilder.CreateSphere("Projectile", {
      diameter: 0.2,
      segments: 1
    }, this.rendererService.getScene());
    mesh.material = this.rendererService.projectileMaterial;

    const trailParticleSystemEntityId = GwtHelper.gwtIssueNumber(this.baseItemType.getWeaponType()!.getTrailParticleSystemConfigId());
    if (trailParticleSystemEntityId || trailParticleSystemEntityId === 0) {
      let particleSystemEntity = this.babylonModelService.getParticleSystemEntity(trailParticleSystemEntityId);
      let particleSystem = this.rendererService.createParticleSystem(particleSystemEntity.id,
        particleSystemEntity.imageId,
        mesh,
        null,
        false);
      particleSystem.disposeOnStop = true;
      particleSystem.start();
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

    keyFrames.push({
      frame: 1,
      value: destination
    });

    xSlide.setKeys(keyFrames);

    mesh.animations.push(xSlide);


    let animatable = this.rendererService.getScene().beginAnimation(mesh,
      0,
      1,
      false,
      projectileSpeed / start.subtract(destination).length());
    animatable.onAnimationEnd = () => {
      this.rendererService.getScene().removeMesh(mesh);
      mesh.dispose();
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
    this.advancedDynamicTexture.addControl(this.nameBlock);
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

}
