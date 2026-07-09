import { Component } from '@angular/core';
import { InventoryArtifact, InventoryArtifactEditorControllerClient, Rareness } from 'src/app/generated/razarion-share';
import { CrudContainerChild } from '../crud-container/crud-container.component';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {ImageItemComponent} from '../../common/image-item/image-item.component';
import {Select} from 'primeng/select';

@Component({
  selector: 'inventory-artifact-editor',
  imports: [
    InputNumber,
    FormsModule,
    ImageItemComponent,
    Select
  ],
  templateUrl: './inventory-artifact-editor.component.html'
})
export class InventoryArtifactEditorComponent implements CrudContainerChild<InventoryArtifact> {
  static editorControllerClient = InventoryArtifactEditorControllerClient;
  inventoryArtifact!: InventoryArtifact;
  rarenessOptions: { label: string, value: Rareness }[] = [
    {label: 'Common', value: Rareness.COMMON},
    {label: 'Un common', value: Rareness.UN_COMMON},
    {label: 'Rare', value: Rareness.RARE},
    {label: 'Epic', value: Rareness.EPIC},
    {label: 'Legendary', value: Rareness.LEGENDARY}
  ];

  init(itemType: InventoryArtifact): void {
    this.inventoryArtifact = itemType;
  }

  exportConfig(): InventoryArtifact {
    return this.inventoryArtifact;
  }

  getId(): number {
    return this.inventoryArtifact.id;
  }
}
