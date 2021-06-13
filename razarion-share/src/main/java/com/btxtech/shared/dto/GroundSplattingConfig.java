package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.CollectionReference;
import com.btxtech.shared.datatypes.CollectionReferenceType;

public class GroundSplattingConfig {
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer textureId;
    private double scale1;
    private double scale2;
    private double blur;
    private double offset;

    public Integer getTextureId() {
        return textureId;
    }

    public void setTextureId(Integer textureId) {
        this.textureId = textureId;
    }

    public double getScale1() {
        return scale1;
    }

    public void setScale1(double scale1) {
        this.scale1 = scale1;
    }

    public double getScale2() {
        return scale2;
    }

    public void setScale2(double scale2) {
        this.scale2 = scale2;
    }

    public double getBlur() {
        return blur;
    }

    public void setBlur(double blur) {
        this.blur = blur;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public GroundSplattingConfig textureId(Integer imageId) {
        setTextureId(imageId);
        return this;
    }

    public GroundSplattingConfig scale1(double scale1) {
        setScale1(scale1);
        return this;
    }

    public GroundSplattingConfig scale2(double scale2) {
        setScale2(scale2);
        return this;
    }

    public GroundSplattingConfig blur(double blur) {
        setBlur(blur);
        return this;
    }

    public GroundSplattingConfig offset(double offset) {
        setOffset(offset);
        return this;
    }
}
