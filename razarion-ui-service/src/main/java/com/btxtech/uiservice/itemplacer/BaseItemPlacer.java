package com.btxtech.uiservice.itemplacer;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.renderer.ViewService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;
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
    private DecimalPosition position;
    private BaseItemType baseItemType;
    private String errorText;
    private List<Vertex> vertexes;

    public BaseItemPlacer init(BaseItemPlacerConfig baseItemPlacerConfig) {
        baseItemType = itemTypeService.getBaseItemType(baseItemPlacerConfig.getBaseItemTypeId());
        baseItemPlacerChecker.init(baseItemType, baseItemPlacerConfig);
        Circle2D circle2D = new Circle2D(new DecimalPosition(0, 0), baseItemPlacerChecker.getEnemyFreeRadius());
        vertexes = circle2D.triangulation(20, 0);
        if (baseItemPlacerConfig.getSuggestedPosition() != null) {
            onMove(baseItemPlacerConfig.getSuggestedPosition());
        } else {
            DecimalPosition cameraCenter = viewService.getCurrentViewField().calculateCenter();
            onMove(cameraCenter);
        }
        return this;
    }

    void onMove(DecimalPosition position) {
        try {
            baseItemPlacerChecker.check(position);
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
        return baseItemPlacerChecker.setupAbsolutePositions(position);
    }

//    public List<ModelMatrices> provideCircleModelMatrices() {
//        return Collections.singletonList(ModelMatrices.createFromPosition(position, nativeMatrixFactory));
//    }

//    public List<ModelMatrices> provideItemModelMatrices() {
//        List<ModelMatrices> result = new ArrayList<>();
//        for (DecimalPosition position : setupAbsolutePositions()) {
//            result.add(ModelMatrices.createFromPosition(position.getX(), position.getY(), this.position.getZ(), Colors.OWN, nativeMatrixFactory));
//        }
//        return result;
//    }

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
