package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.datatypes.Shape;
import com.btxtech.shared.dto.FractalFieldConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.SlopeSkeletonConfig;

import java.util.List;

/**
 * Created by Beat
 * 08.05.2016.
 */
public class SlopeConfig implements ObjectNameIdProvider {
    private int id;
    private String internalName;
    private List<SlopeShape> shape;
    private SlopeSkeletonConfig slopeSkeletonConfig;
    private double fractalMin;
    private double fractalMax;
    private double fractalClampMin;
    private double fractalClampMax;
    private double fractalRoughness;

    public int getId() {
        return id;
    }

    public SlopeConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public SlopeConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public List<SlopeShape> getShape() {
        return shape;
    }

    public SlopeConfig setShape(List<SlopeShape> shape) {
        this.shape = shape;
        return this;
    }

    public SlopeSkeletonConfig getSlopeSkeletonConfig() {
        return slopeSkeletonConfig;
    }

    public SlopeConfig setSlopeSkeletonConfig(SlopeSkeletonConfig slopeSkeletonConfig) {
        this.slopeSkeletonConfig = slopeSkeletonConfig;
        return this;
    }

    public double getFractalRoughness() {
        return fractalRoughness;
    }

    public void setFractalRoughness(double fractalRoughness) {
        this.fractalRoughness = fractalRoughness;
    }

    @Override
    public ObjectNameId createObjectNameId() {
        return new ObjectNameId(id, internalName);
    }

    public double getFractalMin() {
        return fractalMin;
    }

    public SlopeConfig setFractalMin(double fractalMin) {
        this.fractalMin = fractalMin;
        return this;
    }

    public double getFractalMax() {
        return fractalMax;
    }

    public SlopeConfig setFractalMax(double fractalMax) {
        this.fractalMax = fractalMax;
        return this;
    }

    public double getFractalClampMin() {
        return fractalClampMin;
    }

    public SlopeConfig setFractalClampMin(double fractalClampMin) {
        this.fractalClampMin = fractalClampMin;
        return this;
    }

    public double getFractalClampMax() {
        return fractalClampMax;
    }

    public SlopeConfig setFractalClampMax(double fractalClampMax) {
        this.fractalClampMax = fractalClampMax;
        return this;
    }

    public FractalFieldConfig toFractalFiledConfig() {
        FractalFieldConfig fractalFieldConfig = new FractalFieldConfig();
        fractalFieldConfig.setFractalMin(fractalMin);
        fractalFieldConfig.setFractalMax(fractalMax);
        fractalFieldConfig.setClampMin(fractalClampMin);
        fractalFieldConfig.setClampMax(fractalClampMax);
        fractalFieldConfig.setXCount(slopeSkeletonConfig.getSegments());
        Shape shape = new Shape(this.shape);
        fractalFieldConfig.setYCount(shape.getShiftableCount());
        fractalFieldConfig.setFractalRoughness(fractalRoughness);
        return fractalFieldConfig;
    }

    public void fromFractalFiledConfig(FractalFieldConfig fractalFieldConfig) {
        fractalMin = fractalFieldConfig.getFractalMin();
        fractalMax = fractalFieldConfig.getFractalMax();
        fractalClampMin = fractalFieldConfig.getClampMin();
        fractalClampMax = fractalFieldConfig.getClampMax();
        fractalRoughness = fractalFieldConfig.getFractalRoughness();
        slopeSkeletonConfig.setSegments(fractalFieldConfig.getXCount());
    }
}
