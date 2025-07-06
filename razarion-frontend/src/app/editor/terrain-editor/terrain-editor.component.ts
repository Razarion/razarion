import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core';
import {EditorPanel} from "../editor-model";
import {ObjectTerrainEditorComponent} from "./object-terrain-editor.component";
import {HeightMapTerrainEditorComponent} from "./height-map-terrain-editor.component";
import {HttpClient} from "@angular/common/http";
import {MessageService} from "primeng/api";
import {GwtAngularService} from 'src/app/gwtangular/GwtAngularService';
import {TabPanel, TabView} from 'primeng/tabview';
import {Dialog} from 'primeng/dialog';
import {Button} from 'primeng/button';
import {Divider} from 'primeng/divider';
import {BabylonRenderServiceAccessImpl} from '../../game/renderer/babylon-render-service-access-impl.service';
import {TerrainEditorControllerClient} from '../../generated/razarion-share';
import {TypescriptGenerator} from '../../backend/typescript-generator';

@Component({
  selector: 'terrain-editor',
  imports: [
    ObjectTerrainEditorComponent,
    TabPanel,
    HeightMapTerrainEditorComponent,
    TabView,
    Dialog,
    Button,
    Divider
  ],
  templateUrl: './terrain-editor.component.html'
})
export class TerrainEditorComponent extends EditorPanel implements AfterViewInit {
  @ViewChild("objectEditor")
  objectTerrainEditor!: ObjectTerrainEditorComponent;
  @ViewChild("heightMapEditor")
  shapeTerrainEditor!: HeightMapTerrainEditorComponent;
  @ViewChild('miniMapCanvas', {static: true})
  miniMapCanvas!: ElementRef<HTMLCanvasElement>;
  displayMiniMap = false;
  private terrainEditorControllerClient: TerrainEditorControllerClient;

  constructor(httpClient: HttpClient,
              private messageService: MessageService,
              private gwtAngularService: GwtAngularService,
              private renderService: BabylonRenderServiceAccessImpl) {
    super();
    this.terrainEditorControllerClient = new TerrainEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
    this.stopGameUi();
  }

  ngAfterViewInit(): void {
    this.onTabViewChangeEvent(0);
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
    this.shapeTerrainEditor.generateMiniMap(this.miniMapCanvas.nativeElement);
    this.displayMiniMap = true;
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

  private stopGameUi() {
    this.gwtAngularService.gwtAngularFacade.questCockpit.showQuestSideBar(null, false);
    this.gwtAngularService.gwtAngularFacade.inGameQuestVisualizationService.setVisible(false);
    this.renderService.disableSelectionFrame();
    this.gwtAngularService.gwtAngularFacade.itemCockpitFrontend.dispose();
    this.gwtAngularService.gwtAngularFacade.baseItemPlacerPresenter.deactivate();
  }
}
