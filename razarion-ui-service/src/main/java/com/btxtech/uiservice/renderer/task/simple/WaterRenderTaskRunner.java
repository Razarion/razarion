package com.btxtech.uiservice.renderer.task.simple;

import com.btxtech.uiservice.renderer.AbstractRenderTaskRunner;
import com.btxtech.uiservice.renderer.WebGlRenderTask;
import com.btxtech.uiservice.terrain.UiTerrainWaterTile;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class WaterRenderTaskRunner extends AbstractRenderTaskRunner<UiTerrainWaterTile, WaterRenderTaskRunner.RenderTask> {
    public interface RenderTask extends WebGlRenderTask<UiTerrainWaterTile> {
    }

    public RenderTask createRenderTask(UiTerrainWaterTile uiTerrainWaterTile) {
        return createRenderTask(RenderTask.class, uiTerrainWaterTile);
    }
}
