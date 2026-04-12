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
import com.btxtech.shared.gameengine.datatypes.command.BuilderCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderFinalizeCommand;
import com.btxtech.shared.gameengine.datatypes.exception.HouseSpaceExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemLimitExceededException;
import com.btxtech.shared.gameengine.datatypes.exception.NoSuchItemTypeException;
import com.btxtech.shared.gameengine.datatypes.exception.PositionTakenException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.pathing.TerrainDestinationFinderUtil;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import jakarta.inject.Inject;

/**
 * User: beat
 * Date: 21.11.2009
 * Time: 23:53:39
 */

public class SyncBuilder extends SyncBaseAbility {
    private final ItemTypeService itemTypeService;
    private final GameLogicService gameLogicService;
    private final BaseItemService baseItemService;
    private final TerrainService terrainService;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final SyncService syncService;
    private BuilderType builderType;
    private SyncBaseItem currentBuildup;
    private DecimalPosition toBeBuildPosition;
    private BaseItemType toBeBuiltType;
    private boolean building;
    /**
     * Sentinel -1 = not in warmup. >= 0 = ticks remaining before the target item is created and
     * the first addBuildup runs. Set on the first in-range tick after the builder finished turning,
     * decremented each subsequent tick. Drives the client-side build intro animation window.
     */
    private int warmupTicksRemaining = -1;
    /**
     * Sentinel -1 = not in cooldown. >= 0 = ticks remaining after the target reached buildup 1.0
     * before the build job is released via stop(). Drives the client-side build outro animation window.
     */
    private int cooldownTicksRemaining = -1;

    @Inject
    public SyncBuilder(SyncService syncService,
                       SyncItemContainerServiceImpl syncItemContainerService,
                       TerrainService terrainService,
                       BaseItemService baseItemService,
                       GameLogicService gameLogicService,
                       ItemTypeService itemTypeService) {
        this.syncService = syncService;
        this.syncItemContainerService = syncItemContainerService;
        this.terrainService = terrainService;
        this.baseItemService = baseItemService;
        this.gameLogicService = gameLogicService;
        this.itemTypeService = itemTypeService;
    }

    public void init(BuilderType builderType, SyncBaseItem syncBaseItem) {
        super.init(syncBaseItem);
        this.builderType = builderType;
    }

    public boolean isActive() {
        return (toBeBuildPosition != null && toBeBuiltType != null) || currentBuildup != null;
    }

