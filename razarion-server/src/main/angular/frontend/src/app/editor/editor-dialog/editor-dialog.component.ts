import {Component, Input, Type} from "@angular/core";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {EditorModel, EditorPanel, GenericPropertyEditorModel} from "../editor-model";
import {MainCockpitComponent} from "../../game/cockpit/main/main-cockpit.component";
import {PropertyTableComponent} from "../property-table/property-table.component";
import {RenderEngineComponent} from "../render-engine/render-engine.component";
import {GameComponent} from "../../game/game.component";
import {ServerPanelComponent} from "../server-panel/server-panel.component";
import {BackupRestoreComponent} from "../backup-restore/backup-restore.component";
import {TerrainEditorComponent} from "../terrain-editor/terrain-editor.component";
import {ImageEditorComponent} from "../image-editor/image-editor.component";

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

  constructor(private gwtAngularService: GwtAngularService) {
    this.editors.set("Render Engine", RenderEngineComponent)
    this.editors.set("Server Control", ServerPanelComponent)
    this.editors.set("Backup Restore", BackupRestoreComponent)
  }

  onShow() {
    this.collectionNames = this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider().collectionNames();
  }

  openConfigurationEditor(collectionName: string) {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new GenericPropertyEditorModel(PropertyTableComponent, collectionName));
  }

  openAdministrationEditor(name: string, editorComponent: Type<any>) {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel(name, editorComponent));
  }

  openTerrainEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Terrain Editor", TerrainEditorComponent));
  }

  openImageEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Image Editor", ImageEditorComponent));
  }
}
