import {Injectable} from "@angular/core";
import {URL_GLTF, URL_THREE_JS_MODEL, URL_THREE_JS_MODEL_EDITOR} from "src/app/common";
import {Diplomacy, ParticleSystemConfig, ThreeJsModelConfig} from "src/app/gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {GwtHelper} from "../../gwtangular/GwtHelper";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {
  AssetContainer,
  BaseTexture,
  Color3,
  IInspectable,
  InputBlock,
  InspectableType,
  InstancedMesh,
  Material,
  Mesh,
  Node,
  NodeMaterial,
  Nullable,
  ParticleSystem,
  PBRMaterial,
  Scene,
  SceneLoader,
  TransformNode
} from "@babylonjs/core";
import {GLTFFileLoader} from "@babylonjs/loaders";
import JSZip from "jszip";
import {
  BabylonMaterialControllerClient,
  BabylonMaterialEntity,
  GltfEntity,
  Model3DEntity
} from "src/app/generated/razarion-share";
import {TypescriptGenerator} from "src/app/backend/typescript-generator";
import {SimpleMaterial} from "@babylonjs/materials";
import {UiConfigCollectionService} from "../ui-config-collection.service";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import Type = ThreeJsModelConfig.Type;

@Injectable()
export class BabylonModelService {
  private assetContainers: Map<number, AssetContainer> = new Map();
  private nodeMaterials: Map<number, NodeMaterial> = new Map();
  private babylonMaterials: Map<number, Material> = new Map();
  babylonMaterialEntities: Map<number, BabylonMaterialEntity> = new Map();
  private gltfHelpers: Map<number, GltfHelper> = new Map();
  private glbAssetContainers: Map<number, AssetContainer> = new Map();
  private model3DEntities: Map<number, Model3DEntity> = new Map();
  private gwtAngularService!: GwtAngularService;
  private scene!: Scene;
  private threeJsModelConfigs!: ThreeJsModelConfig[];
  private threeJsModelConfigMap: Map<number, ThreeJsModelConfig> = new Map();
  private particleSystemConfigs: Map<number, ParticleSystemConfig> = new Map();
  private particleSystemJson: Map<number, any> = new Map();
  private babylonMaterialsLoaded = false;
  private gltfsLoaded = false;
  private gwtResolver?: () => void;
  private babylonMaterialControllerClient: BabylonMaterialControllerClient;
  diplomacyMaterialCache: Map<number, Map<Diplomacy, Map<string, NodeMaterial>>> = new Map<number, Map<Diplomacy, Map<string, NodeMaterial>>>();

  constructor(private uiConfigCollectionService: UiConfigCollectionService,
              private httpClient: HttpClient,
              private messageService: MessageService) {
    SceneLoader.RegisterPlugin(new GLTFFileLoader());
    this.babylonMaterialControllerClient = new BabylonMaterialControllerClient(TypescriptGenerator.generateHttpClientAdapter(this.httpClient));
    this.loadUiConfigCollection();
  }

  init(threeJsModelConfigs: ThreeJsModelConfig[], particleSystemConfigs: ParticleSystemConfig[], gwtAngularService: GwtAngularService): Promise<void> {
    this.threeJsModelConfigs = threeJsModelConfigs.filter(threeJsModelConfig => !threeJsModelConfig.isDisabled());
    this.particleSystemConfigs = new Map(particleSystemConfigs.map(p => [p.getId(), p]));
    this.gwtAngularService = gwtAngularService;

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
                this.loadParticleSystem(url, threeJsModelConfig, handleResolve);
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
    if (this.babylonMaterialsLoaded && this.gltfsLoaded) {
      handler();
    } else {
      this.gwtResolver = handler;
    }
  }

  private loadUiConfigCollection() {
    this.uiConfigCollectionService.getUiConfigCollection().then(uiConfigCollection => {
      this.handleBabylonMaterials(uiConfigCollection.babylonMaterials);
      this.setupModel3DEntities(uiConfigCollection.model3DEntities);
      this.handleAndLoadGltfs(uiConfigCollection.gltfs);
    });
  }

