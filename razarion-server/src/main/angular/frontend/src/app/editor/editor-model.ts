import {Type} from "@angular/core";
import {MainCockpitComponent} from "../game/cockpit/main/main-cockpit.component";

export class EditorModel {
  constructor(public name: string, public editorComponent: Type<any>) {
  }
}

export class GenericPropertyEditorModel extends EditorModel {
  constructor(public editorComponent: Type<any>, public collectionName: string) {
    super(collectionName, editorComponent);
  }
}

export class EditorPanel {
  public editorModel!: EditorModel;
  private mainCockpitComponent!: MainCockpitComponent;

  init(editorModel: EditorModel, mainCockpitComponent: MainCockpitComponent) {
    this.editorModel = editorModel;
    this.mainCockpitComponent = mainCockpitComponent;
    this.onEditorModel();
  }

  onEditorModel(): void {

  }

  showEditorDialog() {
    this.mainCockpitComponent.editorDialog = true;
  }

}
