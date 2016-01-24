package com.btxtech.client.terrain.slope;


import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class InnerCornerBorder extends AbstractCornerBorder {
    private DecimalPosition innerCenter;
    private DecimalPosition outerStart;
    private DecimalPosition outerEnd;

    public InnerCornerBorder(DecimalPosition current, DecimalPosition previous, DecimalPosition next, double distance) {
        super(distance);
        innerCenter = current;
        double startAngle = current.getAngleToNorth(previous) + MathHelper.QUARTER_RADIANT;
        outerStart = current.getPointFromAngelToNord(startAngle, distance);
        double endAngle = current.getAngleToNorth(next) - MathHelper.QUARTER_RADIANT;
        outerEnd = current.getPointFromAngelToNord(endAngle, distance);
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
        return innerCenter.getAngle(outerStart, outerEnd);
    }

    @Override
    protected DecimalPosition setupInnerPointFormStart(int verticalSpace, int count) {
        return innerCenter;
    }

    @Override
    protected DecimalPosition setupOuterPointFormStart(int verticalSpace, int count) {
        if (count == 0) {
            return outerStart;
        }
        double totalAngle = innerCenter.getAngleToNorth(outerStart) + getSegmentAngle(verticalSpace) * count;
        return innerCenter.getPointFromAngelToNord(totalAngle, getDistance());
    }

}
