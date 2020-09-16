package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.client.shape3d.ClientShape3DUiService;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaiser;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.task.AbstractShape3DRenderTaskRunner;
import jsinterop.base.Js;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 03.08.2016.
 */
@Dependent
public class VertexContainerRendererTask extends AbstractWebGlRenderTask<VertexContainer> implements AbstractShape3DRenderTaskRunner.WebGlVertexContainerRenderTask {
    // private Logger logger = Logger.getLogger(ClientVertexContainerRendererUnit.class.getName());
    @Inject
    private ClientShape3DUiService shape3DUiService;
    // private WebGLUniformLocation characterRepresenting;
    // private WebGLUniformLocation characterRepresentingColor;
    // TODO private WebGLUniformLocation alphaToCoverage;

    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(VertexContainer vertexContainer) {
        return new WebGlFacadeConfig(Shaders.INSTANCE.vertexContainerVertexShader(), Shaders.INSTANCE.vertexContainerFragmentShader())
                .enableTransformation(true)
                .enableOESStandardDerivatives()
                .enableLight()
                .glslFragmentDefines(glslFragmentDefines(vertexContainer));
    }

    @Override
    public void setup(VertexContainer vertexContainer) {
        setupVec3PositionArray(Js.uncheckedCast(shape3DUiService.getVertexFloat32Array(vertexContainer)));
        setupVec3Array(WebGlFacade.A_VERTEX_NORMAL, Js.uncheckedCast(shape3DUiService.getNormFloat32Array(vertexContainer)));
        setupVec2Array(WebGlFacade.A_VERTEX_UV, Js.uncheckedCast(shape3DUiService.getTextureCoordinateFloat32Array(vertexContainer)));

        AlarmRaiser.onNull(vertexContainer.getShape3DMaterialConfig().getPhongMaterialConfig(), Alarm.Type.INVALID_VERTEX_CONTAINER, "No Material in VertexContainer: " + vertexContainer.getShape3DMaterialConfig().getMaterialName(), null);
        setupPhongMaterial(vertexContainer.getShape3DMaterialConfig().getPhongMaterialConfig(), "material");

        // characterRepresenting = webGlFacade.getUniformLocation("characterRepresenting");
        // characterRepresentingColor = webGlFacade.getUniformLocation("characterRepresentingColor");
    }

    private void internalFillBuffers(VertexContainer vertexContainer) {
//   TODO     if (getRenderData().getShape3DMaterialConfig().getAlphaToCoverage() != null) {
//   TODO        alphaToCoverage = webGlFacade.getUniformLocation("alphaToCoverage");
//   TODO     }
    }

    private void prepareDraw() {
//  TODO      if (getRenderData().getShape3DMaterialConfig().getAlphaToCoverage() != null) {
//  TODO          webGlFacade.getCtx3d().enable(SAMPLE_ALPHA_TO_COVERAGE);
//  TODO          webGlFacade.uniform1f(alphaToCoverage, getRenderData().getShape3DMaterialConfig().getAlphaToCoverage());
//  TODO      }

    }

    private void afterDraw() {
        //  TODO    if (getRenderData().getShape3DMaterialConfig().getAlphaToCoverage() != null) {
        //  TODO   webGlFacade.getCtx3d().disable(SAMPLE_ALPHA_TO_COVERAGE);
        //  TODO    }
    }

    private void draw(ModelMatrices modelMatrices) {
//   TODO     if (modelMatrices.getColor() != null && getRenderData().isCharacterRepresenting()) {
//  TODO          webGlFacade.uniform1b(characterRepresenting, true);
//  TODO          webGlFacade.uniform3fNoAlpha(characterRepresentingColor, modelMatrices.getColor());
//  TODO      } else {
//  TODO          webGlFacade.uniform1b(characterRepresenting, false);
//  TODO      }

    }

    private List<String> glslFragmentDefines(VertexContainer vertexContainer) {
        if (vertexContainer.getShape3DMaterialConfig().getAlphaToCoverage() != null) {
            return Collections.singletonList("ALPHA_TO_COVERAGE");
        }
        return null;
    }
}
