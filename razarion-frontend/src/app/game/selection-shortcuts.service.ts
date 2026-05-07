import {Injectable} from '@angular/core';
import {Diplomacy} from '../gwtangular/GwtAngularFacade';
import {BabylonRenderServiceAccessImpl} from './renderer/babylon-render-service-access-impl.service';
import {SelectionService} from './selection.service';
import {GwtAngularService} from '../gwtangular/GwtAngularService';

export type SelectionShortcutCategory = 'builder' | 'factory' | 'harvester' | 'viper';

interface OwnItemRecord {
  category: SelectionShortcutCategory;
  baseItemTypeId: number;
  x: number;
  y: number;
}

@Injectable({providedIn: 'root'})
export class SelectionShortcutsService {
  // Last unit id cycled per category so the next click advances to a stable "next". Tracking by
  // id (not index) keeps cycling consistent when units are produced, destroyed, or move in/out
  // of the rendered set between clicks.
  private lastCycledIds: Record<SelectionShortcutCategory, number | null> = {
    builder: null,
    factory: null,
    harvester: null,
    viper: null
  };
  // BaseItemType ids we've ever seen as own units. Grows monotonically: entries are NOT removed
  // when units are view-culled, so getMyItemCount() returns correct server-side totals even when
  // most units of a type are no longer in babylonBaseItems (e.g. after the camera jumps to one
  // viper, all the others elsewhere on the map disappear from babylonBaseItems).
  private knownTypeIds: Record<SelectionShortcutCategory, Set<number>> = {
    builder: new Set(),
    factory: new Set(),
    harvester: new Set(),
    viper: new Set()
  };
  // Last known position per own unit id. Lets cycle() reach off-screen units that are no longer
  // in babylonBaseItems — without this the cycle gets stuck on the few units in the current
  // viewport (especially noticeable when own units are spread across multiple bases).
  private ownItemCache: Map<number, OwnItemRecord> = new Map();
  // Pending selection id for items that aren't yet in babylonBaseItems when the camera jumps
  // to an off-screen unit. Cleared once the item appears and gets selected, or after a short
  // grace window if the server hasn't re-sent the unit yet.
  private pendingSelectionTimeout: ReturnType<typeof setTimeout> | null = null;
  // Type-id discovery from the static game config runs once when itemTypeService is ready,
  // so getCount() returns correct totals from the start — without waiting until a unit of that
  // category has shown up in the viewport.
  private allTypesPopulated = false;

  constructor(
    private rendererService: BabylonRenderServiceAccessImpl,
    private selectionService: SelectionService,
    private gwtAngularService: GwtAngularService
  ) {
  }

  getCount(category: SelectionShortcutCategory): number {
    this.ensureKnownTypesPopulated();
    this.updateOwnItemCache();
    const facade = this.gwtAngularService.gwtAngularFacade;
    if (!facade?.baseItemUiService) {
      return 0;
    }
    let total = 0;
    this.knownTypeIds[category].forEach(typeId => {
      total += facade.baseItemUiService.getMyItemCount(typeId);
    });
    return total;
  }

  private ensureKnownTypesPopulated(): void {
    if (this.allTypesPopulated) {
      return;
    }
    const facade = this.gwtAngularService.gwtAngularFacade;
    if (!facade?.itemTypeService || typeof facade.itemTypeService.getAllBaseItemTypes !== 'function') {
      return;
    }
    const allTypes = facade.itemTypeService.getAllBaseItemTypes();
    if (!allTypes || allTypes.length === 0) {
      // StaticGameConfig may not be loaded yet on first cockpit render — leave the flag unset
      // so we retry on the next getCount() call.
      return;
    }
    allTypes.forEach(type => {
      const typeId = type.getId();
      if (type.getBuilderType() != null) this.knownTypeIds.builder.add(typeId);
      if (type.getFactoryType() != null) this.knownTypeIds.factory.add(typeId);
      if (type.getHarvesterType() != null) this.knownTypeIds.harvester.add(typeId);
      if (type.getInternalName() === 'Viper') this.knownTypeIds.viper.add(typeId);
    });
    this.allTypesPopulated = true;
  }

