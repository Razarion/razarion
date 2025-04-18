package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import jsinterop.annotations.JsType;

import javax.inject.Inject;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 02.05.2013
 * Time: 18:02
 */

@JsType
public class BaseItemPlacer {
    private final Logger logger = Logger.getLogger(BaseItemPlacer.class.getName());
    private final BaseItemPlacerChecker baseItemPlacerChecker;
    private final ItemTypeService itemTypeService;
    private boolean canBeCanceled;
    private Consumer<DecimalPosition> placeCallback;
    private BaseItemType baseItemType;
    private String errorText;

    @Inject
    public BaseItemPlacer(ItemTypeService itemTypeService, BaseItemPlacerChecker baseItemPlacerChecker) {
        this.itemTypeService = itemTypeService;
        this.baseItemPlacerChecker = baseItemPlacerChecker;
    }

    public BaseItemPlacer init(BaseItemPlacerConfig baseItemPlacerConfig, boolean canBeCanceled, Consumer<DecimalPosition> placeCallback) {
        baseItemType = itemTypeService.getBaseItemType(baseItemPlacerConfig.getBaseItemTypeId());
        this.canBeCanceled = canBeCanceled;
        this.placeCallback = placeCallback;
        baseItemPlacerChecker.init(baseItemType, baseItemPlacerConfig);
//        if (baseItemPlacerConfig.getSuggestedPosition() != null) {
//            onMove(new Vertex(baseItemPlacerConfig.getSuggestedPosition(), 0));
//        }
        return this;
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getEnemyFreeRadius() {
        return baseItemPlacerChecker.getEnemyFreeRadius();
    }

    @SuppressWarnings("unused") // Called by Angular
    public void onMove(double xTerrainPosition, double yTerrainPosition) {
        DecimalPosition position = new DecimalPosition(xTerrainPosition, yTerrainPosition);
        try {
            baseItemPlacerChecker.check(position);
            setupErrorText();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "BaseItemPlacer.onMove() " + position, e);
        }
    }

    @SuppressWarnings("unused") // Called by Angular
    public void onPlace(double xTerrainPosition, double yTerrainPosition) {
        DecimalPosition position = new DecimalPosition(xTerrainPosition, yTerrainPosition);
        try {
            baseItemPlacerChecker.check(position);
            setupErrorText();
            placeCallback.accept(position);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "BaseItemPlacer.onPlace() " + position, e);
        }
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean isPositionValid() {
        return baseItemPlacerChecker.isPositionValid();
    }

    public String getErrorText() {
        return errorText;
    }

    Collection<DecimalPosition> setupAbsolutePositions(DecimalPosition terrainPosition) {
        return baseItemPlacerChecker.setupAbsolutePositions(terrainPosition);
    }

    @SuppressWarnings("unused") // Called by Angular
    public Integer getModel3DId() {
        return baseItemType.getModel3DId();
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
