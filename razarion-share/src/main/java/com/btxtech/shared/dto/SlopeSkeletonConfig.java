package com.btxtech.shared.dto;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * 08.05.2016.
 */
@Portable
@Bindable
public class SlopeSkeletonConfig {
    public enum Type {
        LAND,
        WATER
    }

    private int id;
    private int segments;
    private int rows;
    private int width;
    private int height;
    private int verticalSpace;
    private double bumpMapDepth;
    private Type type;
    private SlopeNode[][] slopeNodes;
    private LightConfig lightConfig;
    private boolean slopeOriented;
    private int imageId;
    private int bumpImageId;
    private int groundSplattingImageId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSegments() {
        return segments;
    }

    public void setSegments(int segments) {
        this.segments = segments;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getRows() {
        return rows;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getBumpMapDepth() {
        return bumpMapDepth;
    }

    public void setBumpMapDepth(double bumpMapDepth) {
        this.bumpMapDepth = bumpMapDepth;
    }

    public int getVerticalSpace() {
        return verticalSpace;
    }

    public void setVerticalSpace(int verticalSpace) {
        this.verticalSpace = verticalSpace;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public SlopeNode[][] getSlopeNodes() {
        return slopeNodes;
    }

    public void setSlopeNodes(SlopeNode[][] slopeNodes) {
        this.slopeNodes = slopeNodes;
    }

    public LightConfig getLightConfig() {
        return lightConfig;
    }

    public void setLightConfig(LightConfig lightConfig) {
        this.lightConfig = lightConfig;
    }

    // Errai can not handle is-getter
    public boolean getSlopeOriented() {
        return slopeOriented;
    }

    public void setSlopeOriented(boolean slopeOriented) {
        this.slopeOriented = slopeOriented;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getBumpImageId() {
        return bumpImageId;
    }

    public void setBumpImageId(int bumpImageId) {
        this.bumpImageId = bumpImageId;
    }

    public int getGroundSplattingImageId() {
        return groundSplattingImageId;
    }

    public void setGroundSplattingImageId(int groundSplattingImageId) {
        this.groundSplattingImageId = groundSplattingImageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlopeSkeletonConfig that = (SlopeSkeletonConfig) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
