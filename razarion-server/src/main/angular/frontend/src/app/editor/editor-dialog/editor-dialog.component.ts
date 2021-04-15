import {Component, Input} from "@angular/core";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {EditorModel} from "../editor-model";
import {MainCockpitComponent} from "../../game/cockpit/main/main-cockpit.component";

@Component({
  selector: 'editor-dialog',
  templateUrl: 'editor-dialog.component.html',
  styleUrls: ['editor-dialog.component.scss']
})
export class EditorDialogComponent {
  crudControllers: string[] = [];
  @Input("editorModels")
  editorModels!: EditorModel[];
  @Input("mainCockpitComponent")
  mainCockpitComponent!: MainCockpitComponent;

  constructor(private gwtAngularService: GwtAngularService) {
  }

  onShow() {
    this.crudControllers = this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.getGenericEditorFrontendProvider().crudControllers();
  }

  openCrudControllerEditor(name: string, index: number) {
    this.mainCockpitComponent.editorDialog = false;
    this.editorModels.push(new EditorModel(name, index));
  }
}
