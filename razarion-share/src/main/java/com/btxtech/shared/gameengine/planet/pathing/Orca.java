package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 29.05.2018.
 */
// http://gamma.cs.unc.edu/ORCA/
public class Orca {
    public static final double TAU = 5;
    public static final double EPSILON = 0.00001;
    private DecimalPosition u;
    private DecimalPosition direction;
    private DecimalPosition relativeVelocity;
    private DecimalPosition relativePosition;
    private DecimalPosition preferredVelocity;
    private double combinedRadius;
    private double maxSpeed;
    private DecimalPosition newVelocity;
    private List<OrcaLine> orcaLines = new ArrayList<>();

    public Orca(SyncPhysicalMovable syncPhysicalMovable, SyncPhysicalMovable other) {
        relativePosition = other.getPosition2d().sub(syncPhysicalMovable.getPosition2d());
        relativeVelocity = syncPhysicalMovable.getVelocity().multiply(PlanetService.TICK_FACTOR).sub(other.getVelocity().multiply(PlanetService.TICK_FACTOR));
        preferredVelocity = syncPhysicalMovable.getVelocity().multiply(PlanetService.TICK_FACTOR);
        maxSpeed = syncPhysicalMovable.getVelocity().magnitude() * PlanetService.TICK_FACTOR;
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

            // Vector from cutoff center to relative velocity.
            DecimalPosition w = relativeVelocity.sub(relativePosition.multiply(PlanetService.TICKS_PER_SECONDS));

            double wLength = w.magnitude();
            DecimalPosition unitW = w.multiply(1.0 / wLength);

            direction = new DecimalPosition(unitW.getY(), -unitW.getX());
            u = unitW.multiply(combinedRadius * PlanetService.TICKS_PER_SECONDS - wLength);
        }
        DecimalPosition point = syncPhysicalMovable.getVelocity().multiply(PlanetService.TICK_FACTOR).add(0.5, u);
        orcaLines.add(new OrcaLine(point, direction));

        int lineFail = linearProgram2(orcaLines, preferredVelocity, false);
        if (lineFail < orcaLines.size()) {
            linearProgram3(orcaLines.size(), lineFail);
        }
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

    public OrcaLine getLine() {
        return orcaLines.get(0);
    }

    public DecimalPosition getNewVelocity() {
        return newVelocity;
    }

    /**
     * Solves a two-dimensional linear program subject to linear constraints
     * defined by orcaLines and a circular constraint.
     *
     * @param orcaLines            Lines defining the linear constraints.
     * @param optimizationVelocity The optimization velocity.
     * @param optimizeDirection    True if the direction should be optimized.
     * @return The number of the line on which it fails, or the number of orcaLines
     * if successful.
     */
    private int linearProgram2(List<OrcaLine> orcaLines, DecimalPosition optimizationVelocity, boolean optimizeDirection) {
        if (optimizeDirection) {
            // Optimize direction. Note that the optimization velocity is of unit length in this case.
            newVelocity = optimizationVelocity.multiply(maxSpeed);
        } else if (optimizationVelocity.magnitude() > maxSpeed) {
            // Optimize closest point and outside circle.
            newVelocity = optimizationVelocity.normalize(maxSpeed);
        } else {
            // Optimize closest point and inside circle.
            newVelocity = optimizationVelocity;
        }

        for (int lineNo = 0; lineNo < orcaLines.size(); lineNo++) {
            if (orcaLines.get(lineNo).getDirection().determinant(orcaLines.get(lineNo).getPoint().sub(newVelocity)) > 0.0) {
                // Result does not satisfy constraint i. Compute new optimal result.
                DecimalPosition tempResult = newVelocity;
                if (!linearProgram1(orcaLines, lineNo, optimizationVelocity, optimizeDirection)) {
                    newVelocity = tempResult;

                    return lineNo;
                }
            }
        }


        return orcaLines.size();
    }

