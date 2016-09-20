package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line2I;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

/**
 * Created by Beat
 * 16.05.2016.
 */
public class Obstacle {
    // TODO setup obstacles for terrain objects (circle)
    private Line2I line;

    public Obstacle(Line2I line) {
        this.line = line;
    }

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

    public DecimalPosition project(DecimalPosition point) {
        return line.getNearestPointOnLine(point);
    }

    public Line2I getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "Obstacle{" +
                "line=" + line +
                '}';
    }
}
