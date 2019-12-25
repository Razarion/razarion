package com.btxtech.shared.gameengine.datatypes.config;

import com.btxtech.shared.dto.FractalFieldConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ObjectNameIdProvider;

/**
 * Created by Beat
 * 08.05.2016.
 */
@Deprecated // Delete
public class SlopeConfig_OLD implements ObjectNameIdProvider {
    private int id;
    private String internalName;
    private SlopeConfig slopeConfig;
    @Deprecated
    private double fractalMin;
    @Deprecated
    private double fractalMax;
    @Deprecated
    private double fractalClampMin;
    @Deprecated
    private double fractalClampMax;
    @Deprecated
    private double fractalRoughness;

    public int getId() {
        return id;
    }

    public SlopeConfig_OLD setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public SlopeConfig_OLD setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public SlopeConfig getSlopeConfig() {
        return slopeConfig;
    }

    public SlopeConfig_OLD setSlopeConfig(SlopeConfig slopeConfig) {
        this.slopeConfig = slopeConfig;
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

    public SlopeConfig_OLD setFractalMin(double fractalMin) {
        this.fractalMin = fractalMin;
        return this;
    }

    public double getFractalMax() {
        return fractalMax;
    }

    public SlopeConfig_OLD setFractalMax(double fractalMax) {
        this.fractalMax = fractalMax;
        return this;
    }

    public double getFractalClampMin() {
        return fractalClampMin;
    }

    public SlopeConfig_OLD setFractalClampMin(double fractalClampMin) {
        this.fractalClampMin = fractalClampMin;
        return this;
    }

    public double getFractalClampMax() {
        return fractalClampMax;
    }

    public SlopeConfig_OLD setFractalClampMax(double fractalClampMax) {
        this.fractalClampMax = fractalClampMax;
        return this;
    }

    public FractalFieldConfig toFractalFiledConfig() {
        FractalFieldConfig fractalFieldConfig = new FractalFieldConfig();
        fractalFieldConfig.setFractalMin(fractalMin);
        fractalFieldConfig.setFractalMax(fractalMax);
        fractalFieldConfig.setClampMin(fractalClampMin);
        fractalFieldConfig.setClampMax(fractalClampMax);
        fractalFieldConfig.setXCount(slopeConfig.getSegments());
        // TODO Shape shape = new Shape(this.slopeShapes);
        // TODO fractalFieldConfig.setYCount(shape.getShiftableCount());
        // TODO fractalFieldConfig.setFractalRoughness(fractalRoughness);
        return fractalFieldConfig;
    }

    public void fromFractalFiledConfig(FractalFieldConfig fractalFieldConfig) {
        fractalMin = fractalFieldConfig.getFractalMin();
        fractalMax = fractalFieldConfig.getFractalMax();
        fractalClampMin = fractalFieldConfig.getClampMin();
        fractalClampMax = fractalFieldConfig.getClampMax();
        fractalRoughness = fractalFieldConfig.getFractalRoughness();
        slopeConfig.setSegments(fractalFieldConfig.getXCount());
    }
}
