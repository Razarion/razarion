package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.client.units.ItemService;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.shared.gameengine.pathing.ModelMatrices;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by Beat
 * 28.12.2015.
 */
@Dependent
public class UnitDepthBufferRenderer extends AbstractRenderer {
    // private Logger logger = Logger.getLogger(UnitDepthBufferRenderer.class.getName());
    @Inject
    private ItemService itemService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private Lighting lighting;
    private VertexShaderAttribute positions;
    private VertexShaderAttribute barycentric;
    private int elementCount;

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
        VertexContainer vertexContainer = itemService.getItemTypeVertexContainer(getId());
        if (vertexContainer == null) {
            return;
        }

        positions.fillBuffer(vertexContainer.getVertices());
        barycentric.fillBuffer(vertexContainer.generateBarycentric());
        elementCount = vertexContainer.getVerticesCount();
    }

    @Override
    public void draw() {
        Collection<ModelMatrices> modelMatrices = itemService.getModelMatrices(getId());
        if (modelMatrices == null || modelMatrices.isEmpty()) {
            return;
        }
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        useProgram();
        uniformMatrix4fv("uPMatrix", lighting.createProjectionTransformation());
        uniformMatrix4fv("uVMatrix", lighting.createViewTransformation());

        positions.activate();
        barycentric.activate();

        for (ModelMatrices model : modelMatrices) {
            uniformMatrix4fv("uMMatrix", model.getModel());

            getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
            WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
        }
    }
}
