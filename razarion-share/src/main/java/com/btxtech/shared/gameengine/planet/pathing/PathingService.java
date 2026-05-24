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
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.NODE_SIZE;

@Singleton
public class PathingService {
    public static final double STOP_DETECTION_NEIGHBOUR_DISTANCE = 0.1;
    public static final double RADIUS_GROW = 1;
    private final Logger logger = Logger.getLogger(PathingService.class.getName());
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final TerrainService terrainService;
    private final PassabilityGrid passabilityGrid;
    private final PathingServiceTracker pathingServiceTracker = new PathingServiceTracker(false);

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




