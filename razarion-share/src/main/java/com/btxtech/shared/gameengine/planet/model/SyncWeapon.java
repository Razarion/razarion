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

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.command.AttackCommand;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.ActiveProjectileContainer;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.BaseService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.PlanetService;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 18.11.2009
 * Time: 16:02:26
 */
@Dependent
public class SyncWeapon extends SyncBaseAbility {
    private static final long CHECK_DELTA = 1000;
    @Inject
    private ActivityService activityService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private BaseService baseService;
    @Inject
    private ActiveProjectileContainer activeProjectileContainer; // Not Synchronized
    @Inject
    private Instance<ActiveProjectileContainer> projectileInstance;
    private WeaponType weaponType;
    private Integer target;
    private boolean followTarget;
    private double reloadProgress;
    private DecimalPosition targetPosition; // Not Synchronized
    private long targetPositionLastCheck; // Not Synchronized
    private SyncMovable.OverlappingHandler overlappingHandler = new SyncMovable.OverlappingHandler() {
        @Override
        public Path calculateNewPath() {
            try {
                SyncBaseItem targetItem = (SyncBaseItem) baseItemService.getItem(target);
                return recalculateNewPath(weaponType.getRange(), targetItem.getSyncItemArea());
            } catch (ItemDoesNotExistException e) {
                stop();
                return null;
            }
        }
    };

    public void init(WeaponType weaponType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.weaponType = weaponType;
        reloadProgress = weaponType.getReloadTime();
        activeProjectileContainer = projectileInstance.get();
        activeProjectileContainer.init(weaponType, syncBaseItem);
    }

    public boolean isActive() {
        return target != null && getSyncBaseItem().isAlive();
    }

    /**
     * @return true if more tick are needed to fulfil the job
     */
    public boolean tick() {
        if (!getSyncBaseItem().isAlive()) {
            return false;
        }

        if (reloadProgress < weaponType.getReloadTime()) {
            reloadProgress += PlanetService.TICK_FACTOR;
        }

        boolean moreTicksNeeded = false;
        if (activeProjectileContainer.tick()) {
            moreTicksNeeded = true;
        }

        if (target != null && tickAttack()) {
            moreTicksNeeded = true;
        }

        return moreTicksNeeded || returnFalseIfReloaded();
    }

    private boolean tickAttack() {
        try {
            if (followTarget && !getSyncBaseItem().hasSyncMovable()) {
                throw new IllegalArgumentException("Weapon is followTarget but has now SyncMovable: " + getSyncBaseItem());
            }

            SyncBaseItem targetItem = (SyncBaseItem) baseItemService.getItem(target);

            if (!baseItemService.isEnemy(getSyncBaseItem(), targetItem)) {
                // May the guild member state has changed
                return false;
            }

            // Check if target has moved away
            if (targetPositionLastCheck + CHECK_DELTA < System.currentTimeMillis() && followTarget && getSyncBaseItem().hasSyncMovable() && isNewPathRecalculationAllowed()) {
                if (targetPosition != null) {
                    if (!targetPosition.equals(targetItem.getSyncItemArea().getPosition())) {
                        targetPosition = targetItem.getSyncItemArea().getPosition();
                        targetPositionLastCheck = System.currentTimeMillis();
                        if (isInRange(targetItem)) {
                            doAttack(targetItem);
                            return true;
                        } else {
                            recalculateAndSetNewPath(weaponType.getRange(), targetItem.getSyncItemArea());
                            activityService.onNewPathRecalculation(getSyncBaseItem());
                            return getSyncBaseItem().getSyncMovable().tickMove(overlappingHandler);
                        }
                    }
                }
                targetPosition = targetItem.getSyncItemArea().getPosition();
                targetPositionLastCheck = System.currentTimeMillis();
            }

            if (followTarget && getSyncBaseItem().hasSyncMovable() && getSyncBaseItem().getSyncMovable().tickMove(overlappingHandler)) {
                return true;
            }

            if (!followTarget && !isInRange(targetItem)) {
                stop();
                return returnFalseIfReloaded();
            }

            if (!isInRange(targetItem)) {
                if (isNewPathRecalculationAllowed()) {
                    // Destination place was may be taken. Calculate a new one or target has moved away
                    recalculateAndSetNewPath(weaponType.getRange(), targetItem.getSyncItemArea());
                    activityService.onNewPathRecalculation(getSyncBaseItem());
                    return true;
                } else {
                    return false;
                }
            }

            doAttack(targetItem);
            return true;
        } catch (ItemDoesNotExistException ignore) {
            // It has may be killed
            stop();
            return returnFalseIfReloaded();
        } catch (TargetHasNoPositionException e) {
            // Target may moved to a container
            stop();
            return returnFalseIfReloaded();
        }
    }

