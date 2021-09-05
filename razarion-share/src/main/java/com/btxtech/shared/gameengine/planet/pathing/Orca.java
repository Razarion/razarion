package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.btxtech.shared.gameengine.planet.PlanetService.TICK_FACTOR;

/**
 * Created by Beat
 * on 29.05.2018.
 */
// http://gamma.cs.unc.edu/ORCA/
// Reciprocal n-body Collision Avoidance
public class Orca {
    public static final double TIME_HORIZON_ITEMS = 0.5;
    public static final double TIME_HORIZON_OBSTACLES = 0.5; // Do not make bigger, it becomes unstable
    public static final double EPSILON = 0.00001;
    // private static final Logger LOGGER = Logger.getLogger(Orca.class.getName());
    private final SyncPhysicalMovable syncPhysicalMovable;
    private final DecimalPosition position;
    private double radius;
    private DecimalPosition preferredVelocity;
    private DecimalPosition newVelocity;
    private double speed;
    private List<OrcaLine> itemOrcaLines = new ArrayList<>();
    private List<OrcaLine> obstacleOrcaLines = new ArrayList<>();
    private List<ObstacleSlope> debugObstacles_WRONG = new ArrayList<>();
    private List<OrcaLine> orcaLines;


//    TODO still two problems:
//    - Push away not working over network (order & movable gets pushed away) & reqursively
//    - Jumping especially near obstacles

    public Orca(SyncPhysicalMovable syncPhysicalMovable) {
        this.syncPhysicalMovable = syncPhysicalMovable;
        position = syncPhysicalMovable.getPosition2d();
        radius = syncPhysicalMovable.getRadius();
        preferredVelocity = DecimalPosition.zeroIfNull(syncPhysicalMovable.getPreferredVelocity());
        speed = preferredVelocity.magnitude();
        //DebugHelperStatic.addOrcaCreate(syncPhysicalMovable);
    }

    public void add(SyncPhysicalArea other) {
        DecimalPosition otherVelocity = null;
        double reciprocalFactor = 1.0;
        if (other instanceof SyncPhysicalMovable) {
            otherVelocity = ((SyncPhysicalMovable) other).getPreferredVelocity();
            if (otherVelocity != null) {
                DecimalPosition relativePosition = position.sub(other.getPosition2d());
                if (relativePosition.normalize().dotProduct(preferredVelocity.normalize()) > 0.0) {
                    // Ignore pusher from directly behind. We can't do anything against them.
                    return;
                }
                reciprocalFactor = 0.5;
            }
        }
        addOrcaLine(other.getPosition2d(), otherVelocity, other.getRadius(), reciprocalFactor);
    }

    public void add(ObstacleTerrainObject obstacleTerrainObject) {
        addOrcaLine(obstacleTerrainObject.getCircle().getCenter(), null, obstacleTerrainObject.getCircle().getRadius(), 1.0);
    }

