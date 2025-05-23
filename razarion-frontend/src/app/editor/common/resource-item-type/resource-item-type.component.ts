import {Component, EventEmitter, Input, Output} from '@angular/core';
import {EditorService} from "../../editor-service";
import {SelectModule} from 'primeng/select';
import {FormsModule} from '@angular/forms';
import {NgIf} from '@angular/common';

@Component({
  selector: 'resource-item-type',
  imports: [
    SelectModule,
    FormsModule,
    NgIf
  ],
  templateUrl: './resource-item-type.component.html'
})
export class ResourceItemTypeComponent {
  @Input("resourceItemTypeId")
  resourceItemTypeId: number | null = null;
  @Output()
  resourceItemTypeIdChange = new EventEmitter<number | null>();
  @Input("readOnly")
  readOnly: boolean = false;
  resourceItemTypeOptions: { name: string, id: number }[] = [];

  constructor(private editorService: EditorService) {
    editorService.readResourceItemTypeObjectNameIds().then(objectNameIds => {
      this.resourceItemTypeOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.resourceItemTypeOptions.push({name: objectNameId.internalName, id: objectNameId.id});
      });
    })
  }

  onChange() {
    this.resourceItemTypeIdChange.emit(this.resourceItemTypeId);
  }

  getCurrentName(): string {
    if (this.resourceItemTypeId) {
      return this.resourceItemTypeOptions.find(value => value.id === this.resourceItemTypeId)?.name || "";
    } else {
      return "";
    }

  }
}
