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
    private FactoryType factoryType;
    private BaseItemType toBeBuiltType;
    private double buildup;
    private DecimalPosition spawnPoint;
    private DecimalPosition rallyPoint;

    @Inject
    public SyncFactory(CommandService commandService,
                       TerrainService terrainService,
                       ItemTypeService itemTypeService,
                       BaseItemService baseItemService,
                       GameLogicService gameLogicService) {
        this.commandService = commandService;
        this.terrainService = terrainService;
        this.itemTypeService = itemTypeService;
        this.baseItemService = baseItemService;
        this.gameLogicService = gameLogicService;
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
        }
        if (buildup >= 1.0) {
            if (baseItemService.getGameEngineMode() == GameEngineMode.MASTER) {
                if (baseItemService.isLevelLimitation4ItemTypeExceeded(toBeBuiltType, 1, (PlayerBaseFull) getSyncBaseItem().getBase())) {
                    gameLogicService.onFactoryLimitation4ItemTypeExceeded();
                    return true;
                }
                if (baseItemService.isHouseSpaceExceeded((PlayerBaseFull) getSyncBaseItem().getBase(), toBeBuiltType, 1)) {
                    gameLogicService.onFactoryHouseSpaceExceeded();
                    return true;
                }
                SyncBaseItem createItem = baseItemService.createSyncBaseItem4Factory(toBeBuiltType, spawnPoint, spawnPoint.getAngle(rallyPoint), (PlayerBaseFull) getSyncBaseItem().getBase(), getSyncBaseItem());
                stop();
                commandService.move(createItem, rallyPoint);
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
        buildup = syncBaseItemInfo.getFactoryBuildupProgress();
        spawnPoint = syncBaseItemInfo.getSpawnPoint();
        rallyPoint = syncBaseItemInfo.getRallyPoint();
    }

    @Override
    public void fillSyncItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        if (toBeBuiltType != null) {
            syncBaseItemInfo.setToBeBuiltTypeId(toBeBuiltType.getId());
        }
        syncBaseItemInfo.setFactoryBuildupProgress(buildup);
        syncBaseItemInfo.setSpawnPoint(spawnPoint);
        syncBaseItemInfo.setRallyPoint(rallyPoint);
    }

    public void stop() {
        buildup = 0;
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
        }
    }

    public double getBuildup() {
        return buildup;
    }

    public BaseItemType getToBeBuiltType() {
        return toBeBuiltType;
    }

    private void setupRallyPoint() {
        if (baseItemService.getGameEngineMode() == GameEngineMode.MASTER) {
            double maxToBeBuiltItemRadius = Double.MIN_VALUE;
            TerrainType toBeBuiltItemTerrainType = TerrainType.LAND;
            for (int ableToBuildId : factoryType.getAbleToBuildIds()) {
                BaseItemType toBeBuilt = itemTypeService.getBaseItemType(ableToBuildId);
                maxToBeBuiltItemRadius = Math.max(maxToBeBuiltItemRadius, toBeBuilt.getPhysicalAreaConfig().getRadius());
                toBeBuiltItemTerrainType = toBeBuilt.getPhysicalAreaConfig().getTerrainType();
            }
            double totalRadius = maxToBeBuiltItemRadius + getSyncBaseItem().getAbstractSyncPhysical().getRadius() + 2.0 * PathingService.STOP_DETECTION_NEIGHBOUR_DISTANCE;

            spawnPoint = findFreePosition(getSyncBaseItem().getAbstractSyncPhysical().getPosition(), maxToBeBuiltItemRadius, toBeBuiltItemTerrainType, totalRadius, false);

            rallyPoint = findFreePosition(spawnPoint, maxToBeBuiltItemRadius, toBeBuiltItemTerrainType, RELAY_POINT_DISTANCE, true);
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
