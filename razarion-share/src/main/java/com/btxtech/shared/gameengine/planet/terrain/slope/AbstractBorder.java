package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public abstract class AbstractBorder {
    private double distance;

    public AbstractBorder(double distance) {
        this.distance = distance;
    }

    protected abstract int getSegmentCount(double verticalSpace);

    protected abstract double getSegmentLength(int count);

    protected abstract DecimalPosition setupInnerPointFormStart(double verticalSpace, int count);

    protected abstract DecimalPosition setupOuterPointFormStart(double verticalSpace, int count);

    public double getDistance() {
        return distance;
    }

    public List<VerticalSegment> setupVerticalSegments(Slope slope, double verticalSpace) {
        List<VerticalSegment> verticalSegments = new ArrayList<>();
        int count = getSegmentCount(verticalSpace);
        double length = getSegmentLength(count);
        for (int i = 0; i < count; i++) {
            VerticalSegment verticalSegment = new VerticalSegment(slope, i, setupInnerPointFormStart(length, i), setupOuterPointFormStart(length, i));
            verticalSegments.add(verticalSegment);
        }
        return verticalSegments;
    }
}
