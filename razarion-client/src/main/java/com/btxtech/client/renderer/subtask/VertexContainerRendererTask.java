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
import com.btxtech.uiservice.renderer.task.progress.BuildupState;
import com.btxtech.uiservice.renderer.task.progress.DemolitionState;
import com.btxtech.uiservice.renderer.task.progress.ProgressState;
import jsinterop.base.Js;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

import static com.btxtech.client.renderer.shaders.Shaders.SHADERS;
import static com.btxtech.client.renderer.shaders.SkeletonDefines.ALPHA_TO_COVERAGE;
import static com.btxtech.client.renderer.shaders.SkeletonDefines.BUILDUP_STATE;
import static com.btxtech.client.renderer.shaders.SkeletonDefines.HEALTH_STATE;
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
    private ProgressState progressState;
    private VertexContainer vertexContainer;
    // private WebGLUniformLocation characterRepresenting;
    // private WebGLUniformLocation characterRepresentingColor;

    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(VertexContainer vertexContainer) {
        this.vertexContainer = vertexContainer;
        return new WebGlFacadeConfig(SHADERS.vertexContainerCustomShader())
                .enableNormTransformation()
                .enableReceiveShadow()
                .enableCastShadow()
                .enableOESStandardDerivatives()
                .enableLight();
    }

    @Override
    public void setProgressState(ProgressState progressState) {
        this.progressState = progressState;
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

        if(progressState != null) {
            if(progressState instanceof BuildupState) {
                setupProgressUniforms("progressZ", "uBuildupTextureSampler");
                setupUniform("buildupMatrix", UniformLocation.Type.MATRIX_4, () -> ((BuildupState)progressState).getBuildupMatrix());
            } else if(progressState instanceof DemolitionState) {
                setupProgressUniforms("uHealth", "uDemolitionSampler");
            } else {
                throw new IllegalArgumentException("Unknown progressState: " + progressState);
            }
        }

        // characterRepresenting = webGlFacade.getUniformLocation("characterRepresenting");
        // characterRepresentingColor = webGlFacade.getUniformLocation("characterRepresentingColor");
    }

    private void setupProgressUniforms(String progressUniformName, String textureSampleName) {
        setupModelMatrixUniform(progressUniformName, UniformLocation.Type.F, modelMatrices -> progressState.calculateProgress(modelMatrices.getProgress()));
        createWebGLTexture(textureSampleName, progressState.getBuildupTextureId());
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
        if(progressState != null) {
            defines.add(BUILDUP_STATE);
        }
    }

    @Override
    protected void glslFragmentCustomDefines(List<String> defines, VertexContainer vertexContainer) {
        defines.add(UV);
        if (vertexContainer.getVertexContainerMaterial().getAlphaToCoverage() != null) {
            defines.add(ALPHA_TO_COVERAGE);
        }
        if(progressState instanceof BuildupState) {
            defines.add(BUILDUP_STATE);
        } else if(progressState instanceof DemolitionState) {
            defines.add(HEALTH_STATE);
        }

    }

    @Override
    protected boolean isAlphaToCoverage() {
        return alphaToCoverage;
    }

    @Override
    protected String getHelperString() {
        return super.getHelperString() + " " + vertexContainer.getKey();
    }
}
