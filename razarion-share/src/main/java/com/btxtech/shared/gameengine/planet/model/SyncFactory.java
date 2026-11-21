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
import java.util.ArrayList;
import java.util.List;

/**
 * User: Razarion contributors
 * Date: 21.11.2009
 * Time: 21:38:19
 */
public class SyncFactory extends SyncBaseAbility {
    private static final double GIVE_UP_RELAY_MIN = Math.toRadians(5);
    /** How many rings around the rally point are searched for a free spot for a unit off the line. */
    private static final int EXIT_RINGS = 4;
    private static final double RELAY_POINT_DISTANCE = 2;
    /** Radius around a rally point candidate in which buildings are looked at: covers the largest
     * building radius (PROD 09/2026: 3.45) with room to spare. */
    private static final double RALLY_BUILDING_SCAN = 10;
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
    /**
     * Units queued behind the one currently building ({@link #toBeBuiltType}). FIFO — the head is
     * promoted to toBeBuiltType by {@link #startNext()} when the active unit finishes. Master-
     * authoritative; synced to slaves via SyncBaseItemInfo.factoryBuildQueue so every client can
     * display it. Empty when the factory builds a single unit (the pre-queue behaviour).
     */
    private final List<BaseItemType> buildQueue = new ArrayList<>();
    /**
     * Max units in flight per factory (active + waiting). Fixed for now; intended to become a
     * per-player unlockable limit (like unlock-item) later.
     */
    private static final int MAX_BUILD_QUEUE = 10;
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
     * Master-only: ticks the finished unit has waited for the spawn spot inside the factory. The
     * unit is created in the factory and drives out by itself; the next one is only created once the
     * previous one has left the spot it would be created on ("door free"). This replaces the old
     * fixed cooldown, in which a mere preview slid out and the real unit was set down on the rally
     * point - without shadow, sound or selection, and on top of whatever stood there.
     */
    private int spawnWaitTicks;
    /** After this long the unit is created even though the spawn spot is still taken. */
    private static final int MAX_SPAWN_WAIT_TICKS = 5 * PlanetService.TICKS_PER_SECONDS;
    /** How often a unit of the own base standing idle on the spawn spot is sent out again. */
    private static final int SPAWN_BLOCKER_NUDGE_TICKS = 2 * PlanetService.TICKS_PER_SECONDS;
    private DecimalPosition rallyPoint;
    /**
     * Throttle counter for the per-tick progress sync. A building factory used to mark itself dirty
     * EVERY tick (warmup/build), sending a full SyncBaseItemInfo 10x/s just to convey the
     * changing buildup float — the dominant gameConnection traffic when several factories build at
     * once. The slave runs this same tick() locally and advances buildup itself (the buildup
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
            // Force a send the moment buildup completes so the slave holds the finished state in
            // lock-step with the master (which decides when the produced unit actually spawns).
            throttledNotify(buildup >= 1.0);
        }
        if (buildup >= 1.0) {
            // Built - create the unit inside the factory, it drives out by itself.
            //
            // MASTER ONLY. The slave advances buildup locally for a smooth animation, but it
            // must NOT terminate the factory on its own local clock. The player who issued
            // the FactoryCommand runs that local simulation ~RTT ahead of the master (executed the
            // command locally without the round-trip). If the slave reached the old `return false`
            // here, BaseItemService.tick() would call stop() on it, flipping the factory to inactive
            // (constructing=0) — and the client would dispose the build preview ~RTT BEFORE the real
            // unit's sync arrives, leaving a visible gap where no unit is shown. (Other players don't
            // see it: their local sim is in lock-step with the master, so their local stop coincides
            // with the real unit's TickInfo.) Worse, the master's still-in-flight buildup corrections
            // then re-activate the factory and restart the whole build animation on the issuer.
            //
            // Instead the slave stays active at buildup=1.0 (the client holds its 'finished' state,
            // the solid preview standing in the factory) and lets the master's authoritative
            // stop()+spawn — bundled into a single TickInfo (see BaseItemService.createSyncBaseItem4Factory)
            // — drive termination. When that sync clears toBeBuiltType, isActive() goes false and the
            // ability ends in lock-step with the real unit appearing: an atomic preview->real handoff.
            if (baseItemService.getGameEngineMode() != GameEngineMode.MASTER) {
                return true;
            }
            DecimalPosition spawnPosition = findSpawnPosition();
            if (spawnPosition == null) {
                // The previous unit (or another of the own base) still stands where this one is
                // created. Hold at buildup=1.0 - the client keeps showing the finished preview.
                return true;
            }
            // Buildability (level limitation + house space) was already checked before this unit's
            // build started (see executeCommand / startNext), so it normally spawns here. Re-check in
            // case the limit filled up DURING the build (e.g. another factory produced the same type):
            // drop the finished unit rather than spawning over-limit or holding forever at buildup=1.0.
            if (canProduce(toBeBuiltType)) {
                // Created where the preview stood, facing the rally point, then sent out like any
                // unit - so it has shadow, sound and selection on the way out, and the pathing
                // makes it go around what stands in front of the factory.
                double angle = getSyncBaseItem().getAbstractSyncPhysical().getPosition().getAngle(rallyPoint);
                // Driving out is only the default: queued before quests, bots and auto harvest hear of
                // the unit, so an order of theirs (a new harvester sent to a crystal) comes after it
                // and wins. Queued after, it overwrote the harvest order and the harvester stood at
                // the rally point.
                boolean inFactory = spawnPosition.equals(getSyncBaseItem().getAbstractSyncPhysical().getPosition());
                baseItemService.createSyncBaseItem4Factory(toBeBuiltType, spawnPosition, angle, (PlayerBaseFull) getSyncBaseItem().getBase(), getSyncBaseItem(),
                        inFactory ? this::driveOut : null);
            }
            spawnWaitTicks = 0;
            // Advance the queue: startNext() promotes the next unit that can actually be produced
            // (dropping any whose limit is exceeded) and force-syncs the transition — the spawned unit
            // and the next-build state ride the same TickInfo (atomic preview->real handoff). If
            // nothing buildable remains, stop the factory.
            if (startNext()) {
                return true;
            }
            stop();
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
        buildQueue.clear();
        if (syncBaseItemInfo.getFactoryBuildQueue() != null) {
            for (Integer typeId : syncBaseItemInfo.getFactoryBuildQueue()) {
                buildQueue.add(itemTypeService.getBaseItemType(typeId));
            }
        }
    }

    @Override
    public void fillSyncItemInfo(SyncBaseItemInfo syncBaseItemInfo) {
        if (toBeBuiltType != null) {
            syncBaseItemInfo.setToBeBuiltTypeId(toBeBuiltType.getId());
        }
        syncBaseItemInfo.setFactoryBuildupProgress(inWarmup ? -1 : buildup);
        syncBaseItemInfo.setRallyPoint(rallyPoint);
        if (!buildQueue.isEmpty()) {
            List<Integer> queueIds = new ArrayList<>(buildQueue.size());
            for (BaseItemType queued : buildQueue) {
                queueIds.add(queued.getId());
            }
            syncBaseItemInfo.setFactoryBuildQueue(queueIds);
        }
    }

    public boolean isInWarmup() {
        return inWarmup;
    }

    public void stop() {
        buildup = 0;
        inWarmup = false;
        warmupTicksRemaining = -1;
        spawnWaitTicks = 0;
        toBeBuiltType = null;
        buildQueue.clear();
        gameLogicService.onSyncFactoryStopped(getSyncBaseItem());
    }

    public void executeCommand(FactoryCommand factoryCommand) throws InsufficientFundsException, NoSuchItemTypeException {
        if (!factoryType.isAbleToBuild(factoryCommand.getToBeBuiltId())) {
            throw new IllegalArgumentException(this + " can not fabricate: " + factoryCommand.getToBeBuiltId());
        }
        BaseItemType tmpToBeBuiltType = itemTypeService.getBaseItemType(factoryCommand.getToBeBuiltId());

        if (toBeBuiltType == null) {
            // Start the first unit only if it can actually be produced (limit/house space). Otherwise
            // drop it rather than building a unit that couldn't spawn. The client normally prevents
            // this by disabling the button at the limit.
            if (canProduce(tmpToBeBuiltType)) {
                toBeBuiltType = tmpToBeBuiltType;
                warmupTicksRemaining = computeWarmupTicks();
                inWarmup = warmupTicksRemaining > 0;
            }
        } else if (buildQueue.size() + 1 < MAX_BUILD_QUEUE) {
            // Factory busy: enqueue behind the active unit. The +1 accounts for the active
            // toBeBuiltType, so active + waiting never exceeds MAX_BUILD_QUEUE. A command that would
            // overflow the queue is silently ignored (the client also disables the button at the limit).
            buildQueue.add(tmpToBeBuiltType);
        }
    }

    /**
     * Promote the head of the build queue to the active build. Master only (called at spawn). Resets
     * build/warmup/spawn wait for the new unit and force-syncs so the slave leaves its held buildup=1.0
     * state and starts the next unit's animation in lock-step with the master.
     */
    private boolean startNext() {
        while (!buildQueue.isEmpty()) {
            BaseItemType next = buildQueue.remove(0);
            // Only start units that can actually be produced right now. Checking BEFORE the build
            // (instead of after it completes) means we never waste resources/time building a unit that
            // then can't spawn because its level limitation or house space is exceeded — such queued
            // units are dropped instead of stalling the factory forever at buildup=1.0.
            if (!canProduce(next)) {
                continue;
            }
            toBeBuiltType = next;
            buildup = 0;
            spawnWaitTicks = 0;
            warmupTicksRemaining = computeWarmupTicks();
            inWarmup = warmupTicksRemaining > 0;
            // Force the first post-transition build tick to sync promptly (mirrors the warmup-exit path).
            sendThrottleCounter = SYNC_THROTTLE_TICKS;
            syncService.notifySendSyncBaseItem(getSyncBaseItem());
            return true;
        }
        return false;
    }

