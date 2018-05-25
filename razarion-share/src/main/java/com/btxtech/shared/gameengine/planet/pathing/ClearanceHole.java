package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 16.05.2016.
 */
@Deprecated
public class ClearanceHole {
    private Logger logger = Logger.getLogger(ClearanceHole.class.getName());
    private SyncPhysicalMovable syncPhysicalMovable;
    private List<AngleSegment> angleSegments = new ArrayList<>();

    public ClearanceHole(SyncPhysicalMovable syncPhysicalMovable) {
        this.syncPhysicalMovable = syncPhysicalMovable;
    }

    public void addOther(SyncPhysicalArea other) {
        addCircle(other.getPosition2d(), other.getRadius());
    }

    public void addOther(Obstacle obstacle) {
        // Bad solution if slope line is piercing
        if (obstacle instanceof ObstacleSlope) {
            ObstacleSlope obstacleSlope = (ObstacleSlope) obstacle;
            DecimalPosition point1;
            DecimalPosition point2;
            if (MathHelper.isCounterClock(syncPhysicalMovable.getPosition2d().getAngle(obstacleSlope.getLine().getPoint1()), syncPhysicalMovable.getPosition2d().getAngle(obstacleSlope.getLine().getPoint2()))) {
                point1 = obstacleSlope.getLine().getPoint1();
                point2 = obstacleSlope.getLine().getPoint2();
            } else {
                point1 = obstacleSlope.getLine().getPoint2();
                point2 = obstacleSlope.getLine().getPoint1();
            }

            double circleAngle1;
            double distance1 = syncPhysicalMovable.getPosition2d().getDistance(point1);
            if (distance1 > syncPhysicalMovable.getRadius()) {
                circleAngle1 = Math.atan(syncPhysicalMovable.getRadius() / distance1);
            } else {
                circleAngle1 = MathHelper.QUARTER_RADIANT;
            }
            double circleAngle2;
            double distance2 = syncPhysicalMovable.getPosition2d().getDistance(point2);
            if (distance2 > syncPhysicalMovable.getRadius()) {
                circleAngle2 = Math.atan(syncPhysicalMovable.getRadius() / distance2);
            } else {
                circleAngle2 = MathHelper.QUARTER_RADIANT;
            }

            double startAngle = MathHelper.normaliseAngle(syncPhysicalMovable.getPosition2d().getAngle(point1) - circleAngle1);
            double endAngle = MathHelper.normaliseAngle(syncPhysicalMovable.getPosition2d().getAngle(point2) + circleAngle2);
            double half = MathHelper.getAngle(startAngle, endAngle) / 2.0;
            double middle = MathHelper.normaliseAngle(startAngle + half);
            angleSegments.add(new AngleSegment(middle, half));
        } else if (obstacle instanceof ObstacleTerrainObject) {
            ObstacleTerrainObject obstacleTerrainObject = (ObstacleTerrainObject) obstacle;
            addCircle(obstacleTerrainObject.getCircle().getCenter(), obstacleTerrainObject.getCircle().getRadius());
        } else {
            logger.warning("ClearanceHole.addOther() Unknown obstacle: " + obstacle);
        }
    }

    private void addCircle(DecimalPosition center, double radius) {
        DecimalPosition distanceVector = center.sub(syncPhysicalMovable.getPosition2d());
        double distance = distanceVector.magnitude();
        double totalRadius = syncPhysicalMovable.getRadius() + radius;
        double halfBlockingAngle;
        if (totalRadius < distance) {
            halfBlockingAngle = Math.asin(totalRadius / distance);
        } else {
            halfBlockingAngle = Math.PI / 2.0;
        }
        angleSegments.add(new AngleSegment(distanceVector.angle(), halfBlockingAngle));
    }

    public double getFreeAngle(double desiredAngle) {
        List<AngleSegment> blockingSegments = mergeSegments();
        if (blockingSegments.isEmpty()) {
            return desiredAngle;
        }
        for (AngleSegment blockingSegment : blockingSegments) {
            if (blockingSegment.isInside(desiredAngle)) {
                return blockingSegment.getNearestSide(desiredAngle);
            }
        }
        return desiredAngle;
    }

    private List<AngleSegment> mergeSegments() {
        List<AngleSegment> angleSegmentClone = new ArrayList<>(angleSegments);
        List<AngleSegment> mergedSegments = new ArrayList<>();

        if (angleSegmentClone.isEmpty()) {
            return mergedSegments;
        }
        AngleSegment angleSegment = angleSegmentClone.remove(0);
        while (angleSegment != null) {
            boolean keepSearching = true;
            while (keepSearching) {
                keepSearching = false;
                for (Iterator<AngleSegment> iterator = angleSegmentClone.iterator(); iterator.hasNext(); ) {
                    AngleSegment segment = iterator.next();
                    if (angleSegment.overlaps(segment)) {
                        angleSegment = angleSegment.combines(segment);
                        iterator.remove();
                        keepSearching = true;
                        break;
                    }
                }
            }
            mergedSegments.add(angleSegment);
            if (angleSegmentClone.isEmpty()) {
                angleSegment = null;
            } else {
                angleSegment = angleSegmentClone.remove(0);
            }
        }
        return mergedSegments;
    }

    public static class AngleSegment {
        private double middle;
        private double half;

        public AngleSegment(double middle, double half) {
            this.middle = middle;
            this.half = half;
        }

        public boolean overlaps(AngleSegment segment) {
            double deltaMiddle = MathHelper.getAngle(middle, segment.middle);
            return deltaMiddle - half - segment.half < 0;
        }

        public AngleSegment combines(AngleSegment segment) {
            double side1 = Math.min(middle - half, segment.middle - segment.half);
            double side2 = Math.max(middle + half, segment.middle + segment.half);
            double newHalf = MathHelper.getAngle(side2, side1) / 2.0;
            return new AngleSegment(MathHelper.negateAngle(side1 + newHalf), newHalf);
        }

        public boolean isInside(double angle) {
            double delta = MathHelper.getAngle(middle, angle);
            return delta < half;
        }

        public double getNearestSide(double angle) {
            double angle1 = MathHelper.negateAngle(middle + half);
            double angle2 = MathHelper.negateAngle(middle - half);

            if (MathHelper.getAngle(angle, angle1) < MathHelper.getAngle(angle, angle2)) {
                return angle1;
            } else {
                return angle2;
            }
        }

        @Override
        public String toString() {
            return "AngleSegment{" + "middle=" + Math.toDegrees(middle) +
                    " half=" + Math.toDegrees(half) +
                    '}';
        }
    }
}
