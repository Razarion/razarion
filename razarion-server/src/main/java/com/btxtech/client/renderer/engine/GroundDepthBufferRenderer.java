package com.btxtech.client.renderer.engine;

import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.uiservice.terrain.TerrainSurface;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class GroundDepthBufferRenderer extends AbstractRenderer {
    // private Logger logger = Logger.getLogger(GroundDepthBufferRenderer.class.getName());
    private VertexShaderAttribute vertices;
    private VertexShaderAttribute barycentric;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private ShadowUiService shadowUiService;

    @PostConstruct
    public void init() {
        Object extension = getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }
        createProgram(Shaders.INSTANCE.depthBufferVertexShader(), Shaders.INSTANCE.depthBufferFragmentShader());
        vertices = createVertexShaderAttribute(A_VERTEX_POSITION);
        barycentric = createVertexShaderAttribute(A_BARYCENTRIC);
    }

    @Override
    public void setupImages() {
        // Ignore
    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = terrainSurface.getGroundVertexList();
        vertices.fillBuffer(vertexList.getVertices());
        barycentric.fillBuffer(vertexList.getBarycentric());
        setElementCount(vertexList);
    }

    @Override
    public void draw() {
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();
        // Projection uniform
        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, shadowUiService.createDepthProjectionTransformation());
        uniformMatrix4fv(U_VIEW_MATRIX, shadowUiService.createDepthViewTransformation());
        uniformMatrix4fv(U_MODEL_MATRIX, Matrix4.createIdentity());

        vertices.activate();
        barycentric.activate();

        // Draw
        drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
