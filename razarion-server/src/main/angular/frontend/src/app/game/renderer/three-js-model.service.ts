import { Injectable } from "@angular/core";
import { URL_THREE_JS_MODEL } from "src/app/common";
import { ThreeJsModelConfig } from "src/app/gwtangular/GwtAngularFacade";
import { Group, Object3D } from "three";
import { GLTF, GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader";
//import * as SkeletonUtils from 'three/examples/jsm/utils/SkeletonUtils.js';

@Injectable()
export class ThreeJsModelService {
    private object3Ds: Object3D[] = [];

    init(threeJsModelConfigs: ThreeJsModelConfig[]): Promise<void> {
        const _this = this;

        let promise = new Promise<void>((resolve, reject) => {
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
        return promise;
    }

    cloneObject3D(threeJsUuid: string): Object3D {
        if (this.object3Ds.length === 0) {
            throw new Error(`Not initialized. Can not clone '${threeJsUuid}`);
        }

        let found = null;
        for (let object3D of this.object3Ds) {
            found = this.findInObject3D(threeJsUuid, object3D);
        }
        if (found != null) {
            return this.createThrreJsModel(found, threeJsUuid);
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

    createThrreJsModel(input: Object3D, name: string): Object3D {
        let clone = input.clone();
        // clone.updateMatrix();
        // clone.updateMatrixWorld(true);
        // clone.updateWorldMatrix(true, true);
        return clone;

        // let group = new Group();
        // group.name = name;
        // this.deepClone(input, group);
        // return group;
    }

    private deepClone(input: Object3D, output: Group) {
        if (input === undefined) {
            return;
        }

        output.add(input.clone());
        // let filteredInput = null;
        // if (input.type === 'Mesh') {
        //     filteredInput = input;
        // } else if (input.type === 'SkinnedMesh') {
        //     filteredInput = input;
        // } else if (input.type === 'Bone') {
        //     filteredInput = input;
        // } else  {
        //     console.warn(`Ignored ${input.name} '${input.type}'`);
        // }

        // if(filteredInput != null) {
        //     output.add(input);
        // }

        const children = input.children;
        for (let i = 0, l = children.length; i < l; i++) {
            this.deepClone(children[i], output);
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

}
