import {AbstractActionManager, AbstractMesh, AnimationGroup, Node, ParticleSystemSet, TransformNode, Vector3} from '@babylonjs/core';
import {BabylonRenderServiceAccessImpl, RazarionMetadata} from './babylon-render-service-access-impl.service';
import type {Nullable} from '@babylonjs/core/types';
import {ParticleSystemEntity} from '../../generated/razarion-share';
import type {Animatable} from '@babylonjs/core/Animations/animatable.core';

export type BuildAnimationPhase = 'intro' | 'loop' | 'outro' | 'progress';

export class RenderObject {
  private model3D!: TransformNode;
  private effectsActive = true;
  private buildAnimationActive = false;
  private allEffectAnimations: { original: Node, animation: Node }[] = [];
  private activeEffectAnimations: Animatable[] = [];
  // Cloned AnimationGroups for the build phase sequencer (intro -> loop -> outro).
  // Stored as cloned groups so each unit instance plays independently from the source asset.
  private introAnimationGroups: AnimationGroup[] = [];
  private loopAnimationGroups: AnimationGroup[] = [];
  private outroAnimationGroups: AnimationGroup[] = [];
  private progressAnimationGroups: AnimationGroup[] = [];
  private allEffectParticles: { entity: ParticleSystemEntity, emitter: AbstractMesh }[] = [];
  private activeEffectParticles: ParticleSystemSet[] = [];
  private muzzleFlashParticleEntity: ParticleSystemEntity | null = null;
  private muzzleFlashEmitterMesh: Nullable<AbstractMesh> = null;
  private muzzleMesh: Nullable<AbstractMesh> = null;
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

  /**
   * Drives the intro -> loop -> outro animation phase sequence, used for action-driven animations
   * like a Builder raising/lowering its arm during a build job. Independent of setEffectsActive.
   * Units without _intro/_loop/_outro animation groups are unaffected.
   *
   * @param onIntroComplete optional callback fired once the intro phase finishes (or immediately
   *        if there is no intro). Useful for deferring follow-up actions like the build beam start
   *        until the arm is fully extended. Not fired if `active` is false, or if the build
   *        animation is deactivated again before the intro completes.
   * @param targetIntroDurationSeconds optional target wall-clock duration the intro phase should
   *        take. The intro AnimationGroup is played at speedRatio = naturalDuration / target so
   *        the visible animation lines up with a server-driven warmup window. 0/undefined = play
   *        at the natural glTF rate (speedRatio 1.0).
   * @param targetOutroDurationSeconds same idea for the outro phase, lined up with a server cooldown.
   */
  setBuildAnimationActive(active: boolean, onIntroComplete?: () => void,
                          targetIntroDurationSeconds?: number, targetOutroDurationSeconds?: number) {
    if (this.buildAnimationActive == active) {
      return;
    }
    this.buildAnimationActive = active;
    if (active) {
      this.stopAllPhasedGroups();
      if (this.introAnimationGroups.length > 0) {
        this.playGroupsOnce(this.introAnimationGroups, () => {
          if (this.buildAnimationActive) {
            this.startLoopGroups();
            if (onIntroComplete) onIntroComplete();
          }
        }, targetIntroDurationSeconds);
      } else {
        this.startLoopGroups();
        if (onIntroComplete) onIntroComplete();
      }
    } else {
      this.stopAllPhasedGroups();
      if (this.outroAnimationGroups.length > 0) {
        this.playGroupsOnce(this.outroAnimationGroups, () => {
          // outro finished — nothing more to do (arm rests in down pose)
        }, targetOutroDurationSeconds);
      }
    }
  }

  isBuildAnimationActive(): boolean {
    return this.buildAnimationActive;
  }

  addPhasedAnimationGroup(phase: BuildAnimationPhase, group: AnimationGroup) {
    // Make sure the cloned group does not auto-play (Babylon's clone may carry over a running state)
    group.stop();
    switch (phase) {
      case 'intro':
        this.introAnimationGroups.push(group);
        break;
      case 'loop':
        this.loopAnimationGroups.push(group);
        break;
      case 'outro':
        this.outroAnimationGroups.push(group);
        break;
      case 'progress':
        this.progressAnimationGroups.push(group);
        // Snap to the last frame on load so the platform rests at the "build complete" (top)
        // position. Without this the columns sit at the GLB bind pose, which is slightly off.
        // Subsequent setProgressAnimationValue calls stop() first, so this snap doesn't interfere.
        group.start(false, 1.0, group.to, group.to);
        break;
    }
  }

  /**
   * Scrubs the progress animation groups to a specific value (0 = start, 1 = end).
   * Used for factory build progress: the platform position is driven directly by the
   * server-reported build progress rather than playing at a fixed speed.
   */
  setProgressAnimationValue(progress: number): void {
    this.progressAnimationGroups.forEach(group => {
      const frame = group.from + progress * (group.to - group.from);
      // stop() first to clear any leftover _isStarted state from a previous snap call —
      // otherwise Babylon's start() early-returns and the previous animation keeps playing.
      group.stop();
      group.start(false, 1.0, frame, frame);
    });
  }

  hasProgressAnimation(): boolean {
    return this.progressAnimationGroups.length > 0;
  }

