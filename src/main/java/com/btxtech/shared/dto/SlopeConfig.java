package com.btxtech.shared.dto;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

import java.util.List;

/**
 * Created by Beat
 * 08.05.2016.
 */
@Portable
@Bindable
public class SlopeConfig {
    private Integer id;
    private String internalName;
    private List<SlopeShape> shape;
    private SlopeSkeleton slopeSkeleton;
    private double fractalShift;
    private double fractalRoughness;

    public Integer getId() {
        return id;
    }

    public boolean hasId() {
        return id != null;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public List<SlopeShape> getShape() {
        return shape;
    }

    public void setShape(List<SlopeShape> shape) {
        this.shape = shape;
    }

    public SlopeSkeleton getSlopeSkeleton() {
        return slopeSkeleton;
    }

    public void setSlopeSkeleton(SlopeSkeleton slopeSkeleton) {
        this.slopeSkeleton = slopeSkeleton;
    }

    public double getFractalShift() {
        return fractalShift;
    }

    public void setFractalShift(double fractalShift) {
        this.fractalShift = fractalShift;
    }

    public double getFractalRoughness() {
        return fractalRoughness;
    }

    public void setFractalRoughness(double fractalRoughness) {
        this.fractalRoughness = fractalRoughness;
    }

    public SlopeNameId createSlopeNameId() {
        return new SlopeNameId(id, internalName);
    }
}
