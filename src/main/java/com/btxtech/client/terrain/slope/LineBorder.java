package com.btxtech.client.terrain.slope;


import com.btxtech.game.jsre.client.common.DecimalPosition;

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
    protected int getSegmentCount(int verticalSpace) {
        double distance = innerStart.getDistance(innerEnd);
        return (int) (distance / verticalSpace);
    }

    @Override
    protected DecimalPosition setupInnerPointFormStart(int verticalSpace, int count) {
        return innerStart.getPointWithDistance(verticalSpace * count, innerEnd, true);
    }

    @Override
    protected DecimalPosition setupOuterPointFormStart(int verticalSpace, int count) {
        return outerStart.getPointWithDistance(verticalSpace * count, outerEnd, true);
    }

}