    /**
     * Master-side check whether the given type can be produced right now: its level limitation and
     * house space must both allow one more. Fires the corresponding game-logic notification on
     * failure (so the player sees why). Called BEFORE a build starts, not after it completes.
     */
    private boolean canProduce(BaseItemType type) {
        PlayerBaseFull base = (PlayerBaseFull) getSyncBaseItem().getBase();
        if (baseItemService.isLevelLimitation4ItemTypeExceeded(type, 1, base)) {
            gameLogicService.onFactoryLimitation4ItemTypeExceeded();
            return false;
        }
        if (baseItemService.isHouseSpaceExceeded(base, type, 1)) {
            gameLogicService.onFactoryHouseSpaceExceeded();
            return false;
        }
        return true;
    }

    /**
     * Remove a waiting unit from the build queue (master only). {@code index} is into the waiting
     * queue only — the active {@link #toBeBuiltType} is not cancelable. Out-of-range indices are
     * ignored. Force-syncs so the change reaches all slaves promptly.
     */
    public void cancelQueueEntry(int index) {
        if (index < 0 || index >= buildQueue.size()) {
            return;
        }
        buildQueue.remove(index);
        syncService.notifySendSyncBaseItem(getSyncBaseItem());
    }

    public List<BaseItemType> getBuildQueue() {
        return buildQueue;
    }

