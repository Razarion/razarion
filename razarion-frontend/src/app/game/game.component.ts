import { Component, ElementRef, HostBinding, NgZone, OnInit, ViewChild } from '@angular/core';
import { FrontendService } from "../service/frontend.service";
import { NavigationStart, Router } from "@angular/router";
import { GwtAngularService } from "../gwtangular/GwtAngularService";
import { EditorModel } from "../editor/editor-model";
import { ItemCockpitComponent } from "./cockpit/item/item-cockpit.component";
import { MainCockpitComponent } from "./cockpit/main/main-cockpit.component";
import { CrashPanelComponent } from "../editor/crash-panel/crash-panel.component";
import { BabylonRenderServiceAccessImpl } from './renderer/babylon-render-service-access-impl.service';
import { environment } from 'src/environments/environment';
import { GameMockService } from './renderer/game-mock.service';
import { BabylonModelService } from './renderer/babylon-model.service';
import {
  ActionServiceListener,
  BaseItemPlacer,
  BaseItemType,
  BuilderType,
  Diplomacy,
  HarvesterType,
  I18nString,
  OwnItemCockpit,
  PhysicalAreaConfig,
  RadarState,
  ScreenCover,
  WeaponType,
} from "../gwtangular/GwtAngularFacade";
import { QuestCockpitComponent } from "./cockpit/quest/quest-cockpit.component";
import { ModelDialogPresenterImpl } from './model-dialog-presenter.impl';
import { GwtInstance } from '../gwtangular/GwtInstance';
import { ActionService } from './action.service';
import { MeshBuilder, Tools } from '@babylonjs/core';
import { TerrainEditorComponent } from '../editor/terrain-editor/terrain-editor.component';
import { GeneratedCrudContainerComponent } from '../editor/crud-editors/crud-container/generated-crud-container.component';
import { ParticleSystemEditorComponent } from '../editor/crud-editors/particle-system-editor/particle-system-editor.component';
import { BaseItemTypeEditorComponent } from '../editor/crud-editors/base-item-type-editor/base-item-type-editor.component';
import { PropertyEditorComponent } from '../editor/property-editor/property-editor.component';
import {GltfEditorComponent} from "../editor/crud-editors/gltf-editor/gltf-editor.component";


@Component({
  templateUrl: 'game.component.html',
  styleUrls: ['game.component.scss']
})
export class GameComponent implements OnInit, ScreenCover {
  @ViewChild('canvas', { static: true })
  canvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('mainCockpit', { static: true })
  mainCockpitComponent!: MainCockpitComponent;
  @ViewChild('itemCockpitContainer', { static: true })
  itemCockpitContainer!: ItemCockpitComponent;
  @ViewChild('questCockpitContainer', { static: true })
  questCockpitContainer!: QuestCockpitComponent;
  editorModels: EditorModel[] = [];
  @HostBinding("style.--cursor")
  cursor: string = '';
  fadeOutCover: boolean = false;
  removeCover: boolean = false;
  modelDialogPresenter: ModelDialogPresenterImpl;
  showInventory = false;
  showUnkock = false;

  constructor(private frontendService: FrontendService,
    private router: Router,
    private gwtAngularService: GwtAngularService,
    private babylonRenderServiceAccessImpl: BabylonRenderServiceAccessImpl,
    private babylonModelService: BabylonModelService,
    private actionService: ActionService,
    private gameMockService: GameMockService,
    private zone: NgZone) {
    this.modelDialogPresenter = new ModelDialogPresenterImpl(this.zone, gwtAngularService);
  }

