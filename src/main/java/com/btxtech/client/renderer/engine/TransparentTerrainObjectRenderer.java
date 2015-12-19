package com.btxtech.client.renderer.engine;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.VertexList;
import elemental.html.WebGLRenderingContext;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 19.12.2015.
 */
@Dependent
public class TransparentTerrainObjectRenderer extends AbstractTerrainObjectRenderer {
    @Override
    protected VertexList getVertexList(TerrainObjectService terrainObjectService) {
        return terrainObjectService.getTransparentVertexList();
    }

    @Override
    protected ImageDescriptor getImageDescriptor(TerrainObjectService terrainObjectService) {
        return terrainObjectService.getTransparentDescriptor();
    }

    @Override
    protected void preDraw(WebGLRenderingContext webGLRenderingContext) {
        webGLRenderingContext.blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
        webGLRenderingContext.enable(WebGLRenderingContext.BLEND);
        webGLRenderingContext.disable(WebGLRenderingContext.DEPTH_TEST);
    }
}
