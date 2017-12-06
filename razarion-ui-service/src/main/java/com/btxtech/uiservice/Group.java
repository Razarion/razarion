/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.uiservice;


import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * User: beat
 * Date: 09.11.2009
 * Time: 23:05:45
 */
@Dependent
public class Group {
    @Inject
    private ItemTypeService itemTypeService;
    private Collection<SyncBaseItemSimpleDto> syncBaseItems = new ArrayList<>();

    void setItems(Collection<SyncBaseItemSimpleDto> syncBaseItems) {
        this.syncBaseItems = syncBaseItems;
    }

    public void addItem(SyncBaseItemSimpleDto syncBaseItem) {
        syncBaseItems.add(syncBaseItem);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Collection<SyncBaseItemSimpleDto> otherSyncBaseItems = ((Group) o).syncBaseItems;

        if (syncBaseItems == null) {
            return otherSyncBaseItems == null;
        } else if (otherSyncBaseItems == null) {
            return false;
        }
        if (syncBaseItems.isEmpty() && otherSyncBaseItems.isEmpty()) {
            return true;
        }
        if (syncBaseItems.size() != otherSyncBaseItems.size()) {
            return false;
        }
        for (SyncBaseItemSimpleDto item1 : syncBaseItems) {
            boolean found = false;
            for (SyncBaseItemSimpleDto item2 : otherSyncBaseItems) {
                if (item1.equals(item2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }


    @Override
    public int hashCode() {
        return syncBaseItems != null ? syncBaseItems.hashCode() : 0;
    }


    public boolean onlyFactories() {
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (baseItemType.getFactoryType() == null) {
                return false;
            }
        }
        return true;
    }

    public boolean onlyConstructionVehicle() {
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (baseItemType.getBuilderType() == null) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(SyncBaseItemSimpleDto syncBaseItem) {
        return syncBaseItems.contains(syncBaseItem);
    }

    public boolean remove(SyncBaseItemSimpleDto syncBaseItem) {
        return syncBaseItems.remove(syncBaseItem);
    }

    public boolean isEmpty() {
        return syncBaseItems.isEmpty();
    }

    public int getCount() {
        return syncBaseItems.size();
    }

    public Collection<SyncBaseItemSimpleDto> getItems() {
        return syncBaseItems;
    }

    public SyncBaseItemSimpleDto getFirst() {
        return syncBaseItems.iterator().next();
    }

    public Collection<SyncBaseItemSimpleDto> getBuilders(int toBeBuiltItemTypeId) {
        Collection<SyncBaseItemSimpleDto> builder = new ArrayList<>();
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (baseItemType.getBuilderType() == null) {
                continue;
            }
            if (!baseItemType.getBuilderType().checkAbleToBuild(toBeBuiltItemTypeId)) {
                continue;
            }
            builder.add(syncBaseItem);
        }
        return builder;
    }

    public Collection<SyncBaseItemSimpleDto> getAttackers(SyncBaseItemSimpleDto target) {
        Collection<SyncBaseItemSimpleDto> attackers = new ArrayList<>();
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (baseItemType.getWeaponType() == null) {
                continue;
            }
            if (baseItemType.getWeaponType().checkItemTypeDisallowed(target.getItemTypeId())) {
                continue;
            }
            attackers.add(syncBaseItem);
        }
        return attackers;
    }

    public Collection<SyncBaseItemSimpleDto> getHarvesters() {
        Collection<SyncBaseItemSimpleDto> harvesters = new ArrayList<>();
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (baseItemType.getHarvesterType() == null) {
                continue;
            }
            harvesters.add(syncBaseItem);
        }
        return harvesters;
    }

    public Collection<SyncBaseItemSimpleDto> getMovables() {
        Collection<SyncBaseItemSimpleDto> movable = new ArrayList<>();
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (baseItemType.getPhysicalAreaConfig().fulfilledMovable()) {
                movable.add(syncBaseItem);
            }
        }
        return movable;
    }

    public Set<TerrainType> getMovableTerrainTypes() {
        Set<TerrainType> movable = new HashSet<>();
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (baseItemType.getPhysicalAreaConfig().fulfilledMovable()) {
                movable.add(baseItemType.getPhysicalAreaConfig().getTerrainType());
            }
        }
        return movable;
    }

    public int count() {
        return syncBaseItems.size();
    }

    public Map<BaseItemType, Collection<SyncBaseItemSimpleDto>> getGroupedItems() {
        HashMap<BaseItemType, Collection<SyncBaseItemSimpleDto>> map = new HashMap<>();
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            Collection<SyncBaseItemSimpleDto> collection = map.computeIfAbsent(baseItemType, k -> new ArrayList<>());
            collection.add(syncBaseItem);
        }
        return map;
    }

    void keepOnlyOwnOfType(BaseItemType baseItemType) {
        for (Iterator<SyncBaseItemSimpleDto> iterator = syncBaseItems.iterator(); iterator.hasNext(); ) {
            BaseItemType currentBaseItemType = itemTypeService.getBaseItemType(iterator.next().getItemTypeId());
            if (!(baseItemType.equals(currentBaseItemType))) {
                iterator.remove();
            }
        }
    }
}
