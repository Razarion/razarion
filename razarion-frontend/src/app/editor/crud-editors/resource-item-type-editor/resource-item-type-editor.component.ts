import {Component} from '@angular/core';
import {CrudContainerChild} from "../crud-container/crud-container.component";
import {ResourceItemType, ResourceItemTypeEditorControllerClient} from "../../../generated/razarion-share";
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {Divider} from 'primeng/divider';
import {TerrainTypeComponent} from '../../common/terrain-type/terrain-type.component';
import {Checkbox} from 'primeng/checkbox';
import {ImageItemComponent} from '../../common/image-item/image-item.component';
import {Model3dComponent} from '../../common/model3d/model3d.component';

@Component({
  selector: 'resource-item-type-editor',
  imports: [
    InputNumber,
    FormsModule,
    Divider,
    TerrainTypeComponent,
    Checkbox,
    ImageItemComponent,
    Model3dComponent
  ],
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
