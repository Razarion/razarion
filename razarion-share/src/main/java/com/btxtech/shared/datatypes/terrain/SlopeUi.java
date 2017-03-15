package com.btxtech.shared.datatypes.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;

/**
 * Created by Beat
 * 12.03.2017.
 */
public class SlopeUi extends GroundUi {
    private int id;
    private Float32ArrayEmu splatting;
    private Float32ArrayEmu slopeFactors;
    private SlopeSkeletonConfig slopeSkeletonConfig;
    private double waterLevel;

    public SlopeUi(int id, SlopeSkeletonConfig slopeSkeletonConfig, double waterLevel, GroundSkeletonConfig groundSkeletonConfig) {
        super(groundSkeletonConfig);
        this.id = id;
        this.slopeSkeletonConfig = slopeSkeletonConfig;
        this.waterLevel = waterLevel;
    }

    public SlopeUi(int id, int elementCount, Float32ArrayEmu vertices, Float32ArrayEmu norms, Float32ArrayEmu tangents, Float32ArrayEmu splatting, Float32ArrayEmu slopeFactors) {
        super(elementCount, vertices, norms, tangents, splatting);
        this.id = id;
        this.splatting = splatting;
        this.slopeFactors = slopeFactors;
    }

    public int getId() {
        return id;
    }

    public Integer getTextureId() {
        return slopeSkeletonConfig.getTextureId();
    }

    public Integer getBmId() {
        return slopeSkeletonConfig.getBmId();
    }

    public String getObjectNameId() {
        return slopeSkeletonConfig.createObjectNameId().toString();
    }

    public Float32ArrayEmu getSlopeFactors() {
        return slopeFactors;
    }

    public Float32ArrayEmu getSplatting() {
        return splatting;
    }

    public LightConfig getSlopeLightConfig() {
        return slopeSkeletonConfig.getLightConfig();
    }

    public boolean isSlopeOriented() {
        return slopeSkeletonConfig.getSlopeOriented();
    }

    public double getBmDepth() {
        return slopeSkeletonConfig.getBmDepth();
    }

    public boolean hasWater() {
        return slopeSkeletonConfig.getType() == SlopeSkeletonConfig.Type.WATER;
    }

    public double getWaterLevel() {
        return waterLevel;
    }

    public double getTextureScale() {
        return slopeSkeletonConfig.getTextureScale();
    }

    public double getBmScale() {
        return slopeSkeletonConfig.getBmScale();
    }

    public void setBuffers(SlopeUi slopeUi) {
        super.setBuffers(slopeUi);
        splatting = slopeUi.getSplatting();
        slopeFactors = slopeUi.getSlopeFactors();
    }

    public void setSlopeSkeletonConfig(SlopeSkeletonConfig slopeSkeletonConfig) {
        this.slopeSkeletonConfig = slopeSkeletonConfig;
    }
}
