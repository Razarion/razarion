import { Injectable } from "@angular/core";
import {
  Alarm,
  BabylonDecal,
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
  SelectionHandler,
  ShapeTransform,
  SlopeConfig,
  StatusProvider,
  TerrainEditorService,
  TerrainObjectConfig,
  TerrainObjectModel,
  TerrainObjectPosition,
  TerrainTile,
  TerrainTileObjectList,
  TerrainTypeService,
  ThreeJsModelConfig,
  ThreeJsModelPackConfig,
  ThreeJsModelPackService,
  Vertex,
  WaterConfig,
  WeaponType
} from "src/app/gwtangular/GwtAngularFacade";
import { HttpClient } from "@angular/common/http";
import { GwtInstance } from "../../gwtangular/GwtInstance";
import { BabylonRenderServiceAccessImpl } from "./babylon-render-service-access-impl.service";
import { QuestCockpitComponent } from "../cockpit/quest/quest-cockpit.component";
import { ConditionTrigger } from "src/app/generated/razarion-share";
import { BabylonTerrainTileImpl } from "./babylon-terrain-tile.impl";

let staticGameConfigJson: any = {
  terrainObjectConfigs: []
};

let displayMockTerrainTile: BabylonTerrainTile[] = [];

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
    getTerrainTypeOnTerrain(nodeIndex: Index): Promise<any> {
      throw new Error("Method not implemented.");
    }
    resourceItemClicked(id: number): void {
      console.info("resourceItemClicked");
    }
    enemyItemClicked(id: number): void {
      console.info("enemyItemClicked");
    }
    terrainClicked(arg0: DecimalPosition): void {
      console.info("terrainClicked");
    }
    friendItemClicked(id: number): void {
      console.info("friendItemClickedvoi");
    }
    ownItemClicked(id: number): void {
      console.info("ownItemClicked");
    }
    boxItemClicked(id: number): void {
      console.info("boxItemClicked");
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


        save(createdTerrainObjects: TerrainObjectPosition[], updatedTerrainObjects: TerrainObjectPosition[]): Promise<string> {
          console.info("---- Save TerrainObjectPosition ----");
          console.info("createdTerrainObjects");
          console.info(createdTerrainObjects);
          console.info("updatedTerrainObjects");
          console.info(updatedTerrainObjects);
          return Promise.resolve("Mock, see console");
        }


        getAllBabylonTerrainTile(): BabylonTerrainTile[] {
          return displayMockTerrainTile;
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
              getString(): string {
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

          getExplosionParticleId(): number | null {
            throw new Error('Method not implemented.');
          }

          getPhysicalAreaConfig(): PhysicalAreaConfig {
            return new class implements PhysicalAreaConfig {
              getRadius(): number {
                return 3;
              }
              fulfilledMovable(): boolean {
                return true;
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
              getString(): string {
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
              getString(): string {
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
    fetch('rest/terrainHeightMap/117', {
      headers: {
        'Content-Type': 'application/octet-stream'
      }
    }).then(response => response.arrayBuffer())
      .then(buffer => {
        const uint16Array = new Uint16Array(buffer);
        let xCount = 2;
        let yCount = 2;
        for (let x = 0; x < xCount; x++) {
          for (let y = 0; y < yCount; y++) {
            let start = x * BabylonTerrainTileImpl.NODE_X_COUNT + y * BabylonTerrainTileImpl.NODE_Y_COUNT;
            let heightMap = uint16Array.slice(start, start + BabylonTerrainTileImpl.NODE_X_COUNT * BabylonTerrainTileImpl.NODE_Y_COUNT);

            const terrainTile = new class implements TerrainTile {
              getGroundHeightMap(): Uint16Array {
                return heightMap;
              }

              getGroundConfigId(): number {
                return 1;
              }

              getWaterConfigId(): number {
                return 1;
              }

              getTerrainTileObjectLists(): TerrainTileObjectList[] {
                return [];
              }

              getBabylonDecals(): BabylonDecal[] {
                return [];
              }

              getIndex(): Index {
                return new class implements Index {
                  add(deltaX: number, deltaY: number): Index {
                    throw GwtInstance.newIndex(this.getX() + deltaX, this.getY() + deltaY);
                  }
                  getX(): number {
                    return x;
                  }

                  getY(): number {
                    return y;
                  }

                  toString(): string {
                    return `${x}:${y}`;
                  }
                }
              }
            }
            const threeJsTerrainTile: BabylonTerrainTile = threeJsRendererService.createTerrainTile(terrainTile);
            displayMockTerrainTile.push(threeJsTerrainTile);
            threeJsTerrainTile.addToScene();
          }
        }
      })
      .catch(error => console.error('Error:', error));
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

        getPositionOffset(): Vertex | null {
          return GwtInstance.newVertex(0, 0, 0);
        }

        getImageId(): number | null {
          return 0;
        }
      });
    });
    return particleSystemConfig;
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

  mockSelectionHandler(): SelectionHandler {
    return new class implements SelectionHandler {
      hasOwnSelection(): boolean {
        return false;
      }

      hasOwnMovable(): boolean {
        return false;
      }

      hasAttackers(): boolean {
        return false;
      }

      canAttack(targetItemTypeId: number): boolean {
        return false;
      }

      hasHarvesters(): boolean {
        return false;
      }

      selectRectangle(xStart: number, yStart: number, width: number, height: number): void {
        console.info("selectRectangle");
      }

      setSelectionListener(callback: () => void): void {
      }
    }
  }
}

