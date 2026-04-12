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
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.FactoryCommand;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.MathHelper;

import jakarta.inject.Inject;

/**
 * User: beat
 * Date: 21.11.2009
 * Time: 21:38:19
 */
public class SyncFactory extends SyncBaseAbility {
    private static final double GIVE_UP_RELAY_MIN = Math.toRadians(5);
    private static final double RELAY_POINT_DISTANCE = 2;
    // private Logger log = Logger.getLogger(SyncFactory.class.getName());
    private final GameLogicService gameLogicService;
    private final BaseItemService baseItemService;
    private final ItemTypeService itemTypeService;
    private final TerrainService terrainService;
    private final CommandService commandService;
    private final SyncService syncService;
    private FactoryType factoryType;
    private BaseItemType toBeBuiltType;
    private double buildup;
    /**
     * Master-only: sentinel -1 = not in warmup. >= 0 = ticks remaining before build progress
     * starts advancing. Gives the client time to play the intro animation (e.g. grid going down).
     */
    private int warmupTicksRemaining = -1;
    /**
     * True while the factory is in its warmup phase. Set on master from executeCommand and cleared
     * when warmupTicksRemaining reaches 0. Set on slave from synchronize() based on a negative
     * factoryBuildupProgress sentinel from master. Drives isInWarmup() so the slave reports the
     * warmup state back to the client renderer instead of independently exiting warmup after 1 tick.
     */
    private boolean inWarmup;
    /**
     * Master-only: sentinel -1 = not in cooldown. >= 0 = ticks remaining after buildup reaches 1.0
     * before the produced unit is actually created. Gives the client time to play the exit
     * animation (preview unit sliding out of the factory).
     */
    private int cooldownTicksRemaining = -1;
    private DecimalPosition rallyPoint;

    @Inject
    public SyncFactory(CommandService commandService,
                       TerrainService terrainService,
                       ItemTypeService itemTypeService,
                       BaseItemService baseItemService,
                       GameLogicService gameLogicService,
                       SyncService syncService) {
        this.commandService = commandService;
        this.terrainService = terrainService;
        this.itemTypeService = itemTypeService;
        this.baseItemService = baseItemService;
        this.gameLogicService = gameLogicService;
        this.syncService = syncService;
    }

    public void init(FactoryType factoryType, SyncBaseItem syncBaseItem) throws NoSuchItemTypeException {
        super.init(syncBaseItem);
        this.factoryType = factoryType;
        setupRallyPoint();
    }

    public FactoryType getFactoryType() {
        return factoryType;
    }

    public boolean isActive() {
        return toBeBuiltType != null;
    }

