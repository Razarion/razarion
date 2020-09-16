package com.btxtech.uiservice.renderer.task.simple;

import com.btxtech.uiservice.renderer.AbstractRenderTaskRunner;
import com.btxtech.uiservice.renderer.WebGlRenderTask;
import com.btxtech.uiservice.terrain.UiTerrainGroundTile;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class GroundRenderTaskRunner extends AbstractRenderTaskRunner<UiTerrainGroundTile, GroundRenderTaskRunner.RenderTask> {
    public interface RenderTask extends WebGlRenderTask<UiTerrainGroundTile> {
    }

    public RenderTask createRenderTask(UiTerrainGroundTile uiTerrainGroundTile) {
        return createRenderTask(RenderTask.class, uiTerrainGroundTile);
    }
}