    public synchronized boolean tick() {
        if (!isInRange()) {
            building = false;
            warmupTicksRemaining = -1;
            cooldownTicksRemaining = -1;
            if (!getAbstractSyncPhysical().canMove()) {
                throw new IllegalStateException("SyncBuilder out of range from build position and getSyncPhysicalArea can not move: " + getSyncBaseItem());
            }
            if (!getSyncPhysicalMovable().hasDestination()) {
                throw new IllegalStateException("SyncBuilder out of range from build position and SyncPhysicalMovable does not have a destination: " + getSyncBaseItem());
            }
            return true;
        }

        if (getSyncPhysicalMovable().hasDestination()) {
            getSyncPhysicalMovable().stop();
        }

        if (getAbstractSyncPhysical().canMove()) {
            double angle;
            if (currentBuildup != null) {
                angle = getSyncBaseItem().getSyncPhysicalMovable().getPosition().getAngle(currentBuildup.getAbstractSyncPhysical().getPosition());
            } else {
                angle = getSyncBaseItem().getSyncPhysicalMovable().getPosition().getAngle(toBeBuildPosition);
            }
            if (getSyncBaseItem().getSyncPhysicalMovable().turnTo(angle)) {
                return true;
            }
        }

        if (currentBuildup == null) {
            if (toBeBuiltType == null || toBeBuildPosition == null) {
                throw new IllegalArgumentException("Invalid attributes |" + toBeBuiltType + "|" + toBeBuildPosition);
            }
            if (baseItemService.getGameEngineMode() == GameEngineMode.MASTER) {
                if (!syncItemContainerService.isFree(toBeBuildPosition, toBeBuiltType)) {
                    stop();
                    return false;
                }
                // Warmup phase: hold the builder in "building" state for N ticks before creating
                // the target item, so the client gets time to play the build intro animation.
                if (warmupTicksRemaining < 0) {
                    warmupTicksRemaining = computeWarmupTicks();
                    if (warmupTicksRemaining > 0) {
                        building = true;
                        // Notify slaves about the new "building" state so they also play the intro
                        syncService.notifySendSyncBaseItem(getSyncBaseItem());
                    }
                }
                if (warmupTicksRemaining > 0) {
                    building = true;
                    warmupTicksRemaining--;
                    return true;
                }
                // warmupTicksRemaining is now 0 — warmup complete. Leave at 0 (not -1) so the
                // else branch sees "already warmed up" and does not repeat.
                try {
                    currentBuildup = baseItemService.createSyncBaseItem4Builder(toBeBuiltType, toBeBuildPosition, (PlayerBaseFull) getSyncBaseItem().getBase(), getSyncBaseItem());
                    syncService.notifySendSyncBaseItem(currentBuildup, true);
                    syncService.notifySendSyncBaseItem(getSyncBaseItem());
                    gameLogicService.onStartBuildingSyncBaseItem(getSyncBaseItem(), currentBuildup);
                    toBeBuildPosition = null;
                    toBeBuiltType = null;
                    return true;
                } catch (ItemLimitExceededException e) {
                    stop();
                    gameLogicService.onItemLimitExceededExceptionBuilder(getSyncBaseItem());
                    return false;
                } catch (HouseSpaceExceededException e) {
                    stop();
                    gameLogicService.onHouseSpaceExceededExceptionBuilder(getSyncBaseItem());
                    return false;
                }
            } else {
                return true;
            }
        } else {
            if (!currentBuildup.isAlive()) {
                stop();
                return false;
            }
            if (currentBuildup.isBuildup()) {
                // Already at buildup 1.0 (e.g. completed by another builder, or we just finished)
                // — run the cooldown phase before releasing the build job.
                return tickCooldown();
            }

            // Warmup phase for continuing an existing partially-built target
            // (BuilderFinalizeCommand). warmupTicksRemaining == 0 means the null branch above
            // already did the warmup for a fresh build — skip. warmupTicksRemaining == -1 means
            // this is the first time we enter the else branch for this build job.
            // Runs on BOTH master and slave so the slave's local simulation does not advance
            // buildup while the intro animation plays.
            if (warmupTicksRemaining < 0) {
                warmupTicksRemaining = computeWarmupTicks();
                if (warmupTicksRemaining > 0) {
                    building = true;
                    syncService.notifySendSyncBaseItem(getSyncBaseItem());
                }
            }
            if (warmupTicksRemaining > 0) {
                building = true;
                warmupTicksRemaining--;
                return true;
            }

            building = true;
            double buildFactor = setupBuildFactor();
            if (getSyncBaseItem().getBase().withdrawalResource(buildFactor * (double) currentBuildup.getBaseItemType().getPrice())) {
                currentBuildup.addBuildup(buildFactor);
                if (currentBuildup.isBuildup()) {
                    syncService.notifySendSyncBaseItem(currentBuildup);
                    return tickCooldown();
                }
                return true;
            } else {
                building = false;
                gameLogicService.onBuilderNoRazarion(getSyncBaseItem());
                return true;
            }
        }
    }

    /**
     * Cooldown phase: keep the builder in "building" state for N ticks after the target reached
     * buildup 1.0, so the client has time to play the build outro animation. Returns true while
     * the cooldown is still ticking, false (and calls stop()) once it's done.
     */
    private boolean tickCooldown() {
        if (cooldownTicksRemaining < 0) {
            cooldownTicksRemaining = computeCooldownTicks();
            if (cooldownTicksRemaining > 0) {
                building = true;
                // Notify slaves so they keep showing the build phase during the cooldown window
                syncService.notifySendSyncBaseItem(getSyncBaseItem());
            }
        }
        if (cooldownTicksRemaining > 0) {
            building = true;
            cooldownTicksRemaining--;
            return true;
        }
        cooldownTicksRemaining = -1;
        stop();
        return false;
    }

    private int computeWarmupTicks() {
        return Math.max(0, (int) Math.round(builderType.getBuildAnimationWarmupSeconds() * PlanetService.TICKS_PER_SECONDS));
    }

    private int computeCooldownTicks() {
        return Math.max(0, (int) Math.round(builderType.getBuildAnimationCooldownSeconds() * PlanetService.TICKS_PER_SECONDS));
    }

    /**
     * The position the build animation/beam should aim at. During warmup the target item does not
     * exist yet, so we fall back to the requested build position.
     */
    public DecimalPosition getEffectiveBuildingPosition() {
        if (currentBuildup != null) {
            return currentBuildup.getAbstractSyncPhysical().getPosition();
        }
        return toBeBuildPosition;
    }

    private double setupBuildFactor() {
        double buildFactor = PlanetService.TICK_FACTOR * builderType.getProgress() / (double) currentBuildup.getBaseItemType().getBuildup();
        if (buildFactor + currentBuildup.getBuildup() > 1.0) {
            buildFactor = 1.0 - currentBuildup.getBuildup();
        }
        return buildFactor;
    }

