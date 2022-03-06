import { Injectable } from "@angular/core";
import { GroundTerrainTile, InputService, SlopeGeometry, TerrainSlopeTile, TerrainTile, TerrainWaterTile, ThreeJsTerrainTile } from "src/app/gwtangular/GwtAngularFacade";
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
        const self = this;
        this.http.get<TerrainTile[]>("/gwt-mock/terrain-tiles").subscribe((terrainTileJsonArray: any[]) => {
            for (let i in terrainTileJsonArray) {
                let terrainTileJson: any = terrainTileJsonArray[i];
                const terrainTile = new class implements TerrainTile {
                    getGroundTerrainTiles(): GroundTerrainTile[] {
                        const groundTerrainTiles: GroundTerrainTile[] = [];
                        if (terrainTileJson.groundTerrainTiles === undefined || terrainTileJson.groundTerrainTiles === null) {
                            return groundTerrainTiles;
                        }
                        for (const [key, value] of Object.entries(terrainTileJson.groundTerrainTiles)) {
                            groundTerrainTiles.push(new class implements GroundTerrainTile {
                                groundConfigId: number = <number>(<any>value)["groundConfigId"];
                                positions: Float32Array = new Float32Array(<number>(<any>value)["positions"]);
                                norms: Float32Array = new Float32Array(<number>(<any>value)["norms"]);
                            });
                        }
                        return groundTerrainTiles;
                    };
                    getTerrainSlopeTiles(): TerrainSlopeTile[] {
                        const terrainSlopeTiles: TerrainSlopeTile[] = [];
                        if (terrainTileJson.terrainSlopeTiles === undefined || terrainTileJson.terrainSlopeTiles === null) {
                            return terrainSlopeTiles;
                        }
                        for (const [key, terrainSlopeTileJson] of Object.entries(terrainTileJson.terrainSlopeTiles)) {
                            terrainSlopeTiles.push(new class implements TerrainSlopeTile {
                                slopeConfigId: number = <number>(<any>terrainSlopeTileJson)["slopeConfigId"];
                                outerSlopeGeometry: SlopeGeometry | null = self.setupGeometry("outerSlopeGeometry", <any>terrainSlopeTileJson);
                                centerSlopeGeometry: SlopeGeometry | null = self.setupGeometry("centerSlopeGeometry", <any>terrainSlopeTileJson);
                                innerSlopeGeometry: SlopeGeometry | null = self.setupGeometry("innerSlopeGeometry", <any>terrainSlopeTileJson);
                            });
                        }
                        return terrainSlopeTiles;
                    }
                    getTerrainWaterTiles(): TerrainWaterTile[] {
                        const terrainWaterTiles: TerrainWaterTile[] = [];
                        if (terrainTileJson.terrainWaterTiles === undefined || terrainTileJson.terrainWaterTiles === null) {
                            return terrainWaterTiles;
                        }
                        for (const [key, terrainWaterTileJson] of Object.entries(terrainTileJson.terrainWaterTiles)) {
                            terrainWaterTiles.push(new class implements TerrainWaterTile {
                                slopeConfigId: number = <number>(<any>terrainWaterTileJson)["slopeConfigId"];
                                positions: Float32Array = new Float32Array(<number>(<any>terrainWaterTileJson)["positions"]);
                                shallowPositions: Float32Array = new Float32Array(<number>(<any>terrainWaterTileJson)["shallowPositions"]);
                                shallowUvs: Float32Array = new Float32Array(<number>(<any>terrainWaterTileJson)["shallowUvs"]);
                            });
                        }
                        return terrainWaterTiles;
                    }

                };
                const threeJsTerrainTile: ThreeJsTerrainTile = threeJsRendererService.createTerrainTile(terrainTile);
                threeJsTerrainTile.addToScene();
            }
        });
    }

    private setupGeometry(slopeGeometryName: string, terrainSlopeTileJson: any): SlopeGeometry | null {
        if (slopeGeometryName in terrainSlopeTileJson) {
            let slopeGeometry = terrainSlopeTileJson[slopeGeometryName];
            return new class implements SlopeGeometry {
                positions: Float32Array = new Float32Array(slopeGeometry["positions"]);
                norms: Float32Array = new Float32Array(slopeGeometry["norms"]);
                uvs: Float32Array = new Float32Array(slopeGeometry["uvs"]);
                slopeFactors: Float32Array = new Float32Array(slopeGeometry["slopeFactors"]);
            };
        } else {
            return null;
        }

    }

}
