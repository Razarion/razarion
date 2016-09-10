package com.btxtech.uiservice.renderer.task.slope;

import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.NormRenderer;
import com.btxtech.uiservice.renderer.RenderOrder;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class SlopeRenderTask extends AbstractRenderTask<Slope> {
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
            ModelRenderer<Slope, CommonRenderComposite<AbstractSlopeRendererUnit, Slope>, AbstractSlopeRendererUnit, Slope> modelRenderer = create();
            CommonRenderComposite<AbstractSlopeRendererUnit, Slope> renderComposite = modelRenderer.create();
            renderComposite.init(slope);
            renderComposite.setRenderUnit(rendererInstance.get());
            renderComposite.setDepthBufferRenderUnit(depthBufferRendererInstance.get());
            renderComposite.setNormRenderUnit(normRendererInstance.get());
            modelRenderer.add(RenderOrder.NORMAL, renderComposite);
            add(modelRenderer);
        }
    }

}