    private int computeWarmupTicks() {
        return Math.max(0, (int) Math.round(factoryType.getAnimationIntroSeconds() * PlanetService.TICKS_PER_SECONDS));
    }

    public double getBuildup() {
        return buildup;
    }

    public BaseItemType getToBeBuiltType() {
        return toBeBuiltType;
    }

    /**
     * Where the finished unit is created, or null while it has to wait. Master only.
     *
     * <p>Normally the factory centre - where the client shows the preview, so the preview hands over
     * to the real unit without a jump. The spot must be free: the unit before may still be on its way
     * out. An own unit standing idle there is sent out (again); after {@link #MAX_SPAWN_WAIT_TICKS}
     * the unit is created anyway rather than stalling the factory for good.
     *
     * <p>A unit the terrain does not allow at the centre - a ship in a dockyard standing on the shore -
     * is created on a free spot at the rally point instead, as all units were before.
     */
    private DecimalPosition findSpawnPosition() {
        DecimalPosition center = getSyncBaseItem().getAbstractSyncPhysical().getPosition();
        double radius = toBeBuiltType.getPhysicalAreaConfig().getRadius();
        TerrainType terrainType = toBeBuiltType.getPhysicalAreaConfig().getTerrainType();
        if (!terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(terrainType, center, radius)) {
            keepRallyPointClear();
            DecimalPosition exit = findExitDestination(radius, terrainType, null);
            return exit != null ? exit : rallyPoint;
        }
        List<SyncBaseItem> blockers = new ArrayList<>();
        for (SyncBaseItem item : syncItemContainerService.findBaseItemsOverlapping(center, radius, getSyncBaseItem())) {
            if (item.getAbstractSyncPhysical().canMove()) {
                blockers.add(item);
            }
        }
        if (blockers.isEmpty() || spawnWaitTicks >= MAX_SPAWN_WAIT_TICKS) {
            keepRallyPointClear();
            return center;
        }
        if (spawnWaitTicks % SPAWN_BLOCKER_NUDGE_TICKS == 0) {
            for (SyncBaseItem blocker : blockers) {
                if (blocker.getBase() == getSyncBaseItem().getBase() && blocker.isIdle()) {
                    driveOut(blocker);
                }
            }
        }
        spawnWaitTicks++;
        return null;
    }

