package com.btxtech.uiservice.particle;

/**
 * Created by Beat
 * 08.02.2017.
 */
public class ParticleShapeConfig {
    private int id;
    private String internalName;
    private double edgeLength;
    private double shadowAlphaCutOff;
    private Integer colorRampImageId;
    private Integer alphaOffsetImageId; // rad canal = alpha, greed canal = offset
    private double[] colorRampXOffsets; // 0..1 for x part of the color-ramp lookup
    private double textureOffsetScope; // 0 .. 0.5 for scoping the offset change to the color-ramp from the green part of the alphaOffsetImage

    public int getId() {
        return id;
    }

    public ParticleShapeConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public ParticleShapeConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public double getEdgeLength() {
        return edgeLength;
    }

    public ParticleShapeConfig setEdgeLength(double edgeLength) {
        this.edgeLength = edgeLength;
        return this;
    }

    public double getShadowAlphaCutOff() {
        return shadowAlphaCutOff;
    }

    public ParticleShapeConfig setShadowAlphaCutOff(double shadowAlphaCutOff) {
        this.shadowAlphaCutOff = shadowAlphaCutOff;
        return this;
    }

    public Integer getColorRampImageId() {
        return colorRampImageId;
    }

    public ParticleShapeConfig setColorRampImageId(Integer colorRampImageId) {
        this.colorRampImageId = colorRampImageId;
        return this;
    }

    public Integer getAlphaOffsetImageId() {
        return alphaOffsetImageId;
    }

    public ParticleShapeConfig setAlphaOffsetImageId(Integer alphaOffsetImageId) {
        this.alphaOffsetImageId = alphaOffsetImageId;
        return this;
    }

    public double[] getColorRampXOffsets() {
        return colorRampXOffsets;
    }

    public double getColorRampXOffset(int index) {
        return colorRampXOffsets[index];
    }

    public ParticleShapeConfig setColorRampXOffsets(double[] colorRampXOffsets) {
        this.colorRampXOffsets = colorRampXOffsets;
        return this;
    }

    public double getTextureOffsetScope() {
        return textureOffsetScope;
    }

    public ParticleShapeConfig setTextureOffsetScope(double textureOffsetScope) {
        this.textureOffsetScope = textureOffsetScope;
        return this;
    }

    @Override
    public String toString() {
        return "ParticleShapeConfig{" +
                "id=" + id +
                ", internalName='" + internalName + '\'' +
                '}';
    }
}
