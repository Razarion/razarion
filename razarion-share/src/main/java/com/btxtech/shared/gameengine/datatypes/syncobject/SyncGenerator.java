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

package com.btxtech.shared.gameengine.datatypes.syncobject;

import com.btxtech.shared.gameengine.datatypes.itemtype.GeneratorType;
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
public class SyncGenerator extends SyncBaseAbility {
    @Inject
    private EnergyService energyService;
    private GeneratorType generatorType;
    private boolean generating = false;

    public void init(GeneratorType generatorType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.generatorType = generatorType;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) {
        // Ignore
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        // Ignore
    }

    public void setGenerating(boolean generating) {
        boolean oldState = this.generating;
        this.generating = generating;
        if (oldState != generating) {
            if (generating) {
                energyService.generatorActivated(this);
            } else {
                energyService.generatorDeactivated(this);
            }
        }
    }

    public int getWattage() {
        return generatorType.getWattage();
    }
}