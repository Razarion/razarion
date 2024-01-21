package com.btxtech.uiservice.questvisualization;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.dto.InGameQuestVisualConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.utils.InterpolationUtils;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.ViewService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 04.03.2018.
 */
@Dependent
public class QuestInGamePlaceVisualization {
    private static final long ANIMATION_PERIOD_MILLIS = 2000;
    @Inject
    private ViewService viewService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    private InGameQuestVisualConfig inGameQuestVisualConfig;
    private PlaceConfig placeConfig;
    private DecimalPosition center;
    private DecimalPosition lastViewCenter;
    private Vertex4 placeConfigBoundary;
    private Rectangle2D placeConfigBoundaryRect;
    private List<ModelMatrices> lastOutOfViewModelMatrices;

    public void init(PlaceConfig placeConfig, InGameQuestVisualConfig inGameQuestVisualConfig) {
        this.placeConfig = placeConfig;
        placeConfigBoundaryRect = placeConfig.toAabb();
        center = placeConfigBoundaryRect.center();
        placeConfigBoundary = new Vertex4(placeConfigBoundaryRect.startX(), placeConfigBoundaryRect.startY(), placeConfigBoundaryRect.endX(), placeConfigBoundaryRect.endY());
        this.inGameQuestVisualConfig = inGameQuestVisualConfig;
    }

    public PlaceConfig getPlaceConfig() {
        return placeConfig;
    }

    public Vertex4 getPlaceConfigBoundary() {
        return placeConfigBoundary;
    }

    public Rectangle2D getPlaceConfigBoundaryRect() {
        return placeConfigBoundaryRect;
    }

    public Integer getOutOfViewShape3DId() {
        return inGameQuestVisualConfig.getOutOfViewNodesMaterialId();
    }

    public List<ModelMatrices> provideOutOfViewModelMatrices() {
        DecimalPosition viewCenter = viewService.getCurrentViewField().calculateCenter();
        if (lastViewCenter != null && lastViewCenter.equalsDelta(viewCenter)) {
            return lastOutOfViewModelMatrices;
        }
        lastViewCenter = viewCenter;
        boolean outOfView = true;
        if (viewService.getCurrentInnerAabb().adjoins(placeConfigBoundaryRect)) {
            outOfView = !placeConfig.checkAdjoins(viewService.getCurrentInnerAabb());
        }
        if (outOfView) {
            double angle = viewCenter.getAngle(this.center);
            lastOutOfViewModelMatrices = Collections.singletonList(ModelMatrices.createFromPositionAndZRotation(viewCenter.getX(), viewCenter.getY(), 0, angle, nativeMatrixFactory));
            return lastOutOfViewModelMatrices;
        } else {
            lastOutOfViewModelMatrices = null;
            return null;
        }
    }

    public double getAnimation() {
        double sinValue = Math.sin(((System.currentTimeMillis() % ANIMATION_PERIOD_MILLIS) / (double) ANIMATION_PERIOD_MILLIS) * MathHelper.ONE_RADIANT);
        return InterpolationUtils.interpolate(0, 1, -1, 1, sinValue);
    }

}
