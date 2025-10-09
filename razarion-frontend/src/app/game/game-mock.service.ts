import {Injectable} from "@angular/core";
import {
  Alarm,
  AngularZoneRunner,
  BabylonDecal,
  BabylonTerrainTile,
  BaseItemType,
  BaseItemUiService,
  BotGround,
  BuilderType,
  BuildupItemCockpit,
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
  InGameQuestVisualizationService,
  InputService,
  InventoryItem,
  InventoryTypeService,
  ItemContainerCockpit,
  ItemTypeService,
  NativeSyncBaseItemTickInfo,
  OtherItemCockpit,
  OwnItemCockpit,
  OwnMultipleIteCockpit,
  PhysicalAreaConfig,
  PlanetConfig,
  PlayerBaseDto,
  QuestConfig,
  QuestProgressInfo,
  RadarState,
  ResourceItemType,
  SelectionService,
  StatusProvider,
  TerrainObjectConfig,
  TerrainTile,
  TerrainTileObjectList,
  TerrainTypeService,
  TipConfig,
  WeaponType
} from "src/app/gwtangular/GwtAngularFacade";
import {GwtInstance} from "../gwtangular/GwtInstance";
import {ConditionTrigger} from "src/app/generated/razarion-share";
import {GwtAngularService} from "../gwtangular/GwtAngularService";
import {BabylonRenderServiceAccessImpl} from './renderer/babylon-render-service-access-impl.service';
import {BabylonTerrainTileImpl} from './renderer/babylon-terrain-tile.impl';
import {HttpClient} from '@angular/common/http';
import {BabylonModelService} from './renderer/babylon-model.service';
import {GameComponent} from './game.component';
import {EditorModel} from '../editor/editor-model';
import {TerrainEditorComponent} from '../editor/terrain-editor/terrain-editor.component';
import {CockpitDisplayService} from './cockpit/cockpit-display.service';
import {toRadians} from 'chart.js/helpers';
import {
  BabylonMaterialEditorComponent
} from '../editor/crud-editors/babylon-material-editor/babylon-material-editor.component';
import {
  GeneratedCrudContainerComponent
} from '../editor/crud-editors/crud-container/generated-crud-container.component';
import {ServerBotEditorComponent} from '../editor/server-bot-editor/server-bot-editor.component';

let staticGameConfigJson: any = {
  terrainObjectConfigs: []
};

let displayMockTerrainTile: BabylonTerrainTile[] = [];

@Injectable({
  providedIn: 'root',
})
export class GameMockService {
  private gameComponent!: GameComponent;

  constructor(private http: HttpClient,
              private gwtAngularService: GwtAngularService,
              private babylonModelService: BabylonModelService,
              private babylonRenderServiceAccessImpl: BabylonRenderServiceAccessImpl,
              private mainCockpitService: CockpitDisplayService) {
  }

  startGame(runGwtMock: boolean, gameComponent: GameComponent) {
    this.gameComponent = gameComponent;
    this.initMocks();

    if (runGwtMock) {
      this.startGwtMock();
    } else {
      this.simulateStartup();
    }
  }

