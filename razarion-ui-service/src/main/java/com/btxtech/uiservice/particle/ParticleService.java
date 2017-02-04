package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.Camera;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 02.02.2017.
 */
@ApplicationScoped
public class ParticleService {
    private static final int GENERATION_DELAY = 100;
    private static final int GENERATION_COUNT = 15;
    private static final int GENERATION_RANDOM_DISTANCE = 5;
    private static final double EDGE_LENGTH = 3;
    private static final double HALF_EDGE = EDGE_LENGTH / 2.0;
    private static final double HALF_HEIGHT = EDGE_LENGTH * Math.sqrt(3.0) / 4.0;
    @Inject
    private Camera camera;
    private List<Particle> particles = new ArrayList<>();
    private long lastGenerationTime;

    public List<ModelMatrices> provideModelMatrices(long timestamp) {
        List<ModelMatrices> modelMatricesList = new ArrayList<>();
        if (lastGenerationTime + GENERATION_DELAY < timestamp) {
            for (int i = 0; i < GENERATION_COUNT; i++) {
                double xRand = Math.random() * GENERATION_RANDOM_DISTANCE;
                double yRand = Math.random() * GENERATION_RANDOM_DISTANCE;
                particles.add(new Particle(new Vertex(200 + xRand, 200 + yRand, 2), timestamp));
            }
            lastGenerationTime = timestamp;
        }
        for (Iterator<Particle> iterator = particles.iterator(); iterator.hasNext(); ) {
            Particle particle = iterator.next();
            ModelMatrices modelMatrices = particle.update(timestamp);
            if (modelMatrices != null) {
                modelMatricesList.add(modelMatrices);
            } else {
                iterator.remove();
            }
        }
        return modelMatricesList;
    }

    public List<Vertex> calculateVertices() {
        Matrix4 billboardMatrix = Matrix4.createXRotation(-camera.getRotateX());
        List<Vertex> vertices = new ArrayList<>();
        vertices.add(billboardMatrix.multiply(new Vertex(-HALF_EDGE, 0, -HALF_HEIGHT), 1.0));
        vertices.add(billboardMatrix.multiply(new Vertex(HALF_EDGE, 0, -HALF_HEIGHT), 1.0));
        vertices.add(billboardMatrix.multiply(new Vertex(0, 0, HALF_HEIGHT), 1.0));
        return vertices;
    }

    public List<Vertex> calculateFadeouts() {
        List<Vertex> fadeouts = new ArrayList<>();
        fadeouts.add(new Vertex(1, 0, 0));
        fadeouts.add(new Vertex(0, 1, 0));
        fadeouts.add(new Vertex(0, 0, 1));
        return fadeouts;
    }

}
