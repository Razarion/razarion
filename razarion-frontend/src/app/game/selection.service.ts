import { Injectable } from '@angular/core';
import { BabylonItem, BaseItemType, Diplomacy } from '../gwtangular/GwtAngularFacade';
import { BabylonBaseItemImpl } from './renderer/babylon-base-item.impl';

@Injectable({
  providedIn: 'root'
})
export class SelectionService {
  private selectedOwnItems: BabylonBaseItemImpl[] = [];
  private selectedOwnItemIds = new Set<number>();
  private selectedOtherId: number | null = null;
  private selectedOtherDiplomacy: Diplomacy | null = null;
  private selectedOtherItemTypeId: number | null = null;
  private selectedOtherBaseId: number | null = null;
  private selectedOtherItem: BabylonItem | null = null;
  private selectionListeners: (() => void)[] = [];

  constructor() {
  }

  selectOwnItems(items: BabylonBaseItemImpl[]): void {
    this.deselectCurrent();
    this.selectedOwnItems = [...items];
    this.selectedOwnItemIds.clear();
    items.forEach(item => this.selectedOwnItemIds.add(item.getId()));
    this.selectedOtherId = null;
    this.selectedOtherDiplomacy = null;
    this.selectedOtherItemTypeId = null;
    this.selectedOtherBaseId = null;
    this.selectedOwnItems.forEach(item => item.select(true));
    this.fireSelectionChanged();
  }

  selectOther(id: number, diplomacy: Diplomacy, itemTypeId?: number, baseId?: number, item?: BabylonItem): void {
    this.deselectCurrent();
    this.selectedOwnItems = [];
    this.selectedOwnItemIds.clear();
    this.selectedOtherId = id;
    this.selectedOtherDiplomacy = diplomacy;
    this.selectedOtherItemTypeId = itemTypeId ?? null;
    this.selectedOtherBaseId = baseId ?? null;
    if (item) {
      item.select(true);
      this.selectedOtherItem = item;
    }
    this.fireSelectionChanged();
  }

  clearSelection(): void {
    this.deselectCurrent();
    this.selectedOwnItems = [];
    this.selectedOwnItemIds.clear();
    this.selectedOtherId = null;
    this.selectedOtherDiplomacy = null;
    this.selectedOtherItemTypeId = null;
    this.selectedOtherBaseId = null;
    this.fireSelectionChanged();
  }

  removeItem(id: number): void {
    const idx = this.selectedOwnItems.findIndex(item => item.getId() === id);
    if (idx >= 0) {
      this.selectedOwnItems.splice(idx, 1);
      // Keep id in selectedOwnItemIds so it can be reattached when scrolling back.
      // Do NOT fire selectionChanged — the selection hasn't changed from the user's
      // perspective, the item just scrolled out of view. Cockpit and cursor stay.
    }
  }

  /**
   * Called when a BabylonBaseItemImpl is created (item scrolls into view).
   * If its ID is in the persistent selection, reattach it.
   */
  tryReattachItem(item: BabylonBaseItemImpl): void {
    if (this.selectedOwnItemIds.has(item.getId())) {
      if (!this.selectedOwnItems.some(i => i.getId() === item.getId())) {
        this.selectedOwnItems.push(item);
        item.select(true);
      }
    }
  }

  removeOther(id: number): void {
    if (this.selectedOtherId === id) {
      if (this.selectedOtherItem) {
        this.selectedOtherItem.select(false);
        this.selectedOtherItem = null;
      }
      // Keep selectedOtherId and metadata — item just scrolled out of view.
      // Do NOT fire selectionChanged so cockpit stays visible.
    }
  }

  keepOnlyOfType(baseItemTypeId: number): void {
    const kept: BabylonBaseItemImpl[] = [];
    const removed: BabylonBaseItemImpl[] = [];
    for (const item of this.selectedOwnItems) {
      if (item.getBaseItemType().getId() === baseItemTypeId) {
        kept.push(item);
      } else {
        removed.push(item);
      }
    }
    if (removed.length === 0) return;
    removed.forEach(item => {
      item.select(false);
      this.selectedOwnItemIds.delete(item.getId());
    });
    this.selectedOwnItems = kept;
    this.fireSelectionChanged();
  }

