package com.btxtech.uiservice.renderer.task.slope;

import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.NormRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.uiservice.terrain.UiTerrainSlopeTile;

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
    @DepthBufferRenderer
    private Instance<AbstractSlopeRendererUnit> depthBufferRendererInstance;
    @Inject
    @NormRenderer
    private Instance<AbstractSlopeRendererUnit> normRendererInstance;

    public ModelRenderer createModelRenderer(UiTerrainSlopeTile uiTerrainSlopeTile) {
        ModelRenderer<UiTerrainSlopeTile, CommonRenderComposite<AbstractSlopeRendererUnit, UiTerrainSlopeTile>, AbstractSlopeRendererUnit, UiTerrainSlopeTile> modelRenderer = create();
        CommonRenderComposite<AbstractSlopeRendererUnit, UiTerrainSlopeTile> renderComposite = modelRenderer.create();
        renderComposite.init(uiTerrainSlopeTile);
        renderComposite.setRenderUnit(rendererInstance.get());
        // renderComposite.setDepthBufferRenderUnit(depthBufferRendererInstance.get());
        // renderComposite.setNormRenderUnit(normRendererInstance.get());
        modelRenderer.add(RenderUnitControl.TERRAIN, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
        return modelRenderer;
    }
}
