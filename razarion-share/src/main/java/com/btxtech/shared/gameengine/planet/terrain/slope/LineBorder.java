package com.btxtech.shared.gameengine.planet.terrain.slope;


import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class LineBorder extends AbstractBorder {
    private DecimalPosition innerStart;
    private DecimalPosition innerEnd;
    private DecimalPosition outerStart;
    private DecimalPosition outerEnd;

    public LineBorder(AbstractCornerBorder current, AbstractCornerBorder next, double distance) {
        super(distance);
        innerStart = current.getInnerEnd();
        innerEnd = next.getInnerStart();
        outerStart = current.getOuterEnd();
        outerEnd = next.getOuterStart();
    }

    public LineBorder(DecimalPosition current, DecimalPosition next) {
        super(0);
        innerStart = current;
        innerEnd = next;
        outerStart = current;
        outerEnd = next;
    }


    @Override
    protected int getSegmentCount(double verticalSpace) {
        double distance = innerStart.getDistance(innerEnd);
        int segments = (int) Math.round(distance / verticalSpace);
        if (segments > 0) {
            return segments;
        } else {
            return 1;
        }
    }

    @Override
    protected double getSegmentLength(int segmentCount) {
        return innerStart.getDistance(innerEnd) / (double) segmentCount;
    }

    @Override
    protected DecimalPosition setupInnerPointFormStart(double verticalSpace, int count) {
        return innerStart.getPointWithDistance(verticalSpace * count, innerEnd, true);
    }

    @Override
    protected DecimalPosition setupOuterPointFormStart(double verticalSpace, int count) {
        return outerStart.getPointWithDistance(verticalSpace * count, outerEnd, true);
    }
}
