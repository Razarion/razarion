package com.btxtech.uiservice.renderer.task.slope;

import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderSubTask;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.uiservice.terrain.UiTerrainSlopeTile;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class SlopeRenderTask extends AbstractRenderTask<UiTerrainSlopeTile> {
    public interface SubTask extends RenderSubTask<UiTerrainSlopeTile> {
    }

    public ModelRenderer createModelRenderer(UiTerrainSlopeTile uiTerrainSlopeTile) {
        ModelRenderer<UiTerrainSlopeTile> modelRenderer = createNew();
        modelRenderer.create(RenderUnitControl.TERRAIN, SlopeRenderTask.SubTask.class, uiTerrainSlopeTile);
        return modelRenderer;
    }
}
