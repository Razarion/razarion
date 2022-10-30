import {Injectable} from "@angular/core";
import {
  Alarm,
  DecimalPosition,
  DrivewayConfig,
  EditorFrontendProvider,
  GenericEditorFrontendProvider,
  GroundConfig,
  GroundSplattingConfig,
  GroundTerrainTile,
  GwtAngularPropertyTable,
  Index,
  InputService,
  NativeMatrix,
  ObjectNameId,
  PerfmonStatistic,
  ShallowWaterConfig,
  SlopeConfig,
  SlopeGeometry,
  SlopeSplattingConfig,
  StatusProvider,
  TerrainEditorService,
  TerrainMarkerService,
  TerrainObjectConfig,
  TerrainObjectModel,
  TerrainObjectPosition,
  TerrainSlopeTile,
  TerrainTile,
  TerrainTileObjectList,
  TerrainTypeService,
  TerrainWaterTile,
  ThreeJsModelConfig,
  ThreeJsModelPackConfig,
  ThreeJsModelPackService,
  ThreeJsTerrainTile,
  Vertex,
  WaterConfig
} from "src/app/gwtangular/GwtAngularFacade";
import {ThreeJsRendererServiceImpl} from "./three-js-renderer-service.impl";
import {HttpClient} from "@angular/common/http";
import * as Stats from 'stats.js';
import {URL_IMAGE} from "../../common";
import {GwtInstance} from "../../gwtangular/GwtInstance";

let staticGameConfigJson: any = {};

@Injectable()
export class GameMockService {
  inputService: InputService = new class implements InputService {
    onViewFieldChanged(bottomLeftX: number, bottomLeftY: number, bottomRightX: number, bottomRightY: number, topRightX: number, topRightY: number, topLeftX: number, topLeftY: number): void {
      console.info("onViewFieldChanged()");
    }
  };

  statusProvider: StatusProvider = new class implements StatusProvider {
    stats: Stats | null = null;

    getClientAlarms(): Alarm[] {
      return [];
    }

    requestServerAlarms(): Promise<Alarm[]> {
      throw new Error("Method not implemented.");
    }

    setStats(stats: Stats | null): void {
      this.stats = stats;
    }

    getStats(): Stats | null {
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
      return new class implements TerrainEditorService {
        getTerrainObjectPositions(): Promise<TerrainObjectPosition[]> {
          return Promise.resolve([new class implements TerrainObjectPosition {
            getId(): number {
              return 8;
            }

            getOffset(): Vertex {
              return GwtInstance.newVertex(0, 0, 0);
            }

            getPosition(): DecimalPosition {
              return GwtInstance.newDecimalPosition(0, 0);
            }

            getRotation(): Vertex {
              return GwtInstance.newVertex(0, 0, 0);
            }

            getScale(): Vertex {
              return GwtInstance.newVertex(0, 0, 0);
            }

            getTerrainObjectConfigId(): number {
              return 0;
            }

            setOffset(offset: Vertex): void {
            }

            setPosition(position: DecimalPosition): void {
            }

            setRotation(rotation: Vertex): void {
            }

            setScale(scale: Vertex): void {
            }

            setTerrainObjectConfigId(terrainObjectConfigId: number): void {
            }
          }]);
        }

        activate(): void {
        }

        deactivate(): void {
        }

        getAllDriveways(): Promise<ObjectNameId[]> {
          return Promise.resolve([new class implements ObjectNameId {
            id: number = 1;
            internalName: string = "Driveway";
          }]);
        }

        getAllSlopes(): Promise<ObjectNameId[]> {
          return Promise.resolve([new class implements ObjectNameId {
            id: number = 1;
            internalName: string = "Land";
          }]);
        }

        getAllTerrainObjects(): Promise<ObjectNameId[]> {
          let objectNameIds: ObjectNameId[] = [];
          staticGameConfigJson.terrainObjectConfigs.forEach((terrainObjectConfigJson: any) => {
            objectNameIds.push(new class implements ObjectNameId {
              id: number = terrainObjectConfigJson.id;
              internalName: string = terrainObjectConfigJson.internalName;
            })
          });

          return Promise.resolve(objectNameIds);
        }

        getCursorCorners(): number {
          return 0;
        }

        getCursorRadius(): number {
          return 0;
        }

        getTerrainObjectRandomScale(): number {
          return 0;
        }

        getTerrainObjectRandomZRotation(): number {
          return 0;
        }

        isDrivewayMode(): boolean {
          return false;
        }

        isInvertedSlope(): boolean {
          return false;
        }

        isSlopeMode(): boolean {
          return false;
        }

        save(createdTerrainObjects: TerrainObjectPosition[], updatedTerrainObjects: TerrainObjectPosition[]): Promise<string> {
          console.info("---- Save TerrainObjectPosition ----");
          console.info("createdTerrainObjects");
          console.info(createdTerrainObjects);
          console.info("updatedTerrainObjects");
          console.info(updatedTerrainObjects);
          return Promise.resolve("Mock, see console");
        }

        setCursorCorners(cursorCorners: number): void {
        }

        setCursorRadius(cursorRadius: number): void {
        }

        setDriveway4New(driveway4New: ObjectNameId): void {
        }

        setDrivewayMode(drivewayMode: boolean): void {
        }

        setInvertedSlope(invertedSlope: boolean): void {
        }

        setSlope4New(slope4New: ObjectNameId): void {
        }

        setSlopeMode(slopeMode: boolean): void {
        }

        setTerrainObject4New(terrainObject4New: ObjectNameId): void {
        }

        setTerrainObjectRandomScale(terrainObjectRandomScale: number): void {
        }

        setTerrainObjectRandomZRotation(terrainObjectRandomZRotation: number): void {
        }

      }
    }
  };

