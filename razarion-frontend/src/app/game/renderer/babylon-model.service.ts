import {Injectable, NgZone} from "@angular/core";
import {Diplomacy} from "src/app/gwtangular/GwtAngularFacade";
import {GwtHelper} from "../../gwtangular/GwtHelper";
import {HttpClient} from "@angular/common/http";
import {
  AbstractMesh,
  AnimationGroup,
  Color3,
  InstancedMesh,
  Material,
  Mesh,
  Node,
  NodeMaterial,
  NodeParticleSystemSet,
  Scene,
  SceneLoader,
  TransformNode
} from "@babylonjs/core";
import {GLTFFileLoader} from "@babylonjs/loaders";
import {Model3DEntity, ParticleSystemEntity} from "src/app/generated/razarion-share";
import {SimpleMaterial} from "@babylonjs/materials";
import {UiConfigCollectionService} from "../ui-config-collection.service";
import {BabylonMaterialContainer, GlbContainer, ParticleSystemSetContainer} from "./babylon-model-container";
import {GltfHelper} from "./gltf-helper";
import {BabylonRenderServiceAccessImpl} from './babylon-render-service-access-impl.service';
import {BuildAnimationPhase, RenderObject} from './render-object';

type AnimationGroupClassification = BuildAnimationPhase | 'legacy';


@Injectable({
  providedIn: 'root'
})
export class BabylonModelService {
  public static readonly RAZ_P_ = "RAZ_P_"
  public static readonly RAZ_M_P_ = "RAZ_M_P_"
  public static readonly RAZ_MUZZLE = "RAZ_MUZZLE"
  public static readonly RAZ_TURRET_ = "RAZ_TURRET_"
  //
  private babylonMaterialContainer = new BabylonMaterialContainer();
  private glbContainer;
  public glbContainerProgress?: { loaded: number, total: number };
  private particleSystemContainer = new ParticleSystemSetContainer();
  private model3DEntities: Map<number, Model3DEntity> = new Map();
  private scene!: Scene;
  private gwtResolver?: () => void;
  public renderer!: BabylonRenderServiceAccessImpl;

  constructor(private uiConfigCollectionService: UiConfigCollectionService,
              httpClient: HttpClient,
              zone: NgZone) {
    SceneLoader.RegisterPlugin(new GLTFFileLoader());
    this.glbContainer = new GlbContainer(this.babylonMaterialContainer, zone);
    this.babylonMaterialContainer.setHttpClient(httpClient);
    this.particleSystemContainer.setHttpClient(httpClient);
  }

  setScene(scene: Scene) {
    this.scene = scene;
  }

  init(): Promise<void> {
    this.loadUiConfigCollection();
    return new Promise<void>((resolve, reject) => {
      this.handleResolve(resolve)
    });
  }

  private handleResolve(handler: () => void) {
    if (this.babylonMaterialContainer.isLoaded() && this.glbContainer.isLoaded() && this.particleSystemContainer.isLoaded()) {
      handler();
    } else {
      this.gwtResolver = handler;
    }
  }

  private loadUiConfigCollection() {
    this.uiConfigCollectionService.getUiConfigCollection().then(uiConfigCollection => {
      this.babylonMaterialContainer.load(uiConfigCollection.babylonMaterials, this, this.scene);
      this.setupModel3DEntities(uiConfigCollection.model3DEntities);
      this.particleSystemContainer.load(uiConfigCollection.particleSystemEntities, this, this.scene)
      this.glbContainer.load(uiConfigCollection.gltfs, this, this.scene);
    });
  }

  public handleLoaded(): void {
    if (this.babylonMaterialContainer.isLoaded() && this.glbContainer.isLoaded() && this.particleSystemContainer.isLoaded()) {
      if (this.gwtResolver) {
        this.gwtResolver();
      }
    }
  }

