package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.dto.Config;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 28.07.2016.
 */
public class Shape3D implements Config {
    private int id;
    private String internalName;
    private List<Element3D> element3Ds;
    private List<ModelMatrixAnimation> modelMatrixAnimations;

    public int getId() {
        return id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public List<Element3D> getElement3Ds() {
        return element3Ds;
    }

    public void setElement3Ds(List<Element3D> element3Ds) {
        this.element3Ds = element3Ds;
    }

    public List<ModelMatrixAnimation> getModelMatrixAnimations() {
        return modelMatrixAnimations;
    }

    public void setModelMatrixAnimations(List<ModelMatrixAnimation> modelMatrixAnimations) {
        this.modelMatrixAnimations = modelMatrixAnimations;
    }

    public int calculateAnimationDuration() {
        if (modelMatrixAnimations == null) {
            throw new IllegalArgumentException("No animation configured for Shape3D: " + this);
        }
        Long first = null;
        Long last = null;
        for (ModelMatrixAnimation modelMatrixAnimation : modelMatrixAnimations) {
            Long tmpFirst = modelMatrixAnimation.firstTimeStamp();
            Long tmpLast = modelMatrixAnimation.lastTimeStamp();
            first = MathHelper.getSafeMin(tmpFirst, first);
            last = MathHelper.getSafeMax(tmpLast, last);
        }
        if (first == null || last == null) {
            throw new IllegalArgumentException("Invalid Animation in Shape3D: " + this);
        }
        return (int) (last - first);
    }

    public Collection<ModelMatrixAnimation> setupAnimations(Element3D element3D) {
        if (modelMatrixAnimations == null) {
            return null;
        }
        Collection<ModelMatrixAnimation> animations = modelMatrixAnimations.stream().filter(modelMatrixAnimation -> element3D.equals(modelMatrixAnimation.getElement3D())).collect(Collectors.toCollection(ArrayList::new));
        if (animations.isEmpty()) {
            return null;
        } else {
            return animations;
        }
    }

    public Shape3D id(int id) {
        this.id = id;
        return this;
    }

    public Shape3D internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public Shape3D element3Ds(List<Element3D> element3Ds) {
        setElement3Ds(element3Ds);
        return this;
    }

    public Shape3D modelMatrixAnimations(List<ModelMatrixAnimation> modelMatrixAnimations) {
        setModelMatrixAnimations(modelMatrixAnimations);
        return this;
    }
}
