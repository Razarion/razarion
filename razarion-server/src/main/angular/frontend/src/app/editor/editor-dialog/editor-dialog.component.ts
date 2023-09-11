import {Component, Input, Type} from "@angular/core";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {EditorModel, EditorPanel, GenericPropertyEditorModel} from "../editor-model";
import {MainCockpitComponent} from "../../game/cockpit/main/main-cockpit.component";
import {RenderEngineComponent} from "../render-engine/render-engine.component";
import {GameComponent} from "../../game/game.component";
import {ServerPanelComponent} from "../server-panel/server-panel.component";
import {BackupRestoreComponent} from "../backup-restore/backup-restore.component";
import {TerrainEditorComponent} from "../terrain-editor/terrain-editor.component";
import {ImageEditorComponent} from "../image-editor/image-editor.component";
import {CollectionSelectorComponent} from "../property-table/collection-selector.component";
import {BabylonRenderServiceAccessImpl} from "../../game/renderer/babylon-render-service-access-impl.service";
import {ServerQuestEditorComponent} from "../server-quest-editor/server-quest-editor.component";
import {ServerBotEditorComponent} from "../server-bot-editor/server-bot-editor.component";
import {ServerStartRegionComponent} from "../server-start-region/server-start-region.component";
import {ServerResourceRegionComponent} from "../server-resource-region/server-resource-region.component";
import {CrudContainerComponent} from "../crud-editors/crud-container/crud-container.component";
import {SlopeEditorComponent} from "../crud-editors/slope-editor/slope-editor.component";

@Component({
  selector: 'editor-dialog',
  templateUrl: 'editor-dialog.component.html',
  styleUrls: ['editor-dialog.component.scss']
})
export class EditorDialogComponent {
  editors: Map<string, Type<EditorPanel>> = new Map<string, Type<EditorPanel>>();
  collectionNames: string[] = [];
  @Input("gameComponent")
  gameComponent!: GameComponent;
  @Input("mainCockpitComponent")
  mainCockpitComponent!: MainCockpitComponent;

  constructor(private gwtAngularService: GwtAngularService, private renderService: BabylonRenderServiceAccessImpl) {
    this.editors.set("Babylon.js", RenderEngineComponent)
    this.editors.set("Server Control", ServerPanelComponent)
    this.editors.set("Backup Restore", BackupRestoreComponent)
  }

  onShow() {
    this.collectionNames = this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider().collectionNames();
  }

  openConfigurationEditor(collectionName: string) {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new GenericPropertyEditorModel(CollectionSelectorComponent, collectionName));
  }

  openAdministrationEditor(name: string, editorComponent: Type<any>) {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel(name, editorComponent));
  }

  openTerrainEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Terrain Editor", TerrainEditorComponent));
  }

  openQuestEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Quest Editor", ServerQuestEditorComponent));
  }

  openBotEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Bot Editor", ServerBotEditorComponent));
  }

  openStartRegionEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Start Region Editor", ServerStartRegionComponent));
  }

  openResourceRegionEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Resource Region Editor", ServerResourceRegionComponent));
  }

  openSlopeEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Slope Editor", CrudContainerComponent, SlopeEditorComponent));
  }

  openImageEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Image Editor", ImageEditorComponent));
  }

  openBabylonInspector() {
    this.mainCockpitComponent.editorDialog = false;
    this.renderService.showInspector();
  }
}
