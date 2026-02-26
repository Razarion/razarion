import {AfterViewInit, Component, ElementRef, OnDestroy, ViewChild} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {ObjectTerrainEditorComponent} from "./object-terrain-editor.component";
import {HeightMapTerrainEditorComponent} from "./height-map-terrain-editor.component";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {GwtAngularService} from 'src/app/gwtangular/GwtAngularService';
import {RadarState} from 'src/app/gwtangular/GwtAngularFacade';
import {Tabs, TabList, Tab, TabPanels, TabPanel} from 'primeng/tabs';
import {Dialog} from 'primeng/dialog';
import {Button} from 'primeng/button';
import {Divider} from 'primeng/divider';
import {BabylonRenderServiceAccessImpl} from '../../game/renderer/babylon-render-service-access-impl.service';
import {SelectionService} from '../../game/selection.service';
import {TerrainEditorControllerClient} from '../../generated/razarion-share';
import {TypescriptGenerator} from '../../backend/typescript-generator';
import {ScrollPanelModule} from 'primeng/scrollpanel';
import {Checkbox} from 'primeng/checkbox';
import {FormsModule} from '@angular/forms';
import {EditorService} from '../editor-service';

@Component({
  selector: 'terrain-editor',
  imports: [
    ObjectTerrainEditorComponent,
    HeightMapTerrainEditorComponent,
    Tabs,
    TabList,
    Tab,
    TabPanels,
    TabPanel,
    Dialog,
    Button,
    Divider,
    ScrollPanelModule,
    Checkbox,
    FormsModule
  ],
  templateUrl: './terrain-editor.component.html'
})
export class TerrainEditorComponent extends EditorPanel implements AfterViewInit, OnDestroy {
  @ViewChild("objectEditor")
  objectTerrainEditor!: ObjectTerrainEditorComponent;
  @ViewChild("heightMapEditor")
  shapeTerrainEditor!: HeightMapTerrainEditorComponent;
  @ViewChild('miniMapCanvas', {static: true})
  miniMapCanvas!: ElementRef<HTMLCanvasElement>;
  displayMiniMap = false;
  overviewMode = false;
  private terrainEditorControllerClient: TerrainEditorControllerClient;
  activeIndex: number = 0;

  constructor(httpClient: HttpClient,
              private messageService: MessageService,
              private gwtAngularService: GwtAngularService,
              private renderService: BabylonRenderServiceAccessImpl,
              private selectionService: SelectionService,
              private editorService: EditorService) {
    super();
    this.terrainEditorControllerClient = new TerrainEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    this.stopGameUi();
  }

  ngAfterViewInit(): void {
    this.onTabViewChangeEvent(0);
  }

  ngOnDestroy(): void {
    if (this.overviewMode) {
      this.gwtAngularService.gwtAngularFacade.mainCockpit.showRadar(RadarState.NONE);
    }
    this.renderService.setOverviewMode(false);
    if (this.activeIndex === 0) {
      this.shapeTerrainEditor.deactivate();
    } else {
      this.objectTerrainEditor.deactivate();
    }
  }

  onTabViewChangeEvent(index: number) {
    if (index === 0) {
      this.shapeTerrainEditor.activate();
      this.objectTerrainEditor.deactivate();
    } else {
      this.objectTerrainEditor.activate();
      this.shapeTerrainEditor.deactivate();
    }
  }

  onShowMiniMapDialog() {
    this.editorService.readServerGameEngineConfig().then(config => {
      this.shapeTerrainEditor.generateMiniMap(this.miniMapCanvas.nativeElement, config.botConfigs);
      this.displayMiniMap = true;
    });
  }

  saveMiniMap() {
    const planetId = this.gwtAngularService.gwtAngularFacade.gameUiControl.getPlanetConfig().getId();
    let dataUrl = this.miniMapCanvas.nativeElement.toDataURL("image/png");
    this.terrainEditorControllerClient.updateMiniMapImage(planetId, dataUrl).then(data => {
      this.messageService.add({
        severity: 'success',
        summary: 'MiniMap saved',
      });
    }).catch(error => {
      console.error(error);
      this.messageService.add({
        severity: 'error',
        summary: 'MiniMap saved failed',
        detail: error.message,
        sticky: true
      });
    })
  }

  onOverviewModeChanged(): void {
    this.renderService.setOverviewMode(this.overviewMode);
    if (this.overviewMode) {
      this.gwtAngularService.gwtAngularFacade.mainCockpit.showRadar(RadarState.WORKING);
    } else {
      this.gwtAngularService.gwtAngularFacade.mainCockpit.showRadar(RadarState.NONE);
    }
  }

  private stopGameUi() {
    this.gwtAngularService.gwtAngularFacade.questCockpit.showQuestSideBar(null, false);
    this.gwtAngularService.gwtAngularFacade.inGameQuestVisualizationService.setVisible(false);
    this.renderService.disableSelectionFrame();
    this.selectionService.clearSelection();
    this.gwtAngularService.gwtAngularFacade.baseItemPlacerPresenter.deactivate();
  }
}
