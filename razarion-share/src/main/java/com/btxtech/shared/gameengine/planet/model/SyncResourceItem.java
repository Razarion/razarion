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


import com.btxtech.shared.gameengine.datatypes.packets.SyncResourceItemInfo;
import com.btxtech.shared.gameengine.planet.ResourceService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 04.12.2009
 * Time: 20:08:41
 */
@Dependent
public class SyncResourceItem extends SyncItem {

    private ResourceService resourceService;
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
        getSyncPhysicalArea().synchronize(syncResourceItemInfo.getSyncPhysicalAreaInfo());
        amount = syncResourceItemInfo.getAmount();
    }

    public SyncResourceItemInfo getSyncInfo() {
        SyncResourceItemInfo syncResourceItemInfo = new SyncResourceItemInfo();
        syncResourceItemInfo.setId(getId());
        syncResourceItemInfo.setSyncPhysicalAreaInfo(getSyncPhysicalArea().getSyncPhysicalAreaInfo());
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