    public boolean tick() {
        // Warmup phase: wait for intro animation before advancing progress.
        // On master, warmupTicksRemaining counts down each tick. On slave, this branch never fires
        // (warmupTicksRemaining stays at -1) — slave's `inWarmup` is set by synchronize() and held
        // until master sends a non-warmup progress value.
        if (warmupTicksRemaining > 0) {
            warmupTicksRemaining--;
            gameLogicService.onSyncFactoryProgress(getSyncBaseItem());
            // Mark dirty so the master pushes the warmup state to slaves every tick. Without this
            // the SyncService dirty-list dedup means slaves only see the very first -1 and stay
            // stuck in warmup state forever (until something else marks the item dirty).
            syncService.notifySendSyncBaseItem(getSyncBaseItem());
            return true;
        }
        // Just exited warmup countdown. Clear flag and fall through so the FIRST post-warmup tick
        // already advances buildup — otherwise we'd send constructing=0 for one tick, which the
        // client interprets as "factory stopped" and resets the build animation.
        if (warmupTicksRemaining == 0) {
            warmupTicksRemaining = -1;
            inWarmup = false;
        }
        // Slave: stay passive while master reports warmup. Don't advance buildup independently.
        if (inWarmup) {
            return true;
        }

        if (buildup < 1.0) {
            double buildFactor = PlanetService.TICK_FACTOR * factoryType.getProgress() / (double) toBeBuiltType.getBuildup();
            if (buildFactor + buildup > 1.0) {
                buildFactor = 1.0 - buildup;
            }

            if (!getSyncBaseItem().getBase().withdrawalResource(buildFactor * (double) toBeBuiltType.getPrice())) {
                gameLogicService.onFactoryNoRazarion();
                return true;
            }
            buildup += buildFactor;
            gameLogicService.onSyncFactoryProgress(getSyncBaseItem());
            // Mark dirty so the buildup increments are pushed to slaves every tick (otherwise
            // slaves only see the initial sync and stay at buildup=0 visually).
            syncService.notifySendSyncBaseItem(getSyncBaseItem());
        }
        if (buildup >= 1.0) {
            // Cooldown phase: delay unit creation so the client can play the exit animation
            // (preview unit sliding out of the factory). Similar to SyncBuilder's cooldown.
            if (cooldownTicksRemaining < 0) {
                cooldownTicksRemaining = computeCooldownTicks();
            }
            if (cooldownTicksRemaining > 0) {
                cooldownTicksRemaining--;
                syncService.notifySendSyncBaseItem(getSyncBaseItem());
                return true;
            }
            // Cooldown done (or no cooldown configured) — create the unit
            if (baseItemService.getGameEngineMode() == GameEngineMode.MASTER) {
                if (baseItemService.isLevelLimitation4ItemTypeExceeded(toBeBuiltType, 1, (PlayerBaseFull) getSyncBaseItem().getBase())) {
                    gameLogicService.onFactoryLimitation4ItemTypeExceeded();
                    return true;
                }
                if (baseItemService.isHouseSpaceExceeded((PlayerBaseFull) getSyncBaseItem().getBase(), toBeBuiltType, 1)) {
                    gameLogicService.onFactoryHouseSpaceExceeded();
                    return true;
                }
                // Spawn the unit directly at the rally point — the client's outro animation already
                // slid the build preview from the factory center to this area.
                double angle = getSyncBaseItem().getAbstractSyncPhysical().getPosition().getAngle(rallyPoint);
                baseItemService.createSyncBaseItem4Factory(toBeBuiltType, rallyPoint, angle, (PlayerBaseFull) getSyncBaseItem().getBase(), getSyncBaseItem());
                stop();
            }
            return false;
        }
        return true;
    }

    @Override
    public void synchronize(SyncBaseItemInfo syncBaseItemInfo) throws NoSuchItemTypeException {
        if (syncBaseItemInfo.getToBeBuiltTypeId() != null) {
            toBeBuiltType = itemTypeService.getBaseItemType(syncBaseItemInfo.getToBeBuiltTypeId());
        } else {
            toBeBuiltType = null;
        }
        double progress = syncBaseItemInfo.getFactoryBuildupProgress();
        if (progress < 0) {
            // Slave: master is in warmup. Don't run a local tick counter — just hold the warmup
            // state until the master sends a non-negative progress value.
            inWarmup = true;
            buildup = 0;
        } else {
            inWarmup = false;
            buildup = progress;
        }
        rallyPoint = syncBaseItemInfo.getRallyPoint();
    }

