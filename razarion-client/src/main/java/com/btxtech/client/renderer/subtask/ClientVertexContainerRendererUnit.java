package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.engine.LightUniforms;
import com.btxtech.client.renderer.engine.WebGlPhongMaterial;
import com.btxtech.client.renderer.engine.shaderattribute.Vec2Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.Vec3Float32ArrayShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.client.shape3d.ClientShape3DUiService;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaiser;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import elemental2.core.Float32Array;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;
import jsinterop.base.Js;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static elemental2.webgl.WebGLRenderingContext.SAMPLE_ALPHA_TO_COVERAGE;

/**
 * Created by Beat
 * 03.08.2016.
 */
@ColorBufferRenderer
@Dependent
public class ClientVertexContainerRendererUnit extends AbstractVertexContainerRenderUnit {
    // private Logger logger = Logger.getLogger(ClientVertexContainerRendererUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    @Inject
    private ClientShape3DUiService shape3DUiService;
    @Inject
    private VisualUiService visualUiService;
    private Vec3Float32ArrayShaderAttribute positions;
    private Vec3Float32ArrayShaderAttribute normals;
    private Vec2Float32ArrayShaderAttribute uvs;
    private WebGlPhongMaterial material;
    // private WebGLUniformLocation characterRepresenting;
    // private WebGLUniformLocation characterRepresentingColor;
    private WebGLUniformLocation alphaToCoverage;
    private WebGLUniformLocation uModelMatrix;
    private WebGLUniformLocation uModelNormMatrix;
    private LightUniforms lightUniforms;

    @Override
    public void init() {
        webGlFacade.enableOESStandardDerivatives();
        webGlFacade.init(new WebGlFacadeConfig(Shaders.INSTANCE.vertexContainerVertexShader(), Shaders.INSTANCE.vertexContainerFragmentShader()).enableTransformation(true)/*.enableReceiveShadow()*/.enableCastShadow());
        positions = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        normals = webGlFacade.createVec3Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_NORMAL);
        uvs = webGlFacade.createVec2Float32ArrayShaderAttribute(WebGlFacade.A_VERTEX_UV);
        lightUniforms = new LightUniforms(webGlFacade);

        uModelMatrix = webGlFacade.getUniformLocation(WebGlFacade.U_MODEL_MATRIX);
        uModelNormMatrix = webGlFacade.getUniformLocation("normalMatrix");


        // characterRepresenting = webGlFacade.getUniformLocation("characterRepresenting");
        // characterRepresentingColor = webGlFacade.getUniformLocation("characterRepresentingColor");
    }

    @Override
    protected void internalFillBuffers(VertexContainer vertexContainer) {
        AlarmRaiser.onNull(vertexContainer.getShape3DMaterialConfig().getPhongMaterialConfig(), Alarm.Type.INVALID_VERTEX_CONTAINER, "No Material in VertexContainer: " + vertexContainer.getShape3DMaterialConfig().getMaterialName(), null);
        material = webGlFacade.createPhongMaterial(vertexContainer.getShape3DMaterialConfig().getPhongMaterialConfig(), "material");
        if (getRenderData().getShape3DMaterialConfig().getAlphaToCoverage() != null) {
            alphaToCoverage = webGlFacade.getUniformLocation("alphaToCoverage");
        }

        Float32Array vertexPositions = Js.uncheckedCast(shape3DUiService.getVertexFloat32Array(vertexContainer));
        positions.fillFloat32Array(vertexPositions);
        normals.fillFloat32Array(Js.uncheckedCast(shape3DUiService.getNormFloat32Array(vertexContainer)));
        uvs.fillFloat32Array(Js.uncheckedCast(shape3DUiService.getTextureCoordinateFloat32Array(vertexContainer)));
        setElementCount((int) (vertexPositions.length / Vertex.getComponentsPerVertex()));
    }

    @Override
    protected void prepareDraw() {
//        if(webGlFacade.canBeSkipped()) {
//            return;
//        }
        webGlFacade.useProgram();
//        webGlFacade.setTransformationUniforms();
        if (getRenderData().getShape3DMaterialConfig().getAlphaToCoverage() != null) {
            webGlFacade.getCtx3d().enable(SAMPLE_ALPHA_TO_COVERAGE);
            webGlFacade.uniform1f(alphaToCoverage, getRenderData().getShape3DMaterialConfig().getAlphaToCoverage());
        }

        lightUniforms.setLightUniforms(webGlFacade);

        material.activate();

//        webGlFacade.activateReceiveShadow();

        positions.activate();
        normals.activate();
        uvs.activate();
    }

    @Override
    protected void afterDraw() {
        if (getRenderData().getShape3DMaterialConfig().getAlphaToCoverage() != null) {
            webGlFacade.getCtx3d().disable(SAMPLE_ALPHA_TO_COVERAGE);
        }
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(uModelMatrix, modelMatrices.getModel());
        webGlFacade.uniformMatrix4fv(uModelNormMatrix, modelMatrices.getNorm());
//        if (modelMatrices.getColor() != null && getRenderData().isCharacterRepresenting()) {
//            webGlFacade.uniform1b(characterRepresenting, true);
//            webGlFacade.uniform3fNoAlpha(characterRepresentingColor, modelMatrices.getColor());
//        } else {
//            webGlFacade.uniform1b(characterRepresenting, false);
//        }

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }

    @Override
    public List<String> getGlslFragmentDefines() {
        if (getRenderData().getShape3DMaterialConfig().getAlphaToCoverage() != null) {
            return Collections.singletonList("ALPHA_TO_COVERAGE");
        }
        return null;
    }
}
