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
import com.btxtech.shared.gameengine.datatypes.command.AttackCommand;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.projectile.ProjectileService;

import javax.enterprise.context.Dependent;
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
    private ProjectileService projectileService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    private WeaponType weaponType;
    private SyncBaseItem target;
    private boolean followTarget;
    private double reloadProgress;
    private DecimalPosition targetPosition; // Not Synchronized
    private long targetPositionLastCheck; // Not Synchronized

    public void init(WeaponType weaponType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.weaponType = weaponType;
        reloadProgress = weaponType.getReloadTime();
    }

    public boolean isActive() {
        return target != null && getSyncBaseItem().isAlive();
    }

    /**
     * @return true if more tick are needed to fulfil the job
     */
    public boolean tick(long timeStamp) {
        if (reloadProgress < weaponType.getReloadTime()) {
            reloadProgress += PlanetService.TICK_FACTOR;
        }

        return target != null && tickAttack(timeStamp);
    }

    private boolean tickAttack(long timeStamp) {
        try {
            if (target.isAlive()) {
                stop();
                return false;
            }

            if (!isInRange(target)) {
                if (!followTarget) {
                    throw new IllegalStateException("SyncWeapon out of range but not allowed to follow target");
                }
                if (!getSyncPhysicalArea().canMove()) {
                    throw new IllegalStateException("SyncWeapon out of range from Target and getSyncPhysicalArea can not move");
                }
                if (!getSyncPhysicalMovable().hasDestination()) {
                    throw new IllegalStateException("SyncWeapon out of range from Target and SyncPhysicalMovable does not have a destination");
                }

                // Check if target has moved away
                if (targetPositionLastCheck + CHECK_DELTA < System.currentTimeMillis()) {
                    if (!targetPosition.equals(target.getSyncPhysicalArea().getXYPosition())) {
                        targetPosition = target.getSyncPhysicalArea().getXYPosition();
                        throw new UnsupportedOperationException();
//                            recalculateAndSetNewPath(weaponType.getRange(), targetItem);
//                            activityService.onNewPathRecalculation(getSyncBaseItem());
//                            return getSyncBaseItem().getSyncMovable().tickMove(overlappingHandler);
                    }
                    targetPositionLastCheck = System.currentTimeMillis();
                }
                return true;
            }

            if (getSyncPhysicalMovable().hasDestination()) {
                getSyncPhysicalMovable().stop();
            }

            doAttack(timeStamp, target);
            return true;
        } catch (TargetHasNoPositionException e) {
            // Target may moved to a container
            stop();
            return returnFalseIfReloaded();
        }
    }

    private void doAttack(long timeStamp, SyncBaseItem targetItem) {
        if (reloadProgress >= weaponType.getReloadTime()) {
            projectileService.fireProjectile(timeStamp, getSyncBaseItem(), targetItem);
            reloadProgress = 0;
        }
    }

    private boolean returnFalseIfReloaded() {
        return reloadProgress < weaponType.getReloadTime();
    }

    public void stop() {
        target = null;
        targetPosition = null;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) {
        // target = syncItemInfo.getTarget();
        followTarget = syncItemInfo.isFollowTarget();
        reloadProgress = syncItemInfo.getReloadProgress();
    }

    @Override
    public void fillSyncItemInfo(SyncItemInfo syncItemInfo) {
        // syncItemInfo.setTarget(target);
        syncItemInfo.setFollowTarget(followTarget);
        syncItemInfo.setReloadProgress(reloadProgress);
    }

    public void executeCommand(AttackCommand attackCommand) throws ItemDoesNotExistException {
        SyncBaseItem target = syncItemContainerService.getSyncBaseItem(attackCommand.getTarget());

        if (!getSyncBaseItem().isEnemy(target)) {
            throw new IllegalArgumentException("Can not attack friendly target. Own: " + getSyncBaseItem() + " target: " + target);
        }

        if (weaponType.getRange() <= 0) {
            throw new IllegalArgumentException("Can not attack target. Range mus be bigger than 0. Own: " + getSyncBaseItem());
        }

        if (weaponType.getDamage() <= 0) {
            throw new IllegalArgumentException("Can not attack target. Damage mus be bigger than 0. Own: " + getSyncBaseItem());
        }

        if (isItemTypeDisallowed(target)) {
            throw new IllegalArgumentException(this + " Weapon not allowed to attack item type: " + target);
        }

        this.target = target;
        followTarget = attackCommand.isFollowTarget();
        getSyncPhysicalMovable().setDestination(attackCommand.getPathToDestination());
        targetPosition = attackCommand.getPathToDestination().getDestination();
        targetPositionLastCheck = System.currentTimeMillis();
    }

    public boolean isItemTypeDisallowed(SyncBaseItem target) {
        return weaponType.checkItemTypeDisallowed(target.getBaseItemType().getId());
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
                && getSyncPhysicalArea().hasPosition()
                && target.getSyncPhysicalArea().hasPosition()
                && !isItemTypeDisallowed((SyncBaseItem) target);
    }

    public boolean isInRange(SyncBaseItem target) throws TargetHasNoPositionException {
        return getSyncBaseItem().getSyncPhysicalArea().isInRange(weaponType.getRange(), target);
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public SyncBaseItem getTarget() {
        return target;
    }
}