    private void addOrcaLine(DecimalPosition otherPosition, DecimalPosition otherVelocity, double otherRadius, double reciprocalFactor) {
        DecimalPosition relativePosition = otherPosition.sub(position);
        DecimalPosition preferredVelocity = DecimalPosition.zeroIfNull(syncPhysicalMovable.getPreferredVelocity());
        DecimalPosition relativeVelocity = preferredVelocity.sub(DecimalPosition.zeroIfNull(otherVelocity));
        double distanceSq = relativePosition.magnitudeSq();
        double combinedRadius = radius + otherRadius;
        double combinedRadiusSq = combinedRadius * combinedRadius;


        OrcaLine orcaLine;
        if (distanceSq >= combinedRadiusSq) {
            // No collision.
            DecimalPosition u;
            DecimalPosition direction;

            DecimalPosition w = relativeVelocity.sub(relativePosition.divide(TIME_HORIZON_ITEMS));

            // Vector from cutoff center to relative velocity.
            double wLengthSq = w.magnitude() * w.magnitude();
            double dotProduct1 = w.dotProduct(relativePosition);

            if (dotProduct1 < 0.0 && dotProduct1 * dotProduct1 > combinedRadiusSq * wLengthSq) {
                // Project on cut-off circle.
                double wLength = Math.sqrt(wLengthSq);
                DecimalPosition unitW = w.divide(wLength);

                direction = new DecimalPosition(unitW.getY(), -unitW.getX()); // Rotate -90deg (clockwise)
                u = unitW.multiply(combinedRadius / TIME_HORIZON_ITEMS - wLength);
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
            DecimalPosition point = preferredVelocity.add(reciprocalFactor, u);
            orcaLine = new OrcaLine(point, direction);
        } else {
            // Collision
            double overlapDistance = combinedRadius - relativePosition.magnitude();
            DecimalPosition overlapCorrection = position.sub(otherPosition).normalize(overlapDistance / TICK_FACTOR); // Make sure the collision is eliminated next tick
            DecimalPosition point = preferredVelocity.add(overlapCorrection.sub(relativeVelocity).multiply(reciprocalFactor));
            point = point.normalize(Math.min(speed, point.magnitude()));
            DecimalPosition direction = new DecimalPosition(-relativePosition.getY(), relativePosition.getX()).normalize();
            orcaLine = new OrcaLine(point, direction);
        }
        orcaLine.setRelativeVelocity(relativeVelocity);
        orcaLine.setRelativePosition(relativePosition);
        orcaLine.setCombinedRadius(combinedRadius);
        itemOrcaLines.add(orcaLine);
    }

    public void add(ObstacleSlope obstacleSlope) {
//        DebugHelperStatic.addOrcaAdd(obstacleSlope);
        // remove if wrong side of obstacle line -> Backface Culling
        if (obstacleSlope.isOutside(position)) {
            return;
        }

        double invTimeHorizonObstacle = 1.0 / TIME_HORIZON_OBSTACLES;

        DecimalPosition relativePosition1 = obstacleSlope.getPoint1().sub(position);
        DecimalPosition relativePosition2 = obstacleSlope.getPoint2().sub(position);

        // Check if velocity obstacle of obstacle is already taken care of by previously constructed obstacle ORCA lines.
        for (OrcaLine orcaLine : obstacleOrcaLines) {
            if (relativePosition1.multiply(invTimeHorizonObstacle).sub(orcaLine.getPoint()).determinant(orcaLine.getDirection()) - invTimeHorizonObstacle * radius >= -EPSILON && relativePosition2.multiply(invTimeHorizonObstacle).sub(orcaLine.getPoint()).determinant(orcaLine.getDirection()) - invTimeHorizonObstacle * radius >= -EPSILON) {
                return;
            }
        }

        // Not yet covered. Check for collisions.
        double distanceSq1 = relativePosition1.magnitudeSq();
        double distanceSq2 = relativePosition2.magnitudeSq();
        double radiusSq = radius * radius;

        DecimalPosition obstacleVector = obstacleSlope.getPoint2().sub(obstacleSlope.getPoint1());
        double s = -relativePosition1.dotProduct(obstacleVector) / obstacleVector.magnitudeSq(); // Projection on obstacleVector (unit vector)
        double distanceSqLine = Math.pow(relativePosition1.add(s, obstacleVector).magnitude(), 2.0); // Distance nearest point on line

        if (s < 0.0 && distanceSq1 <= radiusSq) {
            // Collision with left vertex. Ignore if non-convex.
            if (obstacleSlope.isPoint1Convex()) {
                DecimalPosition direction = new DecimalPosition(-relativePosition1.getY(), relativePosition1.getX()).normalize();
                obstacleOrcaLines.add(new OrcaLine(DecimalPosition.NULL, direction));
                // debugObstacles_WRONG.add(obstacleSlope);
            }
            return;
        }

        if (s > 1.0 && distanceSq2 <= radiusSq) {
            // Collision with right vertex. Ignore if non-convex or if it will be taken care of by neighboring obstacle.
            if (obstacleSlope.isPoint2Convex() && relativePosition2.determinant(obstacleSlope.getPoint2Direction()) >= 0.0) {
                DecimalPosition direction = new DecimalPosition(-relativePosition2.getY(), relativePosition2.getX()).normalize();
                obstacleOrcaLines.add(new OrcaLine(DecimalPosition.NULL, direction));
                // debugObstacles_WRONG.add(obstacleSlope);
            }
            return;
        }

        if (s >= 0.0 && s < 1.0 && distanceSqLine <= radiusSq) {
            // Collision with obstacle segment.
            DecimalPosition direction = obstacleSlope.getPoint1Direction().negate();
            obstacleOrcaLines.add(new OrcaLine(DecimalPosition.NULL, direction));
            //debugObstacles_WRONG.add(obstacleSlope);
            return;
        }

        // No collision. Compute legs. When obliquely viewed, both legs can come from a single vertex. Legs extend cut-off line when non-convex vertex.
        DecimalPosition leftLegDirection;
        DecimalPosition rightLegDirection;

        boolean obstacle1Convex = obstacleSlope.isPoint1Convex();
        boolean obstacle2Convex = obstacleSlope.isPoint2Convex();
        DecimalPosition obstacle1Point = obstacleSlope.getPoint1();
        DecimalPosition obstacle2Point = obstacleSlope.getPoint2();
        DecimalPosition obstacle1Direction = obstacleSlope.getPoint1Direction();
        DecimalPosition obstacle1PreviousDirection = obstacleSlope.getPreviousDirection(); // todo
        DecimalPosition obstacle2Direction = obstacleSlope.getPoint2Direction();
        boolean obstacle1EqualsObstacle2 = false;

        if (s < 0.0 && distanceSqLine <= radiusSq) {
            // Obstacle viewed obliquely so that left vertex defines velocity obstacle.
            if (!obstacleSlope.isPoint1Convex()) {
                // Ignore obstacle.
                return;
            }

            obstacle2Convex = obstacleSlope.isPoint1Convex();
            obstacle2Point = obstacleSlope.getPoint1();
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
            obstacle1Point = obstacleSlope.getPoint2();
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
                leftLegDirection = obstacleSlope.getPoint1Direction().negate();
            }

            if (obstacleSlope.isPoint2Convex()) {
                double leg2 = Math.sqrt(distanceSq2 - radiusSq);
                rightLegDirection = new DecimalPosition(relativePosition2.getX() * leg2 + relativePosition2.getY() * radius, -relativePosition2.getX() * radius + relativePosition2.getY() * leg2).multiply(1.0 / distanceSq2);
            } else {
                // Right vertex non-convex; right leg extends cut-off line.
                rightLegDirection = obstacleSlope.getPoint1Direction();
            }
        }

        // Legs can never point into neighboring edge when convex vertex, take cut-off line of neighboring edge instead. If velocity projected on "foreign" leg, no constraint is added.
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
        DecimalPosition velocity = DecimalPosition.zeroIfNull(syncPhysicalMovable.getPreferredVelocity());

        double t = obstacle1EqualsObstacle2 ? 0.5 : velocity.sub(leftCutOff).dotProduct(cutOffVector) / cutOffVector.magnitudeSq();
        double tLeft = velocity.sub(leftCutOff).dotProduct(leftLegDirection);
        double tRight = velocity.sub(rightCutOff).dotProduct(rightLegDirection);

        if (t < 0.0 && tLeft < 0.0 || obstacle1EqualsObstacle2 && tLeft < 0.0 && tRight < 0.0) {
            // Project on left cut-off circle.
            DecimalPosition unitW = velocity.sub(leftCutOff).normalize();

            DecimalPosition direction = new DecimalPosition(unitW.getY(), -unitW.getX());
            DecimalPosition point = leftCutOff.add(radius * invTimeHorizonObstacle, unitW);
            OrcaLine orcaLine = new OrcaLine(point, direction);
            obstacleOrcaLines.add(orcaLine);
            debugObstacles_WRONG.add(new ObstacleSlope(obstacle1Point, obstacle1Point.getPointWithDistance(10, obstacle1Point.add(obstacle1Direction), true), DecimalPosition.NULL, DecimalPosition.NULL));
            return;
        }

        if (t > 1.0 && tRight < 0.0) {
            // Project on right cut-off circle.
            DecimalPosition unitW = velocity.sub(rightCutOff).normalize();

            DecimalPosition direction = new DecimalPosition(unitW.getY(), -unitW.getX());
            DecimalPosition point = rightCutOff.add(radius * invTimeHorizonObstacle, unitW);
            OrcaLine orcaLine = new OrcaLine(point, direction);
            obstacleOrcaLines.add(orcaLine);
            debugObstacles_WRONG.add(new ObstacleSlope(obstacle1Point, obstacle1Point.getPointWithDistance(10, obstacle1Point.add(obstacle1Direction), true), DecimalPosition.NULL, DecimalPosition.NULL));
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
            obstacleOrcaLines.add(orcaLine);
            debugObstacles_WRONG.add(new ObstacleSlope(obstacle1Point, obstacle1Point.getPointWithDistance(10, obstacle1Point.add(obstacle1Direction), true), DecimalPosition.NULL, DecimalPosition.NULL));
            return;
        }

        if (distanceSqLeft <= distanceSqRight) {
            // Project on left leg.
            if (leftLegForeign) {
                return;
            }

            DecimalPosition point = leftCutOff.add(radius * invTimeHorizonObstacle, new DecimalPosition(-leftLegDirection.getY(), leftLegDirection.getX()));
            OrcaLine orcaLine = new OrcaLine(point, leftLegDirection);
            obstacleOrcaLines.add(orcaLine);
            debugObstacles_WRONG.add(new ObstacleSlope(obstacle1Point, obstacle1Point.getPointWithDistance(10, obstacle1Point.add(obstacle1Direction), true), DecimalPosition.NULL, DecimalPosition.NULL));
            return;
        }

        // Project on right leg.
        if (rightLegForeign) {
            return;
        }

        DecimalPosition direction = rightLegDirection.negate();
        DecimalPosition point = rightCutOff.add(radius / TIME_HORIZON_OBSTACLES, new DecimalPosition(-direction.getY(), direction.getX()));
        OrcaLine orcaLine = new OrcaLine(point, direction);
        obstacleOrcaLines.add(orcaLine);
        debugObstacles_WRONG.add(new ObstacleSlope(obstacle1Point, obstacle1Point.getPointWithDistance(10, obstacle1Point.add(obstacle1Direction), true), DecimalPosition.NULL, DecimalPosition.NULL));
    }