  /**
   * Computes the local Y range of the progress animation by scanning all keyframes of the
   * targeted position animations. Used to position the factory scan ring at the same height as
   * the rising columns. Returns null if there are no progress animations.
   */
  getProgressAnimationLocalYRange(): { min: number, max: number } | null {
    if (this.progressAnimationGroups.length === 0) {
      return null;
    }
    let min = Infinity;
    let max = -Infinity;
    this.progressAnimationGroups.forEach(group => {
      group.targetedAnimations.forEach(ta => {
        if (ta.animation.targetProperty !== 'position') return;
        const keys = ta.animation.getKeys();
        keys.forEach((key: any) => {
          const v = key.value;
          if (v && typeof v.y === 'number') {
            if (v.y < min) min = v.y;
            if (v.y > max) max = v.y;
          }
        });
      });
    });
    return min === Infinity ? null : {min, max};
  }

  /**
   * Computes the smaller half-extent (in local XZ) of the progress animation target nodes.
   * Used to size the factory scan ring so it fits inside the rising column footprint.
   */
  getProgressAnimationFootprintHalfExtent(): number | null {
    const ext = this.getProgressAnimationFootprintExtents();
    return ext ? Math.min(ext.width, ext.depth) / 2 : null;
  }

  /**
   * Computes the local XZ bounding box of the progress animation target nodes — full width/depth
   * spans plus the center offset (since the column rectangle isn't necessarily centered on the
   * model origin). Used to size and place the factory's rectangular scan plate.
   */
  getProgressAnimationFootprintExtents(): { width: number, depth: number, centerX: number, centerZ: number } | null {
    if (this.progressAnimationGroups.length === 0) {
      return null;
    }
    let minX = Infinity, maxX = -Infinity;
    let minZ = Infinity, maxZ = -Infinity;
    this.progressAnimationGroups.forEach(group => {
      group.targetedAnimations.forEach(ta => {
        const target: any = ta.target;
        if (target && target.position) {
          if (target.position.x < minX) minX = target.position.x;
          if (target.position.x > maxX) maxX = target.position.x;
          if (target.position.z < minZ) minZ = target.position.z;
          if (target.position.z > maxZ) maxZ = target.position.z;
        }
      });
    });
    if (minX === Infinity) {
      return null;
    }
    return {
      width: maxX - minX,
      depth: maxZ - minZ,
      centerX: (maxX + minX) / 2,
      centerZ: (maxZ + minZ) / 2
    };
  }

  private startLoopGroups() {
    this.loopAnimationGroups.forEach(group => group.start(true, 1.0));
  }

  private playGroupsOnce(groups: AnimationGroup[], onAllComplete: () => void, targetDurationSeconds?: number) {
    let pending = groups.length;
    if (pending === 0) {
      onAllComplete();
      return;
    }
    groups.forEach(group => {
      const speedRatio = RenderObject.computeGroupSpeedRatio(group, targetDurationSeconds);
      group.onAnimationGroupEndObservable.addOnce(() => {
        pending--;
        if (pending === 0) {
          onAllComplete();
        }
      });
      // Pass explicit from/to so we don't depend on the group's internal _from/_to state which can
      // be stale from a previous snap or play.
      group.start(false, speedRatio, group.from, group.to);
    });
  }

  /**
   * Computes a speedRatio that makes the group's wall-clock duration equal targetDurationSeconds.
   * Returns 1.0 (natural playback) if no target is given, the target is non-positive, or the group
   * has no measurable length.
   */
  private static computeGroupSpeedRatio(group: AnimationGroup, targetDurationSeconds: number | undefined): number {
    if (!targetDurationSeconds || targetDurationSeconds <= 0) {
      return 1.0;
    }
    let framePerSecond = 60;
    if (group.targetedAnimations.length > 0) {
      const fps = group.targetedAnimations[0].animation.framePerSecond;
      if (fps && fps > 0) {
        framePerSecond = fps;
      }
    }
    const naturalDurationSeconds = (group.to - group.from) / framePerSecond;
    if (naturalDurationSeconds <= 0) {
      return 1.0;
    }
    return naturalDurationSeconds / targetDurationSeconds;
  }

  private stopAllPhasedGroups() {
    this.introAnimationGroups.forEach(group => group.stop());
    this.loopAnimationGroups.forEach(group => group.stop());
    this.outroAnimationGroups.forEach(group => group.stop());
    this.progressAnimationGroups.forEach(group => group.stop());
  }

  dispose() {
    this.introAnimationGroups.forEach(group => group.dispose());
    this.loopAnimationGroups.forEach(group => group.dispose());
    this.outroAnimationGroups.forEach(group => group.dispose());
    this.progressAnimationGroups.forEach(group => group.dispose());
    this.introAnimationGroups = [];
    this.loopAnimationGroups = [];
    this.outroAnimationGroups = [];
    this.progressAnimationGroups = [];
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

  setMuzzleMesh(muzzleMesh: AbstractMesh) {
    this.muzzleMesh = muzzleMesh;
  }

  /**
   * Returns the beam origin position. Priority:
   * 1. RAZ_MUZZLE mesh (pure position marker)
   * 2. RAZ_M_P_ mesh (muzzle flash particle emitter)
   * 3. null (caller should fall back to model position)
   */
  getBeamOrigin(): Vector3 | null {
    const mesh = this.muzzleMesh ?? this.muzzleFlashEmitterMesh;
    if (mesh) {
      mesh.computeWorldMatrix(true);
      return mesh.getAbsolutePosition().clone();
    }
    return null;
  }

  setTurretMesh(turretMesh: AbstractMesh) {
    this.turretMesh = turretMesh;
  }

  getTurretMesh(): Nullable<AbstractMesh> {
    return this.turretMesh;
  }
}
