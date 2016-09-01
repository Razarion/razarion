package com.btxtech.uiservice.renderer.ground;

import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.CompositeRenderer;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.NormRenderer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class GroundRenderTask extends AbstractRenderTask {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @ColorBufferRenderer
    private AbstractGroundRendererUnit rendererUnit;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DepthBufferRenderer
    private AbstractGroundRendererUnit depthBufferRendererUnit;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @NormRenderer
    private AbstractGroundRendererUnit normRendererUnit;

    @PostConstruct
    public void postConstruct() {
        CompositeRenderer compositeRenderer = new CompositeRenderer();
        compositeRenderer.setRenderUnit(rendererUnit);
        compositeRenderer.setDepthBufferRenderUnit(depthBufferRendererUnit);
        compositeRenderer.setNormRenderUnit(normRendererUnit);
        add(compositeRenderer);
    }
}
