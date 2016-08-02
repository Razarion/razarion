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


import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.PlanetService;

/**
 * User: beat
 * Date: 04.12.2009
 * Time: 19:22:47
 */
public abstract class SyncBaseAbility {
    private SyncBaseItem syncBaseItem;

    public void init(SyncBaseItem syncBaseItem) {
        this.syncBaseItem = syncBaseItem;
    }

    public SyncBaseItem getSyncBaseItem() {
        return syncBaseItem;
    }

    public SyncItemArea getSyncItemArea() {
        return syncBaseItem.getSyncItemArea();
    }

    public void setPathToDestinationIfSyncMovable(Path path) {
        if (path != null && syncBaseItem.hasSyncMovable()) {
            syncBaseItem.getSyncMovable().setPathToDestination(path.getPath(), path.getActualDestinationAngel());
        }
    }

    public boolean isNewPathRecalculationAllowed() {
        return PlanetService.MODE == PlanetMode.MASTER;
    }

    public void recalculateAndSetNewPath(int range, SyncItemArea target) {
        Path path = recalculateNewPath(range, target);
        setPathToDestinationIfSyncMovable(path);
    }

    public Path recalculateNewPath(int range, SyncItemArea target) {
        // TODO
        throw new UnsupportedOperationException();
//        SyncBaseItem syncItem = getSyncBaseItem();
//        AttackFormationItem format = getPlanetServices().getCollisionService().getDestinationHint(syncItem,
//                range,
//                target);
//        if (format.isInRange()) {
//            Path path = getPlanetServices().getCollisionService().setupPathToDestination(syncItem, format.getDestinationHint());
//            if (!path.isDestinationReachable()) {
//                throw new PathCanNotBeFoundException("Can not find path in recalculateNewPath: " + syncItem, syncItem.getSyncItemArea().getPosition(), null);
//            }
//            return path;
//        } else {
//            throw new PathCanNotBeFoundException("Not in range recalculateNewPath: " + syncItem, syncItem.getSyncItemArea().getPosition(), null);
//        }
    }

    public abstract void synchronize(SyncItemInfo syncItemInfo) throws NoSuchItemTypeException, ItemDoesNotExistException;

    public abstract void fillSyncItemInfo(SyncItemInfo syncItemInfo);


}