  cycle(category: SelectionShortcutCategory): void {
    this.ensureKnownTypesPopulated();
    const entries = this.collectOwnEntriesForCategory(category);
    if (entries.length === 0) {
      return;
    }
    const lastId = this.lastCycledIds[category];
    let nextIdx = 0;
    if (lastId !== null) {
      const lastIdxInEntries = entries.findIndex(e => e.id === lastId);
      nextIdx = lastIdxInEntries >= 0 ? (lastIdxInEntries + 1) % entries.length : 0;
    }
    const next = entries[nextIdx];
    this.lastCycledIds[category] = next.id;

    this.rendererService.setViewFieldCenter(next.x, next.y);

    if (this.pendingSelectionTimeout !== null) {
      clearTimeout(this.pendingSelectionTimeout);
      this.pendingSelectionTimeout = null;
    }
    if (!this.trySelectById(next.id)) {
      // The unit isn't in babylonBaseItems yet — give the server a moment to re-send it after the
      // view-field change, then try once more.
      this.pendingSelectionTimeout = setTimeout(() => {
        this.pendingSelectionTimeout = null;
        this.trySelectById(next.id);
      }, 300);
    }
  }

  // Fetches all own units from the server-aware API (not view-culled) and filters by category.
  // Falls back to the visible-items cache if the server-side method isn't available yet (older
  // WASM, or facade not ready).
  private collectOwnEntriesForCategory(category: SelectionShortcutCategory): { id: number, x: number, y: number }[] {
    const facade = this.gwtAngularService.gwtAngularFacade;
    if (facade?.baseItemUiService && typeof facade.baseItemUiService.getMyOwnSyncItemTickInfos === 'function') {
      const all = facade.baseItemUiService.getMyOwnSyncItemTickInfos();
      if (all && all.length > 0) {
        const typeIds = this.knownTypeIds[category];
        return all
          .filter(info => typeIds.has(info.itemTypeId))
          .map(info => ({id: info.id, x: info.x, y: info.y}))
          .sort((a, b) => a.id - b.id);
      }
    }
    // Fallback: only visible items
    this.updateOwnItemCache();
    return Array.from(this.ownItemCache.entries())
      .filter(([, record]) => record.category === category)
      .sort((a, b) => a[0] - b[0])
      .map(([id, record]) => ({id, x: record.x, y: record.y}));
  }

  private updateOwnItemCache(): void {
    const own = this.rendererService.getBabylonBaseItemsByDiplomacy(Diplomacy.OWN);
    own.forEach(item => {
      const type = item.getBaseItemType();
      let category: SelectionShortcutCategory | null = null;
      if (type.getBuilderType() != null) {
        category = 'builder';
      } else if (type.getFactoryType() != null) {
        category = 'factory';
      } else if (type.getHarvesterType() != null) {
        category = 'harvester';
      } else if (type.getInternalName() === 'Viper') {
        // Viper is a specific combat unit — there is no typed getter for it on BaseItemType,
        // so we match by internal name. Adjust the literal if the unit gets renamed.
        category = 'viper';
      }
      if (!category) {
        return;
      }
      const typeId = type.getId();
      this.knownTypeIds[category].add(typeId);
      const pos = item.getPosition();
      if (pos) {
        this.ownItemCache.set(item.getId(), {
          category,
          baseItemTypeId: typeId,
          x: pos.getX(),
          y: pos.getY()
        });
      }
    });
  }

  private trySelectById(id: number): boolean {
    const ownItems = this.rendererService.getBabylonBaseItemsByDiplomacy(Diplomacy.OWN);
    const item = ownItems.find(i => i.getId() === id);
    if (item) {
      this.selectionService.selectOwnItems([item]);
      return true;
    }
    return false;
  }
}
