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
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.command.UnloadContainerCommand;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.WrongOperationSurfaceException;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemContainerType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: beat
 * Date: 01.05.2010
 * Time: 11:38:49
 */

public class SyncItemContainer extends SyncBaseAbility {
    public static final TerrainType DEFAULT_UNLOAD_TERRAIN_TYPE = TerrainType.LAND;

    private SyncItemContainerServiceImpl syncItemContainerService;

    private TerrainService terrainService;

    private GameLogicService gameLogicService;

    private BaseItemService baseItemService;

    private SyncService syncService;
    private ItemContainerType itemContainerType;
    private List<SyncBaseItem> containedItems = new ArrayList<>();
    private DecimalPosition unloadPos;
    private double maxContainingRadius;

    @Inject
    public SyncItemContainer(SyncService syncService, BaseItemService baseItemService, GameLogicService gameLogicService, TerrainService terrainService, SyncItemContainerServiceImpl syncItemContainerService) {
        this.syncService = syncService;
        this.baseItemService = baseItemService;
        this.gameLogicService = gameLogicService;
        this.terrainService = terrainService;
        this.syncItemContainerService = syncItemContainerService;
    }

    public void init(ItemContainerType itemContainerType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.itemContainerType = itemContainerType;
    }

    @Override
    public void synchronize(SyncBaseItemInfo syncBaseItemInfo) throws ItemDoesNotExistException {
        unloadPos = syncBaseItemInfo.getUnloadPos();
        if (syncBaseItemInfo.getContainedItems() != null && !syncBaseItemInfo.getContainedItems().isEmpty()) {
            containedItems = syncBaseItemInfo.getContainedItems().stream().map(containedId -> syncItemContainerService.getSyncBaseItemSave(containedId)).collect(Collectors.toList());
        } else {
            containedItems = new ArrayList<>();
        }
        setupMaxContainingRadius();
    }

    @Override
    public void fillSyncItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        syncBaseItemInfo.setUnloadPos(unloadPos);
        if (!containedItems.isEmpty()) {
            syncBaseItemInfo.setContainedItems(containedItems.stream().map(SyncItem::getId).collect(Collectors.toList()));
        }
    }

    public void load(SyncBaseItem syncBaseItem) {
        if (baseItemService.getGameEngineMode() != GameEngineMode.MASTER) {
            return;
        }

        checkAbleToContainThrow(syncBaseItem);
        if (containedItems.size() < itemContainerType.getMaxCount()) {
            containedItems.add(syncBaseItem);
            setupMaxContainingRadius();
            syncBaseItem.setContained(getSyncBaseItem());
            syncService.notifySendSyncBaseItem(getSyncBaseItem());
            syncService.notifySendSyncBaseItem(syncBaseItem);
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
        if (baseItemService.getGameEngineMode() != GameEngineMode.MASTER) {
            return;
        }
        containedItems.removeIf(contained -> {
            if (allowedUnload()) {
                contained.clearContained(unloadPos);
                syncService.notifySendSyncBaseItem(contained);
                gameLogicService.onSyncItemUnloaded(contained);
                return true;
            }
            return false;
        });
        setupMaxContainingRadius();
        syncService.notifySendSyncBaseItem(getSyncBaseItem());
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

    public double getMaxContainingRadius() {
        return maxContainingRadius;
    }

    public List<SyncBaseItem> getContainedItems() {
        return containedItems;
    }

    private void checkAbleToContainThrow(SyncBaseItem syncBaseItem) {
        if (getSyncBaseItem().equals(syncBaseItem)) {
            throw new IllegalArgumentException("Can not contain oneself: " + this);
        }

        if (syncBaseItem.getSyncPhysicalArea().getTerrainType() != DEFAULT_UNLOAD_TERRAIN_TYPE) {
            throw new IllegalArgumentException("Container " + getSyncBaseItem() + " does only allow '" + DEFAULT_UNLOAD_TERRAIN_TYPE + "'. Given type:" + syncBaseItem.getSyncPhysicalArea().getTerrainType());
        }

        if (!itemContainerType.isAbleToContain(syncBaseItem.getBaseItemType().getId())) {
            throw new IllegalArgumentException("Container " + getSyncBaseItem() + " is not able to contain: " + syncBaseItem);
        }
    }

    private boolean allowedUnload() throws ItemDoesNotExistException {
        return getSyncPhysicalArea().isInRange(getRange(), unloadPos) && terrainService.getPathingAccess().isTerrainTypeAllowed(DEFAULT_UNLOAD_TERRAIN_TYPE, unloadPos, maxContainingRadius);
    }

    private void setupMaxContainingRadius() {
        if (containedItems != null && !containedItems.isEmpty()) {
            maxContainingRadius = containedItems.stream().map(contained -> contained.getSyncPhysicalArea().getRadius()).max(Double::compare).orElse(0.0);
        } else {
            maxContainingRadius = 0;
        }
    }
}