    private void doAttack(SyncBaseItem targetItem) {
        getSyncItemArea().turnTo(targetItem);
        if (reloadProgress >= weaponType.getReloadTime()) {
            activeProjectileContainer.createProjectile(targetItem);
            activityService.onProjectileFired(getSyncBaseItem());
            reloadProgress = 0;
        }
    }

    private boolean returnFalseIfReloaded() {
        return reloadProgress < weaponType.getReloadTime();

    }

    public void stop() {
        target = null;
        targetPosition = null;
        targetPositionLastCheck = 0;
        activeProjectileContainer.clear();
        if (getSyncBaseItem().hasSyncMovable()) {
            getSyncBaseItem().getSyncMovable().stop();
        }
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) {
        target = syncItemInfo.getTarget();
        followTarget = syncItemInfo.isFollowTarget();
        reloadProgress = syncItemInfo.getReloadProgress();
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        syncItemInfo.setTarget(target);
        syncItemInfo.setFollowTarget(followTarget);
        syncItemInfo.setReloadProgress(reloadProgress);
    }

    public void executeCommand(AttackCommand attackCommand) throws ItemDoesNotExistException {
        if (!getSyncBaseItem().isReady()) {
            return;
        }
        SyncBaseItem target = (SyncBaseItem) baseItemService.getItem(attackCommand.getTarget());
        if (!getSyncBaseItem().isEnemy(target)) {
            throw new IllegalArgumentException("Can not attack friendly target. Own: " + getSyncBaseItem() + " target: " + target);
        }

        if (isItemTypeDisallowed(target)) {
            throw new IllegalArgumentException(this + " Weapon not allowed to attack item type: " + target);
        }

        this.target = attackCommand.getTarget();
        followTarget = attackCommand.isFollowTarget();
        setPathToDestinationIfSyncMovable(attackCommand.getPathToDestination());
        targetPosition = null;
        targetPositionLastCheck = 0;
        activeProjectileContainer.clear();
    }

    public boolean isItemTypeDisallowed(SyncBaseItem target) {
        return weaponType.isItemTypeDisallowed(target.getBaseItemType().getId());
    }

    public boolean isAttackAllowedWithoutMoving(SyncItem target) throws TargetHasNoPositionException {
        if (!(target instanceof SyncBaseItem)) {
            return false;
        }
        SyncBaseItem baseTarget = (SyncBaseItem) target;
        return !isItemTypeDisallowed(baseTarget) && isInRange(baseTarget);

    }

    public boolean isAttackAllowed(SyncItem target) {
        return target instanceof SyncBaseItem
                && getSyncItemArea().hasPosition()
                && target.getSyncItemArea().hasPosition()
                && !isItemTypeDisallowed((SyncBaseItem) target);
    }

    public boolean isInRange(SyncBaseItem target) throws TargetHasNoPositionException {
        return getSyncItemArea().isInRange(weaponType.getRange(), target);
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public boolean isFollowTarget() {
        return followTarget;
    }

    public void setFollowTarget(boolean followTarget) {
        this.followTarget = followTarget;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public double getReloadProgress() {
        return reloadProgress;
    }

    public void setReloadProgress(double reloadProgress) {
        this.reloadProgress = reloadProgress;
    }

    public DecimalPosition getProjectileTarget() {
        return activeProjectileContainer.getProjectileTarget();
    }
}
