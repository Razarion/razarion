package com.btxtech.shared.gameengine.planet.terrain.slope;


import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class OuterCornerBorder extends AbstractCornerBorder {
    private DecimalPosition outerCenter;
    private DecimalPosition innerStart;
    private DecimalPosition innerEnd;

    public OuterCornerBorder(DecimalPosition current, DecimalPosition previous, DecimalPosition next, double distance, double drivewayHeightFactor) {
        super(distance, drivewayHeightFactor);
        double halfOuterAngle = current.angle(previous, next) / 2.0;
        double angle = current.getAngle(next) - halfOuterAngle;
        double cornerDistance = distance / Math.sin(halfOuterAngle);
        outerCenter = current.getPointWithDistance(angle, cornerDistance);
        double startAngle = current.getAngle(previous) - MathHelper.QUARTER_RADIANT;
        innerStart = outerCenter.getPointWithDistance(startAngle, distance);
        double endAngle = current.getAngle(next) + MathHelper.QUARTER_RADIANT;
        innerEnd = outerCenter.getPointWithDistance(endAngle, distance);
    }

    @Override
    public DecimalPosition getInnerStart() {
        return innerStart;
    }

    @Override
    public DecimalPosition getInnerEnd() {
        return innerEnd;
    }

    @Override
    public DecimalPosition getOuterStart() {
        return outerCenter;
    }

    @Override
    public DecimalPosition getOuterEnd() {
        return outerCenter;
    }

    @Override
    protected double getAngle() {
        return outerCenter.angle(innerEnd, innerStart);
    }

    @Override
    protected DecimalPosition setupInnerPointFormStart(double verticalSpace, int count) {
        if (count == 0) {
            return innerStart;
        }
        double totalAngle = outerCenter.getAngle(innerStart) - getSegmentAngle(verticalSpace) * count;
        return outerCenter.getPointWithDistance(totalAngle, getDistance());
    }

    @Override
    protected DecimalPosition setupOuterPointFormStart(double verticalSpace, int count) {
        return outerCenter;
    }
}
