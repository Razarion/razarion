package com.btxtech.shared.gameengine.datatypes.packets;

/**
 * Created by Beat
 * 24.04.2017.
 */
public class SyncBoxItemInfo {
    private int id;
    private int boxItemTypeId;
    private SyncPhysicalAreaInfo syncPhysicalAreaInfo;

    public int getId() {
        return id;
    }

    public SyncBoxItemInfo setId(int id) {
        this.id = id;
        return this;
    }

    public int getBoxItemTypeId() {
        return boxItemTypeId;
    }

    public SyncBoxItemInfo setBoxItemTypeId(int boxItemTypeId) {
        this.boxItemTypeId = boxItemTypeId;
        return this;
    }

    public SyncPhysicalAreaInfo getSyncPhysicalAreaInfo() {
        return syncPhysicalAreaInfo;
    }

    public SyncBoxItemInfo setSyncPhysicalAreaInfo(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        this.syncPhysicalAreaInfo = syncPhysicalAreaInfo;
        return this;
    }
}
