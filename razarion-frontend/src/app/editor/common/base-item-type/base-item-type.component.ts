import {Component, EventEmitter, Input, Output} from '@angular/core';
import {EditorService} from "../../editor-service";

@Component({
    selector: 'base-item-type',
    templateUrl: './base-item-type.component.html'
})
export class BaseItemTypeComponent {
  @Input("baseItemTypeId")
  baseItemTypeId: number | null = null;
  @Output()
  baseItemTypeIdChange = new EventEmitter<number | null>();
  @Output()
  onChangeEmitter = new EventEmitter<void>();
  @Input("readOnly")
  readOnly: boolean = false;
  baseItemTypeOptions: { name: string, id: number }[] = [];

  constructor(editorService: EditorService) {
    editorService.readBaseItemTypeObjectNameIds().then(objectNameIds => {
      this.baseItemTypeOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.baseItemTypeOptions.push({name: objectNameId.internalName, id: objectNameId.id});
      });
    })
  }

  onChange() {
    this.baseItemTypeIdChange.emit(this.baseItemTypeId);
    this.onChangeEmitter.emit()
  }

  getCurrentName(): string {
    if (this.baseItemTypeId) {
      return this.baseItemTypeOptions.find(value => value.id === this.baseItemTypeId)?.name || "";
    } else {
      return "";
    }

  }
}
