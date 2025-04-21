import {Component, Input, Type} from "@angular/core";
import {EditorModel, EditorPanel} from "../editor-model";
import {MainCockpitComponent} from "../../game/cockpit/main/main-cockpit.component";
import {GameComponent} from "../../game/game.component";
import {ServerPanelComponent} from "../server-panel/server-panel.component";
import {BackupRestoreComponent} from "../backup-restore/backup-restore.component";
import {ImageEditorComponent} from "../image-editor/image-editor.component";
import {BabylonRenderServiceAccessImpl} from "../../game/renderer/babylon-render-service-access-impl.service";
import {ServerQuestEditorComponent} from "../server-quest-editor/server-quest-editor.component";
import {ServerBotEditorComponent} from "../server-bot-editor/server-bot-editor.component";
import {ServerStartRegionComponent} from "../server-start-region/server-start-region.component";
import {ServerResourceRegionComponent} from "../server-resource-region/server-resource-region.component";
import {EditorService} from "../editor-service";
import {LevelEditorComponent} from "../crud-editors/level-editor/level-editor.component";
import {GeneratedCrudContainerComponent} from "../crud-editors/crud-container/generated-crud-container.component";
import {BaseItemTypeEditorComponent} from "../crud-editors/base-item-type-editor/base-item-type-editor.component";
import {PlanetEditorComponent} from "../crud-editors/planet-editor/planet-editor.component";
import {BaseMgmtComponent} from "../base-mgmt/base-mgmt.component";
import {BoxItemTypeEditorComponent} from "../crud-editors/box-item-type-editor/box-item-type-editor.component";
import {InventoryItemEditorComponent} from "../crud-editors/inventory-item-editor/inventory-item-editor.component";
import {BoxRegionComponent} from "../box-region/box-region.component";
import {UserMgmtComponent} from "../user-mgmt/user-mgmt.component";
import {TerrainEditorComponent} from "../terrain-editor/terrain-editor.component";
import {ParticleSystemEditorComponent} from "../crud-editors/particle-system-editor/particle-system-editor.component";
import {
  BabylonMaterialEditorComponent
} from "../crud-editors/babylon-material-editor/babylon-material-editor.component";
import {PropertyEditorComponent} from "../property-editor/property-editor.component";
import {GltfEditorComponent} from "../crud-editors/gltf-editor/gltf-editor.component";
import {TerrainObjectEditorComponent} from "../crud-editors/terrain-object-editor/terrain-object-editor.component";
import {GroundEditorComponent} from "../crud-editors/ground-editor/ground-editor.component";
import {
  ResourceItemTypeEditorComponent
} from "../crud-editors/resource-item-type-editor/resource-item-type-editor.component";
import {Button} from 'primeng/button';
import {KeyValuePipe, NgForOf} from '@angular/common';
import {Divider} from 'primeng/divider';
import {Panel} from 'primeng/panel';
import {CockpitDisplayService} from '../../game/cockpit/cockpit-display.service';

@Component({
  selector: 'editor-dialog',
  templateUrl: 'editor-dialog.component.html',
  imports: [
    Button,
    KeyValuePipe,
    Divider,
    Panel,
    NgForOf
  ],
  styleUrls: ['editor-dialog.component.scss']
})
export class EditorDialogComponent {
  editors: Map<string, Type<EditorPanel>> = new Map<string, Type<EditorPanel>>();
  @Input("gameComponent")
  gameComponent!: GameComponent;
  @Input("mainCockpitComponent")
  mainCockpitComponent!: MainCockpitComponent;

  constructor(private renderService: BabylonRenderServiceAccessImpl,
              private cockpitDisplayService: CockpitDisplayService,
              private editorService: EditorService) {
    this.editors.set("Server Control", ServerPanelComponent)
    this.editors.set("Backup Restore", BackupRestoreComponent)
  }

  openLevelEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Level editor", LevelEditorComponent));
  }

  openPropertyEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Properties", PropertyEditorComponent));
  }

  openAdministrationEditor(name: string, editorComponent: Type<any>) {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel(name, editorComponent));
  }

  openBackend() {
    this.mainCockpitComponent.editorDialog = false;
    const url = `/backend`;
    window.open(url, "_blank");
  }

  openTerrainEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Terrain editor", TerrainEditorComponent));
  }

  openQuestEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Quest editor", ServerQuestEditorComponent));
  }

  openBotEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Bot editor", ServerBotEditorComponent));
  }

  openStartRegionEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Start region editor", ServerStartRegionComponent));
  }

  openResourceRegionEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Resource region editor", ServerResourceRegionComponent));
  }

  openBoxRegionEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Box region editor", BoxRegionComponent));
  }

  openImageEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Image editor", ImageEditorComponent));
  }

  openParticleSystemEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Particle systems editor", GeneratedCrudContainerComponent, ParticleSystemEditorComponent));
  }

  openBabylonMaterialEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Babylon material editor", GeneratedCrudContainerComponent, BabylonMaterialEditorComponent));
  }

  openGltfEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("glTF editor", GeneratedCrudContainerComponent, GltfEditorComponent));
  }

  openBabylonInspector() {
    this.mainCockpitComponent.editorDialog = false;
    this.cockpitDisplayService.showQuestCockpit = false;
    this.renderService.showInspector();
  }

  openBaseItemTypeEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Base item type editor", GeneratedCrudContainerComponent, BaseItemTypeEditorComponent));
  }

  openBoxItemTypeEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Box item type editor", GeneratedCrudContainerComponent, BoxItemTypeEditorComponent));
  }

  openResourceItemTypeEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Resource item type editor", GeneratedCrudContainerComponent, ResourceItemTypeEditorComponent));
  }

  openTerrainObjectsEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Terrain object editor", GeneratedCrudContainerComponent, TerrainObjectEditorComponent));
  }

  openGroundEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Ground editor", GeneratedCrudContainerComponent, GroundEditorComponent));
  }

  openInventoryItemEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Inventory item editor", GeneratedCrudContainerComponent, InventoryItemEditorComponent));
  }

  openPlanetEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Planet editor", GeneratedCrudContainerComponent, PlanetEditorComponent));
  }

  openBaseMgmt() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Base management", BaseMgmtComponent));
  }

  openUserMgmt() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("User management", UserMgmtComponent));
  }
}
