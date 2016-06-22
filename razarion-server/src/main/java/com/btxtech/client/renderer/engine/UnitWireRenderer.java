package com.btxtech.client.renderer.engine;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.model.ShadowUiService;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
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
    @Inject
    private ItemService itemService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private ShadowUiService shadowUiService;

    @PostConstruct
    public void init() {
        Object extension = getCtx3d().getExtension("OES_standard_derivatives");
        if (extension == null) {
            throw new WebGlException("OES_standard_derivatives is no supported");
        }
        createProgram(Shaders.INSTANCE.modelViewPerspectiveWireVertexShader(), Shaders.INSTANCE.modelViewPerspectiveWireFragmentShader());
        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        barycentrics = createVertexShaderAttribute(A_BARYCENTRIC);
        textureCoordinate = createShaderTextureCoordinateAttributee(A_TEXTURE_COORDINATE);
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

        setElementCount(vertexContainer);
    }

    @Override
    public void draw() {
        Collection<ModelMatrices> modelMatrices = itemService.getModelMatrices(getId());
        if (modelMatrices == null || modelMatrices.isEmpty()) {
            return;
        }

        useProgram();

        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());
        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());

        positions.activate();
        barycentrics.activate();
        textureCoordinate.activate();
        webGLTexture.activate();

        for (ModelMatrices model : modelMatrices) {
            uniformMatrix4fv(U_MODEL_MATRIX, model.getModel());

            drawArrays(WebGLRenderingContext.TRIANGLES);
        }
    }
}
