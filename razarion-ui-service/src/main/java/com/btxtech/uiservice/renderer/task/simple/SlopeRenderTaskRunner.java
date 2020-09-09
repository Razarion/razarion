package com.btxtech.uiservice.renderer.task.simple;

import com.btxtech.uiservice.renderer.AbstractSimpleRenderTaskRunner;
import com.btxtech.uiservice.terrain.UiTerrainSlopeTile;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class SlopeRenderTaskRunner extends AbstractSimpleRenderTaskRunner<UiTerrainSlopeTile> {
    public interface RenderTask extends com.btxtech.uiservice.renderer.RenderTask<UiTerrainSlopeTile> {
    }

    public com.btxtech.uiservice.renderer.RenderTask createRenderTask(UiTerrainSlopeTile uiTerrainSlopeTile) {
        return createRenderTask(RenderTask.class, uiTerrainSlopeTile);
    }
}
