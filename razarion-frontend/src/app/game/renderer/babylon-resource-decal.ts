import {
  AbstractMesh,
  Color3,
  Material,
  Mesh,
  MeshBuilder,
  Ray,
  StandardMaterial,
  Texture,
  Vector3,
} from "@babylonjs/core";
import {Scene} from "@babylonjs/core/scene";
import {BabylonRenderServiceAccessImpl, RazarionMetadataType} from "./babylon-render-service-access-impl.service";

export class BabylonResourceDecal {
  private decalMesh: Mesh | null = null;
  private decalMaterial: StandardMaterial | null = null;

  constructor(private scene: Scene, private radius: number, private rendererService: BabylonRenderServiceAccessImpl) {
  }

  updatePosition(x: number, y: number, z: number): void {
    this.dispose();

    const ray = new Ray(new Vector3(x, 100, z), new Vector3(0, -1, 0), 200);
    const pick = this.scene.pickWithRay(ray, (mesh: AbstractMesh) => {
      const meta = BabylonRenderServiceAccessImpl.getRazarionMetadata(mesh);
      if (!meta) return false;
      return meta.type === RazarionMetadataType.GROUND || meta.type === RazarionMetadataType.BOT_GROUND;
    });

    const decalSize = this.radius * 2.5;

    if (pick?.pickedMesh && pick.pickedPoint) {
      const meta = BabylonRenderServiceAccessImpl.getRazarionMetadata(pick.pickedMesh);
      const isBotGround = meta?.type === RazarionMetadataType.BOT_GROUND;

      if (!isBotGround) {
        try {
          const normal = pick.getNormal(true) ?? new Vector3(0, 1, 0);
          const decal = MeshBuilder.CreateDecal("resourceDecal", pick.pickedMesh as Mesh, {
            position: pick.pickedPoint.clone(),
            normal: normal,
            size: new Vector3(decalSize, decalSize, decalSize),
          });
          if (decal && decal.getTotalVertices() > 0) {
            this.decalMesh = decal;
          } else {
            decal?.dispose();
          }
        } catch (_) {
          // Decal creation failed — fall through to disc fallback
        }
      }
    }

    // Fallback: flat disc on ground
    if (!this.decalMesh) {
      this.decalMesh = MeshBuilder.CreateDisc("resourceDecalDisc", {
        radius: decalSize / 2,
        tessellation: 32
      }, this.scene);
      this.decalMesh.position = new Vector3(x, (pick?.pickedPoint?.y ?? y) + 0.05, z);
      this.decalMesh.rotation.x = Math.PI / 2;
    }

    this.decalMesh.isPickable = false;
    this.rendererService.directionalLight.includedOnlyMeshes.push(this.decalMesh);
    this.decalMaterial = new StandardMaterial("resourceDecalMat", this.scene);

    const diffuseTex = new Texture("renderer/textures/resource-decal-diffuse.png", this.scene);
    diffuseTex.hasAlpha = true;
    this.decalMaterial.diffuseTexture = diffuseTex;
    this.decalMaterial.emissiveTexture = diffuseTex;
    const bumpTex = new Texture("renderer/textures/resource-decal-normal.png", this.scene);
    bumpTex.level = 2.0;
    this.decalMaterial.bumpTexture = bumpTex;
    this.decalMaterial.useAlphaFromDiffuseTexture = true;
    this.decalMaterial.transparencyMode = Material.MATERIAL_ALPHABLEND;
    this.decalMaterial.backFaceCulling = false;
    this.decalMaterial.specularColor = new Color3(0.08, 0.08, 0.08);
    this.decalMaterial.specularPower = 64;
    this.decalMaterial.zOffset = -2;

    this.decalMesh.material = this.decalMaterial;
  }

  dispose(): void {
    if (this.decalMesh) {
      const index = this.rendererService.directionalLight.includedOnlyMeshes.indexOf(this.decalMesh);
      if (index !== -1) {
        this.rendererService.directionalLight.includedOnlyMeshes.splice(index, 1);
      }
      this.decalMesh.dispose();
      this.decalMesh = null;
    }
    if (this.decalMaterial) {
      this.decalMaterial.dispose();
      this.decalMaterial = null;
    }
  }
}
