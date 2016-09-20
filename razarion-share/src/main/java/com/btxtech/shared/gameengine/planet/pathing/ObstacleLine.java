package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line2I;

/**
 * Created by Beat
 * 20.09.2016.
 */
public class ObstacleLine extends Obstacle {
    private Line2I line;

    public ObstacleLine(Line2I line) {
        this.line = line;
    }

    public DecimalPosition project(DecimalPosition point) {
        return line.getNearestPointOnLine(point);
    }

    public Line2I getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "ObstacleLine{" +
                "line=" + line +
                '}';
    }
}
