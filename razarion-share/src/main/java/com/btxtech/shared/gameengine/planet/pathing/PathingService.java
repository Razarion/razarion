package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.SynchronizationSendingContext;
import com.btxtech.shared.gameengine.planet.model.AbstractSyncPhysical;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.NODE_SIZE;

@Singleton
public class PathingService {
    public static final double STOP_DETECTION_NEIGHBOUR_DISTANCE = 0.1;
    public static final double RADIUS_GROW = 1;
    /** How far around a given-up unit the diagnostic looks for the building it is wedged against.
     * Generous next to the radii that matter (a Factory is 2.55) and still a bounded cell query. */
    private static final double DIAGNOSTIC_BUILDING_SCAN = 20;
    private final Logger logger = Logger.getLogger(PathingService.class.getName());
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final TerrainService terrainService;
    private final PassabilityGrid passabilityGrid;
    private final PathingServiceTracker pathingServiceTracker = new PathingServiceTracker();

    @Inject
    public PathingService(TerrainService terrainService, SyncItemContainerServiceImpl syncItemContainerService, PassabilityGrid passabilityGrid) {
        this.terrainService = terrainService;
        this.syncItemContainerService = syncItemContainerService;
        this.passabilityGrid = passabilityGrid;
    }

    public SimplePath setupPathToDestination(SyncBaseItem syncItem, DecimalPosition destination) {
        return setupPathToDestination(syncItem, syncItem.getAbstractSyncPhysical().getTerrainType(), destination, 0);
    }

    public SimplePath setupPathToDestination(SyncBaseItem syncBaseItem, double rangeOtherTerrain, SyncItem target) {
        return setupPathToDestination(syncBaseItem, rangeOtherTerrain, target.getAbstractSyncPhysical().getTerrainType(), target.getAbstractSyncPhysical().getPosition(), target.getAbstractSyncPhysical().getRadius());
    }

    public SimplePath setupPathToDestination(SyncBaseItem syncBaseItem, double rangeOtherTerrain, TerrainType targetTerrainType, DecimalPosition targetPosition, double targetRadius) {
        double totalRangeOtherTerrain = syncBaseItem.getAbstractSyncPhysical().getRadius() + targetRadius + rangeOtherTerrain;
        return setupPathToDestination(syncBaseItem, targetTerrainType, targetPosition, totalRangeOtherTerrain);
    }

    private SimplePath setupPathToDestination(SyncBaseItem syncItem, TerrainType targetTerrainType, DecimalPosition destination, double totalRangeOtherTerrain) {
        return setupPathToDestination(syncItem.getAbstractSyncPhysical().getPosition(),
                syncItem.getAbstractSyncPhysical().getRadius(),
                syncItem.getAbstractSyncPhysical().getTerrainType(),
                targetTerrainType,
                destination,
                totalRangeOtherTerrain);
    }

