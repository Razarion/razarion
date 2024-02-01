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
  Diplomacy,
  MarkerConfig,
  NativeVertexDto,
  ParticleSystemConfig,
  Vertex
} from "../../gwtangular/GwtAngularFacade";
import { GwtHelper } from "../../gwtangular/GwtHelper";
import { BabylonItemImpl } from "./babylon-item.impl";
import { BabylonModelService } from "./babylon-model.service";
import { BabylonRenderServiceAccessImpl } from "./babylon-render-service-access-impl.service";

export class BabylonBaseItemImpl extends BabylonItemImpl implements BabylonBaseItem {
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

  constructor(id: number, private baseItemType: BaseItemType, diplomacy: Diplomacy, rendererService: BabylonRenderServiceAccessImpl, babylonModelService: BabylonModelService) {
    super(id, baseItemType, diplomacy, rendererService, babylonModelService, rendererService.baseItemContainer);

    this.utilLayer = new UtilityLayerRenderer(rendererService.getScene());
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

      updateAngle(): void {
      }

      isEnemy(): boolean {
        return false;
      }

      updatePosition(): void {
      }

      select(active: boolean): void {
      }

      hover(active: boolean): void {
      }

      mark(markerConfig: MarkerConfig | null): void {
      }

      setBuildingPosition(buildingPosition: NativeVertexDto): void {

      }

      setHarvestingPosition(harvestingPosition: NativeVertexDto | null): void {
      }

      setBuildup(buildup: number): void {
      }

      setConstructing(progress: number): void {
      }

      onProjectileFired(destination: Vertex): void {
      }

      onExplode(): void {
      }

    }
  }

  dispose() {
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

  onProjectileFired(destination: Vertex): void {
    if (!this.baseItemType.getWeaponType()!.getMuzzleFlashParticleSystemConfigId()) {
      console.warn(`No MuzzleFlashParticleSystemConfigId for ${this.baseItemType.getInternalName()} '${this.baseItemType.getId()}'`);
      return;
    }
    const correctDestination = new Vector3(destination.getX(), destination.getZ(), destination.getY());
    let particleSystemConfig = this.babylonModelService.getParticleSystemConfig(this.baseItemType.getWeaponType()!.getMuzzleFlashParticleSystemConfigId()!);
    const emitterMesh = this.findChildMesh(particleSystemConfig.getEmitterMeshPath());
    emitterMesh.computeWorldMatrix(true);
    const particleSystem = this.createParticleSystem(particleSystemConfig, emitterMesh, correctDestination, false);
    particleSystem.disposeOnStop = true;

    this.createProjectile(emitterMesh.absolutePosition, correctDestination);
  }

  onExplode(): void {
    ParticleHelper.CreateAsync("explosion", this.rendererService.getScene()).then((set) => {
      const scale = 0.3;
      set.systems.forEach(s => {
        s.disposeOnStop = true;
        s.minSize *= scale;
        s.maxSize *= scale;
        s.minEmitPower *= scale;
        s.maxEmitPower *= scale;
      });
      set.emitterNode = this.getContainer().position.clone();
      set.start();
    });
  }

  setBuildingPosition(razarionBuildingPosition: NativeVertexDto): void {
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
        const buildingPosition = new Vector3(razarionBuildingPosition.x, razarionBuildingPosition.z, razarionBuildingPosition.y);
        const emitterMesh = this.findChildMesh(particleSystemConfig.getEmitterMeshPath())
        emitterMesh.computeWorldMatrix(true);
        this.buildingParticleSystem = this.createParticleSystem(particleSystemConfig, emitterMesh, buildingPosition, true);
      } catch (e) {
        console.error(e);
      }
    }
  }

  setHarvestingPosition(razarionHarvestingPosition: NativeVertexDto | null): void {
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
        const harvestingPosition = new Vector3(razarionHarvestingPosition.x, razarionHarvestingPosition.z, razarionHarvestingPosition.y);
        const emitterMesh = this.findChildMesh(particleSystemConfig.getEmitterMeshPath())
        emitterMesh.computeWorldMatrix(true);
        this.harvestingParticleSystem = this.createParticleSystem(particleSystemConfig, emitterMesh, harvestingPosition, true);
      } catch (e) {
        console.error(e);
      }
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

  private createParticleSystem(particleSystemConfig: ParticleSystemConfig, emitterMesh: Mesh, destination: Vector3, stretchToDestination: boolean): ParticleSystem {
    const particleJsonConfig = this.babylonModelService.getParticleSystemJson(particleSystemConfig.getThreeJsModelId());

    const particleSystem = ParticleSystem.Parse(particleJsonConfig, this.rendererService.getScene(), "");
    particleSystem.emitter = emitterMesh.absolutePosition;
    const beam = destination.subtract(emitterMesh.absolutePosition);
    const delta = 2;
    const direction1 = beam.subtractFromFloats(delta, delta, delta).normalize();
    const direction2 = beam.subtractFromFloats(-delta, -delta, -delta).normalize();
    particleSystem.createPointEmitter(direction1, direction2);
    if (stretchToDestination) {
      const distance = beam.length();
      particleSystem.minLifeTime = distance;
      particleSystem.maxLifeTime = distance;
    }

    return particleSystem;
  }

}
