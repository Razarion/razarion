import {Injectable} from "@angular/core";
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
  GameUiControl,
  GroundConfig,
  HarvesterType,
  I18nString,
  Index,
  InputService,
  InventoryItem,
  InventoryTypeService,
  ItemTypeService,
  NativeSyncBaseItemTickInfo,
  ObjectNameId,
  PhysicalAreaConfig,
  PlanetConfig,
  PlayerBaseDto,
  QuestConfig,
  QuestProgressInfo,
  ResourceItemType,
  SelectionService,
  StatusProvider,
  TerrainObjectConfig,
  TerrainTile,
  TerrainTileObjectList,
  TerrainTypeService,
  ThreeJsModelConfig,
  Vertex,
  WeaponType
} from "src/app/gwtangular/GwtAngularFacade";
import {HttpClient} from "@angular/common/http";
import {GwtInstance} from "../../gwtangular/GwtInstance";
import {BabylonRenderServiceAccessImpl} from "./babylon-render-service-access-impl.service";
import {QuestCockpitComponent} from "../cockpit/quest/quest-cockpit.component";
import {ConditionTrigger} from "src/app/generated/razarion-share";
import {BabylonTerrainTileImpl} from "./babylon-terrain-tile.impl";

let staticGameConfigJson: any = {
  terrainObjectConfigs: []
};

let displayMockTerrainTile: BabylonTerrainTile[] = [];

@Injectable()
export class GameMockService {

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
            getModel3DId(): number {
              throw new Error("Method not implemented.");
            }

            toString(): string {
              throw new Error("Method not implemented.");
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

            getGroundBabylonMaterialId(): number {
              return this._groundConfigJson.topThreeJsMaterial; // TODO
            };

            getWaterBabylonMaterialId(): number {
              return this._groundConfigJson.bottomThreeJsMaterial; // TODO
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

          getModel3DId(): number | null {
            return null;
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

          getModel3DId(): number | null {
            return null;
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

  readonly TILE_X_COUNT = 2;
  readonly TILE_Y_COUNT = 2;

  mockTerrainTile(threeJsRendererService: BabylonRenderServiceAccessImpl) {
    fetch('rest/terrainHeightMap/117', {
      headers: {
        'Content-Type': 'application/octet-stream'
      }
    }).then(response => response.arrayBuffer())
      .then(buffer => {
        const uint16Array = new Uint16Array(buffer);
        for (let x = 0; x < this.TILE_X_COUNT; x++) {
          for (let y = 0; y < this.TILE_Y_COUNT; y++) {
            let terrainTileIndex: Index = GwtInstance.newIndex(x, y)
            let heightMap = this.setupHeightMap(terrainTileIndex, uint16Array);

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

  private setupHeightMap(terrainTileIndex: Index, terrainHeightMap: Uint16Array): Uint16Array {
    let tileHeightMapStart = this.getTileHeightMapStart(terrainTileIndex);
    let nextXTileHeightMapStart = this.getTileHeightMapStart(terrainTileIndex.add(1, 0));
    let nextYTileHeightMapStart = this.getTileHeightMapStart(terrainTileIndex.add(0, 1));
    let nextXYTileHeightMapStart = this.getTileHeightMapStart(terrainTileIndex.add(1, 1));

    let resultArray = new Uint16Array((BabylonTerrainTileImpl.NODE_X_COUNT + 1) * (BabylonTerrainTileImpl.NODE_Y_COUNT + 1));

    for (let i = 0; i < BabylonTerrainTileImpl.NODE_Y_COUNT; i++) {
      let sourceYOffset = i * BabylonTerrainTileImpl.NODE_X_COUNT;
      let sourceHeightMapStart = tileHeightMapStart + sourceYOffset;
      let sourceHeightMapEnd = sourceHeightMapStart + BabylonTerrainTileImpl.NODE_X_COUNT;
      let destHeightMapStart = i * (BabylonTerrainTileImpl.NODE_X_COUNT + 1);
      try {
        let arrayBufferView = terrainHeightMap.slice(sourceHeightMapStart, sourceHeightMapEnd);
        resultArray.set(arrayBufferView, destHeightMapStart);
        // Add from next X tile
        let sourceNextTileHeightMapStart;
        if (terrainTileIndex.getX() + 1 < this.TILE_X_COUNT) {
          // Inside
          sourceNextTileHeightMapStart = nextXTileHeightMapStart + sourceYOffset;
        } else {
          // Outside
          sourceNextTileHeightMapStart = sourceHeightMapEnd + 1;
        }
        let arrayBufferViewEast = terrainHeightMap.slice(sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1);
        resultArray.set(arrayBufferViewEast, destHeightMapStart + BabylonTerrainTileImpl.NODE_X_COUNT);

        // Add last north row with next values
        if (i == BabylonTerrainTileImpl.NODE_Y_COUNT - 1) {
          if (terrainTileIndex.getY() + 1 < this.TILE_Y_COUNT) {
            let arrayBufferViewEastNorth = terrainHeightMap.slice(nextYTileHeightMapStart, nextYTileHeightMapStart + BabylonTerrainTileImpl.NODE_X_COUNT);
            resultArray.set(arrayBufferViewEastNorth, destHeightMapStart + BabylonTerrainTileImpl.NODE_X_COUNT + 1);
            // Add from next X tile
            if (terrainTileIndex.getX() + 1 < this.TILE_X_COUNT) {
              // Inside
              sourceNextTileHeightMapStart = nextXYTileHeightMapStart;
            } else {
              // Outside
              sourceNextTileHeightMapStart = nextYTileHeightMapStart + BabylonTerrainTileImpl.NODE_X_COUNT + 1;
            }
            let arrayBufferViewNorthEast = terrainHeightMap.slice(sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1);
            resultArray.set(arrayBufferViewNorthEast, destHeightMapStart + BabylonTerrainTileImpl.NODE_X_COUNT + 1 + BabylonTerrainTileImpl.NODE_X_COUNT);
          } else {
            resultArray.set(arrayBufferView, destHeightMapStart + BabylonTerrainTileImpl.NODE_X_COUNT + 1);
            // Add from next X tile
            if (terrainTileIndex.getX() + 1 < this.TILE_X_COUNT) {
              // Inside
              sourceNextTileHeightMapStart = nextXTileHeightMapStart + sourceYOffset;
            } else {
              // Outside
              sourceNextTileHeightMapStart = sourceHeightMapEnd;
            }
            let arrayBufferViewNorthEast = terrainHeightMap.slice(sourceNextTileHeightMapStart, sourceNextTileHeightMapStart + 1);
            resultArray.set(arrayBufferViewNorthEast, destHeightMapStart + BabylonTerrainTileImpl.NODE_X_COUNT + 1 + BabylonTerrainTileImpl.NODE_X_COUNT);
          }
        }
      } catch (error) {
        console.warn(error)
      }
    }
    return resultArray;
  }

  private getTileHeightMapStart(terrainTileIndex: Index): number {
    return terrainTileIndex.getY() * (this.TILE_X_COUNT * BabylonTerrainTileImpl.TILE_NODE_SIZE) + terrainTileIndex.getX() * BabylonTerrainTileImpl.TILE_NODE_SIZE;
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

  mockSelectionService(): SelectionService {
    return new class implements SelectionService {
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

