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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Instance<AutonomousParticleEmitter> autonomousParticleEmitterInstance;
    @Inject
    private Instance<DependentParticleEmitter> dependentParticleEmitterInstance;
    private Map<Integer, ParticleEmitterSequenceConfig> particleEmitterSequenceConfigs = new HashMap<>();
    private Map<Integer, ParticleConfig> particleConfigs = new HashMap<>();
    private List<Particle> particles = new ArrayList<>();
    private List<AutonomousParticleEmitter> waitingEmitters = new ArrayList<>();
    private Collection<ParticleEmitter> activeEmitters = new ArrayList<>();
    private long lastTimeStamp;

    @PostConstruct
    public void DELETE_ME() {
        // Particles

        // Fire emitter
        ParticleEmitterSequenceConfig fire = new ParticleEmitterSequenceConfig().setId(1).setInternalName("Fire");
        DependentParticleEmitterConfig dependentParticleEmitterConfig = new DependentParticleEmitterConfig();
        dependentParticleEmitterConfig.setEmittingCount(10).setEmittingDelay(100).setGenerationRandomDistance(3);
        dependentParticleEmitterConfig.setParticleConfig(new ParticleConfig().setTimeToLive(2000).setSpeedX(0.0).setSpeedXRandomPart(4.0).setSpeedY(0.0).setSpeedYRandomPart(4.0).setSpeedZ(10.0));
        List<DependentParticleEmitterConfig> dependentParticleEmitterConfigs = new ArrayList<>();
        dependentParticleEmitterConfigs.add(dependentParticleEmitterConfig);
        fire.setDependent(dependentParticleEmitterConfigs);
        particleEmitterSequenceConfigs.put(fire.getId(), fire);
        /////////////////////////////
        particleEmitterSequenceConfigs.put(2, new ParticleEmitterSequenceConfig().setId(2).setInternalName("Explosion"));
        particleEmitterSequenceConfigs.put(3, new ParticleEmitterSequenceConfig().setId(3).setInternalName("Detonation"));
        particleEmitterSequenceConfigs.put(4, new ParticleEmitterSequenceConfig().setId(4).setInternalName("Muzzle"));
        /////////////////////////////


        // emitter für puff, emitter für fire
        // TODO , bewegender emitter zeugs wegspicken

        // Fire
        // emitterConfigs.add(new ParticleEmitterConfig().setStart(1000).setTtl(200000).setVelocity(new Vertex(0, 0, 0)).setEmittingCount(10).setEmittingDelay(100).setGenerationRandomDistance(3));
        // Puff
        //     emitterConfigs.add(new ParticleEmitterConfig().setStart(500).setTtl(1000).setVelocity(new Vertex(0, 0, 0)).setEmittingCount(20).setEmittingDelay(100).setGenerationRandomDistance(5));


        // OLD
//        emitterConfigs.add(new ParticleEmitterConfig().setStart(1000).setTtl(20000).setVelocity(new Vertex(10, 10, 10)).setEmittingCount(10).setEmittingDelay(100).setGenerationRandomDistance(3));
//        emitterConfigs.add(new ParticleEmitterConfig().setStart(1000).setTtl(20000).setVelocity(new Vertex(10, -10, 10)).setEmittingCount(10).setEmittingDelay(100).setGenerationRandomDistance(3));
//        emitterConfigs.add(new ParticleEmitterConfig().setStart(1000).setTtl(20000).setVelocity(new Vertex(-10, 10, 10)).setEmittingCount(10).setEmittingDelay(100).setGenerationRandomDistance(3));
//        emitterConfigs.add(new ParticleEmitterConfig().setStart(1000).setTtl(20000).setVelocity(new Vertex(-10, -10, 10)).setEmittingCount(10).setEmittingDelay(100).setGenerationRandomDistance(3));
    }

    public void start(long startTime, Vertex position, int dependentParticleEmitterConfigId) {
        ParticleEmitterSequenceConfig sequenceConfig = getParticleEmitterSequenceConfig(dependentParticleEmitterConfigId);
        if (sequenceConfig.getAutonomous() != null) {
            start(startTime, position, sequenceConfig.getAutonomous());
        }
        if (sequenceConfig.getDependent() != null) {
            start(position, sequenceConfig.getDependent());
        }
    }

    public void start(long startTime, Vertex position, List<AutonomousParticleEmitterConfig> autonomousParticleEmitterConfigs) {
        for (AutonomousParticleEmitterConfig autonomousParticleEmitterConfig : autonomousParticleEmitterConfigs) {
            AutonomousParticleEmitter autonomousParticleEmitter = autonomousParticleEmitterInstance.get();
            autonomousParticleEmitter.init(startTime, position, autonomousParticleEmitterConfig);
            waitingEmitters.add(autonomousParticleEmitter);
        }
        waitingEmitters.sort(Comparator.comparingLong(AutonomousParticleEmitter::getStartTimeStamp));
    }

    public void start(Vertex position, List<DependentParticleEmitterConfig> dependentParticleEmitterConfigs) {
        for (DependentParticleEmitterConfig dependentParticleEmitterConfig : dependentParticleEmitterConfigs) {
            DependentParticleEmitter autonomousParticleEmitter = dependentParticleEmitterInstance.get();
            autonomousParticleEmitter.init(position, dependentParticleEmitterConfig);
            activeEmitters.add(autonomousParticleEmitter);
        }
    }

    public List<ModelMatrices> provideModelMatrices(long timestamp) {
        if (lastTimeStamp == 0) {
            lastTimeStamp = timestamp;
            return null;
        }

        double factor = (timestamp - lastTimeStamp) / 1000.0;
        lastTimeStamp = timestamp;
        // Handle emitters
        while (!waitingEmitters.isEmpty() && waitingEmitters.get(0).getStartTimeStamp() < timestamp) {
            activeEmitters.add(waitingEmitters.remove(0));
        }
        activeEmitters.removeIf(particle -> !particle.tick(timestamp));

        // Handle particles
        particles.removeIf(particle -> !particle.tick(timestamp, factor, camera.getMatrix()));
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

    public ParticleEmitterSequenceConfig getParticleEmitterSequenceConfig(int id) {
        ParticleEmitterSequenceConfig particleEmitterSequenceConfig = particleEmitterSequenceConfigs.get(id);
        if (particleEmitterSequenceConfig == null) {
            throw new IllegalArgumentException("No ParticleEmitterSequenceConfig for id: " + id);
        }
        return particleEmitterSequenceConfig;
    }

    public Collection<ParticleEmitterSequenceConfig> getParticleEmitterSequenceConfigs() {
        return particleEmitterSequenceConfigs.values();
    }

    public void clear() {
        waitingEmitters.clear();
        activeEmitters.clear();
        particles.clear();
    }
}