    @Override
    public void fillSyncItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        if (toBeBuiltType != null) {
            syncBaseItemInfo.setToBeBuiltTypeId(toBeBuiltType.getId());
        }
        syncBaseItemInfo.setFactoryBuildupProgress(inWarmup ? -1 : buildup);
        syncBaseItemInfo.setRallyPoint(rallyPoint);
    }

    public boolean isInWarmup() {
        return inWarmup;
    }

    public void stop() {
        buildup = 0;
        inWarmup = false;
        warmupTicksRemaining = -1;
        cooldownTicksRemaining = -1;
        toBeBuiltType = null;
        gameLogicService.onSyncFactoryStopped(getSyncBaseItem());
    }

    public void executeCommand(FactoryCommand factoryCommand) throws InsufficientFundsException, NoSuchItemTypeException {
        if (!factoryType.isAbleToBuild(factoryCommand.getToBeBuiltId())) {
            throw new IllegalArgumentException(this + " can not fabricate: " + factoryCommand.getToBeBuiltId());
        }
        BaseItemType tmpToBeBuiltType = itemTypeService.getBaseItemType(factoryCommand.getToBeBuiltId());

        if (toBeBuiltType == null) {
            toBeBuiltType = tmpToBeBuiltType;
            warmupTicksRemaining = computeWarmupTicks();
            inWarmup = warmupTicksRemaining > 0;
        }
    }

    private int computeWarmupTicks() {
        return Math.max(0, (int) Math.round(factoryType.getAnimationIntroSeconds() * PlanetService.TICKS_PER_SECONDS));
    }

    private int computeCooldownTicks() {
        return Math.max(0, (int) Math.round(factoryType.getAnimationOutroSeconds() * PlanetService.TICKS_PER_SECONDS));
    }

    public double getBuildup() {
        return buildup;
    }

    public BaseItemType getToBeBuiltType() {
        return toBeBuiltType;
    }

    private void setupRallyPoint() {
        if (baseItemService.getGameEngineMode() == GameEngineMode.MASTER) {
            DecimalPosition factoryPos = getSyncBaseItem().getAbstractSyncPhysical().getPosition();
            if (factoryType.getRallyOffsetX() != 0 || factoryType.getRallyOffsetY() != 0) {
                rallyPoint = new DecimalPosition(
                        factoryPos.getX() + factoryType.getRallyOffsetX(),
                        factoryPos.getY() + factoryType.getRallyOffsetY());
            } else {
                // Auto-compute: find a free position around the factory
                double maxToBeBuiltItemRadius = Double.MIN_VALUE;
                TerrainType toBeBuiltItemTerrainType = TerrainType.LAND;
                for (int ableToBuildId : factoryType.getAbleToBuildIds()) {
                    BaseItemType toBeBuilt = itemTypeService.getBaseItemType(ableToBuildId);
                    maxToBeBuiltItemRadius = Math.max(maxToBeBuiltItemRadius, toBeBuilt.getPhysicalAreaConfig().getRadius());
                    toBeBuiltItemTerrainType = toBeBuilt.getPhysicalAreaConfig().getTerrainType();
                }
                double totalRadius = maxToBeBuiltItemRadius + getSyncBaseItem().getAbstractSyncPhysical().getRadius() + 2.0 * PathingService.STOP_DETECTION_NEIGHBOUR_DISTANCE;
                DecimalPosition freePos = findFreePosition(factoryPos, maxToBeBuiltItemRadius, toBeBuiltItemTerrainType, totalRadius, false);
                rallyPoint = findFreePosition(freePos, maxToBeBuiltItemRadius, toBeBuiltItemTerrainType, RELAY_POINT_DISTANCE, true);
            }
        }
    }

    private DecimalPosition findFreePosition(DecimalPosition start, double radius, TerrainType terrainType, double distance, boolean checkInsight) {
        double delta = MathHelper.QUARTER_RADIANT;
        while (delta > GIVE_UP_RELAY_MIN) {
            for (double angle = 0; angle <= MathHelper.ONE_RADIANT; angle += delta) {
                double correctedAngle = MathHelper.normaliseAngle(angle + MathHelper.THREE_QUARTER_RADIANT);
                DecimalPosition decimalPosition = start.getPointWithDistance(correctedAngle, distance);
                if (terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(terrainType, decimalPosition, radius)) {
                    if (checkInsight) {
                        if (terrainService.getTerrainAnalyzer().isInSight(start, radius, decimalPosition, terrainType)) {
                            return decimalPosition;
                        }
                    } else {
                        return decimalPosition;
                    }
                }
            }
            delta /= 2.0;
        }
        throw new IllegalStateException("SyncFactory.findFreePosition() can not find position: " + getSyncBaseItem());
    }
}
