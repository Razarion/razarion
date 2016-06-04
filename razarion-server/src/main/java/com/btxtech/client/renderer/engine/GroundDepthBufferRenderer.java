package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.model.ShadowUiService;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
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
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String BARYCENTRIC_ATTRIBUTE_NAME = "aBarycentric";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private static final String MODEL_UNIFORM_NAME = "uMMatrix";
    private VertexShaderAttribute vertices;
    private VertexShaderAttribute barycentric;
    private int elementCount;
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
        barycentric = createVertexShaderAttribute(BARYCENTRIC_ATTRIBUTE_NAME);
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

        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();
        // Projection uniform
        uniformMatrix4fv(PERSPECTIVE_UNIFORM_NAME, shadowUiService.createDepthProjectionTransformation());
        uniformMatrix4fv(VIEW_UNIFORM_NAME, shadowUiService.createDepthViewTransformation());
        uniformMatrix4fv(MODEL_UNIFORM_NAME, Matrix4.createIdentity());

        vertices.activate();
        barycentric.activate();

        // Draw
        getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
    }
}