  private startGwtMock() {
    this.gwtAngularService.gwtAngularFacade.inputService = this.inputService;
    this.gwtAngularService.gwtAngularFacade.statusProvider = this.statusProvider;
    this.gwtAngularService.gwtAngularFacade.inGameQuestVisualizationService = this.inGameQuestVisualizationService;
    this.loadMockStaticGameConfig().then(() => {
      this.babylonModelService.init().then(() => {
        this.gwtAngularService.gwtAngularFacade.terrainTypeService = this.mockTerrainTypeService();
        this.mockTerrainTile(this.babylonRenderServiceAccessImpl);
        this.gwtAngularService.gwtAngularFacade.mainCockpit.show()
        this.gwtAngularService.gwtAngularFacade.mainCockpit.showRadar(RadarState.WORKING)
        this.babylonRenderServiceAccessImpl.runRenderer();
        setTimeout(() => {
          // Some very strange babylon behavior, _projectionMatrix is zero matrixd
          this.babylonRenderServiceAccessImpl.setViewFieldCenter(74, 40);
          this.gwtAngularService.gwtAngularFacade.screenCover.removeLoadingCover();
          this.gameComponent.addEditorModel(new EditorModel("Bot Editor", ServerBotEditorComponent));
          // this.gameComponent.addEditorModel(new EditorModel("BabylonMaterial", GeneratedCrudContainerComponent, BabylonMaterialEditorComponent));
        }, 100);
        // this.loadingCover!.hide();
        // this.threeJsRendererService.createProjectile(new class implements Vertex {
        //   getX(): number {
        //     return 0;
        //   }
        //
        //   getY(): number {
        //     return 0;
        //   }
        //
        //   getZ(): number {
        //     return 0;
        //   }
        // }, new class implements Vertex {
        //   getX(): number {
        //     return 8;
        //   }
        //
        //   getY(): number {
        //     return 4;
        //   }
        //
        //   getZ(): number {
        //     return 1;
        //   }
        // }, 2);

        let baseItemType = new class implements BaseItemType {
          getName(): string {
            return "BaseItemType";
          }

          getDescription(): string {
            return "BaseItemType description";
          }

          getBuilderType(): BuilderType {
            return new class implements BuilderType {
              getParticleSystemConfigId(): number | null {
                return 1;
              }
            }
          }

          getHarvesterType(): HarvesterType {
            return new class implements HarvesterType {
              getParticleSystemConfigId(): number | null {
                return 2;
              }
            }
          }

          getWeaponType(): WeaponType {
            return new class implements WeaponType {
              getProjectileSpeed(): number | null {
                return 30;
              }

              getMuzzleFlashParticleSystemConfigId(): number | null {
                return 2;
              }

              getTrailParticleSystemConfigId(): number | null {
                return 2;
              }
            }
          }

          getExplosionParticleId(): number | null {
            return 1;
          }

          getId(): number {
            return 12;
          }

          getInternalName(): string {
            return "Builder";
          }

          getModel3DId(): number | null {
            return 37;
          }

          getPhysicalAreaConfig(): PhysicalAreaConfig {
            return new class implements PhysicalAreaConfig {
              getRadius(): number {
                return 2;
              }

              fulfilledMovable(): boolean {
                return true;
              }
            };
          }
        };

        {
          let babylonBaseItem1 = this.babylonRenderServiceAccessImpl.createBabylonBaseItem(999999, baseItemType, 1, Diplomacy.OWN, "myName");
          babylonBaseItem1.setPosition(GwtInstance.newVertex(70, 40, 1.6));
          // babylonBaseItem1.setAngle(Tools.ToRadians(45));
          babylonBaseItem1.setAngle(0);

          babylonBaseItem1.select(false);

          babylonBaseItem1.setConstructing(0.01);
          babylonBaseItem1.setHealth(0.99);
          // babylonBaseItem1.mark(MarkerConfig);
          // setTimeout(() => {
          //  babylonBaseItem1.onExplode();
          //}, 2000);


          /*
          let x = 0;
          setInterval(() => {
            babylonBaseItem1.setPosition(GwtInstance.newDecimalPosition(8, 8));
            babylonBaseItem1.setAngle(Tools.ToRadians(x));
            x += 20;
            if (x > 360) {
              x = 0;
            }
          }, 100)
          */
          // setInterval(() => babylonBaseItem.setConstructing((Date.now() % 5000) / 5000), 500);
          // setInterval(() => babylonBaseItem1.setHealth(1.0 - (Date.now() % 10000) / 10000), 2000);
        }
        {
          let botGround = this.babylonModelService.cloneModel3D(45, null, Diplomacy.OWN);
          botGround.position.x = 72;
          botGround.position.y = -0.5;
          botGround.position.z = 26;
          botGround.rotationQuaternion = null;
          botGround.rotation.x = toRadians(-22.5);

          botGround = this.babylonModelService.cloneModel3D(45, null, Diplomacy.OWN);
          botGround.position.x = 83.4;
          botGround.position.y = -1;
          botGround.position.z = 38;
          botGround.rotationQuaternion = null;
          botGround.rotation.z = toRadians(-15);

        }
        this.showQuestionCockpit();
        // {
        //   let babylonBaseItem2 = this.babylonRenderServiceAccessImpl.createBabylonBaseItem(999998, baseItemType, Diplomacy.ENEMY);
        //   babylonBaseItem2.setPosition(GwtInstance.newVertex(8, 14, 0));
        //   babylonBaseItem2.setAngle(0);
        //
        //   babylonBaseItem2.updatePosition();
        //   babylonBaseItem2.updateAngle();
        //
        //   babylonBaseItem2.select(true);
        //
        //   babylonBaseItem2.setConstructing(0.33);
        //   babylonBaseItem2.setHealth(0.66);
        //
        //   setInterval(() => babylonBaseItem2.setConstructing((Date.now() % 5000) / 5000), 500);
        //   setInterval(() => babylonBaseItem2.setHealth((Date.now() % 10000) / 10000), 2000);
        // }
        // {
        //   let babylonBaseItem3 = this.babylonRenderServiceAccessImpl.createBabylonBaseItem(999997, baseItemType, Diplomacy.ENEMY);
        //   babylonBaseItem3.setPosition(GwtInstance.newVertex(8, 20, 0));
        //   babylonBaseItem3.setAngle(0);
        //
        //   babylonBaseItem3.updatePosition();
        //   babylonBaseItem3.updateAngle();
        //
        //   babylonBaseItem3.select(true);
        //
        //   babylonBaseItem3.setConstructing(0.99);
        //   babylonBaseItem3.setHealth(0.01);
        //
        //   // setInterval(() => babylonBaseItem3.setConstructing((Date.now() % 5000) / 5000), 500);
        //   // setInterval(() => babylonBaseItem3.setHealth(1.0 - ((Date.now() + 1000) % 10000) / 10000), 2000);
        // }
        // let buildingPosition: NativeVertexDto = new class implements NativeVertexDto {
        //   x = 16;
        //   y = 8;
        //   z = 0;
        // };
        // babylonBaseItem.setBuildingPosition(buildingPosition);

        // babylonBaseItem.onExplode();
        // setInterval(() => babylonBaseItem.onExplode(), 4000)


        // const pbr = new PBRMetallicRoughnessMaterial("pbr", this.threeJsRendererService.getScene());
        // pbr.baseColor = new Color3(1.0, 0.766, 0.336);
        // pbr.metallic = 1.0;
        // pbr.roughness = 0.0;
        //
        // const sphere = MeshBuilder.CreateSphere("TestSphere", {diameter: 4}, this.threeJsRendererService.getScene());
        // sphere.material = pbr;
        // sphere.position. y = 4;
        // sphere.position. x = 8;
        // const plane = MeshBuilder.CreatePlane("TestPlane", {size: 10}, this.threeJsRendererService.getScene());
        // plane.material = pbr;
        // plane.rotation.x = Tools.ToRadians(90);
        // plane.position. y = 0.2;
        // this.threeJsRendererService.getScene().environmentTexture = CubeTexture.CreateFromPrefilteredData("https://playground.babylonjs.com/textures/environment.dds", this.threeJsRendererService.getScene());

        // babylonBaseItem.select(true);
        // let i = 0.1;
        // let move = () => {
        //   babylonBaseItem.updatePosition(271, 290 + i, 0, 0)
        //   i += 0.1
        //   if (i > 2) {
        //     babylonBaseItem.select(false);
        //     return;
        //   }
        //   setTimeout(move, 100)
        // }
        // setTimeout(move, 100)

        let resourceItemType = new class implements ResourceItemType {
          getName(): string {
            return "";
          }

          getDescription(): string {
            return "";
          }

          getRadius(): number {
            return 2;
          }

          getInternalName(): string {
            return "ResourceItemType";
          }

          getId(): number {
            return 999999;
          }

          getModel3DId(): number | null {
            return 42;
          }
        }

        // let babylonResourceItem1 = this.babylonRenderServiceAccessImpl.createBabylonResourceItem(999999, resourceItemType);
        // // babylonResourceItem1.setPosition(GwtInstance.newDecimalPosition(8, 8));
        // //babylonResourceItem1.setAngle(Tools.ToRadians(45));
        // babylonResourceItem1.select(false);
        // setTimeout(() => {
        //   babylonResourceItem1.setPosition(GwtInstance.newDecimalPosition(8, 8));
        // }, 1000);
        //
        //   this.itemCockpitContainer.displayOwnSingleType(1, new class implements OwnItemCockpit {
        //   buildupItemInfos = null;
        //   imageUrl = "/xxxxx";
        //   itemTypeDescr = "Builds Units";
        //   itemTypeName = "Factory";
        //
        //   sellHandler(): void {
        //   }
        //
        // });

        // this.babylonRenderServiceAccessImpl.showOutOfViewMarker(new class implements MarkerConfig {
        //   radius = 1;
        //  nodesMaterialId = null;
        //  placeNodesMaterialId = null;
        //  outOfViewNodesMaterialId = 1;
        //  outOfViewDistanceFromCamera = 3;
        //  outOfViewSize = 1;
        // }, 0);

        // this.babylonRenderServiceAccessImpl.showPlaceMarker(
        //   new class implements PlaceConfig {
        //     getPolygon2D(): Polygon2D | null {
        //       /*
        //       return new class implements Polygon2D {
        //         toCornersAngular(): DecimalPosition[] {
        //           return [
        //             GwtInstance.newDecimalPosition(10, 10),
        //             GwtInstance.newDecimalPosition(80, 30),
        //             GwtInstance.newDecimalPosition(80, 80),
        //             GwtInstance.newDecimalPosition(20, 80)
        //           ];
        //         }
        //
        //       };
        //       */
        //       return null;
        //     }
        //     getPosition(): DecimalPosition | null {
        //       return GwtInstance.newDecimalPosition(10, 10);
        //     }
        //
        //     toRadiusAngular(): number {
        //       return 10;
        //     }
        //
        //   },
        //   new class implements MarkerConfig {
        //     radius = 1;
        //     nodesMaterialId = null;
        //     placeNodesMaterialId = 1;
        //     outOfViewNodesMaterialId = 1;
        //     outOfViewDistanceFromCamera = 3;
        //     outOfViewSize = 1;
        //   });

      });
    });
  }

