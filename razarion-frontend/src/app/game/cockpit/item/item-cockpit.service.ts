import { Injectable } from '@angular/core';
import {
  BaseItemType,
  BaseItemUiService,
  Character,
  Diplomacy,
  GameUiControl,
  ItemCockpitBridge,
  ItemTypeService,
  PlayerBaseDto
} from '../../../gwtangular/GwtAngularFacade';
import { GwtAngularService } from '../../../gwtangular/GwtAngularService';
import { SelectionService } from '../../selection.service';
import { CockpitDisplayService } from '../cockpit-display.service';

// --- View-Model Interfaces ---

export interface OwnItemCockpitModel {
  imageUrl: string;
  itemTypeName: string;
  itemTypeDescr: string;
  buildupItems: BuildupItemModel[] | null;
  containerCount: number | null;
  containerId: number | null;
  canSell: boolean;
}

export interface BuildupItemModel {
  imageUrl: string;
  itemTypeId: number;
  itemTypeName: string;
  price: number;
  itemCount: number;
  itemLimit: number;
  enabled: boolean;
  buildLimitReached: boolean;
  buildHouseSpaceReached: boolean;
  buildNoMoney: boolean;
}

export interface OwnMultipleModel {
  ownItemCockpit: OwnItemCockpitModel;
  count: number;
  baseItemTypeId: number;
}

export interface OtherItemCockpitModel {
  id: number;
  imageUrl: string;
  itemTypeName: string;
  itemTypeDescr: string;
  baseId?: number;
  baseName: string;
  friend: boolean;
  bot: boolean;
  resource: boolean;
  box: boolean;
}

function getImageServiceUrl(thumbnailId: number | null): string {
  return thumbnailId != null ? `/rest/image/${thumbnailId}` : '';
}

@Injectable({ providedIn: 'root' })
export class ItemCockpitService {
  ownItemCockpit: OwnItemCockpitModel | null = null;
  ownMultipleItems: OwnMultipleModel[] | null = null;
  otherItemCockpit: OtherItemCockpitModel | null = null;
  count: number = 0;
  private initialized = false;

  constructor(
    private selectionService: SelectionService,
    private gwtAngularService: GwtAngularService,
    private cockpitDisplayService: CockpitDisplayService
  ) {
    selectionService.addSelectionListener(() => this.onSelectionChanged());
  }

  private get itemTypeService(): ItemTypeService {
    return this.gwtAngularService.gwtAngularFacade.itemTypeService;
  }

  private get baseItemUiService(): BaseItemUiService {
    return this.gwtAngularService.gwtAngularFacade.baseItemUiService;
  }

  private get gameUiControl(): GameUiControl {
    return this.gwtAngularService.gwtAngularFacade.gameUiControl;
  }

  private get itemCockpitBridge(): ItemCockpitBridge {
    return this.gwtAngularService.gwtAngularFacade.itemCockpitBridge;
  }

  init(): void {
    this.initialized = true;
    this.itemCockpitBridge.setCockpitStateCallback(() => this.onStateChanged());
  }

  private onSelectionChanged(): void {
    // Lazy init: bridge becomes available after WASM loads
    if (!this.gwtAngularService.gwtAngularFacade.itemCockpitBridge) return;
    if (!this.initialized) {
      this.init();
    }
    // Unwatch container from previous selection
    this.itemCockpitBridge.unwatchContainerCount();

    const selectedItems = this.selectionService.getSelectedOwnItems();
    if (selectedItems.length > 0) {
      // Own selection
      const groupedByType = new Map<number, typeof selectedItems>();
      for (const item of selectedItems) {
        const typeId = item.getBaseItemType().getId();
        let group = groupedByType.get(typeId);
        if (!group) {
          group = [];
          groupedByType.set(typeId, group);
        }
        group.push(item);
      }

      if (groupedByType.size === 1) {
        // Single type selected
        const [typeId, items] = groupedByType.entries().next().value!;
        const baseItemType = this.itemTypeService.getBaseItemTypeAngular(typeId);
        this.ownItemCockpit = this.createOwnItemCockpit(baseItemType, items);
        this.ownMultipleItems = null;
        this.otherItemCockpit = null;
        this.count = items.length;
      } else {
        // Multiple types selected
        this.ownItemCockpit = null;
        this.ownMultipleItems = this.createOwnMultipleInfo(groupedByType);
        this.otherItemCockpit = null;
        this.count = selectedItems.length;
      }
      this.cockpitDisplayService.showItemCockpit = true;
    } else if (this.selectionService.getSelectedOtherId() != null) {
      // Other selection
      this.ownItemCockpit = null;
      this.ownMultipleItems = null;
      this.otherItemCockpit = this.createOtherItemCockpit();
      this.count = 1;
      this.cockpitDisplayService.showItemCockpit = true;
    } else {
      // No selection
      this.ownItemCockpit = null;
      this.ownMultipleItems = null;
      this.otherItemCockpit = null;
      this.count = 0;
      this.cockpitDisplayService.showItemCockpit = false;
    }
  }

