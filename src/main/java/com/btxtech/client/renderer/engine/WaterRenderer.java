package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Normal;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.VertexList;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
public class WaterRenderer extends AbstractRenderer {
    private static final String A_VERTEX_POSITION = "aVertexPosition";
    private static final String PERSPECTIVE_UNIFORM_NAME = "uPMatrix";
    private static final String VIEW_UNIFORM_NAME = "uVMatrix";
    private VertexShaderAttribute positions;
    private int elementCount;
    // private Logger logger = Logger.getLogger(TerrainSurfaceWireRender.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    @Normal
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.waterVertexShader(), Shaders.INSTANCE.waterFragmentShader());
        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
    }


    @Override
    public void fillBuffers() {
        VertexList vertexList = terrainSurface.getWaterVertexList();
        if (vertexList == null) {
            elementCount = 0;
            return;
        }
        positions.fillBuffer(vertexList.getVertices());

        elementCount = vertexList.getVerticesCount();
    }


    @Override
    public void draw() {
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
        gameCanvas.getCtx3d().depthMask(false);

        useProgram();

        uniformMatrix4fv(PERSPECTIVE_UNIFORM_NAME, projectionTransformation.createMatrix());
        uniformMatrix4fv(VIEW_UNIFORM_NAME, camera.createMatrix());

        positions.activate();

        gameCanvas.getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays", gameCanvas.getCtx3d());

        gameCanvas.getCtx3d().depthMask(true);
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
    }
}