  threeJsModels: ObjectNameId[] = [new class implements ObjectNameId {
    id = 8883;
    internalName = "3D Model Palm Tree";

    toString(): string {
      return "3D Model Palm Tree (8883)"
    }
  }, new class implements ObjectNameId {
    id = 8881;
    internalName = "Stone Pack";

    toString(): string {
      return "Stone Pack (8881)"
    }
  }];

  constructor(private http: HttpClient) {
  }

  loadMockStaticGameConfig(): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      this.http.get<TerrainTile[]>("/gwt-mock/static-game-config").subscribe((value: any) => {
        staticGameConfigJson = value;
        resolve(value);
      });
    });
  }

  mockTerrainTypeService(): TerrainTypeService {
    let _this = this;
    return new class implements TerrainTypeService {
      terrainTypeService = this;

      getTerrainObjectConfig(terrainObjectConfigId: number): TerrainObjectConfig {
        let terrainObjectConfig: TerrainObjectConfig | null = null;
        staticGameConfigJson.terrainObjectConfigs.forEach((terrainObjectConfigJson: any) => {
          if (terrainObjectConfigJson.id != terrainObjectConfigId) {
            return;
          }
          terrainObjectConfig = new class implements TerrainObjectConfig {
            getThreeJsModelPackConfigId(): number {
              return terrainObjectConfigJson.threeJsModelPackConfigId;
            }

            getId(): number {
              return terrainObjectConfigJson.id;
            }

            getInternalName(): string {
              return terrainObjectConfigJson.internalName;
            }
          }
          return
        });
        if (terrainObjectConfig !== null) {
          return terrainObjectConfig;
        } else {
          throw new Error(`No TerrainObjectConfig for id ${terrainObjectConfigId}`);
        }
      }

      getSlopeConfig(slopeConfigId: number): SlopeConfig {
        let slopeConfig: SlopeConfig | null = null;
        staticGameConfigJson.slopeConfigs.forEach((slopeConfigJson: any) => {
          if (slopeConfigJson.id != slopeConfigId) {
            return;
          }
          slopeConfig = new class implements SlopeConfig {
            getId(): number {
              return slopeConfigJson.id;
            }

            getInternalName(): string {
              return slopeConfigJson.internalName;
            }

            getThreeJsMaterial(): number {
              return slopeConfigJson.threeJsMaterial;
            }

            getGroundConfigId(): number {
              return slopeConfigJson.groundConfigId;
            }

            getWaterConfigId(): number {
              return slopeConfigJson.waterConfigId;
            }

            getInnerSlopeSplattingConfig(): SlopeSplattingConfig | null {
              return _this.setupSlopeSplattingConfig(slopeConfigJson.innerSlopeSplattingConfig);
            }

            getOuterSlopeSplattingConfig(): SlopeSplattingConfig | null {
              return _this.setupSlopeSplattingConfig(slopeConfigJson.outerSlopeSplattingConfig);
            }

            getShallowWaterConfig(): ShallowWaterConfig | null {
              if (slopeConfigJson.shallowWaterConfig) {
                return new class implements ShallowWaterConfig {
                  getDistortionId(): number {
                    return slopeConfigJson.shallowWaterConfig.distortionId;
                  }

                  getDistortionStrength(): number {
                    return slopeConfigJson.shallowWaterConfig.distortionStrength;
                  }

                  getDurationSeconds(): number {
                    return slopeConfigJson.shallowWaterConfig.durationSeconds;
                  }

                  getScale(): number {
                    return slopeConfigJson.shallowWaterConfig.scale;
                  }

                  getStencilId(): number {
                    return slopeConfigJson.shallowWaterConfig.stencilId;
                  }

                  getTextureId(): number {
                    return slopeConfigJson.shallowWaterConfig.textureId;
                  }
                };
              } else {
                return null;
              }
            }
          }
          return
        });
        if (slopeConfig !== null) {
          return slopeConfig;
        } else {
          throw new Error(`No SlopeConfig for id ${slopeConfigId}`);
        }

      }

      getDrivewayConfig(drivewayConfigId: number): DrivewayConfig {
        throw new Error("Method not implemented.");
      }

      getGroundConfig(groundConfigId: number): GroundConfig {
        let groundConfig: GroundConfig | null = null;
        staticGameConfigJson.groundConfigs.forEach((groundConfigJson: any) => {
          if (groundConfigJson.id != groundConfigId) {
            return;
          }
          groundConfig = new class implements GroundConfig {
            _groundConfigJson: any = groundConfigJson;

            getId(): number {
              return this._groundConfigJson.id
            };

            getInternalName(): string {
              return this._groundConfigJson.internalName
            };

            getTopThreeJsMaterial(): number {
              return this._groundConfigJson.topThreeJsMaterial;
            };

            getBottomThreeJsMaterial(): number {
              return this._groundConfigJson.bottomThreeJsMaterial;
            };

            getSplatting(): GroundSplattingConfig {
              return this._groundConfigJson.splatting
            };
          }
          return
        });
        if (groundConfig !== null) {
          return groundConfig;
        } else {
          throw new Error(`No GroundConfig for id ${groundConfigId}`);
        }
      }

      getWaterConfig(waterConfigId: number): WaterConfig {
        let waterConfig: WaterConfig | null = null;
        staticGameConfigJson.waterConfigs.forEach((waterConfigJson: any) => {
          if (waterConfigJson.id != waterConfigId) {
            return;
          }
          waterConfig = new class implements WaterConfig {
            _waterConfigJson: any = waterConfigJson;

            getId(): number {
              return this._waterConfigJson.id
            };

            getReflectionId(): number {
              return this._waterConfigJson.reflectionId
            }

            getTransparency(): number {
              return this._waterConfigJson.transparency
            };

            getShininess(): number {
              return this._waterConfigJson.shininess
            }

            getSpecularStrength(): number {
              return this._waterConfigJson.specularStrength
            }

            getDistortionAnimationSeconds(): number {
              return this._waterConfigJson.distortionAnimationSeconds
            }

            getDistortionStrength(): number {
              return this._waterConfigJson.distortionStrength
            }

            getNormalMapId(): number {
              return this._waterConfigJson.normalMapId
            }

            getNormalMapDepth(): number {
              return this._waterConfigJson.normalMapDepth
            }

          }
        });
        if (waterConfig !== null) {
          return waterConfig;
        } else {
          throw new Error(`No WaterConfig for id ${waterConfig}`);
        }
      }
    }
  };

  mockThreeJsModelPackService: ThreeJsModelPackService = new class implements ThreeJsModelPackService {
    getThreeJsModelPackConfig(id: number): ThreeJsModelPackConfig {
      let packJson = staticGameConfigJson.threeJsModelPackConfigs.find((packJson: any) => packJson.id === id);
      if (!packJson) {
        throw new Error(`ThreeJsModelPackConfig not found: ${id}`);
      }
      return new class implements ThreeJsModelPackConfig {
        getId(): number {
          return packJson.id;
        }

        getInternalName(): string {
          return packJson.internalName;
        }

        toNamePathAsArray(): string[] {
          return packJson.namePath;
        }

        getPosition(): Vertex {
          return GwtInstance.newVertex(packJson.position.x, packJson.position.y, packJson.position.z);
        }

        getRotation(): Vertex {
          return GwtInstance.newVertex(packJson.rotation.x, packJson.rotation.y, packJson.rotation.z);
        }

        getScale(): Vertex {
          return GwtInstance.newVertex(packJson.scale.x, packJson.scale.y, packJson.scale.z);
        }

        getThreeJsModelId(): number {
          return packJson.threeJsModelId;
        }
      }
    }
  }

  mockTerrainTile(threeJsRendererService: ThreeJsRendererServiceImpl) {
    const _this = this;
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
                outerSlopeGeometry: SlopeGeometry | null = _this.setupGeometry("outerSlopeGeometry", <any>terrainSlopeTileJson);
                centerSlopeGeometry: SlopeGeometry | null = _this.setupGeometry("centerSlopeGeometry", <any>terrainSlopeTileJson);
                innerSlopeGeometry: SlopeGeometry | null = _this.setupGeometry("innerSlopeGeometry", <any>terrainSlopeTileJson);
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
                terrainObjectModels: TerrainObjectModel[] = _this.setupTerrainObjectModels((<any>terrainTileObjectListJson)["terrainObjectModels"]);
                terrainObjectConfigId = (<any>terrainTileObjectListJson)["terrainObjectConfigId"];
              });
            }
            return terrainTileObjectLists;
          }

          getIndex(): Index {
            return new class implements Index {
              getX(): number {
                return 0;
              }

              getY(): number {
                return 0;
              }

              toString(): string {
                return "TerrainTile.getIndex() MOCK";
              }
            }
          }
        };
        const threeJsTerrainTile: ThreeJsTerrainTile = threeJsRendererService.createTerrainTile(terrainTile, 1);
        threeJsTerrainTile.addToScene();
      }
    });
  }

  mockThreeJsModelConfigs(): ThreeJsModelConfig[] {
    let threeJsModelConfigs: ThreeJsModelConfig[] = [];
    staticGameConfigJson.threeJsModelConfigs.forEach((threeJsModelConfigJson: any) => {
      threeJsModelConfigs.push(new class implements ThreeJsModelConfig {
        getId(): number {
          return threeJsModelConfigJson.id;
        }

        getInternalName(): string {
          return threeJsModelConfigJson.internalName;
        }

        getType(): ThreeJsModelConfig.Type {
          return ThreeJsModelConfig.Type[<ThreeJsModelConfig.Type>threeJsModelConfigJson.type];
        }

      });
    })
    return threeJsModelConfigs;
  }

  private setupSlopeSplattingConfig(slopeSplattingConfig: any): SlopeSplattingConfig | null {
    if (slopeSplattingConfig) {
      return new class implements SlopeSplattingConfig {
        getBlur(): number {
          return slopeSplattingConfig.blur;
        }

        getImpact(): number {
          return slopeSplattingConfig.impact;
        }

        getOffset(): number {
          return slopeSplattingConfig.offset;
        }

        getScale(): number {
          return slopeSplattingConfig.scale;
        }

        getTextureId(): number {
          return slopeSplattingConfig.textureId;
        }
      }
    } else {
      return null;
    }
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

  private setupTerrainObjectModels(terrainObjectModelsJsons: any): TerrainObjectModel[] {
    let terrainObjectModels: TerrainObjectModel[] = [];
    terrainObjectModelsJsons.forEach((terrainObjectModelsJson: any) => {
      terrainObjectModels.push(new class implements TerrainObjectModel {
        model: NativeMatrix = new class implements NativeMatrix {
          getColumnMajorFloat32Array(): Float32Array {
            return new Float32Array(terrainObjectModelsJson.model.columnMajorFloat32Array);
          }
        };
        terrainObjectId: number = terrainObjectModelsJson.terrainObjectId;
      });
    });
    return terrainObjectModels;
  }

}

export function getGwtMockImageUrl(filename: string) {
  return `${URL_IMAGE}/${filename}`
}

