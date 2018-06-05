package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

/**
 * Created by Beat
 * on 29.05.2018.
 */
public class OrcaLine {
    private DecimalPosition point;
    private DecimalPosition direction;

    public OrcaLine(DecimalPosition point, DecimalPosition direction) {
        this.point = point;
        this.direction = direction;
    }

    public DecimalPosition getPoint() {
        return point;
    }

    public DecimalPosition getDirection() {
        return direction;
    }

    public Line toLine() {
        return new Line(point, point.getPointWithDistance(100, direction.add(point), true));
    }
}