  cloneModel3D(model3DId: number, parent: Node | null, diplomacy?: Diplomacy): RenderObject {
    model3DId = GwtHelper.gwtIssueNumber(model3DId);

    let model3DEntity = this.model3DEntities.get(model3DId);
    if (!model3DEntity) {
      throw new Error(`No Model3DEntity for ${model3DId}`);
    }
    let gltfHelper = this.glbContainer.getGltfHelper(model3DEntity.gltfEntityId);
    if (!gltfHelper) {
      throw new Error(`No GltfHelper for gltfEntityId ${model3DEntity.gltfEntityId} for model3DId ${model3DId}`);
    }

    let assetContainer = this.glbContainer.getBabylonModel(model3DEntity.gltfEntityId);
    if (!assetContainer) {
      throw new Error(`No AssetContainer for gltfEntityId ${model3DEntity.gltfEntityId} for model3DId ${model3DId}`);
    }


    // Sort matching animation groups into "legacy" (no suffix; played per-node via beginAnimation
    // for backwards compatibility) and "phased" (intro/loop/outro; played as cloned AnimationGroups
    // by the build phase sequencer).
    const allowedLegacyAnimationUniqueIds: Set<number> = new Set();
    const phasedSourceGroups: { phase: BuildAnimationPhase, group: AnimationGroup }[] = [];
    if (assetContainer.animationGroups) {
      assetContainer.animationGroups.forEach(animationGroup => {
        if (animationGroup.name.startsWith(model3DEntity?.gltfName)) {
          const classification = BabylonModelService.classifyAnimationGroup(animationGroup.name, model3DEntity!.gltfName);
          if (classification === 'legacy') {
            animationGroup.targetedAnimations.forEach(targetedAnimation => {
              allowedLegacyAnimationUniqueIds.add(targetedAnimation.animation.uniqueId);
            })
          } else {
            phasedSourceGroups.push({phase: classification, group: animationGroup});
          }
        }
      });
    }

    let node = assetContainer
      .getNodes()
      .find(childNode => childNode.name === model3DEntity.gltfName);

    if (!node) {
      throw new Error(`Node withe name "${model3DEntity.gltfName}" from model3DId '${model3DId}' not found'`);
    }

    const sourceMap = new Map<string, Mesh>();
    const nodeByName = new Map<string, Node>();
    const renderObject = new RenderObject(this.renderer);
    let transformNode = this.deepCloneNode(node, parent, sourceMap, nodeByName, gltfHelper, allowedLegacyAnimationUniqueIds, renderObject, diplomacy);
    renderObject.setModel3D(transformNode);
    renderObject.setPositionXZ(0.0, 0.0);

    // Clone phased animation groups, retargeting each Animation onto the corresponding cloned node.
    // This is the canonical Babylon way to give each unit instance its own independent phase animations.
    phasedSourceGroups.forEach(({phase, group}) => {
      const clonedGroup = group.clone(`${group.name}#${transformNode.uniqueId}`, (originalTarget: any) => {
        if (!originalTarget) {
          return originalTarget;
        }
        if (originalTarget.id !== undefined) {
          const clonedById = sourceMap.get(originalTarget.id);
          if (clonedById) return clonedById;
        }
        if (originalTarget.name) {
          const clonedByName = nodeByName.get(originalTarget.name);
          if (clonedByName) return clonedByName;
        }
        // Fallback: leave the original target. Better than nothing — a missing target would throw.
        return originalTarget;
      });
      renderObject.addPhasedAnimationGroup(phase, clonedGroup);
    });

    return renderObject;
  }

  private static classifyAnimationGroup(animationGroupName: string, gltfName: string): AnimationGroupClassification {
    const suffix = animationGroupName.substring(gltfName.length);
    if (suffix === '_intro') return 'intro';
    if (suffix === '_loop') return 'loop';
    if (suffix === '_outro') return 'outro';
    if (suffix === '_progress') return 'progress';
    return 'legacy';
  }

