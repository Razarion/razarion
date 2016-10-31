package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;

import java.util.List;

/**
 * User: beat
 * Date: 01.05.13
 * Time: 13:00
 */
public class BaseItemPlacerConfig {
    private DecimalPosition suggestedPosition;
    private int baseItemTypeId;
    private int baseItemCount;
    private double enemyFreeRadius;
    private Polygon2D allowedArea;

    public int getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public BaseItemPlacerConfig setBaseItemTypeId(int baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
        return this;
    }

    public int getBaseItemCount() {
        return baseItemCount;
    }

    public BaseItemPlacerConfig setBaseItemCount(int baseItemCount) {
        this.baseItemCount = baseItemCount;
        return this;
    }

    public double getEnemyFreeRadius() {
        return enemyFreeRadius;
    }

    public BaseItemPlacerConfig setEnemyFreeRadius(double enemyFreeRadius) {
        this.enemyFreeRadius = enemyFreeRadius;
        return this;
    }

    public DecimalPosition getSuggestedPosition() {
        return suggestedPosition;
    }

    public BaseItemPlacerConfig setSuggestedPosition(DecimalPosition suggestedPosition) {
        this.suggestedPosition = suggestedPosition;
        return this;
    }

    public Polygon2D getAllowedArea() {
        return allowedArea;
    }

    public BaseItemPlacerConfig setAllowedArea(Polygon2D allowedArea) {
        this.allowedArea = allowedArea;
        return this;
    }
}
