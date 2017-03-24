package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.shaderattribute.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.shape3d.ClientShape3DUiService;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.AbstractBuildupVertexContainerRenderUnit;
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
public class ClientBuildupVertexContainerDepthBufferRendererUnit extends AbstractBuildupVertexContainerRenderUnit {
    // private Logger logger = Logger.getLogger(ClientVertexContainerRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private VisualUiService visualUiService;
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ShadowUiService shadowUiService;
    @Inject
    private ClientShape3DUiService shape3DUiService;
    private Vec3Float32ArrayShaderAttribute positions;
    private Vec2Float32ArrayShaderAttribute textureCoordinate;
    private WebGlUniformTexture finishTexture;
    private WebGlUniformTexture buildupTexture;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.buildupVertexContainerDeptBufferVertexShader(), Shaders.INSTANCE.buildupVertexContainerDeptBufferFragmentShader());
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        textureCoordinate = webGlFacade.createVec2Float32ArrayShaderAttribute(WebGlFacade.A_TEXTURE_COORDINATE);
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void internalFillBuffers(VertexContainer vertexContainer, Matrix4 buildupMatrix, int buildupTextureId) {
        positions.fillFloat32Array(shape3DUiService.getVertexFloat32Array(vertexContainer));
        textureCoordinate.fillFloat32Array(shape3DUiService.getTextureCoordinateFloat32Array(vertexContainer));
        finishTexture = webGlFacade.createWebGLTexture(vertexContainer.getTextureId(), "uFinishTextureSampler");
        buildupTexture = webGlFacade.createWebGLTexture(buildupTextureId, "uBuildupTextureSampler");
    }

    @Override
    protected void prepareDraw(Matrix4 buildupMatrix) {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, shadowUiService.getDepthProjectionTransformation());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, shadowUiService.getDepthViewTransformation());

        webGlFacade.uniformMatrix4fv("buildupMatrix", buildupMatrix);

        positions.activate();
        textureCoordinate.activate();
        finishTexture.activate();
        buildupTexture.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices, double progressZ) {
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, modelMatrices.getModel());
        webGlFacade.uniform1f("progressZ", progressZ);

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

}
