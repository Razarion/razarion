package com.btxtech.shared.gameengine.pathing;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Line2I;

/**
 * Created by Beat
 * 16.05.2016.
 */
public class Obstacle {
    private Line2I line;

    public Obstacle(Line2I line) {
        this.line = line;
    }

    public Contact hasContact(Unit unit) {
        // There is no check if the unit is inside the restricted area
        DecimalPosition pointOnLine = project(unit.getPosition());
        DecimalPosition sub = unit.getPosition().sub(pointOnLine);
        if (sub.magnitude() >= unit.getRadius()) {
            return null;
        }
        return new Contact(unit, this, sub.normalize());
    }

    public double getDistance(Unit unit) {
        // There is no check if the unit is inside the restricted area
        DecimalPosition pointOnLine = project(unit.getPosition());
        return pointOnLine.getDistance(unit.getPosition()) - unit.getRadius();
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