  private createOwnItemCockpit(baseItemType: BaseItemType, items: { getId(): number }[]): OwnItemCockpitModel {
    const canSell = !this.gameUiControl.isSellSuppressed();

    const model: OwnItemCockpitModel = {
      imageUrl: getImageServiceUrl(baseItemType.getThumbnail()),
      itemTypeName: baseItemType.getName(),
      itemTypeDescr: baseItemType.getDescription(),
      buildupItems: this.createBuildupItems(baseItemType),
      containerCount: null,
      containerId: null,
      canSell
    };

    // Container info
    if (baseItemType.getItemContainerType() != null && items.length === 1) {
      const containerId = items[0].getId();
      model.containerId = containerId;
      model.containerCount = 0; // Initial; will be updated by watch
      this.itemCockpitBridge.watchContainerCount(containerId, (count: number) => {
        if (this.ownItemCockpit && this.ownItemCockpit.containerId === containerId) {
          this.ownItemCockpit.containerCount = count;
        }
      });
    }

    return model;
  }

  private createBuildupItems(baseItemType: BaseItemType): BuildupItemModel[] | null {
    let ableToBuildIds: number[] | null = null;

    if (baseItemType.getBuilderType() != null) {
      ableToBuildIds = baseItemType.getBuilderType()!.getAbleToBuildIds();
    } else if (baseItemType.getFactoryType() != null) {
      ableToBuildIds = baseItemType.getFactoryType()!.getAbleToBuildIds();
    }

    if (!ableToBuildIds || ableToBuildIds.length === 0) {
      return null;
    }

    const planetConfig = this.gameUiControl.getPlanetConfig();
    const models: BuildupItemModel[] = [];

    for (const itemTypeId of ableToBuildIds) {
      if (planetConfig.imitation4ItemType(itemTypeId) <= 0) {
        continue;
      }

      const toBuild = this.itemTypeService.getBaseItemTypeAngular(itemTypeId);
      const model = this.createBuildupItemModel(toBuild);
      models.push(model);
    }

    return models.length > 0 ? models : null;
  }

  private createBuildupItemModel(toBuild: BaseItemType): BuildupItemModel {
    const itemTypeId = toBuild.getId();
    const itemCount = this.baseItemUiService.getMyItemCount(itemTypeId);
    const itemLimit = this.gameUiControl.getMyLimitation4ItemType(itemTypeId);
    const price = toBuild.getPrice();
    const resources = this.baseItemUiService.getResources();
    const usedHouseSpace = this.baseItemUiService.getUsedHouseSpace();
    const houseSpace = this.baseItemUiService.getHouseSpace();
    const planetHouseSpace = this.gameUiControl.getPlanetConfig().getHouseSpace();
    const totalHouseSpace = houseSpace + planetHouseSpace;

    let enabled = true;
    let buildLimitReached = false;
    let buildHouseSpaceReached = false;
    let buildNoMoney = false;

    if (itemCount + 1 > itemLimit) {
      buildLimitReached = true;
      enabled = false;
    } else if (usedHouseSpace + toBuild.getConsumingHouseSpace() > totalHouseSpace) {
      buildHouseSpaceReached = true;
      enabled = false;
    } else if (price > resources) {
      buildNoMoney = true;
      enabled = false;
    }

    return {
      imageUrl: getImageServiceUrl(toBuild.getThumbnail()),
      itemTypeId,
      itemTypeName: toBuild.getName(),
      price,
      itemCount,
      itemLimit,
      enabled,
      buildLimitReached,
      buildHouseSpaceReached,
      buildNoMoney
    };
  }

  private createOwnMultipleInfo(groupedByType: Map<number, { getId(): number, getBaseItemType(): BaseItemType }[]>): OwnMultipleModel[] {
    const models: OwnMultipleModel[] = [];
    for (const [typeId, items] of groupedByType) {
      const baseItemType = this.itemTypeService.getBaseItemTypeAngular(typeId);
      const canSell = !this.gameUiControl.isSellSuppressed();
      models.push({
        ownItemCockpit: {
          imageUrl: getImageServiceUrl(baseItemType.getThumbnail()),
          itemTypeName: baseItemType.getName(),
          itemTypeDescr: baseItemType.getDescription(),
          buildupItems: null,
          containerCount: null,
          containerId: null,
          canSell
        },
        count: items.length,
        baseItemTypeId: typeId
      });
    }
    return models;
  }

