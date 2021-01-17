package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.particle.AutonomousParticleEmitterConfig;
import com.btxtech.shared.datatypes.particle.DependentParticleEmitterConfig;
import com.btxtech.shared.datatypes.particle.ParticleConfig;
import com.btxtech.shared.datatypes.particle.ParticleEmitterSequenceConfig;
import com.btxtech.shared.datatypes.particle.ParticleShapeConfig;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.datatypes.ModelMatrices;
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
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
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
        particleShapeConfig = new ParticleShapeConfig().id(1).internalName("Fire Particle").edgeLength(3).shadowAlphaCutOff(0.38).alphaOffsetImageId(56).colorRampImageId(57).colorRampXOffsets(new double[]{4.0 / 128.0, 12.0 / 128.0, 20.0 / 128.0}).textureOffsetScope(0.1);

        //-------------------------------------------------------------------------
        // Fire
        ParticleEmitterSequenceConfig fire = new ParticleEmitterSequenceConfig().id(1).internalName("Fire");
        DependentParticleEmitterConfig dependentParticleEmitterConfig = new DependentParticleEmitterConfig();
        dependentParticleEmitterConfig.emittingCount(5).emittingDelay(100).generationRandomDistance(3);
        dependentParticleEmitterConfig.particleConfig(new ParticleConfig().particleShapeConfigId(1).particleXColorRampOffsetIndex(0).timeToLive(1500).particleGrowFrom(1.0).particleGrowTo(1.5).velocity(new Vertex(0, 0, 10)).velocityRandomPart(new Vertex(3, 3, 0)).acceleration(new Vertex(-1, -1, 2)));
        List<DependentParticleEmitterConfig> dependentParticleEmitterConfigs = new ArrayList<>();
        dependentParticleEmitterConfigs.add(dependentParticleEmitterConfig);
        fire.dependent(dependentParticleEmitterConfigs);
        particleEmitterSequenceConfigs.put(fire.getId(), fire);
        //-------------------------------------------------------------------------
        // Smoke
        ParticleEmitterSequenceConfig smoke = new ParticleEmitterSequenceConfig().id(5).internalName("Smoke");
        dependentParticleEmitterConfig = new DependentParticleEmitterConfig();
        dependentParticleEmitterConfig.emittingCount(5).emittingDelay(100).generationRandomDistance(3);
        dependentParticleEmitterConfig.particleConfig(new ParticleConfig().particleShapeConfigId(1).particleXColorRampOffsetIndex(1).timeToLive(1000).particleGrowFrom(1.0).particleGrowTo(1.5).velocity(new Vertex(0, 0, 10)).velocityRandomPart(new Vertex(3, 3, 0)).acceleration(new Vertex(-1, -1, 2)));
        dependentParticleEmitterConfigs = new ArrayList<>();
        dependentParticleEmitterConfigs.add(dependentParticleEmitterConfig);
        smoke.dependent(dependentParticleEmitterConfigs);
        particleEmitterSequenceConfigs.put(smoke.getId(), smoke);
        //-------------------------------------------------------------------------
        // Explosion
        ParticleEmitterSequenceConfig explosion = new ParticleEmitterSequenceConfig().id(2).internalName("Explosion");
        List<AutonomousParticleEmitterConfig> autonomousParticleEmitterConfigs = new ArrayList<>();
        explosion.autonomous(autonomousParticleEmitterConfigs);
        explosion.audioIds(Collections.singletonList(18));
        particleEmitterSequenceConfigs.put(explosion.getId(), explosion);
        // Splitter
        AutonomousParticleEmitterConfig splitter1 = new AutonomousParticleEmitterConfig();
        splitter1.startTime(0).timeToLive(1000).velocity(new Vertex(10, -7, 20)).internalName("Splitter 1");
        splitter1.emittingCount(5).emittingDelay(100).generationRandomDistance(0);
        splitter1.particleConfig(new ParticleConfig().particleShapeConfigId(1).particleXColorRampOffsetIndex(0).timeToLive(1500).velocity(new Vertex(0, 0, 10)).velocityRandomPart(new Vertex(2, 2, 0)));
        autonomousParticleEmitterConfigs.add(splitter1);
        AutonomousParticleEmitterConfig splitter2 = new AutonomousParticleEmitterConfig();
        splitter2.startTime(100).timeToLive(1000).velocity(new Vertex(-10, -10, 25)).internalName("Splitter 2");
        splitter2.emittingCount(5).emittingDelay(100).generationRandomDistance(0);
        splitter2.particleConfig(new ParticleConfig().particleShapeConfigId(1).particleXColorRampOffsetIndex(0).timeToLive(1000).velocity(new Vertex(0, 0, 10)).velocityRandomPart(new Vertex(2, 2, 0)));
        autonomousParticleEmitterConfigs.add(splitter2);
        // Smoke
        AutonomousParticleEmitterConfig smokeExplosion = new AutonomousParticleEmitterConfig();
        smokeExplosion.startTime(0).timeToLive(2000).internalName("Smoke");
        smokeExplosion.emittingCount(10).emittingDelay(100).generationRandomDistance(3);
        smokeExplosion.particleConfig(new ParticleConfig().particleShapeConfigId(1).particleXColorRampOffsetIndex(1).timeToLive(4000).velocity(new Vertex(0, 0, 8)).velocityRandomPart(new Vertex(1, 1, 0)));
        autonomousParticleEmitterConfigs.add(smokeExplosion);
        // Main Puff
        AutonomousParticleEmitterConfig mainPuff = new AutonomousParticleEmitterConfig();
        mainPuff.startTime(0).timeToLive(2000).velocity(new Vertex(0, 0, 5)).internalName("Main Puff");
        mainPuff.emittingCount(20).emittingDelay(100).generationRandomDistance(5);
        mainPuff.particleConfig(new ParticleConfig().particleShapeConfigId(1).particleXColorRampOffsetIndex(2).particleGrowFrom(1.0).particleGrowTo(2.0).timeToLive(2000).velocity(new Vertex(0, 0, 10)).velocityRandomPart(new Vertex(2, 2, 0)).acceleration(new Vertex(0, 0, -3)));
        autonomousParticleEmitterConfigs.add(mainPuff);
        //-------------------------------------------------------------------------
        // Detonation
        ParticleEmitterSequenceConfig detonation = new ParticleEmitterSequenceConfig().id(3).internalName("Detonation");
        detonation.audioIds(Arrays.asList(19, 20));
        autonomousParticleEmitterConfigs = new ArrayList<>();
        detonation.autonomous(autonomousParticleEmitterConfigs);
        particleEmitterSequenceConfigs.put(detonation.getId(), detonation);
        // Splitter
        AutonomousParticleEmitterConfig detonationSplitter1 = new AutonomousParticleEmitterConfig();
        detonationSplitter1.startTime(0).timeToLive(200).velocity(new Vertex(0, 0, 20)).internalName("Detonation Splitter 1");
        detonationSplitter1.emittingCount(3).emittingDelay(100).generationRandomDistance(0);
        detonationSplitter1.particleConfig(new ParticleConfig().particleShapeConfigId(1).particleXColorRampOffsetIndex(0).timeToLive(2000).particleGrowFrom(1.0).particleGrowTo(2.0));
        autonomousParticleEmitterConfigs.add(detonationSplitter1);
        //-------------------------------------------------------------------------
        // Muzzle flash
        ParticleEmitterSequenceConfig muzzleFlash = new ParticleEmitterSequenceConfig().id(4).internalName("Muzzle Flash");
        muzzleFlash.audioIds(Arrays.asList(10, 11));
        autonomousParticleEmitterConfigs = new ArrayList<>();
        muzzleFlash.autonomous(autonomousParticleEmitterConfigs);
        particleEmitterSequenceConfigs.put(muzzleFlash.getId(), muzzleFlash);
        // Splitter
        AutonomousParticleEmitterConfig muzzleFlashSplitter = new AutonomousParticleEmitterConfig();
        muzzleFlashSplitter.startTime(0).timeToLive(200).directionSpeed(20.0).internalName("Muzzle Flash Splitter 1");
        muzzleFlashSplitter.emittingCount(1).emittingDelay(40).generationRandomDistance(0);
        muzzleFlashSplitter.particleConfig(new ParticleConfig().particleShapeConfigId(1).particleXColorRampOffsetIndex(0).timeToLive(1000).particleGrowFrom(0.5).particleGrowTo(2.0));
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
        activeEmitters.removeIf(emitter -> !emitter.tick(timestamp, factor));

        // Handle particles
        particles.removeIf(particle -> !particle.tick(timestamp, factor, camera.getMatrix(), nativeMatrixFactory));
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
        modelMatrices.clear();
    }
}
