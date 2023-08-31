package com.btxtech.shared.dto;

import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;

/**
 * User: beat
 * Date: 01.05.13
 * Time: 13:00
 */
public class BaseItemPlacerConfig {
    private DecimalPosition suggestedPosition;
    @CollectionReference(CollectionReferenceType.BASE_ITEM)
    private int baseItemTypeId;
    private int baseItemCount;
    private Double enemyFreeRadius;
    private PlaceConfig allowedArea;

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

    public Double getEnemyFreeRadius() {
        return enemyFreeRadius;
    }

    public BaseItemPlacerConfig setEnemyFreeRadius(Double enemyFreeRadius) {
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

    public PlaceConfig getAllowedArea() {
        return allowedArea;
    }

    public BaseItemPlacerConfig setAllowedArea(PlaceConfig allowedArea) {
        this.allowedArea = allowedArea;
        return this;
    }

}