  private initMocks() {
    this.gwtAngularService.gwtAngularFacade.baseItemUiService = this.mockBaseItemUiService;
    this.gwtAngularService.gwtAngularFacade.itemTypeService = this.mockItemTypeService();
    this.gwtAngularService.gwtAngularFacade.inventoryTypeService = this.mockInventoryTypeService();
    this.gwtAngularService.gwtAngularFacade.selectionService = this.mockSelectionService();
    this.gwtAngularService.gwtAngularFacade.gameUiControl = this.gameUiControl;
  }

  private simulateStartup() {
    // this.showLoading();

    this.showUi();

    // setInterval(() => {
    //   // this.gwtAngularService.gwtAngularFacade.modelDialogPresenter.showLevelUp();
    //   this.gwtAngularService.gwtAngularFacade.modelDialogPresenter.showQuestPassed();
    // }, 3000)
    // this.showUi();

    // this.showEditor();
  }

  private showUserDialog() {
    this.mainCockpitService.showUserDialog = true
  }

  private showLoading() {
    setTimeout(() => {
      this.gwtAngularService.gwtAngularFacade.screenCover.onStartupProgress(25);
    }, 1000);
    setTimeout(() => {
      this.gwtAngularService.gwtAngularFacade.screenCover.onStartupProgress(50);
    }, 2000);
    setTimeout(() => {
      this.gwtAngularService.gwtAngularFacade.screenCover.onStartupProgress(75);
    }, 3000);
    setTimeout(() => {
      this.gwtAngularService.gwtAngularFacade.screenCover.onStartupProgress(100);
      this.fakeRenderImageRemoveLoadingCover();
    }, 4000);
  }