  hasOwnSelection(): boolean {
    return this.selectedOwnItemIds.size > 0;
  }

  getSelectedOwnItems(): BabylonBaseItemImpl[] {
    return this.selectedOwnItems;
  }

  getSelectedOwnItemIds(): number[] {
    return Array.from(this.selectedOwnItemIds);
  }

  getSelectedOtherId(): number | null {
    return this.selectedOtherId;
  }

  getSelectedOtherDiplomacy(): Diplomacy | null {
    return this.selectedOtherDiplomacy;
  }

  getSelectedOtherItemTypeId(): number | null {
    return this.selectedOtherItemTypeId;
  }

  getSelectedOtherBaseId(): number | null {
    return this.selectedOtherBaseId;
  }

  getMovableIds(): number[] {
    return this.selectedOwnItems
      .filter(item => item.getBaseItemType().getPhysicalAreaConfig().fulfilledMovable())
      .map(item => item.getId());
  }

  getAttackerIds(targetItemTypeId: number): number[] {
    return this.selectedOwnItems
      .filter(item => {
        const weapon = item.getBaseItemType().getWeaponType();
        return weapon != null && !weapon.checkItemTypeDisallowed(targetItemTypeId);
      })
      .map(item => item.getId());
  }

  getHarvesterIds(): number[] {
    return this.selectedOwnItems
      .filter(item => item.getBaseItemType().getHarvesterType() != null)
      .map(item => item.getId());
  }

  getBuilderIds(itemTypeId: number): number[] {
    return this.selectedOwnItems
      .filter(item => {
        const builder = item.getBaseItemType().getBuilderType();
        return builder != null && builder.checkAbleToBuild(itemTypeId);
      })
      .map(item => item.getId());
  }

  getContainableIds(containerItemType: BaseItemType): number[] {
    const containerType = containerItemType.getItemContainerType();
    if (containerType == null) return [];
    return this.selectedOwnItems
      .filter(item => containerType.isAbleToContain(item.getBaseItemType().getId()))
      .map(item => item.getId());
  }

  hasMovables(): boolean {
    return this.selectedOwnItems.some(item =>
      item.getBaseItemType().getPhysicalAreaConfig().fulfilledMovable());
  }

  hasAttackers(): boolean {
    return this.selectedOwnItems.some(item =>
      item.getBaseItemType().getWeaponType() != null);
  }

  hasHarvesters(): boolean {
    return this.selectedOwnItems.some(item =>
      item.getBaseItemType().getHarvesterType() != null);
  }

  /**
   * Check if the target item (a container) can contain any of the selected items.
   * Used for cursor display.
   */
  canContain(targetItem: BabylonBaseItemImpl): boolean {
    const baseItemType = targetItem.getBaseItemType();
    if (!baseItemType) return false;
    const containerType = baseItemType.getItemContainerType();
    if (containerType == null) return false;
    return this.selectedOwnItems.some(item =>
      item.getId() !== targetItem.getId() &&
      containerType.isAbleToContain(item.getBaseItemType().getId()));
  }

  /**
   * Check if any selected builder can finalize-build the target item.
   * Used for cursor display.
   */
  canBeFinalizeBuild(targetItem: BabylonBaseItemImpl): boolean {
    if (targetItem.getBuildup() >= 1.0) return false;
    const targetItemTypeId = targetItem.getBaseItemType().getId();
    return this.selectedOwnItems.some(item => {
      if (item.getId() === targetItem.getId()) return false;
      const builder = item.getBaseItemType().getBuilderType();
      return builder != null && builder.checkAbleToBuild(targetItemTypeId);
    });
  }

  addSelectionListener(callback: () => void): void {
    this.selectionListeners.push(callback);
  }

  removeSelectionListener(callback: () => void): void {
    const idx = this.selectionListeners.indexOf(callback);
    if (idx >= 0) {
      this.selectionListeners.splice(idx, 1);
    }
  }

  private deselectCurrent(): void {
    this.selectedOwnItems.forEach(item => item.select(false));
    if (this.selectedOtherItem) {
      this.selectedOtherItem.select(false);
      this.selectedOtherItem = null;
    }
  }

  private fireSelectionChanged(): void {
    this.selectionListeners.forEach(listener => listener());
  }
}
