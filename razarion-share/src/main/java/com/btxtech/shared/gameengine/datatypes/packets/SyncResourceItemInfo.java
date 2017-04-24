package com.btxtech.shared.gameengine.datatypes.packets;

/**
 * Created by Beat
 * 24.04.2017.
 */
public class SyncResourceItemInfo {
    private int id;
    private int resourceItemTypeId;
    private SyncPhysicalAreaInfo syncPhysicalAreaInfo;
    private double amount;

    public int getId() {
        return id;
    }

    public SyncResourceItemInfo setId(int id) {
        this.id = id;
        return this;
    }

    public int getResourceItemTypeId() {
        return resourceItemTypeId;
    }

    public SyncResourceItemInfo setResourceItemTypeId(int resourceItemTypeId) {
        this.resourceItemTypeId = resourceItemTypeId;
        return this;
    }

    public SyncPhysicalAreaInfo getSyncPhysicalAreaInfo() {
        return syncPhysicalAreaInfo;
    }

    public SyncResourceItemInfo setSyncPhysicalAreaInfo(SyncPhysicalAreaInfo syncPhysicalAreaInfo) {
        this.syncPhysicalAreaInfo = syncPhysicalAreaInfo;
        return this;
    }

    public double getAmount() {
        return amount;
    }

    public SyncResourceItemInfo setAmount(double amount) {
        this.amount = amount;
        return this;
    }
}