  private showUi() {
    setTimeout(() => {
      this.fakeRenderImageRemoveLoadingCover();
      this.showMainCockpit();

      this.showQuestionCockpit();
      // this.displayOwnMultipleItemTypesCockpit();
      this.displayOwnSingleTypeCockpit();
      // this.displayOtherItemTypeCockpit();

      this.showUserDialog();
    }, 10);
  }

  private fakeRenderImageRemoveLoadingCover() {
    const element = document.querySelector('.game-main') as HTMLElement;
    if (element) {
      element.style.backgroundImage = `url(${"cockpit/MockGameBackground.jpg"})`;
      element.style.backgroundSize = 'cover';
      element.style.backgroundPosition = 'center';
    }
    this.gwtAngularService.gwtAngularFacade.screenCover.removeLoadingCover();
  }

  private displayOwnSingleTypeCockpit() {
    let buildupItemInfos: BuildupItemCockpit[] = [];
    for (let i = 0; i < 7; i++) {
      buildupItemInfos.push(this.buildBuildupItemCockpit(i));
    }

    this.gwtAngularService.gwtAngularFacade.itemCockpitFrontend.displayOwnSingleType(1, new class implements OwnItemCockpit {
      imageUrl = "/xxxxx";
      itemTypeDescr = "Builds Units";
      itemTypeName = "Factory";
      buildupItemInfos = buildupItemInfos;
      itemContainerInfo: ItemContainerCockpit = new class implements ItemContainerCockpit {
        count = 5;

        onUnload() {
        }

        setAngularZoneRunner(angularZoneRunner: AngularZoneRunner): void {
        }
      };

      sellHandler(): void {
      }
    });
  }

