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
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.ActivityService;
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
    private ActivityService activityService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private BaseItemService baseItemService;
    private HarvesterType harvesterType;
    private Integer target;

    public void init(HarvesterType harvesterType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.harvesterType = harvesterType;
    }

    public boolean isActive() {
        return target != null;
    }

// TODO   public boolean isHarvesting() {
//   TODO     return isActive() && !getSyncBaseItem().getSyncMovable().isActive();
//  TODO  }

    public boolean tick() {
        try {
            SyncResourceItem resource = resourceService.getSyncResourceItem(target);
            if (!isInRange(resource)) {
                if (!getSyncPhysicalMovable().hasDestination()) {
                    throw new IllegalStateException("Harvester out of range from Resource and SyncPhysicalMovable does not have a position");
                }
                return true;
            }
            if (getSyncPhysicalMovable().hasDestination()) {
                getSyncPhysicalMovable().stop();
            }

            double harvestedResources = resource.harvest(PlanetService.TICK_FACTOR * harvesterType.getProgress());
            getSyncBaseItem().getBase().addResource(harvestedResources);
            activityService.onResourcesHarvested(getSyncBaseItem(), harvestedResources, resource);
            return true;
        } catch (ItemDoesNotExistException ignore) {
            // Target may be empty
            stop();
            return false;
        } catch (TargetHasNoPositionException e) {
            // Target moved to a container
            stop();
            return false;
        }
    }

    public void stop() {
        target = null;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) {
        target = syncItemInfo.getTarget();
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        syncItemInfo.setTarget(target);
    }

    public void executeCommand(HarvestCommand harvestCommand) {
        SyncResourceItem resource = resourceService.getSyncResourceItem(harvestCommand.getTarget());
        this.target = resource.getId();
        if (!isInRange(resource)) {
            getSyncPhysicalMovable().setDestination(harvestCommand.getPathToDestination());
        }
    }

    public boolean isInRange(SyncResourceItem target) throws TargetHasNoPositionException {
        return getSyncPhysicalArea().isInRange(harvesterType.getRange(), target);
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public HarvesterType getHarvesterType() {
        return harvesterType;
    }
}
