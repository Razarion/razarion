package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
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

    public DecimalPosition getSuggestedPosition() {
        return suggestedPosition;
    }

    public void setSuggestedPosition(DecimalPosition suggestedPosition) {
        this.suggestedPosition = suggestedPosition;
    }

    public int getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public void setBaseItemTypeId(int baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
    }

    public int getBaseItemCount() {
        return baseItemCount;
    }

    public void setBaseItemCount(int baseItemCount) {
        this.baseItemCount = baseItemCount;
    }

    public Double getEnemyFreeRadius() {
        return enemyFreeRadius;
    }

    public void setEnemyFreeRadius(Double enemyFreeRadius) {
        this.enemyFreeRadius = enemyFreeRadius;
    }

    public PlaceConfig getAllowedArea() {
        return allowedArea;
    }

    public void setAllowedArea(PlaceConfig allowedArea) {
        this.allowedArea = allowedArea;
    }

    public BaseItemPlacerConfig suggestedPosition(DecimalPosition suggestedPosition) {
        setSuggestedPosition(suggestedPosition);
        return this;
    }

    public BaseItemPlacerConfig baseItemTypeId(int baseItemTypeId) {
        setBaseItemTypeId(baseItemTypeId);
        return this;
    }

    public BaseItemPlacerConfig baseItemCount(int baseItemCount) {
        setBaseItemCount(baseItemCount);
        return this;
    }

    public BaseItemPlacerConfig enemyFreeRadius(Double enemyFreeRadius) {
        setEnemyFreeRadius(enemyFreeRadius);
        return this;
    }

    public BaseItemPlacerConfig allowedArea(PlaceConfig allowedArea) {
        setAllowedArea(allowedArea);
        return this;
    }
}
