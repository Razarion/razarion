package com.btxtech.shared.datatypes.terrain;

import com.btxtech.shared.datatypes.Float32ArrayEmu;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.LightConfig;

/**
 * Created by Beat
 * 13.03.2017.
 */
public class GroundUi {
    private GroundSkeletonConfig groundSkeletonConfig;
    private int elementCount;
    private Float32ArrayEmu vertices;
    private Float32ArrayEmu norms;
    private Float32ArrayEmu tangents;
    private Float32ArrayEmu splattings;

    public GroundUi(GroundSkeletonConfig groundSkeletonConfig) {
        this.groundSkeletonConfig = groundSkeletonConfig;
    }

    public GroundUi(int elementCount, Float32ArrayEmu vertices, Float32ArrayEmu norms, Float32ArrayEmu tangents, Float32ArrayEmu splattings) {
        this.elementCount = elementCount;
        this.vertices = vertices;
        this.norms = norms;
        this.tangents = tangents;
        this.splattings = splattings;
    }

    public Float32ArrayEmu getVertices() {
        return vertices;
    }

    public void setVertices(Float32ArrayEmu vertices) {
        this.vertices = vertices;
    }

    public Float32ArrayEmu getNorms() {
        return norms;
    }

    public void setNorms(Float32ArrayEmu norms) {
        this.norms = norms;
    }

    public Float32ArrayEmu getTangents() {
        return tangents;
    }

    public void setTangents(Float32ArrayEmu tangents) {
        this.tangents = tangents;
    }

    public Float32ArrayEmu getSplattings() {
        return splattings;
    }

    public Integer getTopTextureId() {
        return groundSkeletonConfig.getTopTextureId();
    }

    public Integer getTopBmId() {
        return groundSkeletonConfig.getTopBmId();
    }

    public Integer getSplattingId() {
        return groundSkeletonConfig.getSplattingId();
    }

    public Integer getBottomTextureId() {
        return groundSkeletonConfig.getBottomTextureId();
    }

    public Integer getBottomBmId() {
        return groundSkeletonConfig.getBottomBmId();
    }

    public double getTopTextureScale() {
        return groundSkeletonConfig.getTopTextureScale();
    }

    public double getTopBmScale() {
        return groundSkeletonConfig.getTopBmScale();
    }

    public double getSplattingScale() {
        return groundSkeletonConfig.getSplattingScale();
    }

    public double getBottomTextureScale() {
        return groundSkeletonConfig.getBottomTextureScale();
    }

    public double getBottomBmScale() {
        return groundSkeletonConfig.getBottomBmScale();
    }

    public int getElementCount() {
        return elementCount;
    }

    public LightConfig getLightConfig() {
        return groundSkeletonConfig.getLightConfig();
    }

    public double getTopBmDepth() {
        return groundSkeletonConfig.getTopBmDepth();
    }

    public double getBottomBmDepth() {
        return groundSkeletonConfig.getBottomBmDepth();
    }

    public void setBuffers(GroundUi groundUi) {
        elementCount = groundUi.getElementCount();
        vertices = groundUi.getVertices();
        norms = groundUi.getNorms();
        tangents = groundUi.getTangents();
        splattings = groundUi.getSplattings();
    }
}
