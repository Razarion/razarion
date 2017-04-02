package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;

/**
 * Created by Beat
 * 20.09.2016.
 */
public class ObstacleSlope extends Obstacle {
    private Line line;

    public ObstacleSlope(Line line) {
        this.line = line;
    }

    @Override
    public DecimalPosition project(DecimalPosition point) {
        return line.getNearestPointOnLine(point);
    }

    @Override
    public boolean isPiercing(Line line) {
        return this.line.getCrossInclusive(line) != null;
    }

    public Line getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "ObstacleSlope{" +
                "line=" + line +
                '}';
    }
}
