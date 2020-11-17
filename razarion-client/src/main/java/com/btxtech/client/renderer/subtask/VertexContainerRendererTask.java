package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.engine.UniformLocation;
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
import java.util.List;

import static com.btxtech.client.renderer.shaders.Shaders.SHADERS;
import static com.btxtech.client.renderer.shaders.SkeletonDefines.ALPHA_TO_COVERAGE;
import static com.btxtech.client.renderer.shaders.SkeletonDefines.UV;

/**
 * Created by Beat
 * 03.08.2016.
 */
@Dependent
public class VertexContainerRendererTask extends AbstractWebGlRenderTask<VertexContainer> implements AbstractShape3DRenderTaskRunner.RenderTask {
    // private Logger logger = Logger.getLogger(ClientVertexContainerRendererUnit.class.getName());
    @Inject
    private ClientShape3DUiService shape3DUiService;
    private boolean alphaToCoverage;
    // private WebGLUniformLocation characterRepresenting;
    // private WebGLUniformLocation characterRepresentingColor;

    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(VertexContainer vertexContainer) {
        return new WebGlFacadeConfig(SHADERS.vertexContainerCustomShader())
                .enableTransformation(true)
                .enableReceiveShadow()
                .enableCastShadow()
                .enableOESStandardDerivatives()
                .enableLight();
    }

    @Override
    public void setup(VertexContainer vertexContainer) {
        setupVec3PositionArray(Js.uncheckedCast(shape3DUiService.getVertexFloat32Array(vertexContainer)));
        setupVec3Array(WebGlFacade.A_VERTEX_NORMAL, Js.uncheckedCast(shape3DUiService.getNormFloat32Array(vertexContainer)));
        setupVec2Array(WebGlFacade.A_VERTEX_UV, Js.uncheckedCast(shape3DUiService.getTextureCoordinateFloat32Array(vertexContainer)));

        AlarmRaiser.onNull(vertexContainer.getShape3DMaterial().getPhongMaterialConfig(), Alarm.Type.INVALID_VERTEX_CONTAINER, "No Material in VertexContainer: " + vertexContainer.getShape3DMaterial().getMaterialName(), null);
        setupPhongMaterial(vertexContainer.getShape3DMaterial().getPhongMaterialConfig(), "material");

        if (vertexContainer.getShape3DMaterial().getAlphaToCoverage() != null) {
            alphaToCoverage = true;
            setupUniform("alphaToCoverage", UniformLocation.Type.F, () -> vertexContainer.getShape3DMaterial().getAlphaToCoverage());
        }

        // characterRepresenting = webGlFacade.getUniformLocation("characterRepresenting");
        // characterRepresentingColor = webGlFacade.getUniformLocation("characterRepresentingColor");
    }

    private void draw(ModelMatrices modelMatrices) {
//   TODO     if (modelMatrices.getColor() != null && getRenderData().isCharacterRepresenting()) {
//  TODO          webGlFacade.uniform1b(characterRepresenting, true);
//  TODO          webGlFacade.uniform3fNoAlpha(characterRepresentingColor, modelMatrices.getColor());
//  TODO      } else {
//  TODO          webGlFacade.uniform1b(characterRepresenting, false);
//  TODO      }

    }

    @Override
    protected void glslVertexCustomDefines(List<String> defines, VertexContainer vertexContainer) {
        defines.add(UV);
    }

    @Override
    protected void glslFragmentCustomDefines(List<String> defines, VertexContainer vertexContainer) {
        defines.add(UV);
        if (vertexContainer.getShape3DMaterial().getAlphaToCoverage() != null) {
            defines.add(ALPHA_TO_COVERAGE);
        }
    }

    @Override
    protected boolean isAlphaToCoverage() {
        return alphaToCoverage;
    }
}
