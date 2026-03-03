import { Injectable } from '@angular/core';
import { BabylonItem, BaseItemType, Diplomacy } from '../gwtangular/GwtAngularFacade';
import { BabylonBaseItemImpl } from './renderer/babylon-base-item.impl';
import { BabylonAudioService } from './renderer/babylon-audio.service';

@Injectable({
  providedIn: 'root'
})
export class SelectionService {
  private selectedOwnItems: BabylonBaseItemImpl[] = [];
  private selectedOwnItemIds = new Set<number>();
  private selectedOwnItemTypes = new Map<number, BaseItemType>();
  private selectedOtherId: number | null = null;
  private selectedOtherDiplomacy: Diplomacy | null = null;
  private selectedOtherItemTypeId: number | null = null;
  private selectedOtherBaseId: number | null = null;
  private selectedOtherItem: BabylonItem | null = null;
  private selectionListeners: (() => void)[] = [];

  constructor(private babylonAudioService: BabylonAudioService) {
  }

  selectOwnItems(items: BabylonBaseItemImpl[]): void {
    this.deselectCurrent();
    this.selectedOwnItems = [...items];
    this.selectedOwnItemIds.clear();
    this.selectedOwnItemTypes.clear();
    items.forEach(item => {
      this.selectedOwnItemIds.add(item.getId());
      this.selectedOwnItemTypes.set(item.getId(), item.getBaseItemType());
    });
    this.selectedOtherId = null;
    this.selectedOtherDiplomacy = null;
    this.selectedOtherItemTypeId = null;
    this.selectedOtherBaseId = null;
    this.selectedOwnItems.forEach(item => item.select(true));
    this.fireSelectionChanged();
    this.speakOwnSelection(items);
  }

  selectOther(id: number, diplomacy: Diplomacy, itemTypeId?: number, baseId?: number, item?: BabylonItem, itemTypeName?: string): void {
    this.deselectCurrent();
    this.selectedOwnItems = [];
    this.selectedOwnItemIds.clear();
    this.selectedOwnItemTypes.clear();
    this.selectedOtherId = id;
    this.selectedOtherDiplomacy = diplomacy;
    this.selectedOtherItemTypeId = itemTypeId ?? null;
    this.selectedOtherBaseId = baseId ?? null;
    if (item) {
      item.select(true);
      this.selectedOtherItem = item;
    }
    this.fireSelectionChanged();
    this.speakOtherSelection(diplomacy, itemTypeName);
  }

