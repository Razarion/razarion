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
        this.threeJsModelService.init(this.gameMockService.mockThreeJsModelConfigs(), this.gwtAngularService).then(() => {
          this.gwtAngularService.gwtAngularFacade.terrainTypeService = this.gameMockService.mockTerrainTypeService();
          this.gameMockService.mockTerrainTile(this.threeJsRendererService);
          this.mainCockpitComponent.show();
          this.threeJsRendererService.setViewFieldCenter(200, 160);
        });
      });
    } else {
      this.gwtAngularService.gwtAngularFacade.threeJsRendererServiceAccess = this.threeJsRendererService;
      this.gwtAngularService.gwtAngularFacade.mainCockpit = this.mainCockpitComponent;
      this.gwtAngularService.gwtAngularFacade.itemCockpitFrontend = this.itemCockpitContainer;
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

