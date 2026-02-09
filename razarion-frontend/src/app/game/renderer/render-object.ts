import {AbstractActionManager, AbstractMesh, Node, ParticleSystemSet, TransformNode, Vector3} from '@babylonjs/core';
import {BabylonRenderServiceAccessImpl, RazarionMetadata} from './babylon-render-service-access-impl.service';
import type {Nullable} from '@babylonjs/core/types';
import {ParticleSystemEntity} from '../../generated/razarion-share';
import type {Animatable} from '@babylonjs/core/Animations/animatable.core';

export class RenderObject {
  private model3D!: TransformNode;
  private effectsActive = true;
  private allEffectAnimations: { original: Node, animation: Node }[] = [];
  private activeEffectAnimations: Animatable[] = [];
  private allEffectParticles: { entity: ParticleSystemEntity, emitter: AbstractMesh }[] = [];
  private activeEffectParticles: ParticleSystemSet[] = [];
  private muzzleFlashParticleEntity: ParticleSystemEntity | null = null;
  private muzzleFlashEmitterMesh: Nullable<AbstractMesh> = null;
  private turretMesh: Nullable<AbstractMesh> = null;

  constructor(private rendererService: BabylonRenderServiceAccessImpl) {
  }

  setModel3D(model3D: TransformNode) {
    this.model3D = model3D;
  }

  getModel3D(): TransformNode {
    return this.model3D;
  }

  setRotation(rotation3D: Vector3) {
    this.model3D.rotationQuaternion = null;
    this.model3D.rotation = rotation3D
  }

  setRotationY(y: number) {
    this.model3D.rotationQuaternion = null;
    this.model3D.rotation.y = y;
  }

  setRotationYZ(y: number, z: number) {
    this.model3D.rotationQuaternion = null;
    this.model3D.rotation.y = y;
    this.model3D.rotation.z = z;
  }

  setEffectsActive(active: boolean) {
    if (this.effectsActive == active) {
      return;
    }
    if (active) {
      this.allEffectAnimations.forEach((nodes) => {
        const animatable = nodes.original.getScene().beginAnimation(nodes.animation, 0, 60, true);
        this.activeEffectAnimations.push(animatable);
      })
      this.allEffectParticles.forEach((data) => {
        this.rendererService.createParticleSystem(data.entity.id, data.entity.imageId)?.then(particleSystemSet => {
          particleSystemSet.start(data.emitter);
          this.activeEffectParticles.push(particleSystemSet);
        });
      })
    } else {
      this.activeEffectAnimations.forEach((animation => animation.stop()));
      this.activeEffectAnimations = [];
      this.activeEffectParticles.forEach((particleSystemSet => {
        particleSystemSet.dispose();
      }));
      this.activeEffectParticles = [];
    }
    this.effectsActive = active;
  }

  dispose() {
    this.model3D.dispose();
  }

  setPosition(position: Vector3) {
    this.model3D.position = position;
  }

  setPositionXZ(x: number, z: number) {
    this.model3D.position.x = x;
    this.model3D.position.z = z;
  }

  increaseHeight(delta: number) {
    this.model3D.position.y += delta;
  }

  setMetadata(razarionMetadata: RazarionMetadata) {
    this.model3D.getChildMeshes().forEach(childMesh => {
      BabylonRenderServiceAccessImpl.setRazarionMetadata(childMesh, razarionMetadata);
    });
  }

  setActionManager(actionManager: Nullable<AbstractActionManager>) {
    this.model3D.getChildMeshes().forEach(childMesh => {
      childMesh.actionManager = actionManager;
    });
    if (this.model3D.hasOwnProperty('actionManager')) {
      (<AbstractMesh>this.model3D).actionManager = actionManager;
    }
  }

  setName(name: string) {
    this.model3D.name = name;
  }

  prefixName(name: string) {
    this.model3D.name = `Slope ${this.model3D.name}`;
  }

  setParent(parent: Nullable<Node>) {
    this.model3D.parent = parent;
  }

  addEffectAnimation(original: Node, animation: Node) {
    this.allEffectAnimations.push({original: original, animation: animation})
    if (this.effectsActive) {
      const animatable = original.getScene().beginAnimation(animation, 0, 60, true);
      this.activeEffectAnimations.push(animatable);
    }
  }

  addEffectParticleSystem(particleSystemEntity: ParticleSystemEntity, emitterMesh: AbstractMesh) {
    this.allEffectParticles.push({entity: particleSystemEntity, emitter: emitterMesh});
    if (this.effectsActive) {
      this.rendererService.createParticleSystem(particleSystemEntity.id, particleSystemEntity.imageId)?.then(particleSystemSet => {
        particleSystemSet.start(emitterMesh);
        this.activeEffectParticles.push(particleSystemSet);
      });
    }
  }

  addAllShadowCasters(rendererService: BabylonRenderServiceAccessImpl) {
    this.model3D.getChildMeshes().forEach(childMesh => {
      rendererService.shadowGenerator.addShadowCaster(childMesh, true);
    });
  }

  removeAllShadowCasters(rendererService: BabylonRenderServiceAccessImpl) {
    this.model3D.getChildMeshes().forEach(childMesh => {
      rendererService.shadowGenerator.removeShadowCaster(childMesh, true);
    });
  }

  setMuzzleFlash(particleSystemEntity: ParticleSystemEntity, muzzleFlashMesh: AbstractMesh) {
    this.muzzleFlashParticleEntity = particleSystemEntity;
    this.muzzleFlashEmitterMesh = muzzleFlashMesh;
  }

  hasMuzzleFlash() {
    return this.muzzleFlashParticleEntity !== null && this.muzzleFlashEmitterMesh !== null;
  }

  createMuzzleFlashParticleSystemSet(): Promise<ParticleSystemSet> | null {
    return this.rendererService.createParticleSystem(this.muzzleFlashParticleEntity!.id, this.muzzleFlashParticleEntity!.imageId);
  }

  getMuzzleFlashMesh(): AbstractMesh {
    return this.muzzleFlashEmitterMesh!;
  }

  setTurretMesh(turretMesh: AbstractMesh) {
    this.turretMesh = turretMesh;
  }

  getTurretMesh(): Nullable<AbstractMesh> {
    return this.turretMesh;
  }
}
