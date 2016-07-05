package com.btxtech.uiservice.terrain.slope;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Matrix4;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class VerticalSegment {
    private Index inner;
    private Index outer;

    public VerticalSegment(Index inner, Index outer) {
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

    public Index getInner() {
        return inner;
    }

    public Index getOuter() {
        return outer;
    }
}
