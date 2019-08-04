package com.btxtech.shared.gameengine.planet.terrain.slope;


import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 23.01.2016.
 */
public abstract class AbstractCornerBorder extends AbstractBorder {

    public AbstractCornerBorder(double distance, double drivewayHeightFactor) {
        super(distance, drivewayHeightFactor);
    }

    public abstract DecimalPosition getInnerEnd();

    public abstract DecimalPosition getOuterStart();

    public abstract DecimalPosition getOuterEnd();

    protected abstract double getAngle();

    @Override
    protected int getSegmentCount(double horizontalSpace) {
        if (horizontalSpace >= 2.0 * getDistance()) {
            return 1;
        }
        double nodeAngle = getSegmentAngle(horizontalSpace);
        int count = (int) Math.round(getAngle() / nodeAngle);
        if (count > 0) {
            return count;
        } else {
            return 1;
        }
    }

    @Override
    protected double getSegmentLength(int count) {
        return 2.0 * getDistance() * Math.sin(getAngle() / (double) count / 2.0);
    }

    protected double getSegmentAngle(double horizontalSpace) {
        return 2.0 * Math.asin(horizontalSpace / (2.0 * getDistance()));
    }



}
