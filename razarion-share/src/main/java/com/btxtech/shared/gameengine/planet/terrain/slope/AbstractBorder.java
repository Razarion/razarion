package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.utils.MathHelper;

import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public abstract class AbstractBorder {
    private double distance;
    private double drivewayHeightFactor;

    public AbstractBorder(double distance, double drivewayHeightFactor) {
        this.distance = distance;
        this.drivewayHeightFactor = drivewayHeightFactor;
    }

    protected abstract int getSegmentCount(double verticalSpace);

    protected abstract double getSegmentLength(int count);

    public abstract DecimalPosition getInnerStart();

    protected abstract DecimalPosition setupInnerPointFormStart(double verticalSpace, int count);

    protected abstract DecimalPosition setupOuterPointFormStart(double verticalSpace, int count);

    public double getDistance() {
        return distance;
    }

    public void fillVerticalSegments(List<VerticalSegment> verticalSegments, Slope slope, double verticalSpace, AbstractBorder next, UvContext uvContext) {
        int count = getSegmentCount(verticalSpace);
        double length = getSegmentLength(count);
        for (int i = 0; i < count; i++) {
            DecimalPosition pointFromStart = setupInnerPointFormStart(length, i);
            DecimalPosition outer = setupOuterPointFormStart(length, i);
            uvContext.addToUv(outer);
            verticalSegments.add(new VerticalSegment(slope, verticalSegments.size(), pointFromStart, outer, uvContext.getUvY(), calculateDrivewayHeightFactor(pointFromStart, next)));
        }
    }

    private double calculateDrivewayHeightFactor(DecimalPosition actualPosition, AbstractBorder next) {
        if (drivewayHeightFactor == next.getDrivewayHeightFactor()) {
            return drivewayHeightFactor;
        }
        double wholeDistance = getInnerStart().getDistance(next.getInnerStart());
        double actualDistance = getInnerStart().getDistance(actualPosition);
        double factor = MathHelper.clamp(actualDistance / wholeDistance, 0.0, 1.0);
        return factor * (next.getDrivewayHeightFactor() - drivewayHeightFactor) + drivewayHeightFactor;
    }

    public double getDrivewayHeightFactor() {
        return drivewayHeightFactor;
    }
}
