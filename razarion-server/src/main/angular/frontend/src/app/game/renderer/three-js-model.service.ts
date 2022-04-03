import { formatNumber } from "@angular/common";
import { Injectable } from "@angular/core";
import { ThreeJsModelConfig } from "src/app/gwtangular/GwtAngularFacade";
import { Object3D } from "three";
import { GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader";

@Injectable()
export class ThreeJsModelService {
    private object3Ds: Object3D[] = [];

    init(threeJsModelConfigs: ThreeJsModelConfig[]): Promise<void> {
        const _this = this;

        let promise = new Promise<void>((resolve, reject) => {
            const loader = new GLTFLoader();
            let loadingCount = threeJsModelConfigs.length;
            function handleResolve() {
                loadingCount--;
                if (loadingCount === 0) {
                    resolve();
                }
            }
            threeJsModelConfigs.forEach(threeJsModelConfig => {
                loader.load(`gz/three-js-model/${threeJsModelConfig.getId()}`,
                    function (gltf: any) {
                        _this.object3Ds.push(gltf.scene);
                        handleResolve();
                    },
                    undefined, function (error) {
                        console.error(error);
                        handleResolve();
                    });
            });
        })
        return promise;
    }

    cloneObject3D(threeJsUuid: string): Object3D {
        for (let object3D of this.object3Ds) {
            let found = this.iterateOverObject3D(threeJsUuid, object3D);
            if (found != null) {
                return found.clone();
            }
        }
        throw new Error(`No Object3D for uuid ${threeJsUuid}`);
    }

    iterateOverObject3D(threeJsUuid: string, object3D: Object3D): Object3D | null{
        if (object3D.userData.name !== threeJsUuid) {
            return object3D.clone();
        }
        const children = object3D.children;

        for (let i = 0, l = children.length; i < l; i++) {
            const child = children[i];
            return this.iterateOverObject3D(threeJsUuid, child);
        }
        return null;
    }

}