package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by Beat
 * on 07.09.2018.
 */
public class SynchronizationSendingContext {
    private Collection<Set<SyncBaseItem>> collisions = new LinkedList<>();

    public void addCollision(SyncPhysicalMovable syncPhysicalMovable1, SyncPhysicalMovable syncPhysicalMovable2) {
        Set<SyncBaseItem> colliding1 = null;
        Set<SyncBaseItem> colliding2 = null;
        for (Set<SyncBaseItem> collidingItems : collisions) {
            if (collidingItems.contains(toSyncBaseItem(syncPhysicalMovable1.getSyncItem()))) {
                if (colliding1 != null) {
                    throw new IllegalStateException();
                }
                colliding1 = collidingItems;
            }
            if (collidingItems.contains(toSyncBaseItem(syncPhysicalMovable2.getSyncItem()))) {
                if (colliding2 != null) {
                    throw new IllegalStateException();
                }
                colliding2 = collidingItems;
            }
            if (colliding1 != null && colliding2 != null) {
                break;
            }
        }
        if (colliding1 == null && colliding2 == null) {
            Set<SyncBaseItem> collidingItems = new HashSet<>();
            collidingItems.add((SyncBaseItem) syncPhysicalMovable1.getSyncItem());
            collidingItems.add((SyncBaseItem) syncPhysicalMovable2.getSyncItem());
            collisions.add(collidingItems);
        } else if (colliding1 != null && colliding2 != null) {
            colliding1.addAll(colliding2);
            collisions.remove(colliding2);
        } else if (colliding1 != null) {
            colliding1.add((SyncBaseItem) syncPhysicalMovable2.getSyncItem());
        } else {
            colliding2.add((SyncBaseItem) syncPhysicalMovable1.getSyncItem());
        }
    }

    private SyncBaseItem toSyncBaseItem(SyncItem syncItem) {
        return (SyncBaseItem) syncItem;
    }

    public Collection<Set<SyncBaseItem>> getCollisions() {
        return collisions;
    }
}
