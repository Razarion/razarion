import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { TypescriptGenerator } from 'src/app/backend/typescript-generator';
import { getImageUrl } from 'src/app/common';
import { InventoryControllerClient } from 'src/app/generated/razarion-share';
import { InventoryArtifact, InventoryItem } from 'src/app/gwtangular/GwtAngularFacade';
import { GwtAngularService } from 'src/app/gwtangular/GwtAngularService';
import { GameComponent } from '../game.component';
import { Button } from 'primeng/button';

type Tab = 'inventory' | 'workshop' | 'trader';

@Component({
  selector: 'inventory',
  templateUrl: './inventory.component.html',
  imports: [
    Button
  ]
})
export class InventoryComponent implements OnInit {
  getImageUrl = getImageUrl;
  activeTab: Tab = 'inventory';
  crystals?: number;
  // Owned
  inventoryItems: InventoryItem[] = [];
  ownedArtifacts: InventoryArtifact[] = [];
  ownedArtifactCount = new Map<number, number>();
  // Definitions (from the WASM static game config)
  craftableItems: InventoryItem[] = [];
  buyableItems: InventoryItem[] = [];
  buyableArtifacts: InventoryArtifact[] = [];
  private inventoryControllerClient: InventoryControllerClient;

  constructor(httpClient: HttpClient,
    public gwtAngularService: GwtAngularService,
    private messageService: MessageService,
    private gameComponent: GameComponent) {
    this.inventoryControllerClient = new InventoryControllerClient(TypescriptGenerator.generateHttpClientAdapter(httpClient));
  }

  ngOnInit(): void {
    this.loadDefinitions();
    this.reload();
  }

  private loadDefinitions(): void {
    const inventoryTypeService = this.gwtAngularService.gwtAngularFacade.inventoryTypeService;
    const items = inventoryTypeService.getInventoryItems() || [];
    this.craftableItems = items.filter(item => (item.getInventoryArtifactCosts() || []).length > 0);
    this.buyableItems = items.filter(item => item.getCrystalCost() != null);
    this.buyableArtifacts = (inventoryTypeService.getInventoryArtifacts() || []).filter(artifact => artifact.getCrystalCost() != null);
  }

  reload(): void {
    this.crystals = undefined;
    this.inventoryControllerClient.loadInventory().then(inventoryInfo => {
      const inventoryTypeService = this.gwtAngularService.gwtAngularFacade.inventoryTypeService;
      this.crystals = inventoryInfo.crystals;
      // Resolve owned ids against the client's static-config registry. An unknown id (e.g. an
      // item/artifact deleted after it was granted, or a stale registry before warm-restart)
      // must not fail the whole load, so resolve defensively and skip what can't be found.
      this.inventoryItems = (inventoryInfo.inventoryItemIds || [])
        .map(id => this.resolve(id, i => inventoryTypeService.getInventoryItem(i), 'InventoryItem'))
        .filter((item): item is InventoryItem => item != null);
      this.ownedArtifacts = [];
      this.ownedArtifactCount = new Map<number, number>();
      (inventoryInfo.inventoryArtifactIds || []).forEach(id => {
        const artifact = this.resolve(id, i => inventoryTypeService.getInventoryArtifact(i), 'InventoryArtifact');
        if (artifact != null) {
          this.ownedArtifacts.push(artifact);
          this.ownedArtifactCount.set(id, (this.ownedArtifactCount.get(id) || 0) + 1);
        }
      });
    }).catch((reason: any) => this.showError('Failed to load inventory', reason));
  }

  private resolve<T>(id: number, lookup: (id: number) => T, kind: string): T | null {
    try {
      return lookup(id);
    } catch (e) {
      console.warn(`Unknown ${kind} id ${id} not in static-config registry (skipped)`, e);
      return null;
    }
  }

  artifactOfCost(inventoryArtifactId: number | null): InventoryArtifact | null {
    if (inventoryArtifactId == null) {
      return null;
    }
    const inventoryTypeService = this.gwtAngularService.gwtAngularFacade.inventoryTypeService;
    return this.resolve(inventoryArtifactId, id => inventoryTypeService.getInventoryArtifact(id), 'InventoryArtifact');
  }

  ownedCountOf(inventoryArtifactId: number | null): number {
    if (inventoryArtifactId == null) {
      return 0;
    }
    return this.ownedArtifactCount.get(inventoryArtifactId) || 0;
  }

  canAssemble(item: InventoryItem): boolean {
    return (item.getInventoryArtifactCosts() || []).every(cost => this.ownedCountOf(cost.getInventoryArtifactId()) >= cost.getCount());
  }

  /**
   * Image shown for an owned inventory item. When the item has no own image, fall back to the
   * thumbnail of the base item type it grants (the count is rendered as a badge above the image).
   */
  inventoryItemImageUrl(inventoryItem: InventoryItem): string {
    const imageId = inventoryItem.getImageId();
    if (imageId != null) {
      return getImageUrl(imageId);
    }
    return getImageUrl(this.baseItemTypeThumbnail(inventoryItem));
  }

  /** Count badge shown on the fallback thumbnail; null when the item has its own image or grants no base item. */
  inventoryItemBaseItemTypeCount(inventoryItem: InventoryItem): number | null {
    if (inventoryItem.getImageId() != null || inventoryItem.getBaseItemTypeId() == null) {
      return null;
    }
    const count = inventoryItem.getBaseItemTypeCount();
    return count > 0 ? count : null;
  }

  private baseItemTypeThumbnail(inventoryItem: InventoryItem): number | null {
    const baseItemTypeId = inventoryItem.getBaseItemTypeId();
    if (baseItemTypeId == null) {
      return null;
    }
    const itemTypeService = this.gwtAngularService.gwtAngularFacade.itemTypeService;
    const baseItemType = this.resolve(baseItemTypeId, id => itemTypeService.getBaseItemTypeAngular(id), 'BaseItemType');
    return baseItemType?.getThumbnail() ?? null;
  }

  onUse(inventoryItem: InventoryItem): void {
    this.gameComponent.showInventory = false;
    this.gwtAngularService.gwtAngularFacade.inventoryUiService.useItemById(inventoryItem.getId());
  }

  onAssemble(inventoryItem: InventoryItem): void {
    this.inventoryControllerClient.assembleInventoryItem(inventoryItem.getId()).then(assembled => {
      if (assembled) {
        this.showInfo('Assembled');
      } else {
        this.showWarn('Not enough artifacts to assemble this item');
      }
      this.reload();
    }).catch((reason: any) => this.showError('Assemble failed', reason));
  }

  onBuyItem(inventoryItem: InventoryItem): void {
    this.inventoryControllerClient.buyInventoryItem(inventoryItem.getId()).then(bought => {
      if (bought) {
        this.showInfo('Bought');
      } else {
        this.showWarn('Not enough crystals');
      }
      this.reload();
    }).catch((reason: any) => this.showError('Buy failed', reason));
  }

  onBuyArtifact(inventoryArtifact: InventoryArtifact): void {
    this.inventoryControllerClient.buyInventoryArtifact(inventoryArtifact.getId()).then(bought => {
      if (bought) {
        this.showInfo('Bought');
      } else {
        this.showWarn('Not enough crystals');
      }
      this.reload();
    }).catch((reason: any) => this.showError('Buy failed', reason));
  }

  private showError(summary: string, reason: any): void {
    this.messageService.add({ severity: 'error', summary, detail: reason, sticky: true });
  }

  private showWarn(summary: string): void {
    this.messageService.add({ severity: 'warn', summary });
  }

  private showInfo(summary: string): void {
    this.messageService.add({ severity: 'success', summary });
  }
}
