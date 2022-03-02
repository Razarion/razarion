import { SlopeGeometry, TerrainTile, ThreeJsTerrainTile } from "src/app/gwtangular/GwtAngularFacade";
import { BufferAttribute, BufferGeometry, Mesh, MeshBasicMaterial, Scene } from "three";

export class ThreeJsTerrainTileImpl implements ThreeJsTerrainTile {
    private scene = new Scene();

    constructor(terrainTile: TerrainTile, private parentScene: Scene) {
        if (terrainTile.getGroundTerrainTiles() !== null) {
            terrainTile.getGroundTerrainTiles().forEach(groundTerrainTile => {
                let geometry = new BufferGeometry();
                geometry.setAttribute('position', new BufferAttribute(groundTerrainTile.positions, 3));
                geometry.setAttribute('norm', new BufferAttribute(groundTerrainTile.norms, 3));
                const material = new MeshBasicMaterial({ color: 0x00ff00 });
                material.wireframe = true;
                const cube = new Mesh(geometry, material);
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
        this.scene.add(cube);
    }

    addToScene(): void {
        this.parentScene.add(this.scene);
    }

    removeFromScene(): void {
        this.parentScene.remove(this.scene);
    }

}
