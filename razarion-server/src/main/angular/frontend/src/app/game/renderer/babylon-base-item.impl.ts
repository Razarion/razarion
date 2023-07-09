import {Animation, Mesh, MeshBuilder, ParticleHelper, ParticleSystem, Vector3} from "@babylonjs/core";
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

  constructor(id: number, private baseItemType: BaseItemType, diplomacy: Diplomacy, rendererService: ThreeJsRendererServiceImpl, babylonModelService: BabylonModelService) {
    super(id, baseItemType, diplomacy, rendererService, babylonModelService, rendererService.baseItemContainer);
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

      setBuildup(buildup: number): void {
      }

      onProjectileFired(destination: Vertex): void {
      }

      onExplode(): void {
      }

    }
  }

  dispose() {
    this.disposeBuildingParticleSystem();
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
      set.systems.forEach(s => {
        s.disposeOnStop = true;
      });
      set.emitterNode = this.getContainer().position;
      set.start();
    });
  }

  setBuildingPosition(nativeBuildingPosition: NativeVertexDto): void {
    if (nativeBuildingPosition && this.buildingParticleSystem) {
      return;
    }
    if (!nativeBuildingPosition && !this.buildingParticleSystem) {
      return;
    }

    let particleSystemConfigId = this.baseItemType.getBuilderType()?.getParticleSystemConfigId()
    if (!particleSystemConfigId) {
      return;
    }

    if (!nativeBuildingPosition && this.buildingParticleSystem) {
      this.disposeBuildingParticleSystem();
      return;
    }

    if (nativeBuildingPosition && !this.buildingParticleSystem) {
      try {
        let particleSystemConfig = this.babylonModelService.getParticleSystemConfig(this.baseItemType.getBuilderType()?.getParticleSystemConfigId()!);
        const buildingPosition = new Vector3(nativeBuildingPosition.x, nativeBuildingPosition.z, nativeBuildingPosition.y);
        const emitterMesh = this.findChildMesh(particleSystemConfig.getEmitterMeshPath())
        emitterMesh.computeWorldMatrix(true);
        this.buildingParticleSystem = this.createParticleSystem(particleSystemConfig, emitterMesh, buildingPosition, true);
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
