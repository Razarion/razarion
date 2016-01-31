package com.btxtech.client.terrain.slope;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.shared.primitives.Matrix4;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class VerticalSegment {
    private DecimalPosition inner;
    private DecimalPosition outer;

    public VerticalSegment(DecimalPosition inner, DecimalPosition outer) {
        this.inner = inner;
        this.outer = outer;
    }

    public Matrix4 getTransformation() {
        Matrix4 translationMatrix = Matrix4.createTranslation(outer.getX(), outer.getY(), 0);
        if(inner.equals(outer)) {
            return translationMatrix;
        }
        Matrix4 rotationMatrix = Matrix4.createZRotation(outer.getAngleToNorth(inner));
        return translationMatrix.multiply(rotationMatrix);
    }
}