    public SimplePath setupPathToDestination(DecimalPosition position, double radius, TerrainType terrainType, TerrainType targetTerrainType, DecimalPosition destination, double totalRangeOtherTerrain) {
        double correctedRadius = PassabilityGrid.bucketRadius(radius + RADIUS_GROW);
        SimplePath path = new SimplePath();
        List<DecimalPosition> positions = new ArrayList<>();
        PathingNodeWrapper startNode = terrainService.getTerrainAnalyzer().getPathingNodeWrapper(position);
        PathingNodeWrapper destinationNode = terrainService.getTerrainAnalyzer().getPathingNodeWrapper(destination);
        if (startNode.equals(destinationNode)) {
            positions.add(destination);
            path.setWayPositions(positions);
            return path;
        }
        if (!destinationNode.isFree(targetTerrainType)) {
            throw new PathFindingNotFreeException("Destination tile is not free: " + destination);
        }
        PathingNodeWrapper correctedDestinationNode;
        AStarContext aStarContext = new AStarContext(terrainType);
        DecimalPosition additionPathElement = null;
        if (TerrainDestinationFinderUtil.differentTerrain(terrainType, targetTerrainType)) {
            TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(destination, totalRangeOtherTerrain, radius + 2, terrainType, terrainService.getTerrainAnalyzer());
            terrainDestinationFinder.find();
            correctedDestinationNode = terrainDestinationFinder.getReachableNode();
            if (correctedDestinationNode != null) {
                additionPathElement = correctedDestinationNode.getCenter();
            } else {
                return new SimplePath().destinationUnreachable(findNearestPosition(destination, terrainType, position, radius));
            }
        } else {
            List<Index> scopeNodeIndices = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, correctedRadius), (int) NODE_SIZE);
            DestinationFinder destinationFinder = new DestinationFinder(destination, destinationNode, terrainType, scopeNodeIndices, terrainService.getTerrainAnalyzer());
            correctedDestinationNode = destinationFinder.find();
        }
        aStarContext.setPassabilityGrid(passabilityGrid.getOrBuild(terrainType, correctedRadius));
        aStarContext.setBuildingBlockedNodes(BuildingBlockerOverlay.compute(collectBuildings(), position, destination, radius));
        aStarContext.setStartStuck(startNode.isStuck(aStarContext));
        aStarContext.setStartPosition(position);
        aStarContext.setMaxStuckDistance(correctedRadius);
        aStarContext.setDestination(destination);

        AStar aStar = new AStar(startNode, correctedDestinationNode, aStarContext);
        aStar.expandAllNodes();
        com.btxtech.shared.system.debugtool.DebugHelperStatic.setLastAStar(aStar);
        for (PathingNodeWrapper pathingNodeWrapper : aStar.convertPath()) {
            positions.add(pathingNodeWrapper.getCenter());
        }
        if (additionPathElement != null) {
            positions.add(additionPathElement);
        }
        // Append the actually-reachable end-of-path, not the raw destination — if the
        // original sits inside an obstacle's radius+grow clearance (DestinationFinder
        // corrected it) or A* gave up before reaching it (pathFound=false, bestFitNode
        // used), pointing the unit at the original makes it chase a point it cannot
        // stand on and orbit forever.
        if (aStar.isPathFound() && correctedDestinationNode.equals(destinationNode)) {
            positions.add(destination);
        } else {
            positions.add(aStar.getReachedNode().getCenter());
        }
        path.setWayPositions(positions);
        return path;
    }

    /** All immovable items (buildings) as footprint blockers for the A* overlay. Movable units are
     * excluded — they are handled by ORCA, not A*. */
    /**
     * One line's worth of context about a unit that could not get where it was sent.
     *
     * <p>The nearest building is in here because it is the difference between the two stories that
     * end the same way: a unit wedged against a building, and a unit whose route is genuinely cut
     * off. Both look like "it stopped". Only one of them is a bug in this file.
     *
     * <p>Best-effort throughout - this runs at the moment something already went wrong, and a
     * diagnostic that can throw is worse than no diagnostic.
     */
    private String describe(SyncBaseItem syncBaseItem, SyncPhysicalMovable movable) {
        StringBuilder text = new StringBuilder();
        try {
            DecimalPosition position = movable.getPosition();
            DecimalPosition destination = movable.getFinalDestination();
            text.append(" item=").append(syncBaseItem.getId());
            if (syncBaseItem.getBaseItemType() != null) {
                text.append(" type=").append(syncBaseItem.getBaseItemType().getInternalName());
            }
            if (syncBaseItem.getBase() != null) {
                text.append(" base=").append(syncBaseItem.getBase().getBaseId());
            }
            text.append(" pos=").append(format(position));
            text.append(" dest=").append(format(destination));
            if (position != null && destination != null) {
                text.append(" remaining=").append(round(position.getDistance(destination)));
            }
            appendNearestBuilding(text, position, syncBaseItem);
        } catch (Throwable t) {
            text.append(" (context unavailable: ").append(t.getMessage()).append(')');
        }
        return text.toString();
    }

    private void appendNearestBuilding(StringBuilder text, DecimalPosition position, SyncBaseItem self) {
        if (position == null) {
            return;
        }
        SyncBaseItem[] nearest = new SyncBaseItem[1];
        double[] nearestGap = new double[]{Double.MAX_VALUE};
        // Spatial query, not a full item walk: this runs inside the server tick, and a base where
        // twenty units give up in the same second would otherwise pay twenty scans over every item
        // on the planet for the sake of a log line.
        syncItemContainerService.iterateCellQuadBaseItem(position, DIAGNOSTIC_BUILDING_SCAN, other -> {
            AbstractSyncPhysical physical = other.getAbstractSyncPhysical();
            if (other.equals(self) || physical.canMove() || !physical.hasPosition()) {
                return;
            }
            // Gap between the bodies, not between the centres: "0.0" then reads as touching.
            double gap = physical.getPosition().getDistance(position) - physical.getRadius();
            if (gap < nearestGap[0]) {
                nearestGap[0] = gap;
                nearest[0] = other;
            }
        });
        if (nearest[0] == null) {
            return;
        }
        text.append(" nearestBuilding=");
        text.append(nearest[0].getBaseItemType() != null ? nearest[0].getBaseItemType().getInternalName() : "?");
        text.append('#').append(nearest[0].getId());
        text.append('@').append(round(nearestGap[0]));
        if (nearest[0].getBase() != null) {
            text.append(" nearestBuildingBase=").append(nearest[0].getBase().getBaseId());
        }
    }

    private String format(DecimalPosition position) {
        return position == null ? "null" : "(" + round(position.getX()) + "/" + round(position.getY()) + ")";
    }

    private String round(double value) {
        return String.valueOf(Math.round(value * 100.0) / 100.0);
    }

    private List<BuildingBlockerOverlay.Blocker> collectBuildings() {
        List<BuildingBlockerOverlay.Blocker> buildings = new ArrayList<>();
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> {
            AbstractSyncPhysical physical = syncBaseItem.getAbstractSyncPhysical();
            if (physical.canMove() || !physical.hasPosition()) {
                return;
            }
            DecimalPosition buildingPosition = physical.getPosition();
            if (buildingPosition != null) {
                buildings.add(new BuildingBlockerOverlay.Blocker(buildingPosition, physical.getRadius()));
            }
        });
        return buildings;
    }

    public DecimalPosition findNearestPosition(DecimalPosition start, TerrainType terrainType, DecimalPosition destination, double radius) {
        List<Index> scopeNodeIndices = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, radius), (int) NODE_SIZE);

        DecimalPosition distanceVector = destination.sub(start);
        int count = (int) Math.ceil(distanceVector.length() / NODE_SIZE) + 2; // +2 to prevent item stuck if e.g. builder too close to water
        DecimalPosition direction = distanceVector.normalize();

        for (int i = 0; i < count; i++) {
            DecimalPosition position = start.add(direction.multiply(i * NODE_SIZE));
            Index index = TerrainUtil.terrainPositionToNodeIndex(position);
            if (terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(terrainType, index)) {
                if (scopeNodeIndices.stream()
                        .allMatch(scopeIndex -> terrainService.getTerrainAnalyzer().isTerrainTypeAllowed(terrainType, scopeIndex.add(index)))) {
                    return position;
                }

            }

        }
        throw new IllegalArgumentException("TerrainDestinationFinder.findNearestPosition(): no reachable terrain destination found. start: " + start + " destination: " + destination + " radius: " + radius + " terrainType: " + terrainType);
    }

    /**
     * Driven from {@link com.btxtech.shared.gameengine.planet.PlanetService#enableTracking(boolean)},
     * so the pathing breakdown appears exactly when the planet-level breakdown does. Pathing is where
     * the tick budget goes - the production dumps put it at ~90% of the total tick time - and the
     * planet tracker only reports that one number.
     */
    public void enableTracking(boolean track) {
        pathingServiceTracker.setRunning(track);
    }

    public void tick(SynchronizationSendingContext synchronizationSendingContext) {
        try {
            // DebugHelperStatic.setCurrentTick(-1);
            pathingServiceTracker.startTick();
            setupPreferredVelocity();
            pathingServiceTracker.afterPreparation();
            calculateItemVelocity();
            pathingServiceTracker.afterSolveVelocity();
            implementPosition();
            pathingServiceTracker.afterImplementPosition();
            checkDestination();
            pathingServiceTracker.afterCheckDestination();
            syncItemContainerService.afterPathingServiceTick();
            pathingServiceTracker.afterSyncItemContainerService();
            finalization();
            pathingServiceTracker.afterFinalization();
            pathingServiceTracker.endTick();
            // DebugHelperStatic.printAfterTick(debugHelper);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, t.getMessage(), t);
        }
    }

    /**
     * MASTER-only: detect units that ORCA cannot free (crowded with no progress) and replan a fresh,
     * building-aware A* path for them. The new path is pushed to clients through {@code notifier}
     * (→ {@code SyncService.notifySendSyncBaseItem}) so it rides the normal TickInfo channel; clients
     * never replan themselves, which keeps server and prediction in sync.
     *
     * <p>Must be called after {@link #tick} so this tick's movement and crowded-state are settled.
     */
    public void replanStuckItems(Consumer<SyncBaseItem> notifier) {
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> {
            AbstractSyncPhysical physical = syncBaseItem.getAbstractSyncPhysical();
            if (!physical.canMove()) {
                return;
            }
            SyncPhysicalMovable movable = (SyncPhysicalMovable) physical;
            if (!movable.detectStuck()) {
                return;
            }
            if (movable.isReplanBudgetExhausted()) {
                // Goal unreachable after repeated tries — stop instead of thrashing A* forever.
                // This is the end of the line for a unit the player will describe as "stuck", and
                // until this line existed it was the only outcome in the game that produced no
                // record at all: five hours of PROD logs at WARNING held not one pathing entry
                // while units were visibly giving up. Everything a reader needs to decide the next
                // incident goes on one line — where it stood, where it wanted to go, and what was
                // next to it — because the alternative is reconstructing it from the source again.
                logger.warning("[PathingStuck] gave up" + describe(syncBaseItem, movable));
                movable.stop();
                notifier.accept(syncBaseItem);
                return;
            }
            DecimalPosition destination = movable.getFinalDestination();
            if (destination == null) {
                return;
            }
            try {
                List<DecimalPosition> oldWay = movable.getPath() != null ? movable.getPath().getWayPositions() : null;
                SimplePath newPath = setupPathToDestination(movable.getPosition(),
                        physical.getRadius(),
                        physical.getTerrainType(),
                        physical.getTerrainType(),
                        destination,
                        0);
                if (oldWay != null && oldWay.equals(newPath.getWayPositions())) {
                    // A replan that returns the path the unit is already stuck on cannot free it,
                    // and the next two will do the same: A* is deterministic, so the only thing
                    // that can change the answer is the overlay, and the overlay is computed from
                    // this same position. Worth a line of its own — it is the difference between
                    // "the route is genuinely blocked" and "the replan is a no-op", which look
                    // identical from the outside and need opposite fixes.
                    logger.warning("[PathingStuck] replan returned the identical path"
                            + describe(syncBaseItem, movable));
                }
                movable.setReplanPath(newPath);
                notifier.accept(syncBaseItem);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Stuck replan failed for item " + syncBaseItem.getId() + ": " + t.getMessage(), t);
                movable.stop();
                notifier.accept(syncBaseItem);
            }
        });
    }

    private void calculateItemVelocity() {
        ItemVelocityCalculator itemVelocityCalculator = new ItemVelocityCalculator(syncItemContainerService, terrainService.getTerrainAnalyzer());
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> itemVelocityCalculator.analyse(syncBaseItem.getAbstractSyncPhysical()));
        itemVelocityCalculator.calculateVelocity();
    }

    private void setupPreferredVelocity() {
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> {
            if (!syncBaseItem.getAbstractSyncPhysical().canMove()) {
                return;
            }
            syncBaseItem.getSyncPhysicalMovable().setupPreferredVelocity();
        });
    }

    private void implementPosition() {
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> {
            AbstractSyncPhysical abstractSyncPhysical = syncBaseItem.getAbstractSyncPhysical();
            if (!abstractSyncPhysical.canMove()) {
                return;
            }
            ((SyncPhysicalMovable) abstractSyncPhysical).implementPosition();
        });
    }


    private void checkDestination() {
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> {
            if (!syncBaseItem.getAbstractSyncPhysical().canMove()) {
                return;
            }
            ((SyncPhysicalMovable) syncBaseItem.getAbstractSyncPhysical()).stopIfDestinationReached();
        });
    }

    private void finalization() {
        syncItemContainerService.iterateOverBaseItemsIdOrdered(syncBaseItem -> {
            if (!syncBaseItem.getAbstractSyncPhysical().canMove()) {
                return;
            }

            syncBaseItem.getSyncPhysicalMovable().finalization();
        });
    }
}




