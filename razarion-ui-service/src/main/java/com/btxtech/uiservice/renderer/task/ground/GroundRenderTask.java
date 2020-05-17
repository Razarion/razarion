package com.btxtech.uiservice.renderer.task.ground;

import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.uiservice.terrain.UiTerrainGroundTile;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class GroundRenderTask extends AbstractRenderTask<UiTerrainGroundTile> {

    public ModelRenderer createModelRenderer(UiTerrainGroundTile uiTerrainGroundTile) {
        ModelRenderer<UiTerrainGroundTile, CommonRenderComposite<AbstractGroundRendererUnit, UiTerrainGroundTile>, AbstractGroundRendererUnit, UiTerrainGroundTile> modelRenderer = create();
        CommonRenderComposite<AbstractGroundRendererUnit, UiTerrainGroundTile> renderComposite = modelRenderer.create();
        renderComposite.init(uiTerrainGroundTile);
        renderComposite.setRenderUnit(AbstractGroundRendererUnit.class);
        // TODO renderComposite.setDepthBufferRenderUnit(AbstractGroundRendererUnit.class);
        // TODO renderComposite.setNormRenderUnit(AbstractGroundRendererUnit.class);
        modelRenderer.add(RenderUnitControl.TERRAIN, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
        return modelRenderer;
    }
}
