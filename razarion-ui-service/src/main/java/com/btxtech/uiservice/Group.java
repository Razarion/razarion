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
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.SyncBaseItemMonitor;
import com.btxtech.uiservice.item.SyncItemMonitor;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: beat
 * Date: 09.11.2009
 * Time: 23:05:45
 */
@Dependent
public class Group {

    private ItemTypeService itemTypeService;

    private BaseItemUiService baseItemUiService;

    private SelectionHandler selectionHandler;
    // Use syncBaseItemsMonitors
    private Collection<SyncBaseItemSimpleDto> syncBaseItems = new ArrayList<>();
    private Collection<SyncBaseItemMonitor> syncBaseItemsMonitors = new ArrayList<>();

    @Inject
    public Group(SelectionHandler selectionHandler, BaseItemUiService baseItemUiService, ItemTypeService itemTypeService) {
        this.selectionHandler = selectionHandler;
        this.baseItemUiService = baseItemUiService;
        this.itemTypeService = itemTypeService;
    }

    void setItems(Collection<SyncBaseItemSimpleDto> syncBaseItems) {
        this.syncBaseItems = syncBaseItems;
        syncBaseItems.forEach(this::addMonitor);
    }

    public void addItem(SyncBaseItemSimpleDto syncBaseItem) {
        syncBaseItems.add(syncBaseItem);
        addMonitor(syncBaseItem);
    }

    private void addMonitor(SyncBaseItemSimpleDto syncBaseItem) {
        SyncBaseItemMonitor syncBaseItemMonitor = baseItemUiService.monitorSyncItem(syncBaseItem.getId());
        syncBaseItemsMonitors.add(syncBaseItemMonitor);
        syncBaseItemMonitor.setContainedChangeListener(syncItemMonitor -> selectionHandler.baseItemRemoved(new int[]{syncBaseItem.getId()}));
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

    public boolean remove(int syncItemId) {
        removeMonitor(syncItemId);
        for (Iterator<SyncBaseItemSimpleDto> iterator = syncBaseItems.iterator(); iterator.hasNext(); ) {
            SyncBaseItemSimpleDto syncBaseItem = iterator.next();
            if (syncBaseItem.getId() == syncItemId) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return syncBaseItemsMonitors.isEmpty();
    }

    public int getCount() {
        return syncBaseItemsMonitors.size();
    }

    @Deprecated
    // some properties are outdated. These properties are taken during the scelection (Snapshot). Use getItems() or getSyncBaseItemsMonitors().
    public Collection<SyncBaseItemSimpleDto> _getItems() {
        return syncBaseItems;
    }

    public Collection<SyncBaseItemSimpleDto> getItems() {
        return syncBaseItemsMonitors.stream().map(monitor -> monitor.getSyncBaseItemState().getSyncBaseItem()).collect(Collectors.toList());
    }

    public Collection<SyncBaseItemMonitor> getSyncBaseItemsMonitors() {
        return syncBaseItemsMonitors;
    }

    @Deprecated
    // some properties are outdated. These properties are taken during the scelection (Snapshot). Use getSyncBaseItemsMonitors().
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

    public boolean hasAttackers() {
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (baseItemType.getWeaponType() != null) {
                return true;
            }
        }
        return false;
    }

    public Collection<SyncBaseItemSimpleDto> getAttackers(int targetItemTypeId) {
        Collection<SyncBaseItemSimpleDto> attackers = new ArrayList<>();
        for (SyncBaseItemSimpleDto syncBaseItem : syncBaseItems) {
            BaseItemType baseItemType = itemTypeService.getBaseItemType(syncBaseItem.getItemTypeId());
            if (baseItemType.getWeaponType() == null) {
                continue;
            }
            if (baseItemType.getWeaponType().checkItemTypeDisallowed(targetItemTypeId)) {
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
            SyncBaseItemSimpleDto syncBaseItemSimpleDto = iterator.next();
            BaseItemType currentBaseItemType = itemTypeService.getBaseItemType(syncBaseItemSimpleDto.getItemTypeId());
            if (!(baseItemType.equals(currentBaseItemType))) {
                iterator.remove();
                removeMonitor(syncBaseItemSimpleDto);
            }
        }
    }

    public void removeMonitor(SyncBaseItemSimpleDto syncBaseItem) {
        for (Iterator<SyncBaseItemMonitor> iterator = syncBaseItemsMonitors.iterator(); iterator.hasNext(); ) {
            SyncBaseItemMonitor syncBaseItemsMonitor = iterator.next();
            if (syncBaseItemsMonitor.getSyncItemId() == syncBaseItem.getId()) {
                iterator.remove();
                syncBaseItemsMonitor.release();
            }
        }
    }

    public void removeMonitor(int syncBaseItemId) {
        for (Iterator<SyncBaseItemMonitor> iterator = syncBaseItemsMonitors.iterator(); iterator.hasNext(); ) {
            SyncBaseItemMonitor syncBaseItemsMonitor = iterator.next();
            if (syncBaseItemsMonitor.getSyncItemId() == syncBaseItemId) {
                iterator.remove();
                syncBaseItemsMonitor.release();
            }
        }
    }

    public void release() {
        syncBaseItemsMonitors.forEach(SyncItemMonitor::release);
        syncBaseItemsMonitors.clear();
    }
}
