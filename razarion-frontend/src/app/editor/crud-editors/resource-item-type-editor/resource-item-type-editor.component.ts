import {Component} from '@angular/core';
import {CrudContainerChild} from "../crud-container/crud-container.component";
import {ResourceItemType, ResourceItemTypeEditorControllerClient} from "../../../generated/razarion-share";

@Component({
    selector: 'resource-item-type-editor',
    templateUrl: './resource-item-type-editor.component.html'
})
export class ResourceItemTypeEditorComponent implements CrudContainerChild<ResourceItemType> {
  static editorControllerClient = ResourceItemTypeEditorControllerClient;
  resourceItemType!: ResourceItemType;

  init(resourceItemType: ResourceItemType): void {
    this.resourceItemType = resourceItemType;
  }

  exportConfig(): ResourceItemType {
    return this.resourceItemType;
  }

  getId(): number {
    return this.resourceItemType.id;
  }
}
