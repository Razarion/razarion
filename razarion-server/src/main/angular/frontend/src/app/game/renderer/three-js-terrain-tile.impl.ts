import {
  SlopeConfig,
  SlopeGeometry,
  SlopeSplattingConfig,
  TerrainTile,
  ThreeJsTerrainTile
} from "src/app/gwtangular/GwtAngularFacade";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {
  BufferAttribute,
  BufferGeometry,
  Group,
  Material,
  Matrix4,
  Mesh,
  MeshBasicMaterial,
  Scene,
  Vector3
} from "three";
import {ThreeJsModelService} from "./three-js-model.service";
import {ThreeJsWaterRenderService} from "./three-js-water-render.service";

export class ThreeJsTerrainTileImpl implements ThreeJsTerrainTile {
  private group = new Group();

  constructor(terrainTile: TerrainTile,
              private defaultGroundConfigId: number,
              private scene: Scene,
              private gwtAngularService: GwtAngularService,
              private threeJsModelService: ThreeJsModelService,
              private threeJsWaterRenderService: ThreeJsWaterRenderService) {
    this.group.name = `TerrainTile ${terrainTile.getIndex().toString()}`;

    if (terrainTile.getGroundTerrainTiles() !== null) {
      terrainTile.getGroundTerrainTiles().forEach(groundTerrainTile => {
        let groundConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(groundTerrainTile.groundConfigId);
        // Geometry
        let geometry = new BufferGeometry();
        geometry.setAttribute('position', new BufferAttribute(groundTerrainTile.positions, 3));
        geometry.setAttribute('normal', new BufferAttribute(groundTerrainTile.norms, 3));
        geometry.setAttribute('uv', ThreeJsTerrainTileImpl.uvFromPosition(groundTerrainTile.positions));
        let material;
        if (groundConfig.getTopThreeJsMaterial() === undefined || groundConfig.getTopThreeJsMaterial() == null) {
          material = new MeshBasicMaterial({color: 0x11EE11});
          material.wireframe = true;
          console.warn(`No top material in GroundConfig ${groundConfig.getInternalName()} '${groundConfig.getId()}'`);
        } else {
          material = threeJsModelService.getMaterial(groundConfig.getTopThreeJsMaterial());
        }
        const ground = new Mesh(geometry, material);
        ground.name = "Ground"
        ground.receiveShadow = true;
        this.group.add(ground);
      });
    }

    if (terrainTile.getTerrainSlopeTiles() !== null) {
      const _this = this;
      terrainTile.getTerrainSlopeTiles().forEach(terrainSlopeTile => {
        try {
          let slopeConfig = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getSlopeConfig(terrainSlopeTile.slopeConfigId);
          if (slopeConfig.getThreeJsMaterial() === undefined) {
            throw new Error(`SlopeConfig has no threeJsMaterial: ${slopeConfig.toString()}`);
          }
          if (terrainSlopeTile.outerSlopeGeometry !== null && terrainSlopeTile.outerSlopeGeometry !== undefined) {
            this.setupSlopeGeometry(slopeConfig,
              terrainSlopeTile.outerSlopeGeometry,
              threeJsModelService.getNodesMaterial(slopeConfig.getThreeJsMaterial()),
              _this.evalGroundMaterial(null),
              slopeConfig.getOuterSlopeSplattingConfig());
          }
          if (terrainSlopeTile.centerSlopeGeometry !== null && terrainSlopeTile.centerSlopeGeometry !== undefined) {
            this.setupSlopeGeometry(slopeConfig,
              terrainSlopeTile.centerSlopeGeometry,
              threeJsModelService.getNodesMaterial(slopeConfig.getThreeJsMaterial()),
              null,
              null);
          }
          if (terrainSlopeTile.innerSlopeGeometry !== null && terrainSlopeTile.innerSlopeGeometry !== undefined) {
            this.setupSlopeGeometry(slopeConfig,
              terrainSlopeTile.innerSlopeGeometry,
              threeJsModelService.getNodesMaterial(slopeConfig.getThreeJsMaterial()),
              _this.evalGroundMaterial(slopeConfig),
              slopeConfig.getInnerSlopeSplattingConfig());
          }
        } catch (error) {
          // throw new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
          console.error(error);
        }
      });
    }

    this.threeJsWaterRenderService.setup(terrainTile.getTerrainWaterTiles(), this.group);

    if (terrainTile.getTerrainTileObjectLists() !== null) {
      const _this = this;
      terrainTile.getTerrainTileObjectLists().forEach(terrainTileObjectList => {
        try {
          let terrainObjectConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(terrainTileObjectList.terrainObjectConfigId);
          if (!terrainObjectConfig.getThreeJsModelPackConfigId()) {
            throw new Error(`TerrainObjectConfig has no threeJsModelPackConfigId: ${terrainObjectConfig.toString()}`);
          }
          terrainTileObjectList.terrainObjectModels.forEach(terrainObjectModel => {
            let m = terrainObjectModel.model.getColumnMajorFloat32Array();
            let matrix4 = new Matrix4();
            matrix4.set(
              m[0], m[4], m[8], m[12],
              m[1], m[5], m[9], m[13],
              m[2], m[6], m[10], m[14],
              m[3], m[7], m[11], m[15]
            );
            let terrainObject = new Group();
            (<any>terrainObject).razarionTerrainObjectId = terrainObjectModel.terrainObjectId;
            (<any>terrainObject).razarionTerrainObjectConfigId = terrainTileObjectList.terrainObjectConfigId;
            terrainObject.name = `Terrain Object  ${terrainObjectModel.terrainObjectId}`;
            terrainObject.applyMatrix4(matrix4);
            _this.group.add(terrainObject);
            let threeJsModel = threeJsModelService.cloneObject3D(terrainObjectConfig.getThreeJsModelPackConfigId());
            terrainObject.add(threeJsModel)
          });
        } catch (error) {
          // throw new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
          console.error(terrainTileObjectList);
          console.error(error);
        }
      });
    }
  }

