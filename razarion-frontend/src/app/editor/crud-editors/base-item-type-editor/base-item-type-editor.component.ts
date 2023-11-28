import { Component } from '@angular/core';
import { BaseItemType, BaseItemTypeEditorControllerClient } from 'src/app/generated/razarion-share';
import { CrudContainerChild } from '../crud-container/crud-container.component';

@Component({
  selector: 'base-item-type-editor',
  templateUrl: './base-item-type-editor.component.html'
})
export class BaseItemTypeEditorComponent implements CrudContainerChild<BaseItemType> {
  static editorControllerClient = BaseItemTypeEditorControllerClient;
  baseItemType!: BaseItemType;

  init(baseItemType: BaseItemType): void {
    this.baseItemType = baseItemType;
  }

  exportConfig(): BaseItemType {
    return this.baseItemType!;
  }

  getId(): number {
    throw this.baseItemType!.id;
  }
}
