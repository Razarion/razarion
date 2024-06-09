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
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.command.AttackCommand;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderFinalizeCommand;
import com.btxtech.shared.gameengine.datatypes.command.FactoryCommand;
import com.btxtech.shared.gameengine.datatypes.command.HarvestCommand;
import com.btxtech.shared.gameengine.datatypes.command.LoadContainerCommand;
import com.btxtech.shared.gameengine.datatypes.command.MoveCommand;
import com.btxtech.shared.gameengine.datatypes.command.PickupBoxCommand;
import com.btxtech.shared.gameengine.datatypes.command.UnloadContainerCommand;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.TargetHasNoPositionException;
import com.btxtech.shared.gameengine.datatypes.exception.WrongOperationSurfaceException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 04.12.2009
 * Time: 19:11:49
 */
@Dependent
public class SyncBaseItem extends SyncItem {
    @Inject
    private Instance<SyncBaseAbility> instance;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private CommandService commandService;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private BoxService boxService;
    @Inject
    private SyncItemContainerServiceImpl syncItemContainerService;
    private PlayerBase base;
    private double buildup;
    private double health;
    private SyncWeapon syncWeapon;
    private SyncFactory syncFactory;
    private SyncBuilder syncBuilder;
    private SyncHarvester syncHarvester;
    private SyncGenerator syncGenerator;
    private SyncConsumer syncConsumer;
    private SyncItemContainer syncItemContainer;
    private SyncHouse syncHouse;
    private boolean isRazarionEarningOrConsuming = false;
    private double spawnProgress;
    private SyncBoxItem syncBoxItemToPick;
    private SyncBaseItem targetContainer;
    private SyncBaseItem containedIn;


    public void setup(PlayerBase base) throws NoSuchItemTypeException {
        this.base = base;

        BaseItemType baseItemType = getBaseItemType();
        health = baseItemType.getHealth();

        if (baseItemType.getWeaponType() != null) {
            syncWeapon = instance.select(SyncWeapon.class).get();
            syncWeapon.init(baseItemType.getWeaponType(), this);
        } else {
            syncWeapon = null;
        }

        if (baseItemType.getFactoryType() != null) {
            syncFactory = instance.select(SyncFactory.class).get();
            syncFactory.init(baseItemType.getFactoryType(), this);
            isRazarionEarningOrConsuming = true;
        } else {
            syncFactory = null;
        }

        if (baseItemType.getBuilderType() != null) {
            syncBuilder = instance.select(SyncBuilder.class).get();
            syncBuilder.init(baseItemType.getBuilderType(), this);
            isRazarionEarningOrConsuming = true;
        } else {
            syncBuilder = null;
        }

        if (baseItemType.getHarvesterType() != null) {
            syncHarvester = instance.select(SyncHarvester.class).get();
            syncHarvester.init(baseItemType.getHarvesterType(), this);
            isRazarionEarningOrConsuming = true;
        } else {
            syncHarvester = null;
        }

        if (baseItemType.getGeneratorType() != null) {
            syncGenerator = instance.select(SyncGenerator.class).get();
            syncGenerator.init(baseItemType.getGeneratorType(), this);
        } else {
            syncGenerator = null;
        }

        if (baseItemType.getConsumerType() != null) {
            syncConsumer = instance.select(SyncConsumer.class).get();
            syncConsumer.init(baseItemType.getConsumerType(), this);
        } else {
            syncConsumer = null;
        }

        if (baseItemType.getItemContainerType() != null) {
            syncItemContainer = instance.select(SyncItemContainer.class).get();
            syncItemContainer.init(baseItemType.getItemContainerType(), this);
        } else {
            syncItemContainer = null;
        }

        if (baseItemType.getHouseType() != null) {
            syncHouse = instance.select(SyncHouse.class).get();
            syncHouse.init(baseItemType.getHouseType(), this);
        } else {
            syncHouse = null;
        }
    }

    public PlayerBase getBase() {
        return base;
    }

