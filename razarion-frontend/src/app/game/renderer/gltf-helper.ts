import {BabylonMaterialEntity, GltfEntity} from "../../generated/razarion-share";
import {BabylonMaterialContainer, GlbContainer} from "./babylon-model-container";
import {BaseTexture, InputBlock, Material, Mesh, NodeMaterial, Nullable, PBRMaterial} from "@babylonjs/core";
import {Diplomacy} from "../../gwtangular/GwtAngularFacade";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {BabylonModelService} from "./babylon-model.service";
import {FragmentOutputBlock} from "@babylonjs/core/Materials/Node/Blocks/Fragment/fragmentOutputBlock";

export class GltfHelper {
  private materialIds: Map<string, number> = new Map();
  private gltfTexturesMap: Map<string, GltfTextures> = new Map();

  constructor(gltf: GltfEntity,
              private babylonModelService: BabylonModelService,
              private glbContainer: GlbContainer,
              private babylonMaterialContainer: BabylonMaterialContainer) {
    Object.keys(gltf.materialGltfNames).forEach((gltfName: string) => {
      let materialId = gltf.materialGltfNames[gltfName];
      this.materialIds.set(gltfName, materialId)
    })
  }

  handleMaterial(mesh: Mesh, diplomacy?: Diplomacy) {
    const originalMaterialName = mesh.material!.name;
    const materialId = this.materialIds.get(originalMaterialName);
    if (materialId) {
      let babylonMaterialEntity = this.babylonMaterialContainer.getEntity(materialId);
      if (diplomacy) {
        if (!babylonMaterialEntity?.diplomacyColorNode) {
          mesh.material = this.babylonModelService.getBabylonMaterial(materialId, diplomacy).clone(mesh.material!.name);
          mesh.material = this.checkAndSetAlpha(diplomacy, mesh.material!);
          return
        }

        let diplomacyCache = this.glbContainer.diplomacyMaterialCache.get(materialId);
        if (!diplomacyCache) {
          diplomacyCache = new Map<Diplomacy, Map<string, NodeMaterial>>();
          this.glbContainer.diplomacyMaterialCache.set(materialId, diplomacyCache)
        }
        let cachedMaterialNames = diplomacyCache.get(diplomacy);
        if (!cachedMaterialNames) {
          cachedMaterialNames = new Map<string, NodeMaterial>();
          diplomacyCache.set(diplomacy, cachedMaterialNames)
        }
        let cachedMaterial = cachedMaterialNames.get(mesh.material!.name);
        if (!cachedMaterial) {
          cachedMaterial = <NodeMaterial>this.babylonModelService.getBabylonMaterial(materialId).clone(`${mesh.material!.name} ${materialId} '${diplomacy}'`)!;
          cachedMaterial = cachedMaterial.clone(cachedMaterial.name)!
          this.checkAndSetAlpha(diplomacy, cachedMaterial);
          const diplomacyColorNode = (<NodeMaterial>cachedMaterial).getBlockByPredicate(block => {
            return babylonMaterialEntity.diplomacyColorNode === block.name;
          });
          if (diplomacyColorNode) {
            (<InputBlock>diplomacyColorNode).value = BabylonRenderServiceAccessImpl.color4Diplomacy(diplomacy);
          }
          if (babylonMaterialEntity!.overrideAlbedoTextureNode
            || babylonMaterialEntity!.overrideMetallicTextureNode
            || babylonMaterialEntity!.overrideBumpTextureNode
            || babylonMaterialEntity!.overrideAmbientOcclusionTextureNode) {
            let gltfTextures = this.gltfTexturesMap.get(originalMaterialName);
            gltfTextures && gltfTextures.overrideTexture(<NodeMaterial>cachedMaterial);
          }
          (<NodeMaterial>cachedMaterial).build()
          cachedMaterialNames.set(mesh.material!.name, cachedMaterial);
        }
        mesh.material = cachedMaterial!;
      } else {
        mesh.material = this.babylonModelService.getBabylonMaterial(materialId, diplomacy).clone(mesh.material!.name);
        mesh.material = this.checkAndSetAlpha(diplomacy, mesh.material!);
      }
    } else {
      mesh.material = this.checkAndSetAlpha(diplomacy, mesh.material!);
    }
  }

