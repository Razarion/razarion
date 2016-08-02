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


import com.btxtech.shared.gameengine.datatypes.itemtype.ConsumerType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.EnergyService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 22.11.2009
 * Time: 13:30:50
 */
@Dependent
public class SyncConsumer extends SyncBaseAbility {
    @Inject
    private EnergyService energyService;
    private ConsumerType consumerType;
    private boolean consuming = false;
    private boolean operationState;

    public void init(ConsumerType consumerType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.consumerType = consumerType;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) {
        operationState = syncItemInfo.isOperationState();
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        syncItemInfo.setOperationState(operationState);
    }

    public boolean isOperating() {
        return operationState;
    }

    public void setOperationState(boolean operationState) {
        this.operationState = operationState;
    }

    public void setConsuming(boolean consuming) {
        boolean oldState = this.consuming;
        this.consuming = consuming;
        if (oldState != consuming) {
            if (consuming) {
                energyService.consumerActivated(this);
            } else {
                energyService.consumerDeactivated(this);
            }
        }
    }

    public int getWattage() {
        return consumerType.getWattage();
    }

}