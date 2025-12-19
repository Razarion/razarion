import {
  BabylonMaterialControllerClient,
  BabylonMaterialEntity,
  BaseEntity,
  GltfEntity,
  ParticleSystemControllerClient,
  ParticleSystemEntity
} from "../../generated/razarion-share";
import {
  AssetContainer,
  Material,
  NodeMaterial,
  NodeParticleSystemSet,
  PBRMaterial,
  Scene,
  SceneLoader
} from "@babylonjs/core";
import {TypescriptGenerator} from "../../backend/typescript-generator";
import {HttpClient} from "@angular/common/http";
import {BabylonModelService} from "./babylon-model.service";
import {URL_GLTF} from "../../common";
import {GltfHelper} from "./gltf-helper";
import {Diplomacy} from "../../gwtangular/GwtAngularFacade";
import {ISceneLoaderProgressEvent} from '@babylonjs/core/Loading/sceneLoader';
import {NgZone} from '@angular/core';

export abstract class BabylonModelContainer<E extends BaseEntity, B> {
  private entities: Map<number, E> = new Map();
  private loaded = false;
  private babylonModels: Map<number, B> = new Map();
  protected babylonModelService!: BabylonModelService;
  private loadingCount = 0;

  load(entities: E[], babylonModelService: BabylonModelService, scene: Scene) {
    this.babylonModelService = babylonModelService;
    this.entities.clear();
    if (!entities || entities.length === 0) {
      this.loaded = true;
      this.babylonModelService.handleLoaded();
      return;
    }

    this.loadingCount = entities.length

    entities.forEach(entity => {
      this.entities.set(entity.id, entity);
      this.loadBabylonModel(entity, scene);
    });

  }

  isLoaded(): boolean {
    return this.loaded;
  }

  getEntity(entityId: number): E {
    return this.entities.get(entityId)!
  }

  getBabylonModel(entityId: number): B {
    return this.babylonModels.get(entityId)!;
  }

  protected abstract loadBabylonModel(babylonMaterialEntity: E, scene: Scene): void;

  protected setBabylonModel(entity: E, babylonModel: B) {
    this.babylonModels.set(entity.id, babylonModel);
  }

  protected handleBabylonModelLaded() {
    this.loadingCount--;
    if (this.loadingCount <= 0) {
      this.loaded = true;
      this.babylonModelService.handleLoaded();
    }
  }
}

export class BabylonMaterialContainer extends BabylonModelContainer<BabylonMaterialEntity, Material> {
  private babylonMaterialControllerClient!: BabylonMaterialControllerClient;

  public setHttpClient(httpClient: HttpClient): void {
    this.babylonMaterialControllerClient = new BabylonMaterialControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  protected loadBabylonModel(babylonMaterialEntity: BabylonMaterialEntity, scene: Scene): void {
    this.babylonMaterialControllerClient.getData(babylonMaterialEntity.id)
      .then(data => {
        try {
          let material;
          if (babylonMaterialEntity.nodeMaterial) {
            material = NodeMaterial.Parse(data, scene, "/rest/images/");
            material.ignoreAlpha = false; // Can not be saved in the NodeEditor
          } else {
            material = Material.Parse(data, scene, "/rest/images/");
          }
          if (material) {
            this.setBabylonModel(babylonMaterialEntity, material);
          } else {
            console.error(`Error parsing material`);
          }
          this.handleBabylonModelLaded();
        } catch (e) {
          console.error(e);
          console.error(`Error parsing material '${e}'`);
          this.handleBabylonModelLaded();
        }
      })
      .catch(err => {
        console.error(`Error loading Babylon file '${err}'`);
        this.handleBabylonModelLaded();
      })
  }
}

export class GlbContainer extends BabylonModelContainer<GltfEntity, AssetContainer> {
  private gltfHelpers: Map<number, GltfHelper> = new Map();
  diplomacyMaterialCache: Map<number, Map<Diplomacy, Map<string, NodeMaterial>>> = new Map<number, Map<Diplomacy, Map<string, NodeMaterial>>>();

