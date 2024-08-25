import { Injectable } from "@angular/core";
import { URL_THREE_JS_MODEL, URL_THREE_JS_MODEL_EDITOR } from "src/app/common";
import { ParticleSystemConfig, ThreeJsModelConfig } from "src/app/gwtangular/GwtAngularFacade";
import { GwtAngularService } from "../../gwtangular/GwtAngularService";
import { GwtHelper } from "../../gwtangular/GwtHelper";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { MessageService } from "primeng/api";
import {
  AssetContainer,
  Color3,
  IInspectable,
  InspectableType,
  Material,
  Mesh,
  Node,
  NodeMaterial,
  ParticleSystem,
  Scene,
  SceneLoader,
  TransformNode
} from "@babylonjs/core";
import { GLTFFileLoader } from "@babylonjs/loaders";
import JSZip from "jszip";
import { BabylonJsUtils } from "./babylon-js.utils";
import Type = ThreeJsModelConfig.Type;
import { BabylonMaterialControllerClient } from "src/app/generated/razarion-share";
import { TypescriptGenerator } from "src/app/backend/typescript-generator";
import { SimpleMaterial } from "@babylonjs/materials";

@Injectable()
export class BabylonModelService {
  private assetContainers: Map<number, AssetContainer> = new Map();
  private nodeMaterials: Map<number, NodeMaterial> = new Map();
  private babylonMaterials: Map<number, Material> = new Map();
  private gwtAngularService!: GwtAngularService;
  private scene!: Scene;
  private threeJsModelConfigs!: ThreeJsModelConfig[];
  private threeJsModelConfigMap: Map<number, ThreeJsModelConfig> = new Map();
  private particleSystemConfigs: Map<number, ParticleSystemConfig> = new Map();
  private particleSystemJson: Map<number, any> = new Map();
  private babylonMaterialsLoaded = false;
  private gwtResolver: any;
  private babylonMaterialControllerClient: BabylonMaterialControllerClient;

  constructor(private httpClient: HttpClient, private messageService: MessageService) {
    SceneLoader.RegisterPlugin(new GLTFFileLoader());
    this.babylonMaterialControllerClient = new BabylonMaterialControllerClient(TypescriptGenerator.generateHttpClientAdapter(this.httpClient));
  }

  init(threeJsModelConfigs: ThreeJsModelConfig[], particleSystemConfigs: ParticleSystemConfig[], gwtAngularService: GwtAngularService): Promise<void> {
    this.threeJsModelConfigs = threeJsModelConfigs.filter(threeJsModelConfig => !threeJsModelConfig.isDisabled());
    this.particleSystemConfigs = new Map(particleSystemConfigs.map(p => [p.getId(), p]));
    this.gwtAngularService = gwtAngularService;

    this.threeJsModelConfigs.forEach(threeJsModelConfig => this.threeJsModelConfigMap.set(threeJsModelConfig.getId(), threeJsModelConfig))

    this.loadBabylonMaterials();

    return new Promise<void>((resolve, reject) => {
      try {
        let loadingCount = this.threeJsModelConfigs.length;

        let handleResolve = () => {
          loadingCount--;
          if (loadingCount === 0) {
            if (this.babylonMaterialsLoaded) {
              resolve();
            } else {
              this.gwtResolver = resolve;
            }
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
        reject(error);
      }
    });
  }

  private loadBabylonMaterials() {
    this.babylonMaterialControllerClient.readAll()
      .then(materials => {
        if (materials.length === 0) {
          this.babylonMaterialsLoaded = true;
          if (this.gwtResolver) {
            this.gwtResolver();
          }
          return;
        }

        const materialLoadingControl = {
          loadingCount: materials.length
        }

        materials.forEach(material => {
          this.loadMaterial(material.id, materialLoadingControl);
        });
      }).catch(err => {
        console.warn(err);
        this.babylonMaterialsLoaded = true;
        if (this.gwtResolver) {
          this.gwtResolver();
        }
      });
  }

  private loadMaterial(materialId: number, materialLoadingControl: { loadingCount: number; }) {
    this.babylonMaterialControllerClient.getData(materialId)
      .then(data => {
        try {
          let material = Material.Parse(data, this.scene, "/rest/images/");
          if (material) {
            this.babylonMaterials.set(materialId, material);
          } else {
            console.error(`Error parsing material`);
          }
          materialLoadingControl.loadingCount--;
          if (materialLoadingControl.loadingCount <= 0) {
            this.babylonMaterialsLoaded = true;
            if (this.gwtResolver) {
              this.gwtResolver();
            }
          }
        } catch (e) {
          console.error(`Error parsing material '${e}'`);
          materialLoadingControl.loadingCount--;
          if (materialLoadingControl.loadingCount <= 0) {
            this.babylonMaterialsLoaded = true;
            if (this.gwtResolver) {
              this.gwtResolver();
            }
          }
        }
      })
      .catch(err => {
        console.error(`Error loading Babylon file '${err}'`);
        materialLoadingControl.loadingCount--;
        if (materialLoadingControl.loadingCount <= 0) {
          this.babylonMaterialsLoaded = true;
          if (this.gwtResolver) {
            this.gwtResolver();
          }
        }
      })
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

  getNodeMaterialNull(babylonModelId: number | null, errorText: string): NodeMaterial {
    if (babylonModelId) {
      return this.getNodeMaterial(babylonModelId);
    } else {
      return <any>BabylonJsUtils.createErrorMaterial(errorText);
    }
  }

  getNodeMaterial(babylonModelId: number): NodeMaterial {
    if (babylonModelId === undefined) {
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
          this.babylonModelUpload(threeJsModelConfig.getId(), new Blob([json], { type: 'application/json' }));
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

  getBabylonMaterial(babylonMaterialId: number): Material {
    if (babylonMaterialId === undefined) {
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
          { responseType: 'blob' })
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
    this.babylonModelUpload(babylonModelId, new Blob([json], { type: 'application/json' }));
  }
}
