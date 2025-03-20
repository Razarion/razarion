import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { InventoryItemEditorControllerClient } from 'src/app/generated/razarion-share';

@Component({
    selector: 'inventory-item',
    templateUrl: './inventory-item.component.html'
})
export class InventoryItemComponent implements OnInit {
  @Input("inventoryItemId")
  inventoryItemId: number | null = null;
  @Output()
  inventoryItemIdChange = new EventEmitter<number | null>();
  private inventoryItemEditorControllerClient: InventoryItemEditorControllerClient;
  options: { label: string, inventoryItemId: number }[] = [];

  constructor(httpClient: HttpClient) {
    this.inventoryItemEditorControllerClient = new InventoryItemEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
  }

  ngOnInit(): void {
    this.inventoryItemEditorControllerClient.getObjectNameIds().then(objectNameIds => {
      this.options = [];
      objectNameIds.forEach(objectNameId => {
        this.options.push({ label: `${objectNameId.internalName} '${objectNameId.id}'`, inventoryItemId: objectNameId.id });
      });
    });
  }

  onChange() {
    this.inventoryItemIdChange.emit(this.inventoryItemId);
  }

}
