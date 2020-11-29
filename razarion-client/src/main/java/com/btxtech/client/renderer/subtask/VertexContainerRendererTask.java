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
import static com.btxtech.client.renderer.shaders.SkeletonDefines.BUILDUP_STATE;
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
    private AbstractShape3DRenderTaskRunner.BuildupState buildupState;
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
    public void setBuildupState(AbstractShape3DRenderTaskRunner.BuildupState buildupState) {
        this.buildupState = buildupState;
    }

    @Override
    public void setup(VertexContainer vertexContainer) {
        setupVec3PositionArray(Js.uncheckedCast(shape3DUiService.getVertexFloat32Array(vertexContainer)));
        setupVec3Array(WebGlFacade.A_VERTEX_NORMAL, Js.uncheckedCast(shape3DUiService.getNormFloat32Array(vertexContainer)));
        setupVec2Array(WebGlFacade.A_VERTEX_UV, Js.uncheckedCast(shape3DUiService.getTextureCoordinateFloat32Array(vertexContainer)));

        AlarmRaiser.onNull(vertexContainer.getVertexContainerMaterial().getPhongMaterialConfig(), Alarm.Type.INVALID_VERTEX_CONTAINER, "No Material in VertexContainer: " + vertexContainer.getVertexContainerMaterial().getMaterialName(), null);
        setupPhongMaterial(vertexContainer.getVertexContainerMaterial().getPhongMaterialConfig(), "material");

        if (vertexContainer.getVertexContainerMaterial().getAlphaToCoverage() != null) {
            alphaToCoverage = true;
            setupUniform("alphaToCoverage", UniformLocation.Type.F, () -> vertexContainer.getVertexContainerMaterial().getAlphaToCoverage());
        }

        if(buildupState != null) {
            setupProgressUniform("progressZ", UniformLocation.Type.F, progress -> buildupState.getMaxZ() * progress);
            setupUniform("buildupMatrix", UniformLocation.Type.MATRIX_4, () -> buildupState.getBuildupMatrix());
            createWebGLTexture("uBuildupTextureSampler", buildupState.getBuildupTextureId());
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
        if(buildupState != null) {
            defines.add(BUILDUP_STATE);
        }
    }

    @Override
    protected void glslFragmentCustomDefines(List<String> defines, VertexContainer vertexContainer) {
        defines.add(UV);
        if (vertexContainer.getVertexContainerMaterial().getAlphaToCoverage() != null) {
            defines.add(ALPHA_TO_COVERAGE);
        }
        if(buildupState != null) {
            defines.add(BUILDUP_STATE);
        }
    }

    @Override
    protected boolean isAlphaToCoverage() {
        return alphaToCoverage;
    }
}
