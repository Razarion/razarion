import { Injectable } from "@angular/core";
import { URL_THREE_JS_MODEL } from "src/app/common";
import { ThreeJsModelConfig } from "src/app/gwtangular/GwtAngularFacade";
import {Group, Material, Mesh, Object3D} from "three";
import { GLTF, GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader";
//import * as SkeletonUtils from 'three/examples/jsm/utils/SkeletonUtils.js';

@Injectable()
export class ThreeJsModelService {
    private object3Ds: Object3D[] = [];
    private object3DMap: Map<number, any> = new Map();

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
                            _this.object3Ds.push(gltf.scene);
                            _this.object3DMap.set(threeJsModelConfig.getId(), gltf.scene);
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
        if (this.object3Ds.length === 0) {
            throw new Error(`Not initialized. Can not clone '${threeJsUuid}`);
        }

        let found = null;
        for (let object3D of this.object3Ds) {
            found = this.findInObject3D(threeJsUuid, object3D);
            if(found) {
              break;
            }
        }
        if (found != null) {
            return this.createThreeJsModel(found, threeJsUuid);
        }

        let _this = this;
        function gatherThreeJsUserDataNames() {
            let threeJsUserDataNames: string[] = []
            for (let object3D of _this.object3Ds) {
                _this.iterateOverObject3D4Names(object3D, threeJsUserDataNames);
            }
            return threeJsUserDataNames;
        }

        throw new Error(`No Object3D for threeJsUuid '${threeJsUuid}'. Available threeJsUuids in Object3Ds userData.name [${gatherThreeJsUserDataNames()}]`);
    }

    createThreeJsModel(input: Object3D, name: string): Object3D {
        let clone = input.clone();
        clone.name = name;
        this.deepSetup(clone);
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
        const children = object3D.children;

        for (let i = 0, l = children.length; i < l; i++) {
            const child = children[i];
            return this.findInObject3D(threeJsUuid, child);
        }
        return null;
    }

    iterateOverObject3D4Names(object3D: Object3D, userDataName: string[]): void {
        if (object3D.userData.name !== undefined) {
            userDataName.push(object3D.userData.name);
        }
        const children = object3D.children;
        for (let i = 0, l = children.length; i < l; i++) {
            const child = children[i];
            return this.iterateOverObject3D4Names(child, userDataName);
        }
    }

    getMaterial(threeJsModelId: number): Material {
        if(threeJsModelId === undefined) {
          throw new Error(`Material undefined`);
        }

        let gwtIssueNumber = threeJsModelId;
        if(typeof <any>threeJsModelId  !== 'number') {
            gwtIssueNumber = <number>Object.values(threeJsModelId)[0]; // GWT rubbish
        }

        let threeJsObject = this.object3DMap.get(gwtIssueNumber);
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

        if(threeJsObject.children !== undefined) {
          const length = threeJsObject.children.length;
          for (let i = 0; i < length; i++) {
            return this.findMaterialRecursively(threeJsObject.children[i]);
          }
        }

        return undefined;
    }

}
