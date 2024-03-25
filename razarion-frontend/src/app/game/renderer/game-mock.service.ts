import {Injectable} from "@angular/core";
import {
  Alarm,
  BabylonTerrainTile,
  BaseItemType,
  BaseItemUiService,
  BuilderType,
  Character,
  ComparisonConfig,
  ConditionConfig,
  DecimalPosition,
  Diplomacy,
  DrivewayConfig,
  EditorFrontendProvider,
  GameUiControl,
  GenericEditorFrontendProvider,
  GroundConfig,
  GroundSplattingConfig,
  GroundTerrainTile,
  HarvesterType,
  I18nString,
  Index,
  InputService,
  InventoryItem,
  InventoryTypeService,
  ItemTypeService,
  Mesh,
  MeshContainer,
  NativeSyncBaseItemTickInfo,
  ObjectNameId,
  ParticleSystemConfig,
  PhysicalAreaConfig,
  PlanetConfig,
  PlayerBaseDto,
  QuestConfig,
  QuestProgressInfo,
  ResourceItemType,
  ShapeTransform,
  SlopeConfig,
  SlopeGeometry,
  StatusProvider,
  TerrainEditorService,
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
  Vertex,
  WaterConfig,
  WeaponType
} from "src/app/gwtangular/GwtAngularFacade";
import {HttpClient} from "@angular/common/http";
import {GwtInstance} from "../../gwtangular/GwtInstance";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {QuestCockpitComponent} from "../cockpit/quest/quest-cockpit.component";
import {ConditionTrigger} from "src/app/generated/razarion-share";

let staticGameConfigJson: any = {};

@Injectable()
export class GameMockService {
  public unityAssetConverterTestAssetConfig: any = {};
  gameUiControl: GameUiControl = new class implements GameUiControl {
    getPlanetConfig(): PlanetConfig {
      return new class implements PlanetConfig {
        getSize(): DecimalPosition {
          return GwtInstance.newDecimalPosition(320, 320);
        }

        getId(): number {
          return 117;
        }

      };
    }
  };

  inputService: InputService = new class implements InputService {
    onMouseDown(x: number, y: number, z: number): void {
      // console.info(`Terrain Position ${x}:${y} (mouse down)`);
    }

    onMouseMove(x: number, y: number, z: number, primaryButtonDown: boolean): void {
      // console.info(`Terrain Position ${x}:${y}  (mouse move) ${primaryButtonDown}`);
    }

    onMouseUp(x: number, y: number, z: number): void {
      // console.info(`Terrain Position  ${x}:${y} (mouse up)`);
    }

    onViewFieldChanged(bottomLeftX: number, bottomLeftY: number, bottomRightX: number, bottomRightY: number, topRightX: number, topRightY: number, topLeftX: number, topLeftY: number): void {
      // console.info("onViewFieldChanged()");
    }
  };

  statusProvider: StatusProvider = new class implements StatusProvider {

    getClientAlarms(): Alarm[] {
      return [];
    }

    requestServerAlarms(): Promise<Alarm[]> {
      throw new Error("Method not implemented.");
    }
  };