    public void synchronize(SyncBaseItemInfo syncBaseItemInfo) throws ItemDoesNotExistException {
        health = syncBaseItemInfo.getHealth();
        spawnProgress = syncBaseItemInfo.getSpawnProgress();
        setBuildup(syncBaseItemInfo.getBuildup());
        getSyncPhysicalArea().synchronize(syncBaseItemInfo.getSyncPhysicalAreaInfo());

        if (syncBaseItemInfo.getSyncBoxItemId() != null) {
            syncBoxItemToPick = boxService.getSyncBoxItem(syncBaseItemInfo.getSyncBoxItemId());
        } else {
            syncBoxItemToPick = null;
        }

        if (syncBaseItemInfo.getTargetContainer() != null) {
            targetContainer = syncItemContainerService.getSyncBaseItemSave(syncBaseItemInfo.getTargetContainer());
        } else {
            targetContainer = null;
        }

        if (syncBaseItemInfo.getContainedIn() != null) {
            containedIn = syncItemContainerService.getSyncBaseItemSave(syncBaseItemInfo.getContainedIn());
        } else {
            containedIn = null;
        }

        if (syncWeapon != null) {
            syncWeapon.synchronize(syncBaseItemInfo);
        }

        if (syncFactory != null) {
            syncFactory.synchronize(syncBaseItemInfo);
        }

        if (syncBuilder != null) {
            syncBuilder.synchronize(syncBaseItemInfo);
        }

        if (syncHarvester != null) {
            syncHarvester.synchronize(syncBaseItemInfo);
        }

        if (syncConsumer != null) {
            syncConsumer.synchronize(syncBaseItemInfo);
        }

        if (syncGenerator != null) {
            syncGenerator.synchronize(syncBaseItemInfo);
        }

        if (syncItemContainer != null) {
            syncItemContainer.synchronize(syncBaseItemInfo);
        }
    }

    public SyncBaseItemInfo getSyncInfo() {
        SyncBaseItemInfo syncBaseItemInfo = new SyncBaseItemInfo();
        syncBaseItemInfo.setId(getId());
        syncBaseItemInfo.setSyncPhysicalAreaInfo(getSyncPhysicalArea().getSyncPhysicalAreaInfo());
        syncBaseItemInfo.setItemTypeId(getItemType().getId());
        syncBaseItemInfo.setBaseId(base.getBaseId());
        syncBaseItemInfo.setHealth(health);
        syncBaseItemInfo.setBuildup(buildup);
        syncBaseItemInfo.setSpawnProgress(spawnProgress);

        if (syncBoxItemToPick != null) {
            syncBaseItemInfo.setSyncBoxItemId(syncBoxItemToPick.getId());
        }

        if (targetContainer != null) {
            syncBaseItemInfo.setTargetContainer(targetContainer.getId());
        }

        if (containedIn != null) {
            syncBaseItemInfo.setContainedIn(containedIn.getId());
        }

        if (syncWeapon != null) {
            syncWeapon.fillSyncItemInfo(syncBaseItemInfo);
        }

        if (syncFactory != null) {
            syncFactory.fillSyncItemInfo(syncBaseItemInfo);
        }

        if (syncBuilder != null) {
            syncBuilder.fillSyncItemInfo(syncBaseItemInfo);
        }

        if (syncHarvester != null) {
            syncHarvester.fillSyncItemInfo(syncBaseItemInfo);
        }

        if (syncConsumer != null) {
            syncConsumer.fillSyncItemInfo(syncBaseItemInfo);
        }

        if (syncGenerator != null) {
            syncGenerator.fillSyncItemInfo(syncBaseItemInfo);
        }

        if (syncItemContainer != null) {
            syncItemContainer.fillSyncItemInfo(syncBaseItemInfo);
        }

        return syncBaseItemInfo;
    }

    public boolean isIdle() {
        return isBuildup() && !isSpawning() && !getSyncPhysicalArea().hasDestination() && !isAbilityActive() && syncBoxItemToPick == null && targetContainer == null;
    }

    private boolean isAbilityActive() {
        return (syncWeapon != null && syncWeapon.isActive())
                || (syncFactory != null && syncFactory.isActive())
                || (syncBuilder != null && syncBuilder.isActive())
                || (syncHarvester != null && syncHarvester.isActive())
                || (syncItemContainer != null && syncItemContainer.isActive());
    }

