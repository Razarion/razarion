package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.engine.WebGlUniformTexture;
import com.btxtech.client.renderer.engine.shaderattribute.DecimalPositionShaderAttribute;
import com.btxtech.client.renderer.engine.shaderattribute.VertexShaderAttribute;
import com.btxtech.client.renderer.shaders.Shaders;
import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlFacadeConfig;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.particle.ParticleShapeConfig;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.task.particle.AbstractParticleRenderUnit;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLUniformLocation;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 03.02.2017.
 */
@DepthBufferRenderer
@Dependent
public class ClientParticleDepthBufferRenderUnit extends AbstractParticleRenderUnit {
    // private Logger logger = Logger.getLogger(ClientParticleDepthBufferRenderUnit.class.getName());
    @Inject
    private WebGlFacade webGlFacade;
    private VertexShaderAttribute positions;
    private DecimalPositionShaderAttribute alphaTextureCoordinates;
    private WebGlUniformTexture alphaOffset;
    private WebGlUniformTexture colorRamp;
    private ParticleShapeConfig particleShapeConfig;
    private WebGLUniformLocation modelMatrix;
    private WebGLUniformLocation uProgress;
    private WebGLUniformLocation uXColorRampOffset;

    @Override
    public void init() {
        webGlFacade.init(new WebGlFacadeConfig(Shaders.INSTANCE.particleVertexShader(), Shaders.INSTANCE.particleDeptBufferFragmentShader()).enableShadowTransformation());
        positions = webGlFacade.createVertexShaderAttribute(WebGlFacade.A_VERTEX_POSITION);
        alphaTextureCoordinates = webGlFacade.createDecimalPositionShaderAttribute("aAlphaTextureCoordinate");
        modelMatrix = webGlFacade.getUniformLocation(WebGlFacade.U_MODEL_MATRIX);
        uProgress = webGlFacade.getUniformLocation("uProgress");
        uXColorRampOffset = webGlFacade.getUniformLocation("uXColorRampOffset");
    }

    @Override
    protected void fillBuffers(List<Vertex> vertices, List<DecimalPosition> alphaTextureCoordinates, ParticleShapeConfig particleShapeConfig) {
        this.particleShapeConfig = particleShapeConfig;
        positions.fillBuffer(vertices);
        this.alphaTextureCoordinates.fillBuffer(alphaTextureCoordinates);
        alphaOffset = webGlFacade.createWebGLTexture(particleShapeConfig.getAlphaOffsetImageId(), "uAlphaOffsetSampler");
        colorRamp = webGlFacade.createWebGLTexture(particleShapeConfig.getColorRampImageId(), "uColorRampSampler");
    }

    @Override
    protected void prepareDraw() {
        webGlFacade.useProgram();

        positions.activate();
        alphaTextureCoordinates.activate();

        alphaOffset.activate();
        colorRamp.activate();
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        webGlFacade.uniformMatrix4fv(modelMatrix, modelMatrices.getModel());

        webGlFacade.uniform1f(uProgress, modelMatrices.getProgress());
        webGlFacade.uniform1f(uXColorRampOffset, particleShapeConfig.getColorRampXOffset(modelMatrices.getParticleXColorRampOffsetIndex()));

        webGlFacade.drawArrays(WebGLRenderingContext.TRIANGLES);
    }
}