  private createOtherItemCockpit(): OtherItemCockpitModel {
    const id = this.selectionService.getSelectedOtherId()!;
    const diplomacy = this.selectionService.getSelectedOtherDiplomacy()!;
    const itemTypeId = this.selectionService.getSelectedOtherItemTypeId();
    const baseId = this.selectionService.getSelectedOtherBaseId();

    let imageUrl = '';
    let itemTypeName = '';
    let itemTypeDescr = '';
    let baseName = '';
    let friend = false;
    let bot = false;
    let resource = false;
    let box = false;

    if (itemTypeId != null) {
      if (diplomacy === Diplomacy.RESOURCE) {
        const resourceType = this.itemTypeService.getResourceItemTypeAngular(itemTypeId);
        imageUrl = getImageServiceUrl(resourceType.getThumbnail());
        itemTypeName = resourceType.getName();
        itemTypeDescr = resourceType.getDescription();
        resource = true;
      } else if (diplomacy === Diplomacy.BOX) {
        // Box items don't have a specific TS type service method yet
        imageUrl = '';
        itemTypeName = 'Box';
        itemTypeDescr = '';
        box = true;
      } else {
        // Base item (FRIEND, ENEMY)
        const baseItemType = this.itemTypeService.getBaseItemTypeAngular(itemTypeId);
        imageUrl = getImageServiceUrl(baseItemType.getThumbnail());
        itemTypeName = baseItemType.getName();
        itemTypeDescr = baseItemType.getDescription();

        if (baseId != null) {
          const bases = this.baseItemUiService.getBases();
          const playerBase = bases.find((b: PlayerBaseDto) => b.getBaseId() === baseId);
          if (playerBase) {
            const character = playerBase.getCharacter();
            if (character === Character.HUMAN) {
              const name = playerBase.getName();
              if (!name || name.trim() === '') {
                baseName = 'Unnamed user';
              } else {
                baseName = name;
              }
              friend = diplomacy === Diplomacy.FRIEND;
            } else if (character === Character.BOT) {
              baseName = playerBase.getName();
              bot = true;
            } else if (character === Character.BOT_NCP) {
              baseName = playerBase.getName();
              friend = true;
              bot = true;
            }
          }
        }
      }
    }

    return {
      id,
      imageUrl,
      itemTypeName,
      itemTypeDescr,
      baseId: baseId ?? undefined,
      baseName,
      friend,
      bot,
      resource,
      box
    };
  }

  /** Called from Java when item count, house space, or resources change */
  private onStateChanged(): void {
    if (this.ownItemCockpit?.buildupItems) {
      this.refreshBuildupState(this.ownItemCockpit.buildupItems);
    }
    if (this.ownMultipleItems) {
      for (const multi of this.ownMultipleItems) {
        if (multi.ownItemCockpit.buildupItems) {
          this.refreshBuildupState(multi.ownItemCockpit.buildupItems);
        }
      }
    }
  }

  private refreshBuildupState(buildupItems: BuildupItemModel[]): void {
    const resources = this.baseItemUiService.getResources();
    const usedHouseSpace = this.baseItemUiService.getUsedHouseSpace();
    const houseSpace = this.baseItemUiService.getHouseSpace();
    const planetHouseSpace = this.gameUiControl.getPlanetConfig().getHouseSpace();
    const totalHouseSpace = houseSpace + planetHouseSpace;

    for (const item of buildupItems) {
      const toBuild = this.itemTypeService.getBaseItemTypeAngular(item.itemTypeId);
      item.itemCount = this.baseItemUiService.getMyItemCount(item.itemTypeId);
      item.itemLimit = this.gameUiControl.getMyLimitation4ItemType(item.itemTypeId);

      item.buildLimitReached = false;
      item.buildHouseSpaceReached = false;
      item.buildNoMoney = false;
      item.enabled = true;

      if (item.itemCount + 1 > item.itemLimit) {
        item.buildLimitReached = true;
        item.enabled = false;
      } else if (usedHouseSpace + toBuild.getConsumingHouseSpace() > totalHouseSpace) {
        item.buildHouseSpaceReached = true;
        item.enabled = false;
      } else if (item.price > resources) {
        item.buildNoMoney = true;
        item.enabled = false;
      }
    }
  }

  // --- Command methods called from the component ---

  onBuild(itemTypeId: number): void {
    const selectedItems = this.selectionService.getSelectedOwnItems();
    if (selectedItems.length === 0) return;

    const firstItem = selectedItems[0];
    const baseItemType = firstItem.getBaseItemType();

    if (baseItemType.getBuilderType() != null) {
      this.itemCockpitBridge.requestBuild(firstItem.getId(), itemTypeId);
    } else if (baseItemType.getFactoryType() != null) {
      const factoryIds = selectedItems.map(item => item.getId());
      this.itemCockpitBridge.requestFabricate(factoryIds, itemTypeId);
    }
  }

  onSell(): void {
    const itemIds = this.selectionService.getSelectedOwnItemIds();
    if (itemIds.length > 0) {
      this.itemCockpitBridge.sellItems(itemIds);
    }
  }

  onUnload(containerId: number): void {
    this.itemCockpitBridge.requestUnload(containerId);
  }

  keepOnlyOfType(baseItemTypeId: number): void {
    this.selectionService.keepOnlyOfType(baseItemTypeId);
  }
}
