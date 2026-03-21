import {
  AbstractMesh,
  Animation,
  Mesh,
  MeshBuilder,
  MeshExploder,
  UtilityLayerRenderer,
  Vector3,
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
import {BabylonMuzzleFlash} from "./babylon-muzzle-flash";
import {BabylonBuildupEffect} from "./babylon-buildup-effect";
import {BabylonHarvestingBeam} from "./babylon-harvesting-beam";
import {BabylonImpact} from "./babylon-impact";
import {BabylonWreckage} from "./babylon-wreckage";
import {BabylonDamageEffect} from "./babylon-damage-effect";

export class BabylonBaseItemImpl extends BabylonItemImpl implements BabylonBaseItem {
  // See GWT java PlanetService
  static readonly TICK_TIME_MILLI_SECONDS: number = 100;
  private baseId: number;
  private harvestingBeamEffect: BabylonHarvestingBeam | null = null;
  private progressSlider: Slider | null = null;
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
  private healthCallback: ((health: number) => void) | null = null;
  private lastHealth: number = 1.0;
  private readonly uiTexture: AdvancedDynamicTexture;
  private buildupEffect: BabylonBuildupEffect | null = null;
  private isExploding = false;
  private damageEffect: BabylonDamageEffect | null = null;

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
    this.buildupEffect?.cleanup();
    this.buildupEffect = null;
    this.harvestingBeamEffect?.dispose();
    this.harvestingBeamEffect = null;
    this.damageEffect?.dispose();
    this.damageEffect = null;
    if (this.progressSlider) {
      this.uiTexture.removeControl(this.progressSlider)
      this.progressSlider = null;
    }
    if (this.nameBlock) {
      this.uiTexture.removeControl(this.nameBlock)
      this.nameBlock = null;
    }
    this.uiTexture.dispose();
  }

  getBaseItemType(): BaseItemType {
    return this.baseItemType;
  }

  getHealth(): number {
    return this.lastHealth;
  }

  setHealthCallback(callback: ((health: number) => void) | null) {
    this.healthCallback = callback;
  }

  setHealth(health: number): void {
    this.lastHealth = health;
    if (this.healthCallback) {
      this.healthCallback(health);
    }

    // Damage visual effects (smoke, fire, material darkening)
    if (health < 1.0) {
      if (!this.damageEffect) {
        this.damageEffect = new BabylonDamageEffect(
          this.rendererService.getScene(),
          this.getContainer(),
          this.baseItemType.getPhysicalAreaConfig().getRadius()
        );
      }
      this.damageEffect.update(health);
    } else if (this.damageEffect) {
      this.damageEffect.update(1.0);
      this.damageEffect.dispose();
      this.damageEffect = null;
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
      if (this.buildupEffect?.isInitialized()) {
        this.buildupEffect.complete();
        if (!this.buildupEffect.isBuildingBeamActive()) {
          this.buildupEffect = null;
        }
        this.updateItemCursor();
      }
      return;
    }

    // Buildup not progressing: builder stopped, remove ring but keep mesh state
    if (previousBuildup !== null && buildup === previousBuildup) {
      if (this.buildupEffect?.isInitialized()) {
        this.buildupEffect.cleanupRing();
      }
      return;
    }

    if (!this.buildupEffect) {
      this.buildupEffect = new BabylonBuildupEffect(
        this.rendererService.getScene(),
        this.getContainer() as Mesh,
        BabylonRenderServiceAccessImpl.color4Diplomacy(this.diplomacy),
        this.baseItemType.getPhysicalAreaConfig().getRadius()
      );
    }

    if (!this.buildupEffect.isInitialized()) {
      this.buildupEffect.init();
    }

    if (this.buildupEffect.isInitialized() && !this.buildupEffect.hasScanLine()) {
      this.buildupEffect.createRing();
    }

    if (this.buildupEffect.isInitialized()) {
      this.buildupEffect.updateVisibility(buildup);
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

  setBuildupCallback(callback: ((buildup: number) => void) | null) {
    this.buildupCallback = callback;
  }

  handleConstructing(): void {
    // Only show progress slider for factories, builders have the buildup animation
    if (this.isSelectOrHove() && this.progress > 0 && this.baseItemType.getFactoryType() != null) {
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
    let startPosition: Vector3 = renderObject.getBeamOrigin() ?? renderObject.getModel3D().getAbsolutePosition();

    // Procedural muzzle flash particle systems
    BabylonMuzzleFlash.fire(this.rendererService.getScene(), startPosition, this.getContainer().forward.clone());

    const targetPos = targetBaseItem.getContainer().position.clone();
    // Raise impact point above the target so the effect is visible, not hidden inside the mesh
    const targetRadius = targetBaseItem.getBaseItemType().getPhysicalAreaConfig().getRadius();
    targetPos.y += targetRadius * 0.8;
    this.createProjectile(startPosition, null, targetPos);
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

    const wreckagePosition = this.getContainer().position.clone();
    const wreckageRadius = this.baseItemType.getPhysicalAreaConfig().getRadius();

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
        const MAX_DISTANCE = 10;

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
            // Detach visible meshes and create extra clones for more debris
            const survivingMeshes: Mesh[] = [];
            const originals: Mesh[] = [];
            for (const mesh of this.getContainer().getChildMeshes(false) as Mesh[]) {
              if (mesh.isVisible && mesh.getTotalVertices() > 0) {
                mesh.setParent(null);
                survivingMeshes.push(mesh);
                originals.push(mesh);
              }
            }
            // Clone each original 2-3 extra times for denser debris
            for (const orig of originals) {
              const extraCount = 5 + Math.floor(Math.random() * 4);
              for (let c = 0; c < extraCount; c++) {
                try {
                  const clone = orig.clone(`${orig.name}_debris${c}`, null);
                  if (clone) {
                    clone.material = orig.material;
                    survivingMeshes.push(clone);
                  }
                } catch (_) {}
              }
            }
            this.dispose();
            BabylonWreckage.spawn(
              this.rendererService.getScene(),
              wreckagePosition,
              wreckageRadius,
              survivingMeshes
            );
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

    if (hasPosition && this.buildupEffect?.isBuildingBeamActive()) {
      return;
    }
    if (!hasPosition && !this.buildupEffect?.isBuildingBeamActive()) {
      return;
    }

    if (!hasPosition) {
      this.buildupEffect?.disposeBuildingBeam();
      return;
    }

    if (!this.buildupEffect) {
      this.buildupEffect = new BabylonBuildupEffect(
        this.rendererService.getScene(),
        this.getContainer() as Mesh,
        BabylonRenderServiceAccessImpl.color4Diplomacy(this.diplomacy),
        this.baseItemType.getPhysicalAreaConfig().getRadius()
      );
    }

    const renderObject = this.getRenderObject();
    this.buildupEffect.startBuildingBeam(
      new Vector3(bx!, this.getContainer().position.y, by!),
      {
        getBeamOrigin: () => renderObject.getBeamOrigin(),
        getContainerPosition: () => this.getContainer().position.clone()
      },
      (target: Vector3) => {
        const targetItem = this.rendererService.findBabylonBaseItemAtPosition(target);
        if (targetItem?.buildupEffect?.scanLine) {
          targetItem.buildupEffect.scanLine.computeWorldMatrix(true);
          return targetItem.buildupEffect.scanLine.getAbsolutePosition().clone();
        }
        return null;
      }
    );
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

    if (hasPosition && this.harvestingBeamEffect?.isActive()) {
      return;
    }
    if (!hasPosition && !this.harvestingBeamEffect) {
      return;
    }

    if (!hasPosition) {
      this.harvestingBeamEffect?.dispose();
      this.harvestingBeamEffect = null;
      return;
    }

    const renderObject = this.getRenderObject();
    this.harvestingBeamEffect = new BabylonHarvestingBeam(
      this.rendererService.getScene(),
      () => renderObject.getBeamOrigin(),
      () => this.getContainer().position.clone()
    );
    this.harvestingBeamEffect.start(new Vector3(hx!, this.getContainer().position.y, hy!));
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



  private createProjectile(start: Vector3, targetImpactMesh: Nullable<AbstractMesh>, targetPosition: Nullable<Vector3>): void {
    if (!this.baseItemType.getWeaponType()!.getProjectileSpeed()) {
      return;
    }
    const projectileSpeed = GwtHelper.gwtIssueNumber(this.baseItemType.getWeaponType()!.getProjectileSpeed());
    if (projectileSpeed <= 0.0) {
      return;
    }

    const scene = this.rendererService.getScene();

    const meshProjectile = MeshBuilder.CreateSphere("Projectile", {
      diameter: 0.2,
      segments: 1
    }, scene);
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


    let animatable = scene.beginAnimation(meshProjectile,
      0,
      1,
      false,
      projectileSpeed / start.subtract(destination).length());
    animatable.onAnimationEnd = () => {
      try {
        scene.removeMesh(meshProjectile);
        meshProjectile.dispose();
        BabylonImpact.detonate(scene, destination);
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
