import { Component, Input, Type } from "@angular/core";
import { GwtAngularService } from "../../gwtangular/GwtAngularService";
import { EditorModel, EditorPanel, GenericPropertyEditorModel } from "../editor-model";
import { MainCockpitComponent } from "../../game/cockpit/main/main-cockpit.component";
import { RenderEngineComponent } from "../render-engine/render-engine.component";
import { GameComponent } from "../../game/game.component";
import { ServerPanelComponent } from "../server-panel/server-panel.component";
import { BackupRestoreComponent } from "../backup-restore/backup-restore.component";
import { ImageEditorComponent } from "../image-editor/image-editor.component";
import { CollectionSelectorComponent } from "../property-table/collection-selector.component";
import { BabylonRenderServiceAccessImpl } from "../../game/renderer/babylon-render-service-access-impl.service";
import { ServerQuestEditorComponent } from "../server-quest-editor/server-quest-editor.component";
import { ServerBotEditorComponent } from "../server-bot-editor/server-bot-editor.component";
import { ServerStartRegionComponent } from "../server-start-region/server-start-region.component";
import { ServerResourceRegionComponent } from "../server-resource-region/server-resource-region.component";
import { ManuallyCrudContainerComponent } from "../crud-editors/crud-container/manually-crud-container.component";
import { SlopeEditorComponent } from "../crud-editors/slope-editor/slope-editor.component";
import { DrivewayEditorComponent } from "../crud-editors/driveway-editor/driveway-editor.component";
import { TerrainEditor2dComponent } from "../../terrain-editor2d/terrain-editor2d.component";
import { EditorService } from "../editor-service";
import { ObjectTerrainEditorComponent } from "../terrain-editor/object-terrain-editor.component";
import { LevelEditorComponent } from "../crud-editors/level-editor/level-editor.component";
import { GeneratedCrudContainerComponent } from "../crud-editors/crud-container/generated-crud-container.component";
import { BaseItemTypeEditorComponent } from "../crud-editors/base-item-type-editor/base-item-type-editor.component";
import { PlanetEditorComponent } from "../crud-editors/planet-editor/planet-editor.component";
import { BaseMgmtComponent } from "../base-mgmt/base-mgmt.component";
import { BoxItemTypeEditorComponent } from "../crud-editors/box-item-type-editor/box-item-type-editor.component";
import { InventoryItemEditorComponent } from "../crud-editors/inventory-item-editor/inventory-item-editor.component";
import { BoxRegionComponent } from "../box-region/box-region.component";

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

  constructor(private gwtAngularService: GwtAngularService,
    private renderService: BabylonRenderServiceAccessImpl,
    private editorService: EditorService) {
    this.editors.set("Babylon.js", RenderEngineComponent)
    this.editors.set("Server Control", ServerPanelComponent)
    this.editors.set("Backup Restore", BackupRestoreComponent)
  }

  onShow() {
    this.collectionNames = this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider().collectionNames();
  }

  openLevelEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Level editor", LevelEditorComponent));
  }

  openConfigurationEditor(collectionName: string) {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new GenericPropertyEditorModel(CollectionSelectorComponent, collectionName));
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
    this.gameComponent.addEditorModel(new EditorModel("Object terrain editor", ObjectTerrainEditorComponent));
  }

  openTerrainEditor2d() {
    this.mainCockpitComponent.editorDialog = false;
    const url = `/terrain-editor/?${TerrainEditor2dComponent.PLANET_ID_PARAM}=${this.editorService.getPlanetId()}`;
    window.open(url, "_blank", "resizable=yes");
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

  openSlopeEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Slope editor", ManuallyCrudContainerComponent, SlopeEditorComponent));
  }

  openDrivewayEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Driveway editor", ManuallyCrudContainerComponent, DrivewayEditorComponent));
  }

  openImageEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Image editor", ImageEditorComponent));
  }

  openBabylonInspector() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.questCockpitContainer.showCockpit = false;
    this.renderService.showInspector();
  }

  openBaseItemTypeEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Base item type ditor", GeneratedCrudContainerComponent, BaseItemTypeEditorComponent));
  }

  openBoxItemTypeEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Box item type ditor", GeneratedCrudContainerComponent, BoxItemTypeEditorComponent));
  }

  openInventoryItemEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Inventory item ditor", GeneratedCrudContainerComponent, InventoryItemEditorComponent));
  }
  
  openPlanetEditor() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Planet ditor", GeneratedCrudContainerComponent, PlanetEditorComponent));
  }

  openBaseMgmt() {
    this.mainCockpitComponent.editorDialog = false;
    this.gameComponent.addEditorModel(new EditorModel("Base management", BaseMgmtComponent));
  }
}
