package com.btxtech.uiservice.renderer.task.startpoint;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.StartPointItemPlacerChecker;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.StartPointConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collections;
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
    @Inject
    private TerrainService terrainService;
    private Vertex position;
    private BaseItemType baseItemType;
    private String errorText;
    private List<Vertex> vertexes;

    public StartPointItemPlacer init(StartPointConfig startPointConfig) {
        // TODO CursorHandler.getInstance().noCursor();
        // TODO TerrainView.getInstance().setFocus();
        baseItemType = (BaseItemType) itemTypeService.getItemType(startPointConfig.getBaseItemTypeId());
        startPointItemPlacerChecker.init(baseItemType, startPointConfig.getEnemyFreeRadius(), startPointConfig.getAllowedArea());
        Circle2D circle2D = new Circle2D(new DecimalPosition(0, 0), startPointConfig.getEnemyFreeRadius());
        vertexes = circle2D.triangulation(20, 0);
        if (startPointConfig.getSuggestedPosition() != null) {
            onMove(terrainService.calculatePositionGroundMesh(startPointConfig.getSuggestedPosition()));
        } else {
            throw new UnsupportedOperationException("Default position (screen middle) not supported yet");
            // onMove(new BaseItemType(Window.getClientWidth() / 2, Window.getClientHeight() / 2));
        }
        return this;
    }

    public void onMove(Vertex position) {
        startPointItemPlacerChecker.check(position.toXY());
        setupErrorText();
        this.position = position;
    }

    public boolean isPositionValid() {
        return startPointItemPlacerChecker.isPositionValid();
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

    public List<ModelMatrices> provideModelMatrices() {
        Matrix4 model = Matrix4.createTranslation(position.getX(), position.getY(), position.getZ());
        ModelMatrices modelMatrices = new ModelMatrices();
        modelMatrices.setModel(model).setNorm(model.normTransformation());
        return Collections.singletonList(modelMatrices);
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
