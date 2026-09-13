package com.btxtech.shared.gameengine.planet.model;

import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.HouseType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;

import jakarta.inject.Inject;

/**
 * User: Razarion contributors
 * Date: 14.09.2010
 * Time: 13:28:48
 */

public class SyncHouse extends SyncBaseAbility {
    private HouseType houseType;

    @Inject
    public SyncHouse() {
    }

    public void init(HouseType houseType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.houseType = houseType;
    }

    @Override
    public void synchronize(SyncBaseItemInfo syncBaseItemInfo) throws NoSuchItemTypeException, ItemDoesNotExistException {
        // Ignore
    }

    @Override
    public void fillSyncItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        // Ignore
    }

    public int getSpace() {
        return houseType.getSpace();
    }
}
