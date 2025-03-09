import { Component } from '@angular/core';
import { InventoryItem, InventoryItemEditorControllerClient, ItemType } from 'src/app/generated/razarion-share';
import { CrudContainerChild } from '../crud-container/crud-container.component';

@Component({
    selector: 'inventory-item-editor',
    templateUrl: './inventory-item-editor.component.html',
    standalone: false
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