  private buildBuildupItemCockpit(price: number): BuildupItemCockpit {
    return new class implements BuildupItemCockpit {
      imageUrl = "/xxxxx";
      itemTypeId = 12;
      itemTypeName = "Builder";
      price = price;
      itemCount = 13;
      itemLimit = 14;
      enabled = true;
      buildLimitReached = false;
      buildHouseSpaceReached = false;
      buildNoMoney = false;
      progress = 1;

      setAngularZoneRunner(angularZoneRunner: AngularZoneRunner): void {
      }

      onBuild(): void {
      }
    };
  }

  private displayOtherItemTypeCockpit() {
    this.gwtAngularService.gwtAngularFacade.itemCockpitFrontend.displayOtherItemType(new class implements OtherItemCockpit {
      id = 64;
      imageUrl = "/xxxxx";
      itemTypeName = "Builder";
      itemTypeDescr = "Builds Units";
      baseId = 15;
      baseName = "Bot base";
      type = "Bot enemy";
      friend = false;
      bot = false;
      resource = false;
      box = true;
    });
  }

  private displayOwnMultipleItemTypesCockpit() {
    let ownMultipleIteCockpits: OwnMultipleIteCockpit[] = [];
    for (let i = 0; i < 3; i++) {
      ownMultipleIteCockpits.push(new class implements OwnMultipleIteCockpit {
        ownItemCockpit = new class implements OwnItemCockpit {
          itemContainerInfo: ItemContainerCockpit = new class implements ItemContainerCockpit {
            count = 5;

            onUnload() {
            }

            setAngularZoneRunner(angularZoneRunner: AngularZoneRunner): void {
            }
          };
          imageUrl = "/xxxxx";
          itemTypeName = "Builder";
          itemTypeDescr = "Builds Units";
          buildupItemInfos = null;

          sellHandler(): void {
            throw new Error("Method not implemented.");
          };
        };
        count = 12;

        onSelect(): void {
          throw new Error("Method not implemented.");
        }
      });
    }

    this.gwtAngularService.gwtAngularFacade.itemCockpitFrontend.displayOwnMultipleItemTypes(ownMultipleIteCockpits);
  }

  private showMainCockpit() {
    this.gwtAngularService.gwtAngularFacade.mainCockpit.showRadar(RadarState.NO_POWER);
    this.gwtAngularService.gwtAngularFacade.mainCockpit.displayXps(5, 20);
    this.gwtAngularService.gwtAngularFacade.mainCockpit.displayLevel(1)
    this.gwtAngularService.gwtAngularFacade.mainCockpit.displayEnergy(0, 1);
    this.gwtAngularService.gwtAngularFacade.mainCockpit.show()
  }

  private showQuestionCockpit() {
    this.gwtAngularService.gwtAngularFacade.questCockpit.showQuestSideBar(new class implements QuestConfig {
      getId(): number {
        return 0;
      }

      getInternalName(): string {
        return '';
      }

      getTipConfig(): TipConfig | null {
        // return new class implements TipConfig {
        //   getTipString(): Tip {
        //     return Tip.BUILD;
        //   }
        //
        //   getActorItemTypeId(): number {
        //     return 12;
        //   }
        //
        // };
        return null;
      }

      getConditionConfig(): ConditionConfig {
        return new class implements ConditionConfig {
          getComparisonConfig(): ComparisonConfig {
            return new class implements ComparisonConfig {
              getCount(): number | null {
                return 10;
              }

              toTypeCountAngular(): number[][] {
                return [[1, 2]]
              }

              getTimeSeconds(): number | null {
                return null;
              }
            };
          }

          getConditionTrigger(): ConditionTrigger {
            return ConditionTrigger.SYNC_ITEM_CREATED;
          }
        }
      }

      // getConditionConfig(): ConditionConfig {
      //   return new class implements ConditionConfig {
      //     getComparisonConfig(): ComparisonConfig {
      //       return new class implements ComparisonConfig {
      //         getCount(): number | null {
      //           return 10;
      //         }
      //
      //         toTypeCountAngular(): number[][] {
      //           return [[1, 2]]
      //         }
      //
      //         getTimeSeconds(): number | null {
      //           return null;
      //         }
      //       };
      //     }
      //
      //     getConditionTrigger(): ConditionTrigger {
      //       return ConditionTrigger.HARVEST;
      //     }
      //   }
      // }
    }, true)

    this.gwtAngularService.gwtAngularFacade.questCockpit.onQuestProgress(new class implements QuestProgressInfo {
      getBotBasesInformation(): string | null {
        return null;
      }

      getCount(): number | null {
        return 2;
      }

      getSecondsRemaining(): number | null {
        return 25;
      }

      toTypeCountAngular(): number[][] {
        return [[1, 2], [2, 2]];
      }
    });
  }

