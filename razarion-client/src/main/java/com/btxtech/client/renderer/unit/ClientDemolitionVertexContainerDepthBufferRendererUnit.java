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
import com.btxtech.uiservice.renderer.AbstractDemolitionVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.ShadowUiService;
import elemental.html.WebGLRenderingContext;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 03.08.2016.
 */
@DepthBufferRenderer
@Dependent
public class ClientDemolitionVertexContainerDepthBufferRendererUnit extends AbstractDemolitionVertexContainerRenderUnit {
    // private Logger logger = Logger.getLogger(ClientVertexContainerRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ShadowUiService shadowUiService;
    private VertexShaderAttribute positions;
    private ShaderTextureCoordinateAttribute textureCoordinate;
    private WebGlUniformTexture webGLTexture;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.demolitionVertexContainerDeptBufferVertexShader(), Shaders.INSTANCE.demolitionVertexContainerDeptBufferFragmentShader());
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        textureCoordinate = webGlFacade.createShaderTextureCoordinateAttribute(WebGlFacade.A_TEXTURE_COORDINATE);
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void internalFillBuffers(VertexContainer vertexContainer, Integer baseItemDemolitionCuttingImageId, Integer baseItemDemolitionLookUpImageId) {
        positions.fillBuffer(vertexContainer.getVertices());
        textureCoordinate.fillBuffer(vertexContainer.getTextureCoordinates());
        webGLTexture = webGlFacade.createWebGLTexture(vertexContainer.getTextureId(), WebGlFacade.U_TEXTURE);
    }

    @Override
    protected void prepareDraw(Matrix4 heightMatrix, double maxHeight) {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, shadowUiService.getDepthProjectionTransformation());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, shadowUiService.getDepthViewTransformation());
        webGlFacade.uniformMatrix4fv("heightMatrix", heightMatrix);
        webGlFacade.uniform1f("uMaxHeight", maxHeight);

        positions.activate();
        textureCoordinate.activate();
        webGLTexture.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices, double health) {
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, modelMatrices.getModel());
        webGlFacade.uniform1f("uHealth", health);

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

}
