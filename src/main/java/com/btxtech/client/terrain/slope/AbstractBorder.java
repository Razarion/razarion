package com.btxtech.client.terrain.slope;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Line;

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

    protected abstract DecimalPosition setupInnerPointFormStart(int verticalSpace, int count);

    protected abstract DecimalPosition setupOuterPointFormStart(int verticalSpace, int count);

    public double getDistance() {
        return distance;
    }

    public int setupVerticalSegments(int verticalSpace) {
        int count = getSegmentCount(verticalSpace);
        for (int i = 0; i < count + 1; i++) {
            verticalSegments.add(new VerticalSegment(setupInnerPointFormStart(verticalSpace, i), setupOuterPointFormStart(verticalSpace, i)));
        }
        return count + 1;
    }

    public List<VerticalSegment> getVerticalSegments() {
        return verticalSegments;
    }

}
