package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.engine.UniformLocation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.particle.ParticleShapeConfig;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.task.ParticleRenderTaskRunner;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import static com.btxtech.client.renderer.webgl.WebGlFacadeConfig.Blend.SOURCE_ALPHA;

/**
 * Created by Beat
 * 03.02.2017.
 */
@Dependent
public class ParticleRenderTask extends AbstractWebGlRenderTask<ParticleShapeConfig> implements ParticleRenderTaskRunner.RenderTask {
    @Inject
    private Camera camera;

    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(ParticleShapeConfig particleShapeConfig) {
        return new WebGlFacadeConfig(Shaders.SHADERS.particleCustom())
                .enableCastShadow()
                .blend(SOURCE_ALPHA)
                .writeDepthBuffer(false);
    }

    @Override
    protected void setup(ParticleShapeConfig particleShapeConfig) {
        setupVec3VertexPositionArray(particleShapeConfig.calculateVertices(-camera.getRotateX()));
        setupDecimalPositionArray("aAlphaTextureCoordinate", particleShapeConfig.calculateAlphaTextureCoordinates());

        setupModelMatrixUniform("uProgress", UniformLocation.Type.F, ModelMatrices::getProgress);
        setupUniform("uTextureOffsetScope", UniformLocation.Type.F, particleShapeConfig::getTextureOffsetScope);
        setupModelMatrixUniform("uXColorRampOffset", UniformLocation.Type.F, (modelMatrices) -> particleShapeConfig.getColorRampXOffset(modelMatrices.getParticleXColorRampOffsetIndex()));

        createWebGLTexture("uAlphaOffsetSampler", particleShapeConfig.getAlphaOffsetImageId());
        createWebGLTexture("uColorRampSampler", particleShapeConfig.getColorRampImageId());
    }
}