  ngOnInit(): void {
    // this.loadingCover!.render = true;

    this.gwtAngularService.crashListener = () => this.addEditorModel(new EditorModel("Crash Information Panel", CrashPanelComponent));
    this.gwtAngularService.gwtAngularFacade.modelDialogPresenter = this.modelDialogPresenter;

    this.babylonRenderServiceAccessImpl.setup(this.canvas.nativeElement);

    if (environment.gwtMock) {
      let runGwtMock = true;
      this.gwtAngularService.gwtAngularFacade.baseItemUiService = this.gameMockService.mockBaseItemUiService;
      this.gwtAngularService.gwtAngularFacade.itemTypeService = this.gameMockService.mockItemTypeService();
      this.gwtAngularService.gwtAngularFacade.inventoryTypeService = this.gameMockService.mockInventoryTypeService();
      this.gwtAngularService.gwtAngularFacade.selectionService = this.gameMockService.mockSelectionService();
      this.gwtAngularService.gwtAngularFacade.gameUiControl = this.gameMockService.gameUiControl;
      if (runGwtMock) {
        this.gwtAngularService.gwtAngularFacade.inputService = this.gameMockService.inputService;
        this.gwtAngularService.gwtAngularFacade.statusProvider = this.gameMockService.statusProvider;
        this.gwtAngularService.gwtAngularFacade.threeJsModelPackService = this.gameMockService.mockThreeJsModelPackService;
        this.gameMockService.loadMockStaticGameConfig().then(() => {
          this.gameMockService.loadMockAssetConfig().then(() => {
            this.babylonModelService.init(this.gameMockService.mockThreeJsModelConfigs(), this.gameMockService.mockParticleSystemConfigs(), this.gwtAngularService).then(() => {
              this.gwtAngularService.gwtAngularFacade.terrainTypeService = this.gameMockService.mockTerrainTypeService();
              this.gameMockService.mockTerrainTile(this.babylonRenderServiceAccessImpl);
              this.mainCockpitComponent.show(true);
              this.mainCockpitComponent.showRadar(RadarState.WORKING);
              this.babylonRenderServiceAccessImpl.runRenderer(this.gameMockService.createMeshContainers());
              setTimeout(() => {
                // Some very strange babylon behavior, _projectionMatrix is zero matrix
                this.babylonRenderServiceAccessImpl.setViewFieldCenter(0, 0);
                this.fadeOutLoadingCover();
                setTimeout(() => {
                  // Some very strange babylon behavior, _projectionMatrix is zero matrix
                  this.removeLoadingCover();
                }, 2000);
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
                getI18nName(): I18nString {
                  return new class implements I18nString {
                    getString(): string {
                      return "I18nString";
                    }
                  };
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
                  return 0;
                }

                getInternalName(): string {
                  return "Builder";
                }

                getModel3DId(): number | null {
                  return 22743;
                }

                getMeshContainerId(): number | null {
                  return 22743;
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

                getThreeJsModelPackConfigId(): number | null {
                  return null;
                }

              };

              {
                let babylonBaseItem1 = this.babylonRenderServiceAccessImpl.createBabylonBaseItem(999999, baseItemType, Diplomacy.ENEMY);
                babylonBaseItem1.setPosition(GwtInstance.newDecimalPosition(8, 8));
                babylonBaseItem1.setAngle(Tools.ToRadians(45));

                babylonBaseItem1.select(false);

                babylonBaseItem1.setConstructing(0.01);
                babylonBaseItem1.setHealth(0.99);
                // babylonBaseItem1.mark(MarkerConfig);
                setTimeout(() => {
                  babylonBaseItem1.onExplode();
                }, 2000);


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

              this.itemCockpitContainer.displayOwnSingleType(1, new class implements OwnItemCockpit {
                buildupItemInfos = null;
                imageUrl = "/xxxxx";
                itemTypeDescr = "Builds Units";
                itemTypeName = "Factory";

                sellHandler(): void {
                }

              });

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

              setTimeout(() => {
                this.addEditorModel(new EditorModel("???", GeneratedCrudContainerComponent, GltfEditorComponent));
                // this.addEditorModel(new EditorModel("???", TerrainEditorComponent));
              }, 2000);
            });
          });
        });
      } else {
        this.mainCockpitComponent.show(true);
        this.mainCockpitComponent.showRadar(RadarState.NO_POWER);
        this.mainCockpitComponent.displayXps(5, 20);
        this.mainCockpitComponent.displayLevel(1)
        this.mainCockpitComponent.displayEnergy(0, 1);
        // this.addEditorModel(new EditorModel("???", TerrainEditorComponent));
        // this.addEditorModel(new EditorModel("???", PropertyEditorComponent));
        this.addEditorModel(new EditorModel("???", GeneratedCrudContainerComponent, GltfEditorComponent));
        // this.showInventory = true;
        this.fadeOutLoadingCover();
        this.removeLoadingCover();


        // this.gameMockService.showQuestSideBar(this.questCockpitContainer);
        // this.gameMockService.onQuestProgress(this.questCockpitContainer);
        // let questDialogVisible = false;
        // setInterval(() => {
        //  if (questDialogVisible) {
        //     this.gameMockService.showQuestSideBar(this.questCockpitContainer);
        //     this.gameMockService.onQuestProgress(this.questCockpitContainer);
        //   } else {
        //     this.gameMockService.hideQuestSideBar(this.questCockpitContainer);
        //   }
        //   questDialogVisible = !questDialogVisible;
        // }, 5000);

      }
      // setTimeout(() => {
      //   this.gwtAngularService.gwtAngularFacade.modelDialogPresenter.showLevelUp();
      // }, 100);
      // setTimeout(() => {
      //   this.gwtAngularService.gwtAngularFacade.modelDialogPresenter.showBaseLost();
      // }, 110);
      // setTimeout(() => {
      //   this.gwtAngularService.gwtAngularFacade.modelDialogPresenter.showQuestPassed();
      // }, 120);
      // setTimeout(() => {
      //   let boxContent = new class implements BoxContent {
      //     toInventoryItemArray(): InventoryItem[] {
      //       return [
      //         new class implements InventoryItem {
      //           getI18nName(): I18nString {
      //             return new class implements I18nString {
      //               getString(language: string): string {
      //                 return "3 viper pack";
      //               }
      //             };
      //           }
      //           getRazarion(): number | null {
      //             return null
      //           }
      //           getBaseItemTypeId(): number | null {
      //             return null
      //           }
      //           getBaseItemTypeCount(): number {
      //             throw 0;
      //           }
      //           getImageId(): number | null {
      //             return null
      //           }
      //
      //         }
      //       ];
      //     }
      //     getCrystals(): number {
      //       return 0;
      //     }
      //   };
      //   this.gwtAngularService.gwtAngularFacade.modelDialogPresenter.showBoxPicked(boxContent);
      // }, 100);
      // setTimeout(() => {
      //   this.gwtAngularService.gwtAngularFacade.baseItemPlacerPresenter.activate(new class implements BaseItemPlacer {
      //     isPositionValid(): boolean {
      //       return true;
      //     }

      //     getEnemyFreeRadius(): number {
      //       return 5;
      //     }

      //     onMove(xTerrainPosition: number, yTerrainPosition: number): void {
      //     }

      //     onPlace(xTerrainPosition: number, yTerrainPosition: number): void {
      //     }

      //   });
      // }, 1000);

    }
    this.gwtAngularService.gwtAngularFacade.screenCover = this;
    this.gwtAngularService.gwtAngularFacade.babylonRenderServiceAccess = this.babylonRenderServiceAccessImpl;
    this.gwtAngularService.gwtAngularFacade.actionServiceListener = this.actionService;
    this.gwtAngularService.gwtAngularFacade.mainCockpit = this.mainCockpitComponent;
    this.gwtAngularService.gwtAngularFacade.itemCockpitFrontend = this.itemCockpitContainer;
    this.gwtAngularService.gwtAngularFacade.questCockpit = this.questCockpitContainer;
    this.gwtAngularService.gwtAngularFacade.baseItemPlacerPresenter = this.babylonRenderServiceAccessImpl.createBaseItemPlacerPresenter();

    // Prevent running game in the background if someone press the browser history navigation button
    // Proper solution is to stop the game
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        window.location.href = event.url;
      }
    });
    // TODO if (!this.frontendService.isCookieAllowed()) {
    //   this.router.navigate(['/nocookies']);
    //   return;
    // }
    if (!environment.gwtMock) {
      this.frontendService.autoLogin().then(loggedIn => {
        this.startGame();
      });
    }

    // TODO remove
    // let ownItemCockpit: OwnItemCockpit = {
    //   buildupItemInfos: null,
    //   imageUrl: "/rest/image/45",
    //   itemTypeName: "Viper",
    //   itemTypeDescr: "Greift andere Einheiten an",
    //   sellButton: false
    // };
    // this.itemCockpitContainer.displayOwnSingleType(11, ownItemCockpit)
    // TODO remove ends

    // TODO if(this.loadingCover) {
    //    this.loadingCover.show(null, this.canvas);
    // }
  }