  // --------------------------------------- old ---------------------------------------

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

  inGameQuestVisualizationService: InGameQuestVisualizationService = new class implements InGameQuestVisualizationService {
    setVisible(visible: boolean): void {
    }
  };

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
            }

            getInternalName(): string {
              return this._groundConfigJson.internalName
            }

            getGroundBabylonMaterialId(): number {
              return this._groundConfigJson.groundBabylonMaterialId;
            }

            getWaterBabylonMaterialId(): number {
              return this._groundConfigJson.waterBabylonMaterialId;
            }

            getUnderWaterBabylonMaterialId(): number {
              return this._groundConfigJson.underWaterBabylonMaterialId;
            }

            getBotBabylonMaterialId(): number {
              return this._groundConfigJson.botBabylonMaterialId;
            }

            getBotWallBabylonMaterialId(): number {
              return this._groundConfigJson.botWallBabylonMaterialId;
            }
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
          getName(): string {
            return `BaseItemType '${baseItemTypeId}'`;
          }

          getDescription(): string {
            return "";
          }

          getBuilderType(): BuilderType | null {
            return null;
          }

          getHarvesterType(): HarvesterType | null {
            return null;
          }

          getId(): number {
            return baseItemTypeId;
          }

          getInternalName(): string {
            return this.getName();
          }

          getModel3DId(): number | null {
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

          getWeaponType(): WeaponType | null {
            return null;
          }
        }
      }

      getResourceItemTypeAngular(resourceItemTypeId: number): ResourceItemType {
        return new class implements ResourceItemType {
          getId(): number {
            return 0;
          }

          getInternalName(): string {
            return "";
          }

          getName(): string {
            return "";
          }

          getDescription(): string {
            return "";
          }

          getModel3DId(): number | null {
            return null;
          }

          getRadius(): number {
            return 0;
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
    fetch('/rest/terrainHeightMap/117', {
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
                return 252;
              }

              getTerrainTileObjectLists(): TerrainTileObjectList[] {
                return [];
              }

              getBabylonDecals(): BabylonDecal[] {
                return [{
                  babylonMaterialId: 11,
                  xPos: 28,
                  yPos: 19,
                  xSize: 21,
                  ySize: 21
                }];
              }

              getBotGrounds(): BotGround[] {
                const positions = [];
                for (let x = 0; x < 5; x++) {
                  for (let y = 0; y < 5; y++) {
                    positions.push(GwtInstance.newDecimalPosition(64 + x * 4, 30 + y * 4))
                  }
                }
                const botGround = {
                  model3DId: 45,
                  height: 0.6,
                  positions: positions
                }

                return [botGround];
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

  // Copied from com.btxtech.common.ClientNativeTerrainShapeAccess.createTileGroundHeightMap()
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

      canContain(itemId: number) {
        return false;
      }

      canBeFinalizeBuild(itemId: number) {
        return false;
      }

      selectRectangle(xStart: number, yStart: number, width: number, height: number): void {
        console.info("selectRectangle");
      }

      setSelectionListener(callback: () => void): void {
      }
    }
  }

  private showEditor() {
    setTimeout(() => {
      this.fakeRenderImageRemoveLoadingCover();
      // this.gameComponent.addEditorModel(new EditorModel("Level editor", LevelEditorComponent));
      // this.gameComponent.addEditorModel(new EditorModel("Start region editor", ServerStartRegionComponent));
      // this.gameComponent.addEditorModel(new EditorModel("???", GeneratedCrudContainerComponent, GltfEditorComponent));
      this.gameComponent.addEditorModel(new EditorModel("Terrain Editor", TerrainEditorComponent));
    }, 100);
  }
}

