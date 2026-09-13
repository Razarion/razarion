package com.btxtech.shared.gameengine.planet.model;


import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;

/**
 * User: Razarion contributors
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

    public AbstractSyncPhysical getAbstractSyncPhysical() {
        return syncBaseItem.getAbstractSyncPhysical();
    }

    public SyncPhysicalMovable getSyncPhysicalMovable() {
        return syncBaseItem.getSyncPhysicalMovable();
    }

    public abstract void synchronize(SyncBaseItemInfo syncItemInfo) throws NoSuchItemTypeException, ItemDoesNotExistException;

    public abstract void fillSyncItemInfo(SyncBaseItemInfo syncItemInfo);


}
