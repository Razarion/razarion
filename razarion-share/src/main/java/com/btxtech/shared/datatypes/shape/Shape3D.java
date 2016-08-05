package com.btxtech.shared.datatypes.shape;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

/**
 * Created by Beat
 * 28.07.2016.
 */
@Portable
public class Shape3D {
    private String tag;
    private List<Element3D> element3Ds;
    private List<ModelMatrixAnimation> modelMatrixAnimations;

    public String getTag() {
        return tag;
    }

    public Shape3D setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public List<Element3D> getElement3Ds() {
        return element3Ds;
    }

    public Shape3D setElement3Ds(List<Element3D> element3Ds) {
        this.element3Ds = element3Ds;
        for (Element3D element3D : element3Ds) {
            element3D.setShape3DTag(tag);
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

    @Override
    public String toString() {
        return "Shape3D{" +
                "element3Ds=" + element3Ds +
                ", modelMatrixAnimations=" + modelMatrixAnimations +
                '}';
    }
}
