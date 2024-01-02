import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { getImageUrl } from 'src/app/common';
import { InventoryControllerClient } from 'src/app/generated/razarion-share';
import { InventoryItem } from 'src/app/gwtangular/GwtAngularFacade';
import { GwtAngularService } from 'src/app/gwtangular/GwtAngularService';
import { GameComponent } from '../game.component';

@Component({
  selector: 'inventory',
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.scss']
})
export class InventoryComponent implements OnInit {
  getImageUrl = getImageUrl;
  inventoryItems: InventoryItem[] = [];
  crystals?: number;
  private inventoryControllerClient: InventoryControllerClient;

  constructor(httpClient: HttpClient,
    public gwtAngularService: GwtAngularService,
    private messageService: MessageService,
    private gameComponent: GameComponent) {
    this.inventoryControllerClient = new InventoryControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.crystals = undefined
    this.inventoryControllerClient.loadInventory().then(inventoryInfo => {
      this.crystals = inventoryInfo.crystals;
      let newInventoryItems: InventoryItem[] = [];
      inventoryInfo.inventoryItemIds.forEach(inventoryItemId => {
        let inventoryItem = this.gwtAngularService.gwtAngularFacade.inventoryTypeService.getInventoryItem(inventoryItemId);
        newInventoryItems.push(inventoryItem);
      });
      this.inventoryItems = newInventoryItems;
    }).catch((reason: any) => {
      this.messageService.add({
        severity: 'error',
        summary: `Failed to load inventory`,
        detail: reason,
        sticky: true
      });
    });
  }

  onUse(inventoryItem: InventoryItem): void {
    this.gameComponent.showInventory = false;
    this.gwtAngularService.gwtAngularFacade.inventoryUiService.useItem(inventoryItem);
  }

}
