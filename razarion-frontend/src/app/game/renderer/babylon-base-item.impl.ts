import {
  Animation,
  InputBlock,
  Mesh,
  MeshBuilder,
  NodeMaterial,
  ParticleHelper,
  ParticleSystem,
  TransformNode,
  UtilityLayerRenderer,
  Vector3
} from "@babylonjs/core";
import {
  BabylonBaseItem,
  BaseItemType,
  DecimalPosition,
  Diplomacy,
  MarkerConfig,
} from "../../gwtangular/GwtAngularFacade";
import { GwtHelper } from "../../gwtangular/GwtHelper";
import { BabylonItemImpl } from "./babylon-item.impl";
import { BabylonModelService } from "./babylon-model.service";
import { BabylonRenderServiceAccessImpl } from "./babylon-render-service-access-impl.service";
import { ActionService } from "../action.service";
import { LocationVisualization } from "src/app/editor/common/place-config/location-visualization";
import { GwtInstance } from "../../gwtangular/GwtInstance";

export class BabylonBaseItemImpl extends BabylonItemImpl implements BabylonBaseItem {
  // See GWT java PlanetService
  static readonly TICK_TIME_MILLI_SECONDS: number = 100;
  private readonly PROGRESS_BAR_NODE_MATERIAL_ID = 54; // Put in properties
  private readonly HEALTH_BAR_NODE_MATERIAL_ID = 55; // Put in properties
  private buildingParticleSystem: ParticleSystem | null = null;
  private harvestingParticleSystem: ParticleSystem | null = null;
  private progressBar: Mesh | undefined;
  private healthBar: Mesh | undefined;
  private progress: number = 0;
  private readonly utilLayer: UtilityLayerRenderer;
  private healthInputBlock: InputBlock | undefined;
  private progressInputBlock: InputBlock | undefined;
  private position3D: Vector3 | null = null;
  private oldPosition3D: Vector3 | null = null;
  private lastPositionUpdateTime: number | null = null;
  private rotation3D: Vector3 | null = null;
  private oldRotation3D: Vector3 | null = null;
  private lastRotationUpdateTime: number | null = null;

  constructor(id: number,
    private baseItemType: BaseItemType,
    diplomacy: Diplomacy,
    rendererService: BabylonRenderServiceAccessImpl,
    actionService: ActionService,
    babylonModelService: BabylonModelService) {
    super(id, baseItemType, diplomacy, rendererService, babylonModelService, actionService, rendererService.baseItemContainer);

    this.utilLayer = new UtilityLayerRenderer(rendererService.getScene());

    if (baseItemType.getPhysicalAreaConfig().fulfilledMovable()) {
      rendererService.addInterpolationListener(this);
    }
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

      getPosition(): DecimalPosition | null {
        return null;
      }

      setHealth(health: number): void {
      }

      setPosition(position: DecimalPosition): void {
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

      onProjectileFired(destination: DecimalPosition): void {
      }

      onExplode(): void {
      }

    }
  }

  dispose() {
    if (this.baseItemType.getPhysicalAreaConfig().fulfilledMovable()) {
      this.rendererService.removeInterpolationListener(this);
    }
    this.disposeBuildingParticleSystem();
    this.disposeHarvestingParticleSystem();
    if (this.healthBar) {
      this.healthBar.material!.dispose()
      this.healthBar = undefined;
    }
    if (this.progressBar) {
      this.progressBar.material!.dispose()
      this.progressBar = undefined;
    }
    super.dispose();
  }

  getBaseItemType(): BaseItemType {
    return this.baseItemType;
  }

  setHealth(health: number): void {
    if (this.isSelectOrHove() && !this.healthBar) {
      this.healthBar = MeshBuilder.CreatePlane("Health Bar", {
        width: this.baseItemType.getPhysicalAreaConfig().getRadius() * 2,
        height: this.baseItemType.getPhysicalAreaConfig().getRadius() * 0.08
      }, this.utilLayer.utilityLayerScene);
      this.healthBar.position.y = 0.5 + this.baseItemType.getPhysicalAreaConfig().getRadius() - this.baseItemType.getPhysicalAreaConfig().getRadius() * 0.08;
      this.healthBar.parent = this.getContainer();
      this.healthBar.billboardMode = TransformNode.BILLBOARDMODE_ALL;
      let nodeMaterial = this.babylonModelService.getNodeMaterial(this.HEALTH_BAR_NODE_MATERIAL_ID);
      this.healthBar.material = nodeMaterial.clone(`${nodeMaterial.name} '${this.getId()}'`);
      this.healthInputBlock = <InputBlock>(<NodeMaterial>this.healthBar.material).getBlockByName("health");
      if (this.healthInputBlock) {
        this.healthInputBlock.value = health;
      } else {
        console.warn(`Health block not found in NodeMaterial ${this.HEALTH_BAR_NODE_MATERIAL_ID}`)
      }
    } else if (!this.isSelectOrHove() && this.healthBar) {
      this.healthBar.material!.dispose()
      this.healthBar.dispose()
      this.healthBar = undefined;
    } else if (this.isSelectOrHove() && this.healthBar) {
      if (this.healthInputBlock) {
        this.healthInputBlock.value = health;
      }
    }
  }

