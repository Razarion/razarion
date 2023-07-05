import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {FrontendService} from "../service/frontend.service";
import {NavigationStart, Router} from "@angular/router";
import {GwtAngularService} from "../gwtangular/GwtAngularService";
import {EditorModel} from "../editor/editor-model";
import {ItemCockpitComponent} from "./cockpit/item/item-cockpit.component";
import {MainCockpitComponent} from "./cockpit/main/main-cockpit.component";
import {CrashPanelComponent} from "../editor/crash-panel/crash-panel.component";
import {ThreeJsRendererServiceImpl} from './renderer/three-js-renderer-service.impl';
import {environment} from 'src/environments/environment';
import {GameMockService} from './renderer/game-mock.service';
import {BabylonModelService} from './renderer/babylon-model.service';
import {
  BaseItemType,
  BuilderType,
  Diplomacy,
  NativeVertexDto,
  PhysicalAreaConfig,
  WeaponType
} from "../gwtangular/GwtAngularFacade";
import {GwtInstance} from "../gwtangular/GwtInstance";


@Component({
  templateUrl: 'game.component.html',
  styleUrls: ['game.component.scss']
})
export class GameComponent implements OnInit {
  @ViewChild('canvas', {static: true})
  canvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('mainCockpit', {static: true})
  mainCockpitComponent!: MainCockpitComponent;
  @ViewChild('itemCockpitContainer', {static: true})
  itemCockpitContainer!: ItemCockpitComponent;
  // TODO @ViewChild('loadingCover', {static: true})
  // TODO loadingCover?: OverlayPanel;
  editorModels: EditorModel[] = [];

  constructor(private frontendService: FrontendService,
              private router: Router,
    private gwtAngularService: GwtAngularService,
    private threeJsRendererService: ThreeJsRendererServiceImpl,
    private threeJsModelService: BabylonModelService,
    private gameMockService: GameMockService) {
  }

  ngOnInit(): void {
    this.gwtAngularService.crashListener = () => this.addEditorModel(new EditorModel("Crash Information Panel", CrashPanelComponent));

    this.threeJsRendererService.setup(this.canvas.nativeElement);

    if (environment.gwtMock) {
      this.gwtAngularService.gwtAngularFacade.inputService = this.gameMockService.inputService;
      this.gwtAngularService.gwtAngularFacade.statusProvider = this.gameMockService.statusProvider;
      this.gwtAngularService.gwtAngularFacade.editorFrontendProvider = this.gameMockService.editorFrontendProvider;
      this.gwtAngularService.gwtAngularFacade.threeJsModelPackService = this.gameMockService.mockThreeJsModelPackService;
      this.gameMockService.loadMockStaticGameConfig().then(() => {
        this.gameMockService.loadMockAssetConfig().then(() => {
          this.threeJsModelService.init(this.gameMockService.mockThreeJsModelConfigs(), this.gameMockService.mockParticleSystemConfigs(), this.gwtAngularService).then(() => {
            this.gwtAngularService.gwtAngularFacade.terrainTypeService = this.gameMockService.mockTerrainTypeService();
            this.gameMockService.mockTerrainTile(this.threeJsRendererService);
            this.mainCockpitComponent.show();
            this.threeJsRendererService.initMeshContainers(this.gameMockService.createMeshContainers());
            this.threeJsRendererService.setViewFieldCenter(5, 2);
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
              getBuilderType(): BuilderType {
                return new class implements BuilderType {
                  getParticleSystemConfigId(): number | null {
                    return 1;
                  }
                }
              }

              getWeaponType(): WeaponType {
                return new class implements WeaponType {
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

            let babylonBaseItem = this.threeJsRendererService.createSyncBaseItem(999999, baseItemType, Diplomacy.ENEMY);
            babylonBaseItem.setPosition(GwtInstance.newVertex(8, 8, 0));
            babylonBaseItem.setAngle(0);

            babylonBaseItem.updatePosition();
            babylonBaseItem.updateAngle();

              let buildingPosition: NativeVertexDto = new class implements NativeVertexDto {
                x = 16;
                y = 8;
                z = 0;
              };
              babylonBaseItem.setBuildingPosition(buildingPosition);

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

          });
        });
      });
    } else {
      this.gwtAngularService.gwtAngularFacade.threeJsRendererServiceAccess = this.threeJsRendererService;
      this.gwtAngularService.gwtAngularFacade.mainCockpit = this.mainCockpitComponent;
      this.gwtAngularService.gwtAngularFacade.itemCockpitFrontend = this.itemCockpitContainer;
      this.gwtAngularService.gwtAngularFacade.baseItemPlacerPresenter = this.threeJsRendererService.createBaseItemPlacerPresenter();
    }

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
  }

  removeEditorModel(editorModel: EditorModel) {
    this.editorModels.splice(this.editorModels.indexOf(editorModel), 1);
  }

}

