import { Injectable } from "@angular/core";
import { URL_THREE_JS_MODEL } from "src/app/common";
import { ThreeJsModelConfig } from "src/app/gwtangular/GwtAngularFacade";
import {Group, Material, Mesh, Object3D, Vector3} from "three";
import { GLTF, GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader";
import {Euler} from "three/src/math/Euler";
//import * as SkeletonUtils from 'three/examples/jsm/utils/SkeletonUtils.js';

@Injectable()
export class ThreeJsModelService {
    private threeJsModels: Object3D[] = [];
    private threeJsModelMap: Map<number, any> = new Map();

    init(threeJsModelConfigs: ThreeJsModelConfig[]): Promise<void> {
      const _this = this;

      return new Promise<void>((resolve, reject) => {
            try {
                const loader = new GLTFLoader();
                let loadingCount = threeJsModelConfigs.length;
                function handleResolve() {
                    loadingCount--;
                    if (loadingCount === 0) {
                        resolve();
                    }
                }
                threeJsModelConfigs.forEach(threeJsModelConfig => {
                    loader.load(`${URL_THREE_JS_MODEL}/${threeJsModelConfig.getId()}`,
                        (gltf: GLTF) => {
                            _this.threeJsModels.push(gltf.scene);
                            _this.threeJsModelMap.set(threeJsModelConfig.getId(), gltf.scene);
                            handleResolve();
                        },
                        (progressEvent: ProgressEvent) => {
                            // TODO console.info(`Loading Three.js model: '${threeJsModelConfig.getInternalName()} (${threeJsModelConfig.getId()})': ${progressEvent.loaded}/${progressEvent.total}`);
                        },
                        (error: ErrorEvent) => {
                            console.error(error);
                            handleResolve();
                        });
                });
            } catch (error) {
                console.error(error);
                reject(error);
            }
        });
    }

    cloneObject3D(threeJsUuid: string): Object3D {
        if (this.threeJsModels.length === 0) {
            throw new Error(`Not initialized. Can not clone '${threeJsUuid}`);
        }

        for (let object3D of this.threeJsModels) {
          let found = this.findInObject3D(threeJsUuid, object3D);
            if(found) {
              return this.createThreeJsModel(found);
            }
        }

        let threeJsUserDataNames: string[] = []
        this.threeJsModels.forEach(object3D => {
          this.iterateOverObject3D4Names(object3D, threeJsUserDataNames);
          threeJsUserDataNames.push("\n");
        });

        throw new Error(`No Object3D for threeJsUuid '${threeJsUuid}'. Available threeJsUuids in Object3Ds userData.name [${threeJsUserDataNames}]`);
    }

    createThreeJsModel(input: Object3D): Object3D {
        let parentTransformation = ThreeJsModelService.setupParentTransformationRecursively(input);

        let clone = input.clone();
        this.deepSetup(clone);
        clone.scale.copy(parentTransformation.scale);
        clone.rotation.copy(parentTransformation.rotation);
        return clone;
    }

    private deepSetup(object3D: Object3D) {
        if (object3D === undefined) {
            return;
        }

        object3D.castShadow = true;

        const children = object3D.children;
        for (let i = 0, l = children.length; i < l; i++) {
            this.deepSetup(children[i]);
        }
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
      return this.threeJsModelMap.get(threeJsModelId);
    }

    getMaterial(threeJsModelId: number): Material {
        if(threeJsModelId === undefined) {
          throw new Error(`Material undefined`);
        }

        let gwtIssueNumber = threeJsModelId;
        if(typeof <any>threeJsModelId  !== 'number') {
            gwtIssueNumber = <number>Object.values(threeJsModelId)[0]; // GWT rubbish
        }

        let threeJsObject = this.threeJsModelMap.get(gwtIssueNumber);
        if(threeJsObject === undefined) {
          throw new Error(`No Material for threeJsModelId '${gwtIssueNumber}'.`);
        }

      let material:Material | undefined = this.findMaterialRecursively(threeJsObject);
        if(material === undefined) {
            throw new Error(`No material found in threeJsModelId '${threeJsModelId}' does not have a material.`);
        }
        return material;
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

  static setupParentTransformationRecursively(object3D: Object3D): {scale: Vector3; rotation: Euler} {
      if(object3D.parent === null) {
        return {scale: object3D.scale, rotation: object3D.rotation} ;
      }

      let parentTransformation = ThreeJsModelService.setupParentTransformationRecursively(object3D.parent);
      parentTransformation.scale.multiply(object3D.scale);
      parentTransformation.rotation.set(parentTransformation.rotation.x + object3D.rotation.x,
        parentTransformation.rotation.y + object3D.rotation.y,
        parentTransformation.rotation.z + object3D.rotation.z);
      return parentTransformation;
  }
}
