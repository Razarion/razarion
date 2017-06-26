package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.gameengine.planet.terrain.slope.VerticalSegment;

/**
 * Created by Beat
 * on 19.06.2017.
 */
public class FractionalSlopeSegment {
    private DecimalPosition inner;
    private DecimalPosition outer;
    private int index;
    private double drivewayHeightFactor;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getDrivewayHeightFactor() {
        return drivewayHeightFactor;
    }

    public void setDrivewayHeightFactor(double drivewayHeightFactor) {
        this.drivewayHeightFactor = drivewayHeightFactor;
    }

    public void setInner(DecimalPosition inner) {
        this.inner = inner;
    }

    public void setOuter(DecimalPosition outer) {
        this.outer = outer;
    }

    public Matrix4 setupTransformation() {
        Matrix4 translationMatrix = Matrix4.createTranslation(outer.getX(), outer.getY(), 0);
        if (inner.equals(outer)) {
            return translationMatrix;
        }
        Matrix4 rotationMatrix = Matrix4.createZRotation(outer.getAngle(inner));
        return translationMatrix.multiply(rotationMatrix);
    }

    public static FractionalSlopeSegment fromVerticalSegment(VerticalSegment verticalSegment) {
        FractionalSlopeSegment fractionalSlopeSegment = new FractionalSlopeSegment();
        fractionalSlopeSegment.setIndex(verticalSegment.getIndex());
        fractionalSlopeSegment.setInner(verticalSegment.getInner());
        fractionalSlopeSegment.setOuter(verticalSegment.getOuter());
        fractionalSlopeSegment.setDrivewayHeightFactor(verticalSegment.getDrivewayHeightFactor());
        return fractionalSlopeSegment;
    }
}
