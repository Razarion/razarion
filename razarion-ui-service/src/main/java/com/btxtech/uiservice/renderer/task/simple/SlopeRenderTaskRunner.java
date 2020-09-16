package com.btxtech.uiservice.renderer.task.simple;

import com.btxtech.uiservice.renderer.AbstractSimpleRenderTaskRunner;
import com.btxtech.uiservice.renderer.WebGlRenderTask;
import com.btxtech.uiservice.terrain.UiTerrainSlopeTile;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class SlopeRenderTaskRunner extends AbstractSimpleRenderTaskRunner<UiTerrainSlopeTile> {
    public interface RenderTask extends WebGlRenderTask<UiTerrainSlopeTile> {
    }

    public WebGlRenderTask createRenderTask(UiTerrainSlopeTile uiTerrainSlopeTile) {
        return createRenderTask(RenderTask.class, uiTerrainSlopeTile);
    }
}
