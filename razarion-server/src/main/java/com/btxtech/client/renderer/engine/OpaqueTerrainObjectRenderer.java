package com.btxtech.client.renderer.engine;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.dto.VertexContainer;
import elemental.html.WebGLRenderingContext;

import javax.enterprise.context.Dependent;

/**
 * Created by Beat
 * 19.12.2015.
 */
@Dependent
public class OpaqueTerrainObjectRenderer extends AbstractTerrainObjectRenderer {

    @Override
    protected VertexContainer getVertexContainer(TerrainObjectService terrainObjectService) {
        return terrainObjectService.getOpaqueVertexContainer(getId());
    }

    @Override
    protected void preDraw(WebGLRenderingContext webGLRenderingContext) {
        webGLRenderingContext.disable(WebGLRenderingContext.BLEND);
        webGLRenderingContext.enable(WebGLRenderingContext.DEPTH_TEST);
    }
}