    private boolean isInRange() {
        if (toBeBuiltType == null && currentBuildup == null) {
            throw new IllegalStateException();
        }

        double range;
        TerrainType toBeBuildTerrainType = toBeBuiltType != null ?
                toBeBuiltType.getPhysicalAreaConfig().getTerrainType() : currentBuildup.getBaseItemType().getPhysicalAreaConfig().getTerrainType();
        if (TerrainDestinationFinderUtil.differentTerrain(getSyncBaseItem().getAbstractSyncPhysical().getTerrainType(), toBeBuildTerrainType)) {
            range = builderType.getRangeOtherTerrain();
        } else {
            range = builderType.getRange();
        }

        if (currentBuildup != null) {
            return getSyncBaseItem().getAbstractSyncPhysical().isInRange(range, currentBuildup);
        } else {
            return getSyncBaseItem().getAbstractSyncPhysical().isInRange(range, toBeBuildPosition, toBeBuiltType);
        }
    }

    public synchronized void stop() {
        boolean propagationNeeded = isActive();
        SyncBaseItem tmpCurrentBuildup = currentBuildup;
        currentBuildup = null;
        toBeBuiltType = null;
        toBeBuildPosition = null;
        building = false;
        warmupTicksRemaining = -1;
        cooldownTicksRemaining = -1;
        if (propagationNeeded) {
            gameLogicService.onSynBuilderStopped(getSyncBaseItem(), tmpCurrentBuildup);
        }
    }

    @Override
    public void synchronize(SyncBaseItemInfo syncBaseItemInfo) throws ItemDoesNotExistException {
        toBeBuildPosition = syncBaseItemInfo.getToBeBuildPosition();
        if (syncBaseItemInfo.getToBeBuiltTypeId() != null) {
            toBeBuiltType = itemTypeService.getBaseItemType(syncBaseItemInfo.getToBeBuiltTypeId());
        } else {
            toBeBuiltType = null;
        }
        Integer currentBuildupId = syncBaseItemInfo.getCurrentBuildup();
        boolean hadNoBuildup = (currentBuildup == null);
        if (currentBuildupId != null) {
            currentBuildup = syncItemContainerService.getSyncBaseItemSave(currentBuildupId);
        } else {
            currentBuildup = null;
        }
        // When the master just created the target item (null -> non-null), mark warmup as done
        // so the else branch does not trigger a redundant second warmup on the slave.
        // For continue-build (BuilderFinalizeCommand), currentBuildup was already non-null from
        // the start, so hadNoBuildup is false and warmupTicksRemaining stays at -1, enabling
        // the else-branch warmup correctly.
        if (hadNoBuildup && currentBuildup != null) {
            warmupTicksRemaining = 0;
        }
        // Mirror the master's building flag explicitly so the slave correctly sees the warmup
        // window (where currentBuildup is still null but the build animation should already play).
        Boolean syncedBuilding = syncBaseItemInfo.getBuilderBuilding();
        building = syncedBuilding != null && syncedBuilding;
    }

    @Override
    public void fillSyncItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        syncBaseItemInfo.setToBeBuildPosition(toBeBuildPosition);
        if (toBeBuiltType != null) {
            syncBaseItemInfo.setToBeBuiltTypeId(toBeBuiltType.getId());
        }
        if (currentBuildup != null) {
            syncBaseItemInfo.setCurrentBuildup(currentBuildup.getId());
        }
        syncBaseItemInfo.setBuilderBuilding(building);
    }

    public void executeCommand(BuilderCommand builderCommand) {
        if (!builderType.checkAbleToBuild(builderCommand.getToBeBuiltId())) {
            throw new IllegalArgumentException(this + " can not build: " + builderCommand.getToBeBuiltId());
        }
        BaseItemType tmpToBeBuiltType = itemTypeService.getBaseItemType(builderCommand.getToBeBuiltId());
        if (!terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(tmpToBeBuiltType.getPhysicalAreaConfig().getTerrainType(), builderCommand.getPositionToBeBuilt(), tmpToBeBuiltType.getPhysicalAreaConfig().getRadius())) {
            throw new PositionTakenException(builderCommand.getPositionToBeBuilt(), builderCommand.getToBeBuiltId());
        }

        toBeBuiltType = tmpToBeBuiltType;
        toBeBuildPosition = builderCommand.getPositionToBeBuilt();
        getSyncPhysicalMovable().setPath(builderCommand.getSimplePath());
    }

    public synchronized void executeCommand(BuilderFinalizeCommand builderFinalizeCommand) throws NoSuchItemTypeException, ItemDoesNotExistException {
        SyncBaseItem syncBaseItem = syncItemContainerService.getSyncBaseItemSave(builderFinalizeCommand.getBuildingId());
        if (!builderType.checkAbleToBuild(syncBaseItem.getItemType().getId())) {
            throw new IllegalArgumentException(this + " can not build: " + builderFinalizeCommand.getBuildingId());
        }

        currentBuildup = syncBaseItem;
        toBeBuiltType = syncBaseItem.getBaseItemType();
        getSyncPhysicalMovable().setPath(builderFinalizeCommand.getSimplePath());
    }

    public BuilderType getBuilderType() {
        return builderType;
    }

    public boolean isBuilding() {
        return building;
    }

    public SyncBaseItem getCurrentBuildup() {
        return currentBuildup;
    }
}
