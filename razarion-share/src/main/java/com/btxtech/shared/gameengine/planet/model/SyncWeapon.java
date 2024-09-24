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
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.command.AttackCommand;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.gameengine.planet.projectile.ProjectileService;

import javax.enterprise.context.Dependent;
import javax.inject.Provider;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 18.11.2009
 * Time: 16:02:26
 */
@Dependent
public class SyncWeapon extends SyncBaseAbility {
    private static final long CHECK_DELTA = 1000;

    private BaseItemService baseItemService;

    private ProjectileService projectileService;

    private SyncItemContainerServiceImpl syncItemContainerService;

    private PathingService pathingService;

    private Provider<SyncTurret> syncTurretInstance;

    private SyncService syncService;
    private WeaponType weaponType;
    private SyncBaseItem target;
    private boolean followTarget;
    private double reloadProgress;
    private DecimalPosition targetPosition; // Not Synchronized
    private long targetPositionLastCheck; // Not Synchronized
    private SyncTurret syncTurret;

    @Inject
    public SyncWeapon(SyncService syncService, Provider<com.btxtech.shared.gameengine.planet.model.SyncTurret> syncTurretInstance, PathingService pathingService, SyncItemContainerServiceImpl syncItemContainerService, ProjectileService projectileService, BaseItemService baseItemService) {
        this.syncService = syncService;
        this.syncTurretInstance = syncTurretInstance;
        this.pathingService = pathingService;
        this.syncItemContainerService = syncItemContainerService;
        this.projectileService = projectileService;
        this.baseItemService = baseItemService;
    }

    public void init(WeaponType weaponType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.weaponType = weaponType;
        reloadProgress = weaponType.getReloadTime();
        syncTurret = syncTurretInstance.get();
        syncTurret.init(getSyncBaseItem(), weaponType.getTurretType());
    }

    public boolean isActive() {
        return target != null && getSyncBaseItem().isAlive();
    }

    /**
     * @return true if more tick are needed to fulfil the job
     */
    public boolean tick() {
        if (reloadProgress < weaponType.getReloadTime()) {
            reloadProgress += PlanetService.TICK_FACTOR;
        }

        return target != null && tickAttack();
    }

    private boolean tickAttack() {
        try {
            if (!target.isAlive()) {
                stop();
                return false;
            }

            if (syncTurret != null) {
                syncTurret.tick(target.getSyncPhysicalArea().getPosition());
            }

            if (!isInRange(target)) {
                if (!followTarget) {
                    stop();
                    return false;
                }
                if (!getSyncPhysicalArea().canMove()) {
                    throw new IllegalStateException("SyncWeapon out of range from Target and getSyncPhysicalArea can not move");
                }
                if (!getSyncPhysicalMovable().hasDestination()) {
                    if (baseItemService.getGameEngineMode() == GameEngineMode.MASTER) {
                        getSyncPhysicalMovable().setPath(pathingService.setupPathToDestination(getSyncBaseItem(), weaponType.getRange(), target));
                        syncService.notifySendSyncBaseItem(getSyncBaseItem());
                    } else {
                        return true;
                    }
                }

                // Check if target has moved away
                if (baseItemService.getGameEngineMode() == GameEngineMode.MASTER) {
                    if (targetPositionLastCheck + CHECK_DELTA < System.currentTimeMillis()) {
                        if (!targetPosition.equals(target.getSyncPhysicalArea().getPosition())) {
                            targetPosition = target.getSyncPhysicalArea().getPosition();
                            getSyncPhysicalMovable().setPath(pathingService.setupPathToDestination(getSyncBaseItem(), weaponType.getRange(), target));
                            syncService.notifySendSyncBaseItem(getSyncBaseItem());
                        }
                        targetPositionLastCheck = System.currentTimeMillis();
                    }
                }
                return true;
            }

            if (followTarget && getSyncPhysicalMovable().hasDestination()) {
                getSyncPhysicalMovable().stop();
            }

            if (syncTurret != null && !syncTurret.isOnTarget(target.getSyncPhysicalArea().getPosition())) {
                return true;
            }

            doAttack(target);
            return true;
        } catch (TargetHasNoPositionException e) {
            // Target may moved to a container
            stop();
            return returnFalseIfReloaded();
        }
    }

    private void doAttack(SyncBaseItem targetItem) {
        if (reloadProgress >= weaponType.getReloadTime()) {
            projectileService.fireProjectile(getSyncBaseItem(), targetItem);
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
    public void synchronize(SyncBaseItemInfo syncBaseItemInfo) {
        if (syncBaseItemInfo.getTarget() != null) {
            target = syncItemContainerService.getSyncBaseItemSave(syncBaseItemInfo.getTarget());
            targetPosition = target.getSyncPhysicalArea().getPosition();
            targetPositionLastCheck = System.currentTimeMillis();
        } else {
            target = null;
            targetPosition = null;
        }
        followTarget = syncBaseItemInfo.getFollowTarget();
        reloadProgress = syncBaseItemInfo.getReloadProgress();
    }

    @Override
    public void fillSyncItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        if (target != null && target.isAlive()) {
            syncBaseItemInfo.setTarget(target.getId());
        }
        syncBaseItemInfo.setFollowTarget(followTarget);
        syncBaseItemInfo.setReloadProgress(reloadProgress);
    }

    public void executeCommand(AttackCommand attackCommand) throws ItemDoesNotExistException {
        SyncBaseItem target = syncItemContainerService.getSyncBaseItemSave(attackCommand.getTarget());

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
        if (followTarget) {
            getSyncPhysicalMovable().setPath(attackCommand.getSimplePath());
        }
        targetPosition = target.getSyncPhysicalArea().getPosition();
        targetPositionLastCheck = System.currentTimeMillis();
    }

    public boolean isItemTypeDisallowed(SyncBaseItem target) {
        return weaponType.checkItemTypeDisallowed(target.getBaseItemType().getId());
    }

    boolean isAttackAllowed(SyncItem target) {
        return target instanceof SyncBaseItem
                && getSyncPhysicalArea().hasPosition()
                && target.getSyncPhysicalArea().hasPosition()
                && !isItemTypeDisallowed((SyncBaseItem) target);
    }

    boolean isInRange(SyncBaseItem target) throws TargetHasNoPositionException {
        return getSyncBaseItem().getSyncPhysicalArea().isInRange(weaponType.getRange(), target);
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public SyncBaseItem getTarget() {
        return target;
    }

    public SyncTurret getSyncTurret() {
        return syncTurret;
    }
}
