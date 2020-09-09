package com.btxtech.uiservice.renderer.task.water;

import com.btxtech.uiservice.renderer.AbstractModelRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.uiservice.terrain.UiTerrainWaterTile;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class WaterRenderTask extends AbstractModelRenderTask<UiTerrainWaterTile> {

    public ModelRenderer createModelRenderer(UiTerrainWaterTile uiTerrainWaterTile) {
        ModelRenderer<UiTerrainWaterTile> modelRenderer = create();
        CommonRenderComposite<AbstractWaterRendererUnit, UiTerrainWaterTile> renderComposite = modelRenderer.create();
        renderComposite.init(uiTerrainWaterTile);
        renderComposite.setRenderUnit(AbstractWaterRendererUnit.class);
        // renderComposite.setNormRenderUnit(AbstractWaterRendererUnit.class);
        modelRenderer.add(RenderUnitControl.WATER, renderComposite);
        add(modelRenderer);
        return modelRenderer;
    }
}