    /**
     * Solves a one-dimensional linear program on a specified line subject to
     * linear constraints defined by orcaLines and a circular constraint.
     *
     * @param orcaLines            Lines defining the linear constraints.
     * @param lineNo               The specified line constraint.
     * @param optimizationVelocity The optimization velocity.
     * @param optimizeDirection    True if the direction should be optimized.
     * @return True if successful.
     */
    private boolean linearProgram1(List<OrcaLine> orcaLines, int lineNo, DecimalPosition optimizationVelocity, boolean optimizeDirection) {
        final double dotProduct = orcaLines.get(lineNo).getPoint().dotProduct(orcaLines.get(lineNo).getDirection());
        final double discriminant = dotProduct * dotProduct + maxSpeed * maxSpeed - orcaLines.get(lineNo).getPoint().magnitude() * orcaLines.get(lineNo).getPoint().magnitude();

        if (discriminant < 0.0) {
            // Max speed circle fully invalidates line lineNo.
            return false;
        }

        double sqrtDiscriminant = Math.sqrt(discriminant);
        double tLeft = -sqrtDiscriminant - dotProduct;
        double tRight = sqrtDiscriminant - dotProduct;

        for (int i = 0; i < lineNo; i++) {
            double denominator = orcaLines.get(lineNo).getDirection().determinant(orcaLines.get(i).getDirection());
            double numerator = orcaLines.get(i).getDirection().determinant(orcaLines.get(lineNo).getPoint().sub(orcaLines.get(i).getPoint()));

            if (Math.abs(denominator) <= EPSILON) {
                // Lines lineNo and i are (almost) parallel.
                if (numerator < 0.0) {
                    return false;
                }

                continue;
            }

            final double t = numerator / denominator;

            if (denominator >= 0.0) {
                // Line i bounds line lineNo on the right.
                tRight = Math.min(tRight, t);
            } else {
                // Line i bounds line lineNo on the left.
                tLeft = Math.max(tLeft, t);
            }

            if (tLeft > tRight) {
                return false;
            }
        }

        if (optimizeDirection) {
            // Optimize direction.
            if (optimizationVelocity.dotProduct(orcaLines.get(lineNo).getDirection()) > 0.0) {
                // Take right extreme.
                newVelocity = orcaLines.get(lineNo).getPoint().add(tRight, orcaLines.get(lineNo).getDirection());
            } else {
                // Take left extreme.
                newVelocity = orcaLines.get(lineNo).getPoint().add(tLeft, orcaLines.get(lineNo).getDirection());
            }
        } else {
            // Optimize closest point.
            final double t = orcaLines.get(lineNo).getDirection().dotProduct(optimizationVelocity.sub(orcaLines.get(lineNo).getPoint()));

            if (t < tLeft) {
                newVelocity = orcaLines.get(lineNo).getPoint().add(tLeft, orcaLines.get(lineNo).getDirection());
            } else if (t > tRight) {
                newVelocity = orcaLines.get(lineNo).getPoint().add(tRight, orcaLines.get(lineNo).getDirection());
            } else {
                newVelocity = orcaLines.get(lineNo).getPoint().add(t, orcaLines.get(lineNo).getDirection());
            }
        }

        return true;
    }

    /**
     * Solves a two-dimensional linear program subject to linear constraints
     * defined by lines and a circular constraint.
     *
     * @param numObstacleLines Count of obstacle lines.
     * @param beginLine        The line on which the 2-D linear program failed.
     */
    private void linearProgram3(int numObstacleLines, int beginLine) {
        double distance = 0.0;

        for (int i = beginLine; i < orcaLines.size(); i++) {
            if (orcaLines.get(i).getDirection().determinant(orcaLines.get(i).getPoint().sub(newVelocity)) > distance) {
                // Result does not satisfy constraint of line i.
                final List<OrcaLine> projectedLines = new ArrayList<>(numObstacleLines);
                for (int j = 0; j < numObstacleLines; j++) {
                    projectedLines.add(orcaLines.get(j));
                }

                for (int j = numObstacleLines; j < i; j++) {
                    final double determinant = orcaLines.get(i).getDirection().determinant(orcaLines.get(j).getDirection());
                    final DecimalPosition point;

                    if (Math.abs(determinant) <= EPSILON) {
                        // Line i and line j are parallel.
                        if (orcaLines.get(i).getDirection().dotProduct(orcaLines.get(j).getDirection()) > 0.0) {
                            // Line i and line j point in the same direction.
                            continue;
                        }

                        // Line i and line j point in opposite direction.
                        point = orcaLines.get(i).getPoint().add(orcaLines.get(j).getPoint()).multiply(0.5);
                    } else {
                        point = orcaLines.get(i).getPoint().add(orcaLines.get(i).getDirection().multiply(orcaLines.get(j).getDirection().determinant(orcaLines.get(i).getPoint().sub(orcaLines.get(j).getPoint())) / determinant));
                    }

                    DecimalPosition direction = orcaLines.get(j).getDirection().sub(orcaLines.get(i).getDirection()).normalize();
                    projectedLines.add(new OrcaLine(point, direction));
                }

                DecimalPosition tempResult = newVelocity;
                if (linearProgram2(projectedLines, new DecimalPosition(-orcaLines.get(i).getDirection().getY(), orcaLines.get(i).getDirection().getX()), true) < projectedLines.size()) {
                    // This should in principle not happen. The result is by
                    // definition already in the feasible region of this linear
                    // program. If it fails, it is due to small floating point
                    // error, and the current result is kept.
                    newVelocity = tempResult;
                }

                distance = orcaLines.get(i).getDirection().determinant(orcaLines.get(i).getPoint().sub(newVelocity));
            }
        }
    }

}
