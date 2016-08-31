package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.StartPointItemPlacerChecker;
import com.btxtech.shared.dto.StartPointConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * User: beat
 * Date: 02.05.2013
 * Time: 18:02
 */
@Dependent
public class StartPointItemPlacer {
    @Inject
    private StartPointItemPlacerChecker startPointItemPlacerChecker;
    @Inject
    private ItemTypeService itemTypeService;
    private DecimalPosition position;
    private BaseItemType baseItemType;
    private String errorText;

    public StartPointItemPlacer init(StartPointConfig startPointInfo) {
        // TODO CursorHandler.getInstance().noCursor();
        if (startPointInfo.getSuggestedPosition() != null) {
            position = startPointInfo.getSuggestedPosition();
        } else {
            throw new UnsupportedOperationException("Default position (screen middle) not supported yet");
            // position = new BaseItemType(Window.getClientWidth() / 2, Window.getClientHeight() / 2);
        }
        // TODO TerrainView.getInstance().setFocus();
        baseItemType = (BaseItemType) itemTypeService.getItemType(startPointInfo.getBaseItemTypeId());
        startPointItemPlacerChecker.init(baseItemType, startPointInfo.getEnemyFreeRadius(), startPointInfo.getAllowedArea());
        onMove(position);
        return this;
    }

    public void onMove(DecimalPosition position) {
        startPointItemPlacerChecker.check(position);
        setupErrorText();
    }

    public double getItemFreeRadius() {
        return startPointItemPlacerChecker.getEnemyFreeRadius();
    }

    public boolean isPositionValid() {
        return startPointItemPlacerChecker.isPositionValid();
    }

    public DecimalPosition getPosition() {
        return position;
    }

    public String getErrorText() {
        return errorText;
    }

    public BaseItemType getBaseItemType() {
        return baseItemType;
    }

    private void setupErrorText() {
        // TODO
//        if (!startPointItemPlacerChecker.isEnemiesOk()) {
//            errorText = ClientI18nHelper.CONSTANTS.enemyTooNear();
//        } else if (!startPointItemPlacerChecker.isItemsOk()) {
//            errorText = ClientI18nHelper.CONSTANTS.notPlaceOver();
//        } else if (!startPointItemPlacerChecker.isTerrainOk()) {
//            errorText = ClientI18nHelper.CONSTANTS.notPlaceHere();
//        } else {
//            errorText = null;
//        }
    }
}
