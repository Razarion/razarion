import {Component, effect, ElementRef, HostBinding, HostListener, NgZone, OnInit, ViewChild} from '@angular/core';
import {NgClass} from '@angular/common';
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
import {TooltipModule} from 'primeng/tooltip';
import {EditorDialogComponent} from '../editor/editor-dialog/editor-dialog.component';
import {DrawerModule} from 'primeng/drawer';
import {CockpitDisplayService} from './cockpit/cockpit-display.service';
import {UiSettingsService} from './ui-settings.service';
import {CompactLayoutService, CompactPanel} from './cockpit/compact-layout.service';
import {InventoryComponent} from './inventory/inventory.component';
import {UnlockComponent} from './unlock/unlock.component';
import {UserService} from '../auth/user.service';
import {ChatCockpitComponent} from './cockpit/chat/chat-cockpit.component';
import {InfoDialogComponent} from './info-dialog/info-dialog.component';
import {BabylonAudioService} from './renderer/babylon-audio.service';
import {ServerRestartComponent} from './server-restart/server-restart.component';
import {RadarComponent} from './cockpit/main/radar/radar.component';
import {SelectionShortcutsService} from './selection-shortcuts.service';
import {TouchSelectionModeService} from './renderer/touch-selection-mode.service';
import {RadarState} from '../gwtangular/GwtAngularFacade';


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
    ServerRestartComponent,
    RadarComponent,
    NgClass,
    TooltipModule
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

  /**
   * The corner minimap on a phone. Large enough that the view field rectangle and the coloured item
   * dots (drawn at 0.4 * zoom pixels, so ~5px at the default zoom) can be told apart, small enough
   * to leave the playfield readable next to the icon bar.
   */
  protected readonly compactRadarSize = 104;
  protected expandedRadarSize = GameComponent.calculateExpandedRadarSize();
  protected radarExpanded = false;
  protected readonly WORKING = RadarState.WORKING;

  constructor(private gwtAngularService: GwtAngularService,
              public cockpitDisplayService: CockpitDisplayService,
              public compactLayout: CompactLayoutService,
              private babylonRenderServiceAccessImpl: BabylonRenderServiceAccessImpl,
              private babylonAudioService: BabylonAudioService,
              private gameMockService: GameMockService,
              private actionService: ActionService,
              public selectionShortcuts: SelectionShortcutsService,
              public touchSelectionMode: TouchSelectionModeService,
              private userService: UserService,
              private directorService: DirectorService,
              protected uiSettingsService: UiSettingsService,
              private route: ActivatedRoute,
              private zone: NgZone) {
    this.modelDialogPresenter = new ModelDialogPresenterImpl(this.zone, cockpitDisplayService);
    // Turning the chat off while its overlay is open would leave the layout believing a panel is
    // up: the panel itself goes invisible, but the minimap stays hidden behind nothing.
    this.uiSettingsService.chatVisible$.subscribe(visible => {
      if (!visible) {
        this.compactLayout.closeIfOpen('chat');
      }
    });
    // An expanded map covers the screen, and so does an opening panel. Leaving it expanded would
    // mean closing the panel uncovers a full-screen map the player did not ask for again.
    effect(() => {
      if (this.compactLayout.openPanel()) {
        this.radarExpanded = false;
      }
    });
  }

  /**
   * The expanded map is a square, so the short side of the screen decides. The margin keeps the
   * status strip, the quest line and the icon bar clear of it; the lower bound stops a very small
   * screen from expanding to something no bigger than the corner map.
   */
  private static calculateExpandedRadarSize(): number {
    if (typeof window === 'undefined') {
      return RadarComponent.WIDTH;
    }
    const short = Math.min(window.innerWidth, window.innerHeight);
    return Math.max(180, Math.min(short - 96, 420));
  }

  @HostListener('window:resize')
  protected onWindowResize(): void {
    this.expandedRadarSize = GameComponent.calculateExpandedRadarSize();
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

  /**
   * Whether a cockpit panel is on screen right now.
   * <p>
   * Two conditions, and both have to hold in compact mode: the game says the panel has something to
   * show, and the player has opened it. On the desktop only the first applies - there every panel
   * has its own corner and none of them is in the way.
   */
  protected isPanelVisible(panel: CompactPanel): boolean {
    const available = this.isPanelAvailable(panel);
    if (!this.compactLayout.compact()) {
      return available;
    }
    return available && this.compactLayout.isOpen(panel);
  }

  /**
   * Whether the game has this panel to show at all. The chat additionally answers to the settings
   * dialog: the cockpit flag says a chat exists, the setting says the player wants to look at it.
   * Also read by the template, which must not offer an icon for a panel that cannot open.
   */
  protected isPanelAvailable(panel: CompactPanel): boolean {
    switch (panel) {
      case 'main':
        return this.cockpitDisplayService.showMainCockpit;
      case 'item':
        return this.cockpitDisplayService.showItemCockpit;
      case 'quest':
        return this.cockpitDisplayService.showQuestCockpit;
      case 'chat':
        return this.cockpitDisplayService.showChatCockpit && this.uiSettingsService.chatVisible;
    }
  }

  /**
   * The two panels a player still has the field in mind behind: the build menu, which opens itself
   * as soon as a builder is selected, and the quest they tapped open to read. Both are short, so
   * the map can move up out from behind them instead of going away - see .compact-radar-docked.
   * <p>
   * The menu and the chat are the other kind: panels you open, read and close again, tall enough
   * to reach the top of the screen, and with nothing on them a map helps with. Those still hide it.
   */
  protected isRadarDocked(): boolean {
    const panel = this.compactLayout.openPanel();
    return panel === 'item' || panel === 'quest';
  }

  /** A map behind an opaque panel is worse than no map: it would take taps meant for the panel. */
  protected isRadarHidden(): boolean {
    return this.compactLayout.openPanel() !== null && !this.isRadarDocked();
  }

  /** True while a director recording is running — drives the REC badge. */
  protected get recording(): boolean {
    return this.directorService.recording();
  }

  openInventory() {
    this.cockpitDisplayService.showInventory = true;
  }

  openUnlock() {
    this.cockpitDisplayService.showUnlock = true;
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

