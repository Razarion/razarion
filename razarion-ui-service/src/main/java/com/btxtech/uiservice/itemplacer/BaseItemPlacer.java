package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.system.ExceptionHandler;
import jsinterop.annotations.JsType;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;

/**
 * User: beat
 * Date: 02.05.2013
 * Time: 18:02
 */
@Dependent
@JsType
public class BaseItemPlacer {
    @Inject
    private BaseItemPlacerChecker baseItemPlacerChecker;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private Vertex position;
    private BaseItemType baseItemType;
    private String errorText;

    public BaseItemPlacer init(BaseItemPlacerConfig baseItemPlacerConfig) {
        baseItemType = itemTypeService.getBaseItemType(baseItemPlacerConfig.getBaseItemTypeId());
        baseItemPlacerChecker.init(baseItemType, baseItemPlacerConfig);
        if (baseItemPlacerConfig.getSuggestedPosition() != null) {
            onMove(new Vertex(baseItemPlacerConfig.getSuggestedPosition(), 0));
        }
        return this;
    }

    public double getEnemyFreeRadius() {
        return baseItemPlacerChecker.getEnemyFreeRadius();
    }

    void onMove(Vertex position) {
        try {
            baseItemPlacerChecker.check(position.toXY());
            setupErrorText();
            this.position = position;
        } catch (Exception e) {
            exceptionHandler.handleException("BaseItemPlacer.onMove() " + position, e);
        }
    }

    public boolean isPositionValid() {
        return baseItemPlacerChecker.isPositionValid();
    }

    public Vertex getPosition() {
        return position;
    }

    public String getErrorText() {
        return errorText;
    }

    public BaseItemType getBaseItemType() {
        return baseItemType;
    }

    Collection<DecimalPosition> setupAbsolutePositions() {
        return baseItemPlacerChecker.setupAbsolutePositions(position.toXY());
    }

    private void setupErrorText() {
//        if (!baseItemPlacerChecker.isEnemiesOk()) {
//            errorText = ClientI18nHelper.getConstants().enemyTooNear();
//        } else if (!baseItemPlacerChecker.isItemsOk()) {
//            errorText = ClientI18nHelper.getConstants().notPlaceOver();
//        } else if (!baseItemPlacerChecker.isTerrainOk()) {
//            errorText = ClientI18nHelper.getConstants().notPlaceHere();
//        } else {
//            errorText = null;
//        }
    }
}
