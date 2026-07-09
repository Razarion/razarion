import { Component, OnInit } from '@angular/core';
import { BoxItemType, BoxItemTypeEditorControllerClient, BoxItemTypePossibility } from 'src/app/generated/razarion-share';
import { CrudContainerChild } from '../crud-container/crud-container.component';
import {InputNumber} from 'primeng/inputnumber';
import {InventoryItemComponent} from '../../common/inventory-item/inventory-item.component';
import {InventoryArtifactComponent} from '../../common/inventory-artifact/inventory-artifact.component';
import {PercentInputComponent} from '../../common/percent-input/percent-input.component';
import {FormsModule} from '@angular/forms';
import {TableModule} from 'primeng/table';
import {Divider} from 'primeng/divider';
import {TerrainTypeComponent} from '../../common/terrain-type/terrain-type.component';
import {Checkbox} from 'primeng/checkbox';
import {ImageItemComponent} from '../../common/image-item/image-item.component';
import {Model3dComponent} from '../../common/model3d/model3d.component';
import {Button} from 'primeng/button';

@Component({
  selector: 'box-item-type-editor',
  imports: [
    InputNumber,
    InventoryItemComponent,
    InventoryArtifactComponent,
    PercentInputComponent,
    FormsModule,
    TableModule,
    Divider,
    TerrainTypeComponent,
    Checkbox,
    ImageItemComponent,
    Model3dComponent,
    Button
  ],
  templateUrl: './box-item-type-editor.component.html'
})
export class BoxItemTypeEditorComponent implements CrudContainerChild<BoxItemType> {
  static editorControllerClient = BoxItemTypeEditorControllerClient;
  boxItemType!: BoxItemType

  init(boxItemType: BoxItemType): void {
    this.boxItemType = boxItemType;
  }

  addPossibility(): void {
    if (!this.boxItemType.boxItemTypePossibilities) {
      this.boxItemType.boxItemTypePossibilities = [];
    }
    this.boxItemType.boxItemTypePossibilities.push({
      possibility: 0,
      inventoryItemId: null,
      inventoryArtifactId: null,
      crystals: null
    } as any as BoxItemTypePossibility);
  }

  removePossibility(index: number): void {
    this.boxItemType.boxItemTypePossibilities?.splice(index, 1);
  }

  exportConfig(): BoxItemType {
    return this.boxItemType;
  }

  getId(): number {
    return this.boxItemType.id;
  }
}
