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
  AngularCursorService,
  BaseItemType,
  BuilderType,
  ComparisonConfig,
  ConditionConfig,
  ConditionTrigger,
  CursorType,
  Diplomacy,
  HarvesterType,
  I18nString,
  OwnItemCockpit,
  PhysicalAreaConfig,
  QuestConfig,
  QuestProgressInfo,
  RadarState,
  ScreenCover,
  WeaponType
} from "../gwtangular/GwtAngularFacade";
import { GwtInstance } from "../gwtangular/GwtInstance";
import { GwtHelper } from "../gwtangular/GwtHelper";
import { QuestCockpitComponent } from "./cockpit/quest/quest-cockpit.component";
import { LevelEditorComponent } from '../editor/crud-editors/level-editor/level-editor.component';
import {
  GeneratedCrudContainerComponent
} from "../editor/crud-editors/crud-container/generated-crud-container.component";
import { BaseItemTypeEditorComponent } from '../editor/crud-editors/base-item-type-editor/base-item-type-editor.component';
import { ServerQuestEditorComponent } from '../editor/server-quest-editor/server-quest-editor.component';
import { BaseMgmtComponent } from '../editor/base-mgmt/base-mgmt.component';


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

  constructor(private frontendService: FrontendService,
    private router: Router,
    private gwtAngularService: GwtAngularService,
    private threeJsRendererService: BabylonRenderServiceAccessImpl,
    private threeJsModelService: BabylonModelService,
    private gameMockService: GameMockService,
    private zone: NgZone) {
  }

  ngOnInit(): void {
    // this.loadingCover!.render = true;

    this.gwtAngularService.crashListener = () => this.addEditorModel(new EditorModel("Crash Information Panel", CrashPanelComponent));

    this.threeJsRendererService.setup(this.canvas.nativeElement);

    if (environment.gwtMock) {
      let runGwtMock = false;
      this.gwtAngularService.gwtAngularFacade.baseItemUiService = this.gameMockService.mockBaseItemUiService;
      if (runGwtMock) {
        this.gwtAngularService.gwtAngularFacade.gameUiControl = this.gameMockService.gameUiControl;
        this.gwtAngularService.gwtAngularFacade.inputService = this.gameMockService.inputService;
        this.gwtAngularService.gwtAngularFacade.statusProvider = this.gameMockService.statusProvider;
        this.gwtAngularService.gwtAngularFacade.editorFrontendProvider = this.gameMockService.editorFrontendProvider;
        this.gwtAngularService.gwtAngularFacade.threeJsModelPackService = this.gameMockService.mockThreeJsModelPackService;
        this.gameMockService.loadMockStaticGameConfig().then(() => {
          this.gameMockService.loadMockAssetConfig().then(() => {
            this.threeJsModelService.init(this.gameMockService.mockThreeJsModelConfigs(), this.gameMockService.mockParticleSystemConfigs(), this.gwtAngularService).then(() => {
              this.gwtAngularService.gwtAngularFacade.terrainTypeService = this.gameMockService.mockTerrainTypeService();
              this.gwtAngularService.gwtAngularFacade.itemTypeService = this.gameMockService.mockItemTypeService();
              this.gameMockService.mockTerrainTile(this.threeJsRendererService);
              this.mainCockpitComponent.show(true);
              this.mainCockpitComponent.showRadar(RadarState.WORKING);
              this.threeJsRendererService.runRenderer(this.gameMockService.createMeshContainers());
              setTimeout(() => {
                // Some very strange babylon behavior, _projectionMatrix is zero matrix
                this.threeJsRendererService.setViewFieldCenter(5, 2);
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
                    getString(language: string): string {
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
                  }
                }

                getId(): number {
                  return 0;
                }

                getInternalName(): string {
                  return "Builder";
                }

                getMeshContainerId(): number | null {
                  return 22743;
                }

                getPhysicalAreaConfig(): PhysicalAreaConfig {
                  return new class implements PhysicalAreaConfig {
                    getRadius(): number {
                      return 2;
                    }
                  };
                }

                getThreeJsModelPackConfigId(): number | null {
                  return null;
                }

              };

              {
                let babylonBaseItem1 = this.threeJsRendererService.createBabylonBaseItem(999999, baseItemType, Diplomacy.ENEMY);
                babylonBaseItem1.setPosition(GwtInstance.newVertex(8, 8, 0));
                babylonBaseItem1.setAngle(0);

                babylonBaseItem1.updatePosition();
                babylonBaseItem1.updateAngle();

                babylonBaseItem1.select(true);

                babylonBaseItem1.setConstructing(0.01);
                babylonBaseItem1.setHealth(0.99);

                // setInterval(() => babylonBaseItem.setConstructing((Date.now() % 5000) / 5000), 500);
                // setInterval(() => babylonBaseItem1.setHealth(1.0 - (Date.now() % 10000) / 10000), 2000);
              }
              {
                let babylonBaseItem2 = this.threeJsRendererService.createBabylonBaseItem(999998, baseItemType, Diplomacy.ENEMY);
                babylonBaseItem2.setPosition(GwtInstance.newVertex(8, 14, 0));
                babylonBaseItem2.setAngle(0);

                babylonBaseItem2.updatePosition();
                babylonBaseItem2.updateAngle();

                babylonBaseItem2.select(true);

                babylonBaseItem2.setConstructing(0.33);
                babylonBaseItem2.setHealth(0.66);

                setInterval(() => babylonBaseItem2.setConstructing((Date.now() % 5000) / 5000), 500);
                setInterval(() => babylonBaseItem2.setHealth((Date.now() % 10000) / 10000), 2000);
              }
              {
                let babylonBaseItem3 = this.threeJsRendererService.createBabylonBaseItem(999997, baseItemType, Diplomacy.ENEMY);
                babylonBaseItem3.setPosition(GwtInstance.newVertex(8, 20, 0));
                babylonBaseItem3.setAngle(0);

                babylonBaseItem3.updatePosition();
                babylonBaseItem3.updateAngle();

                babylonBaseItem3.select(true);

                babylonBaseItem3.setConstructing(0.99);
                babylonBaseItem3.setHealth(0.01);

                // setInterval(() => babylonBaseItem3.setConstructing((Date.now() % 5000) / 5000), 500);
                // setInterval(() => babylonBaseItem3.setHealth(1.0 - ((Date.now() + 1000) % 10000) / 10000), 2000);
              }
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

              this.questCockpitContainer.showQuestSideBar(new class implements QuestConfig {
                getConditionConfig(): ConditionConfig | null {
                  return new class implements ConditionConfig {
                    getConditionTrigger(): ConditionTrigger {
                      return ConditionTrigger.SYNC_ITEM_POSITION;
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
                  return "Build";
                }

                getDescription(): string {
                  return "Build a Factory";
                }

              }, new class implements QuestProgressInfo {
                getBotBasesInformation(): string | null {
                  return null;
                }

                getCount(): number | null {
                  return 1;
                }

                getSecondsRemaining(): number | null {
                  return null;
                }

                toTypeCountAngular(): number[][] {
                  return [];
                }

              },
                false)
            });
          });
        });
      } else {
        this.gwtAngularService.gwtAngularFacade.editorFrontendProvider = this.gameMockService.editorFrontendProvider;
        this.mainCockpitComponent.show(true);
        this.mainCockpitComponent.showRadar(RadarState.NO_POWER);
        this.mainCockpitComponent.displayXps(5, 20);
        this.mainCockpitComponent.displayLevel(1)
        this.mainCockpitComponent.displayEnergy(2,10);
        this.addEditorModel(new EditorModel("???", BaseMgmtComponent));
        this.fadeOutLoadingCover();
        this.removeLoadingCover();
      }
    }
    this.gwtAngularService.gwtAngularFacade.screenCover = this;
    this.gwtAngularService.gwtAngularFacade.threeJsRendererServiceAccess = this.threeJsRendererService;
    this.gwtAngularService.gwtAngularFacade.angularCursorService = this.createAngularCursorService();
    this.gwtAngularService.gwtAngularFacade.mainCockpit = this.mainCockpitComponent;
    this.gwtAngularService.gwtAngularFacade.itemCockpitFrontend = this.itemCockpitContainer;
    this.gwtAngularService.gwtAngularFacade.questCockpit = this.questCockpitContainer;
    this.gwtAngularService.gwtAngularFacade.baseItemPlacerPresenter = this.threeJsRendererService.createBaseItemPlacerPresenter();

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
    GameComponent.insertGameScript('erraiBusRemoteCommunicationEnabled = false;');
    GameComponent.insertGameScript('erraiJaxRsJacksonMarshallingActive = true;');
    GameComponent.loadGameScriptUrl('/NativeRazarion.js');
    GameComponent.loadGameScriptUrl('/razarion_client/razarion_client.nocache.js');
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

  private createAngularCursorService(): AngularCursorService {
    let gameComponent = this;
    return new class implements AngularCursorService {
      setCursor(cursorType: CursorType, allowed: boolean): void {
        cursorType = GwtHelper.gwtIssueStringEnum(cursorType, CursorType);
        switch (cursorType) {
          case CursorType.GO:
            if (allowed) {
              gameComponent.cursor = "url(\"/assets/cursors/go.png\"), auto"
            } else {
              gameComponent.cursor = "url(\"/assets/cursors/go-no.png\"), auto"
            }
            break;
          case CursorType.ATTACK:
            if (allowed) {
              gameComponent.cursor = "url(\"/assets/cursors/attack.png\"), auto"
            } else {
              gameComponent.cursor = "url(\"/assets/cursors/attack-no.png\"), auto"
            }
            break;
          case CursorType.COLLECT:
            if (allowed) {
              gameComponent.cursor = "url(\"/assets/cursors/collect.png\"), auto"
            } else {
              gameComponent.cursor = "url(\"/assets/cursors/collect-no.png\"), auto"
            }
            break;
          default:
            gameComponent.cursor = "default"
            console.warn(`Unknown cursorType ${cursorType}`)
        }
      }

      setDefaultCursor(): void {
        gameComponent.cursor = "default"
      }

      setPointerCursor(): void {
        gameComponent.cursor = "pointer"
      }
    }
  }
}

