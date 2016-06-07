package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.model.ShadowUiService;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.gameengine.pathing.ModelMatrices;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
abstract public class AbstractTerrainObjectDepthBufferRenderer extends AbstractRenderer {
    // private Logger logger = Logger.getLogger(TerrainDepthBufferObjectRenderer.class.getName());
    private VertexShaderAttribute positions;
    private VertexShaderAttribute barycentric;
    private int elementCount;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private ShadowUiService shadowUiService;

    abstract protected VertexContainer getVertexContainer(TerrainObjectService terrainObjectService);

    @PostConstruct
    public void init() {
        Object extension = getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }
        createProgram(Shaders.INSTANCE.depthBufferVertexShader(), Shaders.INSTANCE.depthBufferFragmentShader());
        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        barycentric = createVertexShaderAttribute(A_BARYCENTRIC);
    }

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers() {
        VertexContainer vertexContainer = getVertexContainer(terrainObjectService);
        if (vertexContainer == null) {
            elementCount = 0;
            return;
        }
        positions.fillBuffer(vertexContainer.getVertices());
        barycentric.fillBuffer(vertexContainer.generateBarycentric());

        elementCount = vertexContainer.getVerticesCount();
    }

    @Override
    public void draw() {
        if (elementCount == 0) {
            return;
        }

        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();
        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, shadowUiService.createDepthProjectionTransformation());
        uniformMatrix4fv(U_VIEW_MATRIX, shadowUiService.createDepthViewTransformation());

        positions.activate();
        barycentric.activate();

        for (ModelMatrices modelMatrix : terrainObjectService.getObjectIdMatrices(getId())) {
            uniformMatrix4fv(U_MODEL_MATRIX, modelMatrix.getModel());
            getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
            WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
        }
    }
}
