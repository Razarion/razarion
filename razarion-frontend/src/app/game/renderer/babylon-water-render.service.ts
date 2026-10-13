import {Injectable} from "@angular/core";
import {GroundConfig, Index} from "../../gwtangular/GwtAngularFacade";
import {VertexBuffer} from "@babylonjs/core/Buffers/buffer";
import {NodeMaterial} from "@babylonjs/core/Materials/Node/nodeMaterial";
import {CubeTexture} from "@babylonjs/core/Materials/Textures/cubeTexture";
import {Vector3} from "@babylonjs/core/Maths/math.vector";
import {Mesh} from "@babylonjs/core/Meshes/mesh";
import {MeshBuilder} from "@babylonjs/core/Meshes/meshBuilder";
import {TransformNode} from "@babylonjs/core/Meshes/transformNode";
import {BabylonModelService} from "./babylon-model.service";
import type {Scene} from "@babylonjs/core/scene";
import {SphericalPolynomial} from "@babylonjs/core/Maths/sphericalPolynomial";
import {BabylonTerrainTileImpl} from "./babylon-terrain-tile.impl";
import type {FloatArray} from '@babylonjs/core/types';
import {ReflectionTextureBaseBlock} from '@babylonjs/core/Materials/Node/Blocks/Dual/reflectionTextureBaseBlock';
import {BabylonRenderServiceAccessImpl, RazarionMetadataType} from "./babylon-render-service-access-impl.service";
import {whitecapMaterial} from "./whitecap-material";

/**
 * Per-tile water resources. Nothing in them is the tile's own to release: the water NodeMaterial is a
 * shared model material fetched from BabylonModelService, the whitecap material and the reflection
 * cube texture are the scene's; the water and whitecap MESHES are children of the tile container and
 * are disposed by the container. dispose() stays for the tile to call.
 */
export interface WaterTileResources {
  waterMesh: Mesh;
  whitecapMesh: Mesh;
  dispose(): void;
}

/**
 * The spherical polynomial Babylon works out for the clouds.jpg cube (x, y, z, xx, yy, zz, yz, zx, xy),
 * read off a Pixel 7 after it had (2026-10-12). Redo it if the picture changes: let the texture
 * compute it once (comment out the assignment in reflection()) and read
 * reflectionCubeTexture._texture._sphericalPolynomial.
 */
const CLOUDS_SPHERICAL_POLYNOMIAL: number[][] = [
  [-0.00485989, -0.00442480, -0.00335805],
  [-0.00072347, -0.00244799, -0.00358058],
  [0, 0, 0],
  [0.28752938, 0.38278582, 0.49801066],
  [0.28841903, 0.38352570, 0.49848199],
  [0.28797421, 0.38315576, 0.49824633],
  [0.00022214, 0.00060267, 0.00084347],
  [0, 0, 0],
  [0, 0, 0],
];

@Injectable({
  providedIn: 'root'
})
export class BabylonWaterRenderService {
  private reflectionScene: Scene | null = null;
  private reflectionCubeTexture: CubeTexture | null = null;

  constructor(private babylonModelService: BabylonModelService) {
  }

  /**
   * The sky the water reflects, one for the scene. Every water tile used to make its own from the same
   * picture and put it on the shared water material - and the material's reflection works out the
   * texture's spherical harmonics, which reads the six faces back from the GPU and folds them on the
   * CPU: 1.1 s on the main thread of a Pixel 7 for every water tile that came into view (2026-10-12).
   * And the one texture still cost that once, on the first water tile, often well into the game: the
   * picture never changes, so its polynomial is written down (CLOUDS_SPHERICAL_POLYNOMIAL) and Babylon
   * finds it there instead of working it out.
   */
  private reflection(scene: Scene): CubeTexture {
    if (!this.reflectionCubeTexture || this.reflectionScene !== scene) {
      this.reflectionScene = scene;
      this.reflectionCubeTexture = CubeTexture.CreateFromImages([
        "renderer/env/clouds.jpg", // +X
        "renderer/env/clouds.jpg", // +Y
        "renderer/env/clouds.jpg", // +Z
        "renderer/env/clouds.jpg", // -X
        "renderer/env/clouds.jpg", // -Y
        "renderer/env/clouds.jpg", // -Z
      ], scene);
      this.reflectionCubeTexture.sphericalPolynomial = SphericalPolynomial.FromArray(CLOUDS_SPHERICAL_POLYNOMIAL);
    }
    return this.reflectionCubeTexture;
  }

