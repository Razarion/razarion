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
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.command.AttackCommand;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderFinalizeCommand;
import com.btxtech.shared.gameengine.datatypes.command.FactoryCommand;
import com.btxtech.shared.gameengine.datatypes.command.LoadContainerCommand;
import com.btxtech.shared.gameengine.datatypes.command.MoneyCollectCommand;
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
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncItemInfo;
import com.btxtech.shared.gameengine.planet.ActivityService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BaseService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.PlanetService;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 04.12.2009
 * Time: 19:11:49
 */
@Dependent
public class SyncBaseItem extends SyncTickItem implements SyncBaseObject {
    @Inject
    private Instance<SyncBaseAbility> instance;
    @Inject
    private Instance<SyncPhysicalMovable> instanceMovable;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private BaseService baseService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private CommandService commandService;
    @Inject
    private ActivityService activityService;
    private PlayerBase base;
    private double buildup;
    private double health;
    private SyncWeapon syncWeapon;
    private SyncFactory syncFactory;
    private SyncBuilder syncBuilder;
    private SyncHarvester syncHarvester;
    private SyncGenerator syncGenerator;
    private SyncConsumer syncConsumer;
    private SyncSpecial syncSpecial;
    private SyncItemContainer syncItemContainer;
    private SyncHouse syncHouse;
    private Integer containedIn;
    private boolean isMoneyEarningOrConsuming = false;
    private PlayerBase killedBy;
    private ItemLifecycle itemLifecycle;
    private double spawnProgress;

    public void setup(PlayerBase base, ItemLifecycle itemLifecycle) throws NoSuchItemTypeException {
        this.base = base;
        this.itemLifecycle = itemLifecycle;

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
            if (PlanetService.MODE == PlanetMode.MASTER) {
                syncFactory.calculateRallyPoint();
            }
            isMoneyEarningOrConsuming = true;
        } else {
            syncFactory = null;
        }

        if (baseItemType.getBuilderType() != null) {
            syncBuilder = instance.select(SyncBuilder.class).get();
            syncBuilder.init(baseItemType.getBuilderType(), this);
            isMoneyEarningOrConsuming = true;
        } else {
            syncBuilder = null;
        }

