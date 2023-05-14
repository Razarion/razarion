import {
  SlopeConfig,
  SlopeGeometry,
  TerrainTile,
  ThreeJsTerrainTile
} from "src/app/gwtangular/GwtAngularFacade";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {BabylonModelService} from "./babylon-model.service";
import {ThreeJsWaterRenderService} from "./three-js-water-render.service";
import {Mesh, NodeMaterial, TransformNode} from "@babylonjs/core";
import {ThreeJsRendererServiceImpl} from "./three-js-renderer-service.impl";
import {BabylonJsUtils} from "./babylon-js.utils";

export class ThreeJsTerrainTileImpl implements ThreeJsTerrainTile {
  private readonly container: Mesh;

  constructor(terrainTile: TerrainTile,
              private defaultGroundConfigId: number,
              private gwtAngularService: GwtAngularService,
              private rendererService: ThreeJsRendererServiceImpl,
              private threeJsModelService: BabylonModelService,
              private threeJsWaterRenderService: ThreeJsWaterRenderService) {
    this.container = new Mesh(`Terrain Tile ${terrainTile.getIndex().toString()}`);
    if (terrainTile.getGroundTerrainTiles() !== null) {
      terrainTile.getGroundTerrainTiles().forEach(groundTerrainTile => {
        try {
          const vertexData = BabylonJsUtils.createVertexData(groundTerrainTile.positions, groundTerrainTile.norms);
          const ground = new Mesh("Ground", null);
          vertexData.applyToMesh(ground)

          let groundConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(groundTerrainTile.groundConfigId);
          if (groundConfig.getTopThreeJsMaterial()) {
            ground.material = threeJsModelService.getNodeMaterial(groundConfig.getTopThreeJsMaterial());
          } else {
            ground.material = BabylonJsUtils.createErrorMaterial(`No top or bottom material in GroundConfig ${groundConfig.getInternalName()} '${groundConfig.getId()}'`);
          }
          ground.receiveShadows = true;
          ground.parent = this.container;
          this.container.getChildren().push(ground);
        } catch (error) {
          console.error(error);
        }
      });
    }

    if (terrainTile.getTerrainSlopeTiles() !== null) {
      terrainTile.getTerrainSlopeTiles().forEach(terrainSlopeTile => {
        try {
          let slopeConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getSlopeConfig(terrainSlopeTile.slopeConfigId);
          if (terrainSlopeTile.centerSlopeGeometry) {
            this.setupSlopeGeometry(slopeConfig,
              terrainSlopeTile.centerSlopeGeometry,
              threeJsModelService.getNodeMaterialNull(slopeConfig.getThreeJsMaterial(), `SlopeConfig has no threeJsMaterial: ${slopeConfig.getInternalName()} (${slopeConfig.getId()})`));
          }
        } catch (error) {
          // throw new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
          console.error(error);
        }
      });
    }

    this.threeJsWaterRenderService.setup(terrainTile.getTerrainWaterTiles(), this.container);

    if (terrainTile.getTerrainTileObjectLists() !== null) {
      terrainTile.getTerrainTileObjectLists().forEach(terrainTileObjectList => {
        try {
          let terrainObjectConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(terrainTileObjectList.terrainObjectConfigId);
          if (!terrainObjectConfig.getThreeJsModelPackConfigId()) {
            throw new Error(`TerrainObjectConfig has no threeJsModelPackConfigId: ${terrainObjectConfig.toString()}`);
          }
          terrainTileObjectList.terrainObjectModels.forEach(terrainObjectModel => {
            try {
              const terrainObjectModelTransform = new TransformNode(`TerrainObjectModel (${terrainObjectModel.terrainObjectId})`);
              terrainObjectModelTransform.parent = this.container;
              this.container.getChildren().push(terrainObjectModelTransform);
              terrainObjectModelTransform.position.set(
                terrainObjectModel.position.getX(),
                terrainObjectModel.position.getZ(),
                terrainObjectModel.position.getY());
              if (terrainObjectModel.scale) {
                terrainObjectModelTransform.scaling.set(
                  terrainObjectModel.scale.getX(),
                  terrainObjectModel.scale.getZ(),
                  terrainObjectModel.scale.getY());
              }
              if (terrainObjectModel.rotation) {
                terrainObjectModelTransform.rotationQuaternion = null;
                terrainObjectModelTransform.rotation.set(
                  terrainObjectModel.rotation.getX(),
                  terrainObjectModel.rotation.getZ(),
                  terrainObjectModel.rotation.getY());
              }
              let terrainObjectMesh: Mesh = <Mesh>threeJsModelService.cloneMesh(terrainObjectConfig.getThreeJsModelPackConfigId(), terrainObjectModelTransform);
              terrainObjectMesh.name = `TerrainObject '${terrainObjectConfig.getInternalName()} (${terrainObjectConfig.getId()})'`;
              terrainObjectMesh.parent = terrainObjectModelTransform;
            } catch (error) {
              console.error(error);
            }
          });
        } catch (error) {
          console.error(terrainTileObjectList);
          console.error(error);
        }
      });
    }
  }

  addToScene(): void {
    this.rendererService.addToScene(this.container);
  }

  removeFromScene(): void {
    this.rendererService.removeFromScene(this.container);
  }

  private setupSlopeGeometry(slopeConfig: SlopeConfig, slopeGeometry: SlopeGeometry, material: NodeMaterial): void {
    const slope = new Mesh(`Slope (${slopeConfig.getInternalName()}[${slopeConfig.getId()}])`, null);
    const vertexData = BabylonJsUtils.createVertexData(slopeGeometry.positions, slopeGeometry.norms);
    vertexData.uvs = slopeGeometry.uvs;
    vertexData.uvs2 = this.convertFloatTOVec2(slopeGeometry.slopeFactors);
    vertexData.applyToMesh(slope)

    slope.parent = this.container;
    slope.material = material;
    slope.receiveShadows = true;

    this.container.getChildren().push(slope);
  }

  private convertFloatTOVec2(vec1: Float32Array) {
    let vec2Array = new Float32Array(vec1.length * 2);
    for (let i = 0; i < vec1.length; i++) {
      vec2Array[i * 2] = vec1[i];
    }
    return vec2Array;
  }
}
