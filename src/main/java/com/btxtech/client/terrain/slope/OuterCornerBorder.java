package com.btxtech.client.terrain.slope;


import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class OuterCornerBorder extends AbstractCornerBorder {
    private DecimalPosition outerCenter;
    private DecimalPosition innerStart;
    private DecimalPosition innerEnd;

    public OuterCornerBorder(DecimalPosition current, DecimalPosition previous, DecimalPosition next, double distance) {
        super(distance);
        double halfOuterAngle = current.getAngle(previous, next) / 2.0;
        double angle = current.getAngleToNorth(next) - halfOuterAngle;
        double cornerDistance = distance / Math.sin(halfOuterAngle);
        outerCenter = current.getPointFromAngelToNord(angle, cornerDistance);
        double startAngle = current.getAngleToNorth(previous) - MathHelper.QUARTER_RADIANT;
        innerStart = outerCenter.getPointFromAngelToNord(startAngle, distance);
        double endAngle = current.getAngleToNorth(next) + MathHelper.QUARTER_RADIANT;
        innerEnd = outerCenter.getPointFromAngelToNord(endAngle, distance);
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
        return outerCenter.getAngle(innerEnd, innerStart);
    }

    @Override
    protected DecimalPosition setupInnerPointFormStart(int verticalSpace, int count) {
        if (count == 0) {
            return innerStart;
        }
        double totalAngle = outerCenter.getAngleToNorth(innerStart) - getSegmentAngle(verticalSpace) * count;
        return outerCenter.getPointFromAngelToNord(totalAngle, getDistance());
    }

    @Override
    protected DecimalPosition setupOuterPointFormStart(int verticalSpace, int count) {
        return outerCenter;
    }
}
