package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.engine.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.shape3d.ClientShape3DUiService;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.AbstractBuildupVertexContainerRenderUnit;
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
public class ClientBuildupVertexContainerRendererUnit extends AbstractBuildupVertexContainerRenderUnit {
    // private Logger logger = Logger.getLogger(ClientVertexContainerRendererUnit.class.getName());
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
    @Inject
    private ClientShape3DUiService shape3DUiService;
    private Vec3Float32ArrayShaderAttribute positions;
    private Vec3Float32ArrayShaderAttribute norms;
    private Vec2Float32ArrayShaderAttribute textureCoordinateAttribute;
    private WebGlUniformTexture texture;
    private Color ambient;
    private Color diffuse;

    @PostConstruct
    public void init() {
        webGlFacade.setAbstractRenderUnit(this);
        webGlFacade.createProgram(Shaders.INSTANCE.buildupVertexContainerVertexShader(), Shaders.INSTANCE.buildupVertexContainerFragmentShader());
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        norms = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        textureCoordinateAttribute = webGlFacade.createVec2Float32ArrayShaderAttribute(WebGlFacade.A_TEXTURE_COORDINATE);
        webGlFacade.enableReceiveShadow();
    }

    @Override
    public void setupImages() {
    }

    @Override
    protected void internalFillBuffers(VertexContainer vertexContainer) {
        texture = webGlFacade.createWebGLTexture(vertexContainer.getTextureId(), "uSampler");
        positions.fillFloat32Array(shape3DUiService.getVertexFloat32Array(vertexContainer));
        norms.fillFloat32Array(shape3DUiService.getNormFloat32Array(vertexContainer));
        textureCoordinateAttribute.fillFloat32Array(shape3DUiService.getTextureCoordinateFloat32Array(vertexContainer));

        ambient = vertexContainer.getAmbient();
        diffuse = vertexContainer.getDiffuse();
    }

    @Override
    protected void prepareDraw(Matrix4 buildupMatrix) {
        webGlFacade.useProgram();

        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_MATRIX, camera.getMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_VIEW_NORM_MATRIX, camera.getNormMatrix());
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_PERSPECTIVE_MATRIX, projectionTransformation.getMatrix());

        webGlFacade.uniform3fNoAlpha("uLightingAmbient", ambient);
        webGlFacade.uniform3f("uLightingDirection", visualUiService.getShape3DLightDirection());
        webGlFacade.uniform3fNoAlpha("uLightingDiffuse", diffuse);
        webGlFacade.uniformMatrix4fv("buildupMatrix", buildupMatrix);
        // webGlFacade.uniform1f("uSpecularHardness", baseItemUiService.getSpecularHardness());
        // webGlFacade.uniform1f("uSpecularIntensity", baseItemUiService.getSpecularIntensity());

        webGlFacade.activateReceiveShadow();

        texture.activate();
        positions.activate();
        norms.activate();
        textureCoordinateAttribute.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices, double progressZ) {
        webGlFacade.uniformMatrix4fv(WebGlFacade.U_MODEL_MATRIX, modelMatrices.getModel());
        webGlFacade.uniformMatrix4fv("uNMMatrix", modelMatrices.getNorm());
        webGlFacade.uniform1f("progressZ", progressZ);

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

}
