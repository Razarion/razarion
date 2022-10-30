import {Injectable} from "@angular/core";
import {URL_THREE_JS_MODEL} from "src/app/common";
import {ThreeJsModelConfig} from "src/app/gwtangular/GwtAngularFacade";
import {Group, Material, Object3D} from "three";
import {GLTF, GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {GwtHelper} from "../../gwtangular/GwtHelper";
// import NodeObjectLoader from "three/examples/jsm/nodes/loaders/NodeObjectLoader";
// import {NodeMaterialLoader} from "three/examples/jsm/nodes/Nodes";
import {NodeObjectLoader} from "three/examples/jsm/nodes/Nodes";



//import * as SkeletonUtils from 'three/examples/jsm/utils/SkeletonUtils.js';

@Injectable()
export class ThreeJsModelService {
    private threeJsModelMap: Map<number, any> = new Map();
    private gwtAngularService!: GwtAngularService;
    private loader = new GLTFLoader();

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
                      this.blobToGltf(url, threeJsModelConfig, handleResolve);
                      break;

                    case ThreeJsModelConfig.Type.NODES_MATERIAL:
                      this.blobToNodesMaterial(url, threeJsModelConfig, handleResolve);
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

    private blobToGltf(url: string, threeJsModelConfig: ThreeJsModelConfig, handleResolve: () => void) {
      this.loader.load(url,
        (gltf: GLTF) => {
          this.threeJsModelMap.set(threeJsModelConfig.getId(), gltf.scene);
          handleResolve();
        },
        (progressEvent: ProgressEvent) => {
          // TODO console.info(`Loading Three.js model: '${threeJsModelConfig.getInternalName()} (${threeJsModelConfig.getId()})': ${progressEvent.loaded}/${progressEvent.total}`);
        },
        (error: ErrorEvent) => {
          console.error(error);
          handleResolve();
        });
    }

    private blobToNodesMaterial(url: string, threeJsModelConfig: ThreeJsModelConfig, handleResolve: () => void) {
      let nodeObjectLoader = new NodeObjectLoader();
      nodeObjectLoader.load(url,
        (nodeMaterial :any) =>{
        console.warn(nodeMaterial)
          this.threeJsModelMap.set(threeJsModelConfig.getId(), nodeMaterial);
          handleResolve();
        },
        ()=>{},
        (event: Error | ErrorEvent)=>{
          console.warn(event)
          handleResolve();
        }
      );
    }

    cloneObject3D(threeJsModelPackConfigId: number): Object3D {
        let threeJsModelPackConfig = this.gwtAngularService.gwtAngularFacade.threeJsModelPackService.getThreeJsModelPackConfig(threeJsModelPackConfigId);

        let threeJsModel: Object3D = this.getThreeJsModel(threeJsModelPackConfig.getThreeJsModelId());

        threeJsModel = this.removeAuxScene(threeJsModel);

        let object3D = this.findObject3D(threeJsModel, threeJsModelPackConfig.toNamePathAsArray());

        if(object3D === null) {
            throw new Error(`No Object3D for threeJsModelPackConfigId '${threeJsModelPackConfigId}'. Three.js Path  '${threeJsModelPackConfig.toNamePathAsArray()}'`);
        }

        let positionScale = new Group();
        positionScale.name = `ThreeJsModelPackConfigId position scale ${threeJsModelPackConfig.getInternalName()} (${threeJsModelPackConfig.getId()})`;
        positionScale.position.set(
          threeJsModelPackConfig.getPosition().getX(),
          threeJsModelPackConfig.getPosition().getY(),
          threeJsModelPackConfig.getPosition().getZ());
        positionScale.scale.set(
          threeJsModelPackConfig.getScale().getX(),
          threeJsModelPackConfig.getScale().getY(),
          threeJsModelPackConfig.getScale().getZ());
        positionScale.add(this.deepSetup(object3D.clone()));

        let rotation = new Group();
        rotation.name = `ThreeJsModelPackConfigId rotation`;
        rotation.rotation.set(
          threeJsModelPackConfig.getRotation().getX(),
          threeJsModelPackConfig.getRotation().getY(),
          threeJsModelPackConfig.getRotation().getZ());
        rotation.add(positionScale);

        return rotation;
    }

    private findObject3D(object3D: Object3D, namePath: string[]): Object3D | null {
        if(namePath.length == 0) {
            throw new Error("Empty namePath array is not allowed")
        }

        if(object3D.name === namePath[0]) {
            if(namePath.length == 1) {
                return object3D;
            }
            let childNamePath = namePath.slice(1);
            for (let childObject3D of object3D.children) {
                let found = this.findObject3D(childObject3D, childNamePath);
                if(found) {
                    return found;
                }
            }
        }
        return null;
    }

    private deepSetup(object3D: Object3D): Object3D {
        if (object3D === undefined) {
            return object3D;
        }

        object3D.castShadow = true;

        // TODO remove
        if((<any>object3D).isMesh && (<any>object3D).material) {
          (<any>object3D).material.alphaToCoverage = true;
          (<any>object3D).material. depthWrite = true;
        }
        // TODO remove ends

        const children = object3D.children;
        for (let i = 0, l = children.length; i < l; i++) {
            this.deepSetup(children[i]);
        }

        return object3D;
    }

    findInObject3D(threeJsUuid: string, object3D: Object3D): Object3D | null {
        if (object3D.userData.name === threeJsUuid) {
            return object3D;
        }

        for (let children of object3D.children) {
            let found = this.findInObject3D(threeJsUuid, children);
            if(found) {
                return found;
            }
        }
        return null;
    }

    iterateOverObject3D4Names(object3D: Object3D, userDataName: string[]): void {
        if (object3D.userData.name !== undefined) {
            userDataName.push(object3D.userData.name);
        }
        object3D.children.forEach(child => this.iterateOverObject3D4Names(child, userDataName));
    }

    getThreeJsModel(threeJsModelId: number): any {
      if(threeJsModelId === undefined) {
        throw new Error(`ThreeJsModel id undefined`);
      }

      threeJsModelId = GwtHelper.gwtIssueNumber(threeJsModelId);

      let threeJsModel = this.threeJsModelMap.get(threeJsModelId);

      if (!threeJsModel) {
        throw new Error(`No ThreeJsModel for threeJsModelId '${threeJsModelId}`);
      }

      return threeJsModel;
    }

    getMaterial(threeJsModelId: number): Material {
        let threeJsModel = this.getThreeJsModel(threeJsModelId);

        let material:Material | undefined = this.findMaterialRecursively(threeJsModel);
        if(material === undefined) {
            throw new Error(`No material found in threeJsModelId '${threeJsModelId}' does not have a material.`);
        }
        return material;
    }

    getNodesMaterial(threeJsModelId: number): Material {
        return this.getThreeJsModel(threeJsModelId);
    }

    private findMaterialRecursively(threeJsObject: any): Material | undefined {
        if(threeJsObject.material !== undefined) {
            return threeJsObject.material;
        }

        for (let child of threeJsObject.children) {
            let found = this.findMaterialRecursively(child);
            if(found) {
                return found;
           }
        }
        return undefined;
    }

    private removeAuxScene(object3D: Object3D): Object3D {
        if(object3D.name === 'AuxScene') {
            return object3D.children[0];
        }
        return object3D;
    }
}
