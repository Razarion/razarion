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
  NativeVertexDto,
  ParticleSystemConfig,
  Vertex
} from "../../gwtangular/GwtAngularFacade";
import {GwtHelper} from "../../gwtangular/GwtHelper";
import {BabylonItemImpl} from "./babylon-item.impl";
import {BabylonModelService} from "./babylon-model.service";
import {ThreeJsRendererServiceImpl} from "./three-js-renderer-service.impl";

export class BabylonBaseItemImpl extends BabylonItemImpl implements BabylonBaseItem {
  private health: number = 0;
  private buildingParticleSystem: ParticleSystem | null = null;
  private harvestingParticleSystem: ParticleSystem | null = null;
  private constructing: Mesh | undefined;
  private progress: number = 0;
  private readonly utilLayer: UtilityLayerRenderer;
  private readonly constructingMaterial: NodeMaterial;
  private readonly PROGRESS_BAR_NODE_MATERIAL_ID = 54;

  constructor(id: number, private baseItemType: BaseItemType, diplomacy: Diplomacy, rendererService: ThreeJsRendererServiceImpl, babylonModelService: BabylonModelService) {
    super(id, baseItemType, diplomacy, rendererService, babylonModelService, rendererService.baseItemContainer);

    this.utilLayer = new UtilityLayerRenderer(rendererService.getScene());
    // Setup constructing material
    this.constructingMaterial = this.babylonModelService.getNodeMaterial(this.PROGRESS_BAR_NODE_MATERIAL_ID);
    let progressBlock = this.constructingMaterial.getBlockByName("progress");
    if (progressBlock) {
      this.constructingMaterial.onBindObservable.add((mesh: any) => {
        (<InputBlock>progressBlock).value = (<any>mesh).progress;
      });
    } else {
      console.warn(`Progress block not found in NodeMaterial ${this.PROGRESS_BAR_NODE_MATERIAL_ID}`)
    }
  }

  public static createDummy(id: number): BabylonBaseItem {
    return new class implements BabylonBaseItem {
      dispose(): void {
      }

      setAngle(angle: number): void {
      }

      getAngle(): number {
        return 0;
      }

      getHealth(): number {
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

      updateHealth(): void {
      }

      updatePosition(): void {
      }

      select(active: boolean): void {
      }

      hover(active: boolean): void {
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
    super.dispose();
  }

  getHealth(): number {
    return this.health;
  }

  setHealth(health: number): void {
    this.health = health;
  }

  updateHealth(): void {
  }

  setBuildup(buildup: number): void {
    this.getContainer().scaling.y = buildup;
  }

  setConstructing(progress: number): void {
    this.progress = progress;
    this.handleConstructing();
    if (this.constructing) {
      (<any>this.constructing).progress = this.progress;
    }
  }

  handleConstructing(): void {
    if (this.isSelectOdHove() && this.progress > 0) {
      if (!this.constructing) {
        this.constructing = MeshBuilder.CreatePlane("Status", {
          width: this.baseItemType.getPhysicalAreaConfig().getRadius() * 2,
          height: this.baseItemType.getPhysicalAreaConfig().getRadius() * 0.08
        }, this.utilLayer.utilityLayerScene);

        this.constructing.position.y = 0.5 + this.baseItemType.getPhysicalAreaConfig().getRadius();
        this.constructing.parent = this.getContainer();
        this.constructing.billboardMode = TransformNode.BILLBOARDMODE_ALL;
        this.constructing.material = this.constructingMaterial;
      }
    } else {
      if (this.constructing) {
        this.constructing.dispose();
        this.constructing = undefined;
      }
    }
  }

  onProjectileFired(destination: Vertex): void {
    if (!this.baseItemType.getWeaponType().getMuzzleFlashParticleSystemConfigId()) {
      console.warn(`No MuzzleFlashParticleSystemConfigId for ${this.baseItemType.getInternalName()} '${this.baseItemType.getId()}'`);
      return;
    }
    const correctDestination = new Vector3(destination.getX(), destination.getZ(), destination.getY());
    let particleSystemConfig = this.babylonModelService.getParticleSystemConfig(this.baseItemType.getWeaponType().getMuzzleFlashParticleSystemConfigId()!);
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
    if (!this.baseItemType.getWeaponType().getProjectileSpeed()) {
      return;
    }
    const projectileSpeed = GwtHelper.gwtIssueNumber(this.baseItemType.getWeaponType().getProjectileSpeed());
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
