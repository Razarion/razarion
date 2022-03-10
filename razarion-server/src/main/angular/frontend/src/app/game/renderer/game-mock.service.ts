import { Injectable } from "@angular/core";
import { Alarm, EditorFrontendProvider, GenericEditorFrontendProvider, GroundTerrainTile, GwtAngularPropertyTable, InputService, NativeMatrix, ObjectNameId, PerfmonStatistic, RendererEditorService, RenderTaskRunnerControl, SlopeGeometry, StatusProvider, TerrainEditorService, TerrainMarkerService, TerrainSlopeTile, TerrainTile, TerrainTileObjectList, TerrainWaterTile, ThreeJsTerrainTile } from "src/app/gwtangular/GwtAngularFacade";
import { ThreeJsRendererServiceImpl } from "./three-js-renderer-service.impl";
import { HttpClient } from "@angular/common/http";
import * as Stats from 'stats.js';

@Injectable()
export class GameMockService {
    inputService: InputService = new class implements InputService {
        onViewFieldChanged(bottomLeftX: number, bottomLeftY: number, bottomRightX: number, bottomRightY: number, topRightX: number, topRightY: number, topLeftX: number, topLeftY: number): void {
            console.info("onViewFieldChanged()");
        }
    };

    statusProvider: StatusProvider = new class implements StatusProvider {
        stats : Stats| null = null;
        
        getClientAlarms(): Alarm[] {
            return [];
        }
        requestServerAlarms(): Promise<Alarm[]> {
            throw new Error("Method not implemented.");
        }
        setStats(stats: Stats | null): void {
            this.stats = stats;
        }
        getStats(): Stats | null{
            return this.stats;
        }

    };

    editorFrontendProvider: EditorFrontendProvider = new class implements EditorFrontendProvider {
        getGenericEditorFrontendProvider(): GenericEditorFrontendProvider {
            return new class implements GenericEditorFrontendProvider {
                collectionNames(): string[] {
                    return [];
                }
                requestObjectNameIds(collectionName: string): Promise<ObjectNameId[]> {
                    throw new Error("Method not implemented.");
                }
                requestObjectNameId(collectionName: string, configId: number): Promise<ObjectNameId> {
                    throw new Error("Method not implemented.");
                }
                createConfig(collectionName: string): Promise<GwtAngularPropertyTable> {
                    throw new Error("Method not implemented.");
                }
                readConfig(collectionName: string, configId: number): Promise<GwtAngularPropertyTable> {
                    throw new Error("Method not implemented.");
                }
                updateConfig(collectionName: string, gwtAngularPropertyTable: GwtAngularPropertyTable): Promise<void> {
                    throw new Error("Method not implemented.");
                }
                deleteConfig(collectionName: string, gwtAngularPropertyTable: GwtAngularPropertyTable): Promise<void> {
                    throw new Error("Method not implemented.");
                }
                colladaConvert(gwtAngularPropertyTable: GwtAngularPropertyTable, colladaString: string): Promise<void> {
                    throw new Error("Method not implemented.");
                }

            };
        }
        getClientPerfmonStatistics(): PerfmonStatistic[] {
            throw new Error("Method not implemented.");
        }
        getWorkerPerfmonStatistics(): Promise<PerfmonStatistic[]> {
            throw new Error("Method not implemented.");
        }
        getTerrainMarkerService(): TerrainMarkerService {
            throw new Error("Method not implemented.");
        }
        getTerrainEditorService(): TerrainEditorService {
            throw new Error("Method not implemented.");
        }
        getCameraFrontendService(): RendererEditorService {
            throw new Error("Method not implemented.");
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
                    getTerrainTileObjectLists(): TerrainTileObjectList[] {
                        const terrainTileObjectLists: TerrainTileObjectList[] = [];
                        if (terrainTileJson.terrainTileObjectLists === undefined || terrainTileJson.terrainTileObjectLists === null) {
                            return terrainTileObjectLists;
                        }
                        for (const [key, terrainTileObjectListJson] of Object.entries(terrainTileJson.terrainTileObjectLists)) {
                            terrainTileObjectLists.push(new class implements TerrainTileObjectList {
                                models: NativeMatrix[] = self.setupNativeMatrix((<any>terrainTileObjectListJson)["models"]);
                            });
                        }
                        return terrainTileObjectLists;
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

    private setupNativeMatrix(nativeMatricesJson: any): NativeMatrix[] {
        let nativeMatrix: NativeMatrix[] = [];
        nativeMatricesJson.forEach((nativeMatrixJson: any) => {
            nativeMatrix.push(new class implements NativeMatrix {
                getColumnMajorFloat32Array(): Float32Array {
                    return new Float32Array(nativeMatrixJson.columnMajorFloat32Array);
                }

            });
        });
        return nativeMatrix;
    }

    setupRendererEditorService(): RendererEditorService {
        return new class implements RendererEditorService {
            isRenderInterpolation(): boolean {
                return false;
            }
            setRenderInterpolation(value: boolean): void {
            }
            isCallGetError(): boolean {
                return false;
            }
            setCallGetError(callGetError: boolean): void {
            }
            getCameraXPosition(): number {
                return 0;
            }
            setCameraXPosition(x: number): void {
            }
            getCameraYPosition(): number {
                return 0;
            }
            setCameraYPosition(y: number): void {
            }
            getCameraZPosition(): number {
                return 0;
            }
            setCameraZPosition(z: number): void {
            }
            getCameraXRotation(): number {
                return 0;
            }
            setCameraXRotation(x: number): void {
            }
            getCameraZRotation(): number {
                return 0;
            }
            setCameraZRotation(z: number): void {
            }
            getCameraOpeningAngleY(): number {
                return 0;
            }
            setCameraOpeningAngleY(y: number): void {
            }
            getRenderTaskRunnerControls(): RenderTaskRunnerControl[] {
                return [];
            }

        };
    }

}
