package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 29.05.2018.
 */
// http://gamma.cs.unc.edu/ORCA/
public class Orca {
    public static final double TIME_HORIZON_ITEMS = 2;
    public static final double TIME_HORIZON_OBSTACLES = 2;
    public static final double EPSILON = 0.00001;
    private SyncPhysicalMovable syncPhysicalMovable;
    private DecimalPosition position;
    private double radius;
    private DecimalPosition preferredVelocity;
    private DecimalPosition newVelocity;
    private double maxSpeed;
    private List<OrcaLine> orcaLines = new ArrayList<>();

    public Orca(SyncPhysicalMovable syncPhysicalMovable) {
        this.syncPhysicalMovable = syncPhysicalMovable;
        position = syncPhysicalMovable.getPosition2d();
        radius = syncPhysicalMovable.getRadius();
        preferredVelocity = syncPhysicalMovable.getPreferredVelocity();
        maxSpeed = syncPhysicalMovable.getPreferredVelocity().magnitude();
    }

    public void add(SyncPhysicalMovable other) {
        DecimalPosition relativePosition = other.getPosition2d().sub(position);
        DecimalPosition relativeVelocity = DecimalPosition.zeroIfNull(syncPhysicalMovable.getVelocity()).sub(DecimalPosition.zeroIfNull(other.getVelocity()));
        double distanceSq = relativePosition.magnitudeSq();
        double combinedRadius = radius + other.getRadius();
        double combinedRadiusSq = combinedRadius * combinedRadius;

        DecimalPosition u;
        DecimalPosition direction;

        if (distanceSq > combinedRadiusSq) {
            // No collision.
            DecimalPosition w = relativeVelocity.sub(relativePosition.divide(TIME_HORIZON_ITEMS));

            // Vector from cutoff center to relative velocity.
            double wLengthSq = w.magnitude() * w.magnitude();
            double dotProduct1 = w.dotProduct(relativePosition);

            if (dotProduct1 < 0.0 && dotProduct1 * dotProduct1 > combinedRadiusSq * wLengthSq) {
                DebugHelperStatic.add2printOnTick("\n No collision cut-off circle: "/* + other.getSyncItem().getId()*/);
                // Project on cut-off circle.
                double wLength = Math.sqrt(wLengthSq);
                DecimalPosition unitW = w.divide(wLength);

                direction = new DecimalPosition(unitW.getY(), -unitW.getX()); // Rotate -90deg (clockwise)
                u = unitW.multiply(combinedRadius / TIME_HORIZON_ITEMS - wLength);
            } else {
                DebugHelperStatic.add2printOnTick("\n No collision legs: "/* + other.getSyncItem().getId()*/);
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
            DebugHelperStatic.add2printOnTick("\n Collision: "/* + other.getSyncItem().getId()*/);
            // Collision. Project on cut-off circle of time timeStep.

            // Vector from cutoff center to relative velocity.
            DecimalPosition w = relativeVelocity.sub(relativePosition.multiply(PlanetService.TICKS_PER_SECONDS));

            double wLength = w.magnitude();
            DecimalPosition unitW = w.multiply(1.0 / wLength);

            direction = new DecimalPosition(unitW.getY(), -unitW.getX());
            u = unitW.multiply(combinedRadius * PlanetService.TICKS_PER_SECONDS - wLength);
        }
        DecimalPosition point = DecimalPosition.zeroIfNull(syncPhysicalMovable.getVelocity()).add(0.5, u);
        OrcaLine orcaLine = new OrcaLine(point, direction);
        orcaLine.setRelativeVelocity(relativeVelocity);
        orcaLine.setRelativePosition(relativePosition);
        orcaLine.setCombinedRadius(combinedRadius);
        orcaLine.setU(u);
        orcaLines.add(orcaLine);
    }

    public void add(ObstacleSlope obstacleSlope) {
        double invTimeHorizonObstacle = 1.0 / TIME_HORIZON_OBSTACLES;

        DecimalPosition relativePosition1 = obstacleSlope.getLine().getPoint1().sub(position);
        DecimalPosition relativePosition2 = obstacleSlope.getLine().getPoint2().sub(position);

//                // Check if velocity obstacle of obstacle is already taken care of
//                // by previously constructed obstacle ORCA lines.
//                boolean alreadyCovered = false;
//
//                for (final Line orcaLine : lines) {
//                    if (RVOMath.det(relativePosition1.scalarMultiply(invTimeHorizonObstacle).subtract(orcaLine.point), orcaLine.direction) - invTimeHorizonObstacle * radius >= -RVOMath.EPSILON && RVOMath.det(relativePosition2.scalarMultiply(invTimeHorizonObstacle).subtract(orcaLine.point), orcaLine.direction) - invTimeHorizonObstacle * radius >= -RVOMath.EPSILON) {
//                        alreadyCovered = true;
//
//                        break;
//                    }
//                }
//
//                if (alreadyCovered) {
//                    continue;
//                }
//
//                // Not yet covered. Check for collisions.
        double distanceSq1 = relativePosition1.magnitudeSq();
        double distanceSq2 = relativePosition2.magnitudeSq();
        double radiusSq = radius * radius;

        DecimalPosition obstacleVector = obstacleSlope.getLine().getPoint2().sub(obstacleSlope.getLine().getPoint1());
        double s = -relativePosition1.dotProduct(obstacleVector) / obstacleVector.magnitudeSq(); // Projection on obstacleVector (unit vector)
        double distanceSqLine = Math.pow(relativePosition1.add(s, obstacleVector).magnitude(), 2.0); // Distance nearest point on line
//
//                if (s < 0.0 && distanceSq1 <= radiusSq) {
//                    // Collision with left vertex. Ignore if non-convex.
//                    if (obstacle1.convex) {
//                        final Vector2D direction = new Vector2D(-relativePosition1.getY(), relativePosition1.getX()).normalize();
//                        lines.add(new Line(Vector2D.ZERO, direction));
//                    }
//
//                    continue;
//                }
//
//                if (s > 1.0 && distanceSq2 <= radiusSq) {
//                    // Collision with right vertex. Ignore if non-convex or if it
//                    // will be taken care of by neighboring obstacle.
//                    if (obstacle2.convex && RVOMath.det(relativePosition2, obstacle2.direction) >= 0.0) {
//                        final Vector2D direction = new Vector2D(-relativePosition2.getY(), relativePosition2.getX()).normalize();
//                        lines.add(new Line(Vector2D.ZERO, direction));
//                    }
//
//                    continue;
//                }
//
//                if (s >= 0.0 && s < 1.0 && distanceSqLine <= radiusSq) {
//                    // Collision with obstacle segment.
//                    final Vector2D direction = obstacle1.direction.negate();
//                    lines.add(new Line(Vector2D.ZERO, direction));
//
//                    continue;
//                }

        // No collision. Compute legs. When obliquely viewed, both legs can come from a single vertex. Legs extend cut-off line when non-convex vertex.
        DecimalPosition leftLegDirection;
        DecimalPosition rightLegDirection;

        boolean obstacle1Convex = obstacleSlope.isPoint1Convex();
        boolean obstacle2Convex = obstacleSlope.isPoint2Convex();
        DecimalPosition obstacle1Point = obstacleSlope.getLine().getPoint1();
        DecimalPosition obstacle2Point = obstacleSlope.getLine().getPoint2();
        DecimalPosition obstacle1Direction = obstacleSlope.setupDirection();
        DecimalPosition obstacle1PreviousDirection = obstacleSlope.setupPreviousDirection();
        DecimalPosition obstacle2Direction = obstacleSlope.setupNextDirection();
        boolean obstacle1EqualsObstacle2 = false;

        if (s < 0.0 && distanceSqLine <= radiusSq) {
            // Obstacle viewed obliquely so that left vertex defines velocity obstacle.
            if (!obstacleSlope.isPoint1Convex()) {
                // Ignore obstacle.
                return;
            }

            obstacle2Convex = obstacleSlope.isPoint1Convex();
            obstacle2Point = obstacleSlope.getLine().getPoint1();
            obstacle2Direction = obstacle1Direction;
            obstacle1EqualsObstacle2 = true;

            double leg1 = Math.sqrt(distanceSq1 - radiusSq);
            leftLegDirection = new DecimalPosition(relativePosition1.getX() * leg1 - relativePosition1.getY() * radius, relativePosition1.getX() * radius + relativePosition1.getY() * leg1).multiply(1.0 / distanceSq1);
            rightLegDirection = new DecimalPosition(relativePosition1.getX() * leg1 + relativePosition1.getY() * radius, -relativePosition1.getX() * radius + relativePosition1.getY() * leg1).multiply(1.0 / distanceSq1);
        } else if (s > 1.0 && distanceSqLine <= radiusSq) {
            // Obstacle viewed obliquely so that right vertex defines velocity obstacle.
            if (!obstacleSlope.isPoint2Convex()) {
                // Ignore obstacle.
                return;
            }

            obstacle1Convex = obstacleSlope.isPoint2Convex();
            obstacle1Point = obstacleSlope.getLine().getPoint2();
            obstacle1PreviousDirection = obstacle1Direction;
            obstacle1Direction = obstacle2Direction;
            obstacle1EqualsObstacle2 = true;

            double leg2 = Math.sqrt(distanceSq2 - radiusSq);
            leftLegDirection = new DecimalPosition(relativePosition2.getX() * leg2 - relativePosition2.getY() * radius, relativePosition2.getX() * radius + relativePosition2.getY() * leg2).multiply(1.0 / distanceSq2);
            rightLegDirection = new DecimalPosition(relativePosition2.getX() * leg2 + relativePosition2.getY() * radius, -relativePosition2.getX() * radius + relativePosition2.getY() * leg2).multiply(1.0 / distanceSq2);
        } else {
            // Usual situation.
            if (obstacleSlope.isPoint1Convex()) {
                double leg1 = Math.sqrt(distanceSq1 - radiusSq);
                leftLegDirection = new DecimalPosition(relativePosition1.getX() * leg1 - relativePosition1.getY() * radius, relativePosition1.getX() * radius + relativePosition1.getY() * leg1).multiply(1.0 / distanceSq1);
            } else {
                // Left vertex non-convex; left leg extends cut-off line.
                leftLegDirection = obstacleSlope.setupDirection().negate();
            }

            if (obstacleSlope.isPoint2Convex()) {
                double leg2 = Math.sqrt(distanceSq2 - radiusSq);
                rightLegDirection = new DecimalPosition(relativePosition2.getX() * leg2 + relativePosition2.getY() * radius, -relativePosition2.getX() * radius + relativePosition2.getY() * leg2).multiply(1.0 / distanceSq2);
            } else {
                // Right vertex non-convex; right leg extends cut-off line.
                rightLegDirection = obstacleSlope.setupDirection();
            }
        }

        // Legs can never point into neighboring edge when convex vertex,
        // take cut-off line of neighboring edge instead. If velocity
        // projected on "foreign" leg, no constraint is added.
        boolean leftLegForeign = false;
        boolean rightLegForeign = false;

        if (obstacle1Convex && leftLegDirection.determinant(obstacle1PreviousDirection.negate()) >= 0.0) {
            // Left leg points into obstacle.
            leftLegDirection = obstacle1PreviousDirection.negate();
            leftLegForeign = true;
        }

        if (obstacle2Convex && rightLegDirection.determinant(obstacle2Direction) <= 0.0) {
            // Right leg points into obstacle.
            rightLegDirection = obstacle2Direction;
            rightLegForeign = true;
        }

        // Compute cut-off centers.
        DecimalPosition leftCutOff = obstacle1Point.sub(position).multiply(invTimeHorizonObstacle);
        DecimalPosition rightCutOff = obstacle2Point.sub(position).multiply(invTimeHorizonObstacle);
        DecimalPosition cutOffVector = rightCutOff.sub(leftCutOff);

        // Project current velocity on velocity obstacle.

        // Check if current velocity is projected on cutoff circles.
        DecimalPosition velocity = DecimalPosition.zeroIfNull(syncPhysicalMovable.getVelocity());

        double t = obstacle1EqualsObstacle2 ? 0.5 : velocity.sub(leftCutOff).dotProduct(cutOffVector) / cutOffVector.magnitudeSq();
        double tLeft = velocity.sub(leftCutOff).dotProduct(leftLegDirection);
        double tRight = velocity.sub(rightCutOff).dotProduct(rightLegDirection);

        if (t < 0.0 && tLeft < 0.0 || obstacle1EqualsObstacle2 && tLeft < 0.0 && tRight < 0.0) {
            // Project on left cut-off circle.
            DecimalPosition unitW = velocity.sub(leftCutOff).normalize();

            DecimalPosition direction = new DecimalPosition(unitW.getY(), -unitW.getX());
            DecimalPosition point = leftCutOff.add(radius * invTimeHorizonObstacle, unitW);
            OrcaLine orcaLine = new OrcaLine(point, direction);
            orcaLines.add(orcaLine);
            return;
        }

        if (t > 1.0 && tRight < 0.0) {
            // Project on right cut-off circle.
            DecimalPosition unitW = velocity.sub(rightCutOff).normalize();

            DecimalPosition direction = new DecimalPosition(unitW.getY(), -unitW.getX());
            DecimalPosition point = rightCutOff.add(radius * invTimeHorizonObstacle, unitW);
            OrcaLine orcaLine = new OrcaLine(point, direction);
            orcaLines.add(orcaLine);
            return;
        }

        // Project on left leg, right leg, or cut-off line, whichever is
        // closest to velocity.
        double distanceSqCutOff = t < 0.0 || t > 1.0 || obstacle1EqualsObstacle2 ? Double.POSITIVE_INFINITY : velocity.getDistanceSq(leftCutOff.add(cutOffVector.multiply(t)));
        double distanceSqLeft = tLeft < 0.0 ? Double.POSITIVE_INFINITY : velocity.getDistanceSq(leftCutOff.add(leftLegDirection.multiply(tLeft)));
        double distanceSqRight = tRight < 0.0 ? Double.POSITIVE_INFINITY : velocity.getDistanceSq(rightCutOff.add(rightLegDirection.multiply(tRight)));

        if (distanceSqCutOff <= distanceSqLeft && distanceSqCutOff <= distanceSqRight) {
            // Project on cut-off line.
            DecimalPosition direction = obstacle1Direction.negate();
            DecimalPosition point = leftCutOff.add(radius * invTimeHorizonObstacle, new DecimalPosition(-direction.getY(), direction.getX()));
            OrcaLine orcaLine = new OrcaLine(point, direction);
            orcaLines.add(orcaLine);

            return;
        }

        if (distanceSqLeft <= distanceSqRight) {
            // Project on left leg.
            if (leftLegForeign) {
                return;
            }

            DecimalPosition point = leftCutOff.add(radius * invTimeHorizonObstacle, new DecimalPosition(-leftLegDirection.getY(), leftLegDirection.getX()));
            OrcaLine orcaLine = new OrcaLine(point, leftLegDirection);
            orcaLines.add(orcaLine);
            return;
        }

        // Project on right leg.
        if (rightLegForeign) {
            return;
        }

        DecimalPosition direction = rightLegDirection.negate();
        DecimalPosition point = rightCutOff.add(radius / TIME_HORIZON_OBSTACLES, new DecimalPosition(-direction.getY(), direction.getX()));
        OrcaLine orcaLine = new OrcaLine(point, direction);
        orcaLines.add(orcaLine);
    }

    public DecimalPosition getNewVelocity() {
        return newVelocity;
    }

    public boolean isEmpty() {
        return orcaLines.isEmpty();
    }

    public List<OrcaLine> getOrcaLines() {
        return orcaLines;
    }

    public void implementVelocity() {
        if (newVelocity != null) {
            syncPhysicalMovable.setVelocity(newVelocity);
        }
    }

    public void solve() {
        int lineFail = linearProgram2(orcaLines, preferredVelocity, false);
        if (lineFail < orcaLines.size()) {
            linearProgram3(orcaLines.size(), lineFail);
        }
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
                syncPhysicalMovable.setCrowded();
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
        double dotProduct = orcaLines.get(lineNo).getPoint().dotProduct(orcaLines.get(lineNo).getDirection());
        double discriminant = dotProduct * dotProduct + maxSpeed * maxSpeed - orcaLines.get(lineNo).getPoint().magnitude() * orcaLines.get(lineNo).getPoint().magnitude();

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

            double t = numerator / denominator;

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
            double t = orcaLines.get(lineNo).getDirection().dotProduct(optimizationVelocity.sub(orcaLines.get(lineNo).getPoint()));

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
                List<OrcaLine> projectedLines = new ArrayList<>(numObstacleLines);
                for (int j = 0; j < numObstacleLines; j++) {
                    projectedLines.add(orcaLines.get(j));
                }

                for (int j = numObstacleLines; j < i; j++) {
                    double determinant = orcaLines.get(i).getDirection().determinant(orcaLines.get(j).getDirection());
                    DecimalPosition point;

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
