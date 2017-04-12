package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class VerticalSegment {
    private Slope slope;
    private int index;
    private DecimalPosition inner;
    private DecimalPosition outer;
    private VerticalSegment predecessor;
    private VerticalSegment successor;

    public VerticalSegment(Slope slope, int index, DecimalPosition inner, DecimalPosition outer) {
        this.slope = slope;
        this.index = index;
        this.inner = inner;
        this.outer = outer;
    }

    public Slope getSlope() {
        return slope;
    }

    public Matrix4 getTransformation() {
        Matrix4 translationMatrix = Matrix4.createTranslation(outer.getX(), outer.getY(), 0);
        if (inner.equals(outer)) {
            return translationMatrix;
        }
        Matrix4 rotationMatrix = Matrix4.createZRotation(outer.getAngle(inner));
        return translationMatrix.multiply(rotationMatrix);
    }

    public DecimalPosition getInner() {
        return inner;
    }

    public DecimalPosition getOuter() {
        return outer;
    }

    public VerticalSegment getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(VerticalSegment predecessor) {
        this.predecessor = predecessor;
    }

    public VerticalSegment getSuccessor() {
        return successor;
    }

    public void setSuccessor(VerticalSegment successor) {
        this.successor = successor;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
