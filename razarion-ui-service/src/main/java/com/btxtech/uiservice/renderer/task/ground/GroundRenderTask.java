package com.btxtech.uiservice.renderer.task.ground;

import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.terrain.UiTerrainTile;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class GroundRenderTask extends AbstractRenderTask<UiTerrainTile> {
    @Inject
    private TerrainUiService terrainUiService;

    public ModelRenderer createModelRenderer(UiTerrainTile uiTerrainTile) {
        ModelRenderer<UiTerrainTile, CommonRenderComposite<AbstractGroundRendererUnit, UiTerrainTile>, AbstractGroundRendererUnit, UiTerrainTile> modelRenderer = create();
        CommonRenderComposite<AbstractGroundRendererUnit, UiTerrainTile> renderComposite = modelRenderer.create();
        renderComposite.init(uiTerrainTile);
        renderComposite.setRenderUnit(AbstractGroundRendererUnit.class);
        renderComposite.setDepthBufferRenderUnit(AbstractGroundRendererUnit.class);
        renderComposite.setNormRenderUnit(AbstractGroundRendererUnit.class);
        modelRenderer.add(RenderUnitControl.TERRAIN, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
        return modelRenderer;
    }
}
