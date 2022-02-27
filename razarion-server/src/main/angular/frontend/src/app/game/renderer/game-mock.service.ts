import { Injectable } from "@angular/core";
import { GroundTerrainTile, InputService, TerrainTile, ThreeJsTerrainTile } from "src/app/gwtangular/GwtAngularFacade";
import { ThreeJsRendererServiceImpl } from "./three-js-renderer-service.impl";
import { HttpClient } from "@angular/common/http";

@Injectable()
export class GameMockService {

    inputService: InputService = new class implements InputService {
        onViewFieldChanged(bottomLeftX: number, bottomLeftY: number, bottomRightX: number, bottomRightY: number, topRightX: number, topRightY: number, topLeftX: number, topLeftY: number): void {
            console.info("onViewFieldChanged()");
        }
    };

    constructor(private http: HttpClient) {
    }

    mockTerrainTile(threeJsRendererService: ThreeJsRendererServiceImpl) {
        this.http.get<TerrainTile[]>("/gwt-mock/terrain-tiles").subscribe((terrainTileJsonArray: any[]) => {
            for (let i in terrainTileJsonArray) {
                let terrainTileJson: any = terrainTileJsonArray[i];
                const terrainTile = new class implements TerrainTile {
                    getGroundTerrainTiles(): GroundTerrainTile[] {
                        const groundTerrainTiles: GroundTerrainTile[] = [];
                        for (const [key, value] of Object.entries(terrainTileJson.groundTerrainTiles)) {
                            groundTerrainTiles.push(new class implements GroundTerrainTile {
                                groundConfigId: number = <number>(<any>value)["groundConfigId"];
                                positions: Float32Array = new Float32Array(<number>(<any>value)["positions"]);
                                norms: Float32Array = new Float32Array(<number>(<any>value)["norms"]);
                            });
                        }
                        return groundTerrainTiles;
                    };
                };
                const threeJsTerrainTile: ThreeJsTerrainTile = threeJsRendererService.createTerrainTile(terrainTile);
                threeJsTerrainTile.addToScene();
            }
        });
    }
}
