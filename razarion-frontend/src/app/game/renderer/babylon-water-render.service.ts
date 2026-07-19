import {Injectable} from "@angular/core";
import {GroundConfig, Index} from "../../gwtangular/GwtAngularFacade";
import {
  CubeTexture,
  Mesh,
  MeshBuilder,
  NodeMaterial,
  TransformNode,
  Vector3,
  VertexBuffer
} from "@babylonjs/core";
import {BabylonModelService} from "./babylon-model.service";
import {BabylonTerrainTileImpl} from "./babylon-terrain-tile.impl";
import type {FloatArray} from '@babylonjs/core/types';
import {ReflectionTextureBaseBlock} from '@babylonjs/core/Materials/Node/Blocks/Dual/reflectionTextureBaseBlock';
import {BabylonRenderServiceAccessImpl, RazarionMetadataType} from "./babylon-render-service-access-impl.service";
import {buildWhitecapMaterial} from "./whitecap-material";

/**
 * Per-tile water resources plus a dispose() that releases only the PER-TILE parts
 * (whitecap material + reflection CubeTexture). The water NodeMaterial itself is a shared
 * model material fetched from BabylonModelService and must NOT be disposed here; the water and
 * whitecap MESHES are children of the tile container and are disposed by the container.
 */
export interface WaterTileResources {
  waterMesh: Mesh;
  whitecapMesh: Mesh;
  dispose(): void;
}

@Injectable({
  providedIn: 'root'
})
export class BabylonWaterRenderService {
  constructor(private babylonModelService: BabylonModelService) {
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
    const reflectionCubeTexture = CubeTexture.CreateFromImages([
      "renderer/env/clouds.jpg", // +X
      "renderer/env/clouds.jpg", // +Y
      "renderer/env/clouds.jpg", // +Z
      "renderer/env/clouds.jpg", // -X
      "renderer/env/clouds.jpg", // -Y
      "renderer/env/clouds.jpg", // -Z
    ], rendererService.getScene());
    reflectionBlock.texture = reflectionCubeTexture;

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

    // Whitecap overlay mesh — sits just above water surface
    const whitecaps = MeshBuilder.CreateGround("Whitecaps", {
      width: BabylonTerrainTileImpl.NODE_X_COUNT,
      height: BabylonTerrainTileImpl.NODE_Y_COUNT,
      subdivisions: 160
    });
    whitecaps.material = buildWhitecapMaterial(rendererService.getScene());
    whitecaps.setVerticesData(VertexBuffer.UV2Kind, uv2GroundHeightMap);
    whitecaps.parent = container;
    whitecaps.position.x = water.position.x;
    whitecaps.position.y = 0.02;
    whitecaps.position.z = water.position.z;
    whitecaps.isPickable = false;
    container.getChildren().push(whitecaps);

    const whitecapMaterial = whitecaps.material;

    return {
      waterMesh: water,
      whitecapMesh: whitecaps,
      dispose: () => {
        // Whitecap material is built fresh per tile (buildWhitecapMaterial) — dispose it and its
        // textures; its onDispose hook removes the per-frame whitecap animation observer.
        if (whitecapMaterial) {
          whitecapMaterial.dispose(false, true);
        }
        // The reflection CubeTexture is created per tile but assigned onto the SHARED water
        // material's Reflection block, so only the most recently created tile's texture is actually
        // bound. Dispose ours unless it is still the bound one (that tile is currently visible, not
        // being evicted); disposing the bound texture would blank every water surface.
        if (reflectionCubeTexture && reflectionBlock.texture !== reflectionCubeTexture) {
          reflectionCubeTexture.dispose();
        }
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
