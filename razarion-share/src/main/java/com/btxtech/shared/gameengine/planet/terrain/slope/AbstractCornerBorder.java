package com.btxtech.shared.gameengine.planet.terrain.slope;


import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 23.01.2016.
 */
public abstract class AbstractCornerBorder extends AbstractBorder {
    private double drivewayHeightFactor;

    public AbstractCornerBorder(double distance, double drivewayHeightFactor) {
        super(distance);
        this.drivewayHeightFactor = drivewayHeightFactor;
    }

    public abstract DecimalPosition getInnerStart();

    public abstract DecimalPosition getInnerEnd();

    public abstract DecimalPosition getOuterStart();

    public abstract DecimalPosition getOuterEnd();

    protected abstract double getAngle();

    @Override
    protected int getSegmentCount(double verticalSpace) {
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
    protected double getSegmentLength(int count) {
        return 2.0 * getDistance() * Math.sin(getAngle() / (double) count / 2.0);
    }

    protected double getSegmentAngle(double verticalSpace) {
        return 2.0 * Math.asin(verticalSpace / (2.0 * getDistance()));
    }

    @Override
    protected double getDrivewayHeightFactorStart() {
        return drivewayHeightFactor;
    }

    @Override
    protected double getDrivewayHeightFactorEnd() {
        return drivewayHeightFactor;
    }
}
