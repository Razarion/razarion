package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.units.UnitService;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Matrix4;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 28.12.2015.
 */
@Dependent
public class UnitDepthBufferRenderer extends AbstractRenderer {
    @Inject
    private UnitService unitService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private Lighting lighting;
    private VertexShaderAttribute positions;
    private VertexShaderAttribute barycentric;
    private int elementCount;
    // private Logger logger = Logger.getLogger(UnitDepthBufferRenderer.class.getName());

    @PostConstruct
    public void init() {
        Object extension = getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }
        createProgram(Shaders.INSTANCE.depthBufferVertexShader(), Shaders.INSTANCE.depthBufferFragmentShader());
        positions = createVertexShaderAttribute("aVertexPosition");
        barycentric = createVertexShaderAttribute("aBarycentric");
    }

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers() {
        VertexList vertexList = unitService.getVertexList();
        if (vertexList == null) {
            return;
        }
        positions.fillBuffer(vertexList.getVertices());
        barycentric.fillBuffer(vertexList.getBarycentric());
        elementCount = vertexList.getVerticesCount();
    }

    @Override
    public void draw() {
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();
        uniformMatrix4fv("uPMatrix", lighting.createProjectionTransformation());
        uniformMatrix4fv("uVMatrix", lighting.createViewTransformation());
        uniformMatrix4fv("uMMatrix", unitService.getModelMatrix());

        positions.activate();
        barycentric.activate();

        // Draw
        getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
        WebGlUtil.checkLastWebGlError("drawArrays",getCtx3d());
    }
}