  clearSelection(): void {
    this.deselectCurrent();
    this.selectedOwnItems = [];
    this.selectedOwnItemIds.clear();
    this.selectedOwnItemTypes.clear();
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

  /** Permanently remove a disposed item (sold/destroyed). Clears selection if empty. */
  disposeItem(id: number): void {
    const idx = this.selectedOwnItems.findIndex(item => item.getId() === id);
    if (idx >= 0) {
      this.selectedOwnItems.splice(idx, 1);
    }
    this.selectedOwnItemTypes.delete(id);
    if (this.selectedOwnItemIds.delete(id) && this.selectedOwnItemIds.size === 0) {
      this.fireSelectionChanged();
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

  /** Permanently remove a disposed other item (destroyed). Clears selection. */
  disposeOther(id: number): void {
    if (this.selectedOtherId === id) {
      if (this.selectedOtherItem) {
        this.selectedOtherItem.select(false);
        this.selectedOtherItem = null;
      }
      this.selectedOtherId = null;
      this.selectedOtherDiplomacy = null;
      this.selectedOtherItemTypeId = null;
      this.selectedOtherBaseId = null;
      this.fireSelectionChanged();
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
      this.selectedOwnItemTypes.delete(item.getId());
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
    const ids: number[] = [];
    for (const [id, type] of this.selectedOwnItemTypes) {
      if (type.getPhysicalAreaConfig().fulfilledMovable()) {
        ids.push(id);
      }
    }
    return ids;
  }

  getAttackerIds(targetItemTypeId: number): number[] {
    const ids: number[] = [];
    for (const [id, type] of this.selectedOwnItemTypes) {
      const weapon = type.getWeaponType();
      if (weapon != null && !weapon.checkItemTypeDisallowed(targetItemTypeId)) {
        ids.push(id);
      }
    }
    return ids;
  }

  getHarvesterIds(): number[] {
    const ids: number[] = [];
    for (const [id, type] of this.selectedOwnItemTypes) {
      if (type.getHarvesterType() != null) {
        ids.push(id);
      }
    }
    return ids;
  }

  getBuilderIds(itemTypeId: number): number[] {
    const ids: number[] = [];
    for (const [id, type] of this.selectedOwnItemTypes) {
      const builder = type.getBuilderType();
      if (builder != null && builder.checkAbleToBuild(itemTypeId)) {
        ids.push(id);
      }
    }
    return ids;
  }

  getContainableIds(containerItemType: BaseItemType): number[] {
    const containerType = containerItemType.getItemContainerType();
    if (containerType == null) return [];
    const ids: number[] = [];
    for (const [id, type] of this.selectedOwnItemTypes) {
      if (containerType.isAbleToContain(type.getId())) {
        ids.push(id);
      }
    }
    return ids;
  }

  hasMovables(): boolean {
    for (const type of this.selectedOwnItemTypes.values()) {
      if (type.getPhysicalAreaConfig().fulfilledMovable()) return true;
    }
    return false;
  }

  hasAttackers(): boolean {
    for (const type of this.selectedOwnItemTypes.values()) {
      if (type.getWeaponType() != null) return true;
    }
    return false;
  }

  hasHarvesters(): boolean {
    for (const type of this.selectedOwnItemTypes.values()) {
      if (type.getHarvesterType() != null) return true;
    }
    return false;
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
    for (const [id, type] of this.selectedOwnItemTypes) {
      if (id !== targetItem.getId() && containerType.isAbleToContain(type.getId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if any selected builder can finalize-build the target item.
   * Used for cursor display.
   */
  canBeFinalizeBuild(targetItem: BabylonBaseItemImpl): boolean {
    if (targetItem.getBuildup() >= 1.0) return false;
    const targetItemTypeId = targetItem.getBaseItemType().getId();
    for (const [id, type] of this.selectedOwnItemTypes) {
      if (id === targetItem.getId()) continue;
      const builder = type.getBuilderType();
      if (builder != null && builder.checkAbleToBuild(targetItemTypeId)) {
        return true;
      }
    }
    return false;
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

  private speakOwnSelection(items: BabylonBaseItemImpl[]): void {
    if (items.length === 0) return;

    const counts = new Map<string, number>();
    for (const item of items) {
      const name = item.getBaseItemType().getName();
      if (name) {
        counts.set(name, (counts.get(name) ?? 0) + 1);
      }
    }
    if (counts.size === 0) return;

    const parts: string[] = [];
    for (const [name, count] of counts) {
      parts.push(count === 1 ? name : `${count} ${name}s`);
    }
    this.babylonAudioService.speakSelection(parts.join(', '));
  }

  private speakOtherSelection(diplomacy: Diplomacy, itemTypeName?: string): void {
    const name = itemTypeName ?? 'target';
    switch (diplomacy) {
      case Diplomacy.ENEMY:
        this.babylonAudioService.speakCommand(`Enemy ${name} spotted`);
        break;
      case Diplomacy.FRIEND:
        this.babylonAudioService.speakCommand(`Other player's ${name}`);
        break;
      case Diplomacy.RESOURCE:
        this.babylonAudioService.speakCommand(`Resource ${name}`);
        break;
      case Diplomacy.BOX:
        this.babylonAudioService.speakCommand(`Supply box`);
        break;
    }
  }
}
