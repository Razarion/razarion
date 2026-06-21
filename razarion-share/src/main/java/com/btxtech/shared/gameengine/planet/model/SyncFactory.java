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
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.SyncService;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.MathHelper;

import jakarta.inject.Inject;
import java.util.Collection;

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
    private final SyncItemContainerServiceImpl syncItemContainerService;
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
    /**
     * Throttle counter for the per-tick progress sync. A building factory used to mark itself dirty
     * EVERY tick (warmup/build/cooldown), sending a full SyncBaseItemInfo 10x/s just to convey the
     * changing buildup float — the dominant gameConnection traffic when several factories build at
     * once. The slave runs this same tick() locally and advances buildup itself (the buildup/cooldown
     * branches below have no MASTER guard), so these syncs are only corrections and can be throttled
     * to every SYNC_THROTTLE_TICKS tick without making the build animation step.
     */
    private static final int SYNC_THROTTLE_TICKS = 5;
    private int sendThrottleCounter;

    @Inject
    public SyncFactory(CommandService commandService,
                       TerrainService terrainService,
                       ItemTypeService itemTypeService,
                       BaseItemService baseItemService,
                       GameLogicService gameLogicService,
                       SyncService syncService,
                       SyncItemContainerServiceImpl syncItemContainerService) {
        this.commandService = commandService;
        this.terrainService = terrainService;
        this.itemTypeService = itemTypeService;
        this.baseItemService = baseItemService;
        this.gameLogicService = gameLogicService;
        this.syncService = syncService;
        this.syncItemContainerService = syncItemContainerService;
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

    /**
     * Override the rally point computed by setupRallyPoint(). Called when the builder's
     * BuilderCommand carried an explicit rally point — the placer already validated it on
     * the client, so we trust it here. Master only.
     */
    public void setRallyPoint(DecimalPosition rallyPoint) {
        this.rallyPoint = rallyPoint;
    }

    public DecimalPosition getRallyPoint() {
        return rallyPoint;
    }

    /**
     * Send the factory's progress to the slaves, but at most every {@link #SYNC_THROTTLE_TICKS}
     * ticks — the slave interpolates locally between corrections. Pass {@code force=true} for events
     * the slave must not miss promptly (e.g. buildup reaching 1.0). No-op on slaves (notify guards).
     */
    private void throttledNotify(boolean force) {
        if (force || ++sendThrottleCounter >= SYNC_THROTTLE_TICKS) {
            sendThrottleCounter = 0;
            syncService.notifySendSyncBaseItem(getSyncBaseItem());
        }
    }

    public boolean tick() {
        // Warmup phase: wait for intro animation before advancing progress.
        // On master, warmupTicksRemaining counts down each tick. On slave, this branch never fires
        // (warmupTicksRemaining stays at -1) — slave's `inWarmup` is set by synchronize() and held
        // until master sends a non-warmup progress value.
        if (warmupTicksRemaining > 0) {
            warmupTicksRemaining--;
            gameLogicService.onSyncFactoryProgress(getSyncBaseItem());
            // No keep-alive during warmup: progress is the constant -1 sentinel, already delivered to
            // the slave by BaseItemService.executeCommand's sync. The slave holds its warmup state
            // until the first non-negative buildup arrives (forced on the warmup->build transition
            // below), so re-sending the unchanged -1 every few ticks is pure waste.
            return true;
        }
        // Just exited warmup countdown. Clear flag and fall through so the FIRST post-warmup tick
        // already advances buildup — otherwise we'd send constructing=0 for one tick, which the
        // client interprets as "factory stopped" and resets the build animation.
        if (warmupTicksRemaining == 0) {
            warmupTicksRemaining = -1;
            inWarmup = false;
            // Force the first build tick to sync so the slave promptly leaves its (held) warmup state
            // instead of waiting up to SYNC_THROTTLE_TICKS for the next throttled correction.
            sendThrottleCounter = SYNC_THROTTLE_TICKS;
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
            // Throttled correction — the slave advances buildup with the same formula each tick.
            // Force a send the moment buildup completes so the slave's cooldown starts in lock-step
            // with the master's (which decides when the produced unit actually spawns).
            throttledNotify(buildup >= 1.0);
        }
        if (buildup >= 1.0) {
            // Cooldown phase: delay unit creation so the client can play the exit animation
            // (preview unit sliding out of the factory). Similar to SyncBuilder's cooldown.
            if (cooldownTicksRemaining < 0) {
                cooldownTicksRemaining = computeCooldownTicks();
                // Kick the previous unit off the rally point now (start of cooldown), so it has
                // the entire outro animation window to walk away. By the time we actually spawn
                // the new unit at the end of cooldown, the rally point is typically already clear.
                if (baseItemService.getGameEngineMode() == GameEngineMode.MASTER) {
                    displaceUnitsAtRallyPoint();
                }
            }
            if (cooldownTicksRemaining > 0) {
                cooldownTicksRemaining--;
                // No keep-alive during cooldown: progress is the constant buildup=1.0 (already sent on
                // the forced build-complete tick), the client runs the exit animation on a timer
                // (outroSeconds), and the slave counts its own cooldown down deterministically. The
                // factory's final progress=0 is synced when the unit spawns (stop() + idle path).
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

    /**
     * If a previously-built unit is still parked on the rally point, send it a move command to a
     * free spot nearby so the just-finished unit can take its place. Without this, two units would
     * spawn overlapping each other and stay stuck because ORCA only resolves overlap during
     * movement — two stationary units never push apart.
     *
     * Buildings can't move and are ignored; units already on the move keep their existing path
     * (we don't overwrite — they'll clear the rally point on their own as they move out).
     */
    private void displaceUnitsAtRallyPoint() {
        double radius = toBeBuiltType.getPhysicalAreaConfig().getRadius();
        Collection<SyncBaseItem> overlapping = syncItemContainerService.findBaseItemsOverlapping(rallyPoint, radius, getSyncBaseItem());
        if (overlapping.isEmpty()) {
            return;
        }
        TerrainType terrainType = toBeBuiltType.getPhysicalAreaConfig().getTerrainType();
        for (SyncBaseItem occupant : overlapping) {
            if (!occupant.getAbstractSyncPhysical().canMove()) {
                continue;
            }
            // Only displace genuinely idle units — don't yank attackers, harvesters, or units already
            // moving somewhere. Active units will clear the rally point on their own (or ORCA resolves
            // any brief overlap once they move).
            if (!occupant.isIdle()) {
                continue;
            }
            DecimalPosition freeSpot = findDisplacementSpot(occupant, terrainType);
            if (freeSpot != null) {
                try {
                    commandService.move(occupant, freeSpot);
                } catch (Exception e) {
                    // Move command can fail if the path becomes unreachable mid-tick. Not fatal —
                    // worst case the new unit overlaps the occupant and ORCA sorts it out the next
                    // time either of them moves.
                }
            }
        }
    }

    /**
     * Search radial angles around the occupant for a position that's terrain-allowed for its
     * physical area, far enough from the rally point that it won't be re-displaced next tick.
     */
    private DecimalPosition findDisplacementSpot(SyncBaseItem occupant, TerrainType terrainType) {
        double occupantRadius = occupant.getAbstractSyncPhysical().getRadius();
        double newUnitRadius = toBeBuiltType.getPhysicalAreaConfig().getRadius();
        double distance = newUnitRadius + occupantRadius + 2.0 * PathingService.STOP_DETECTION_NEIGHBOUR_DISTANCE;
        DecimalPosition occupantPos = occupant.getAbstractSyncPhysical().getPosition();
        // Start by trying the direction occupant is already pushed to (away from rally), then sweep.
        // If occupant sits exactly on the rally point, fall back to +X.
        double baseAngle = rallyPoint.equals(occupantPos) ? 0 : rallyPoint.getAngle(occupantPos);
        double delta = MathHelper.QUARTER_RADIANT;
        while (delta > GIVE_UP_RELAY_MIN) {
            for (double angle = 0; angle <= MathHelper.ONE_RADIANT; angle += delta) {
                DecimalPosition candidate = rallyPoint.getPointWithDistance(MathHelper.normaliseAngle(baseAngle + angle), distance);
                if (terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(terrainType, candidate, occupantRadius)) {
                    return candidate;
                }
                if (angle > 0) {
                    candidate = rallyPoint.getPointWithDistance(MathHelper.normaliseAngle(baseAngle - angle), distance);
                    if (terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(terrainType, candidate, occupantRadius)) {
                        return candidate;
                    }
                }
            }
            delta /= 2.0;
        }
        return null;
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
