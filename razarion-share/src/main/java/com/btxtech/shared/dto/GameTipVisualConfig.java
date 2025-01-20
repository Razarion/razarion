package com.btxtech.shared.dto;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.datatypes.Color;

/**
 * Created by Beat
 * 06.12.2016.
 */
public class GameTipVisualConfig {
    private double cornerLength;
    private double cornerMoveDistance;
    private int cornerMoveDuration;
    // TODO @CollectionReference(CollectionReferenceType.SHAPE_3D)
    private Integer selectShape3DId;
    // TODO @CollectionReference(CollectionReferenceType.SHAPE_3D)
    private Integer outOfViewShape3DId;
    private Color selectCornerColor;
    // TODO @CollectionReference(CollectionReferenceType.SHAPE_3D)
    private Integer defaultCommandShape3DId;
    private Color moveCommandCornerColor;
    private Color toBeFinalizedCornerColor;
    // TODO @CollectionReference(CollectionReferenceType.SHAPE_3D)
    private Integer baseItemPlacerShape3DId;
    private Color baseItemPlacerCornerColor;
    private Color grabCommandCornerColor;
    private Color attackCommandCornerColor;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer westLeftMouseGuiImageId;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer southLeftMouseGuiImageId;
    // TODO @CollectionReference(CollectionReferenceType.SHAPE_3D)
    private Integer directionShape3DId;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer scrollDialogKeyboardImageId;

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

    public Integer getOutOfViewShape3DId() {
        return outOfViewShape3DId;
    }

    public GameTipVisualConfig setOutOfViewShape3DId(Integer outOfViewShape3DId) {
        this.outOfViewShape3DId = outOfViewShape3DId;
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

    public Integer getBaseItemPlacerShape3DId() {
        return baseItemPlacerShape3DId;
    }

    public GameTipVisualConfig setBaseItemPlacerShape3DId(Integer baseItemPlacerShape3DId) {
        this.baseItemPlacerShape3DId = baseItemPlacerShape3DId;
        return this;
    }

    public Color getBaseItemPlacerCornerColor() {
        return baseItemPlacerCornerColor;
    }

    public GameTipVisualConfig setBaseItemPlacerCornerColor(Color baseItemPlacerCornerColor) {
        this.baseItemPlacerCornerColor = baseItemPlacerCornerColor;
        return this;
    }

    public Color getGrabCommandCornerColor() {
        return grabCommandCornerColor;
    }

    public GameTipVisualConfig setGrabCommandCornerColor(Color grabCommandCornerColor) {
        this.grabCommandCornerColor = grabCommandCornerColor;
        return this;
    }

    public Color getAttackCommandCornerColor() {
        return attackCommandCornerColor;
    }

    public GameTipVisualConfig setAttackCommandCornerColor(Color attackCommandCornerColor) {
        this.attackCommandCornerColor = attackCommandCornerColor;
        return this;
    }

    public Integer getWestLeftMouseGuiImageId() {
        return westLeftMouseGuiImageId;
    }

    public GameTipVisualConfig setWestLeftMouseGuiImageId(Integer westLeftMouseGuiImageId) {
        this.westLeftMouseGuiImageId = westLeftMouseGuiImageId;
        return this;
    }

    public Integer getSouthLeftMouseGuiImageId() {
        return southLeftMouseGuiImageId;
    }

    public GameTipVisualConfig setSouthLeftMouseGuiImageId(Integer southLeftMouseGuiImageId) {
        this.southLeftMouseGuiImageId = southLeftMouseGuiImageId;
        return this;
    }

    public Integer getDirectionShape3DId() {
        return directionShape3DId;
    }

    public GameTipVisualConfig setDirectionShape3DId(Integer directionShape3DId) {
        this.directionShape3DId = directionShape3DId;
        return this;
    }

    public Integer getScrollDialogKeyboardImageId() {
        return scrollDialogKeyboardImageId;
    }

    public GameTipVisualConfig setScrollDialogKeyboardImageId(Integer scrollDialogKeyboardImageId) {
        this.scrollDialogKeyboardImageId = scrollDialogKeyboardImageId;
        return this;
    }
}
