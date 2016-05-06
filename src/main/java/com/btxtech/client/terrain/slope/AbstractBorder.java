package com.btxtech.client.terrain.slope;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public abstract class AbstractBorder {
    private double distance;
    private List<VerticalSegment> verticalSegments = new ArrayList<>();

    public AbstractBorder(double distance) {
        this.distance = distance;
    }

    protected abstract int getSegmentCount(int verticalSpace);

    protected abstract int getSegmentLength(int count);

    protected abstract Index setupInnerPointFormStart(int verticalSpace, int count);

    protected abstract Index setupOuterPointFormStart(int verticalSpace, int count);

    public double getDistance() {
        return distance;
    }

    public int setupVerticalSegments(int verticalSpace) {
        int count = getSegmentCount(verticalSpace);
        int length = getSegmentLength(count);
        for (int i = 0; i < count; i++) {
            verticalSegments.add(new VerticalSegment(setupInnerPointFormStart(length, i), setupOuterPointFormStart(length, i)));
        }
        return count;
    }

    public List<VerticalSegment> getVerticalSegments() {
        return verticalSegments;
    }

}
