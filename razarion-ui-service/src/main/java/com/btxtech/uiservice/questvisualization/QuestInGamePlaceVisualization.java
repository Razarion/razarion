package com.btxtech.uiservice.questvisualization;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.renderer.MarkerConfig;
import com.btxtech.uiservice.renderer.ViewField;

import jakarta.inject.Inject;

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
    // Tracks the currently displayed state so the (expensive) bridge calls only
    // run on a transition. onViewChanged fires on every camera/scroll move, and
    // showPlaceMarker re-converts the whole PlaceConfig (polygon corners) across
    // the WASM bridge each call — re-sending it every frame churns GC and stutters
    // scrolling, even though the TS side just early-returns on the existing mesh.
    // null = nothing displayed yet (force the first call).
    private Boolean lastOutOfView;

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
        lastOutOfView = null;
    }

    private void updateVisualization(ViewField viewField, Rectangle2D viewFieldAabb) {
        boolean outOfView = true;
        if (viewFieldAabb.adjoins(placeConfigBoundaryRect)) {
            outOfView = !placeConfig.checkAdjoins(viewField.calculateInnerAabbRectangle());
        }
        boolean transitioned = lastOutOfView == null || lastOutOfView != outOfView;
        if (outOfView) {
            // The out-of-view marker's angle changes continuously while scrolling,
            // so it must keep updating; but the (place) marker only needs clearing
            // once, on the transition out of view.
            double angle = viewField.calculateCenter().getAngle(this.center);
            babylonRendererService.showOutOfViewMarker(markerConfig, angle);
            if (transitioned) {
                babylonRendererService.showPlaceMarker(null, null);
            }
        } else if (transitioned) {
            babylonRendererService.showOutOfViewMarker(null, 0);
            babylonRendererService.showPlaceMarker(placeConfig, markerConfig);
        }
        lastOutOfView = outOfView;
    }
}
