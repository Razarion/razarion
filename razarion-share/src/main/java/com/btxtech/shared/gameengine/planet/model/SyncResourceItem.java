package com.btxtech.shared.gameengine.planet.model;


import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;
import com.btxtech.shared.gameengine.planet.ResourceService;

import jakarta.inject.Inject;

/**
 * User: Razarion contributors
 * Date: 04.12.2009
 * Time: 20:08:41
 */

public class SyncResourceItem extends SyncItem {

    private final ResourceService resourceService;
    private double amount;

    @Inject
    public SyncResourceItem(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public void setup(int amount) {
        this.amount = amount;
    }

    public double harvest(double amount) {
        if (this.amount > amount) {
            this.amount -= amount;
            return amount;
        } else {
            amount = this.amount;
            this.amount = 0;
            resourceService.resourceExhausted(this);
            return amount;
        }
    }

    public void synchronize(SyncResourceItemInfo syncResourceItemInfo) {
        getAbstractSyncPhysical().synchronize(syncResourceItemInfo.getSyncPhysicalAreaInfo());
        amount = syncResourceItemInfo.getAmount();
    }

    public SyncResourceItemInfo getSyncInfo() {
        SyncResourceItemInfo syncResourceItemInfo = new SyncResourceItemInfo();
        syncResourceItemInfo.setId(getId());
        syncResourceItemInfo.setSyncPhysicalAreaInfo(getAbstractSyncPhysical().getSyncPhysicalAreaInfo());
        syncResourceItemInfo.setResourceItemTypeId(getItemType().getId());
        syncResourceItemInfo.setAmount(amount);
        return syncResourceItemInfo;
    }

    @Override
    public boolean isAlive() {
        return amount > 0;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
