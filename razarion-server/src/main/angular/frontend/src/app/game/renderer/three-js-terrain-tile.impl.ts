import { SlopeGeometry, TerrainTile, ThreeJsTerrainTile } from "src/app/gwtangular/GwtAngularFacade";
import { GwtAngularService } from "src/app/gwtangular/GwtAngularService";
import { BufferAttribute, BufferGeometry, Group, Matrix4, Mesh, MeshBasicMaterial, Scene } from "three";
import { ThreeJsModelService } from "./three-js-model.service";

export class ThreeJsTerrainTileImpl implements ThreeJsTerrainTile {
    private group = new Group();

    constructor(terrainTile: TerrainTile, private parentScene: Scene, gwtAngularService: GwtAngularService, threeJsModelService: ThreeJsModelService) {
        this.group.name = `TerrainTile ${terrainTile.getIndex().toString()}`;
        if (terrainTile.getGroundTerrainTiles() !== null) {
            terrainTile.getGroundTerrainTiles().forEach(groundTerrainTile => {
                let groundConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getGroundConfig(groundTerrainTile.groundConfigId);
                // Geometry
                let geometry = new BufferGeometry();
                geometry.setAttribute('position', new BufferAttribute(groundTerrainTile.positions, 3));
                geometry.setAttribute('normal', new BufferAttribute(groundTerrainTile.norms, 3));
                let uvs = new Float32Array(groundTerrainTile.positions.length * 2 / 3);
                let uvCount = uvs.length / 2;
                for (let uvIndex = 0; uvIndex < uvCount; uvIndex++) {
                    uvs[uvIndex * 2] = groundTerrainTile.positions[uvIndex * 3];
                    uvs[uvIndex * 2 + 1] = groundTerrainTile.positions[uvIndex * 3 + 1];
                }
                geometry.setAttribute('uv', new BufferAttribute(uvs, 2));
                const material = threeJsModelService.getMaterial(groundConfig.getTopThreeJsMaterial());
                const cube = new Mesh(geometry, material);
                cube.name = "Ground"
                cube.receiveShadow = true;
                this.group.add(cube);
            });
        }
        if (terrainTile.getTerrainSlopeTiles() !== null) {
            terrainTile.getTerrainSlopeTiles().forEach(terrainSlopeTile => {
                if (terrainSlopeTile.outerSlopeGeometry !== null && terrainSlopeTile.outerSlopeGeometry !== undefined) {
                    this.setupSlopeGeometry(terrainSlopeTile.outerSlopeGeometry);
                }
                if (terrainSlopeTile.centerSlopeGeometry !== null && terrainSlopeTile.centerSlopeGeometry !== undefined) {
                    this.setupSlopeGeometry(terrainSlopeTile.centerSlopeGeometry);
                }
                if (terrainSlopeTile.innerSlopeGeometry !== null && terrainSlopeTile.innerSlopeGeometry !== undefined) {
                    this.setupSlopeGeometry(terrainSlopeTile.innerSlopeGeometry);
                }
            });
        }
        if (terrainTile.getTerrainWaterTiles() !== null) {
            terrainTile.getTerrainWaterTiles().forEach(terrainWaterTile => {
                if (terrainWaterTile.positions !== null && terrainWaterTile.positions !== undefined) {
                    this.setupWater(terrainWaterTile.positions);
                }
                if (terrainWaterTile.shallowPositions !== null && terrainWaterTile.shallowPositions !== undefined) {
                    this.setupShallowWater(terrainWaterTile.shallowPositions, terrainWaterTile.shallowUvs);
                }
            });
        }
        if (terrainTile.getTerrainTileObjectLists() !== null) {
            const _this = this;
            terrainTile.getTerrainTileObjectLists().forEach(terrainTileObjectList => {
                try {
                    let terrainObjectConfig = gwtAngularService.gwtAngularFacade.terrainTypeService.getTerrainObjectConfig(terrainTileObjectList.terrainObjectConfigId);
                    if (terrainObjectConfig.getThreeJsUuid() === undefined) {
                        throw new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
                    }
                    terrainTileObjectList.models.forEach(model => {
                        let m = model.getColumnMajorFloat32Array();
                        let matrix4 = new Matrix4();
                        matrix4.set(
                            m[0], m[4], m[8], m[12],
                            m[1], m[5], m[9], m[13],
                            m[2], m[6], m[10], m[14],
                            m[3], m[7], m[11], m[15]
                        );
                        let threeJsModel = threeJsModelService.cloneObject3D(terrainObjectConfig.getThreeJsUuid());
                        threeJsModel.applyMatrix4(matrix4);
                        // threeJsModel.position.x = 100;

                        // object3D.traverse((innerObject3D: Object3D) => {
                            // innerObject3D.matrixAutoUpdate = true;
                        // });
                        // object3D.traverse((innerObject3D: Object3D) => {
                        //     if (innerObject3D.type === 'SkinnedMesh') {
                        //         console.info(innerObject3D.type)
                        //         // object3D.position.x = 10;
                        //         // object3D.position.y = 20;
                        //         innerObject3D.scale.x = 10;
                        //         innerObject3D.scale.y = 10;
                        //         innerObject3D.scale.z = 10;
                        //         innerObject3D.translateX(20);
                        //         innerObject3D.updateMatrix();
                        //         innerObject3D.updateWorldMatrix(true, true);
                        //         // innerObject3D.applyMatrix4(matrix4);
                        //     }
                        // });

                        _this.group.add(threeJsModel);
                    });
                } catch (error) {
                    // hrow new Error(`TerrainObjectConfig has no threeJsUuid: ${terrainObjectConfig.toString()}`);
                    console.error(terrainTileObjectList);
                    console.error(error);
                }
            });
        }
    }

    private setupSlopeGeometry(slopeGeometry: SlopeGeometry): void {
        let geometry = new BufferGeometry();
        geometry.setAttribute('position', new BufferAttribute(slopeGeometry.positions, 3));
        geometry.setAttribute('norm', new BufferAttribute(slopeGeometry.norms, 3));
        geometry.setAttribute('uvs', new BufferAttribute(slopeGeometry.uvs, 2));
        geometry.setAttribute('slopeFactors', new BufferAttribute(slopeGeometry.slopeFactors, 3));

        const material = new MeshBasicMaterial({ color: 0xAAAAAA });
        material.wireframe = true;
        const cube = new Mesh(geometry, material);
        cube.name = "Slope";
        this.group.add(cube);
    }

    private setupWater(positions: Float32Array) {
        let geometry = new BufferGeometry();
        geometry.setAttribute('position', new BufferAttribute(positions, 3));
        const material = new MeshBasicMaterial({ color: 0x0000ff });
        material.wireframe = true;
        const cube = new Mesh(geometry, material);
        cube.name = "Water";
        this.group.add(cube);
    }

    private setupShallowWater(shallowPositions: Float32Array, shallowUvs: Float32Array) {
        let geometry = new BufferGeometry();
        geometry.setAttribute('position', new BufferAttribute(shallowPositions, 3));
        geometry.setAttribute('uvs', new BufferAttribute(shallowUvs, 3));
        const material = new MeshBasicMaterial({ color: 0x5555ff });
        material.wireframe = true;
        const cube = new Mesh(geometry, material);
        cube.name = "Shallow Water";
        this.group.add(cube);
    }

    addToScene(): void {
        this.parentScene.add(this.group);
    }

    removeFromScene(): void {
        this.parentScene.remove(this.group);
    }

}