  constructor(private babylonMaterialContainer: BabylonMaterialContainer, private zone: NgZone) {
    super();
  }

  public getGltfHelper(gltfEntityId: number) {
    return this.gltfHelpers.get(gltfEntityId);
  }

  protected loadBabylonModel(gltfEntity: GltfEntity, scene: Scene): void {
    const url = `${URL_GLTF}/glb/${gltfEntity.id}`;
    const gltfHelper = new GltfHelper(gltfEntity, this.babylonModelService, this, this.babylonMaterialContainer);
    this.gltfHelpers.set(gltfEntity.id, gltfHelper);
    try {
      let hasError = false;
      const result = SceneLoader.LoadAssetContainer(url, '', scene, assetContainer => {
          try {
            this.zone.run(() => {
              this.babylonModelService.glbContainerProgress = undefined;
            });
            if (!hasError) {
              this.setBabylonModel(gltfEntity, assetContainer);
              this.assignGlbTextures(gltfEntity, assetContainer, gltfHelper);
              this.handleBabylonModelLaded();
            }
          } catch (error) {
            console.error(error);
          }
        },
        (event: ISceneLoaderProgressEvent) => {
          this.zone.run(() => {
            this.babylonModelService.glbContainerProgress = {
              loaded: event.loaded, total: event.total
            }
          });
        },
        (scene: Scene, message: string, exception?: any) => {
          hasError = true;
          console.error(`Error loading glTF/glb '${url}'. exception: '${exception}'`);
          this.handleBabylonModelLaded();
        }, ".glb")
      if (result === null) {
        console.error(`Error loading glTF/glb '${url}'`);
        this.handleBabylonModelLaded();
      }
    } catch (e) {
      console.error(`Error loading glTF/glb '${url}'`);
      console.error(e);
      this.handleBabylonModelLaded();
    }

  }

  private assignGlbTextures(gltf: GltfEntity, assetContainer: AssetContainer, gltfHelper: GltfHelper) {
    Object.keys(gltf.materialGltfNames).forEach((gltfMaterialName: string) => {
      let materialId = gltf.materialGltfNames[gltfMaterialName];
      let babylonMaterialEntity = this.babylonMaterialContainer.getEntity(materialId);
      if (babylonMaterialEntity) {
        if (babylonMaterialEntity.overrideAlbedoTextureNode
          || babylonMaterialEntity.overrideMetallicTextureNode
          || babylonMaterialEntity.overrideBumpTextureNode
          || babylonMaterialEntity.overrideAmbientOcclusionTextureNode) {
          let glbMaterial = <PBRMaterial>assetContainer.materials.find(material => material.name === gltfMaterialName);
          if (glbMaterial) {
            gltfHelper.assignTextures(babylonMaterialEntity, glbMaterial);
          } else {
            console.warn(`No material in AssetContainer ${gltfMaterialName}`)
          }
        }
      } else {
        console.warn(`BabylonMaterialEntity not found. materialId: ${materialId}`)
      }
    });
  }
}

export class ParticleSystemSetContainer extends BabylonModelContainer<ParticleSystemEntity, NodeParticleSystemSet> {
  private particleSystemControllerClient!: ParticleSystemControllerClient;

  public setHttpClient(httpClient: HttpClient): void {
    this.particleSystemControllerClient = new ParticleSystemControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  protected loadBabylonModel(particleSystemEntity: ParticleSystemEntity, scene: Scene): void {
    this.particleSystemControllerClient.getData(particleSystemEntity.id)
      .then(data => {
        this.handleBabylonModelLaded();
        this.setBabylonModel(particleSystemEntity, NodeParticleSystemSet.Parse(data));
      }).catch(err => {
      console.error(`Load Particle System failed (inner). '${particleSystemEntity.internalName} (${particleSystemEntity.id})' Reason: ${err}`);
      this.handleBabylonModelLaded();
    }).catch(err => {
      console.error(`Load Particle System failed (outer). '${particleSystemEntity.internalName} (${particleSystemEntity.id})' Reason: ${err}`);
      this.handleBabylonModelLaded();
    })
  }

}
