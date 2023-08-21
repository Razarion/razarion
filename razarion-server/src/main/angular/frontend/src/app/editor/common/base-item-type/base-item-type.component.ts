import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {EditorService} from "../../editor-service";

@Component({
  selector: 'base-item-type',
  templateUrl: './base-item-type.component.html',
  styleUrls: ['./base-item-type.component.scss']
})
export class BaseItemTypeComponent implements OnInit {
  @Input("baseItemTypeId")
  baseItemTypeId?: string;
  @Output()
  baseItemTypeIdChange = new EventEmitter<string>();
  @Output()
  onChangeEmitter = new EventEmitter<void>();
  baseItemTypeOptions: { name: string, id: string }[] = [];

  constructor(private editorService: EditorService) {
    editorService.readBaseItemTypeObjectNameIds().then(objectNameIds => {
      this.baseItemTypeOptions = [];
      objectNameIds.forEach(objectNameId => {
        this.baseItemTypeOptions.push({name: objectNameId.internalName, id: objectNameId.id.toString()});
      });
    })
  }

  ngOnInit(): void {
  }

  onChange() {
    this.baseItemTypeIdChange.emit(this.baseItemTypeId);
    this.onChangeEmitter.emit()
  }
}
