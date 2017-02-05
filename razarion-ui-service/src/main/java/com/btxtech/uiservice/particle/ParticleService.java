package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.Camera;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 02.02.2017.
 */
@ApplicationScoped
public class ParticleService {
    private static final Vertex START_POSITION = new Vertex(200, 200, 2);
    private static final double EDGE_LENGTH = 3;
    private static final double HALF_EDGE = EDGE_LENGTH / 2.0;
    private static final double HALF_HEIGHT = EDGE_LENGTH * Math.sqrt(3.0) / 4.0;
    @Inject
    private Camera camera;
    @Inject
    private Instance<ParticleEmitter> particleEmitterInstance;
    private List<Particle> particles = new ArrayList<>();
    private List<ParticleEmitterConfig> emitterConfigs = new ArrayList<>();
    private Collection<ParticleEmitter> activeEmitters = new ArrayList<>();
    private long startTimeStamp;

    @PostConstruct
    public void postConstruct() {
        // emitter für puff, emitter für fire, bewegender emitter
        // kries partikel mit smoke texture
        emitterConfigs.add(new ParticleEmitterConfig().setStart(1000).setTtl(200000).setVelocity(new Vertex(0, 0, 0)).setEmittingCount(10).setEmittingDelay(100).setGenerationRandomDistance(3));
//        emitterConfigs.add(new ParticleEmitterConfig().setStart(1000).setTtl(20000).setVelocity(new Vertex(10, 10, 10)).setEmittingCount(10).setEmittingDelay(100).setGenerationRandomDistance(3));
//        emitterConfigs.add(new ParticleEmitterConfig().setStart(1000).setTtl(20000).setVelocity(new Vertex(10, -10, 10)).setEmittingCount(10).setEmittingDelay(100).setGenerationRandomDistance(3));
//        emitterConfigs.add(new ParticleEmitterConfig().setStart(1000).setTtl(20000).setVelocity(new Vertex(-10, 10, 10)).setEmittingCount(10).setEmittingDelay(100).setGenerationRandomDistance(3));
//        emitterConfigs.add(new ParticleEmitterConfig().setStart(1000).setTtl(20000).setVelocity(new Vertex(-10, -10, 10)).setEmittingCount(10).setEmittingDelay(100).setGenerationRandomDistance(3));
    }

    public List<ModelMatrices> provideModelMatrices(long timestamp) {
        if (startTimeStamp == 0) {
            startTimeStamp = timestamp;
            return null;
        }

        // Handle emitters
        while (!emitterConfigs.isEmpty() && emitterConfigs.get(0).getStart() + startTimeStamp < timestamp) {
            ParticleEmitter particleEmitter = particleEmitterInstance.get();
            particleEmitter.init(timestamp, START_POSITION, emitterConfigs.remove(0));
            activeEmitters.add(particleEmitter);
        }
        activeEmitters.removeIf(particle -> !particle.tick(timestamp));

        // Handle particles
        particles.removeIf(particle -> !particle.tick(timestamp, camera.getMatrix()));
        Collections.sort(particles);

        return particles.stream().map(Particle::getModelMatrices).collect(Collectors.toList());
    }

    public List<Vertex> calculateVertices() {
        Matrix4 billboardMatrix = Matrix4.createXRotation(-camera.getRotateX());
        List<Vertex> vertices = new ArrayList<>();
        // Triangle 1
        vertices.add(billboardMatrix.multiply(new Vertex(0, 0, 0), 1.0));
        vertices.add(billboardMatrix.multiply(new Vertex(EDGE_LENGTH, 0, 0), 1.0));
        vertices.add(billboardMatrix.multiply(new Vertex(0, 0, EDGE_LENGTH), 1.0));
        // Triangle 2
        vertices.add(billboardMatrix.multiply(new Vertex(EDGE_LENGTH, 0, 0), 1.0));
        vertices.add(billboardMatrix.multiply(new Vertex(EDGE_LENGTH, 0, EDGE_LENGTH), 1.0));
        vertices.add(billboardMatrix.multiply(new Vertex(0, 0, EDGE_LENGTH), 1.0));
        return vertices;
    }

    public List<DecimalPosition> calculateAlphaTextureCoordinates() {
        List<DecimalPosition> alphaTextureCoordinates = new ArrayList<>();
        // Triangle 1
        alphaTextureCoordinates.add(new DecimalPosition(0, 0));
        alphaTextureCoordinates.add(new DecimalPosition(1, 0));
        alphaTextureCoordinates.add(new DecimalPosition(0, 1));
        // Triangle 2
        alphaTextureCoordinates.add(new DecimalPosition(1, 0));
        alphaTextureCoordinates.add(new DecimalPosition(1, 1));
        alphaTextureCoordinates.add(new DecimalPosition(0, 1));
        return alphaTextureCoordinates;
    }

    public void addParticles(Particle particle) {
        particles.add(particle);
    }
}
