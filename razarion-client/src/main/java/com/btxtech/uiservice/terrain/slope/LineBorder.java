package com.btxtech.uiservice.terrain.slope;


import com.btxtech.game.jsre.client.common.Index;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class LineBorder extends AbstractBorder {
    private Index innerStart;
    private Index innerEnd;
    private Index outerStart;
    private Index outerEnd;

    public LineBorder(AbstractCornerBorder current, AbstractCornerBorder next, double distance) {
        super(distance);
        innerStart = current.getInnerEnd();
        innerEnd = next.getInnerStart();
        outerStart = current.getOuterEnd();
        outerEnd = next.getOuterStart();
    }

    public LineBorder(Index current, Index next) {
        super(0);
        innerStart = current;
        innerEnd = next;
        outerStart = current;
        outerEnd = next;
    }


    @Override
    protected int getSegmentCount(int verticalSpace) {
        double distance = innerStart.getDistance(innerEnd);
        int segments = (int) Math.round(distance / verticalSpace);
        if (segments > 0) {
            return segments;
        } else {
            return 1;
        }
    }

    @Override
    protected int getSegmentLength(int segmentCount) {
        return (int) Math.round(innerStart.getDistance(innerEnd) / (double) segmentCount);
    }

    @Override
    protected Index setupInnerPointFormStart(int verticalSpace, int count) {
        return innerStart.getPointWithDistance(verticalSpace * count, innerEnd, true);
    }

    @Override
    protected Index setupOuterPointFormStart(int verticalSpace, int count) {
        return outerStart.getPointWithDistance(verticalSpace * count, outerEnd, true);
    }
}
