import {Injectable} from "@angular/core";
import {URL_THREE_JS_MODEL, URL_THREE_JS_MODEL_EDITOR} from "src/app/common";
import {ThreeJsModelConfig} from "src/app/gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {GwtHelper} from "../../gwtangular/GwtHelper";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {
  AssetContainer,
  IInspectable,
  InspectableType,
  Mesh,
  Node,
  NodeMaterial,
  Scene,
  SceneLoader
} from "@babylonjs/core";
import {GLTFFileLoader} from "@babylonjs/loaders";
import Type = ThreeJsModelConfig.Type;

@Injectable()
export class BabylonModelService {
  private assetContainers: Map<number, AssetContainer> = new Map();
  private nodeMaterials: Map<number, NodeMaterial> = new Map();
  private gwtAngularService!: GwtAngularService;
  private scene!: Scene;

  constructor(private httpClient: HttpClient, private messageService: MessageService) {
    SceneLoader.RegisterPlugin(new GLTFFileLoader());
  }

  init(threeJsModelConfigs: ThreeJsModelConfig[], gwtAngularService: GwtAngularService): Promise<void> {
    const _this = this;
    _this.gwtAngularService = gwtAngularService;

    return new Promise<void>((resolve, reject) => {
      try {
        let loadingCount = threeJsModelConfigs.length;

        function handleResolve() {
          loadingCount--;
          if (loadingCount === 0) {
            resolve();
          }
        }

        threeJsModelConfigs.forEach(threeJsModelConfig => {
          try {
            const url = `${URL_THREE_JS_MODEL}/${threeJsModelConfig.getId()}`;
            switch (GwtHelper.gwtIssueStringEnum(threeJsModelConfig.getType())) {
              case ThreeJsModelConfig.Type.GLTF:
                this.loadAssetContainer(url, threeJsModelConfig, handleResolve);
                break;
              case ThreeJsModelConfig.Type.NODES_MATERIAL:
                this.loadNodeMaterial(url, threeJsModelConfig, handleResolve);
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

  cloneMesh(threeJsModelPackConfigId: number, parent: Node): Node {
    const threeJsModelPackConf = this.gwtAngularService.gwtAngularFacade.threeJsModelPackService.getThreeJsModelPackConfig(threeJsModelPackConfigId);

    const assetContainer: AssetContainer = this.getAssetContainer(threeJsModelPackConf.getThreeJsModelId());

    let childMesh = null;

    for (let childNod of assetContainer.getNodes()) {
      childMesh = this.findChildNode(childNod, threeJsModelPackConf.toNamePathAsArray());
      if (childMesh) {
        break;
      }
    }

    if (childMesh == null) {
      throw new Error(`No Mesh for threeJsModelPackConfigId '${threeJsModelPackConfigId}'. Three.js Path  '${threeJsModelPackConf.toNamePathAsArray()}'`);
    }

    const mesh = (<Mesh>childMesh).clone("", parent);

    mesh.receiveShadows = true;
    mesh.getChildMeshes().forEach(m => m.receiveShadows = true);

    mesh.position.set(threeJsModelPackConf.getPosition().getX(),
      threeJsModelPackConf.getPosition().getZ(),
      threeJsModelPackConf.getPosition().getY());
    mesh.scaling.set(threeJsModelPackConf.getScale().getX(),
      threeJsModelPackConf.getScale().getZ(),
      threeJsModelPackConf.getScale().getY());
    mesh.rotationQuaternion = null;
    mesh.rotation.set(threeJsModelPackConf.getRotation().getX(),
      threeJsModelPackConf.getRotation().getZ(),
      threeJsModelPackConf.getRotation().getY());
    return mesh;
  }

  private findChildNode(node: Node, namePath: string[]): Node | null {
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
        progress => {
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
      handleResolve();
    })
  }

  getAssetContainer(threeJsModelId: number): any {
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

}