    /** Sends a unit standing in the factory to a free spot at the rally point. Master only. */
    private void driveOut(SyncBaseItem unit) {
        double radius = unit.getAbstractSyncPhysical().getRadius();
        DecimalPosition destination = findExitDestination(radius, unit.getAbstractSyncPhysical().getTerrainType(), unit);
        try {
            commandService.move(unit, destination != null ? destination : rallyPoint);
        } catch (Exception e) {
            // No way out found this tick: the unit stays in the factory, the next finished unit waits
            // for the spot and nudges it again (findSpawnPosition).
        }
    }

    /**
     * The rally point if it is free, else the first free spot on rings around it, starting on the
     * side away from the factory. Free means: terrain allowed, no item on it, and no unit of the own
     * base on its way there - so units off the line do not drive onto each other.
     *
     * @param unit the unit that will go there, ignored when looking at occupants; null for a unit not
     *             created yet
     * @return null if nothing free was found
     */
    private DecimalPosition findExitDestination(double radius, TerrainType terrainType, SyncBaseItem unit) {
        if (isFreeExitSpot(rallyPoint, radius, terrainType, unit)) {
            return rallyPoint;
        }
        DecimalPosition factoryPos = getSyncBaseItem().getAbstractSyncPhysical().getPosition();
        double baseAngle = factoryPos.equals(rallyPoint) ? 0 : factoryPos.getAngle(rallyPoint);
        double distance = 2.0 * radius + 2.0 * PathingService.STOP_DETECTION_NEIGHBOUR_DISTANCE;
        for (int ring = 0; ring < EXIT_RINGS; ring++) {
            double ringDistance = distance + ring * (2.0 * radius + PathingService.STOP_DETECTION_NEIGHBOUR_DISTANCE);
            double delta = MathHelper.QUARTER_RADIANT;
            while (delta > GIVE_UP_RELAY_MIN) {
                for (double angle = 0; angle <= MathHelper.HALF_RADIANT; angle += delta) {
                    DecimalPosition candidate = rallyPoint.getPointWithDistance(MathHelper.normaliseAngle(baseAngle + angle), ringDistance);
                    if (isFreeExitSpot(candidate, radius, terrainType, unit)) {
                        return candidate;
                    }
                    if (angle > 0) {
                        candidate = rallyPoint.getPointWithDistance(MathHelper.normaliseAngle(baseAngle - angle), ringDistance);
                        if (isFreeExitSpot(candidate, radius, terrainType, unit)) {
                            return candidate;
                        }
                    }
                }
                delta /= 2.0;
            }
        }
        return null;
    }

