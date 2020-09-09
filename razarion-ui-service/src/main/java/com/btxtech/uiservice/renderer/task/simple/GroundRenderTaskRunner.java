package com.btxtech.uiservice.renderer.task.simple;

import com.btxtech.uiservice.renderer.AbstractSimpleRenderTaskRunner;
import com.btxtech.uiservice.terrain.UiTerrainGroundTile;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class GroundRenderTaskRunner extends AbstractSimpleRenderTaskRunner<UiTerrainGroundTile> {
    public interface RenderTask extends com.btxtech.uiservice.renderer.RenderTask<UiTerrainGroundTile> {
    }

    public com.btxtech.uiservice.renderer.RenderTask createRenderTask(UiTerrainGroundTile uiTerrainGroundTile) {
        return createRenderTask(RenderTask.class, uiTerrainGroundTile);
    }
}
