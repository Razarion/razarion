import { SlopeGeometry, TerrainTile, ThreeJsTerrainTile } from "src/app/gwtangular/GwtAngularFacade";
import { BoxGeometry, BufferAttribute, BufferGeometry, Matrix4, Mesh, MeshBasicMaterial, Object3D, Scene } from "three";

export class ThreeJsTerrainTileImpl implements ThreeJsTerrainTile {
    private scene = new Scene();

    constructor(terrainTile: TerrainTile, private parentScene: Scene, threejsObject3D: Object3D) {
        this.scene.name = "TerrainTile";
        if (terrainTile.getGroundTerrainTiles() !== null) {
            terrainTile.getGroundTerrainTiles().forEach(groundTerrainTile => {
                let geometry = new BufferGeometry();
                geometry.setAttribute('position', new BufferAttribute(groundTerrainTile.positions, 3));
                geometry.setAttribute('norm', new BufferAttribute(groundTerrainTile.norms, 3));
                const material = new MeshBasicMaterial({ color: 0x00ff00 });
                material.wireframe = true;
                const cube = new Mesh(geometry, material);
                cube.name = "Ground"
                this.scene.add(cube);
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
            terrainTile.getTerrainTileObjectLists().forEach(terrainTileObjectList => {
                terrainTileObjectList.models.forEach(model => {
                    let m = model.getColumnMajorFloat32Array();
                    let matrix4 = new Matrix4();
                    matrix4.set(
                        m[0], m[4], m[8], m[12],
                        m[1], m[5], m[9], m[13],
                        m[2], m[6], m[10], m[14],
                        m[3], m[7], m[11], m[15]
                    );
                    let object3D = threejsObject3D.clone();
                    object3D.name = "Terrain Object"
                    object3D.applyMatrix4(matrix4);
                    this.scene.add(object3D);
                });
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
        this.scene.add(cube);
    }

    private setupWater(positions: Float32Array) {
        let geometry = new BufferGeometry();
        geometry.setAttribute('position', new BufferAttribute(positions, 3));
        const material = new MeshBasicMaterial({ color: 0x0000ff });
        material.wireframe = true;
        const cube = new Mesh(geometry, material);
        cube.name = "Water";
        this.scene.add(cube);
    }

    private setupShallowWater(shallowPositions: Float32Array, shallowUvs: Float32Array) {
        let geometry = new BufferGeometry();
        geometry.setAttribute('position', new BufferAttribute(shallowPositions, 3));
        geometry.setAttribute('uvs', new BufferAttribute(shallowUvs, 3));
        const material = new MeshBasicMaterial({ color: 0x5555ff });
        material.wireframe = true;
        const cube = new Mesh(geometry, material);
        cube.name = "Shallow Water";
        this.scene.add(cube);
    }

    addToScene(): void {
        this.parentScene.add(this.scene);
    }

    removeFromScene(): void {
        this.parentScene.remove(this.scene);
    }

}
