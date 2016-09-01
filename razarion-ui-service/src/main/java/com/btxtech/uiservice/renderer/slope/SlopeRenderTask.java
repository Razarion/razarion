package com.btxtech.uiservice.renderer.slope;

import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.CompositeRenderer;
import com.btxtech.uiservice.renderer.NormRenderer;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class SlopeRenderTask extends AbstractRenderTask {
    @Inject
    @ColorBufferRenderer
    private Instance<AbstractSlopeRendererUnit> rendererInstance;
    @Inject
    @ColorBufferRenderer
    private Instance<AbstractSlopeRendererUnit> depthBufferRendererInstance;
    @Inject
    @NormRenderer
    private Instance<AbstractSlopeRendererUnit> normRendererInstance;
    @Inject
    private TerrainService terrainService;

    @PostConstruct
    public void postConstruct() {
        for (Slope slope : terrainService.getSlopes()) {
            CompositeRenderer compositeRenderer = new CompositeRenderer();
            AbstractSlopeRendererUnit slopeUnitRenderer = rendererInstance.get();
            slopeUnitRenderer.setSlope(slope);
            compositeRenderer.setRenderUnit(slopeUnitRenderer);
            AbstractSlopeRendererUnit slopeDepthBufferUnitRenderer = depthBufferRendererInstance.get();
            slopeDepthBufferUnitRenderer.setSlope(slope);
            compositeRenderer.setDepthBufferRenderUnit(slopeDepthBufferUnitRenderer);
            AbstractSlopeRendererUnit slopeNormUnitRenderer = normRendererInstance.get();
            slopeNormUnitRenderer.setSlope(slope);
            compositeRenderer.setNormRenderUnit(slopeNormUnitRenderer);
            add(compositeRenderer);
        }
    }

}
