import {Injectable} from "@angular/core";
import {URL_THREE_JS_MODEL} from "src/app/common";
import {ThreeJsModelConfig} from "src/app/gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {GwtHelper} from "../../gwtangular/GwtHelper";
import * as BABYLON from 'babylonjs';
import {Mesh} from "babylonjs/Meshes/mesh";
import Scene = BABYLON.Scene;
import AssetContainer = BABYLON.AssetContainer;
import Node = BABYLON.Node;
import NodeMaterial = BABYLON.NodeMaterial;


//import * as SkeletonUtils from 'three/examples/jsm/utils/SkeletonUtils.js';

@Injectable()
export class ThreeJsModelService {
  private assetContainers: Map<number, AssetContainer> = new Map();
  private nodeMaterials: Map<number, NodeMaterial> = new Map();
  private gwtAngularService!: GwtAngularService;
  private scene!: Scene;


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

    let rootMesh = null;

    for (let childNod of assetContainer.getNodes()) {
      rootMesh = this.findChildNode(childNod, threeJsModelPackConf.toNamePathAsArray());
      if (rootMesh) {
        break;
      }
    }

    if (rootMesh == null) {
      throw new Error(`No Mesh for threeJsModelPackConfigId '${threeJsModelPackConfigId}'. Three.js Path  '${threeJsModelPackConf.toNamePathAsArray()}'`);
    }

    const mesh = (<Mesh>rootMesh).clone("", parent);

    mesh.position.set(threeJsModelPackConf.getPosition().getX(),
      threeJsModelPackConf.getPosition().getY(),
      threeJsModelPackConf.getPosition().getZ());
    mesh.scaling.set(threeJsModelPackConf.getScale().getX(),
      threeJsModelPackConf.getScale().getY(),
      threeJsModelPackConf.getScale().getZ());
    mesh.rotationQuaternion = null;
    mesh.rotation.set(threeJsModelPackConf.getRotation().getX(),
      threeJsModelPackConf.getRotation().getY(),
      threeJsModelPackConf.getRotation().getZ());
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
      const result = BABYLON.SceneLoader.LoadAssetContainer(url, '', this.scene, assetContainer => {
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
        })
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
    BABYLON.NodeMaterial.ParseFromFileAsync(
      `Node Material '${threeJsModelConfig.getInternalName()} (${threeJsModelConfig.getId()})'`,
      url,
      this.scene
    ).then(nodeMaterial => {
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

  getNodeMaterial(babylonJsModelId: number): NodeMaterial {
    if (babylonJsModelId === undefined) {
      throw new Error(`getNodeMaterial(): babylonJsModelId id undefined`);
    }
    babylonJsModelId = GwtHelper.gwtIssueNumber(babylonJsModelId);

    let nodeMaterial = this.nodeMaterials.get(babylonJsModelId);

    if (!nodeMaterial) {
      throw new Error(`No NodeMaterial for babylonJsModelId '${babylonJsModelId}'`);
    }

    return nodeMaterial;
  }
}
