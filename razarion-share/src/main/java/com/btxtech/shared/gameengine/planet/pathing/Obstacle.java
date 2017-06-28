package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeObstacle;

/**
 * Created by Beat
 * 16.05.2016.
 */
public abstract class Obstacle {

    public abstract DecimalPosition project(DecimalPosition point);

    public abstract boolean isPiercing(Line line);

    public Contact hasContact(SyncPhysicalMovable item) {
        // There is no check if the unit is inside the restricted area
        DecimalPosition pointOnLine = project(item.getPosition2d());
        DecimalPosition sub = item.getPosition2d().sub(pointOnLine);
        if (sub.magnitude() >= item.getRadius()) {
            return null;
        }
        return new Contact(item, this, sub.normalize());
    }

    public double getDistance(SyncPhysicalArea item) {
        // There is no check if the unit is inside the restricted area
        DecimalPosition nearestPointOnObstacle = project(item.getPosition2d());
        return nearestPointOnObstacle.getDistance(item.getPosition2d()) - item.getRadius();
    }

    public abstract NativeObstacle toNativeObstacle();
}
