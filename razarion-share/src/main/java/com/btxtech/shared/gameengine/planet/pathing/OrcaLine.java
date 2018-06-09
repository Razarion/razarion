package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;

/**
 * Created by Beat
 * on 29.05.2018.
 */
public class OrcaLine {
    private DecimalPosition point;
    private DecimalPosition direction;
    private DecimalPosition relativeVelocity;
    private DecimalPosition relativePosition;
    private double combinedRadius;
    private DecimalPosition u;

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

    public DecimalPosition getU() {
        return u;
    }

    public void setU(DecimalPosition u) {
        this.u = u;
    }
}
