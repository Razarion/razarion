import {Component, ElementRef, HostBinding, NgZone, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {environment} from 'src/environments/environment';
import {DirectorService} from './director/director.service';

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
import {UserService} from '../auth/user.service';
import {ChatCockpitComponent} from './cockpit/chat/chat-cockpit.component';
import {InfoDialogComponent} from './info-dialog/info-dialog.component';
import {BabylonAudioService} from './renderer/babylon-audio.service';
import {ServerRestartComponent} from './server-restart/server-restart.component';


@Component({
  templateUrl: 'game.component.html',
  imports: [
    ScreenCoverComponent,
    EditorPanelComponent,
    DrawerModule,
    MainCockpitComponent,
    QuestCockpitComponent,
    Dialog,
    EditorDialogComponent,
    ItemCockpitComponent,
    InventoryComponent,
    UnlockComponent,
    ChatCockpitComponent,
    InfoDialogComponent,
    ServerRestartComponent
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
  @ViewChild('questCockpitContainer', {static: true})
  questCockpitContainer!: QuestCockpitComponent;
  @ViewChild('chatCockpitComponent', {static: true})
  chatCockpitComponent!: ChatCockpitComponent;
  @ViewChild('serverRestartComponent', {static: true})
  serverRestartComponent!: ServerRestartComponent;
  @HostBinding("style.--cursor")
  cursor: string = '';

  editorModels: EditorModel[] = [];
  modelDialogPresenter: ModelDialogPresenterImpl;
  showInventory = false;
  showUnlock = false;

  constructor(private gwtAngularService: GwtAngularService,
              public cockpitDisplayService: CockpitDisplayService,
              private babylonRenderServiceAccessImpl: BabylonRenderServiceAccessImpl,
              private babylonAudioService: BabylonAudioService,
              private gameMockService: GameMockService,
              private actionService: ActionService,
              private userService: UserService,
              private directorService: DirectorService,
              private route: ActivatedRoute,
              private zone: NgZone) {
    this.modelDialogPresenter = new ModelDialogPresenterImpl(this.zone, cockpitDisplayService);
  }

  ngOnInit(): void {
    this.userService.checkToken()
      .then(() => {
        this.initAndStart();
      });
  }

  private initAndStart(): void {
    this.gwtAngularService.crashListener = () => this.addEditorModel(new EditorModel("Crash Information Panel", CrashPanelComponent));
    this.gwtAngularService.gwtAngularFacade.modelDialogPresenter = this.modelDialogPresenter;
    this.babylonRenderServiceAccessImpl.setup(this.canvas.nativeElement);
    this.babylonAudioService.init();

    this.gwtAngularService.gwtAngularFacade.screenCover = this.loadingComponent;
    this.gwtAngularService.gwtAngularFacade.babylonRenderServiceAccess = this.babylonRenderServiceAccessImpl;
    this.gwtAngularService.gwtAngularFacade.mainCockpit = this.mainCockpitComponent;
    this.gwtAngularService.gwtAngularFacade.questCockpit = this.questCockpitContainer;
    this.gwtAngularService.gwtAngularFacade.chatCockpit = this.chatCockpitComponent;
    this.gwtAngularService.gwtAngularFacade.serverRestartPresenter = this.serverRestartComponent;
    this.gwtAngularService.gwtAngularFacade.baseItemPlacerPresenter = this.babylonRenderServiceAccessImpl.createBaseItemPlacerPresenter();
    this.actionService.setRendererService(this.babylonRenderServiceAccessImpl);

    if (environment.gwtMock) {
      this.gameMockService.startGame(true, this);
    } else {
      this.startGame();
    }

    // Director mode (/director route): film the live world for social clips.
    // The game boots exactly as normal; this only hands camera control to the
    // DirectorService and starts it polling the studio command channel.
    if (this.route.snapshot.data['director']) {
      this.directorService.activate(this.babylonRenderServiceAccessImpl);
    }
  }

  private startGame(): void {
    GameComponent.loadGameScriptUrl('/teavm-client/client-bootstrap.js');
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

  getGameComponent(): GameComponent {
    return this;
  }

  /** True while a director recording is running — drives the REC badge. */
  protected get recording(): boolean {
    return this.directorService.recording();
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
    this.cockpitDisplayService.showChatCockpit = false;
  }

  removeEditorModel(editorModel: EditorModel) {
    this.editorModels.splice(this.editorModels.indexOf(editorModel), 1);
  }

}