  private deepCloneNode(root: Node,
                        parent: Node | null,
                        sourceMap: Map<string, Mesh>,
                        nodeByName: Map<string, Node>,
                        gltfHelper: GltfHelper,
                        allowedAnimationUniqueIds: Set<number>,
                        renderObject: RenderObject,
                        diplomacy?: Diplomacy): TransformNode {
    let clonedRoot = root.clone(root.name, parent, true);
    if (!clonedRoot) {
      return root as TransformNode;
    }
    clonedRoot.metadata = {};
    sourceMap.set(root.id, <Mesh>clonedRoot);
    if (root.name) {
      nodeByName.set(root.name, clonedRoot);
    }
    if (clonedRoot instanceof Mesh) {
      const mesh = <Mesh>clonedRoot;
      mesh.receiveShadows = true;
      mesh.hasVertexAlpha = false;
      gltfHelper.handleMaterial(mesh, diplomacy);
    } else if (clonedRoot instanceof TransformNode) {
      const transformNode = <TransformNode>clonedRoot;
      if (root.animations && root.animations.length) {
        transformNode.animations = [];
        root.animations.forEach(animation => {
          transformNode.animations.push(animation.clone());
        })
      }
    }
    this.setupAnimations(root, clonedRoot, allowedAnimationUniqueIds, renderObject);
    this.setupParticleSystems(clonedRoot, renderObject);

    root.getChildren().forEach((child) => {
      if (child instanceof InstancedMesh) {
        const instancedMesh = <InstancedMesh>child;
        const clonedSource = sourceMap.get(instancedMesh.sourceMesh.id);
        if (clonedSource) {
          const clonedMesh = clonedSource.clone(instancedMesh.name, null, true); // doNotCloneChildren — children are handled by recursive deepCloneNode below
          clonedMesh.metadata = {};
          clonedMesh.setParent(clonedRoot);
          clonedMesh.position.copyFrom(instancedMesh.position);
          clonedMesh.rotation.copyFrom(instancedMesh.rotation);
          if (instancedMesh.rotationQuaternion) {
            clonedMesh.rotationQuaternion = instancedMesh.rotationQuaternion.clone();
          }
          clonedMesh.scaling.copyFrom(instancedMesh.scaling);
          clonedMesh.setPivotMatrix(instancedMesh.getPivotMatrix())
          clonedMesh.receiveShadows = true;
          clonedMesh.hasVertexAlpha = false;
          gltfHelper.handleMaterial(clonedMesh, diplomacy);
          if (instancedMesh.animations && instancedMesh.animations.length) {
            clonedMesh.animations = [];
            instancedMesh.animations.forEach(animation => {
              clonedMesh.animations.push(animation.clone());
            })
          }
          this.setupAnimations(instancedMesh, clonedMesh, allowedAnimationUniqueIds, renderObject);
          if (clonedMesh && instancedMesh.name) {
            nodeByName.set(instancedMesh.name, clonedMesh);
          }
          this.setupParticleSystems(clonedMesh, renderObject);
          // Recursively clone children of the InstancedMesh (e.g. Detail meshes parented to a
          // Column). Without this, child nodes added under instanced meshes in the GLB are lost.
          instancedMesh.getChildren().forEach((grandchild) => {
            this.deepCloneNode(grandchild, clonedMesh, sourceMap, nodeByName, gltfHelper, allowedAnimationUniqueIds, renderObject, diplomacy);
          });
        } else {
          const clonedMesh = instancedMesh.sourceMesh.clone(instancedMesh.name, null, true); // doNotCloneChildren — children are handled by recursive deepCloneNode below
          clonedMesh.metadata = {};
          clonedMesh.setParent(clonedRoot);
          clonedMesh.position.copyFrom(instancedMesh.position);
          clonedMesh.rotation.copyFrom(instancedMesh.rotation);
          if (instancedMesh.rotationQuaternion) {
            clonedMesh.rotationQuaternion = instancedMesh.rotationQuaternion.clone();
          }
          clonedMesh.scaling.copyFrom(instancedMesh.scaling);
          clonedMesh.setPivotMatrix(instancedMesh.getPivotMatrix())
          clonedMesh.receiveShadows = true;
          clonedMesh.hasVertexAlpha = false;
          gltfHelper.handleMaterial(clonedMesh, diplomacy);
          if (instancedMesh.animations && instancedMesh.animations.length) {
            clonedMesh.animations = [];
            instancedMesh.animations.forEach(animation => {
              clonedMesh.animations.push(animation.clone());
            })
          }
          this.setupAnimations(instancedMesh, clonedMesh, allowedAnimationUniqueIds, renderObject);
          if (clonedMesh && instancedMesh.name) {
            nodeByName.set(instancedMesh.name, clonedMesh);
          }
          this.setupParticleSystems(clonedMesh, renderObject);
          // Recursively clone children of the InstancedMesh (same as above for the else branch).
          instancedMesh.getChildren().forEach((grandchild) => {
            this.deepCloneNode(grandchild, clonedMesh, sourceMap, nodeByName, gltfHelper, allowedAnimationUniqueIds, renderObject, diplomacy);
          });
        }
      } else {
        this.deepCloneNode(child, clonedRoot, sourceMap, nodeByName, gltfHelper, allowedAnimationUniqueIds, renderObject, diplomacy);
      }
    })

    return <TransformNode>clonedRoot;
  }

