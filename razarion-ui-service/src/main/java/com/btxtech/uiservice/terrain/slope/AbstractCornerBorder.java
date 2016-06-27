package com.btxtech.uiservice.terrain.slope;


import com.btxtech.game.jsre.client.common.Index;

/**
 * Created by Beat
 * 23.01.2016.
 */
public abstract class AbstractCornerBorder extends AbstractBorder {
    public AbstractCornerBorder(double distance) {
        super(distance);
    }

    public abstract Index getInnerStart();

    public abstract Index getInnerEnd();

    public abstract Index getOuterStart();

    public abstract Index getOuterEnd();

    protected abstract double getAngle();

    @Override
    protected int getSegmentCount(int verticalSpace) {
        if (verticalSpace >= 2.0 * getDistance()) {
            return 1;
        }
        double nodeAngle = getSegmentAngle(verticalSpace);
        int count = (int) Math.round(getAngle() / nodeAngle);
        if (count > 0) {
            return count;
        } else {
            return 1;
        }
    }

    @Override
    protected int getSegmentLength(int count) {
        return (int) Math.round(2.0 * getDistance() * Math.sin(getAngle() / (double) count / 2.0));
    }

    protected double getSegmentAngle(int verticalSpace) {
        return 2.0 * Math.asin(verticalSpace / (2.0 * getDistance()));
    }

}