    private boolean isFreeExitSpot(DecimalPosition candidate, double radius, TerrainType terrainType, SyncBaseItem unit) {
        if (!terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(terrainType, candidate, radius)) {
            return false;
        }
        if (!syncItemContainerService.findBaseItemsOverlapping(candidate, radius, unit).isEmpty()) {
            return false;
        }
        // A unit sent out a moment ago is still on its way: its target counts as taken.
        for (SyncBaseItem other : ((PlayerBaseFull) getSyncBaseItem().getBase()).getItems()) {
            if (other == unit || !other.getAbstractSyncPhysical().canMove()) {
                continue;
            }
            DecimalPosition destination = other.getSyncPhysicalMovable().getFinalDestination();
            if (destination != null
                    && destination.getDistance(candidate) < radius + other.getAbstractSyncPhysical().getRadius()) {
                return false;
            }
        }
        return true;
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

    /**
     * First spot of the sweep that the terrain allows and no building covers. A factory whose every
     * spot is covered by buildings - a bot base laid out densely - still gets the first spot the
     * terrain allows, as before buildings were looked at: a factory without a rally point cannot be
     * created at all.
     */
    private DecimalPosition findFreePosition(DecimalPosition start, double radius, TerrainType terrainType, double distance, boolean checkInsight) {
        DecimalPosition terrainOnly = null;
        double delta = MathHelper.QUARTER_RADIANT;
        while (delta > GIVE_UP_RELAY_MIN) {
            for (double angle = 0; angle <= MathHelper.ONE_RADIANT; angle += delta) {
                double correctedAngle = MathHelper.normaliseAngle(angle + MathHelper.THREE_QUARTER_RADIANT);
                DecimalPosition decimalPosition = start.getPointWithDistance(correctedAngle, distance);
                if (terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(terrainType, decimalPosition, radius)
                        && (!checkInsight || terrainService.getTerrainAnalyzer().isInSight(start, radius, decimalPosition, terrainType))) {
                    if (isClearOfBuildings(decimalPosition, radius)) {
                        return decimalPosition;
                    }
                    if (terrainOnly == null) {
                        terrainOnly = decimalPosition;
                    }
                }
            }
            delta /= 2.0;
        }
        if (terrainOnly != null) {
            return terrainOnly;
        }
        throw new IllegalStateException("SyncFactory.findFreePosition() can not find position: " + getSyncBaseItem());
    }

    /**
     * The rally point is chosen when the factory is placed, which is usually before the rest of the
     * base exists: the player's placer puts it right beside the factory, checked against the terrain
     * only. A powerplant or radar built later can stand on it, or leave it in a gap narrower than the
     * unit, and then every unit of this factory spawns wedged between two buildings - the Harvester
     * and Viper share of the PROD "[PathingStuck] gave up" at the own base. Checked when the unit is
     * about to appear, and moved if a building took the spot. Master only.
     */
    private void keepRallyPointClear() {
        double unitRadius = toBeBuiltType.getPhysicalAreaConfig().getRadius();
        if (rallyPoint != null && isClearOfBuildings(rallyPoint, unitRadius)) {
            return;
        }
        DecimalPosition clear = findClearRallyPoint(unitRadius, toBeBuiltType.getPhysicalAreaConfig().getTerrainType());
        if (clear != null) {
            rallyPoint = clear;
            syncService.notifySendSyncBaseItem(getSyncBaseItem());
        }
    }

    /**
     * A spot around the factory, as far from the other buildings as the sweep finds - the direction
     * that leads out of the base rather than into the next gap. Without other buildings around, every
     * candidate ties and the first one of the sweep wins, like in {@link #findFreePosition}.
     */
    private DecimalPosition findClearRallyPoint(double unitRadius, TerrainType terrainType) {
        DecimalPosition factoryPos = getSyncBaseItem().getAbstractSyncPhysical().getPosition();
        double distance = getSyncBaseItem().getAbstractSyncPhysical().getRadius() + unitRadius
                + 2.0 * PathingService.STOP_DETECTION_NEIGHBOUR_DISTANCE + RELAY_POINT_DISTANCE;
        double delta = MathHelper.QUARTER_RADIANT;
        while (delta > GIVE_UP_RELAY_MIN) {
            DecimalPosition best = null;
            double bestGap = -1;
            for (double angle = 0; angle < MathHelper.ONE_RADIANT; angle += delta) {
                DecimalPosition candidate = factoryPos.getPointWithDistance(MathHelper.normaliseAngle(angle + MathHelper.THREE_QUARTER_RADIANT), distance);
                if (!terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(terrainType, candidate, unitRadius)
                        || !isClearOfBuildings(candidate, unitRadius)) {
                    continue;
                }
                double gap = gapToOtherBuildings(candidate, unitRadius);
                if (gap > bestGap) {
                    best = candidate;
                    bestGap = gap;
                }
            }
            if (best != null) {
                return best;
            }
            delta /= 2.0;
        }
        return null;
    }

    /** Whether a unit of this radius fits at the position without touching a building. */
    private boolean isClearOfBuildings(DecimalPosition position, double unitRadius) {
        boolean[] clear = {true};
        syncItemContainerService.iterateCellQuadBaseItem(position, 2.0 * (unitRadius + RALLY_BUILDING_SCAN), other -> {
            AbstractSyncPhysical physical = other.getAbstractSyncPhysical();
            if (!clear[0] || physical.canMove() || !physical.hasPosition()) {
                return;
            }
            if (physical.getPosition().getDistance(position) < physical.getRadius() + unitRadius + PathingService.STOP_DETECTION_NEIGHBOUR_DISTANCE) {
                clear[0] = false;
            }
        });
        return clear[0];
    }

    /** Free room between a unit at the position and the nearest building other than this factory,
     * {@link #RALLY_BUILDING_SCAN} if there is none that close. */
    private double gapToOtherBuildings(DecimalPosition position, double unitRadius) {
        double[] gap = {RALLY_BUILDING_SCAN};
        syncItemContainerService.iterateCellQuadBaseItem(position, 2.0 * (unitRadius + RALLY_BUILDING_SCAN), other -> {
            AbstractSyncPhysical physical = other.getAbstractSyncPhysical();
            if (other == getSyncBaseItem() || physical.canMove() || !physical.hasPosition()) {
                return;
            }
            gap[0] = Math.min(gap[0], physical.getPosition().getDistance(position) - physical.getRadius() - unitRadius);
        });
        return gap[0];
    }
}
