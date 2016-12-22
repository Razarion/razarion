package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.ShaderTextureCoordinateAttribute;
import com.btxtech.client.renderer.engine.VertexShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.AbstractFireVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.AbstractLookUpVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 03.08.2016.
 */
@ColorBufferRenderer
@Dependent
public class ClientFireVertexContainerRendererUnit extends AbstractFireVertexContainerRenderUnit {
    // private Logger logger = Logger.getLogger(ClientFireVertexContainerRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private Camera camera;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private BaseItemUiService baseItemUiService;
    private VertexShaderAttribute positions;
    private ShaderTextureCoordinateAttribute textureCoordinateAttribute;
    private WebGlUniformTexture texture;
    private WebGlUniformTexture textureGradient;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.fireVertexContainerVertexShader(), Shaders.INSTANCE.fireVertexContainerFragmentShader());
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        textureCoordinateAttribute = webGlFacade.createShaderTextureCoordinateAttribute(WebGlFacade.A_TEXTURE_COORDINATE);
        webGlFacade.enableReceiveShadow();
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void internalFillBuffers(VertexContainer vertexContainer) {
        texture = webGlFacade.createWebGLTexture(vertexContainer.getTextureId(), WebGlFacade.U_TEXTURE);
        textureGradient = webGlFacade.createWebGLTexture(vertexContainer.getLookUpTextureId(), "uLookUpTexture");
        positions.fillBuffer(vertexContainer.getVertices());
        textureCoordinateAttribute.fillBuffer(vertexContainer.getTextureCoordinates());
    }

    @Override
    protected void prepareDraw(double yTextureOffset, Matrix4 heightMatrix, double minHeight, double maxHeight) {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());


        webGlFacade.uniform1f("yTextureOffset", yTextureOffset);
        webGlFacade.uniformMatrix4fv("heightMatrix", heightMatrix);
        webGlFacade.uniform1f("minHeight", minHeight);
        webGlFacade.uniform1f("maxHeight", maxHeight);

        texture.activate();
        textureGradient.activate();
        positions.activate();
        textureCoordinateAttribute.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, modelMatrices.getModel());

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

}