  public setup(index: Index, groundConfig: GroundConfig, container: TransformNode, uv2GroundHeightMap: FloatArray, rendererService: BabylonRenderServiceAccessImpl): WaterTileResources {
    const water = MeshBuilder.CreateGround("Water", {
      width: BabylonTerrainTileImpl.NODE_X_COUNT,
      height: BabylonTerrainTileImpl.NODE_Y_COUNT,
      subdivisions: 160
    });

    water.material = this.babylonModelService.getBabylonMaterial(groundConfig.getWaterBabylonMaterialId());
    const sharedWaterMaterial = <NodeMaterial>water.material;
    sharedWaterMaterial.ignoreAlpha = false; // Can not be saved in the NodeEditor
    const reflectionBlock = <ReflectionTextureBaseBlock>sharedWaterMaterial.getBlockByName("Reflection");
    const reflection = this.reflection(rendererService.getScene());
    if (reflectionBlock.texture !== reflection) {
      reflectionBlock.texture = reflection;
    }

    // Reduce water texture tiling — default Ground Scale of 15 is too repetitive
    const waterMat = water.material as NodeMaterial;
    const groundScaleBlock = waterMat.getInputBlocks().find(b => b.name === "Ground Scale");
    if (groundScaleBlock) {
      groundScaleBlock.value = new Vector3(40, 40, 40);
    }
    const waveSpeedBlock = waterMat.getInputBlocks().find(b => b.name === "Wave Speed");
    if (waveSpeedBlock) {
      waveSpeedBlock.value = 0.03;
    }

    water.setVerticesData(VertexBuffer.UV2Kind, uv2GroundHeightMap)
    water.receiveShadows = true;
    water.parent = container;
    water.position.x = index.getX() * BabylonTerrainTileImpl.NODE_X_COUNT + BabylonTerrainTileImpl.NODE_X_COUNT / 2;
    water.position.z = index.getY() * BabylonTerrainTileImpl.NODE_Y_COUNT + BabylonTerrainTileImpl.NODE_Y_COUNT / 2;
    container.getChildren().push(water);

    BabylonRenderServiceAccessImpl.setRazarionMetadataSimple(water, RazarionMetadataType.GROUND, undefined, undefined);

    // Whitecap overlay mesh — sits just above water surface. The same grid and uv2 as the water, so
    // it shares the water's geometry instead of building a second one.
    const whitecaps = new Mesh("Whitecaps", rendererService.getScene());
    water.geometry!.applyToMesh(whitecaps);
    whitecaps.material = whitecapMaterial(rendererService.getScene());
    whitecaps.parent = container;
    whitecaps.position.x = water.position.x;
    whitecaps.position.y = 0.02;
    whitecaps.position.z = water.position.z;
    whitecaps.isPickable = false;
    container.getChildren().push(whitecaps);

    return {
      waterMesh: water,
      whitecapMesh: whitecaps,
      dispose: () => {
        // Nothing of the tile's own - see WaterTileResources
      }
    };
  }

  public static updateWaterUV2(waterMesh: Mesh, positions: number[]): void {
    const xCount = (BabylonTerrainTileImpl.NODE_X_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    const yCount = (BabylonTerrainTileImpl.NODE_Y_COUNT / BabylonTerrainTileImpl.NODE_SIZE) + 1;
    const uv2GroundHeightMap: number[] = [];

    for (let y = 0; y < yCount; y++) {
      for (let x = 0; x < xCount; x++) {
        const invertedY = xCount - y - 1;
        const index2 = (x + invertedY * xCount) * 3;
        const invertedGroundHeight = positions[index2 + 1];
        uv2GroundHeightMap.push(invertedGroundHeight, 0);
      }
    }

    BabylonTerrainTileImpl.computeShoreDirections(uv2GroundHeightMap, xCount, yCount);
    waterMesh.setVerticesData(VertexBuffer.UV2Kind, uv2GroundHeightMap);
  }

}
