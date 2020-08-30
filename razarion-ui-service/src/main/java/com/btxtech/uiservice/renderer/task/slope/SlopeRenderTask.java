package com.btxtech.uiservice.renderer.task.slope;

import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.uiservice.terrain.UiTerrainSlopeTile;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class SlopeRenderTask extends AbstractRenderTask<Slope> {
    public ModelRenderer createModelRenderer(UiTerrainSlopeTile uiTerrainSlopeTile) {
        ModelRenderer<UiTerrainSlopeTile> modelRenderer = create();
        CommonRenderComposite<AbstractSlopeRendererUnit, UiTerrainSlopeTile> renderComposite = modelRenderer.create();
        renderComposite.init(uiTerrainSlopeTile);
        renderComposite.setRenderUnit(AbstractSlopeRendererUnit.class);
        // renderComposite.setDepthBufferRenderUnit(depthBufferRendererInstance.get());
        // renderComposite.setNormRenderUnit(normRendererInstance.get());
        modelRenderer.add(RenderUnitControl.TERRAIN, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
        return modelRenderer;
    }
}
