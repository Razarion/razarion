package com.btxtech.uiservice.renderer.water;

import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.CompositeRenderer;
import com.btxtech.uiservice.renderer.NormRenderer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class WaterRenderTask extends AbstractRenderTask {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @ColorBufferRenderer
    private AbstractWaterRendererUnit rendererInstance;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @NormRenderer
    private AbstractWaterRendererUnit normRendererInstance;

    @PostConstruct
    public void postConstruct() {
        CompositeRenderer compositeRenderer = new CompositeRenderer();
        compositeRenderer.setRenderUnit(rendererInstance);
        compositeRenderer.setNormRenderUnit(normRendererInstance);
        add(compositeRenderer);
    }

}
