import {Component, ElementRef, HostBinding, NgZone, OnInit, ViewChild} from '@angular/core';
import {environment} from 'src/environments/environment';
import {CommonModule} from "@angular/common";
import {ScreenCoverComponent} from "./screen-cover/screen-cover.component";
import {GwtAngularService} from "../gwtangular/GwtAngularService";
import {GameMockService} from "./game-mock.service";
import {BabylonRenderServiceAccessImpl} from './renderer/babylon-render-service-access-impl.service';
import {EditorModel} from '../editor/editor-model';
import {QuestCockpitComponent} from './cockpit/quest/quest-cockpit.component';
import {EditorPanelComponent} from '../editor/editor-panel/editor-panel.component';
import {MainCockpitComponent} from './cockpit/main/main-cockpit.component';
import {CrashPanelComponent} from '../editor/crash-panel/crash-panel.component';
import {ModelDialogPresenterImpl} from './model-dialog-presenter.impl';
import {ActionService} from './action.service';
import {ItemCockpitComponent} from './cockpit/item/item-cockpit.component';
import {Dialog} from 'primeng/dialog';
import {EditorDialogComponent} from '../editor/editor-dialog/editor-dialog.component';
import {DrawerModule} from 'primeng/drawer';
import {CockpitDisplayService} from './cockpit/cockpit-display.service';
import {InventoryComponent} from './inventory/inventory.component';
import {UnlockComponent} from './unlock/unlock.component';
import {AuthService} from '../auth/auth.service';


@Component({
  templateUrl: 'game.component.html',
  imports: [
    CommonModule,
    ScreenCoverComponent,
    EditorPanelComponent,
    DrawerModule,
    MainCockpitComponent,
    QuestCockpitComponent,
    Dialog,
    EditorDialogComponent,
    ItemCockpitComponent,
    InventoryComponent,
    UnlockComponent
  ],
  styleUrls: ['game.component.scss']
})
export class GameComponent implements OnInit {
  @ViewChild('loadingComponent', {static: true})
  loadingComponent!: ScreenCoverComponent;
  @ViewChild('canvas', {static: true})
  canvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('mainCockpit', {static: true})
  mainCockpitComponent!: MainCockpitComponent;
  @ViewChild('itemCockpitContainer', {static: true})
  itemCockpitContainer!: ItemCockpitComponent;
  @ViewChild('questCockpitContainer', {static: true})
  questCockpitContainer!: QuestCockpitComponent;
  @HostBinding("style.--cursor")
  cursor: string = '';

  editorModels: EditorModel[] = [];
  modelDialogPresenter: ModelDialogPresenterImpl;
  showInventory = false;
  showUnlock = false;

  constructor(private gwtAngularService: GwtAngularService,
              public cockpitDisplayService: CockpitDisplayService,
              private babylonRenderServiceAccessImpl: BabylonRenderServiceAccessImpl,
              private gameMockService: GameMockService,
              private actionService: ActionService,
              private authService: AuthService,
              private zone: NgZone) {
    this.modelDialogPresenter = new ModelDialogPresenterImpl(this.zone, gwtAngularService);
  }

  ngOnInit(): void {
    this.authService.checkToken()
      .then(() => {
        this.initAndStart();
      });
  }

  private initAndStart(): void {
    this.gwtAngularService.crashListener = () => this.addEditorModel(new EditorModel("Crash Information Panel", CrashPanelComponent));
    this.gwtAngularService.gwtAngularFacade.modelDialogPresenter = this.modelDialogPresenter;
    this.babylonRenderServiceAccessImpl.setup(this.canvas.nativeElement);

    this.gwtAngularService.gwtAngularFacade.screenCover = this.loadingComponent;
    this.gwtAngularService.gwtAngularFacade.babylonRenderServiceAccess = this.babylonRenderServiceAccessImpl;
    this.gwtAngularService.gwtAngularFacade.actionServiceListener = this.actionService;
    this.gwtAngularService.gwtAngularFacade.mainCockpit = this.mainCockpitComponent;
    this.gwtAngularService.gwtAngularFacade.itemCockpitFrontend = this.itemCockpitContainer;
    this.gwtAngularService.gwtAngularFacade.questCockpit = this.questCockpitContainer;
    this.gwtAngularService.gwtAngularFacade.baseItemPlacerPresenter = this.babylonRenderServiceAccessImpl.createBaseItemPlacerPresenter();

    if (environment.gwtMock) {
      this.gameMockService.startGame(false, this);
    } else {
      this.startGame();
    }
  }

  private startGame(): void {
    GameComponent.insertGameScript('window.RAZ_startTime = new Date().getTime();');
    // TODO GameComponent.insertMeta('gwt:property', "locale=" + this.frontendService.getLanguage());
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

  openInventory() {
    this.showInventory = true;
  }

  openUnlock() {
    this.showUnlock = true;
  }

  addEditorModel(editorModel: EditorModel) {
    this.editorModels.push(editorModel);
    this.cockpitDisplayService.showQuestCockpit = false;
  }

  removeEditorModel(editorModel: EditorModel) {
    this.editorModels.splice(this.editorModels.indexOf(editorModel), 1);
  }

}

