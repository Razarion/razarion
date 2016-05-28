package com.btxtech.client.renderer.engine;

import com.btxtech.client.ImageDescriptor;
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
 * 04.09.2015.
 */
@Dependent
public class UnitWireRenderer extends AbstractRenderer {
    // private Logger logger = Logger.getLogger(UnitWireRenderer.class.getName());
    private VertexShaderAttribute positions;
    private VertexShaderAttribute barycentrics;
    private ShaderTextureCoordinateAttribute textureCoordinate;
    private WebGlUniformTexture webGLTexture;
    private int elementCount;
    @Inject
    private ItemService itemService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private Lighting lighting;

    @PostConstruct
    public void init() {
        Object extension = getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }
        createProgram(Shaders.INSTANCE.modelViewPerspectiveWireVertexShader(), Shaders.INSTANCE.modelViewPerspectiveWireFragmentShader());
        positions = createVertexShaderAttribute("aVertexPosition");
        barycentrics = createVertexShaderAttribute("aBarycentric");
        textureCoordinate = createShaderTextureCoordinateAttributee("aTextureCoord");
    }

    @Override
    public void setupImages() {
        webGLTexture = createWebGLTexture(ImageDescriptor.CHESS_TEXTURE_08, "uSampler");
    }

    @Override
    public void fillBuffers() {
        VertexContainer vertexContainer = itemService.getItemTypeVertexContainer(getId());
        if (vertexContainer == null) {
            return;
        }

        positions.fillBuffer(vertexContainer.getVertices());
        barycentrics.fillBuffer(vertexContainer.generateBarycentric());
        textureCoordinate.fillBuffer(vertexContainer.getTextureCoordinates());

        elementCount = vertexContainer.getVerticesCount();
    }

    @Override
    public void draw() {
        Collection<ModelMatrices> modelMatrices = itemService.getModelMatrices(getId());
        if (modelMatrices == null || modelMatrices.isEmpty()) {
            return;
        }

        useProgram();

        uniformMatrix4fv("uVMatrix", camera.createMatrix());
        uniformMatrix4fv("uPMatrix", projectionTransformation.createMatrix());

        positions.activate();
        barycentrics.activate();
        textureCoordinate.activate();
        webGLTexture.activate();

        for (ModelMatrices model : modelMatrices) {
            uniformMatrix4fv("uMMatrix", model.getModel());

            getCtx3d().drawArrays(WebGLRenderingContext.TRIANGLES, 0, elementCount);
            WebGlUtil.checkLastWebGlError("drawArrays", getCtx3d());
        }
    }
}
