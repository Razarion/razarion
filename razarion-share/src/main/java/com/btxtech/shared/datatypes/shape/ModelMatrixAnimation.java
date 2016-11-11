package com.btxtech.shared.datatypes.shape;

import java.util.List;

/**
 * Created by Beat
 * 28.07.2016.
 */
public class ModelMatrixAnimation {
    public enum Axis {
        X, Y, Z
    }

    private String id;
    private Element3D element3D;
    private TransformationModification modification;
    private Axis axis;
    private List<TimeValueSample> timeValueSamples;
    private AnimationTrigger animationTrigger;

    public String getId() {
        return id;
    }

    public ModelMatrixAnimation setId(String id) {
        this.id = id;
        return this;
    }

    public Element3D getElement3D() {
        return element3D;
    }

    public ModelMatrixAnimation setElement3D(Element3D element3D) {
        this.element3D = element3D;
        return this;
    }

    public TransformationModification getModification() {
        return modification;
    }

    public ModelMatrixAnimation setModification(TransformationModification modification) {
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

    public AnimationTrigger getAnimationTrigger() {
        return animationTrigger;
    }

    public ModelMatrixAnimation setAnimationTrigger(AnimationTrigger animationTrigger) {
        this.animationTrigger = animationTrigger;
        return this;
    }

    public Long firstTimeStamp() {
        if(timeValueSamples == null || timeValueSamples.isEmpty()) {
            return null;
        }
        return timeValueSamples.get(0).getTimeStamp();
    }

    public Long lastTimeStamp() {
        if(timeValueSamples == null || timeValueSamples.isEmpty()) {
            return null;
        }
        return timeValueSamples.get(timeValueSamples.size() - 1).getTimeStamp();
    }

    @Override
    public String toString() {
        return "ModelMatrixAnimation{" +
                "id=" + id +
                ", modification=" + modification +
                ", axis=" + axis +
                ", timeValueSamples=" + timeValueSamples +
                ", element3D id=" + element3D.getId() +
                ", animationTrigger=" + animationTrigger +
                '}';
    }
}
