package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.ImageDescriptor;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ShadowUiService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 04.09.2015.
 */
@Dependent
@Deprecated
public class ItemWireUnitRenderer extends AbstractWebGlUnitRenderer {
    // private Logger logger = Logger.getLogger(ItemWireUnitRenderer.class.getName());
    private VertexShaderAttribute positions;
    private VertexShaderAttribute barycentrics;
    private ShaderTextureCoordinateAttribute textureCoordinate;
    private WebGlUniformTexture_OLD webGLTexture;
    @Inject
    private BaseItemUiService baseItemUiService;
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

    public void fillBuffers() {
        VertexContainer vertexContainer = baseItemUiService.getItemTypeVertexContainer(getId());
        if (vertexContainer == null) {
            return;
        }

        positions.fillBuffer(vertexContainer.getVertices());
        barycentrics.fillBuffer(vertexContainer.generateBarycentric());
        textureCoordinate.fillBuffer(vertexContainer.getTextureCoordinates());

        setElementCount(vertexContainer);
    }

    @Override
    protected void prepareDraw() {
        useProgram();

        uniformMatrix4fv(U_VIEW_MATRIX, camera.getMatrix());
        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());

        positions.activate();
        barycentrics.activate();
        textureCoordinate.activate();
        webGLTexture.activate();
    }

//  TODO   @Override
//  TODO   protected void modelDraw(ModelMatrices modelMatrices) {
//  TODO       uniformMatrix4fv(U_MODEL_MATRIX, modelMatrices.getModel());
//TODO
//   TODO      drawArrays(WebGLRenderingContext.TRIANGLES);
//   TODO  }
}
