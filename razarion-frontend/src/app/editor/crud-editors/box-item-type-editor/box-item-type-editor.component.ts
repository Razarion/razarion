import { Component, OnInit } from '@angular/core';
import { BoxItemType, BoxItemTypeEditorControllerClient } from 'src/app/generated/razarion-share';
import { CrudContainerChild } from '../crud-container/crud-container.component';

@Component({
    selector: 'box-item-type-editor',
    templateUrl: './box-item-type-editor.component.html'
})
export class BoxItemTypeEditorComponent implements CrudContainerChild<BoxItemType> {
  static editorControllerClient = BoxItemTypeEditorControllerClient;
  boxItemType!: BoxItemType

  init(boxItemType: BoxItemType): void {
    this.boxItemType = boxItemType;
  }

  exportConfig(): BoxItemType {
    return this.boxItemType;
  }

  getId(): number {
    return this.boxItemType.id;
  }
}
