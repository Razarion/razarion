package com.btxtech.shared;

/**
 * Created by Beat
 * 02.05.2016.
 */
public class GroundHeightEntry {
    private int xIndex;
    private int yIndex;
    private double height;

    public GroundHeightEntry() {
    }

    /**
     * Used by GWT and errai
     */
    public GroundHeightEntry(int xIndex, int yIndex, double height) {
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.height = height;
    }

    public int getXIndex() {
        return xIndex;
    }

    public void setXIndex(int xIndex) {
        this.xIndex = xIndex;
    }

    public int getYIndex() {
        return yIndex;
    }

    public void setYIndex(int yIndex) {
        this.yIndex = yIndex;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