    public DecimalPosition getNewVelocity() {
        return newVelocity;
    }

    public boolean isEmpty() {
        return itemOrcaLines.isEmpty() && obstacleOrcaLines.isEmpty();
    }

    public List<OrcaLine> getItemOrcaLines() {
        return itemOrcaLines;
    }

    public List<OrcaLine> getObstacleOrcaLines() {
        return obstacleOrcaLines;
    }

    public List<OrcaLine> getOrcaLines() {
        List<OrcaLine> orcaLines = new ArrayList<>();
        orcaLines.addAll(itemOrcaLines);
        orcaLines.addAll(obstacleOrcaLines);
        return orcaLines;
    }

    public void implementVelocity() {
        if (newVelocity != null) {
            syncPhysicalMovable.setVelocity(newVelocity);
        }
    }

    public void solve() {
        orcaLines = getOrcaLines();
        newVelocity = linearProgram();
    }

    private DecimalPosition linearProgram() {
        // 1) If preferredVelocity does not violate any OrcaLine
        if (orcaLines.stream().allMatch(orcaLine -> orcaLine.isVelocityAllowed(preferredVelocity))) {
            return preferredVelocity;
        }

        syncPhysicalMovable.setCrowded();

        // 2) If a simple intersection between OrcaLine and speed is possible
        List<DecimalPosition> intersections = new ArrayList<>();
        orcaLines.forEach(orcaLine -> {
            Collection<DecimalPosition> possibleVelocities = orcaLine.toLine().circleLineIntersection(speed);
            if (possibleVelocities == null) {
                return;
            }
            DecimalPosition possibleVelocity = DecimalPosition.getNearestPoint(preferredVelocity, possibleVelocities);
            if (isVelocityAllowed(possibleVelocity, orcaLine)) {
                intersections.add(possibleVelocity);
            }
        });
        if (!intersections.isEmpty()) {
            return DecimalPosition.getNearestPoint(preferredVelocity, intersections);
        }

        //  3) Find best solution
        List<DecimalPosition> possibleVelocities = new ArrayList<>();
        for (int i = 0; i < orcaLines.size(); i++) {
            OrcaLine orcaLine = orcaLines.get(i);
            Line line = orcaLine.toLine();
            for (int j = i + 1; j < orcaLines.size(); j++) {
                OrcaLine otherOrcaLine = orcaLines.get(j);
                Line otherLine = otherOrcaLine.toLine();
                DecimalPosition possibleVelocity = line.getCrossInfinite(otherLine);
                if (possibleVelocity != null) {
                    if (possibleVelocity.magnitude() <= speed) {
                        if (isVelocityAllowed(possibleVelocity, orcaLine, otherOrcaLine)) {
                            possibleVelocities.add(possibleVelocity);
                        }
                    } else {
                        DecimalPosition correctedPossibleVelocity = possibleVelocity.normalize(speed);
                        if (isVelocityAllowed(correctedPossibleVelocity)) {
                            possibleVelocities.add(correctedPossibleVelocity);
                        }
                    }
                }
            }
        }

        if (!possibleVelocities.isEmpty()) {
            return DecimalPosition.getNearestPoint(preferredVelocity, possibleVelocities);
        }

        return DecimalPosition.NULL;
    }

    private boolean isVelocityAllowed(DecimalPosition velocity, OrcaLine... ignoredLines) {
        Set<OrcaLine> ignored = new HashSet<>();
        Collections.addAll(ignored, ignoredLines);
        return orcaLines.stream().allMatch(ol -> ignored.contains(ol) || ol.isVelocityAllowed(velocity));
    }

    public List<ObstacleSlope> getDebugObstacles_WRONG() {
        return debugObstacles_WRONG;
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public SyncPhysicalMovable getSyncPhysicalMovable() {
        return syncPhysicalMovable;
    }
}
