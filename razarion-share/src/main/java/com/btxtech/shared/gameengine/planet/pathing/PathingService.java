package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class PathingService {
    public static final double MAXIMUM_CORRECTION = 0.2;
    public static final double PENETRATION_TOLERANCE = 1;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;

    public Path setupPathToDestination(SyncBaseItem syncItem, DecimalPosition destination) {
        return new Path().setDestination(destination);
    }

    public Path setupPathToDestination(SyncBaseItem syncItem, SyncItem target) {
        throw new UnsupportedOperationException();
        // return new Path().setDestination(destination);
    }

    public void tick() {
        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                return null;
            }

            ((SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea()).setupForTick(syncItemContainerService);

            return null;
        });

        Collection<Contact> contacts = findContacts();
        for (int i = 0; i < 10; i++) {
            solveVelocityContacts(contacts);
        }
        implementPosition();
        while (!solvePositionContacts()) ;

        syncItemContainerService.iterateOverBaseItems(false, false, null, syncBaseItem -> {
            if (!syncBaseItem.getSyncPhysicalArea().canMove()) {
                return null;
            }
            SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) syncBaseItem.getSyncPhysicalArea();
            if (syncPhysicalMovable.hasDestination() && syncPhysicalMovable.checkDestinationReached(syncItemContainerService)) {
                syncPhysicalMovable.stop();
            }
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
                findUnitContacts(syncBaseItem, alreadyAddedItems, contacts);
            }
            alreadyAddedItems.add(syncPhysicalMovable);

            return null;
        });
        return contacts;
    }

    private void findObstacleContacts(SyncPhysicalMovable item, Collection<Contact> contacts) {
        for (Obstacle obstacle : terrainService.getObstacles()) {
            Contact contact = obstacle.hasContact(item);
            if (contact != null) {
                contacts.add(contact);
            }
        }
    }

    private void findUnitContacts(SyncBaseItem syncBaseItem, Collection<SyncPhysicalArea> alreadyAddedItems, Collection<Contact> contacts) {
        syncItemContainerService.iterateOverBaseItems(false, false, syncBaseItem, null, otherSyncBaseItem -> {
            SyncPhysicalArea other = otherSyncBaseItem.getSyncPhysicalArea();

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
            } else {
                DecimalPosition velocity = item1.getVelocity();
                double projection = contact.getNormal().dotProduct(velocity);
                if (projection < 0) {
                    DecimalPosition pushAway = contact.getNormal().multiply(-projection);
                    DecimalPosition newVelocity = velocity.add(pushAway);
                    item1.setVelocity(newVelocity);
                }
            }
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
                    DecimalPosition pushAway = item1.getXYPosition().sub(item2.getXYPosition()).normalize(penetration / 2.0);
                    item1.addToXYPosition(pushAway);
                    item2.addToXYPosition(pushAway.multiply(-1.0));
                } else {
                    DecimalPosition pushAway = item1.getXYPosition().sub(item2.getXYPosition()).normalize(penetration);
                    item1.addToXYPosition(pushAway);
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

            for (Obstacle obstacle : terrainService.getObstacles()) {
                // There is no check if the unit is inside the restricted area
                DecimalPosition projection = obstacle.project(syncPhysicalMovable.getXYPosition());
                double distance = projection.getDistance(syncPhysicalMovable.getXYPosition()) - syncPhysicalMovable.getRadius();
                if (distance >= -PENETRATION_TOLERANCE) {
                    continue;
                }
                solved.setO(false);
                double penetration = -distance;
                if (penetration > MAXIMUM_CORRECTION) {
                    penetration = MAXIMUM_CORRECTION;
                }
                DecimalPosition pushAway = syncPhysicalMovable.getXYPosition().sub(projection).normalize(penetration);
                syncPhysicalMovable.addToXYPosition(pushAway);
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
            distance = item1.getDesiredPosition().getDistance(item2.getXYPosition()) - item1.getRadius() - item2.getRadius();
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
}

// TODO make two units dest in middle (both point to the destination before start)
// TODO 1 slow
// TODO 3: prevent lining up
// TODO 16,17,23 does not bypass enough
// TODO 18 tooks very 1ong unti1 are units are stopped
// TODO 31,32,33 very slow
// TODO Performance(solve position contacts & stop condition)
// TODO different speed, acceleration and radius





