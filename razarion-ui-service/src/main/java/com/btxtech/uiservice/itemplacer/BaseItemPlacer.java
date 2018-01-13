package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.renderer.ViewService;

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
    private ViewService viewService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    @Inject
    private ExceptionHandler exceptionHandler;
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
            DecimalPosition cameraCenter = viewService.getCurrentViewField().calculateCenter();
            onMove(new Vertex(cameraCenter, 0));
        }
        return this;
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
        return Collections.singletonList(ModelMatrices.createFromPosition(position, nativeMatrixFactory));
    }

    public List<ModelMatrices> provideItemModelMatrices() {
        List<ModelMatrices> result = new ArrayList<>();
        for (DecimalPosition position : setupAbsolutePositions()) {
            result.add(ModelMatrices.createFromPosition(position.getX(), position.getY(), this.position.getZ(), nativeMatrixFactory));
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
