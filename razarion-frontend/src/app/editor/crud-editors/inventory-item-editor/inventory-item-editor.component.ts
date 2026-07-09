import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { InventoryArtifact, InventoryArtifactCount, InventoryArtifactEditorControllerClient, InventoryItem, InventoryItemEditorControllerClient, ItemType } from 'src/app/generated/razarion-share';
import { CrudContainerChild } from '../crud-container/crud-container.component';
import {InputNumber} from 'primeng/inputnumber';
import {FormsModule} from '@angular/forms';
import {BaseItemTypeComponent} from '../../common/base-item-type/base-item-type.component';
import {ImageItemComponent} from '../../common/image-item/image-item.component';
import {InventoryArtifactComponent} from '../../common/inventory-artifact/inventory-artifact.component';
import {Button} from 'primeng/button';
import {Divider} from 'primeng/divider';

@Component({
  selector: 'inventory-item-editor',
  imports: [
    InputNumber,
    FormsModule,
    BaseItemTypeComponent,
    ImageItemComponent,
    InventoryArtifactComponent,
    Button,
    Divider
  ],
  templateUrl: './inventory-item-editor.component.html'
})
export class InventoryItemEditorComponent implements CrudContainerChild<InventoryItem> {
  static editorControllerClient = InventoryItemEditorControllerClient;
  inventoryItem!: InventoryItem;
  private crystalCostByArtifactId = new Map<number, number>();

  constructor(httpClient: HttpClient) {
    new InventoryArtifactEditorControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient))
      .readAll()
      .then(artifacts => {
        this.crystalCostByArtifactId.clear();
        artifacts.forEach((artifact: InventoryArtifact) => this.crystalCostByArtifactId.set(artifact.id, artifact.crystalCost));
      });
  }

  init(itemType: InventoryItem): void {
    this.inventoryItem = itemType;
  }

  /** Total crystals it would cost to buy all required artifacts (crystalCost * count, summed). */
  get artifactCrystalCostTotal(): number {
    return (this.inventoryItem?.inventoryArtifactCosts || []).reduce((sum, cost) => {
      if (cost.inventoryArtifactId == null) {
        return sum;
      }
      return sum + (this.crystalCostByArtifactId.get(cost.inventoryArtifactId) || 0) * cost.count;
    }, 0);
  }

  exportConfig(): InventoryItem {
    return this.inventoryItem;
  }

  getId(): number {
    return this.inventoryItem.id;
  }

  addArtifactCost(): void {
    if (!this.inventoryItem.inventoryArtifactCosts) {
      this.inventoryItem.inventoryArtifactCosts = [];
    }
    this.inventoryItem.inventoryArtifactCosts.push({inventoryArtifactId: null, count: 1} as any);
  }

  removeArtifactCost(index: number): void {
    this.inventoryItem.inventoryArtifactCosts.splice(index, 1);
  }
}
