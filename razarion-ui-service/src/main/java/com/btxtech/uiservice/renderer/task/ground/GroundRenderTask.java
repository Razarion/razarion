package com.btxtech.uiservice.renderer.task.ground;

import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderSubTask;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.uiservice.terrain.UiTerrainGroundTile;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class GroundRenderTask extends AbstractRenderTask<UiTerrainGroundTile> {
    public interface SubTask extends RenderSubTask<UiTerrainGroundTile> {
    }

    public ModelRenderer createModelRenderer(UiTerrainGroundTile uiTerrainGroundTile) {
        ModelRenderer<UiTerrainGroundTile> modelRenderer = createNew();
        modelRenderer.create(RenderUnitControl.TERRAIN, SubTask.class, uiTerrainGroundTile);
        return modelRenderer;
    }
}