  static uvFromPosition(positions: Float32Array) {
    let uvs = new Float32Array(positions.length * 2 / 3);
    let uvCount = uvs.length / 2;
    for (let uvIndex = 0; uvIndex < uvCount; uvIndex++) {
      uvs[uvIndex * 2] = positions[uvIndex * 3];
      uvs[uvIndex * 2 + 1] = positions[uvIndex * 3 + 1];
    }
    return new BufferAttribute(uvs, 2);
  }

  static fillVec3(vec: Vector3, length: number): BufferAttribute {
    let float32Array = new Float32Array(length);
    for (let i = 0; i < length / 3; i++) {
      float32Array[i * 3] = vec.x;
      float32Array[i * 3 + 1] = vec.y;
      float32Array[i * 3 + 2] = vec.z;
    }
    return new BufferAttribute(float32Array, 3);
  }

  addToScene(): void {
    this.scene.add(this.group);
  }

  removeFromScene(): void {
    this.scene.remove(this.group);
  }

  private evalGroundMaterial(slopeConfig: SlopeConfig | null): Material {
    if (slopeConfig && slopeConfig.getGroundConfigId()) {
      let innerGroundConfigMaterialId = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(slopeConfig.getGroundConfigId()).getTopThreeJsMaterial();
      return this.threeJsModelService.getMaterial(innerGroundConfigMaterialId);
    } else {
      let innerGroundConfigMaterialId = this.gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(this.defaultGroundConfigId).getTopThreeJsMaterial();
      return this.threeJsModelService.getMaterial(innerGroundConfigMaterialId);
    }
  }

  private setupSlopeGeometry(slopeConfig: SlopeConfig, slopeGeometry: SlopeGeometry, material: Material, groundMaterial: Material | null, splatting: SlopeSplattingConfig | null): void {
    if (groundMaterial && splatting) {
    } else {
      let geometry = new BufferGeometry();
      geometry.setAttribute('position', new BufferAttribute(slopeGeometry.positions, 3));
      geometry.setAttribute('normal', new BufferAttribute(slopeGeometry.norms, 3));
      geometry.setAttribute('uv', new BufferAttribute(slopeGeometry.uvs, 2));
      let slope = new Mesh(geometry, material);
      slope.name = `Slope (${slopeConfig.getInternalName()}[${slopeConfig.getId()}])`;
      slope.receiveShadow = true;
      this.group.add(slope);
    }
  }
}
