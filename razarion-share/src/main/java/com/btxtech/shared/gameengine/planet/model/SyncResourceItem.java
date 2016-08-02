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
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.BaseItemService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 04.12.2009
 * Time: 20:08:41
 */
@Dependent
public class SyncResourceItem extends SyncItem {
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ActivityService activityService;
    private double amount;
    private boolean missionMoney = false;

    public void init(int id, Index position, ResourceType resourceType) {
        // TODO super.init(id, position, resourceType);
        amount = resourceType.getAmount();
        throw new UnsupportedOperationException();
    }

    public double harvest(double amount) {
        if (this.amount > amount) {
            this.amount -= amount;
            activityService.onResourceAmountChanged(this);
            return amount;
        } else {
            amount = this.amount;
            this.amount = 0;
            baseItemService.killSyncItem(this, null, false, false);
            return amount;
        }
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) throws NoSuchItemTypeException, ItemDoesNotExistException {
        super.synchronize(syncItemInfo);
        amount = syncItemInfo.getAmount();
    }

    @Override
    public SyncItemInfo getSyncInfo() {
        SyncItemInfo syncItemInfo = super.getSyncInfo();
        syncItemInfo.setAmount(amount);
        return syncItemInfo;
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

    public boolean isMissionMoney() {
        return missionMoney;
    }

    public void setMissionMoney(boolean missionMoney) {
        this.missionMoney = missionMoney;
    }
}
