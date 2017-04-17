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

import com.btxtech.shared.gameengine.datatypes.command.HarvestCommand;
import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.ResourceService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 22.11.2009
 * Time: 13:30:50
 */
@Dependent
public class SyncHarvester extends SyncBaseAbility {
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private BaseItemService baseItemService;
    private HarvesterType harvesterType;
    private SyncResourceItem resource;
    private boolean harvesting;

    public void init(HarvesterType harvesterType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.harvesterType = harvesterType;
    }

    public boolean isActive() {
        return resource != null;
    }

    public boolean isHarvesting() {
        return harvesting;
    }

    public boolean tick() {
        if (!resource.isAlive()) {
            stop();
            return false;
        }
        if (!isInRange(resource)) {
            harvesting = false;
            if (!getSyncPhysicalMovable().hasDestination()) {
                throw new IllegalStateException("Harvester out of range from Resource and SyncPhysicalMovable does not have a position");
            }
            return true;
        }
        if (getSyncPhysicalMovable().hasDestination()) {
            getSyncPhysicalMovable().stop();
        }
        harvesting = true;

        double harvestedResources = resource.harvest(PlanetService.TICK_FACTOR * harvesterType.getProgress());
        getSyncBaseItem().getBase().addResource(harvestedResources);
        gameLogicService.onResourcesHarvested(getSyncBaseItem(), harvestedResources, resource);
        return true;
    }

    public void stop() {
        harvesting = false;
        resource = null;
    }

    @Override
    public void synchronize(SyncBaseItemInfo syncBaseItemInfo) {
        // resource = syncItemInfo.getTarget();
    }

    @Override
    public void fillSyncItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        // syncItemInfo.setTarget(resource);
    }

    public void executeCommand(HarvestCommand harvestCommand) {
        resource = resourceService.getSyncResourceItem(harvestCommand.getTarget());
        if (!isInRange(resource)) {
            getSyncPhysicalMovable().setPath(harvestCommand.getPathToDestination());
        }
    }

    public boolean isInRange(SyncResourceItem target) throws TargetHasNoPositionException {
        return getSyncPhysicalArea().isInRange(harvesterType.getRange(), target);
    }

    public SyncResourceItem getResource() {
        return resource;
    }
}
