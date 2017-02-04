package com.btxtech.uiservice.renderer.task.particle;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.particle.Particle;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;

import java.util.List;

/**
 * Created by Beat
 * 01.02.2017.
 */
public abstract class AbstractParticleRenderUnit extends AbstractRenderUnit<Void> {
    protected abstract void fillBuffers(List<Vertex> vertices, List<Vertex> vertexFadeouts);

    @Override
    public void setupImages() {

    }

    @Override
    public void fillBuffers(Void ignore) {
        List<Vertex> vertices = Particle.calculateVertices();
        fillBuffers(vertices, Particle.calculateFadeouts());
        setElementCount(vertices);
    }
}
