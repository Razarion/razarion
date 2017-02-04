package com.btxtech.uiservice.renderer.task.particle;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.particle.ParticleService;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * 01.02.2017.
 */
public abstract class AbstractParticleRenderUnit extends AbstractRenderUnit<Void> {
    @Inject
    private ParticleService particleService;

    protected abstract void fillBuffers(List<Vertex> vertices, List<Vertex> vertexFadeouts);

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(Void ignore) {
        List<Vertex> vertices = particleService.calculateVertices();
        fillBuffers(vertices, particleService.calculateFadeouts());
        setElementCount(vertices);
    }
}
