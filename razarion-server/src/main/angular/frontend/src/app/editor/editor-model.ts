import {Type} from "@angular/core";

export class EditorModel {
  constructor(public name: string, public editorComponent: Type<any>) {
  }
}

export class GenericPropertyEditorModel extends EditorModel {
  constructor(public editorComponent: Type<any>, public crudControllerName: string, public crudControllerIndex: number) {
    super(crudControllerName, editorComponent);
  }
}

export class EditorPanel {
  public editorModel!: EditorModel;

  setEditorModel(editorModel: EditorModel) {
    this.editorModel = editorModel;
    this.onEditorModel();
  }

  onEditorModel(): void {

  }

}
