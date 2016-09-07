package com.btxtech.uiservice.renderer.task.startpoint;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.StartPointItemPlacerChecker;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.StartPointConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

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
    private List<Vertex> vertexes;

    public StartPointItemPlacer init(StartPointConfig startPointConfig) {
        // TODO CursorHandler.getInstance().noCursor();
        if (startPointConfig.getSuggestedPosition() != null) {
            position = startPointConfig.getSuggestedPosition();
        } else {
            throw new UnsupportedOperationException("Default position (screen middle) not supported yet");
            // position = new BaseItemType(Window.getClientWidth() / 2, Window.getClientHeight() / 2);
        }
        // TODO TerrainView.getInstance().setFocus();
        baseItemType = (BaseItemType) itemTypeService.getItemType(startPointConfig.getBaseItemTypeId());
        startPointItemPlacerChecker.init(baseItemType, startPointConfig.getEnemyFreeRadius(), startPointConfig.getAllowedArea());
        Circle2D circle2D = new Circle2D(new DecimalPosition(0, 0), startPointConfig.getEnemyFreeRadius());
        vertexes = circle2D.triangulation(10, 0);
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

    public List<Vertex> getVertexes() {
        return vertexes;
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
