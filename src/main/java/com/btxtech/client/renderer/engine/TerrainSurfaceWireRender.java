package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Vertex;
import elemental.html.WebGLBuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLUniformLocation;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Dependent
public class TerrainSurfaceWireRender extends AbstractViewPerspectiveWireRenderer {
    @Inject
    private TerrainSurface terrainSurface;

    @Override
    protected List<Vertex> getVertexList() {
        return terrainSurface.getVertexList().getVertices();
    }

    @Override
    protected List<Vertex> getBarycentricList() {
        return terrainSurface.getVertexList().getBarycentric();
    }
}
