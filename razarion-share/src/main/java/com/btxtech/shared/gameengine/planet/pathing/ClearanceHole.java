package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 16.05.2016.
 */
public class ClearanceHole {
    private SyncPhysicalMovable syncPhysicalMovable;
    private List<AngleSegment> angleSegments = new ArrayList<>();

    public ClearanceHole(SyncPhysicalMovable syncPhysicalMovable) {
        this.syncPhysicalMovable = syncPhysicalMovable;
    }

    public void addOther(SyncPhysicalArea other) {
        DecimalPosition distanceVector = other.getXYPosition().sub(syncPhysicalMovable.getXYPosition());
        double distance = distanceVector.magnitude();
        double radius = syncPhysicalMovable.getRadius() + other.getRadius();
        double halfBlockingAngle;
        if (radius < distance) {
            halfBlockingAngle = Math.asin(radius / distance);
        } else {
            halfBlockingAngle = Math.PI / 2.0;
        }
        angleSegments.add(new AngleSegment(distanceVector.angle(), halfBlockingAngle));
    }

    public double getFreeAngle(double desiredAngle) {
        List<AngleSegment> blockingSegments = findBlockingSegments();
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

    private List<AngleSegment> findBlockingSegments() {
        List<AngleSegment> angleSegmentClone = new ArrayList<>(angleSegments);
        List<AngleSegment> blockingSegments = new ArrayList<>();

        if (angleSegmentClone.isEmpty()) {
            return blockingSegments;
        }
        AngleSegment angleSegment = angleSegmentClone.remove(0);
        while (angleSegment != null) {
            boolean keepSearching = true;
            while (keepSearching) {
                keepSearching = false;
                for (AngleSegment segment : angleSegmentClone) {
                    if (angleSegment.overlaps(segment)) {
                        angleSegment = angleSegment.combines(segment);
                        angleSegmentClone.remove(0);
                        keepSearching = true;
                        break;
                    }
                }
            }
            blockingSegments.add(angleSegment);
            if (angleSegmentClone.isEmpty()) {
                angleSegment = null;
            } else {
                angleSegment = angleSegmentClone.remove(0);
            }
        }
        return blockingSegments;
    }

    public static class AngleSegment {
        private double middle;
        private double half;

        public AngleSegment(double middle, double half) {
            this.middle = middle;
            this.half = half;
        }

        public boolean overlaps(AngleSegment segment) {
            double deltaMiddle = MathHelper.getAngel(middle, segment.middle);
            return deltaMiddle - half - segment.half < 0;
        }

        public AngleSegment combines(AngleSegment segment) {
            double side1 = Math.min(middle - half, segment.middle - segment.half);
            double side2 = Math.max(middle + half, segment.middle + segment.half);
            double newHalf = MathHelper.getAngel(side2, side1) / 2.0;
            return new AngleSegment(MathHelper.negateAngel(side1 + newHalf), newHalf);
        }

        public boolean isInside(double angle) {
            double delta = MathHelper.getAngel(middle, angle);
            return delta < half;
        }

        public double getNearestSide(double angle) {
            double angle1 = MathHelper.negateAngel(middle + half);
            double angle2 = MathHelper.negateAngel(middle - half);

            if (MathHelper.getAngel(angle, angle1) < MathHelper.getAngel(angle, angle2)) {
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
