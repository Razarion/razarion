import {Injectable} from "@angular/core";
import {URL_THREE_JS_MODEL, URL_THREE_JS_MODEL_EDITOR} from "src/app/common";
import {Diplomacy, ThreeJsModelConfig} from "src/app/gwtangular/GwtAngularFacade";
import {GwtHelper} from "../../gwtangular/GwtHelper";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {
  AssetContainer,
  Color3,
  IInspectable,
  InspectableType,
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
import Type = ThreeJsModelConfig.Type;

@Injectable()
export class BabylonModelService {
  private assetContainers: Map<number, AssetContainer> = new Map();
  private nodeMaterials: Map<number, NodeMaterial> = new Map();
  private babylonMaterialContainer = new BabylonMaterialContainer();
  private glbContainer = new GlbContainer(this.babylonMaterialContainer);
  private particleSystemContainer = new ParticleSystemContainer();
  private model3DEntities: Map<number, Model3DEntity> = new Map();
  private scene!: Scene;
  private threeJsModelConfigs!: ThreeJsModelConfig[];
  private threeJsModelConfigMap: Map<number, ThreeJsModelConfig> = new Map();
  private gwtResolver?: () => void;

  constructor(private uiConfigCollectionService: UiConfigCollectionService,
              private httpClient: HttpClient,
              private messageService: MessageService) {
    SceneLoader.RegisterPlugin(new GLTFFileLoader());
    this.loadUiConfigCollection();
    this.babylonMaterialContainer.setHttpClient(httpClient);
    this.particleSystemContainer.setHttpClient(httpClient);
  }

  setScene(scene: Scene) {
    this.scene = scene;
  }

  init(threeJsModelConfigs: ThreeJsModelConfig[]): Promise<void> {
    this.threeJsModelConfigs = threeJsModelConfigs.filter(threeJsModelConfig => !threeJsModelConfig.isDisabled());

    this.threeJsModelConfigs.forEach(threeJsModelConfig => this.threeJsModelConfigMap.set(threeJsModelConfig.getId(), threeJsModelConfig))

    return new Promise<void>((resolve, reject) => {
      try {
        let loadingCount = this.threeJsModelConfigs.length;

        let handleResolve = () => {
          loadingCount--;
          if (loadingCount === 0) {
            this.handleResolve(resolve)
          }
        }

        this.threeJsModelConfigs.forEach(threeJsModelConfig => {
          try {
            const url = `${URL_THREE_JS_MODEL}/${threeJsModelConfig.getId()}`;
            switch (GwtHelper.gwtIssueStringEnum(threeJsModelConfig.getType(), ThreeJsModelConfig.Type)) {
              case ThreeJsModelConfig.Type.GLTF:
                this.loadAssetContainer(url, threeJsModelConfig, handleResolve);
                break;
              case ThreeJsModelConfig.Type.NODES_MATERIAL:
                this.loadNodeMaterial(url, threeJsModelConfig, handleResolve);
                break;
              case ThreeJsModelConfig.Type.PARTICLE_SYSTEM_JSON:
                handleResolve();
                break;
              default:
                console.warn(`Unknown type '${threeJsModelConfig.getType()}' in ThreeJsModelConfig (${threeJsModelConfig.getInternalName()}[${threeJsModelConfig.getId()}])`);
                handleResolve();
                break;
            }
          } catch (error) {
            console.warn(`Error in ThreeJsModelConfig (${threeJsModelConfig.getInternalName()}[${threeJsModelConfig.getId()}]) ${error}`);
            handleResolve();
          }
        });
      } catch (error) {
        console.error(error);
        this.handleResolve(() => {
          reject(error)
        })
      }
    });
  }

  private handleResolve(handler: () => void) {
    if (this.babylonMaterialContainer.isLoaded() && this.glbContainer.isLoaded() && this.particleSystemContainer.isLoaded()) {
      console.info(`Gwt handleResolve now`)
      handler();
    } else {
      console.info(`Gwt handleResolve later`)
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
      console.info(`UiConfigCollection loaded`)
      if (this.gwtResolver) {
        console.info(`UiConfigCollection calls this.gwtResolver()`)
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
        }
      } else {
        this.deepCloneNode(child, clonedRoot, sourceMap, gltfHelper, diplomacy);
      }
    })

    return <TransformNode>clonedRoot;
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

  private loadAssetContainer(url: string, threeJsModelConfig: ThreeJsModelConfig, handleResolve: () => void) {
    try {
      let hasError = false;
      const result = SceneLoader.LoadAssetContainer(url, '', this.scene, assetContainer => {
          if (!hasError) {
            this.assetContainers.set(threeJsModelConfig.getId(), assetContainer);
            handleResolve();
          }
        },
        () => {
        },
        (scene: Scene, message: string, exception?: any) => {
          hasError = true;
          console.error(`Error loading Babylon Asset '${url}'. exception: '${exception}'`);
          handleResolve();
        }, ".glb")
      if (result === null) {
        console.error("LoadAssetContainer failed");
        handleResolve();
      }
    } catch (e) {
      console.error(`Exception loading Babylon Asset ${url}`);
      console.error(e);
      handleResolve();
    }
  }

  private loadNodeMaterial(url: string, threeJsModelConfig: ThreeJsModelConfig, handleResolve: () => void) {
    NodeMaterial.ParseFromFileAsync(
      `${threeJsModelConfig.getInternalName()}(${threeJsModelConfig.getId()})`,
      url,
      this.scene
    ).then(nodeMaterial => {
      nodeMaterial.inspectableCustomProperties = this.setupEditorProperties(threeJsModelConfig, nodeMaterial);
      nodeMaterial.ignoreAlpha = true;
      this.nodeMaterials.set(threeJsModelConfig.getId(), nodeMaterial);
      handleResolve();
    }).catch(reason => {
      console.error(`Load NodeMaterial failed. Node Material: '${threeJsModelConfig.getInternalName()} (${threeJsModelConfig.getId()})' Reason: ${reason}`);
      let nodeMaterial = new NodeMaterial(`FAILED '${threeJsModelConfig.getInternalName()} (${threeJsModelConfig.getId()})'`)
      nodeMaterial.inspectableCustomProperties = this.setupEditorProperties(threeJsModelConfig, nodeMaterial);
      this.nodeMaterials.set(threeJsModelConfig.getId(), nodeMaterial);
      handleResolve();
    })
  }

  getNodeMaterial(babylonModelId: number | null): NodeMaterial {
    if (babylonModelId === null) {
      throw new Error(`getNodeMaterial(): babylonModelId undefined`);
    }
    babylonModelId = GwtHelper.gwtIssueNumber(babylonModelId);

    let nodeMaterial: NodeMaterial = <NodeMaterial>this.nodeMaterials.get(babylonModelId);
    if (!nodeMaterial) {
      console.error(`No NodeMaterial for babylonModelId '${babylonModelId}'`);
      nodeMaterial = this.createMissingNodeMaterial(babylonModelId);
    }

    return nodeMaterial;
  }

  private createMissingNodeMaterial(babylonModelId: number): NodeMaterial {
    const material = NodeMaterial.CreateDefault(`_Missing NodeMaterial ${babylonModelId}`);
    material.backFaceCulling = false; // Camera looking in negative z direction. https://doc.babylonjs.com/features/featuresDeepDive/mesh/creation/custom/custom#visibility
    material.inspectableCustomProperties = this.setupEditorProperties(new class implements ThreeJsModelConfig {
      getId(): number {
        return babylonModelId;
      }

      getInternalName(): string {
        return "_Missing NodeMaterial";
      }

      getType(): ThreeJsModelConfig.Type {
        return Type.NODES_MATERIAL;
      }

      getNodeMaterialId(): number | null {
        return null;
      }

      isDisabled(): boolean {
        return false;
      }

    }, material);
    return material;
  }


  private setupEditorProperties(threeJsModelConfig: ThreeJsModelConfig, nodeMaterial: NodeMaterial): IInspectable[] {
    return [
      {
        label: `Save to Razarion '${threeJsModelConfig.getInternalName()}(${threeJsModelConfig.getId()}')`,
        propertyName: "dummy",
        callback: () => {
          const json = this.serializeNodeMaterial(nodeMaterial);
          this.babylonModelUpload(threeJsModelConfig.getId(), new Blob([json], {type: 'application/json'}));
        },
        type: InspectableType.Button
      }
    ];
  }

  private serializeNodeMaterial(material: NodeMaterial): string {
    // See Babylon.js code
    // packages/tools/nodeEditor/src/serializationTools.ts Serialize

    // const bufferSerializationState = Texture.SerializeBuffers;
    // Texture.SerializeBuffers = DataStorage.ReadBoolean("EmbedTextures", true);

    // this.UpdateLocations(material, globalState, frame);

    const serializationObject = material.serialize();

    // Texture.SerializeBuffers = bufferSerializationState;

    return JSON.stringify(serializationObject, undefined, 2);
  }


  private babylonModelUpload(babylonModelId: number, blob: Blob) {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/octet-stream'
      })
    };
    this.httpClient.put(`${URL_THREE_JS_MODEL_EDITOR}/upload/${babylonModelId}`, blob, httpOptions)
      .subscribe({
        complete: () => this.messageService.add({
          severity: 'success',
          life: 300,
          summary: 'Save successful'
        }),
        error: (error: any) => {
          console.error(error);
          this.messageService.add({
            severity: 'error',
            summary: `${error.name}: ${error.status}`,
            detail: `${error.statusText}`,
            sticky: true
          });
        }
      })
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


