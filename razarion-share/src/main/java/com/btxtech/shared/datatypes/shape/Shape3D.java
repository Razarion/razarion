package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 28.07.2016.
 */
public class Shape3D implements ObjectNameIdProvider {
    private int dbId;
    private String internalName;
    private List<Element3D> element3Ds;
    private List<ModelMatrixAnimation> modelMatrixAnimations;

    public int getDbId() {
        return dbId;
    }

    public Shape3D setDbId(int dbId) {
        this.dbId = dbId;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public Shape3D setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public List<Element3D> getElement3Ds() {
        return element3Ds;
    }

    public Shape3D setElement3Ds(List<Element3D> element3Ds) {
        this.element3Ds = element3Ds;
        if (element3Ds != null) {
            for (Element3D element3D : element3Ds) {
                element3D.setShape3DInternalName(internalName);
            }
        }
        return this;
    }

    public List<ModelMatrixAnimation> getModelMatrixAnimations() {
        return modelMatrixAnimations;
    }

    public Shape3D setModelMatrixAnimations(List<ModelMatrixAnimation> modelMatrixAnimations) {
        this.modelMatrixAnimations = modelMatrixAnimations;
        return this;
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

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(dbId, internalName);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Shape3D shape3D = (Shape3D) o;
        return dbId == shape3D.dbId;
    }

    @Override
    public int hashCode() {
        return dbId;
    }

    @Override
    public String toString() {
        return "Shape3D{" +
                "element3Ds=" + element3Ds +
                ", modelMatrixAnimations=" + modelMatrixAnimations +
                '}';
    }
}
