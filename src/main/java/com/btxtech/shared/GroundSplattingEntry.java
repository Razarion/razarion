package com.btxtech.shared;

/**
 * Created by Beat
 * 02.05.2016.
 */
public class GroundSplattingEntry {
    private int xIndex;
    private int yIndex;
    private double splatting;

    /**
     * Used by GWT and errai
     */
    public GroundSplattingEntry() {
    }

    public GroundSplattingEntry(int xIndex, int yIndex, double splatting) {
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.splatting = splatting;
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

    public double getSplatting() {
        return splatting;
    }

    public void setSplatting(double splatting) {
        this.splatting = splatting;
    }
}
