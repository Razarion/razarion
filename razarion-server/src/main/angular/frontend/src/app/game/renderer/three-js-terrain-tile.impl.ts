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
import {
  AbstractMesh,
  Color3,
  Mesh,
  MeshBuilder,
  NodeMaterial,
  Scene,
  StandardMaterial,
  TransformNode,
  Vector3,
  VertexBuffer,
  VertexData
} from "@babylonjs/core";

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
          const ground = new Mesh("Ground", null);
          const vertexData = new VertexData();
          for (let i = 0; i < groundTerrainTile.positions.length / 3; i++) {
            const newPositionY = groundTerrainTile.positions[i * 3 + 2];
            const newPositionZ = groundTerrainTile.positions[i * 3 + 1];
            groundTerrainTile.positions[i * 3 + 1] = newPositionY;
            groundTerrainTile.positions[i * 3 + 2] = newPositionZ;
            const newNormalY = groundTerrainTile.norms[i * 3 + 2];
            const newNormalZ = groundTerrainTile.norms[i * 3 + 1];
            groundTerrainTile.norms[i * 3 + 1] = newNormalY;
            groundTerrainTile.norms[i * 3 + 2] = newNormalZ;
          }

          vertexData.positions = groundTerrainTile.positions;
          vertexData.normals = groundTerrainTile.norms;
          vertexData.indices = this.generateIndices(groundTerrainTile.positions.length);

          vertexData.applyToMesh(ground)

          let groundConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(groundTerrainTile.groundConfigId);
          if (groundConfig.getTopThreeJsMaterial()) {
              ground.material = threeJsModelService.getNodeMaterial(groundConfig.getTopThreeJsMaterial());
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
            throw new Error(`SlopeConfig has no threeJsMaterial: ${slopeConfig.getInternalName()} (${slopeConfig.getId()})`);
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
          // throw new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
          console.error(terrainTileObjectList);
          console.error(error);
        }
      });
    }
  }

  private evalGroundMaterial(slopeConfig: SlopeConfig | null): NodeMaterial {
    if (slopeConfig && slopeConfig.getGroundConfigId()) {
      let innerGroundConfigMaterialId = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(slopeConfig.getGroundConfigId()).getTopThreeJsMaterial();
      return this.threeJsModelService.getNodeMaterial(innerGroundConfigMaterialId);
    } else {
      let innerGroundConfigMaterialId = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(this.defaultGroundConfigId).getTopThreeJsMaterial();
      return this.threeJsModelService.getNodeMaterial(innerGroundConfigMaterialId);
    }
  }

  private addErrorMaterial(mesh: Mesh) {
    const material = new StandardMaterial("Error Material");
    material.diffuseColor = new Color3(1, 0, 0);
    material.emissiveColor = new Color3(1, 0, 0);
    material.specularColor = new Color3(1, 0, 0);
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

  static fillVec3(vec: Vector3, length: number): any {
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

  private setupSlopeGeometry(slopeConfig: SlopeConfig, slopeGeometry: SlopeGeometry, material: NodeMaterial, groundMaterial: NodeMaterial | null, splatting: SlopeSplattingConfig | null): void {
    if (groundMaterial && splatting) {
    } else {

      for (let i = 0; i < slopeGeometry.positions.length / 3; i++) {
        const newPositionY = slopeGeometry.positions[i * 3 + 2];
        const newPositionZ = slopeGeometry.positions[i * 3 + 1];
        slopeGeometry.positions[i * 3 + 1] = newPositionY;
        slopeGeometry.positions[i * 3 + 2] = newPositionZ;
        const newNormalY = slopeGeometry.norms[i * 3 + 2];
        const newNormalZ = slopeGeometry.norms[i * 3 + 1];
        slopeGeometry.norms[i * 3 + 1] = newNormalY;
        slopeGeometry.norms[i * 3 + 2] = newNormalZ;
      }

      const slope = new Mesh(`Slope (${slopeConfig.getInternalName()}[${slopeConfig.getId()}])`, null);
      const vertexData = new VertexData();
      vertexData.positions = slopeGeometry.positions;
      vertexData.normals = slopeGeometry.norms;
      vertexData.uvs = slopeGeometry.uvs;
      vertexData.indices = this.generateIndices(slopeGeometry.positions.length);

      vertexData.applyToMesh(slope)

      slope.parent = this.container;

      slope.material = material;

      this.showNormals(slope, 1, Color3.White(), this.scene);

      this.container.getChildren().push(slope);
    }
  }

  showNormals(mesh: AbstractMesh, size: number, color: Color3, scene: Scene) {
    const normals: any = mesh.getVerticesData(VertexBuffer.NormalKind);
    const positions: any = mesh.getVerticesData(VertexBuffer.PositionKind);
    color = color || Color3.White();
    size = size || 1;

    var lines = [];
    for (var i = 0; i < normals.length; i += 3) {
      var v1 = Vector3.FromArray(positions, i);
      var v2 = v1.add(Vector3.FromArray(normals, i).scaleInPlace(size));
      lines.push([v1.add(mesh.position), v2.add(mesh.position)]);
    }
    const normalLines = MeshBuilder.CreateLineSystem("normalLines", {lines: lines}, scene);
    normalLines.color = color;
    return normalLines;
  }

  private generateIndices(positionCount: number): number[] {
    const indices = [];
    for (let i = 0; i < positionCount / 3; i++) {
      indices[i] = i;
    }
    return indices;
  }
}
