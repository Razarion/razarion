package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Line2I;

/**
 * Created by Beat
 * 20.09.2016.
 */
public class ObstacleLine extends Obstacle {
    private Line line;

    public ObstacleLine(Line line) {
        this.line = line;
    }

    public DecimalPosition project(DecimalPosition point) {
        return line.getNearestPointOnLine(point);
    }

    public Line getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "ObstacleLine{" +
                "line=" + line +
                '}';
    }
}
