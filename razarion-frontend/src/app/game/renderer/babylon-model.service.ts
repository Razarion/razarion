import {Injectable} from "@angular/core";
import {Diplomacy} from "src/app/gwtangular/GwtAngularFacade";
import {GwtHelper} from "../../gwtangular/GwtHelper";
import {HttpClient} from "@angular/common/http";
import {
  Color3,
  InstancedMesh,
  Material,
  Mesh,
  Node,
  NodeMaterial,
  Scene,
  SceneLoader,
  TransformNode
} from "@babylonjs/core";
import {GLTFFileLoader} from "@babylonjs/loaders";
import {Model3DEntity, ParticleSystemEntity} from "src/app/generated/razarion-share";
import {SimpleMaterial} from "@babylonjs/materials";
import {UiConfigCollectionService} from "../ui-config-collection.service";
import {BabylonMaterialContainer, GlbContainer, ParticleSystemContainer} from "./babylon-model-container";
import {GltfHelper} from "./gltf-helper";

@Injectable({
  providedIn: 'root'
})
export class BabylonModelService {
  private babylonMaterialContainer = new BabylonMaterialContainer();
  private glbContainer = new GlbContainer(this.babylonMaterialContainer);
  private particleSystemContainer = new ParticleSystemContainer();
  private model3DEntities: Map<number, Model3DEntity> = new Map();
  private scene!: Scene;
  private gwtResolver?: () => void;

  constructor(private uiConfigCollectionService: UiConfigCollectionService,
              httpClient: HttpClient) {
    SceneLoader.RegisterPlugin(new GLTFFileLoader());
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

  cloneModel3D(model3DId: number, parent: Node | null, diplomacy?: Diplomacy): TransformNode {
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

    let node = assetContainer
      .getNodes()
      .find(childNode => childNode.name === model3DEntity.gltfName);

    if (!node) {
      throw new Error(`Node withe name "${model3DEntity.gltfName}" from model3DId '${model3DId}' not found'`);
    }

    const sourceMap = new Map<string, Mesh>();
    let transformNode = this.deepCloneNode(node, parent, sourceMap, gltfHelper, diplomacy);
    transformNode.position.x = 0.0;
    transformNode.position.z = 0.0;
    return transformNode;
  }

  private deepCloneNode(root: Node, parent: Node | null, sourceMap: Map<string, Mesh>, gltfHelper: GltfHelper, diplomacy?: Diplomacy): TransformNode {
    let clonedRoot = root.clone(root.name, parent, true);
    this.startAnimations(clonedRoot);
    sourceMap.set(root.id, <Mesh>clonedRoot);
    if (clonedRoot instanceof Mesh) {
      const mesh = <Mesh>clonedRoot;
      mesh.receiveShadows = true;
      mesh.hasVertexAlpha = false;
      gltfHelper.handleMaterial(mesh, diplomacy);
    }

    root.getChildren().forEach(child => {
      if (child instanceof InstancedMesh) {
        const instancedMesh = <InstancedMesh>child;
        const clonedSource = sourceMap.get(instancedMesh.sourceMesh.id);
        if (clonedSource) {
          const clonedMesh = clonedSource.clone(instancedMesh.name); // Instance does not work
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
          this.startAnimations(clonedMesh);
        } else {
          const clonedMesh = instancedMesh.sourceMesh.clone(instancedMesh.name); // Instance does not work
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
          this.startAnimations(clonedMesh);
        }
      } else {
        this.deepCloneNode(child, clonedRoot, sourceMap, gltfHelper, diplomacy);
      }
    })

    return <TransformNode>clonedRoot;
  }

  private startAnimations(node: Node | null) {
    if (!node) {
      return;
    }
    if (node?.animations) {
      node.animations.forEach((animation) => {
        node.getScene().beginAnimation(node, 0, 60, true)
      })
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

  getParticleSystemJson(particleSystemEntityId: number): any {
    particleSystemEntityId = GwtHelper.gwtIssueNumber(particleSystemEntityId);
    let json = this.particleSystemContainer.getBabylonModel(particleSystemEntityId);
    if (json) {
      return json;
    }

    throw new Error(`No ParticleSystem json for ParticleSystemEntity ('${particleSystemEntityId}') JSON found`);
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