        if (baseItemType.getHarvesterType() != null) {
            syncHarvester = instance.select(SyncHarvester.class).get();
            syncHarvester.init(baseItemType.getHarvesterType(), this);
            isMoneyEarningOrConsuming = true;
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

        if (baseItemType.getSpecialType() != null) {
            syncSpecial = instance.select(SyncSpecial.class).get();
            syncSpecial.init(baseItemType.getSpecialType(), this);
        } else {
            syncSpecial = null;
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

    public ItemLifecycle getItemLifecycle() {
        return itemLifecycle;
    }

    private void checkBase(PlayerBase syncBase) {
        if (base == null && syncBase == null) {
            return;
        }
        if (base == null) {
            throw new IllegalArgumentException(this + " this.base == null; sync base: " + syncBase);
        }

        if (!base.equals(syncBase)) {
            throw new IllegalArgumentException(this + " bases do not match: client: " + base + " sync: " + syncBase);
        }
    }

    public PlayerBase getBase() {
        return base;
    }

    @Override
    public void synchronize(SyncItemInfo syncItemInfo) throws ItemDoesNotExistException {
        checkBase(syncItemInfo.getBase());
        if (getItemType().getId() != syncItemInfo.getItemTypeId()) {
            setItemType(itemTypeService.getBaseItemType(syncItemInfo.getItemTypeId()));
            // TODO fireItemChanged(SyncItemListener.Change.ITEM_TYPE_CHANGED, null);
            // TODO  setup();
        }
        health = syncItemInfo.getHealth();
        setBuildup(syncItemInfo.getBuildup());
        containedIn = syncItemInfo.getContainedIn();
        killedBy = syncItemInfo.getKilledBy();

        // TODO if (syncMovable != null) {
        // TODO     syncMovable.synchronize(syncItemInfo);
        // TODO }

        if (syncWeapon != null) {
            syncWeapon.synchronize(syncItemInfo);
        }

        if (syncFactory != null) {
            syncFactory.synchronize(syncItemInfo);
        }

        if (syncBuilder != null) {
            syncBuilder.synchronize(syncItemInfo);
        }

        if (syncHarvester != null) {
            syncHarvester.synchronize(syncItemInfo);
        }

        if (syncConsumer != null) {
            syncConsumer.synchronize(syncItemInfo);
        }

        if (syncItemContainer != null) {
            syncItemContainer.synchronize(syncItemInfo);
        }

        super.synchronize(syncItemInfo);
    }

    @Override
    public SyncItemInfo getSyncInfo() {
        SyncItemInfo syncItemInfo = super.getSyncInfo();
        syncItemInfo.setBase(base);
        syncItemInfo.setHealth(health);
        syncItemInfo.setBuildup(buildup);
        syncItemInfo.setContainedIn(containedIn);
        syncItemInfo.setKilledBy(killedBy);

        // TODO if (syncMovable != null) {
        // TODO     syncMovable.fillSyncItemInfo(syncItemInfo);
        // TODO }

        if (syncWeapon != null) {
            syncWeapon.fillSyncItemInfo(syncItemInfo);
        }

        if (syncFactory != null) {
            syncFactory.fillSyncItemInfo(syncItemInfo);
        }

        if (syncBuilder != null) {
            syncBuilder.fillSyncItemInfo(syncItemInfo);
        }

        if (syncHarvester != null) {
            syncHarvester.fillSyncItemInfo(syncItemInfo);
        }

        if (syncConsumer != null) {
            syncConsumer.fillSyncItemInfo(syncItemInfo);
        }

        if (syncItemContainer != null) {
            syncItemContainer.fillSyncItemInfo(syncItemInfo);
        }

        return syncItemInfo;
    }

    public boolean isIdle() {
        return isReady()
                // TODO && !(syncMovable != null && syncMovable.isActive())
                && !(syncWeapon != null && syncWeapon.isActive())
                && !(syncFactory != null && syncFactory.isActive())
                && !(syncBuilder != null && syncBuilder.isActive())
                && !(syncHarvester != null && syncHarvester.isActive());
    }

    @Override
    public boolean tick() throws ItemDoesNotExistException, NoSuchItemTypeException {
        if (itemLifecycle == ItemLifecycle.SPAWN) {
            spawnProgress += PlanetService.TICK_FACTOR / (getBaseItemType().getSpawnDurationMillis() / 1000.0);
            if (spawnProgress >= 1.0) {
                spawnProgress = 1.0;
                itemLifecycle = ItemLifecycle.ALIVE;
                activityService.onSpawnSyncItemFinished(this);
            } else {
                return true;
            }
        }

        if (hasSyncConsumer() && !getSyncConsumer().isOperating()) {
            return false;
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

        return getSyncPhysicalArea().canMove() && ((SyncPhysicalMovable) getSyncPhysicalArea()).hasDestination();
    }

    public void stop() {
        getSyncPhysicalArea().stop();

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

        if (baseCommand instanceof AttackCommand) {
            getSyncWeapon().executeCommand((AttackCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof MoveCommand) {
            ((SyncPhysicalMovable)getSyncPhysicalArea()).setDestination(((MoveCommand)baseCommand).getPathToDestination().getDestination());
            return;
        }

        if (baseCommand instanceof MoneyCollectCommand) {
            getSyncHarvester().executeCommand((MoneyCollectCommand) baseCommand);
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
            throw new UnsupportedOperationException();
            //  getSyncMovable().executeCommand((LoadContainerCommand) baseCommand);
            // return;
        }

        if (baseCommand instanceof UnloadContainerCommand) {
            getSyncItemContainer().executeCommand((UnloadContainerCommand) baseCommand);
            return;
        }

        if (baseCommand instanceof PickupBoxCommand) {
            throw new UnsupportedOperationException();
            // getSyncMovable().executeCommand((PickupBoxCommand) baseCommand);
            // return;
        }

        throw new IllegalArgumentException("Command not supported: " + baseCommand);
    }

    private void checkId(BaseCommand baseCommand) {
        if (baseCommand.getId() != getId()) {
            throw new IllegalArgumentException(this + "Id do not match: " + getId() + " command: " + baseCommand.getId());
        }
    }

    public SyncPhysicalDirection getSyncMovable() {
        throw new UnsupportedOperationException();

        // if (syncMovable == null) {
        //     throw new IllegalStateException(this + " has no SyncMovable");
        // }
        // return syncMovable;
    }

    public boolean hasSyncHarvester() {
        return syncHarvester != null;
    }

    public SyncHarvester getSyncHarvester() {
        if (syncHarvester == null) {
            throw new IllegalStateException(this + " has no SyncHarvester");
        }
        return syncHarvester;
    }

    public SyncFactory getSyncFactory() {
        if (syncFactory == null) {
            throw new IllegalStateException(this + " has no SyncFactory");
        }
        return syncFactory;
    }

    public boolean hasSyncFactory() {
        return syncFactory != null;
    }

    public boolean hasSyncWeapon() {
        return syncWeapon != null;
    }

    public SyncWeapon getSyncWeapon() {
        if (syncWeapon == null) {
            throw new IllegalStateException(this + " has no syncWeapon");
        }
        return syncWeapon;
    }

    public boolean hasSyncBuilder() {
        return syncBuilder != null;
    }

    public SyncBuilder getSyncBuilder() {
        if (syncBuilder == null) {
            throw new IllegalStateException(this + " has no SyncBuilder");
        }
        return syncBuilder;
    }

    public boolean hasSyncGenerator() {
        return syncGenerator != null;
    }

    public SyncGenerator getSyncGenerator() {
        if (syncGenerator == null) {
            throw new IllegalStateException(this + " has no SyncGenerator");
        }
        return syncGenerator;
    }

    public boolean hasSyncConsumer() {
        return syncConsumer != null;
    }

    public SyncConsumer getSyncConsumer() {
        if (syncConsumer == null) {
            throw new IllegalStateException(this + " has no SyncConsumer");
        }
        return syncConsumer;
    }

    public boolean hasSyncSpecial() {
        return syncSpecial != null;
    }

    public SyncSpecial getSyncSpecial() {
        if (syncConsumer == null) {
            throw new IllegalStateException(this + " has no SyncSpecial");
        }
        return syncSpecial;
    }

    public boolean hasSyncItemContainer() {
        return syncItemContainer != null;
    }

    public SyncItemContainer getSyncItemContainer() {
        if (syncItemContainer == null) {
            throw new IllegalStateException(this + " has no SyncItemContainer");
        }
        return syncItemContainer;
    }

    public boolean hasSyncHouse() {
        return syncHouse != null;
    }

    public SyncHouse getSyncHouse() {
        if (syncHouse == null) {
            throw new IllegalStateException(this + " has no SyncHouse");
        }
        return syncHouse;
    }

    public boolean isEnemy(SyncBaseItem syncBaseItem) {
        return baseItemService.isEnemy(this, syncBaseItem);
    }

    public boolean isEnemy(PlayerBase playerBase) {
        return getBase().isEnemy(playerBase);
    }

    public void decreaseHealth(double progress, PlayerBase actor) {
        health -= progress;
        activityService.onHealthDecreased(this); // TODO call baseItemService here
        if (health <= 0) {
            health = 0;
            baseItemService.killSyncItem(this, actor, false, true); // TODO do not call baseItemService
        }
    }

    public void increaseHealth(double progress) {
        health += progress;
        activityService.onHealthIncreased(this);
        if (health >= getBaseItemType().getHealth()) {
            health = getBaseItemType().getHealth();
        }
    }

    public boolean isReady() {
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

    public void addBuildup(double buildup) {
        setBuildup(this.buildup + buildup);
    }

    public void setBuildup(double buildup) {
        if (buildup > 1.0) {
            buildup = 1.0;
        }
        if (this.buildup == buildup) {
            return;
        }
        this.buildup = buildup;
        if (syncConsumer != null) {
            syncConsumer.setConsuming(buildup >= 1.0);
        }
        if (syncGenerator != null) {
            syncGenerator.setGenerating(buildup >= 1.0);
        }
        activityService.onBuildup(this);
    }

    public BaseItemType getBaseItemType() {
        return (BaseItemType) getItemType();
    }

    public double getHealth() {
        return health;
    }

    public void setContained(int itemContainer) {
        this.containedIn = itemContainer;
        getSyncItemArea().setPosition(null);
    }

    public void clearContained(DecimalPosition position) {
        containedIn = null;
        getSyncItemArea().setPosition(position);
    }

    public Integer getContainedIn() {
        return containedIn;
    }

    public boolean isContainedIn() {
        return containedIn != null;
    }

    public PlayerBase getKilledBy() {
        return killedBy;
    }

    public void setKilledBy(PlayerBase killedBy) {
        this.killedBy = killedBy;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        if (hasSyncHarvester()) {
            builder.append(" target: ");
            builder.append(getSyncHarvester().getTarget());
        }
        if (containedIn != null) {
            builder.append(" containedIn: ");
            builder.append(containedIn);
        }
        builder.append(" ");
        builder.append(base);
        return builder.toString();
    }

    public boolean isMoneyEarningOrConsuming() {
        return isMoneyEarningOrConsuming;
    }

    public double getDropBoxPossibility() {
        return getBaseItemType().getDropBoxPossibility();
    }

    public void onAttacked(SyncBaseItem syncBaseItem) throws TargetHasNoPositionException {
        activityService.onAttacked(this);
        if (PlanetService.MODE != PlanetMode.MASTER) {
            return;
        }
        if (!isAlive()) {
            return;
        }
        if (!hasSyncWeapon()) {
            return;
        }
        if (!isIdle()) {
            return;
        }
        SyncWeapon syncWeapon = getSyncWeapon();
        if (!syncWeapon.isAttackAllowed(syncBaseItem)) {
            return;
        }

        if (syncWeapon.isInRange(syncBaseItem)) {
            commandService.defend(this, syncBaseItem);
        }
    }

    public double getSpawnProgress() {
        return spawnProgress;
    }

    public void setSpawnProgress(double spawnProgress) {
        this.spawnProgress = spawnProgress;
    }
}
