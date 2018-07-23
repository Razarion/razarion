package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * on 20.07.2018.
 */
public class TickContext {
    private Collection<SyncPhysicalMovable> pushAways = new ArrayList<>();
    private Collection<SyncPhysicalMovable> movings = new ArrayList<>();

    public void addPushAway(SyncPhysicalMovable pushAway) {
        pushAways.add(pushAway);
    }

    public void addMoving(SyncPhysicalMovable moving) {
        movings.add(moving);
    }

    public Collection<SyncPhysicalMovable> getPushAways() {
        return pushAways;
    }

    public Collection<SyncPhysicalMovable> getMovings() {
        return movings;
    }
}
