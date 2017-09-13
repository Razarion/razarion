package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Color;

/**
 * Created by Beat
 * on 12.09.2017.
 */
public class InGameQuestVisualConfig {
    private double cornerLength;
    private Integer outOfViewShape3DId;
    private double moveDistance;
    private long duration;
    private Color harvestColor;
    private Color attackColor;

    public double getCornerLength() {
        return cornerLength;
    }

    public void setCornerLength(double cornerLength) {
        this.cornerLength = cornerLength;
    }

    public Integer getOutOfViewShape3DId() {
        return outOfViewShape3DId;
    }

    public void setOutOfViewShape3DId(Integer outOfViewShape3DId) {
        this.outOfViewShape3DId = outOfViewShape3DId;
    }

    public double getMoveDistance() {
        return moveDistance;
    }

    public void setMoveDistance(double moveDistance) {
        this.moveDistance = moveDistance;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Color getHarvestColor() {
        return harvestColor;
    }

    public void setHarvestColor(Color harvestColor) {
        this.harvestColor = harvestColor;
    }

    public Color getAttackColor() {
        return attackColor;
    }

    public void setAttackColor(Color attackColor) {
        this.attackColor = attackColor;
    }
}
