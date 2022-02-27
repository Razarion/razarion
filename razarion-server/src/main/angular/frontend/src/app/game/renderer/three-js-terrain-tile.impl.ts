import { TerrainTile, ThreeJsTerrainTile } from "src/app/gwtangular/GwtAngularFacade";
import { BufferAttribute, BufferGeometry, Mesh, MeshBasicMaterial, Scene } from "three";

export class ThreeJsTerrainTileImpl implements ThreeJsTerrainTile {
    private scene = new Scene();

    constructor(terrainTile: TerrainTile, private parentScene: Scene) {
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

    addToScene(): void {
        this.parentScene.add(this.scene);
    }

    removeFromScene(): void {
        this.parentScene.remove(this.scene);
    }

}