  private handleBabylonMaterials(babylonMaterialEntities: BabylonMaterialEntity[]) {
    this.babylonMaterialEntities.clear();
    if (babylonMaterialEntities.length === 0) {
      this.babylonMaterialsLoaded = true;
      this.handleLoaded();
      return;
    }

    const materialLoadingControl = {
      loadingCount: babylonMaterialEntities.length
    }

    babylonMaterialEntities.forEach(babylonMaterialEntity => {
      this.babylonMaterialEntities.set(babylonMaterialEntity.id, babylonMaterialEntity);
      this.loadMaterial(babylonMaterialEntity, materialLoadingControl);
    });
  }

  private loadMaterial(babylonMaterialEntity: BabylonMaterialEntity, materialLoadingControl: { loadingCount: number }) {
    this.babylonMaterialControllerClient.getData(babylonMaterialEntity.id)
      .then(data => {
        try {
          let material;
          if (babylonMaterialEntity.nodeMaterial) {
            material = NodeMaterial.Parse(data, this.scene, "/rest/images/");
          } else {
            material = Material.Parse(data, this.scene, "/rest/images/");
          }
          if (material) {
            this.babylonMaterials.set(babylonMaterialEntity.id, material);
          } else {
            console.error(`Error parsing material`);
          }
          this.handleMaterialLoaded(materialLoadingControl);
        } catch (e) {
          console.error(e);
          console.error(`Error parsing material '${e}'`);
          this.handleMaterialLoaded(materialLoadingControl);
        }
      })
      .catch(err => {
        console.error(`Error loading Babylon file '${err}'`);
        this.handleMaterialLoaded(materialLoadingControl);
      })
  }

  private handleLoaded() {
    if (this.gwtResolver) {
      this.gwtResolver();
    }
  }

  private handleMaterialLoaded(materialLoadingControl: { loadingCount: number }) {
    materialLoadingControl.loadingCount--;
    if (materialLoadingControl.loadingCount <= 0) {
      this.babylonMaterialsLoaded = true;
      this.handleLoaded();
    }
  }

  private handleGltfGlbLoaded(gltfLoadingControl: { loadingCount: number }) {
    gltfLoadingControl.loadingCount--;
    if (gltfLoadingControl.loadingCount <= 0) {
      this.gltfsLoaded = true;
      this.handleLoaded();
    }
  }

  private handleAndLoadGltfs(gltfs: GltfEntity[]) {
    this.gltfHelpers.clear();
    if (!gltfs || gltfs.length === 0) {
      this.gltfsLoaded = true;
      this.handleLoaded();
      return;
    }

    const gltfLoadingControl = {
      loadingCount: gltfs.length
    }

    gltfs.forEach(gltf => {
      const gltfHelper = new GltfHelper(gltf, this);
      this.gltfHelpers.set(gltf.id, gltfHelper);
      this.loadGltfGlb(gltf, gltfHelper, gltfLoadingControl);
    });
  }

  private loadGltfGlb(gltf: GltfEntity, gltfHelper: GltfHelper, gltfLoadingControl: { loadingCount: number }) {
    const url = `${URL_GLTF}/glb/${gltf.id}`;
    try {
      let hasError = false;
      const result = SceneLoader.LoadAssetContainer(url, '', this.scene, assetContainer => {
          if (!hasError) {
            this.glbAssetContainers.set(gltf.id!, assetContainer);
            this.assignGlbTextures(gltf, assetContainer, gltfHelper);
            this.handleGltfGlbLoaded(gltfLoadingControl);
          }
        },
        () => {
        },
        (scene: Scene, message: string, exception?: any) => {
          hasError = true;
          console.error(`Error loading glTF/glb '${url}'. exception: '${exception}'`);
          this.handleGltfGlbLoaded(gltfLoadingControl);
        }, ".glb")
      if (result === null) {
        console.error(`Error loading glTF/glb '${url}'`);
        this.handleGltfGlbLoaded(gltfLoadingControl);
      }
    } catch (e) {
      console.error(`Error loading glTF/glb '${url}'`);
      console.error(e);
      this.handleGltfGlbLoaded(gltfLoadingControl);
    }
  }

