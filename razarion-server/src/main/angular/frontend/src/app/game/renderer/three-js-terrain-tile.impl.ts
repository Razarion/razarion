import {
  SlopeConfig,
  SlopeGeometry,
  SlopeSplattingConfig,
  TerrainTile,
  ThreeJsTerrainTile
} from "src/app/gwtangular/GwtAngularFacade";
import {GwtAngularService} from "src/app/gwtangular/GwtAngularService";
import {ThreeJsModelService} from "./three-js-model.service";
import {ThreeJsWaterRenderService} from "./three-js-water-render.service";
import * as BABYLON from 'babylonjs';

export class ThreeJsTerrainTileImpl implements ThreeJsTerrainTile {
  constructor(terrainTile: TerrainTile,
              private defaultGroundConfigId: number,
              private container: BABYLON.AssetContainer,
              private gwtAngularService: GwtAngularService,
              private threeJsModelService: ThreeJsModelService,
              private threeJsWaterRenderService: ThreeJsWaterRenderService) {
    if (terrainTile.getGroundTerrainTiles() !== null) {
      terrainTile.getGroundTerrainTiles().forEach(groundTerrainTile => {
        // let groundConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(groundTerrainTile.groundConfigId);
        // // Geometry
        // let geometry = new BufferGeometry();
        // geometry.setAttribute('position', new BufferAttribute(groundTerrainTile.positions, 3));
        // geometry.setAttribute('normal', new BufferAttribute(groundTerrainTile.norms, 3));
        // geometry.setAttribute('uv', ThreeJsTerrainTileImpl.uvFromPosition(groundTerrainTile.positions));
        // let material;
        // if (groundConfig.getTopThreeJsMaterial() === undefined || groundConfig.getTopThreeJsMaterial() == null) {
        //   material = new MeshBasicMaterial({color: 0x11EE11});
        //   material.wireframe = true;
        //   console.warn(`No top material in GroundConfig ${groundConfig.getInternalName()} '${groundConfig.getId()}'`);
        // } else {
        //   material = threeJsModelService.getMaterial(groundConfig.getTopThreeJsMaterial());
        // }
        // const ground = new Mesh(geometry, material);
        // ground.name = "Ground"
        // ground.receiveShadow = true;
        // this.scene.add(ground);
        container.meshes.push(BABYLON.Mesh.CreateBox("Test Mesh", 20, null));
      });
    }

    if (terrainTile.getTerrainSlopeTiles() !== null) {
      const _this = this;
      terrainTile.getTerrainSlopeTiles().forEach(terrainSlopeTile => {
      });
    }

    this.threeJsWaterRenderService.setup(terrainTile.getTerrainWaterTiles(), this.container);

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
            // let matrix4 = new Matrix4();
            // matrix4.set(
            //   m[0], m[4], m[8], m[12],
            //   m[1], m[5], m[9], m[13],
            //   m[2], m[6], m[10], m[14],
            //   m[3], m[7], m[11], m[15]
            // );
            // let terrainObject = new Group();
            // (<any>terrainObject).razarionTerrainObjectId = terrainObjectModel.terrainObjectId;
            // (<any>terrainObject).razarionTerrainObjectConfigId = terrainTileObjectList.terrainObjectConfigId;
            // terrainObject.name = `Terrain Object  ${terrainObjectModel.terrainObjectId}`;
            // terrainObject.applyMatrix4(matrix4);
            // _this.scene.add(terrainObject);
            // let threeJsModel = threeJsModelService.cloneObject3D(terrainObjectConfig.getThreeJsModelPackConfigId());
            // terrainObject.add(threeJsModel)
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
    this.container.addAllToScene();
  }

  removeFromScene(): void {
    this.container.removeAllFromScene();
  }

  private setupSlopeGeometry(slopeConfig: SlopeConfig, slopeGeometry: SlopeGeometry, material: any, groundMaterial: any | null, splatting: SlopeSplattingConfig | null): void {
    // if (groundMaterial && splatting) {
    // } else {
    //   let geometry = new BufferGeometry();
    //   geometry.setAttribute('position', new BufferAttribute(slopeGeometry.positions, 3));
    //   geometry.setAttribute('normal', new BufferAttribute(slopeGeometry.norms, 3));
    //   geometry.setAttribute('uv', new BufferAttribute(slopeGeometry.uvs, 2));
    //   let slope = new Mesh(geometry, material);
    //   slope.name = `Slope (${slopeConfig.getInternalName()}[${slopeConfig.getId()}])`;
    //   slope.receiveShadow = true;
    //   this.scene.add(slope);
    // }
  }
}
