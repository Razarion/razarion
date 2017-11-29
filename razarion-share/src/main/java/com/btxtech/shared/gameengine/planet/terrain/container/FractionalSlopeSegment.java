package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeFractionalSlopeSegment;
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

    public FractionalSlopeSegment() {
    }

    public FractionalSlopeSegment(NativeFractionalSlopeSegment nativeFractionalSlopeSegment) {
        inner = new DecimalPosition(nativeFractionalSlopeSegment.xI, nativeFractionalSlopeSegment.yI);
        outer = new DecimalPosition(nativeFractionalSlopeSegment.xO, nativeFractionalSlopeSegment.yO);
        index = nativeFractionalSlopeSegment.index;
        if (nativeFractionalSlopeSegment.drivewayHeightFactor != null) {
            drivewayHeightFactor = nativeFractionalSlopeSegment.drivewayHeightFactor;
        }
    }

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

    public DecimalPosition getInner() {
        return inner;
    }

    public DecimalPosition getOuter() {
        return outer;
    }

    public Matrix4 setupTransformation(boolean inverted) {
        if (!inverted) {
            return setupTransformationNormal();
        } else {
            return setupTransformationInverted();
        }
    }

    private Matrix4 setupTransformationNormal() {
        Matrix4 translationMatrix = Matrix4.createTranslation(outer.getX(), outer.getY(), 0);
        if (inner.equals(outer)) {
            return translationMatrix;
        }
        Matrix4 rotationMatrix = Matrix4.createZRotation(outer.getAngle(inner));
        return translationMatrix.multiply(rotationMatrix);
    }

    private Matrix4 setupTransformationInverted() {
        Matrix4 translationMatrix = Matrix4.createTranslation(inner.getX(), inner.getY(), 0);
        if (inner.equals(outer)) {
            return translationMatrix;
        }
        Matrix4 rotationMatrix = Matrix4.createZRotation(inner.getAngle(outer));
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

    public NativeFractionalSlopeSegment toNativeFractionalSlopeSegment() {
        NativeFractionalSlopeSegment nativeFractionalSlopeSegment = new NativeFractionalSlopeSegment();
        nativeFractionalSlopeSegment.xI = inner.getX();
        nativeFractionalSlopeSegment.yI = inner.getY();
        nativeFractionalSlopeSegment.xO = outer.getX();
        nativeFractionalSlopeSegment.yO = outer.getY();
        nativeFractionalSlopeSegment.index = index;
        if (drivewayHeightFactor != 0) {
            nativeFractionalSlopeSegment.drivewayHeightFactor = drivewayHeightFactor;
        }
        return nativeFractionalSlopeSegment;
    }
}