  cloneModel3D(model3DId: number, parent: Node | null, diplomacy?: Diplomacy): TransformNode {
    model3DId = GwtHelper.gwtIssueNumber(model3DId);

    let model3DEntity = this.model3DEntities.get(model3DId);
    if (!model3DEntity) {
      throw new Error(`No Model3DEntity for ${model3DId}`);
    }
    let gltfHelper = this.gltfHelpers.get(model3DEntity.gltfEntityId);
    if (!gltfHelper) {
      throw new Error(`No GltfHelper for gltfEntityId ${model3DEntity.gltfEntityId} for model3DId ${model3DId}`);
    }

    let assetContainer = this.glbAssetContainers.get(model3DEntity.gltfEntityId);
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

  cloneMesh(threeJsModelPackConfigId: number, parent: Node | null): TransformNode {
    const threeJsModelPackConf = this.gwtAngularService.gwtAngularFacade.threeJsModelPackService.getThreeJsModelPackConfig(threeJsModelPackConfigId);

    const assetContainer: AssetContainer = this.getAssetContainer(threeJsModelPackConf.getThreeJsModelId());

    let childNode = null;

    for (let childNod of assetContainer.getNodes()) {
      childNode = BabylonModelService.findChildNode(childNod, threeJsModelPackConf.toNamePathAsArray());
      if (childNode) {
        break;
      }
    }

    if (childNode == null) {
      throw new Error(`No Mesh for threeJsModelPackConfigId '${threeJsModelPackConfigId}'. Three.js Path  '${threeJsModelPackConf.toNamePathAsArray()}'`);
    }

    if (typeof (<any>childNode).clone !== 'function') {
      throw new Error(`childNode can not be cloned "${childNode}" typeof childNode = "${typeof childNode}". threeJsModelPackConfigId '${threeJsModelPackConfigId}'. Three.js Path  '${threeJsModelPackConf.toNamePathAsArray()}'`);
    }

    const mesh = (<any>childNode).clone("", parent);

    if (mesh instanceof Mesh) {
      (<Mesh>mesh).receiveShadows = true
    }
    mesh.getChildren().forEach((m: any) => {
      if (m instanceof Mesh) {
        (<Mesh>m).receiveShadows = true
      }
    });


    if (threeJsModelPackConf.getPosition()) {
      mesh.position.set(threeJsModelPackConf.getPosition().getX(),
        threeJsModelPackConf.getPosition().getZ(),
        threeJsModelPackConf.getPosition().getY());
    }
    if (threeJsModelPackConf.getScale()) {
      mesh.scaling.set(threeJsModelPackConf.getScale().getX(),
        threeJsModelPackConf.getScale().getZ(),
        threeJsModelPackConf.getScale().getY());
    }
    if (threeJsModelPackConf.getRotation()) {
      mesh.rotationQuaternion = null;
      mesh.rotation.set(threeJsModelPackConf.getRotation().getX(),
        threeJsModelPackConf.getRotation().getZ(),
        threeJsModelPackConf.getRotation().getY());
    }
    return mesh;
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

  private loadParticleSystem(url: string, threeJsModelConfig: ThreeJsModelConfig, handleResolve: () => void) {
    this.httpClient.get(url).subscribe({
      next: (json) => {
        this.particleSystemJson.set(threeJsModelConfig.getId(), json);
        handleResolve();
      },
      error: (error: any) => {
        console.error(`Load Particle System failed. '${threeJsModelConfig.getInternalName()} (${threeJsModelConfig.getId()})' Reason: ${error}`);
        handleResolve();
      }
    })
  }

  getAssetContainer(threeJsModelId: number): AssetContainer {
    if (threeJsModelId === undefined) {
      throw new Error(`ThreeJsModel id undefined`);
    }

    threeJsModelId = GwtHelper.gwtIssueNumber(threeJsModelId);

    let assetContainer = this.assetContainers.get(threeJsModelId);

    if (!assetContainer) {
      throw new Error(`No AssetContainers for threeJsModelId '${threeJsModelId}'`);
    }

    return assetContainer;
  }

  setScene(scene: Scene) {
    this.scene = scene;
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

    let material: Material = <NodeMaterial>this.babylonMaterials.get(babylonMaterialId);
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

  dumpAll(): Promise<JSZip> {
    return new Promise<JSZip>((resolve) => {
      const zip = new JSZip();
      let pending = this.threeJsModelConfigs.length;
      this.threeJsModelConfigs.forEach(babylonModelConfig => {
        this.httpClient.get(`${URL_THREE_JS_MODEL}/${babylonModelConfig.getId()}`,
          {responseType: 'blob'})
          .subscribe({
            next(blob) {
              zip.file(`id_${babylonModelConfig.getId()}`, blob);
              pending--;
              if (pending == 0) {
                resolve(zip);
              }
            },
            error: (error: any) => {
              console.error(error);
              this.messageService.add({
                severity: 'error',
                summary: `${error.name}: ${error.status}`,
                detail: `Error download ${babylonModelConfig.getId()} (${error.statusText})`,
                sticky: true
              });
              pending--;
              if (pending == 0) {
                resolve(zip);
              }
            }
          })
      });
    });

  }

  getThreeJsModelConfig(id: number): ThreeJsModelConfig {
    id = GwtHelper.gwtIssueNumber(id);

    let threeJsModelConfig = this.threeJsModelConfigMap.get(id);
    if (threeJsModelConfig) {
      return threeJsModelConfig;
    }

    throw new Error(`No ThreeJsModelConfig for '${id}'`);
  }

  getParticleSystemConfig(id: number): ParticleSystemConfig {
    id = GwtHelper.gwtIssueNumber(id);

    let particleSystemConfig = this.particleSystemConfigs.get(id);
    if (particleSystemConfig) {
      return particleSystemConfig;
    }

    throw new Error(`No ParticleSystemConfig for '${id}'`);
  }

  getParticleSystemJson(id: number): any {
    id = GwtHelper.gwtIssueNumber(id);
    let json = this.particleSystemJson.get(id);
    if (json) {
      return json;
    }

    throw new Error(`No ParticleSystemJson.threeJsModelConfig('${id}') JSON found`);
  }

  updateParticleSystemJson(babylonModelId: number, particleSystem: ParticleSystem) {
    const json = JSON.stringify(particleSystem!.serialize());
    this.babylonModelUpload(babylonModelId, new Blob([json], {type: 'application/json'}));
  }

  private setupModel3DEntities(model3DEntities: Model3DEntity[]) {
    this.model3DEntities.clear();
    if (model3DEntities) {
      model3DEntities.forEach(model3DEntity => {
        this.model3DEntities.set(model3DEntity.id, model3DEntity);
      });
    }
  }

  private assignGlbTextures(gltf: GltfEntity, assetContainer: AssetContainer, gltfHelper: GltfHelper) {
    Object.keys(gltf.materialGltfNames).forEach((gltfMaterialName: string) => {
      let materialId = gltf.materialGltfNames[gltfMaterialName];
      let babylonMaterialEntity = this.babylonMaterialEntities.get(materialId)!;
      if (babylonMaterialEntity.overrideAlbedoTextureNode
        || babylonMaterialEntity.overrideMetallicTextureNode
        || babylonMaterialEntity.overrideBumpTextureNode
        || babylonMaterialEntity.overrideAmbientOcclusionTextureNode) {
        let glbMaterial = <PBRMaterial>assetContainer.materials.find(material => material.name === gltfMaterialName);
        if (glbMaterial) {
          gltfHelper.assignTextures(babylonMaterialEntity, glbMaterial);
        } else {
          console.warn(`No material in AssetContainer ${gltfMaterialName}`)
        }
      }
    });
  }
}

class GltfTextures {
  constructor(public albedoTexture: Nullable<BaseTexture>,
              private metallicTexture: Nullable<BaseTexture>,
              private bumpTexture: Nullable<BaseTexture>,
              private ambientOcclusionTexture: Nullable<BaseTexture>,
              private babylonMaterialEntity: BabylonMaterialEntity) {
  }

  overrideTexture(nodeMaterial: NodeMaterial) {
    if (this.albedoTexture) {
      (<any>nodeMaterial.getBlockByName(this.babylonMaterialEntity.overrideAlbedoTextureNode)).texture = this.albedoTexture;
    }
    if (this.metallicTexture) {
      (<any>nodeMaterial.getBlockByName(this.babylonMaterialEntity.overrideMetallicTextureNode)).texture = this.metallicTexture;
    }
    if (this.bumpTexture) {
      (<any>nodeMaterial.getBlockByName(this.babylonMaterialEntity.overrideBumpTextureNode)).texture = this.bumpTexture;
    }
    if (this.ambientOcclusionTexture) {
      (<any>nodeMaterial.getBlockByName(this.babylonMaterialEntity.overrideAmbientOcclusionTextureNode)).texture = this.ambientOcclusionTexture;
      (<any>nodeMaterial.getBlockByName("ambientOcclusionEnable")).value = 1;
    }
  }
}

class GltfHelper {
  private materialIds: Map<string, number> = new Map();
  private gltfTexturesMap: Map<string, GltfTextures> = new Map();

  constructor(gltf: GltfEntity,
              private babylonModelService: BabylonModelService) {
    Object.keys(gltf.materialGltfNames).forEach((gltfName: string) => {
      let materialId = gltf.materialGltfNames[gltfName];
      this.materialIds.set(gltfName, materialId)
    })
  }

  handleMaterial(mesh: Mesh, diplomacy?: Diplomacy) {
    const originalMaterialName = mesh.material!.name;
    const materialId = this.materialIds.get(originalMaterialName);
    if (materialId) {
      let babylonMaterialEntity = this.babylonModelService.babylonMaterialEntities.get(materialId);
      if (diplomacy) {
        if (!babylonMaterialEntity?.diplomacyColorNode) {
          mesh.material = this.babylonModelService.getBabylonMaterial(materialId, diplomacy).clone(mesh.material!.name);
          return
        }

        let diplomacyCache = this.babylonModelService.diplomacyMaterialCache.get(materialId);
        if (!diplomacyCache) {
          diplomacyCache = new Map<Diplomacy, Map<string, NodeMaterial>>();
          this.babylonModelService.diplomacyMaterialCache.set(materialId, diplomacyCache)
        }
        let cachedMaterialNames = diplomacyCache.get(diplomacy);
        if (!cachedMaterialNames) {
          cachedMaterialNames = new Map<string, NodeMaterial>();
          diplomacyCache.set(diplomacy, cachedMaterialNames)
        }
        let cachedMaterial = cachedMaterialNames.get(mesh.material!.name);
        if (!cachedMaterial) {
          cachedMaterial = <NodeMaterial>this.babylonModelService.getBabylonMaterial(materialId).clone(`${mesh.material!.name} ${materialId} '${diplomacy}'`)!;
          cachedMaterial = cachedMaterial.clone(cachedMaterial.name)!
          const diplomacyColorNode = (<NodeMaterial>cachedMaterial).getBlockByPredicate(block => {
            return babylonMaterialEntity.diplomacyColorNode === block.name;
          });
          if (diplomacyColorNode) {
            (<InputBlock>diplomacyColorNode).value = BabylonRenderServiceAccessImpl.color4Diplomacy(diplomacy);
          }
          if (babylonMaterialEntity!.overrideAlbedoTextureNode
            || babylonMaterialEntity!.overrideMetallicTextureNode
            || babylonMaterialEntity!.overrideBumpTextureNode
            || babylonMaterialEntity!.overrideAmbientOcclusionTextureNode) {
            let gltfTextures = this.gltfTexturesMap.get(originalMaterialName);
            gltfTextures && gltfTextures.overrideTexture(<NodeMaterial>cachedMaterial);
          }
          (<NodeMaterial>cachedMaterial).build()
          cachedMaterialNames.set(mesh.material!.name, cachedMaterial);
        }
        mesh.material = cachedMaterial;
      } else {
        mesh.material = this.babylonModelService.getBabylonMaterial(materialId, diplomacy).clone(mesh.material!.name);
      }
    }
  }

  assignTextures(babylonMaterialEntity: BabylonMaterialEntity, glbMaterial: PBRMaterial) {
    let albedoTexture: Nullable<BaseTexture> = null;
    let metallicTexture: Nullable<BaseTexture> = null;
    let bumpTexture: Nullable<BaseTexture> = null;
    let ambientOcclusionTexture: Nullable<BaseTexture> = null;

    if (babylonMaterialEntity.overrideAlbedoTextureNode) {
      albedoTexture = glbMaterial._albedoTexture;
    }
    if (babylonMaterialEntity.overrideMetallicTextureNode) {
      metallicTexture = glbMaterial._metallicTexture;
    }
    if (babylonMaterialEntity.overrideBumpTextureNode) {
      bumpTexture = glbMaterial._bumpTexture;
    }
    if (babylonMaterialEntity.overrideAmbientOcclusionTextureNode) {
      ambientOcclusionTexture = glbMaterial._ambientTexture;
    }
    if (albedoTexture || metallicTexture || bumpTexture) {
      this.gltfTexturesMap.set(glbMaterial.name, new GltfTextures(albedoTexture,
        metallicTexture,
        bumpTexture,
        ambientOcclusionTexture,
        babylonMaterialEntity));
    }
  }
}
