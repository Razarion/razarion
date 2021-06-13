package com.btxtech.shared.datatypes.particle;

import com.btxtech.shared.datatypes.CollectionReference;
import com.btxtech.shared.datatypes.CollectionReferenceType;
import com.btxtech.shared.dto.Config;

import java.util.List;

/**
 * Created by Beat
 * 08.02.2017.
 */
public class ParticleShapeConfig implements Config {
    private int id;
    private String internalName;
    private double edgeLength;
    private double shadowAlphaCutOff;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer colorRampImageId;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer alphaOffsetImageId; // rad canal = alpha, greed canal = offset
    private List<Double> colorRampXOffsets; // 0..1 for x part of the color-ramp lookup
    private double textureOffsetScope; // 0 .. 0.5 for scoping the offset change to the color-ramp from the green part of the alphaOffsetImage

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public double getEdgeLength() {
        return edgeLength;
    }

    public void setEdgeLength(double edgeLength) {
        this.edgeLength = edgeLength;
    }

    public double getShadowAlphaCutOff() {
        return shadowAlphaCutOff;
    }

    public void setShadowAlphaCutOff(double shadowAlphaCutOff) {
        this.shadowAlphaCutOff = shadowAlphaCutOff;
    }

    public Integer getColorRampImageId() {
        return colorRampImageId;
    }

    public void setColorRampImageId(Integer colorRampImageId) {
        this.colorRampImageId = colorRampImageId;
    }

    public Integer getAlphaOffsetImageId() {
        return alphaOffsetImageId;
    }

    public void setAlphaOffsetImageId(Integer alphaOffsetImageId) {
        this.alphaOffsetImageId = alphaOffsetImageId;
    }

    public List<Double> getColorRampXOffsets() {
        return colorRampXOffsets;
    }

    public void setColorRampXOffsets(List<Double> colorRampXOffsets) {
        this.colorRampXOffsets = colorRampXOffsets;
    }

    public double getTextureOffsetScope() {
        return textureOffsetScope;
    }

    public void setTextureOffsetScope(double textureOffsetScope) {
        this.textureOffsetScope = textureOffsetScope;
    }

    public ParticleShapeConfig id(int id) {
        this.id = id;
        return this;
    }

    public ParticleShapeConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public ParticleShapeConfig edgeLength(double edgeLength) {
        setEdgeLength(edgeLength);
        return this;
    }

    public ParticleShapeConfig shadowAlphaCutOff(double shadowAlphaCutOff) {
        setShadowAlphaCutOff(shadowAlphaCutOff);
        return this;
    }

    public ParticleShapeConfig colorRampImageId(Integer colorRampImageId) {
        setColorRampImageId(colorRampImageId);
        return this;
    }

    public ParticleShapeConfig alphaOffsetImageId(Integer alphaOffsetImageId) {
        setAlphaOffsetImageId(alphaOffsetImageId);
        return this;
    }

    public ParticleShapeConfig colorRampXOffsets(List<Double> colorRampXOffsets) {
        setColorRampXOffsets(colorRampXOffsets);
        return this;
    }

    public ParticleShapeConfig textureOffsetScope(double textureOffsetScope) {
        setTextureOffsetScope(textureOffsetScope);
        return this;
    }

    public double lookupColorRampXOffset4Index(int index) {
        return colorRampXOffsets.get(index);
    }

    @Override
    public String toString() {
        return "ParticleShapeConfig{" +
                "id=" + id +
                ", internalName='" + internalName + '\'' +
                '}';
    }
}
