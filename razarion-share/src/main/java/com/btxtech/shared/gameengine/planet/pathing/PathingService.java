package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class PathingService {
    public static final double MAXIMUM_CORRECTION = 0.02;
    public static final double PENETRATION_TOLERANCE = 0.1;
    public static final double STOP_DETECTION_NEIGHBOUR_DISTANCE = 0.1;
    public static final double STOP_DETECTION_DISTANCE = 0.1;
    // private Logger logger = Logger.getLogger(PathingService.class.getName());
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    private PathingServiceTracker pathingServiceTracker = new PathingServiceTracker(false);
    private PathingServiceUpdateListener pathingServiceUpdateListener;

    public void setPathingServiceUpdateListener(PathingServiceUpdateListener pathingServiceUpdateListener) {
        this.pathingServiceUpdateListener = pathingServiceUpdateListener;
    }

    public SimplePath setupPathToDestination(SyncBaseItem syncItem, DecimalPosition destination) {
        return setupPathToDestination(syncItem, syncItem.getSyncPhysicalArea().getTerrainType(), destination, 0);
    }

    public SimplePath setupPathToDestination(SyncBaseItem syncBaseItem, double range, SyncItem target) {
        return setupPathToDestination(syncBaseItem, range, target.getSyncPhysicalArea().getTerrainType(), target.getSyncPhysicalArea().getPosition2d(), target.getSyncPhysicalArea().getRadius());
    }

    public SimplePath setupPathToDestination(SyncBaseItem syncBaseItem, double range, TerrainType targetTerrainType, DecimalPosition targetPosition, double targetRadius) {
        double totalRange = syncBaseItem.getSyncPhysicalArea().getRadius() + targetRadius + range;
        return setupPathToDestination(syncBaseItem, targetTerrainType, targetPosition, totalRange);
    }

    private SimplePath setupPathToDestination(SyncBaseItem syncItem, TerrainType targetTerrainType, DecimalPosition destination, double totalRange) {
        SimplePath path = new SimplePath();
        List<DecimalPosition> positions = new ArrayList<>();
        PathingNodeWrapper startNode = terrainService.getPathingAccess().getPathingNodeWrapper(syncItem.getSyncPhysicalArea().getPosition2d());
        PathingNodeWrapper destinationNode = terrainService.getPathingAccess().getPathingNodeWrapper(destination);
        if (startNode.equals(destinationNode)) {
            positions.add(destination);
            path.setWayPositions(positions);
            path.setTotalRange(totalRange);
            return path;
        }
        if (!destinationNode.isFree(targetTerrainType)) {
            throw new PathFindingNotFreeException("Destination tile is not free: " + destination);
        }
        // long time = System.currentTimeMillis();
        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, syncItem.getSyncPhysicalArea().getRadius()), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
        PathingNodeWrapper correctedDestinationNode;
        AStarContext aStarContext;
        DecimalPosition additionPathElement = null;
        if (TerrainDestinationFinder.differentTerrain(syncItem.getSyncPhysicalArea().getTerrainType(), targetTerrainType)) {
            TerrainDestinationFinder terrainDestinationFinder = new TerrainDestinationFinder(syncItem.getSyncPhysicalArea().getPosition2d(), destination, totalRange, syncItem.getSyncPhysicalArea().getRadius(), syncItem.getSyncPhysicalArea().getTerrainType(), terrainService.getPathingAccess());
            terrainDestinationFinder.find();
            // destination = terrainDestinationFinder.getReachableDestination();
            correctedDestinationNode = terrainDestinationFinder.getReachableNode();
            additionPathElement = correctedDestinationNode.getCenter();
            totalRange = 0;
            aStarContext = new AStarContext(syncItem.getSyncPhysicalArea().getTerrainType(), subNodeIndexScope);
        } else {
//            DestinationFinder destinationFinder = new DestinationFinder(syncItem.getSyncPhysicalArea().getPosition2d(), destination, destinationNode, syncItem.getSyncPhysicalArea().getTerrainType(), subNodeIndexScope, terrainService.getPathingAccess());
//            destinationFinder.find();
//            correctedDestinationNode = terrainService.getPathingAccess().getPathingNodeWrapper(destinationFinder.getCorrectedDestination());;
//            destination = destinationFinder.getCorrectedDestination();
            DestinationFinder destinationFinder = new DestinationFinder(destination, destinationNode, syncItem.getSyncPhysicalArea().getTerrainType(), subNodeIndexScope, terrainService.getPathingAccess());
            correctedDestinationNode = destinationFinder.find();
            aStarContext = new AStarContext(syncItem.getSyncPhysicalArea().getTerrainType(), subNodeIndexScope);
        }
        aStarContext.setStartSuck(startNode.isStuck(aStarContext));
        aStarContext.setStartPosition(syncItem.getSyncPhysicalArea().getPosition2d());
        aStarContext.setMaxStuckDistance(syncItem.getSyncPhysicalArea().getRadius());
        aStarContext.setDestination(destination);

        AStar aStar = new AStar(startNode, correctedDestinationNode, aStarContext);
        aStar.expandAllNodes();
        for (PathingNodeWrapper pathingNodeWrapper : aStar.convertPath()) {
            positions.add(pathingNodeWrapper.getCenter());
        }
        // logger.severe("Time for Pathing: " + (System.currentTimeMillis() - time) + " CloseListSize: " + aStar.getCloseListSize());
        if (additionPathElement != null) {
            positions.add(additionPathElement);
        }
        positions.add(destination);
        path.setWayPositions(positions);
        path.setTotalRange(totalRange);
        return path;
    }

    public void tick() {
        pathingServiceTracker.startTick();
        preparation();
        pathingServiceTracker.afterPreparation();

        Collection<Contact> contacts = findContacts();
        pathingServiceTracker.afterFindContacts();
        solveVelocity(contacts);
        pathingServiceTracker.afterSolveVelocity();
        implementPosition();
        pathingServiceTracker.afterImplementPosition();
        solvePosition();
        pathingServiceTracker.afterSolvePosition();
        checkDestination();
        pathingServiceTracker.afterCheckDestination();

        finalization();
        pathingServiceTracker.afterFinalization();
        if (pathingServiceUpdateListener != null) {
            pathingServiceUpdateListener.onPathingTickFinished();
        }
        pathingServiceTracker.afterUpdateListener();
        pathingServiceTracker.endTick();
    }

    private void preparation() {
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                return null;
            }

            syncBaseItem.getSyncPhysicalMovable().setupForTick();

            return null;
        });
    }

    private Collection<Contact> findContacts() {
        Collection<Contact> contacts = new ArrayList<>();
        Collection<SyncPhysicalArea> alreadyAddedItems = new ArrayList<>();

        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            SyncPhysicalArea syncPhysicalArea = syncBaseItem.getSyncPhysicalArea();
            if (!syncPhysicalArea.canMove()) {
                return null;
            }

            SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) syncPhysicalArea;
            if (syncPhysicalMovable.isMoving()) {
                findObstacleContacts(syncPhysicalMovable, contacts);
                findItemContacts(syncBaseItem, alreadyAddedItems, contacts);
            }
            alreadyAddedItems.add(syncPhysicalMovable);

            return null;
        });
        return contacts;
    }

    private void findObstacleContacts(SyncPhysicalMovable item, Collection<Contact> contacts) {
        for (Obstacle obstacle : terrainService.getPathingAccess().getObstacles(item)) {
            Contact contact = obstacle.hasContact(item);
            if (contact != null) {
                contacts.add(contact);
            }
        }
    }

    private void findItemContacts(SyncBaseItem syncBaseItem, Collection<SyncPhysicalArea> alreadyAddedItems, Collection<Contact> contacts) {
        syncItemContainerService.iterateOverItems(false, false, syncBaseItem, syncBaseItem, otherSyncItem -> {
            SyncPhysicalArea other = otherSyncItem.getSyncPhysicalArea();

            if (alreadyAddedItems.contains(other)) {
                return null;
            }

            Contact contact = ((SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea()).hasContact(other);
            if (contact != null) {
                contacts.add(contact);
            }
            return null;
        });
    }

    private void solveVelocity(Collection<Contact> contacts) {
        for (int i = 0; i < 10; i++) {
            solveVelocityContacts(contacts);
        }
    }

    private void solveVelocityContacts(Collection<Contact> contacts) {
        for (Contact contact : contacts) {
            SyncPhysicalMovable item1 = contact.getItem1();
            if (contact.hasUnit2AndCanMove()) {
                SyncPhysicalMovable item2 = (SyncPhysicalMovable) contact.getItem2();
                double newPenetration = calculateNewPenetration(item1, item2);
                if (newPenetration == 0) {
                    continue;
                }
                DecimalPosition relativeVelocity = item1.getVelocity();
                if (item2.getVelocity() != null) {
                    relativeVelocity = relativeVelocity.sub(item2.getVelocity());
                }
                double projection = contact.getNormal().dotProduct(relativeVelocity);
                DecimalPosition pushAway = contact.getNormal().multiply(-projection / 2.0);
                DecimalPosition newVelocity1 = item1.getVelocity().add(pushAway);
                item1.setVelocity(newVelocity1);
                DecimalPosition velocity2 = item2.getVelocity();
                if (velocity2 == null) {
                    velocity2 = new DecimalPosition(0, 0);
                }
                DecimalPosition newVelocity2 = velocity2.add(pushAway.multiply(-1));
                item2.setVelocity(newVelocity2);
                onPathingChanged(item1);
                onPathingChanged(item2);
            } else {
                DecimalPosition velocity = item1.getVelocity();
                double projection = contact.getNormal().dotProduct(velocity);
                if (projection < 0) {
                    DecimalPosition pushAway = contact.getNormal().multiply(-projection);
                    DecimalPosition newVelocity = velocity.add(pushAway);
                    item1.setVelocity(newVelocity);
                    onPathingChanged(item1);
                }
            }
        }
    }

    private void solvePosition() {
        boolean solved = false;
        for (int i = 0; i < 10 && !solved; i++) {
            solved = solvePositionContacts();
        }
    }

    private boolean solvePositionContacts() {
        SingleHolder<Boolean> solved = new SingleHolder<>(true);
        // Units
        List<SyncPhysicalMovable> itemsToCheck = new ArrayList<>();
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            SyncPhysicalArea syncPhysicalArea = syncBaseItem.getSyncPhysicalArea();
            if (!syncPhysicalArea.canMove()) {
                return null;
            }
            itemsToCheck.add((SyncPhysicalMovable) syncPhysicalArea);
            return null;
        });

        while (!itemsToCheck.isEmpty()) {
            SyncPhysicalMovable item1 = itemsToCheck.remove(0);
            for (SyncPhysicalMovable item2 : itemsToCheck) {
                double distance = item1.getDistance(item2);
                if (distance >= -PENETRATION_TOLERANCE) {
                    continue;
                }
                solved.setO(false);
                double penetration = -distance;
                if (penetration > MAXIMUM_CORRECTION) {
                    penetration = MAXIMUM_CORRECTION;
                }
                if (item2.canMove()) {
                    DecimalPosition pushAway = item1.getPosition2d().sub(item2.getPosition2d()).normalize(penetration / 2.0);
                    item1.addToPosition2d(pushAway);
                    item2.addToPosition2d(pushAway.multiply(-1.0));
                    onPathingChanged(item1);
                    onPathingChanged(item2);
                } else {
                    DecimalPosition pushAway = item1.getPosition2d().sub(item2.getPosition2d()).normalize(penetration);
                    item1.addToPosition2d(pushAway);
                    onPathingChanged(item1);
                }
            }
        }
        // obstacles
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            SyncPhysicalArea syncPhysicalArea = syncBaseItem.getSyncPhysicalArea();
            if (!syncPhysicalArea.canMove()) {
                return null;
            }
            SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) syncPhysicalArea;

            for (Obstacle obstacle : terrainService.getPathingAccess().getObstacles(syncPhysicalMovable)) {
                // There is no check if the unit is inside the restricted area
                DecimalPosition projection = obstacle.project(syncPhysicalMovable.getPosition2d());
                double distance = projection.getDistance(syncPhysicalMovable.getPosition2d()) - syncPhysicalMovable.getRadius();
                if (distance >= -PENETRATION_TOLERANCE) {
                    continue;
                }
                solved.setO(false);
                double penetration = -distance;
                if (penetration > MAXIMUM_CORRECTION) {
                    penetration = MAXIMUM_CORRECTION;
                }
                DecimalPosition pushAway = syncPhysicalMovable.getPosition2d().sub(projection).normalize(penetration);
                syncPhysicalMovable.addToPosition2d(pushAway);
                onPathingChanged(syncPhysicalMovable);
            }

            return null;
        });

        return solved.getO();
    }

    private double calculateNewPenetration(SyncPhysicalMovable item1, SyncPhysicalArea item2) {
        double distance;
        if (item2.canMove()) {
            distance = item1.getDesiredPosition().getDistance(((SyncPhysicalMovable) item2).getDesiredPosition()) - item1.getRadius() - item2.getRadius();
        } else {
            distance = item1.getDesiredPosition().getDistance(item2.getPosition2d()) - item1.getRadius() - item2.getRadius();
        }
        if (distance > 0) {
            return 0;
        }
        return -distance;
    }

    private void implementPosition() {
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            SyncPhysicalArea syncPhysicalArea = syncBaseItem.getSyncPhysicalArea();
            if (!syncPhysicalArea.canMove()) {
                return null;
            }
            ((SyncPhysicalMovable) syncPhysicalArea).implementPosition();
            return null;
        });
    }


    private void checkDestination() {
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                return null;
            }
            SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea();
            if (syncPhysicalMovable.hasDestination() && syncPhysicalMovable.checkDestinationReached()) {
                syncPhysicalMovable.stop();
                onPathingChanged(syncPhysicalMovable);
            }
            return null;
        });
    }

    private void finalization() {
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                return null;
            }

            syncBaseItem.getSyncPhysicalMovable().setupPosition3d();

            return null;
        });
    }

    private void onPathingChanged(SyncPhysicalMovable syncPhysicalMovable) {
        if (pathingServiceUpdateListener != null) {
            pathingServiceUpdateListener.onPathingChanged(syncPhysicalMovable);
        }
    }
}




