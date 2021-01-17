package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.engine.UniformLocation;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.particle.ParticleShapeConfig;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.task.ParticleRenderTaskRunner;
import elemental2.webgl.WebGLRenderingContext;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collections;

import static com.btxtech.client.renderer.webgl.WebGlFacadeConfig.Blend.SOURCE_ALPHA;

/**
 * Created by Beat
 * 03.02.2017.
 */
@Dependent
public class ParticleRenderTask extends AbstractWebGlRenderTask<ParticleShapeConfig> implements ParticleRenderTaskRunner.RenderTask {
    @Inject
    private GameCanvas gameCanvas;

    @Override
    protected WebGlFacadeConfig getWebGlFacadeConfig(ParticleShapeConfig particleShapeConfig) {
        return new WebGlFacadeConfig(Shaders.SHADERS.particleCustom())
                .enableCastShadow()
                .blend(SOURCE_ALPHA)
                .writeDepthBuffer(false)
                .drawMode(WebGLRenderingContext.POINTS);
    }

    @Override
    protected void setup(ParticleShapeConfig particleShapeConfig) {
        setupVec3VertexPositionArray(Collections.singletonList(Vertex.ZERO));

        setupModelMatrixUniform("uProgress", UniformLocation.Type.F, ModelMatrices::getProgress);
        setupUniform("uEdgeLength", UniformLocation.Type.F, particleShapeConfig::getEdgeLength);
        setupUniform("uShadowAlphaCutOff", UniformLocation.Type.F, () -> getRenderPass() == RenderService.Pass.SHADOW ? particleShapeConfig.getShadowAlphaCutOff() : 0.0);
        setupUniform("uTextureOffsetScope", UniformLocation.Type.F, particleShapeConfig::getTextureOffsetScope);
        setupUniform("uWidthInPixels", UniformLocation.Type.F, gameCanvas::getWidth);
        setupModelMatrixUniform("uXColorRampOffset", UniformLocation.Type.F, (modelMatrices) -> particleShapeConfig.lookupColorRampXOffset4Index(modelMatrices.getParticleXColorRampOffsetIndex()));

        createWebGLTexture("uAlphaOffsetSampler", particleShapeConfig.getAlphaOffsetImageId());
        createWebGLTexture("uColorRampSampler", particleShapeConfig.getColorRampImageId());
    }
}