  fadeInLoadingCover(): void {
    throw new Error("Not Implemented fadeInLoadingCover()");
  }

  fadeOutLoadingCover(): void {
    this.zone.run(() => {
      this.fadeOutCover = true;
    });
  }

  hideStoryCover(): void {
    throw new Error("Not Implemented hideStoryCover()");
  }

  removeLoadingCover(): void {
    this.zone.run(() => {
      this.removeCover = true;
    });
  }

  showStoryCover(html: string): void {
    throw new Error("Not Implemented showStoryCover()");
  }

  private startGame(): void {
    GameComponent.insertGameScript('window.RAZ_startTime = new Date().getTime();');
    GameComponent.insertMeta('gwt:property', "locale=" + this.frontendService.getLanguage());
    GameComponent.loadGameScriptUrl('/NativeRazarion.js');
    GameComponent.loadGameScriptUrl('/com.btxtech.client.RazarionClient/com.btxtech.client.RazarionClient.nocache.js');
  }

  private static loadGameScriptUrl(url: string) {
    // Check if exits
    let scriptsElements = document.getElementsByTagName('script');
    for (let i = scriptsElements.length; i--;) {
      if (scriptsElements[i].src.startsWith(url)) {
        return;
      }
    }
    // Add
    let scriptObject = document.createElement('script');
    // scriptObject.src = 'http://localhost:8080' + url;
    scriptObject.src = url + '?t=' + new Date().getTime();
    scriptObject.type = 'text/javascript';
    scriptObject.charset = 'utf-8';
    document.getElementsByTagName('head')[0].appendChild(scriptObject);
  }

  private static insertGameScript(script: string) {
    let scriptObject = document.createElement('script');
    scriptObject.text = script;
    scriptObject.type = 'text/javascript';
    scriptObject.charset = 'utf-8';
    document.getElementsByTagName('head')[0].appendChild(scriptObject);
  }

  private static insertMeta(name: string, content: string) {
    let meta = document.createElement('meta');
    meta.name = name;
    meta.content = content;
    document.getElementsByTagName('head')[0].appendChild(meta);
  }

  getGameComponent(): GameComponent {
    return this;
  }

  addEditorModel(editorModel: EditorModel) {
    this.editorModels.push(editorModel);
    this.questCockpitContainer.showCockpit = false;
  }

  removeEditorModel(editorModel: EditorModel) {
    this.editorModels.splice(this.editorModels.indexOf(editorModel), 1);
  }

  openInventory() {
    this.showInventory = true;
  }

  openUnlock() {
    this.showUnkock = true;
  }


}

