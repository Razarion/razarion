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

import javax.inject.Inject;
import javax.inject.Singleton;
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
    private final PathingServiceTracker pathingServiceTracker = new PathingServiceTracker(false);

    @Inject
    public PathingService(TerrainService terrainService, SyncItemContainerServiceImpl syncItemContainerService) {
        this.terrainService = terrainService;
        this.syncItemContainerService = syncItemContainerService;
    }

    public SimplePath setupPathToDestination(SyncBaseItem syncItem, DecimalPosition destination) {
        return setupPathToDestination(syncItem, syncItem.getAbstractSyncPhysical().getTerrainType(), destination, 0);
    }

    public SimplePath setupPathToDestination(SyncBaseItem syncBaseItem, double range, SyncItem target) {
        return setupPathToDestination(syncBaseItem, range, target.getAbstractSyncPhysical().getTerrainType(), target.getAbstractSyncPhysical().getPosition(), target.getAbstractSyncPhysical().getRadius());
    }

    public SimplePath setupPathToDestination(SyncBaseItem syncBaseItem, double range, TerrainType targetTerrainType, DecimalPosition targetPosition, double targetRadius) {
        double totalRange = syncBaseItem.getAbstractSyncPhysical().getRadius() + targetRadius + range;
        return setupPathToDestination(syncBaseItem, targetTerrainType, targetPosition, totalRange);
    }

    private SimplePath setupPathToDestination(SyncBaseItem syncItem, TerrainType targetTerrainType, DecimalPosition destination, double totalRange) {
        return setupPathToDestination(syncItem.getAbstractSyncPhysical().getPosition(),
                syncItem.getAbstractSyncPhysical().getRadius(),
                syncItem.getAbstractSyncPhysical().getTerrainType(),
                targetTerrainType,
                destination,
                totalRange);
    }

    public SimplePath setupPathToDestination(DecimalPosition position, double radius, TerrainType terrainType, TerrainType targetTerrainType, DecimalPosition destination, double totalRange) {
        // long time = System.currentTimeMillis();
        // Attention due to performance!! isInSight() surface data (Obstacle-Model) is not based on the AStar surface data -> AStar model must overlap Obstacle-Model
        double correctedRadius = radius + RADIUS_GROW;
        SimplePath path = new SimplePath();
        List<DecimalPosition> positions = new ArrayList<>();
        PathingNodeWrapper startNode = terrainService.getTerrainAnalyzer().getPathingNodeWrapper(position);
        PathingNodeWrapper destinationNode = terrainService.getTerrainAnalyzer().getPathingNodeWrapper(destination);
        if (startNode.equals(destinationNode)) {
            positions.add(destination);
            path.setWayPositions(positions);
            // LOGGER.severe("Time for Pathing in same node: " + (System.currentTimeMillis() - time));
            return path;
        }
        if (!destinationNode.isFree(targetTerrainType)) {
            throw new PathFindingNotFreeException("Destination tile is not free: " + destination);
        }
        List<Index> scopeNodeIndices = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, correctedRadius), (int) NODE_SIZE);
        PathingNodeWrapper correctedDestinationNode;
        AStarContext aStarContext;
        DecimalPosition additionPathElement = null;
        if (TerrainDestinationFinderUtil.differentTerrain(terrainType, targetTerrainType)) {
            TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(destination, totalRange, radius + 2, terrainType, terrainService.getTerrainAnalyzer());
            terrainDestinationFinder.find();
            correctedDestinationNode = terrainDestinationFinder.getReachableNode();
            if (correctedDestinationNode != null) {
                additionPathElement = correctedDestinationNode.getCenter();
                aStarContext = new AStarContext(terrainType, scopeNodeIndices);
            } else {
                return new SimplePath().destinationUnreachable(findNearestPosition(destination, terrainType, position, radius));
            }
        } else {
//            DestinationFinder destinationFinder = new DestinationFinder(position, destination, destinationNode, syncItem.getSyncPhysicalArea().getTerrainType(), scopeNodeIndices, terrainService.getPathingAccess());
//            destinationFinder.find();
//            correctedDestinationNode = terrainService.getPathingAccess().getPathingNodeWrapper(destinationFinder.getCorrectedDestination());;
//            destination = destinationFinder.getCorrectedDestination();
            DestinationFinder destinationFinder = new DestinationFinder(destination, destinationNode, terrainType, scopeNodeIndices, terrainService.getTerrainAnalyzer());
            correctedDestinationNode = destinationFinder.find();
            aStarContext = new AStarContext(terrainType, scopeNodeIndices);
        }
        aStarContext.setStartStuck(startNode.isStuck(aStarContext));
        aStarContext.setStartPosition(position);
        aStarContext.setMaxStuckDistance(correctedRadius);
        aStarContext.setDestination(destination);

        AStar aStar = new AStar(startNode, correctedDestinationNode, aStarContext);
        aStar.expandAllNodes();
        for (PathingNodeWrapper pathingNodeWrapper : aStar.convertPath()) {
            positions.add(pathingNodeWrapper.getCenter());
        }
        // LOGGER.severe("Time for Pathing: " + (System.currentTimeMillis() - time) + " CloseListSize: " + aStar.getCloseListSize());
        if (additionPathElement != null) {
            positions.add(additionPathElement);
        }
        positions.add(destination);
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




