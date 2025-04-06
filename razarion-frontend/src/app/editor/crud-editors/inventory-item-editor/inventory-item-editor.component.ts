import { Component } from '@angular/core';
import { InventoryItem, InventoryItemEditorControllerClient, ItemType } from 'src/app/generated/razarion-share';
import { CrudContainerChild } from '../crud-container/crud-container.component';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {BaseItemTypeComponent} from '../../common/base-item-type/base-item-type.component';
import {ImageItemComponent} from '../../common/image-item/image-item.component';

@Component({
  selector: 'inventory-item-editor',
  imports: [
    InputNumber,
    FormsModule,
    BaseItemTypeComponent,
    ImageItemComponent
  ],
  templateUrl: './inventory-item-editor.component.html'
})
export class InventoryItemEditorComponent implements CrudContainerChild<InventoryItem> {
  static editorControllerClient = InventoryItemEditorControllerClient;
  inventoryItem!: InventoryItem;

  init(itemType: InventoryItem): void {
    this.inventoryItem = itemType;
  }

  exportConfig(): InventoryItem {
    return this.inventoryItem;
  }

  getId(): number {
    return this.inventoryItem.id;
  }
}