    /**
     * Ticks this sync item
     *
     * @return true if more tick are needed to fullfil the job
     * @throws ItemDoesNotExistException if the target item does no exist any longer
     * @throws NoSuchItemTypeException   if the target item type does not exist
     */
    public boolean tick() throws ItemDoesNotExistException, NoSuchItemTypeException {
        if (isSpawning()) {
            spawnProgress += PlanetService.TICK_FACTOR / (getBaseItemType().getSpawnDurationMillis() / 1000.0);
            if (spawnProgress >= 1.0) {
                spawnProgress = 1.0;
                handleIfItemBecomesReady();
                gameLogicService.onSpawnSyncItemFinished(this);
            } else {
                return true;
            }
        }

        if (syncBoxItemToPick != null) {
            return pickSyncBoxItem();
        }

        if (targetContainer != null) {
            return putInContainer();
        }

        if (syncWeapon != null && syncWeapon.isActive()) {
            return syncWeapon.tick();
        }

        if (syncFactory != null && syncFactory.isActive()) {
            return syncFactory.tick();
        }

        if (syncBuilder != null && syncBuilder.isActive()) {
            return syncBuilder.tick();
        }

        if (syncHarvester != null && syncHarvester.isActive()) {
            return syncHarvester.tick();
        }

        if (syncItemContainer != null && syncItemContainer.isActive()) {
            return syncItemContainer.tick();
        }

        return getSyncPhysicalArea().hasDestination();
    }

    public void stop(boolean stopMovable) {
        if (stopMovable) {
            getSyncPhysicalArea().stop();
        }

        syncBoxItemToPick = null;
        targetContainer = null;

        if (syncWeapon != null) {
            syncWeapon.stop();
        }

        if (syncFactory != null) {
            syncFactory.stop();
        }

        if (syncBuilder != null) {
            syncBuilder.stop();
        }

        if (syncHarvester != null) {
            syncHarvester.stop();
        }

        if (syncItemContainer != null) {
            syncItemContainer.stop();
        }
    }

