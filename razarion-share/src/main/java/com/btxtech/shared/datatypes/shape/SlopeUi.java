package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;

/**
 * Created by Beat
 * 12.03.2017.
 */
public class SlopeUi {
    private int id;
    private int elementCount;
    private Float32ArrayEmu vertices;
    private Float32ArrayEmu norms;
    private Float32ArrayEmu tangents;
    private Float32ArrayEmu splatting;
    private Float32ArrayEmu slopeFactors;
    private SlopeSkeletonConfig slopeSkeletonConfig;
    private double waterLevel;

    public SlopeUi(int id, SlopeSkeletonConfig slopeSkeletonConfig, double waterLevel) {
        this.id = id;
        this.slopeSkeletonConfig = slopeSkeletonConfig;
        this.waterLevel = waterLevel;
    }

    public SlopeUi(int id, int elementCount, Float32ArrayEmu vertices, Float32ArrayEmu norms, Float32ArrayEmu tangents, Float32ArrayEmu splatting, Float32ArrayEmu slopeFactors) {
        this.id = id;
        this.elementCount = elementCount;
        this.vertices = vertices;
        this.norms = norms;
        this.tangents = tangents;
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

    public Float32ArrayEmu getVertices() {
        return vertices;
    }

    public Float32ArrayEmu getNorms() {
        return norms;
    }

    public int getElementCount() {
        return elementCount;
    }

    public Float32ArrayEmu getTangents() {
        return tangents;
    }

    public Float32ArrayEmu getSlopeFactors() {
        return slopeFactors;
    }

    public Float32ArrayEmu getSplatting() {
        return splatting;
    }

    public LightConfig getLightConfig() {
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
        elementCount = slopeUi.getElementCount();
        vertices = slopeUi.getVertices();
        norms = slopeUi.getNorms();
        tangents = slopeUi.getTangents();
        splatting = slopeUi.getSplatting();
        slopeFactors = slopeUi.getSlopeFactors();
    }
}
