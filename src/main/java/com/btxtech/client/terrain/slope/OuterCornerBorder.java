package com.btxtech.client.terrain.slope;


import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class OuterCornerBorder extends AbstractCornerBorder {
    private Index outerCenter;
    private Index innerStart;
    private Index innerEnd;

    public OuterCornerBorder(Index current, Index previous, Index next, double distance) {
        super(distance);
        double halfOuterAngle = current.getAngle(previous, next) / 2.0;
        double angle = current.getAngleToNorth(next) - halfOuterAngle;
        double cornerDistance = distance / Math.sin(halfOuterAngle);
        outerCenter = current.getPointFromAngleRound(angle, cornerDistance);
        double startAngle = current.getAngleToNorth(previous) - MathHelper.QUARTER_RADIANT;
        innerStart = outerCenter.getPointFromAngleRound(startAngle, distance);
        double endAngle = current.getAngleToNorth(next) + MathHelper.QUARTER_RADIANT;
        innerEnd = outerCenter.getPointFromAngleRound(endAngle, distance);
    }

    @Override
    public Index getInnerStart() {
        return innerStart;
    }

    @Override
    public Index getInnerEnd() {
        return innerEnd;
    }

    @Override
    public Index getOuterStart() {
        return outerCenter;
    }

    @Override
    public Index getOuterEnd() {
        return outerCenter;
    }

    @Override
    protected double getAngle() {
        return outerCenter.getAngle(innerEnd, innerStart);
    }

    @Override
    protected Index setupInnerPointFormStart(int verticalSpace, int count) {
        if (count == 0) {
            return innerStart;
        }
        double totalAngle = outerCenter.getAngleToNorth(innerStart) - getSegmentAngle(verticalSpace) * count;
        return outerCenter.getPointFromAngleRound(totalAngle, getDistance());
    }

    @Override
    protected Index setupOuterPointFormStart(int verticalSpace, int count) {
        return outerCenter;
    }
}
