package com.btxtech.shared.datatypes.shape;

import java.util.List;

/**
 * Created by Beat
 * 28.07.2016.
 */
public class ModelMatrixAnimation {
    public enum Modification {
        SCALE;
    }

    public enum Axis {
        X, Y, Z
    }

    private Element3D element3D;
    private Modification modification;
    private Axis axis;
    private List<TimeValueSample> timeValueSamples;

    public Element3D getElement3D() {
        return element3D;
    }

    public ModelMatrixAnimation setElement3D(Element3D element3D) {
        this.element3D = element3D;
        return this;
    }

    public Modification getModification() {
        return modification;
    }

    public ModelMatrixAnimation setModification(Modification modification) {
        this.modification = modification;
        return this;
    }

    public List<TimeValueSample> getTimeValueSamples() {
        return timeValueSamples;
    }

    public Axis getAxis() {
        return axis;
    }

    public ModelMatrixAnimation setAxis(Axis axis) {
        this.axis = axis;
        return this;
    }

    public ModelMatrixAnimation setTimeValueSamples(List<TimeValueSample> timeValueSamples) {
        this.timeValueSamples = timeValueSamples;
        return this;
    }

    @Override
    public String toString() {
        return "ModelMatrixAnimation{" +
                "modification=" + modification +
                ", axis=" + axis +
                ", timeValueSamples=" + timeValueSamples +
                ", element3D=" + element3D +
                '}';
    }
}
