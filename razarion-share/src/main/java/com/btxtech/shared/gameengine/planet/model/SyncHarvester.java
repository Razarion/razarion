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

import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.command.MoneyCollectCommand;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.BaseService;
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
    private BaseItemService baseItemService;
    @Inject
    private ActivityService activityService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private BaseService baseService;
    private HarvesterType harvesterType;
    private Integer target;
//    private SyncMovable.OverlappingHandler overlappingHandler = new SyncMovable.OverlappingHandler() {
//        @Override
//        public Path calculateNewPath() {
//            try {
//                SyncResourceItem resource = (SyncResourceItem) baseItemService.getItem(target);
//                return recalculateNewPath(harvesterType.getRange(), resource.getSyncItemArea());
//            } catch (ItemDoesNotExistException e) {
//                stop();
//                return null;
//            }
//        }
//    };

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

    public boolean tick() throws ItemDoesNotExistException {
        if (!getSyncBaseItem().isAlive()) {
            return false;
        }

        // TODOif (getSyncBaseItem().getSyncMovable().tickMove(overlappingHandler)) {
        // TODO    return true;
        // TODO}

//        try {
//            SyncResourceItem resource = (SyncResourceItem) baseItemService.getItem(target);
//            if (!isInRange(resource)) {
//                if (isNewPathRecalculationAllowed()) {
//                    // Destination place was may be taken. Calculate a new one.
//                    recalculateAndSetNewPath(harvesterType.getRange(), resource.getSyncItemArea());
//                    activityService.onNewPathRecalculation(getSyncBaseItem());
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//            getSyncItemArea().turnTo(resource);
//            double money = resource.harvest(PlanetService.TICK_FACTOR * harvesterType.getProgress());
//            baseService.depositResource(money, getSyncBaseItem().getBase());
//            return true;
//        } catch (ItemDoesNotExistException ignore) {
//            // Target may be empty
//            stop();
//            return false;
//        } catch (TargetHasNoPositionException e) {
//            // Target moved to a container
//            stop();
//            return false;
//        }
        throw new UnsupportedOperationException();
    }

    public void stop() {
//        target = null;
//        getSyncBaseItem().getSyncMovable().stop();
        throw new UnsupportedOperationException();

    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) {
        target = syncItemInfo.getTarget();
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        syncItemInfo.setTarget(target);
    }

    public void executeCommand(MoneyCollectCommand attackCommand) throws ItemDoesNotExistException {
        SyncResourceItem resource = resourceService.getSyncResourceItem(attackCommand.getTarget());
        this.target = resource.getId();
        setPathToDestinationIfSyncMovable(attackCommand.getPathToDestination());
    }

    public boolean isInRange(SyncResourceItem target) throws TargetHasNoPositionException {
        return getSyncItemArea().isInRange(harvesterType.getRange(), target);
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
