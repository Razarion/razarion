package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

/**
 * Created by Beat
 * 16.05.2016.
 */
public abstract class Obstacle {

    public abstract DecimalPosition project(DecimalPosition point);

    public Contact hasContact(SyncPhysicalMovable item) {
        // There is no check if the unit is inside the restricted area
        DecimalPosition pointOnLine = project(item.getXYPosition());
        DecimalPosition sub = item.getXYPosition().sub(pointOnLine);
        if (sub.magnitude() >= item.getRadius()) {
            return null;
        }
        return new Contact(item, this, sub.normalize());
    }

    public double getDistance(SyncPhysicalMovable item) {
        // There is no check if the unit is inside the restricted area
        DecimalPosition pointOnLine = project(item.getXYPosition());
        return pointOnLine.getDistance(item.getXYPosition()) - item.getRadius();
    }
}
