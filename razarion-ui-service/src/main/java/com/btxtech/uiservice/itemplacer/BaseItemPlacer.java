package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: beat
 * Date: 02.05.2013
 * Time: 18:02
 */
@Dependent
public class BaseItemPlacer {
    @Inject
    private BaseItemPlacerChecker baseItemPlacerChecker;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    private Vertex position;
    private BaseItemType baseItemType;
    private String errorText;
    private List<Vertex> vertexes;

    public BaseItemPlacer init(BaseItemPlacerConfig baseItemPlacerConfig) {
        baseItemType = itemTypeService.getBaseItemType(baseItemPlacerConfig.getBaseItemTypeId());
        baseItemPlacerChecker.init(baseItemType, baseItemPlacerConfig);
        Circle2D circle2D = new Circle2D(new DecimalPosition(0, 0), baseItemPlacerChecker.getEnemyFreeRadius());
        vertexes = circle2D.triangulation(20, 0);
        if (baseItemPlacerConfig.getSuggestedPosition() != null) {
            onMove(new Vertex(baseItemPlacerConfig.getSuggestedPosition(), 0));
        } else {
            DecimalPosition cameraCenter = terrainScrollHandler.getCurrentViewField().calculateCenter();
            onMove(new Vertex(cameraCenter, 0));
        }
        return this;
    }

    void onMove(Vertex position) {
        baseItemPlacerChecker.check(position.toXY());
        setupErrorText();
        this.position = position;
    }

    public boolean isPositionValid() {
        return baseItemPlacerChecker.isPositionValid();
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

    Collection<DecimalPosition> setupAbsolutePositions() {
        return baseItemPlacerChecker.setupAbsolutePositions(position.toXY());
    }

    public List<ModelMatrices> provideCircleModelMatrices() {
        Matrix4 model = Matrix4.createTranslation(position);
        return Collections.singletonList(new ModelMatrices(model));
    }

    public List<ModelMatrices> provideItemModelMatrices() {
        List<ModelMatrices> result = new ArrayList<>();
        for (DecimalPosition position : setupAbsolutePositions()) {
            Matrix4 model = Matrix4.createTranslation(position.getX(), position.getY(), this.position.getZ());
            result.add(new ModelMatrices(model));
        }
        return result;
    }

    private void setupErrorText() {
        // TODO
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
