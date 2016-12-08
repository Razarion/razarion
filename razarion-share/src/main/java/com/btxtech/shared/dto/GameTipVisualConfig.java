package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Color;

/**
 * Created by Beat
 * 06.12.2016.
 */
public class GameTipVisualConfig {
    private double cornerLength;
    private double cornerMoveDistance;
    private int cornerMoveDuration;
    private Integer selectShape3DId;
    private Color selectCornerColor;
    private Integer defaultCommandShape3DId;
    private Color moveCommandCornerColor;
    private Color toBeFinalizedCornerColor;
    private Integer buildCommandShape3DId;
    private Color buildCommandCornerColor;
    private Color harvestCommandCornerColor;
    private Color attackCommandCornerColor;

    public double getCornerLength() {
        return cornerLength;
    }

    public GameTipVisualConfig setCornerLength(double cornerLength) {
        this.cornerLength = cornerLength;
        return this;
    }

    public double getCornerMoveDistance() {
        return cornerMoveDistance;
    }

    public GameTipVisualConfig setCornerMoveDistance(double cornerMoveDistance) {
        this.cornerMoveDistance = cornerMoveDistance;
        return this;
    }

    public int getCornerMoveDuration() {
        return cornerMoveDuration;
    }

    public GameTipVisualConfig setCornerMoveDuration(int cornerMoveDuration) {
        this.cornerMoveDuration = cornerMoveDuration;
        return this;
    }

    public Integer getSelectShape3DId() {
        return selectShape3DId;
    }

    public GameTipVisualConfig setSelectShape3DId(Integer selectShape3DId) {
        this.selectShape3DId = selectShape3DId;
        return this;
    }

    public Color getSelectCornerColor() {
        return selectCornerColor;
    }

    public GameTipVisualConfig setSelectCornerColor(Color selectCornerColor) {
        this.selectCornerColor = selectCornerColor;
        return this;
    }

    public Integer getDefaultCommandShape3DId() {
        return defaultCommandShape3DId;
    }

    public GameTipVisualConfig setDefaultCommandShape3DId(Integer defaultCommandShape3DId) {
        this.defaultCommandShape3DId = defaultCommandShape3DId;
        return this;
    }

    public Color getMoveCommandCornerColor() {
        return moveCommandCornerColor;
    }

    public GameTipVisualConfig setMoveCommandCornerColor(Color moveCommandCornerColor) {
        this.moveCommandCornerColor = moveCommandCornerColor;
        return this;
    }

    public Color getToBeFinalizedCornerColor() {
        return toBeFinalizedCornerColor;
    }

    public GameTipVisualConfig setToBeFinalizedCornerColor(Color toBeFinalizedCornerColor) {
        this.toBeFinalizedCornerColor = toBeFinalizedCornerColor;
        return this;
    }

    public Integer getBuildCommandShape3DId() {
        return buildCommandShape3DId;
    }

    public GameTipVisualConfig setBuildCommandShape3DId(Integer buildCommandShape3DId) {
        this.buildCommandShape3DId = buildCommandShape3DId;
        return this;
    }

    public Color getBuildCommandCornerColor() {
        return buildCommandCornerColor;
    }

    public GameTipVisualConfig setBuildCommandCornerColor(Color buildCommandCornerColor) {
        this.buildCommandCornerColor = buildCommandCornerColor;
        return this;
    }

    public Color getHarvestCommandCornerColor() {
        return harvestCommandCornerColor;
    }

    public GameTipVisualConfig setHarvestCommandCornerColor(Color harvestCommandCornerColor) {
        this.harvestCommandCornerColor = harvestCommandCornerColor;
        return this;
    }

    public Color getAttackCommandCornerColor() {
        return attackCommandCornerColor;
    }

    public GameTipVisualConfig setAttackCommandCornerColor(Color attackCommandCornerColor) {
        this.attackCommandCornerColor = attackCommandCornerColor;
        return this;
    }
}
