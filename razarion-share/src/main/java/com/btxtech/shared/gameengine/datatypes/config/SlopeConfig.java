package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.datatypes.Shape;
import com.btxtech.shared.dto.FractalFieldConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
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
    private SlopeSkeletonConfig slopeSkeletonConfig;
    private double fractalMin;
    private double fractalMax;
    private double fractalClampMin;
    private double fractalClampMax;
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

    public SlopeSkeletonConfig getSlopeSkeletonConfig() {
        return slopeSkeletonConfig;
    }

    public void setSlopeSkeletonConfig(SlopeSkeletonConfig slopeSkeletonConfig) {
        this.slopeSkeletonConfig = slopeSkeletonConfig;
    }

    public double getFractalRoughness() {
        return fractalRoughness;
    }

    public void setFractalRoughness(double fractalRoughness) {
        this.fractalRoughness = fractalRoughness;
    }

    public ObjectNameId createSlopeNameId() {
        return new ObjectNameId(id, internalName);
    }

    public double getFractalMin() {
        return fractalMin;
    }

    public void setFractalMin(double fractalMin) {
        this.fractalMin = fractalMin;
    }

    public double getFractalMax() {
        return fractalMax;
    }

    public void setFractalMax(double fractalMax) {
        this.fractalMax = fractalMax;
    }

    public double getFractalClampMin() {
        return fractalClampMin;
    }

    public void setFractalClampMin(double fractalClampMin) {
        this.fractalClampMin = fractalClampMin;
    }

    public double getFractalClampMax() {
        return fractalClampMax;
    }

    public void setFractalClampMax(double fractalClampMax) {
        this.fractalClampMax = fractalClampMax;
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
