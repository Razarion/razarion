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

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.SurfaceType;
import com.btxtech.shared.gameengine.datatypes.command.UnloadContainerCommand;
import com.btxtech.shared.gameengine.datatypes.exception.ItemContainerFullException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.WrongOperationSurfaceException;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.CollectionUtils;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * User: beat
 * Date: 01.05.2010
 * Time: 11:38:49
 */
@Dependent
public class SyncItemContainer extends SyncBaseAbility {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private ActivityService activityService;
    private ItemContainerType itemContainerType;
    private List<Integer> containedItems = new ArrayList<>();
    private Index unloadPos;

    public void init(ItemContainerType itemContainerType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.itemContainerType = itemContainerType;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) throws NoSuchItemTypeException, ItemDoesNotExistException {
        unloadPos = syncItemInfo.getUnloadPos();
        containedItems = syncItemInfo.getContainedItems();
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        syncItemInfo.setUnloadPos(Index.saveCopy(unloadPos));
        syncItemInfo.setContainedItems(CollectionUtils.saveArrayListCopy(containedItems));
    }

    public void load(SyncBaseItem syncBaseItem) throws ItemContainerFullException, WrongOperationSurfaceException {
        if (PlanetService.MODE != PlanetMode.MASTER) {
            return;
        }

        isAbleToContainThrow(syncBaseItem);
        isOnOperationSurfaceThrow();
        containedItems.add(syncBaseItem.getId());
        syncBaseItem.setContained(getSyncBaseItem().getId());
        activityService.onSyncItemLoaded(getSyncBaseItem(), syncBaseItem);
    }

    public void executeCommand(UnloadContainerCommand unloadContainerCommand) throws WrongOperationSurfaceException {
        if (containedItems.isEmpty()) {
            throw new IllegalStateException("No items in item container: " + getSyncBaseItem());
        }
        isOnOperationSurfaceThrow();
        unloadPos = unloadContainerCommand.getUnloadPos();
    }

    public boolean tick() throws ItemDoesNotExistException {
        if (!getSyncBaseItem().isAlive()) {
            return false;
        }
        if (!isActive()) {
            return false;
        }

        getSyncItemArea().turnTo(unloadPos);
        unload();
        stop();
        return false;
    }

    private void unload() throws ItemDoesNotExistException {
        if (PlanetService.MODE != PlanetMode.MASTER) {
            return;
        }
        for (Iterator<Integer> iterator = containedItems.iterator(); iterator.hasNext(); ) {
            Integer containedItem = iterator.next();
            if (allowedUnload(unloadPos, containedItem)) {
                SyncBaseItem syncItem = (SyncBaseItem) baseItemService.getItem(containedItem);
                syncItem.clearContained(unloadPos);
                iterator.remove();
            }
        }
        activityService.onSyncItemUnloaded(getSyncBaseItem());
    }

    public void stop() {
        unloadPos = null;
        if (getSyncBaseItem().hasSyncMovable()) {
            getSyncBaseItem().getSyncMovable().stop();
        }
    }

    public Index getUnloadPos() {
        return unloadPos;
    }

    public void setUnloadPos(Index unloadPos) {
        this.unloadPos = unloadPos;
    }

    public boolean isActive() {
        return unloadPos != null;
    }

    public int getRange() {
        return itemContainerType.getRange();
    }

    public ItemContainerType getItemContainerType() {
        return itemContainerType;
    }

    public List<Integer> getContainedItems() {
        return containedItems;
    }

    public void setContainedItems(List<Integer> containedItems) {
        this.containedItems = containedItems;
    }

    private void isAbleToContainThrow(SyncBaseItem syncBaseItem) throws ItemContainerFullException {
        if (getSyncBaseItem().equals(syncBaseItem)) {
            throw new IllegalArgumentException("Can not contain oneself: " + this);
        }

        if (!itemContainerType.isAbleToContain(syncBaseItem.getBaseItemType().getId())) {
            throw new IllegalArgumentException("Container " + getSyncBaseItem() + " is not able to contain: " + syncBaseItem);
        }

        if (containedItems.size() >= itemContainerType.getMaxCount()) {
            throw new ItemContainerFullException(this, containedItems.size());
        }
    }

    private void isOnOperationSurfaceThrow() throws WrongOperationSurfaceException {
        SurfaceType operationSurfaceType = itemContainerType.getOperationSurfaceType();
        if (operationSurfaceType == null || operationSurfaceType == SurfaceType.NONE) {
            return;
        }
        if (!terrainService.hasSurfaceTypeInRegion(operationSurfaceType, getSyncItemArea().generateCoveringRectangle())) {
            throw new WrongOperationSurfaceException(getSyncBaseItem());
        }
    }

    public boolean isAbleToLoad(SyncBaseItem syncBaseItem) {
        try {
            isAbleToContainThrow(syncBaseItem);
            isOnOperationSurfaceThrow();
            return true;
        } catch (IllegalArgumentException ignore) {
            return false;
        } catch (ItemContainerFullException ignore) {
            return false;
        } catch (WrongOperationSurfaceException e) {
            return false;
        }
    }

    public boolean atLeastOneAllowedToLoad(Collection<SyncBaseItem> syncBaseItems) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            if (isAbleToLoad(syncBaseItem)) {
                return true;
            }
        }
        return false;
    }

    public boolean atLeastOneAllowedToUnload(Index position) {
        try {
            isOnOperationSurfaceThrow();
            for (Integer containedItem : containedItems) {
                if (allowedUnload(position, containedItem)) {
                    return true;
                }
            }
        } catch (ItemDoesNotExistException e) {
            exceptionHandler.handleException(e);
        } catch (WrongOperationSurfaceException e) {
            return false;
        }
        return false;
    }

    private boolean allowedUnload(Index position, int containedItem) throws ItemDoesNotExistException {
        return isInUnloadRange(position) && terrainService.isFree(position, baseItemService.getItem(containedItem).getItemType(), null, null);
    }

    private boolean isInUnloadRange(Index unloadPos) {
        return getSyncItemArea().isInRange(getRange(), unloadPos);
    }
}
