package com.btxtech.uiservice.terrain.slope;


import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class InnerCornerBorder extends AbstractCornerBorder {
    private Index innerCenter;
    private Index outerStart;
    private Index outerEnd;

    public InnerCornerBorder(Index current, Index previous, Index next, double distance) {
        super(distance);
        innerCenter = current;
        double startAngle = current.getAngleToNorth(previous) + MathHelper.QUARTER_RADIANT;
        outerStart = current.getPointFromAngleRound(startAngle, distance);
        double endAngle = current.getAngleToNorth(next) - MathHelper.QUARTER_RADIANT;
        outerEnd = current.getPointFromAngleRound(endAngle, distance);
    }

    @Override
    public Index getInnerStart() {
        return innerCenter;
    }

    @Override
    public Index getInnerEnd() {
        return innerCenter;
    }

    @Override
    public Index getOuterStart() {
        return outerStart;
    }

    @Override
    public Index getOuterEnd() {
        return outerEnd;
    }

    @Override
    protected double getAngle() {
        return innerCenter.getAngle(outerStart, outerEnd);
    }

    @Override
    protected Index setupInnerPointFormStart(int verticalSpace, int count) {
        return innerCenter;
    }

    @Override
    protected Index setupOuterPointFormStart(int verticalSpace, int count) {
        if (count == 0) {
            return outerStart;
        }
        double totalAngle = innerCenter.getAngleToNorth(outerStart) + getSegmentAngle(verticalSpace) * count;
        return innerCenter.getPointFromAngleRound(totalAngle, getDistance());
    }
}
