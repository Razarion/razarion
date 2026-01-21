package com.btxtech.uiservice.questvisualization;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.MarkerConfig;
import com.btxtech.uiservice.renderer.ViewField;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 04.03.2018.
 */
public class QuestInGamePlaceVisualization {

    private final BabylonRendererService babylonRendererService;
    private PlaceConfig placeConfig;
    private DecimalPosition center;
    private Rectangle2D placeConfigBoundaryRect;
    private MarkerConfig markerConfig;

    @Inject
    public QuestInGamePlaceVisualization(BabylonRendererService babylonRendererService) {
        this.babylonRendererService = babylonRendererService;
    }

    public void init(PlaceConfig placeConfig, MarkerConfig markerConfig) {
        this.placeConfig = placeConfig;
        placeConfigBoundaryRect = placeConfig.toAabb();
        if (placeConfigBoundaryRect == null && placeConfig.getPosition() != null) {
            // No radius set
            placeConfigBoundaryRect = new Rectangle2D(
                    placeConfig.getPosition().getX() - 1,
                    placeConfig.getPosition().getY() - 1,
                    2,
                    2);
        }
        this.markerConfig = markerConfig;
        center = placeConfigBoundaryRect.center();
    }

    public void onViewChanged(ViewField viewField, Rectangle2D viewFieldAabb) {
        updateVisualization(viewField, viewFieldAabb);
    }

    public void release() {
        babylonRendererService.showOutOfViewMarker(null, 0);
        babylonRendererService.showPlaceMarker(null, null);
    }

    private void updateVisualization(ViewField viewField, Rectangle2D viewFieldAabb) {
        boolean outOfView = true;
        if (viewFieldAabb.adjoins(placeConfigBoundaryRect)) {
            outOfView = !placeConfig.checkAdjoins(viewField.calculateInnerAabbRectangle());
        }
        if (outOfView) {
            double angle = viewField.calculateCenter().getAngle(this.center);
            babylonRendererService.showOutOfViewMarker(markerConfig, angle);
            babylonRendererService.showPlaceMarker(null, null);
        } else {
            babylonRendererService.showOutOfViewMarker(null, 0);
            babylonRendererService.showPlaceMarker(placeConfig, markerConfig);
        }

    }
}
