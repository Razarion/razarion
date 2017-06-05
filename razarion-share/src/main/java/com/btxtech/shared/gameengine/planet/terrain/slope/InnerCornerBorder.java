package com.btxtech.shared.gameengine.planet.terrain.slope;


import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class InnerCornerBorder extends AbstractCornerBorder {
    private DecimalPosition innerCenter;
    private DecimalPosition outerStart;
    private DecimalPosition outerEnd;

    public InnerCornerBorder(DecimalPosition current, DecimalPosition previous, DecimalPosition next, double distance, double drivewayHeightFactor) {
        super(distance, drivewayHeightFactor);
        innerCenter = current;
        double startAngle = current.getAngle(previous) + MathHelper.QUARTER_RADIANT;
        outerStart = current.getPointWithDistance(startAngle, distance);
        double endAngle = current.getAngle(next) - MathHelper.QUARTER_RADIANT;
        outerEnd = current.getPointWithDistance(endAngle, distance);
    }

    @Override
    public DecimalPosition getInnerStart() {
        return innerCenter;
    }

    @Override
    public DecimalPosition getInnerEnd() {
        return innerCenter;
    }

    @Override
    public DecimalPosition getOuterStart() {
        return outerStart;
    }

    @Override
    public DecimalPosition getOuterEnd() {
        return outerEnd;
    }

    @Override
    protected double getAngle() {
        return innerCenter.angle(outerStart, outerEnd);
    }

    @Override
    protected DecimalPosition setupInnerPointFormStart(double verticalSpace, int count) {
        return innerCenter;
    }

    @Override
    protected DecimalPosition setupOuterPointFormStart(double verticalSpace, int count) {
        if (count == 0) {
            return outerStart;
        }
        double totalAngle = innerCenter.getAngle(outerStart) + getSegmentAngle(verticalSpace) * count;
        return innerCenter.getPointWithDistance(totalAngle, getDistance());
    }


}
