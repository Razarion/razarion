package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * User: beat
 * Date: 01.05.13
 * Time: 13:00
 */
@Portable
public class StartPointConfig {
    private DecimalPosition suggestedPosition;
    private int baseItemTypeId;
    private double enemyFreeRadius;
    private Polygon2D allowedArea;

    public int getBaseItemTypeId() {
        return baseItemTypeId;
    }

    public StartPointConfig setBaseItemTypeId(int baseItemTypeId) {
        this.baseItemTypeId = baseItemTypeId;
        return this;
    }

    public double getEnemyFreeRadius() {
        return enemyFreeRadius;
    }

    public StartPointConfig setEnemyFreeRadius(double enemyFreeRadius) {
        this.enemyFreeRadius = enemyFreeRadius;
        return this;
    }

    public DecimalPosition getSuggestedPosition() {
        return suggestedPosition;
    }

    public StartPointConfig setSuggestedPosition(DecimalPosition suggestedPosition) {
        this.suggestedPosition = suggestedPosition;
        return this;
    }

    public Polygon2D getAllowedArea() {
        return allowedArea;
    }

    public StartPointConfig setAllowedArea(Polygon2D allowedArea) {
        this.allowedArea = allowedArea;
        return this;
   }
}
