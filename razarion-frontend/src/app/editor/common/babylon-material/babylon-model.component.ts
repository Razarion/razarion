import {Component, EventEmitter, Input, Output} from '@angular/core';
import {EditorService} from "../../editor-service";

@Component({
  selector: 'babylon-model',
  templateUrl: './babylon-model.component.html'
})
export class BabylonModelComponent {
  @Input("babylonModelId")
  babylonModelId: number | null = null;
  @Output()
  babylonModelIdChange = new EventEmitter<number | null>();
  @Input("readOnly")
  readOnly: boolean = false;
  babylonModelOptions: { name: string, id: number }[] = [];

  constructor(private editorService: EditorService) {
    editorService.readBabylonMaterialObjectNameIds().then(objectNameIds => {
      this.babylonModelOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.babylonModelOptions.push({name: objectNameId.internalName, id: objectNameId.id});
      });
    })
  }

  onChange() {
    this.babylonModelIdChange.emit(this.babylonModelId);
  }

  getCurrentName(): string {
    if (this.babylonModelId) {
      return this.babylonModelOptions.find(value => value.id === this.babylonModelId)?.name || "";
    } else {
      return "";
    }
  }
}