  setBuildup(buildup: number): void {
    this.getContainer().scaling.y = buildup;
  }

  setConstructing(progress: number): void {
    this.progress = progress;
    this.handleConstructing();
    if (this.progressInputBlock) {
      this.progressInputBlock.value = this.progress;
    }
  }

  handleConstructing(): void {
    if (this.isSelectOrHove() && this.progress > 0) {
      if (!this.progressBar) {
        this.progressBar = MeshBuilder.CreatePlane("Progress Bar", {
          width: this.baseItemType.getPhysicalAreaConfig().getRadius() * 2,
          height: this.baseItemType.getPhysicalAreaConfig().getRadius() * 0.08
        }, this.utilLayer.utilityLayerScene);

        this.progressBar.position.y = 0.5 + this.baseItemType.getPhysicalAreaConfig().getRadius();
        this.progressBar.parent = this.getContainer();
        this.progressBar.billboardMode = TransformNode.BILLBOARDMODE_ALL;
        let nodeMaterial = this.babylonModelService.getNodeMaterial(this.PROGRESS_BAR_NODE_MATERIAL_ID);
        this.progressBar.material = nodeMaterial.clone(`${nodeMaterial.name} '${this.getId()}'`);
        this.progressInputBlock = <InputBlock>(<NodeMaterial>this.progressBar.material).getBlockByName("progress");
        if (!this.progressInputBlock) {
          console.warn(`Health block not found in NodeMaterial ${this.HEALTH_BAR_NODE_MATERIAL_ID}`)
        }
      }
    } else {
      if (this.progressBar) {
        this.progressBar.material!.dispose();
        this.progressBar.dispose();
        this.progressBar = undefined;
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

    let particleSystemConfig = this.babylonModelService.getParticleSystemConfig(this.baseItemType.getWeaponType()!.getMuzzleFlashParticleSystemConfigId()!);
    const emitterMesh = this.findChildMesh(particleSystemConfig.getEmitterMeshPath());
    emitterMesh.computeWorldMatrix(true);
    const particleSystem = this.rendererService.createParticleSystem(particleSystemConfig.getThreeJsModelId(), particleSystemConfig.getImageId(), emitterMesh.absolutePosition, correctDestination, false);
    particleSystem.disposeOnStop = true;
    particleSystem.start();

    this.createProjectile(emitterMesh.absolutePosition, correctDestination);
  }

  onExplode(): void {
    if (GwtHelper.gwtIssueNumber(this.baseItemType.getExplosionParticleId() == null)) {
      console.warn(`No ExplosionParticleId for base item type ${this.getId()}`)
      return;
    }

    let particleSystemConfig = this.babylonModelService.getParticleSystemConfig(this.baseItemType.getExplosionParticleId()!);

    let positionOffset = particleSystemConfig.getPositionOffset() || GwtInstance.newVertex(0, 0, 0);

    let emittingPosition = new Vector3(this.getContainer().position.x + positionOffset.getX(),
      this.getContainer().position.y + positionOffset.getZ(),
      this.getContainer().position.z + positionOffset.getY());

    const particleSystem = this.rendererService.createParticleSystem(particleSystemConfig.getThreeJsModelId(),
      particleSystemConfig.getImageId(),
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
        let particleSystemConfig = this.babylonModelService.getParticleSystemConfig(this.baseItemType.getBuilderType()?.getParticleSystemConfigId()!);
        const height = LocationVisualization.getHeightFromTerrain(razarionBuildingPosition.getX(), razarionBuildingPosition.getY(), this.rendererService);
        const buildingPosition = new Vector3(razarionBuildingPosition.getX(), height, razarionBuildingPosition.getY());
        const emitterMesh = this.findChildMesh(particleSystemConfig.getEmitterMeshPath())
        emitterMesh.computeWorldMatrix(true);
        this.buildingParticleSystem = this.rendererService.createParticleSystem(particleSystemConfig.getThreeJsModelId(), particleSystemConfig.getImageId(), emitterMesh.absolutePosition, buildingPosition, true);
        this.buildingParticleSystem.start();
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
        let particleSystemConfig = this.babylonModelService.getParticleSystemConfig(this.baseItemType.getHarvesterType()?.getParticleSystemConfigId()!);
        const harvestingPosition = new Vector3(razarionHarvestingPosition.getX(), this.getContainer().position.y, razarionHarvestingPosition.getY());
        const emitterMesh = this.findChildMesh(particleSystemConfig.getEmitterMeshPath())
        emitterMesh.computeWorldMatrix(true);
        this.harvestingParticleSystem = this.rendererService.createParticleSystem(particleSystemConfig.getThreeJsModelId(), particleSystemConfig.getImageId(), emitterMesh.absolutePosition, harvestingPosition, true);
        this.harvestingParticleSystem.start();
      } catch (e) {
        console.error(e);
      }
    }
  }

  onPosition3D(position3D: Vector3): boolean {
    let updateImmediately = !this.position3D || !this.lastPositionUpdateTime;
    this.oldPosition3D = this.position3D;
    this.position3D = position3D.clone();
    this.lastPositionUpdateTime = Date.now();
    return updateImmediately;
  }

  onRotation3D(rotation3D: Vector3): boolean {
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