  private setupAnimations(original: Node | null, created: Node | null, allowedAnimationUniqueIds: Set<number>, renderObject: RenderObject) {
    if (!original || !created) {
      return;
    }
    if (original?.animations && original?.animations.length
      && created?.animations && created?.animations.length) {
      original.animations.forEach((originalAnimation) => {
        if (allowedAnimationUniqueIds.has(originalAnimation.uniqueId)) {
          let createdAnimation = created.animations.find(c => c.name == originalAnimation.name);
          if (createdAnimation) {
            renderObject.addEffectAnimation(original, created);
          }
        }
      })
    }
  }

  private setupParticleSystems(node: Node | null, renderObject: RenderObject) {
    if (!node) {
      return;
    }
    if (node instanceof AbstractMesh) {
      const abstractMesh = (<AbstractMesh>node);
      if (abstractMesh.name.startsWith(BabylonModelService.RAZ_P_)) {
        try {
          const particleSystemEntityId = parseInt(abstractMesh.name.replace(BabylonModelService.RAZ_P_, ""), 10);
          let particleSystemEntity = this.getParticleSystemEntity(particleSystemEntityId);
          abstractMesh.isVisible = false;
          renderObject.addEffectParticleSystem(particleSystemEntity, abstractMesh);
        } catch (exception) {
          console.error(exception);
        }
      } else if (abstractMesh.name.startsWith(BabylonModelService.RAZ_M_P_)) {
        try {
          const particleSystemEntityId = parseInt(abstractMesh.name.replace(BabylonModelService.RAZ_M_P_, ""), 10);
          let particleSystemEntity = this.getParticleSystemEntity(particleSystemEntityId);
          abstractMesh.isVisible = false;
          renderObject.setMuzzleFlash(particleSystemEntity, abstractMesh);
        } catch (exception) {
          console.error(exception);
        }
      }

      if (abstractMesh.name.startsWith(BabylonModelService.RAZ_MUZZLE)) {
        abstractMesh.isVisible = false;
        renderObject.setMuzzleMesh(abstractMesh);
      }

      if (abstractMesh.name.startsWith(BabylonModelService.RAZ_TURRET_)) {
        renderObject.setTurretMesh(abstractMesh);
      }
    }
  }

  public static findChildNode(node: Node, namePath: string[]): Node | null {
    if (namePath.length == 0) {
      throw new Error("Empty namePath array is not allowed")
    }

    if (node.id === namePath[0]) {
      if (namePath.length == 1) {
        return node;
      }
      let childNamePath = namePath.slice(1);
      for (let childMesh of node.getChildren()) {
        let found = this.findChildNode(<Mesh>childMesh, childNamePath);
        if (found) {
          return found;
        }
      }
    }
    return null;
  }

  getBabylonMaterial(babylonMaterialId: number | null, diplomacy?: Diplomacy): Material {
    if (babylonMaterialId === null) {
      throw new Error(`getBabylonMaterial(): babylonMaterialId undefined`);
    }
    babylonMaterialId = GwtHelper.gwtIssueNumber(babylonMaterialId);

    let material: Material = <NodeMaterial>this.babylonMaterialContainer.getBabylonModel(babylonMaterialId);
    if (!material) {
      console.error(`No material for babylonMaterialId '${babylonMaterialId}'`);
      material = this.createMissingBabylonMaterial(babylonMaterialId);
    }

    return material;
  }

  private createMissingBabylonMaterial(babylonMaterialId: number): Material {
    let material = new SimpleMaterial(`Missin material '${babylonMaterialId}'`, this.scene);
    material.diffuseColor = new Color3(1, 0, 0);
    return material;
  }

  getParticleSystemEntity(id: number): ParticleSystemEntity {
    id = GwtHelper.gwtIssueNumber(id);

    let particleSystemEntity = this.particleSystemContainer.getEntity(id);
    if (particleSystemEntity) {
      return particleSystemEntity;
    }

    throw new Error(`No ParticleSystemEntity for '${id}'`);
  }

  getNodeParticleSystemSet(particleSystemEntityId: number): NodeParticleSystemSet | null {
    return this.particleSystemContainer.getBabylonModel(particleSystemEntityId) ?? null;
  }

  private setupModel3DEntities(model3DEntities: Model3DEntity[]) {
    this.model3DEntities.clear();
    if (model3DEntities) {
      model3DEntities.forEach(model3DEntity => {
        this.model3DEntities.set(model3DEntity.id, model3DEntity);
      });
    }
  }
}


