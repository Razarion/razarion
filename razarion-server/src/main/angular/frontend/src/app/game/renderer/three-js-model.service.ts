import {Injectable} from "@angular/core";
import {URL_THREE_JS_MODEL} from "src/app/common";
import {ThreeJsModelConfig} from "src/app/gwtangular/GwtAngularFacade";
import {Group, Material, Object3D} from "three";
import {GLTF, GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {GwtHelper} from "../../gwtangular/GwtHelper";

//import * as SkeletonUtils from 'three/examples/jsm/utils/SkeletonUtils.js';

@Injectable()
export class ThreeJsModelService {
    private threeJsModelMap: Map<number, any> = new Map();
    private gwtAngularService!: GwtAngularService;

    init(threeJsModelConfigs: ThreeJsModelConfig[], gwtAngularService: GwtAngularService): Promise<void> {
      const _this = this;
      _this.gwtAngularService = gwtAngularService;

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
        if(threeJsModelId === undefined) {
          throw new Error(`Material undefined`);
        }

        threeJsModelId = GwtHelper.gwtIssueNumber(threeJsModelId);

        let threeJsObject = this.threeJsModelMap.get(threeJsModelId);
        if(threeJsObject === undefined) {
          throw new Error(`No Material for threeJsModelId '${threeJsModelId}'.`);
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

    private removeAuxScene(object3D: Object3D): Object3D {
        if(object3D.name === 'AuxScene') {
            return object3D.children[0];
        }
        return object3D;
    }
}
