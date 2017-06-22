package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;

/**
 * Created by Beat
 * on 19.06.2017.
 */
public class FractionalSlopeSegment {
    private DecimalPosition inner;
    private DecimalPosition outer;
    private int index;
    private double drivewayHeightFactor;

    public Matrix4 setupTransformation() {
        Matrix4 translationMatrix = Matrix4.createTranslation(outer.getX(), outer.getY(), 0);
        if (inner.equals(outer)) {
            return translationMatrix;
        }
        Matrix4 rotationMatrix = Matrix4.createZRotation(outer.getAngle(inner));
        return translationMatrix.multiply(rotationMatrix);
    }

    public int getIndex() {
        return index;
    }

    public double getDrivewayHeightFactor() {
        return drivewayHeightFactor;
    }

    public void setDrivewayHeightFactor(double drivewayHeightFactor) {
        this.drivewayHeightFactor = drivewayHeightFactor;
    }
}