  editorFrontendProvider: EditorFrontendProvider = new class implements EditorFrontendProvider {
    getGenericEditorFrontendProvider(): GenericEditorFrontendProvider {
      return new class implements GenericEditorFrontendProvider {
        collectionNames(): string[] {
          return ["Svelte-jsoneditor"];
        }

        requestObjectNameIds(collectionName: string): Promise<ObjectNameId[]> {
          return Promise.resolve([new class implements ObjectNameId {
            id = -99999;
            internalName = "Svelte-jsoneditor";
          }]);
        }

        getPathForCollection(collectionName: string): string {
          return `editor/${collectionName}`;
        }

      };
    }
    getTerrainEditorService(): TerrainEditorService {
      return new class implements TerrainEditorService {
        getTerrainObjectPositions(): Promise<TerrainObjectPosition[]> {
          return Promise.resolve([new class implements TerrainObjectPosition {
            getId(): number {
              return 8;
            }

            setId(id: number): void {
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

              toString(): string {
                return `${this.internalName} (${this.id})`;
              }
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

        isDrivewayMode(): boolean {
          return false;
        }

        isInvertedSlope(): boolean {
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
      }
    }
  };

  getThreeJsModels(): ObjectNameId[] {
    let objectNameIds: ObjectNameId[] = [];
    staticGameConfigJson.threeJsModelConfigs.forEach((threeJsModelJson: any) => {
      objectNameIds.push(new class implements ObjectNameId {
        id: number = threeJsModelJson.id;
        internalName: string = threeJsModelJson.internalName;

        toString(): string {
          return `${this.internalName} (${this.id})`;
        }
      })
    });
    return objectNameIds;
  }

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

  loadMockAssetConfig(): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      this.http.get<TerrainTile[]>("/gwt-mock/unity-asset-converter-test-asset-config").subscribe((value: any) => {
        this.unityAssetConverterTestAssetConfig = value;
        resolve(value);
      });
    });
  }

  mockTerrainTypeService(): TerrainTypeService {
    let _this = this;
    return new class implements TerrainTypeService {
      calculateGroundHeight(slopeConfigId: number): number {
        return 1;
      }

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

            getRadius(): number {
              return terrainObjectConfigJson.radius;
            }

            setRadius(radius: number): void {
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

            getShallowWaterThreeJsMaterial(): number {
              return slopeConfigJson.shallowWaterThreeJsMaterial;
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

            getInternalName(): string {
              return waterConfigJson.internalName;
            }

            getMaterial(): number {
              return waterConfigJson.material;
            }

            getId(): number {
              return this._waterConfigJson.id
            };


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

  mockItemTypeService(): ItemTypeService {
    return new class implements ItemTypeService {
      getBaseItemTypeAngular(baseItemTypeId: number): BaseItemType {
        return new class implements BaseItemType {
          getBuilderType(): BuilderType | null {
            return null;
          }

          getHarvesterType(): HarvesterType | null {
            return null;
          }

          getI18nName(): I18nString {
            return new class implements I18nString {
              getString(language: string): string {
                return "Viper";
              }
            };
          }

          getId(): number {
            return 1;
          }

          getInternalName(): string {
            return "InternalName";
          }

          getMeshContainerId(): number | null {
            return null;
          }

          getPhysicalAreaConfig(): PhysicalAreaConfig {
            return new class implements PhysicalAreaConfig {
              getRadius(): number {
                return 3;
              }
            };
          }

          getThreeJsModelPackConfigId(): number | null {
            return null;
          }

          getWeaponType(): WeaponType | null {
            return null;
          }
        }
      }

      getResourceItemTypeAngular(resourceItemTypeId: number): ResourceItemType {
        return new class implements ResourceItemType {
          getI18nName(): I18nString {
            return new class implements I18nString {
              getString(language: string): string {
                return "I18nString";
              }
            };
          }

          getId(): number {
            return 0;
          }

          getInternalName(): string {
            return "";
          }

          getMeshContainerId(): number | null {
            return null;
          }

          getRadius(): number {
            return 0;
          }

          getThreeJsModelPackConfigId(): number | null {
            return null;
          }
        }
      }
    }
  }

  mockInventoryTypeService(): InventoryTypeService {
    return new class implements InventoryTypeService {
      getInventoryItem(id: number): InventoryItem {
        return new class implements InventoryItem {
          getI18nName(): I18nString {
            return new class implements I18nString {
              getString(language: string): string {
                return "Viper Pack";
              }
            }
          }

          getRazarion(): number | null {
            return null;
          }

          getBaseItemTypeId(): number | null {
            return 1;
          }

          getBaseItemTypeCount(): number {
            return 2;
          }

          getImageId(): number | null {
            return 36;
          }

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

  mockBaseItemUiService: BaseItemUiService = new class implements BaseItemUiService {
    getVisibleNativeSyncBaseItemTickInfos(bottomLeft: DecimalPosition, topRight: DecimalPosition): NativeSyncBaseItemTickInfo[] {
      return [
        {
          x: 50,
          y: 60
        }
      ];
    }

    diplomacy4SyncBaseItem(nativeSyncBaseItemTickInfo: NativeSyncBaseItemTickInfo): Diplomacy {
      return Diplomacy.OWN;
    }

    getBases(): PlayerBaseDto[] {
      return [
        {
          getName() {
            return "Base 1"
          },

          getBaseId(): number {
            return 1;
          },

          getCharacter(): Character {
            return Character.HUMAN
          }

        }
      ];
    }
  }

  mockTerrainTile(threeJsRendererService: BabylonRenderServiceAccessImpl) {
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
                centerSlopeGeometry: SlopeGeometry | null = _this.setupGeometry("centerSlopeGeometry", <any>terrainSlopeTileJson);
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
                return terrainTileJson.index.x;
              }

              getY(): number {
                return terrainTileJson.index.y;
              }

              toString(): string {
                return `${terrainTileJson.index.x}:${terrainTileJson.index.y}`;
              }
            }
          }
        };
        const threeJsTerrainTile: BabylonTerrainTile = threeJsRendererService.createTerrainTile(terrainTile);
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

        getNodeMaterialId(): number | null {
          return threeJsModelConfigJson.nodeMaterialId;
        }

        isDisabled(): boolean {
          return threeJsModelConfigJson.disabled;
        }

      });
    })
    return threeJsModelConfigs;
  }

  mockParticleSystemConfigs(): ParticleSystemConfig[] {
    let particleSystemConfig: ParticleSystemConfig[] = [];
    staticGameConfigJson.particleSystemConfigs.forEach((particleSystemConfigJson: any) => {
      particleSystemConfig.push(new class implements ParticleSystemConfig {
        getThreeJsModelId(): number {
          return particleSystemConfigJson.threeJsModelId;
        }

        getEmitterMeshPath(): string[] {
          return particleSystemConfigJson.emitterMeshPath;
        }

        getId(): number {
          return particleSystemConfigJson.id;
        }

        getInternalName(): string {
          return particleSystemConfigJson.internalName;
        }
      });
    });
    return particleSystemConfig;
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
        terrainObjectId: number = terrainObjectModelsJson.terrainObjectId;
        position: Vertex = GwtInstance.newVertex(terrainObjectModelsJson.position.x, terrainObjectModelsJson.position.y, terrainObjectModelsJson.position.z);
        scale: Vertex | null = terrainObjectModelsJson.scale ? GwtInstance.newVertex(terrainObjectModelsJson.scale.x, terrainObjectModelsJson.scale.y, terrainObjectModelsJson.scale.z) : null;
        rotation: Vertex | null = terrainObjectModelsJson.rotation ? GwtInstance.newVertex(terrainObjectModelsJson.rotation.x, terrainObjectModelsJson.rotation.y, terrainObjectModelsJson.rotation.z) : null;
      });
    });
    return terrainObjectModels;
  }

  createMeshContainers(): MeshContainer[] {
    let meshContainers: MeshContainer[] = [];
    for (let meshContainerJson of this.unityAssetConverterTestAssetConfig.meshContainers) {
      meshContainers.push(this.createMeshContainer(meshContainerJson));
    }
    return meshContainers;
  }

  private createMeshContainer(meshContainerJson: any): MeshContainer {
    const _this = this;
    return new class implements MeshContainer {
      toChildrenArray(): MeshContainer[] | null {
        if (meshContainerJson.children) {
          let meshContainers: MeshContainer[] = [];
          for (let childMeshContainerJson of meshContainerJson.children) {
            meshContainers.push(_this.createMeshContainer(childMeshContainerJson));
          }
          return meshContainers;
        } else {
          return null;
        }
      }

      getId(): number {
        return meshContainerJson.id;
      }

      getInternalName(): string {
        return meshContainerJson.internalName;
      }

      getMesh(): Mesh | null {
        if (meshContainerJson.mesh) {
          return new class implements Mesh {
            getElement3DId(): string {
              return meshContainerJson.mesh.element3DId;
            }

            toShapeTransformsArray(): ShapeTransform[] | null {
              if (meshContainerJson.mesh.shapeTransforms) {
                let shapeTransforms: ShapeTransform[] = [];
                for (let shapeTransformJson of meshContainerJson.mesh.shapeTransforms) {
                  shapeTransforms.push(new class implements ShapeTransform {
                    getRotateW(): number {
                      return shapeTransformJson.rotateW;
                    }

                    getRotateX(): number {
                      return shapeTransformJson.rotateX;
                    }

                    getRotateY(): number {
                      return shapeTransformJson.rotateY;
                    }

                    getRotateZ(): number {
                      return shapeTransformJson.rotateZ;
                    }

                    getScaleX(): number {
                      return shapeTransformJson.scaleZ;
                    }

                    getScaleY(): number {
                      return shapeTransformJson.scaleY;
                    }

                    getScaleZ(): number {
                      return shapeTransformJson.scaleZ;
                    }

                    getTranslateX(): number {
                      return shapeTransformJson.translateX;
                    }

                    getTranslateY(): number {
                      return shapeTransformJson.translateY;
                    }

                    getTranslateZ(): number {
                      return shapeTransformJson.translateZ;
                    }
                  });
                }
                return shapeTransforms;
              } else {
                return null;
              }
            }

            getThreeJsModelId(): number | null {
              return meshContainerJson.mesh.threeJsModelId;
            }
          }
        } else {
          return null;
        }
      }
    }
  }

  showQuestSideBar(questCockpitContainer: QuestCockpitComponent) {
    questCockpitContainer.showQuestSideBar(new class implements QuestConfig {
      getConditionConfig(): ConditionConfig | null {
        return new class implements ConditionConfig {
          getConditionTrigger(): ConditionTrigger {
            return ConditionTrigger.SYNC_ITEM_CREATED;
          }

          getComparisonConfig(): ComparisonConfig {
            return new class implements ComparisonConfig {
              getCount(): number | null {
                return null;
              }

              getTimeSeconds(): number | null {
                return null;
              }

              toTypeCountAngular(): number[][] {
                return [[1, 2], [2, 3]];
              }

            };
          }
        };
      }

      getId(): number {
        return 0;
      }

      getInternalName(): string {
        return "";
      }

      getTitle(): string {
        return "Place";
      }

      getDescription(): string | null {
        return null;
      }

    },
      true)
  }

  hideQuestSideBar(questCockpitContainer: QuestCockpitComponent) {
    questCockpitContainer.showQuestSideBar(null, false);
  }

  onQuestProgress(questCockpitContainer: QuestCockpitComponent) {
    questCockpitContainer.onQuestProgress(new class implements QuestProgressInfo {
      getBotBasesInformation(): string | null {
        return null;
      }

      getCount(): number | null {
        return 7;
      }

      getSecondsRemaining(): number | null {
        return 25;
      }

      toTypeCountAngular(): number[][] {
        return [[1, 2], [2, 2]];
      }

    });
  }
}

