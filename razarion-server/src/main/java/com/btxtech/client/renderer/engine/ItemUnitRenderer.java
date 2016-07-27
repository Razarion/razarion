package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.dto.VertexContainer;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 28.12.2015.
 */
@Dependent
public class ItemUnitRenderer extends AbstractWebGlUnitRenderer {
    // private Logger logger = Logger.getLogger(ItemUnitRenderer.class.getName());
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private ShadowUiService shadowUiService;
    private VertexShaderAttribute positions;
    private VertexShaderAttribute norms;
    private ShaderTextureCoordinateAttribute textureCoordinateAttribute;
    private WebGlUniformTexture texture;

    @PostConstruct
    public void init() {
        createProgram(Shaders.INSTANCE.unitVertexShader(), Shaders.INSTANCE.unitFragmentShader());
        positions = createVertexShaderAttribute(A_VERTEX_POSITION);
        norms = createVertexShaderAttribute("aVertexNormal");
        textureCoordinateAttribute = createShaderTextureCoordinateAttributee(A_TEXTURE_COORDINATE);
    }

    @Override
    public void setupImages() {
        texture = createWebGLTexture(baseItemUiService.getImageDescriptor(), "uSampler");
        enableShadow();
    }

    @Override
    public void fillBuffers() {
        VertexContainer vertexContainer = baseItemUiService.getItemTypeVertexContainer(getId());
        if (vertexContainer == null) {
            return;
        }
        positions.fillBuffer(vertexContainer.getVertices());
        norms.fillBuffer(vertexContainer.getNorms());
        textureCoordinateAttribute.fillBuffer(vertexContainer.getTextureCoordinates());

        setElementCount(vertexContainer);
    }

    @Override
    protected void preModelDraw() {
        useProgram();
        getCtx3d().disable(WebGLRenderingContext.BLEND);
        getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);

        uniformMatrix4fv(U_VIEW_MATRIX, camera.createMatrix());
        uniformMatrix4fv(U_VIEW_NORM_MATRIX, camera.createNormMatrix());
        uniformMatrix4fv(U_PERSPECTIVE_MATRIX, projectionTransformation.createMatrix());
        uniform3f("uAmbientColor", shadowUiService.getAmbientIntensity(), shadowUiService.getAmbientIntensity(), shadowUiService.getAmbientIntensity());
        uniform3f("uLightingDirection", shadowUiService.getLightDirection());
        uniform3f("uLightingColor", shadowUiService.getDiffuseIntensity(), shadowUiService.getDiffuseIntensity(), shadowUiService.getDiffuseIntensity());
        uniform1f("uSpecularHardness", baseItemUiService.getSpecularHardness());
        uniform1f("uSpecularIntensity", baseItemUiService.getSpecularIntensity());

        activateShadow();

        positions.activate();
        norms.activate();
        textureCoordinateAttribute.activate();
        texture.activate();
    }

    @Override
    protected void modelDraw(ModelMatrices modelMatrices) {
        uniformMatrix4fv(U_MODEL_MATRIX, modelMatrices.getModel());
        uniformMatrix4fv("uNMMatrix", modelMatrices.getNorm());

        drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
