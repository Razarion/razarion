import { Component } from '@angular/core';
import { BaseItemType, BaseItemTypeEditorControllerClient } from 'src/app/generated/razarion-share';
import { HttpClient } from '@angular/common/http';
import { CrudContainerChild } from '../crud-container/crud-container.component';

@Component({
  selector: 'base-item-type-editor',
  templateUrl: './base-item-type-editor.component.html'
})
export class BaseItemTypeEditorComponent implements CrudContainerChild<BaseItemType> {
  static editorControllerClient = BaseItemTypeEditorControllerClient;
  config?: BaseItemType;

  init(config: BaseItemType): void {
    this.config = config;
  }

  exportConfig(): BaseItemType {
    return this.config!;
  }

  getId(): number {
    throw this.config!.id;
  }
}
