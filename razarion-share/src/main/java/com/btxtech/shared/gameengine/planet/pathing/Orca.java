package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

/**
 * Created by Beat
 * on 29.05.2018.
 */
// http://gamma.cs.unc.edu/ORCA/
public class Orca {
    public static final double TAU = 5;
    private DecimalPosition u;
    private DecimalPosition direction;
    private DecimalPosition relativeVelocity;
    private DecimalPosition relativePosition;
    private double combinedRadius;
    private Line line;
    private Line line1;

    public Orca(SyncPhysicalMovable syncPhysicalMovable, SyncPhysicalMovable other) {
        relativePosition = other.getPosition2d().sub(syncPhysicalMovable.getPosition2d());
        relativeVelocity = syncPhysicalMovable.getVelocity().multiply(PlanetService.TICK_FACTOR).sub(other.getVelocity().multiply(PlanetService.TICK_FACTOR));
        double distanceSq = relativePosition.magnitude() * relativePosition.magnitude();
        combinedRadius = syncPhysicalMovable.getRadius() + other.getRadius();
        double combinedRadiusSq = combinedRadius * combinedRadius;

        if (distanceSq > combinedRadiusSq) {
            // No collision.
            DecimalPosition w = relativeVelocity.sub(relativePosition.divide(TAU));

            // Vector from cutoff center to relative velocity.
            double wLengthSq = w.magnitude() * w.magnitude();
            double dotProduct1 = w.dotProduct(relativePosition);

            if (dotProduct1 < 0.0 && dotProduct1 * dotProduct1 > combinedRadiusSq * wLengthSq) {
                // Project on cut-off circle.
                double wLength = Math.sqrt(wLengthSq);
                DecimalPosition unitW = w.divide(wLength);

                direction = new DecimalPosition(unitW.getY(), -unitW.getX()); // Rotate -90deg
                u = unitW.multiply(combinedRadius / TAU - wLength);
            } else {
                // Project on legs.
                double leg = Math.sqrt(distanceSq - combinedRadiusSq);

                if (relativePosition.determinant(w) > 0.0) {
                    // Project on left leg.
                    direction = new DecimalPosition(relativePosition.getX() * leg - relativePosition.getY() * combinedRadius, relativePosition.getX() * combinedRadius + relativePosition.getY() * leg).divide(distanceSq);
                } else {
                    // Project on right leg.
                    direction = new DecimalPosition(relativePosition.getX() * leg + relativePosition.getY() * combinedRadius, -relativePosition.getX() * combinedRadius + relativePosition.getY() * leg).divide(-distanceSq);
                }

                double dotProduct2 = relativeVelocity.dotProduct(direction);
                u = direction.multiply(dotProduct2).sub(relativeVelocity);
            }
        } else {
            // Collision. Project on cut-off circle of time timeStep.
            return; // TODO
        }
        DecimalPosition point = syncPhysicalMovable.getVelocity().multiply(PlanetService.TICK_FACTOR).add(0.5, u);
        DecimalPosition point2 = syncPhysicalMovable.getVelocity().multiply(PlanetService.TICK_FACTOR).add(u);
        line = new Line(point, point.getPointWithDistance(100, direction.add(point), true));
        line1 = new Line(point2, point2.getPointWithDistance(100, direction.add(point2), true));
    }

    public DecimalPosition getRelativeVelocity() {
        return relativeVelocity;
    }

    public DecimalPosition getRelativePosition() {
        return relativePosition;
    }

    public double getCombinedRadius() {
        return combinedRadius;
    }

    public DecimalPosition getU() {
        return u;
    }

    public DecimalPosition getDirection() {
        return direction;
    }

    public Line getLine() {
        return line;
    }

    public Line getLine1() {
        return line1;
    }
}
