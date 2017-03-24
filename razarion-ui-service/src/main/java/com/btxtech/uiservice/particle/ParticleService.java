package com.btxtech.uiservice.particle;

import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.Camera;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
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
    @Inject
    private Camera camera;
    @Inject
    private Instance<AutonomousParticleEmitter> autonomousParticleEmitterInstance;
    @Inject
    private Instance<DependentParticleEmitter> dependentParticleEmitterInstance;
    private Map<Integer, ParticleEmitterSequenceConfig> particleEmitterSequenceConfigs = new HashMap<>();
    private ParticleShapeConfig particleShapeConfig;
    private List<Particle> particles = new ArrayList<>();
    private List<AutonomousParticleEmitter> waitingEmitters = new ArrayList<>();
    private Collection<ParticleEmitter> activeEmitters = new ArrayList<>();
    private long lastTimeStamp;
    private List<ModelMatrices> modelMatrices = new ArrayList<>();

    @PostConstruct
    public void DELETE_ME() {
        // Particles
        particleShapeConfig = new ParticleShapeConfig().setId(1).setInternalName("Fire Particle").setEdgeLength(3).setAlphaOffsetImageId(272951).setColorRampImageId(272952).setColorRampXOffsets(new double[]{4.0 / 128.0, 12.0 / 128.0, 20.0 / 128.0}).setTextureOffsetScope(0.1);

        //-------------------------------------------------------------------------
        // Fire
        ParticleEmitterSequenceConfig fire = new ParticleEmitterSequenceConfig().setId(1).setInternalName("Fire");
        DependentParticleEmitterConfig dependentParticleEmitterConfig = new DependentParticleEmitterConfig();
        dependentParticleEmitterConfig.setEmittingCount(5).setEmittingDelay(100).setGenerationRandomDistance(3);
        dependentParticleEmitterConfig.setParticleConfig(new ParticleConfig().setParticleShapeConfigId(1).setParticleXColorRampOffsetIndex(0).setTimeToLive(1500).setParticleGrowFrom(1.0).setParticleGrowTo(1.5).setVelocity(new Vertex(0, 0, 10)).setVelocityRandomPart(new Vertex(3, 3, 0)).setAcceleration(new Vertex(-1, -1, 2)));
        List<DependentParticleEmitterConfig> dependentParticleEmitterConfigs = new ArrayList<>();
        dependentParticleEmitterConfigs.add(dependentParticleEmitterConfig);
        fire.setDependent(dependentParticleEmitterConfigs);
        particleEmitterSequenceConfigs.put(fire.getId(), fire);
        //-------------------------------------------------------------------------
        // Smoke
        ParticleEmitterSequenceConfig smoke = new ParticleEmitterSequenceConfig().setId(5).setInternalName("Smoke");
        dependentParticleEmitterConfig = new DependentParticleEmitterConfig();
        dependentParticleEmitterConfig.setEmittingCount(5).setEmittingDelay(100).setGenerationRandomDistance(3);
        dependentParticleEmitterConfig.setParticleConfig(new ParticleConfig().setParticleShapeConfigId(1).setParticleXColorRampOffsetIndex(1).setTimeToLive(1000).setParticleGrowFrom(1.0).setParticleGrowTo(1.5).setVelocity(new Vertex(0, 0, 10)).setVelocityRandomPart(new Vertex(3, 3, 0)).setAcceleration(new Vertex(-1, -1, 2)));
        dependentParticleEmitterConfigs = new ArrayList<>();
        dependentParticleEmitterConfigs.add(dependentParticleEmitterConfig);
        smoke.setDependent(dependentParticleEmitterConfigs);
        particleEmitterSequenceConfigs.put(smoke.getId(), smoke);
        //-------------------------------------------------------------------------
        // Explosion
        ParticleEmitterSequenceConfig explosion = new ParticleEmitterSequenceConfig().setId(2).setInternalName("Explosion");
        List<AutonomousParticleEmitterConfig> autonomousParticleEmitterConfigs = new ArrayList<>();
        explosion.setAutonomous(autonomousParticleEmitterConfigs);
        explosion.setAudioIds(Collections.singletonList(284041));
        particleEmitterSequenceConfigs.put(explosion.getId(), explosion);
        // Splitter
        AutonomousParticleEmitterConfig splitter1 = new AutonomousParticleEmitterConfig();
        splitter1.setStartTime(0).setTimeToLive(1000).setVelocity(new Vertex(10, -7, 20)).setInternalName("Splitter 1");
        splitter1.setEmittingCount(5).setEmittingDelay(100).setGenerationRandomDistance(0);
        splitter1.setParticleConfig(new ParticleConfig().setParticleShapeConfigId(1).setParticleXColorRampOffsetIndex(0).setTimeToLive(1500).setVelocity(new Vertex(0, 0, 10)).setVelocityRandomPart(new Vertex(2, 2, 0)));
        autonomousParticleEmitterConfigs.add(splitter1);
        AutonomousParticleEmitterConfig splitter2 = new AutonomousParticleEmitterConfig();
        splitter2.setStartTime(100).setTimeToLive(1000).setVelocity(new Vertex(-10, -10, 25)).setInternalName("Splitter 2");
        splitter2.setEmittingCount(5).setEmittingDelay(100).setGenerationRandomDistance(0);
        splitter2.setParticleConfig(new ParticleConfig().setParticleShapeConfigId(1).setParticleXColorRampOffsetIndex(0).setTimeToLive(1000).setVelocity(new Vertex(0, 0, 10)).setVelocityRandomPart(new Vertex(2, 2, 0)));
        autonomousParticleEmitterConfigs.add(splitter2);
        // Smoke
        AutonomousParticleEmitterConfig smokeExplosion = new AutonomousParticleEmitterConfig();
        smokeExplosion.setStartTime(0).setTimeToLive(2000).setInternalName("Smoke");
        smokeExplosion.setEmittingCount(10).setEmittingDelay(100).setGenerationRandomDistance(3);
        smokeExplosion.setParticleConfig(new ParticleConfig().setParticleShapeConfigId(1).setParticleXColorRampOffsetIndex(1).setTimeToLive(4000).setVelocity(new Vertex(0, 0, 8)).setVelocityRandomPart(new Vertex(1, 1, 0)));
        autonomousParticleEmitterConfigs.add(smokeExplosion);
        // Main Puff
        AutonomousParticleEmitterConfig mainPuff = new AutonomousParticleEmitterConfig();
        mainPuff.setStartTime(0).setTimeToLive(2000).setVelocity(new Vertex(0, 0, 5)).setInternalName("Main Puff");
        mainPuff.setEmittingCount(20).setEmittingDelay(100).setGenerationRandomDistance(5);
        mainPuff.setParticleConfig(new ParticleConfig().setParticleShapeConfigId(1).setParticleXColorRampOffsetIndex(2).setParticleGrowFrom(1.0).setParticleGrowTo(2.0).setTimeToLive(2000).setVelocity(new Vertex(0, 0, 10)).setVelocityRandomPart(new Vertex(2, 2, 0)).setAcceleration(new Vertex(0, 0, -3)));
        autonomousParticleEmitterConfigs.add(mainPuff);
        //-------------------------------------------------------------------------
        // Detonation
        ParticleEmitterSequenceConfig detonation = new ParticleEmitterSequenceConfig().setId(3).setInternalName("Detonation");
        detonation.setAudioIds(Arrays.asList(284042, 284043));
        autonomousParticleEmitterConfigs = new ArrayList<>();
        detonation.setAutonomous(autonomousParticleEmitterConfigs);
        particleEmitterSequenceConfigs.put(detonation.getId(), detonation);
        // Splitter
        AutonomousParticleEmitterConfig detonationSplitter1 = new AutonomousParticleEmitterConfig();
        detonationSplitter1.setStartTime(0).setTimeToLive(200).setVelocity(new Vertex(0, 0, 20)).setInternalName("Detonation Splitter 1");
        detonationSplitter1.setEmittingCount(3).setEmittingDelay(100).setGenerationRandomDistance(0);
        detonationSplitter1.setParticleConfig(new ParticleConfig().setParticleShapeConfigId(1).setParticleXColorRampOffsetIndex(0).setTimeToLive(2000).setParticleGrowFrom(1.0).setParticleGrowTo(2.0));
        autonomousParticleEmitterConfigs.add(detonationSplitter1);
        //-------------------------------------------------------------------------
        // Muzzle flash
        ParticleEmitterSequenceConfig muzzleFlash = new ParticleEmitterSequenceConfig().setId(4).setInternalName("Muzzle Flash");
        muzzleFlash.setAudioIds(Arrays.asList(272523, 272524));
        autonomousParticleEmitterConfigs = new ArrayList<>();
        muzzleFlash.setAutonomous(autonomousParticleEmitterConfigs);
        particleEmitterSequenceConfigs.put(muzzleFlash.getId(), muzzleFlash);
        // Splitter
        AutonomousParticleEmitterConfig muzzleFlashSplitter = new AutonomousParticleEmitterConfig();
        muzzleFlashSplitter.setStartTime(0).setTimeToLive(200).setDirectionSpeed(20.0).setInternalName("Muzzle Flash Splitter 1");
        muzzleFlashSplitter.setEmittingCount(1).setEmittingDelay(40).setGenerationRandomDistance(0);
        muzzleFlashSplitter.setParticleConfig(new ParticleConfig().setParticleShapeConfigId(1).setParticleXColorRampOffsetIndex(0).setTimeToLive(1000).setParticleGrowFrom(0.5).setParticleGrowTo(2.0));
        autonomousParticleEmitterConfigs.add(muzzleFlashSplitter);
    }

    public ParticleEmitterSequenceHandler start(long timestamp, Vertex position, Vertex direction, ParticleEmitterSequenceConfig dependentParticleEmitterConfig) {
        if (dependentParticleEmitterConfig.getAutonomous() != null) {
            start(timestamp, position, direction, dependentParticleEmitterConfig.getAutonomous());
        }
        ParticleEmitterSequenceHandler handler = null;
        if (dependentParticleEmitterConfig.getDependent() != null) {
            handler = new ParticleEmitterSequenceHandler(this);
            start(position, dependentParticleEmitterConfig.getDependent(), handler);
        }
        return handler;
    }

    private void start(long timestamp, Vertex position, Vertex direction, List<AutonomousParticleEmitterConfig> autonomousParticleEmitterConfigs) {
        for (AutonomousParticleEmitterConfig autonomousParticleEmitterConfig : autonomousParticleEmitterConfigs) {
            AutonomousParticleEmitter autonomousParticleEmitter = autonomousParticleEmitterInstance.get();
            autonomousParticleEmitter.init(timestamp, position, direction, autonomousParticleEmitterConfig);
            waitingEmitters.add(autonomousParticleEmitter);
        }
        waitingEmitters.sort(Comparator.comparingLong(AutonomousParticleEmitter::getStartTimeStamp));
    }

    private void start(Vertex position, List<DependentParticleEmitterConfig> dependentParticleEmitterConfigs, ParticleEmitterSequenceHandler handler) {
        for (DependentParticleEmitterConfig dependentParticleEmitterConfig : dependentParticleEmitterConfigs) {
            DependentParticleEmitter dependentParticleEmitter = dependentParticleEmitterInstance.get();
            dependentParticleEmitter.init(position, dependentParticleEmitterConfig);
            activeEmitters.add(dependentParticleEmitter);
            handler.addDependentParticleEmitter(dependentParticleEmitter);
        }
    }

    public void preRender(long timestamp) {
        modelMatrices.clear();
        if (lastTimeStamp == 0) {
            lastTimeStamp = timestamp;
            return;
        }

        double factor = (timestamp - lastTimeStamp) / 1000.0;
        lastTimeStamp = timestamp;
        // Handle emitters
        while (!waitingEmitters.isEmpty() && waitingEmitters.get(0).getStartTimeStamp() < timestamp) {
            activeEmitters.add(waitingEmitters.remove(0));
        }
        activeEmitters.removeIf(particle -> !particle.tick(timestamp, factor));

        // Handle particles
        particles.removeIf(particle -> !particle.tick(timestamp, factor, camera.getMatrix()));
        Collections.sort(particles);

        modelMatrices = particles.stream().map(Particle::getModelMatrices).collect(Collectors.toList());
    }

    public List<ModelMatrices> provideModelMatrices() {
        return modelMatrices;
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

    public ParticleShapeConfig getParticleShapeConfig() {
        return particleShapeConfig;
    }

    public void removeParticleEmitter(ParticleEmitter particleEmitter) {
        activeEmitters.remove(particleEmitter);
    }

    public void clear() {
        waitingEmitters.clear();
        activeEmitters.clear();
        particles.clear();
    }
}
