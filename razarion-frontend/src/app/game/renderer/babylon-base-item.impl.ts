import {
  AbstractMesh,
  Animation,
  Mesh,
  MeshBuilder,
  MeshExploder,
  ParticleSystemSet,
  UtilityLayerRenderer,
  Vector3
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
import {Nullable} from '@babylonjs/core/types';

export class BabylonBaseItemImpl extends BabylonItemImpl implements BabylonBaseItem {
  // See GWT java PlanetService
  static readonly TICK_TIME_MILLI_SECONDS: number = 100;
  private baseId: number;
  private buildingParticleSystem: ParticleSystemSet | null = null;
  private harvestingParticleSystem: ParticleSystemSet | null = null;
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
  private readonly uiTexture: AdvancedDynamicTexture;
  private buildupMeshes: Mesh[] | null = null;
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
              disposeCallback: (() => void) | null) {
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
    if (this.baseItemType.getPhysicalAreaConfig().fulfilledMovable()) {
      this.rendererService.removeInterpolationListener(this);
    }
    this.disposeBuildingParticleSystem();
    this.disposeHarvestingParticleSystem();
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
    this.getRenderObject().setEffectsActive(buildup >= 1.0);

    if (buildup >= 1.0 && this.buildupMeshes != null) {
      this.buildupMeshes.forEach(m => m.isVisible = true);
      this.buildupMeshes = null;
      this.updateItemCursor();
      return;
    }

    if (buildup < 1.0 && this.buildupMeshes == null) {

      this.buildupMeshes = this.getContainer().getChildMeshes() as Mesh[];
      this.buildupMeshes = this.buildupMeshes.filter(mesh => mesh.isVisible);

      this.buildupMeshes.sort((a, b) => {
        return a.position.y - b.position.y;
      });

      this.buildupMeshes.forEach(m => m.isVisible = false);
    }

    if (buildup < 1.0 && this.buildupMeshes != null) {
      const stepSize = 1.0 / this.buildupMeshes.length;

      for (let i = 0; i < this.buildupMeshes.length; i++) {
        this.buildupMeshes[i].isVisible = buildup > i * stepSize;
      }
    }
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

    let startPosition: Vector3;
    const renderObject = this.getRenderObject();
    if (renderObject.hasMuzzleFlash()) {
      renderObject.createMuzzleFlashParticleSystemSet()
        ?.then(particleSystemSet => particleSystemSet.start(renderObject.getMuzzleFlashMesh()));
      startPosition = renderObject.getMuzzleFlashMesh().getAbsolutePosition();
    } else {
      startPosition = renderObject.getModel3D().getAbsolutePosition();
    }

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
        if (this.getRenderObject().hasMuzzleFlash()) {
          this.getRenderObject()
            .createMuzzleFlashParticleSystemSet()
            ?.then(particleSystemSet => {
              this.buildingParticleSystem = particleSystemSet;
              particleSystemSet.start(this.getRenderObject().getMuzzleFlashMesh());
            });
          // TODO const buildingPosition = new Vector3(razarionBuildingPosition.getX(), height, razarionBuildingPosition.getY());
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
        if (this.getRenderObject().hasMuzzleFlash()) {
          this.getRenderObject().createMuzzleFlashParticleSystemSet()?.then(particleSystemSet => {
            this.harvestingParticleSystem = particleSystemSet;
            particleSystemSet.start(this.getRenderObject().getMuzzleFlashMesh());
          });
          // TODO const harvestingPosition = new Vector3(razarionHarvestingPosition.getX(), this.getContainer().position.y, razarionHarvestingPosition.getY());
        }
      } catch (e) {
        console.error(e);
      }
    }
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

  private disposeBuildingParticleSystem() {
    if (this.buildingParticleSystem) {
      this.buildingParticleSystem!.dispose();
      this.buildingParticleSystem = null;
    }
  }

  private disposeHarvestingParticleSystem() {
    if (this.harvestingParticleSystem) {
      this.harvestingParticleSystem.dispose();
      this.harvestingParticleSystem = null;
    }
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

}
