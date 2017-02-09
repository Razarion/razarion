package com.btxtech.uiservice.renderer.task.particle;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.particle.ParticleShapeConfig;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.Camera;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 01.02.2017.
 */
public abstract class AbstractParticleRenderUnit extends AbstractRenderUnit<ParticleShapeConfig> {
    private Logger logger = Logger.getLogger(AbstractParticleRenderUnit.class.getName());
    @Inject
    private Camera camera;

    protected abstract void fillBuffers(List<Vertex> vertices, List<DecimalPosition> alphaTextureCoordinates, ParticleShapeConfig particleShapeConfig);

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(ParticleShapeConfig particleShapeConfig) {
        if (particleShapeConfig.getAlphaOffsetImageId() == null) {
            logger.warning("ParticleShapeConfig has no alphaOffsetImageId: " + particleShapeConfig);
            return;
        }
        if (particleShapeConfig.getColorRampImageId() == null) {
            logger.warning("ParticleShapeConfig has no colorRampImageId: " + particleShapeConfig);
            return;
        }

        List<Vertex> vertices = particleShapeConfig.calculateVertices(-camera.getRotateX());
        fillBuffers(vertices, particleShapeConfig.calculateAlphaTextureCoordinates(), particleShapeConfig);
        setElementCount(vertices);
    }
}