  private checkAndSetAlpha(diplomacy: Diplomacy | undefined, material: Material): Material {
    if (diplomacy == Diplomacy.OWN_PLACER) {
      if (material instanceof NodeMaterial) {
        material.ignoreAlpha = false;
        const fragmentOutput = (<NodeMaterial>material).getBlockByName("FragmentOutput");
        if (fragmentOutput) {
          const alphaBlock = new InputBlock("Alpha");
          alphaBlock.value = 0.4;
          alphaBlock.output.connectTo((<FragmentOutputBlock>fragmentOutput).a);
        }
        return material;
      } else {
        let alphaMaterial = material.clone(`clone of ${material.name} ${diplomacy}`)!;
        alphaMaterial.alpha = 0.4;
        alphaMaterial.transparencyMode = 2; // ALPHABLEND
        return alphaMaterial;
      }
    }
    return material;
  }

  assignTextures(babylonMaterialEntity: BabylonMaterialEntity, glbMaterial: PBRMaterial) {
    let albedoTexture: Nullable<BaseTexture> = null;
    let metallicTexture: Nullable<BaseTexture> = null;
    let bumpTexture: Nullable<BaseTexture> = null;
    let ambientOcclusionTexture: Nullable<BaseTexture> = null;

    if (babylonMaterialEntity.overrideAlbedoTextureNode) {
      albedoTexture = glbMaterial._albedoTexture;
    }
    if (babylonMaterialEntity.overrideMetallicTextureNode) {
      metallicTexture = glbMaterial._metallicTexture;
    }
    if (babylonMaterialEntity.overrideBumpTextureNode) {
      bumpTexture = glbMaterial._bumpTexture;
    }
    if (babylonMaterialEntity.overrideAmbientOcclusionTextureNode) {
      ambientOcclusionTexture = glbMaterial._ambientTexture;
    }
    if (albedoTexture || metallicTexture || bumpTexture) {
      this.gltfTexturesMap.set(glbMaterial.name, new GltfTextures(albedoTexture,
        metallicTexture,
        bumpTexture,
        ambientOcclusionTexture,
        babylonMaterialEntity));
    }
  }
}


class GltfTextures {
  constructor(public albedoTexture: Nullable<BaseTexture>,
              private metallicTexture: Nullable<BaseTexture>,
              private bumpTexture: Nullable<BaseTexture>,
              private ambientOcclusionTexture: Nullable<BaseTexture>,
              private babylonMaterialEntity: BabylonMaterialEntity) {
  }

  overrideTexture(nodeMaterial: NodeMaterial) {
    if (this.albedoTexture) {
      (<any>nodeMaterial.getBlockByName(this.babylonMaterialEntity.overrideAlbedoTextureNode)).texture = this.albedoTexture;
    }
    if (this.metallicTexture) {
      (<any>nodeMaterial.getBlockByName(this.babylonMaterialEntity.overrideMetallicTextureNode)).texture = this.metallicTexture;
    }
    if (this.bumpTexture) {
      (<any>nodeMaterial.getBlockByName(this.babylonMaterialEntity.overrideBumpTextureNode)).texture = this.bumpTexture;
    }
    if (this.ambientOcclusionTexture) {
      (<any>nodeMaterial.getBlockByName(this.babylonMaterialEntity.overrideAmbientOcclusionTextureNode)).texture = this.ambientOcclusionTexture;
      (<any>nodeMaterial.getBlockByName("ambientOcclusionEnable")).value = 1;
    }
  }
}
