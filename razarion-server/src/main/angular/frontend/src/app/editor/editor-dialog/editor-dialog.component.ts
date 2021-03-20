import {Component} from "@angular/core";
import {GwtAngularService} from "../../gwtangular/GwtAngularService";
import {GameComponent} from "../../game/game.component";
import {EditorModel} from "../editor-model";

@Component({
  selector: 'editor-dialog',
  templateUrl: 'editor-dialog.component.html',
  styleUrls: ['editor-dialog.component.scss']
})
export class EditorDialogComponent {
  crudControllers: string[] = [];

  constructor(private gwtAngularService: GwtAngularService, private gameComponent: GameComponent) {
  }

  onShow() {
    this.crudControllers = this.gwtAngularService.gwtAngularFacade.editorFrontendProvider.crudControllers();
  }

  openCrudControllerEditor(name: string, index: number) {
    this.gameComponent.editorDialog = false;
    this.gameComponent.insertEditorPanel(new EditorModel(name, index));
  }
}
