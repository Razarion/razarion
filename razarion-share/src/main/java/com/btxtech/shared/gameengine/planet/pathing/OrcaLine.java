package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;

/**
 * Created by Beat
 * on 29.05.2018.
 */
public class OrcaLine {
    private final DecimalPosition point;
    private final DecimalPosition direction;
    private DecimalPosition relativeVelocity;
    private DecimalPosition relativePosition;
    private double combinedRadius;

    public OrcaLine(DecimalPosition point, DecimalPosition direction) {
        if (direction.magnitude() < 0.9 || direction.magnitude() > 1.1) {
            throw new IllegalStateException("direction: " + direction);
        }
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
        return new Line(point.getPointWithDistance(-100, direction.add(point), true), point.getPointWithDistance(100, direction.add(point), true));
    }

    public DecimalPosition getRelativeVelocity() {
        return relativeVelocity;
    }

    public void setRelativeVelocity(DecimalPosition relativeVelocity) {
        this.relativeVelocity = relativeVelocity;
    }

    public DecimalPosition getRelativePosition() {
        return relativePosition;
    }

    public void setRelativePosition(DecimalPosition relativePosition) {
        this.relativePosition = relativePosition;
    }

    public double getCombinedRadius() {
        return combinedRadius;
    }

    public void setCombinedRadius(double combinedRadius) {
        this.combinedRadius = combinedRadius;
    }

    public boolean isVelocityAllowed(DecimalPosition velocity) {
        return direction.determinant(velocity.sub(point)) >= 0.0;
    }
}
