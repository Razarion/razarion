package com.btxtech.shared.dto;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * 08.05.2016.
 */
@Portable
@Bindable
public class SlopeSkeleton {
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
    private double slopeGroundBlur;
    private double bumpMapDepth;
    private double specularIntensity;
    private double specularHardness;
    private Type type;
    private SlopeNode[][] slopeNodes;

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

    public double getSlopeGroundBlur() {
        return slopeGroundBlur;
    }

    public void setSlopeGroundBlur(double slopeGroundBlur) {
        this.slopeGroundBlur = slopeGroundBlur;
    }

    public double getBumpMapDepth() {
        return bumpMapDepth;
    }

    public void setBumpMapDepth(double bumpMapDepth) {
        this.bumpMapDepth = bumpMapDepth;
    }

    public double getSpecularIntensity() {
        return specularIntensity;
    }

    public void setSpecularIntensity(double specularIntensity) {
        this.specularIntensity = specularIntensity;
    }

    public double getSpecularHardness() {
        return specularHardness;
    }

    public void setSpecularHardness(double specularHardness) {
        this.specularHardness = specularHardness;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlopeSkeleton that = (SlopeSkeleton) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