    public void executeCommand(BaseCommand baseCommand) throws ItemDoesNotExistException, InsufficientFundsException, NoSuchItemTypeException, ItemLimitExceededException, HouseSpaceExceededException, WrongOperationSurfaceException {
        checkId(baseCommand);

        if (!isBuildup()) {
            throw new java.lang.IllegalStateException("SyncBaseItem is not buildup: " + this);
        }

        if (baseCommand instanceof AttackCommand) {
            getSyncWeapon().executeCommand((AttackCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof MoveCommand) {
            ((SyncPhysicalMovable) getSyncPhysicalArea()).setPath(((MoveCommand) baseCommand).getSimplePath());
            return;
        }

        if (baseCommand instanceof HarvestCommand) {
            getSyncHarvester().executeCommand((HarvestCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof BuilderCommand) {
            getSyncBuilder().executeCommand((BuilderCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof BuilderFinalizeCommand) {
            getSyncBuilder().executeCommand((BuilderFinalizeCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof FactoryCommand) {
            getSyncFactory().executeCommand((FactoryCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof LoadContainerCommand) {
            executeLoadContainerCommand((LoadContainerCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof UnloadContainerCommand) {
            getSyncItemContainer().executeCommand((UnloadContainerCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof PickupBoxCommand) {
            PickupBoxCommand pickupBoxCommand = (PickupBoxCommand) baseCommand;
            getSyncPhysicalMovable().setPath(pickupBoxCommand.getSimplePath());
            syncBoxItemToPick = boxService.getSyncBoxItem(pickupBoxCommand.getSynBoxItemId());
            return;
        }

        throw new IllegalArgumentException("Command not supported: " + baseCommand);
    }

    private void checkId(BaseCommand baseCommand) {
        if (baseCommand.getId() != getId()) {
            throw new IllegalArgumentException(this + "Id do not match: " + getId() + " command: " + baseCommand.getId());
        }
    }

    private void executeLoadContainerCommand(LoadContainerCommand loadContainerCommand) {
        if (loadContainerCommand.getId() == (loadContainerCommand.getItemContainer())) {
            throw new IllegalArgumentException("Can not contain oneself: " + this);
        }
        targetContainer = syncItemContainerService.getSyncBaseItemSave(loadContainerCommand.getItemContainer());
        ((SyncPhysicalMovable) getSyncPhysicalArea()).setPath(loadContainerCommand.getSimplePath());
    }

    public SyncItem getTarget() {
        if (syncWeapon != null && syncWeapon.isActive()) {
            return syncWeapon.getTarget();
        }

        if (syncBuilder != null && syncBuilder.isActive()) {
            return syncBuilder.getCurrentBuildup();
        }

        if (syncHarvester != null && syncHarvester.isActive()) {
            return syncHarvester.getResource();
        }

        if (syncBoxItemToPick != null) {
            return syncBoxItemToPick;
        }

        if (targetContainer != null) {
            return targetContainer;
        }

        return null;
    }

    public SyncHarvester getSyncHarvester() {
        return syncHarvester;
    }

    public SyncFactory getSyncFactory() {
        return syncFactory;
    }

    public SyncWeapon getSyncWeapon() {
        return syncWeapon;
    }

    public SyncBuilder getSyncBuilder() {
        return syncBuilder;
    }

    public SyncGenerator getSyncGenerator() {
        return syncGenerator;
    }

    public SyncConsumer getSyncConsumer() {
        return syncConsumer;
    }

    public SyncItemContainer getSyncItemContainer() {
        return syncItemContainer;
    }

    public SyncHouse getSyncHouse() {
        return syncHouse;
    }

    public boolean isEnemy(SyncBaseItem syncBaseItem) {
        return baseItemService.isEnemy(this, syncBaseItem);
    }

    public boolean isEnemy(PlayerBase playerBase) {
        return getBase().isEnemy(playerBase);
    }

    private void decreaseHealth(double damage, SyncBaseItem actor) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            baseItemService.killSyncItem(this, actor);
        }
    }

    public void increaseHealth(double progress) {
        health += progress;
        gameLogicService.onHealthIncreased(this);
        if (health >= getBaseItemType().getHealth()) {
            health = getBaseItemType().getHealth();
        }
    }

    public void clearHealth() {
        health = 0;
    }

    public boolean isBuildup() {
        return buildup >= 1.0;
    }

    public double getBuildup() {
        return buildup;
    }

    public boolean isAlive() {
        return health > 0.0;
    }

    public boolean isHealthy() {
        return health >= getBaseItemType().getHealth();
    }

    public double getNormalizedHealth() {
        return Math.min(1.0, health / (double) getBaseItemType().getHealth());
    }

    public void addBuildup(double deltaBuildup) {
        double newBuildup = buildup + deltaBuildup;
        if (buildup < 1.0 && newBuildup >= 1.0) {
            setBuildup(newBuildup);
            handleIfItemBecomesReady();
        } else {
            setBuildup(newBuildup);
        }
    }

    public void setBuildup(double buildup) {
        if (buildup > 1.0) {
            this.buildup = 1.0;
        } else if (buildup < 0.0) {
            this.buildup = 0.0;
        } else {
            this.buildup = buildup;
        }
    }

    public void handleIfItemBecomesReady() {
        if (isSpawning() || !isBuildup()) {
            return;
        }
        if (syncConsumer != null) {
            syncConsumer.onReady();
        }
        if (syncGenerator != null) {
            syncGenerator.onReady();
        }
        gameLogicService.onBuildup(this);
    }

    public BaseItemType getBaseItemType() {
        return (BaseItemType) getItemType();
    }

    public double getHealth() {
        return health;
    }

    public void setContained(SyncBaseItem itemContainer) {
        this.containedIn = itemContainer;
        getSyncPhysicalArea().setPosition2d(null, false);
        if (getSyncPhysicalArea().canMove()) {
            getSyncPhysicalMovable().stop();
        }
    }

    public void clearContained(DecimalPosition position) {
        containedIn = null;
        getSyncPhysicalArea().setPosition2d(position, false);
    }

    public SyncBaseItem getContainedIn() {
        return containedIn;
    }

    public boolean isContainedIn() {
        return containedIn != null;
    }

    private boolean pickSyncBoxItem() {
        if (!syncBoxItemToPick.isAlive()) {
            stop(true);
            return false;
        }
        if (!getSyncPhysicalArea().isInRange(getBaseItemType().getBoxPickupRange(), syncBoxItemToPick)) {
            if (!getSyncPhysicalArea().canMove()) {
                throw new IllegalStateException("SyncBaseItem out of range from Box and getSyncPhysicalArea can not move");
            }
            if (!getSyncPhysicalMovable().hasDestination()) {
                throw new IllegalStateException("SyncBaseItem out of range from Box and SyncPhysicalMovable does not have a destination");
            }
            return true;
        }
        boxService.onSyncBoxItemPicked(syncBoxItemToPick, this);
        stop(true);
        return false;
    }


    private boolean putInContainer() {
        if (!targetContainer.isAlive()) {
            stop(true);
            return false;
        }
        if (getSyncPhysicalArea().isInRange(targetContainer.getSyncItemContainer().getRange(), targetContainer)) {
            targetContainer.getSyncItemContainer().load(this);
            stop(true);
            return false;
        } else {
            return true;
        }
    }

    public boolean isRazarionEarningOrConsuming() {
        return isRazarionEarningOrConsuming;
    }

    public double getDropBoxPossibility() {
        return getBaseItemType().getDropBoxPossibility();
    }

    public void onAttacked(double damage, SyncBaseItem actor) throws TargetHasNoPositionException {
        gameLogicService.onAttacked(this, actor, damage);
        decreaseHealth(damage, actor);
        if (baseItemService.getGameEngineMode() != GameEngineMode.MASTER) {
            return;
        }
        if (!isAlive()) {
            return;
        }
        if (!actor.isAlive()) {
            return;
        }
        if (syncWeapon == null) {
            return;
        }
        if (!isIdle()) {
            return;
        }
        if (!syncWeapon.isAttackAllowed(actor)) {
            return;
        }

        if (syncWeapon.isInRange(actor)) {
            commandService.defend(this, actor);
        }
    }

    public double getSpawnProgress() {
        return spawnProgress;
    }

    public void setSpawnProgress(double spawnProgress) {
        if (spawnProgress > 1.0) {
            this.spawnProgress = 1.0;
        } else if (spawnProgress < 0.0) {
            this.spawnProgress = 0.0;
        } else {
            this.spawnProgress = spawnProgress;
        }
    }

    public boolean isSpawning() {
        return spawnProgress < 1.0;
    }

    public NativeSyncBaseItemTickInfo createNativeSyncBaseItemTickInfo() {
        NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo = new NativeSyncBaseItemTickInfo();
        nativeSyncBaseItemTickInfo.id = getId();
        nativeSyncBaseItemTickInfo.itemTypeId = getBaseItemType().getId();
        nativeSyncBaseItemTickInfo.baseId = base.getBaseId();
        if (containedIn == null) {
            if (syncWeapon != null && syncWeapon.getSyncTurret() != null) {
                nativeSyncBaseItemTickInfo.turretAngle = syncWeapon.getSyncTurret().getAngle();
            }
            nativeSyncBaseItemTickInfo.x = getSyncPhysicalArea().getPosition().getX();
            nativeSyncBaseItemTickInfo.y = getSyncPhysicalArea().getPosition().getY();
            nativeSyncBaseItemTickInfo.angle = getSyncPhysicalArea().getAngle();
            if (syncHarvester != null && syncHarvester.isHarvesting()) {
                nativeSyncBaseItemTickInfo.harvestingResourcePosition = syncHarvester.getResource().getSyncPhysicalArea().getPosition();
            }
            if (syncBuilder != null && syncBuilder.isBuilding()) {
                nativeSyncBaseItemTickInfo.buildingPosition = syncBuilder.getCurrentBuildup().getSyncPhysicalArea().getPosition();
                nativeSyncBaseItemTickInfo.constructing = syncBuilder.getCurrentBuildup().getBuildup();
                nativeSyncBaseItemTickInfo.constructingBaseItemTypeId = syncBuilder.getCurrentBuildup().getBaseItemType().getId();
            }
            if (syncFactory != null && syncFactory.isActive()) {
                nativeSyncBaseItemTickInfo.constructing = syncFactory.getBuildup();
                if (syncFactory.getToBeBuiltType() != null) {
                    nativeSyncBaseItemTickInfo.constructingBaseItemTypeId = syncFactory.getToBeBuiltType().getId();
                }
            }
            nativeSyncBaseItemTickInfo.contained = false;
        } else {
            nativeSyncBaseItemTickInfo.contained = true;
        }
        if (syncItemContainer != null) {
            nativeSyncBaseItemTickInfo.containingItemCount = syncItemContainer.getContainedItems().size();
            nativeSyncBaseItemTickInfo.maxContainingRadius = syncItemContainer.getMaxContainingRadius();
        }
        nativeSyncBaseItemTickInfo.spawning = spawnProgress;
        nativeSyncBaseItemTickInfo.buildup = buildup;
        nativeSyncBaseItemTickInfo.health = getNormalizedHealth();
        return nativeSyncBaseItemTickInfo;
    }

    @Override
    public String toString() {
        return super.toString() + "|" + base;
    }
}
