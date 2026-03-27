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

@Injectable({
  providedIn: 'root'
})
export class BabylonWaterRenderService {
  constructor(private babylonModelService: BabylonModelService) {
  }

  public setup(index: Index, groundConfig: GroundConfig, container: TransformNode, uv2GroundHeightMap: FloatArray, rendererService: BabylonRenderServiceAccessImpl): Mesh {
    const water = MeshBuilder.CreateGround("Water", {
      width: BabylonTerrainTileImpl.NODE_X_COUNT,
      height: BabylonTerrainTileImpl.NODE_Y_COUNT,
      subdivisions: 160
    });

    water.material = this.babylonModelService.getBabylonMaterial(groundConfig.getWaterBabylonMaterialId());
    (<NodeMaterial>water.material).ignoreAlpha = false; // Can not be saved in the NodeEditor
    (<ReflectionTextureBaseBlock>(<NodeMaterial>water.material).getBlockByName("Reflection")).texture = CubeTexture.CreateFromImages([
      "renderer/env/clouds.jpg", // +X
      "renderer/env/clouds.jpg", // +Y
      "renderer/env/clouds.jpg", // +Z
      "renderer/env/clouds.jpg", // -X
      "renderer/env/clouds.jpg", // -Y
      "renderer/env/clouds.jpg", // -Z
    ], rendererService.getScene());

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
    container.getChildren().push(whitecaps);

    return water;
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
