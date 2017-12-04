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

package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.command.UnloadContainerCommand;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.WrongOperationSurfaceException;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.utils.CollectionUtils;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * User: beat
 * Date: 01.05.2010
 * Time: 11:38:49
 */
@Dependent
public class SyncItemContainer extends SyncBaseAbility {
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private GameLogicService gameLogicService;
    private ItemContainerType itemContainerType;
    private List<Integer> containedItems = new ArrayList<>();
    private DecimalPosition unloadPos;

    public void init(ItemContainerType itemContainerType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.itemContainerType = itemContainerType;
    }

    @Override
    public void synchronize(SyncBaseItemInfo syncBaseItemInfo) throws ItemDoesNotExistException {
        unloadPos = syncBaseItemInfo.getUnloadPos();
        containedItems = syncBaseItemInfo.getContainedItems();
    }

    @Override
    public void fillSyncItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        syncBaseItemInfo.setUnloadPos(unloadPos);
        syncBaseItemInfo.setContainedItems(CollectionUtils.saveArrayListCopy(containedItems));
    }

    public void load(SyncBaseItem syncBaseItem) {
        if (PlanetService.MODE != PlanetMode.MASTER) {
            return;
        }

        checkAbleToContainThrow(syncBaseItem);
        if (containedItems.size() < itemContainerType.getMaxCount()) {
            containedItems.add(syncBaseItem.getId());
            syncBaseItem.setContained(getSyncBaseItem());
            gameLogicService.onSyncItemLoaded(getSyncBaseItem(), syncBaseItem);
        }
    }

    public void executeCommand(UnloadContainerCommand unloadContainerCommand) throws WrongOperationSurfaceException {
        if (containedItems.isEmpty()) {
            throw new IllegalStateException("No items in item container: " + getSyncBaseItem());
        }
        unloadPos = unloadContainerCommand.getUnloadPos();
    }

    public boolean tick() throws ItemDoesNotExistException {
        unload();
        stop();
        return false;
    }

    private void unload() throws ItemDoesNotExistException {
        if (PlanetService.MODE != PlanetMode.MASTER) {
            return;
        }
        containedItems.removeIf(containedItemId -> {
            SyncBaseItem containedItem = syncItemContainerService.getSyncBaseItemSave(containedItemId);
            if (allowedUnload(unloadPos, containedItem)) {
                containedItem.clearContained(unloadPos);
                gameLogicService.onSyncItemUnloaded(containedItem);
                return true;
            }
            return false;
        });
        gameLogicService.onSyncItemContainerUnloaded(getSyncBaseItem());
    }

    public void stop() {
        unloadPos = null;
    }

    public boolean isActive() {
        return unloadPos != null;
    }

    public double getRange() {
        return itemContainerType.getRange();
    }

    public List<Integer> getContainedItems() {
        return containedItems;
    }

    private void checkAbleToContainThrow(SyncBaseItem syncBaseItem) {
        if (getSyncBaseItem().equals(syncBaseItem)) {
            throw new IllegalArgumentException("Can not contain oneself: " + this);
        }

        if (!itemContainerType.isAbleToContain(syncBaseItem.getBaseItemType().getId())) {
            throw new IllegalArgumentException("Container " + getSyncBaseItem() + " is not able to contain: " + syncBaseItem);
        }
    }

    private boolean allowedUnload(DecimalPosition position, SyncBaseItem containedItem) throws ItemDoesNotExistException {
        return getSyncPhysicalArea().isInRange(getRange(), unloadPos)
                && terrainService.getPathingAccess().isTerrainTypeAllowed(containedItem.getSyncPhysicalArea().getTerrainType(), position, containedItem.getSyncPhysicalArea().getRadius());
    }
}
