import {Component, Input, Type} from "@angular/core";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {EditorModel, GenericPropertyEditorModel} from "../editor-model";
import {MainCockpitComponent} from "../../game/cockpit/main/main-cockpit.component";
import {PropertyTableComponent} from "../property-table/property-table.component";
import {RenderEngineComponent} from "../render-engine/render-engine.component";

@Component({
  selector: 'editor-dialog',
  templateUrl: 'editor-dialog.component.html',
  styleUrls: ['editor-dialog.component.scss']
})
export class EditorDialogComponent {
  editors: Map<string, Type<any>> = new Map<string, Type<any>>();
  crudControllerEditors: string[] = [];
  @Input("editorModels")
  editorModels!: EditorModel[];
  @Input("mainCockpitComponent")
  mainCockpitComponent!: MainCockpitComponent;

  constructor(private gwtAngularService: GwtAngularService) {
    this.editors.set("Render Engine", RenderEngineComponent)
  }

  onShow() {
    this.crudControllerEditors = this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider().crudControllers();
  }

  openCrudControllerEditor(name: string, index: number) {
    this.mainCockpitComponent.editorDialog = false;
    this.editorModels.push(new GenericPropertyEditorModel(PropertyTableComponent, name, index));
  }

  openEditor(name: string, editorComponent: Type<any>) {
    this.mainCockpitComponent.editorDialog = false;
    this.editorModels.push(new EditorModel(name, editorComponent));
  }
}
