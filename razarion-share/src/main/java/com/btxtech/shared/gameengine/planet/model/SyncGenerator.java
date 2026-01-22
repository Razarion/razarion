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

import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.itemtype.GeneratorType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.energy.EnergyService;

import jakarta.inject.Inject;

/**
 * User: beat
 * Date: 22.11.2009
 * Time: 13:30:50
 */

public class SyncGenerator extends SyncBaseAbility {

    private final EnergyService energyService;

    private final BaseItemService baseItemService;
    private GeneratorType generatorType;

    @Inject
    public SyncGenerator(BaseItemService baseItemService, EnergyService energyService) {
        this.baseItemService = baseItemService;
        this.energyService = energyService;
    }

    public void init(GeneratorType generatorType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.generatorType = generatorType;
    }

    @Override
    public void synchronize(SyncBaseItemInfo syncBaseItemInfo) {
        if (getSyncBaseItem().isBuildup() && !getSyncBaseItem().isSpawning()) {
            energyService.generatorActivated(this);
        }
    }

    @Override
    public void fillSyncItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        // Ignore
    }

    public void onReady() {
        if (baseItemService.getGameEngineMode() == GameEngineMode.MASTER) {
            energyService.generatorActivated(this);
        }
    }

    public int getWattage() {
        return generatorType.getWattage();
    }
}