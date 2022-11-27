import {
  SlopeConfig,
  SlopeGeometry,
  SlopeSplattingConfig,
  TerrainTile,
  ThreeJsTerrainTile
} from "src/app/gwtangular/GwtAngularFacade";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {BabylonModelService} from "./babylon-model.service";
import {ThreeJsWaterRenderService} from "./three-js-water-render.service";
import * as BABYLON from 'babylonjs';
import {Scene} from 'babylonjs';
import Mesh = BABYLON.Mesh;
import TransformNode = BABYLON.TransformNode;

export class ThreeJsTerrainTileImpl implements ThreeJsTerrainTile {
  private readonly container: Mesh;

  constructor(terrainTile: TerrainTile,
              private defaultGroundConfigId: number,
              private scene: Scene,
              private gwtAngularService: GwtAngularService,
              private threeJsModelService: BabylonModelService,
              private threeJsWaterRenderService: ThreeJsWaterRenderService) {
    this.container = new Mesh(`Terrain Tile ${terrainTile.getIndex().toString()}`);
    if (terrainTile.getGroundTerrainTiles() !== null) {
      terrainTile.getGroundTerrainTiles().forEach(groundTerrainTile => {
        try {
          const ground = new BABYLON.Mesh("Ground", null);
          const vertexData = new BABYLON.VertexData();
          const indices = [];
          for (let i = 0; i < groundTerrainTile.positions.length / 3; i++) {
            indices[i] = i;
          }
          vertexData.positions = groundTerrainTile.positions;
          vertexData.normals = groundTerrainTile.norms;
          vertexData.indices = indices;

          vertexData.applyToMesh(ground)

          let groundConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(groundTerrainTile.groundConfigId);
          if (groundConfig.getTopThreeJsMaterial()) {
              ground.material = threeJsModelService.getNodeMaterial(groundConfig.getTopThreeJsMaterial());
              ground.material.backFaceCulling = false; // Camera looking in negative z direction. https://doc.babylonjs.com/features/featuresDeepDive/mesh/creation/custom/custom#visibility
          } else {
            this.addErrorMaterial(ground);
            console.warn(`No top or bottom material in GroundConfig ${groundConfig.getInternalName()} '${groundConfig.getId()}'`);
          }
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
          if (slopeConfig.getThreeJsMaterial() === undefined) {
            throw new Error(`SlopeConfig has no threeJsMaterial: ${slopeConfig.toString()}`);
          }
          let material = threeJsModelService.getNodeMaterial(slopeConfig.getThreeJsMaterial());
          if (terrainSlopeTile.outerSlopeGeometry !== null && terrainSlopeTile.outerSlopeGeometry !== undefined) {
            this.setupSlopeGeometry(slopeConfig,
              terrainSlopeTile.outerSlopeGeometry,
              material,
              this.evalGroundMaterial(null),
              slopeConfig.getOuterSlopeSplattingConfig());
          }
          if (terrainSlopeTile.centerSlopeGeometry !== null && terrainSlopeTile.centerSlopeGeometry !== undefined) {
            this.setupSlopeGeometry(slopeConfig,
              terrainSlopeTile.centerSlopeGeometry,
              material,
              null,
              null);
          }
          if (terrainSlopeTile.innerSlopeGeometry !== null && terrainSlopeTile.innerSlopeGeometry !== undefined) {
            this.setupSlopeGeometry(slopeConfig,
              terrainSlopeTile.innerSlopeGeometry,
              material,
              this.evalGroundMaterial(slopeConfig),
              slopeConfig.getInnerSlopeSplattingConfig());
          }
        } catch (error) {
          // throw new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
          console.error(error);
        }
      });
    }

    // TODO this.threeJsWaterRenderService.setup(terrainTile.getTerrainWaterTiles(), this.container);

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
                terrainObjectModel.position.getY(),
                terrainObjectModel.position.getZ());
              if (terrainObjectModel.scale) {
                terrainObjectModelTransform.scaling.set(
                  terrainObjectModel.scale.getX(),
                  terrainObjectModel.scale.getY(),
                  terrainObjectModel.scale.getZ());
              }
              if (terrainObjectModel.rotation) {
                terrainObjectModelTransform.rotationQuaternion = null;
                terrainObjectModelTransform.rotation.set(
                  terrainObjectModel.rotation.getX(),
                  terrainObjectModel.rotation.getY(),
                  terrainObjectModel.rotation.getZ());
              }
              let terrainObjectMesh: Mesh = <Mesh>threeJsModelService.cloneMesh(terrainObjectConfig.getThreeJsModelPackConfigId(), terrainObjectModelTransform);
              terrainObjectMesh.name = `TerrainObject '${terrainObjectConfig.getInternalName()} (${terrainObjectConfig.getId()})'`;
              terrainObjectMesh.parent = terrainObjectModelTransform;
            } catch (error) {
              console.error(error);
            }
          });
        } catch (error) {
          // throw new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
          console.error(terrainTileObjectList);
          console.error(error);
        }
      });
    }
  }

  private evalGroundMaterial(slopeConfig: SlopeConfig | null): BABYLON.NodeMaterial {
    if (slopeConfig && slopeConfig.getGroundConfigId()) {
      let innerGroundConfigMaterialId = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(slopeConfig.getGroundConfigId()).getTopThreeJsMaterial();
      return this.threeJsModelService.getNodeMaterial(innerGroundConfigMaterialId);
    } else {
      let innerGroundConfigMaterialId = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(this.defaultGroundConfigId).getTopThreeJsMaterial();
      return this.threeJsModelService.getNodeMaterial(innerGroundConfigMaterialId);
    }
  }

  private addErrorMaterial(mesh: BABYLON.Mesh) {
    const material = new BABYLON.StandardMaterial("Error Material");
    material.diffuseColor = new BABYLON.Color3(1, 0, 0);
    material.emissiveColor = new BABYLON.Color3(1, 0, 0);
    material.specularColor = new BABYLON.Color3(1, 0, 0);
    material.backFaceCulling = false; // Camera looking in negative z direction. https://doc.babylonjs.com/features/featuresDeepDive/mesh/creation/custom/custom#visibility
    mesh.material = material;
  }

  static uvFromPosition(positions: Float32Array) {
    // let uvs = new Float32Array(positions.length * 2 / 3);
    // let uvCount = uvs.length / 2;
    // for (let uvIndex = 0; uvIndex < uvCount; uvIndex++) {
    //   uvs[uvIndex * 2] = positions[uvIndex * 3];
    //   uvs[uvIndex * 2 + 1] = positions[uvIndex * 3 + 1];
    // }
    // return new BufferAttribute(uvs, 2);
  }

  static fillVec3(vec: BABYLON.Vector3, length: number): any {
    // let float32Array = new Float32Array(length);
    // for (let i = 0; i < length / 3; i++) {
    //   float32Array[i * 3] = vec.x;
    //   float32Array[i * 3 + 1] = vec.y;
    //   float32Array[i * 3 + 2] = vec.z;
    // }
    // return new BufferAttribute(float32Array, 3);
  }

  addToScene(): void {
    this.scene.addMesh(this.container);
  }

  removeFromScene(): void {
    this.scene.removeMesh(this.container);
  }

  private setupSlopeGeometry(slopeConfig: SlopeConfig, slopeGeometry: SlopeGeometry, material: BABYLON.NodeMaterial, groundMaterial: BABYLON.NodeMaterial | null, splatting: SlopeSplattingConfig | null): void {
    if (groundMaterial && splatting) {
    } else {
      const slope = new BABYLON.Mesh(`Slope (${slopeConfig.getInternalName()}[${slopeConfig.getId()}])`, null);
      const vertexData = new BABYLON.VertexData();
      const indices = [];
      for (let i = 0; i < slopeGeometry.positions.length / 3; i++) {
        indices[i] = i;
      }
      vertexData.positions = slopeGeometry.positions;
      vertexData.normals = slopeGeometry.norms;
      vertexData.uvs = slopeGeometry.uvs;
      vertexData.indices = indices;

      vertexData.applyToMesh(slope)

      slope.parent = this.container;

      slope.material = material;
      slope.material.backFaceCulling = false; // Camera looking in negative z direction. https://doc.babylonjs.com/features/featuresDeepDive/mesh/creation/custom/custom#visibility

      this.container.getChildren().push(slope);
    }
  }
}
