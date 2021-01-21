package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.particle.AutonomousParticleEmitterConfig;
import com.btxtech.shared.datatypes.particle.DependentParticleEmitterConfig;
import com.btxtech.shared.datatypes.particle.ParticleEmitterSequenceConfig;
import com.btxtech.shared.datatypes.particle.ParticleShapeConfig;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.Camera;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<ParticleShapeConfig> particleShapeConfigs = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private List<AutonomousParticleEmitter> waitingEmitters = new ArrayList<>();
    private Collection<ParticleEmitter> activeEmitters = new ArrayList<>();
    private long lastTimeStamp;
    private MapList<Integer, ModelMatrices> modelMatrices = new MapList<>();

    public void onGameUiControlInitEvent(@Observes GameUiControlInitEvent gameUiControlInitEvent) {
        particleShapeConfigs.clear();
        particleShapeConfigs.addAll(gameUiControlInitEvent.getColdGameUiContext().getParticleShapeConfigs());
        particleEmitterSequenceConfigs.clear();
        gameUiControlInitEvent.getColdGameUiContext().getParticleEmitterSequenceConfigs()
                .forEach(particleEmitterSequenceConfig -> particleEmitterSequenceConfigs.put(particleEmitterSequenceConfig.getId(), particleEmitterSequenceConfig));
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

        particles.forEach(particle -> modelMatrices.put(particle.getParticleShapeConfigId(), particle.getModelMatrices()));
    }

    public List<ModelMatrices> provideModelMatrices(int id) {
        return modelMatrices.get(id);
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

    public List<ParticleShapeConfig> getParticleShapeConfigs() {
        return particleShapeConfigs;
    }

    public void removeParticleEmitter(ParticleEmitter particleEmitter) {
        activeEmitters.remove(particleEmitter);
    }

    public void editorUpdate(ParticleEmitterSequenceConfig particleEmitterSequenceConfig) {
        particleEmitterSequenceConfigs.put(particleEmitterSequenceConfig.getId(), particleEmitterSequenceConfig);
    }

    public void editorUpdate(ParticleShapeConfig particleShapeConfig) {
        particleShapeConfigs.removeIf(config -> particleShapeConfig.getId() == config.getId());
        particleShapeConfigs.add(particleShapeConfig);
    }

    public void clear() {
        waitingEmitters.clear();
        activeEmitters.clear();
        particles.clear();
        modelMatrices.clear();
    }
}
