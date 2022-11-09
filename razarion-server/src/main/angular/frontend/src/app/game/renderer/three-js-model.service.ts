import {Injectable} from "@angular/core";
import {URL_THREE_JS_MODEL} from "src/app/common";
import {ThreeJsModelConfig} from "src/app/gwtangular/GwtAngularFacade";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {GwtHelper} from "../../gwtangular/GwtHelper";
import * as BABYLON from 'babylonjs';
import NullEngine = BABYLON.NullEngine;
import Scene = BABYLON.Scene;
import AssetContainer = BABYLON.AssetContainer;


//import * as SkeletonUtils from 'three/examples/jsm/utils/SkeletonUtils.js';

@Injectable()
export class ThreeJsModelService {
  private threeJsModelMap: Map<number, AssetContainer> = new Map();
  private gwtAngularService!: GwtAngularService;
  private virtualScene = new Scene(new NullEngine(), {virtual: true});


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

  cloneObject3D(threeJsModelPackConfigId: number): any {
    const threeJsModelPackConfig = this.gwtAngularService.gwtAngularFacade.threeJsModelPackService.getThreeJsModelPackConfig(threeJsModelPackConfigId);

    const threeJsModel: any = this.getThreeJsModel(threeJsModelPackConfig.getThreeJsModelId());

    return threeJsModel;
    //
    // threeJsModel = this.removeAuxScene(threeJsModel);
    //
    // let object3D = this.findObject3D(threeJsModel, threeJsModelPackConfig.toNamePathAsArray());
    //
    // if(object3D === null) {
    //     throw new Error(`No Object3D for threeJsModelPackConfigId '${threeJsModelPackConfigId}'. Three.js Path  '${threeJsModelPackConfig.toNamePathAsArray()}'`);
    // }

    // let positionScale = new Group();
    // positionScale.name = `ThreeJsModelPackConfigId position scale ${threeJsModelPackConfig.getInternalName()} (${threeJsModelPackConfig.getId()})`;
    // positionScale.position.set(
    //   threeJsModelPackConfig.getPosition().getX(),
    //   threeJsModelPackConfig.getPosition().getY(),
    //   threeJsModelPackConfig.getPosition().getZ());
    // positionScale.scale.set(
    //   threeJsModelPackConfig.getScale().getX(),
    //   threeJsModelPackConfig.getScale().getY(),
    //   threeJsModelPackConfig.getScale().getZ());
    // positionScale.add(this.deepSetup(object3D.clone()));
    //
    // let rotation = new Group();
    // rotation.name = `ThreeJsModelPackConfigId rotation`;
    // rotation.rotation.set(
    //   threeJsModelPackConfig.getRotation().getX(),
    //   threeJsModelPackConfig.getRotation().getY(),
    //   threeJsModelPackConfig.getRotation().getZ());
    // rotation.add(positionScale);
    //
    // return rotation;
  }

    private blobToGltf(url: string, threeJsModelConfig: ThreeJsModelConfig, handleResolve: () => void) {
      try {
        const result = BABYLON.SceneLoader.LoadAssetContainer(url, '', this.virtualScene, assetContainer => {
            this.threeJsModelMap.set(threeJsModelConfig.getId(), assetContainer);
            handleResolve();
          },
          progress => {
          },
          error => {
            console.error(`Error loading Babylon Asset ${url}`);
            console.error(error);
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

    private blobToNodesMaterial(url: string, threeJsModelConfig: ThreeJsModelConfig, handleResolve: () => void) {
      // let nodeObjectLoader = new NodeObjectLoader();
      // nodeObjectLoader.load(url,
      //   (nodeMaterial :any) =>{
      //   console.warn(nodeMaterial)
      //     this.threeJsModelMap.set(threeJsModelConfig.getId(), nodeMaterial);
      //     handleResolve();
      //   },
      //   ()=>{},
      //   (event: Error | ErrorEvent)=>{
      //     console.warn(event)
      handleResolve();
      //   }
      // );
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

}
